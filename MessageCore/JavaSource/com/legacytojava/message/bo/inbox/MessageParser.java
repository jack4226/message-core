package com.legacytojava.message.bo.inbox;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bean.BodypartBean;
import com.legacytojava.message.bean.BodypartUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageNode;
import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.bo.rule.RuleBase;
import com.legacytojava.message.bo.rule.RuleLoader;
import com.legacytojava.message.bo.rule.RuleMatcher;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.idtokens.EmailIdParser;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.dao.outbox.MsgRenderedDao;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.MsgInboxVo;
import com.legacytojava.message.vo.outbox.MsgRenderedVo;

/**
 * Scan email header and body, and match rules to determine the ruleName.
 */
@Component("messageParser")
@Scope(value="prototype")
public final class MessageParser {
	static final Logger logger = Logger.getLogger(MessageParser.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private final RfcCodeScan rfcScan;
	private final RuleMatcher ruleMatcher;
	static final String TEN_DASHES = "----------";
	static final String ORIGMSG_SEPARATOR = "-----Original Message-----";
	static final String REPLY_SEPARATOR = "---------Reply Separator---------";
	static final String LF = System.getProperty("line.separator", "\n");

	@Autowired
	private RuleLoader ruleLoader;
	@Autowired
	private EmailAddrDao emailAddrDao;
	@Autowired
	private MsgInboxDao msgInboxDao;
	@Autowired
	private MsgRenderedDao msgRenderedDao;
	
	/**
	 * default constructor
	 */
	public MessageParser() throws IOException {
		rfcScan = RfcCodeScan.getInstance();
		ruleMatcher = new RuleMatcher();
	}

	/**
	 * Scans email properties to find out the ruleName. It also checks VERP
	 * headers and returned Email_Id to get original recipient.
	 * 
	 * @param msgBean
	 *            a MessageBean instance
	 */
	public String parse(MessageBean msgBean) {
		if (isDebugEnabled)
			logger.debug("Entering parse() method...");
		if (msgBean.getMsgRefId() == null) {
			try {
				// Search Email Id from body, Email_Id X-header and VERP X-header
				Long emailId = parseEmailId(msgBean);
				if (emailId != null) {
					msgBean.setMsgRefId(emailId);
				}
			}
			catch (Exception e) {
				logger.warn("parse() - Invalid Email_Id returned from parseEmailId: " + e);
			}
		}
		else { // MsgRefId could be set from AssignRuleNameBo, and etc.
			logger.warn("parse() - MsgRefId already exist: " + msgBean.getMsgRefId());
		}
		
		String ruleName = null;
		
		/*
		 * Rule Name could be assigned before entering this method. If the
		 * preassigned rule name is an internal rule, use it directly.
		 */
		if (msgBean.getRuleName() != null) {
			for (RuleNameType name : RuleNameType.values()) {
				if (name.toString().equals(msgBean.getRuleName())) {
					ruleName = name.toString();
					break;
				}
			}
		}

		// retrieve attachments into an array, it also gathers rfc822/Delivery Status.
		List<MessageNode> subNodes = BodypartUtil.retrieveAttachments(msgBean);
		if (subNodes != null && subNodes.size() != msgBean.getAttachCount()) {
			logger.warn("Attachment count from MailReader (" + msgBean.getAttachCount()
					+ ") is different from the count from RuleEngine (" + subNodes.size() + ")");
		}

		List<RuleBase> preRules = ruleLoader.getPreRuleSet(); // always check rule changes.
		// match pre-rfc-scan rules
		if (ruleName == null) {
			ruleName = ruleMatcher.match(msgBean, preRules, ruleLoader.getSubRuleSet());
		}
		// end of pre-rfc-scan rules

		// scan message for Enhanced Mail System Status Code (rfc1893/rfc3464)
		BodypartBean aNode = null;
		if (msgBean.getReport() != null) {
			/*
			 * multipart/report mime type is present, retrieve DSN/MDN report.
			 * execute this logic even a rule name is matched during pre-scan. 
			 */
			MessageNode mNode = msgBean.getReport();
			// locate message/delivery-status section
			aNode = BodypartUtil.retrieveDlvrStatus(mNode.getBodypartNode(), mNode.getLevel());
			if (aNode != null) {
				// first scan message/delivery-status
				byte[] attchValue = (byte[]) aNode.getValue();
				if (attchValue != null) {
					if (isDebugEnabled) {
						logger.debug("parse() - scan message/report status -----<" + LF
								+ new String(attchValue) + ">-----");
					}
					if (ruleName == null) {
						ruleName = rfcScan.examineBody(new String(attchValue));
					}
					parseDsn(attchValue, msgBean);
					msgBean.setDsnDlvrStat(new String(attchValue));
				}
			}
			else if ((aNode = BodypartUtil.retrieveMDNReceipt(mNode.getBodypartNode(), mNode
					.getLevel())) != null) {
				// got message/disposition-notification
				byte[] attchValue = (byte[]) aNode.getValue();
				if (attchValue != null) {
					if (isDebugEnabled) {
						logger.debug("parse() - display message/report status -----<" + LF
								+ new String(attchValue) + ">-----");
					}
					if (ruleName == null) {
						ruleName = RuleNameType.MDN_RECEIPT.toString();
					}
					// MDN comes with original and final recipients
					parseDsn(attchValue, msgBean);
					msgBean.setDsnDlvrStat(new String(attchValue));
				}
			}
			else {
				// missing message/* section, try text/plain
				List<BodypartBean> nodes = BodypartUtil.retrieveReportText(mNode
						.getBodypartNode(), mNode.getLevel());
				if (!nodes.isEmpty()) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
					for (BodypartBean bodyPart : nodes) {
						byte[] attchValue = (byte[]) bodyPart.getValue();
						try {
							baos.write(attchValue);
						}
						catch (IOException e) {
							logger.error("IOException caught", e);
						}
					}
					try {
						baos.close();
					}
					catch (IOException e) {}
					byte[] attchValue = baos.toByteArray();
					if (attchValue != null) {
						if (isDebugEnabled) {
							logger.debug("parse() - scan message/report text -----<" + LF
									+ new String(attchValue) + ">-----");
						}
						if (ruleName == null) {
							ruleName = rfcScan.examineBody(new String(attchValue));
						}
						parseDsn(attchValue, msgBean);
						msgBean.setDsnText(new String(attchValue));
					}
				}
			}
			// locate possible message/rfc822 section under multipart/report
			aNode = BodypartUtil.retrieveMessageRfc822(mNode.getBodypartNode(), mNode.getLevel());
			if (aNode != null && msgBean.getRfc822() == null) {
				msgBean.setRfc822(new MessageNode(aNode, mNode.getLevel()));
			}
			// locate possible text/rfc822-headers section under multipart/report
			aNode = BodypartUtil.retrieveRfc822Headers(mNode.getBodypartNode(), mNode.getLevel());
			if (aNode != null && msgBean.getRfc822() == null) {
				msgBean.setRfc822(new MessageNode(aNode, mNode.getLevel()));
			}
		}

		if (msgBean.getRfc822() != null) {
			/*
			 * message/rfc822 is present, retrieve RFC report.
			 * execute this logic even a rule name is matched during pre-scan.
			 */
			MessageNode mNode = msgBean.getRfc822();
			aNode = BodypartUtil.retrieveRfc822Text(mNode.getBodypartNode(), mNode.getLevel());
			if (aNode != null) {
				StringBuffer sb = new StringBuffer();
				// get original message headers
				List<MsgHeader> vheader = aNode.getHeaders();
				for (int i = 0; vheader != null && i < vheader.size(); i++) {
					MsgHeader header = vheader.get(i);
					sb.append(header.getName() + ": " + header.getValue() + LF);
				}
				boolean foundAll = false;
				String rfcHeaders = sb.toString();
				if (!StringUtil.isEmpty(rfcHeaders)) {
					// rfc822 headers
					if (isDebugEnabled) {
						logger.debug("parse() - scan rfc822 headers -----<" + LF + rfcHeaders
								+ ">-----");
					}
					foundAll = parseRfc(rfcHeaders, msgBean);
					msgBean.setDsnRfc822(rfcHeaders);
				}
				byte[] attchValue = (byte[]) aNode.getValue();
				if (attchValue != null) {
					// rfc822 text
					String rfcText = new String(attchValue);
					sb.append(rfcText);
					String mtype = aNode.getMimeType();
					if (mtype.startsWith("text/") || mtype.startsWith("message/")) {
						if (foundAll == false) {
							if (isDebugEnabled) {
								logger.debug("parse() - scan rfc822 text -----<" + LF + rfcText
										+ ">-----");
							}
							parseRfc(rfcText, msgBean);
							msgBean.setDsnRfc822(sb.toString());
						}
					}
					if (msgBean.getDsnText() == null) {
						msgBean.setDsnText(rfcText);
					}
					else {
						msgBean.setDsnText(msgBean.getDsnText() + LF + LF + "RFC822 Text:" + LF
								+ rfcText);
					}
				}
				if (ruleName == null) {
					ruleName = rfcScan.examineBody(sb.toString());
				}
			}
		} // end of RFC Scan

		String body = msgBean.getBody();
		if (msgBean.getRfc822() != null && ruleName == null) {
			// message/rfc822 is present, scan message body for rfc1893 status code
			// TODO: may cause false positives. need to revisit this.
			if (isDebugEnabled)
				logger.debug("parse() - scan body text -----<" + LF + body + ">-----");
			ruleName = rfcScan.examineBody(body);
		}

		// check CC/BCC
		if (ruleName == null) {
			// if the "real_to" address is not found in envelope, but is
			// included in CC or BCC: set ruleName to CC_USER
			for (int i = 0; msgBean.getTo() != null && i < msgBean.getTo().length; i++) {
				Address to = msgBean.getTo()[i];
				if (containsNoAddress(msgBean.getToEnvelope(), to)) {
					if (containsAddress(msgBean.getCc(), to)
							|| containsAddress(msgBean.getBcc(), to)) {
						ruleName = RuleNameType.CC_USER.toString();
						break;
					}
				}
			}
		}

		// match main rules
		if (ruleName == null) {
			List<RuleBase> rules = ruleLoader.getRuleSet();
			Map<String, List<RuleBase>> subRules = ruleLoader.getSubRuleSet();
			// matching custom rules
			ruleName = ruleMatcher.match(msgBean, rules, subRules);
		}

		// check VERP bounce address, set rule name to SOFT_BOUNCE if VERP recipient found
		List<MsgHeader> headers = msgBean.getHeaders();
		for (MsgHeader header : headers) {
			if (Constants.VERP_BOUNCE_ADDR_XHEADER.equals(header.getName())) {
				logger.info("parse() - VERP Recipient found: ==>" + header.getValue() + "<==");
				if (msgBean.getOrigRcpt() != null && !StringUtil.isEmpty(header.getValue())
						&& !msgBean.getOrigRcpt().equalsIgnoreCase(header.getValue())) {
					logger.warn("parse() - replace original recipient: " + msgBean.getOrigRcpt()
							+ " with VERP recipient: " + header.getValue());
				}
				if (!StringUtil.isEmpty(header.getValue())) {
					// VERP Bounce - always override
					msgBean.setOrigRcpt(header.getValue());
				}
				else {
					logger.warn("parse() - " + Constants.VERP_BOUNCE_ADDR_XHEADER
							+ " Header found, but it has no value.");
				}
				if (ruleName == null) {
					// a bounced mail shouldn't have Return-Path
					String rPath = msgBean.getReturnPath() == null ? "" : msgBean.getReturnPath();
					if (StringUtil.isEmpty(rPath) || "<>".equals(rPath.trim())) {
						ruleName = RuleNameType.SOFT_BOUNCE.toString();
					}
				}
				break;
			}
		}

		// find client id by matching Email's TO address to addresses from
		// Client records (Send E-mails Return-Path) and EmailTemplate records
		// (Mailing List address)
		String clientId = ruleLoader.findClientIdByAddr(msgBean.getToAsString());
		if (clientId != null) {
			if (isDebugEnabled)
				logger.debug("parse() - Client Id found by matching TO address: " + clientId);
			if (StringUtil.isEmpty(msgBean.getClientId())) {
				msgBean.setClientId(clientId);
			}
			else if (!clientId.equals(msgBean.getClientId())) {
				logger.warn("parse() - Client Id from TO: " + clientId + " is different from"
						+ " MessageBean's existing Client Id: " + msgBean.getClientId());
			}
		}

		// do we have the EmailId that links to original "sent" message?
		if (msgBean.getMsgRefId() != null && msgBean.getIsReceived()) {
			// yes, retrieve the final recipient from original "sent" message
			EmailAddrVo addrVo = emailAddrDao.getToByMsgRefId(msgBean.getMsgRefId());
			if (addrVo != null) {
				if (RuleNameType.SEND_MAIL.toString().equals(addrVo.getRuleName())) {
					// only if the original message is an "sent" message
					if (StringUtil.isEmpty(msgBean.getFinalRcpt())
							&& StringUtil.isEmpty(msgBean.getOrigRcpt())) {
						// and nothing was found from delivery status or rfc822
						msgBean.setFinalRcpt(addrVo.getEmailAddr());
					}
				}
			}
			else {
				logger.warn("parse() - EmailAddr record not found by MsgRefId: "
						+ msgBean.getMsgRefId());
			}
			// find original client id
			MsgInboxVo origVo = msgInboxDao.getByPrimaryKey(msgBean.getMsgRefId());
			if (origVo == null) { // could be deleted by User or purged by Purge routine
				logger.warn("parse() - MsgInbox record not found by MsgId: "
						+ msgBean.getMsgRefId());
			}
			else {
				if (StringUtil.isEmpty(msgBean.getClientId())) {
					msgBean.setClientId(origVo.getClientId());
				}
				if (StringUtil.isEmpty(msgBean.getCustId())) {
					msgBean.setCustId(origVo.getCustId());
				}
				if (origVo.getRenderId() != null) {
					MsgRenderedVo vo = msgRenderedDao.getByPrimaryKey(origVo.getRenderId());
					if (vo != null) {
						msgBean.setMsgSourceId(vo.getMsgSourceId());
					}
				}
			}
		}

		// if it's hard or soft bounce and no final recipient was found, scan
		// message body for final recipient using known patterns.
		if (RuleNameType.HARD_BOUNCE.toString().equals(ruleName)
				|| RuleNameType.SOFT_BOUNCE.toString().equals(ruleName)) {
			if (StringUtil.isEmpty(msgBean.getFinalRcpt())
					&& StringUtil.isEmpty(msgBean.getOrigRcpt())) {
				String finalRcpt = BounceAddressFinder.getInstance().find(body);
				if (!StringUtil.isEmpty(finalRcpt)) {
					logger.info("parse() - Final Recipient found from message body: " + finalRcpt);
					msgBean.setFinalRcpt(finalRcpt);
				}
			}
		}

		// match post-rules
		if (ruleName != null) {
			msgBean.setRuleName(ruleName); // post rules may evaluate RuleName
			List<RuleBase> postRules = ruleLoader.getPostRuleSet();
			Map<String, List<RuleBase>> subRules = ruleLoader.getSubRuleSet();
			String post_ruleName = ruleMatcher.match(msgBean, postRules, subRules);
			if (post_ruleName != null) {
				ruleName = post_ruleName;
			}
		}

		if (ruleName == null) { // use default
			ruleName = RuleNameType.GENERIC.toString();
		}

		msgBean.setRuleName(ruleName);
		logger.info("parse() - RuleName: " + ruleName);

		return ruleName;
	}

	private boolean containsAddress(Address[] addrs, Address to) {
		if (to != null && addrs != null && addrs.length > 0) {
			for (int i = 0; i < addrs.length; i++) {
				if (to.equals(addrs[i])) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean containsNoAddress(Address[] addrs, Address to) {
		if (to != null && addrs != null && addrs.length > 0) {
			for (int i = 0; i < addrs.length; i++) {
				if (to.equals(addrs[i])) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Parse the message/delivery-status to retrieve DSN fields. Also used by
	 * message/disposition-notification to retrieve final recipient.
	 * 
	 * @param attchValue -
	 *            delivery status text
	 * @param msgBean -
	 *            MessageBean object
	 */
	private void parseDsn(byte[] attchValue, MessageBean msgBean) {
		// retrieve Final-Recipient, Action, and Status
		ByteArrayInputStream bais = new ByteArrayInputStream(attchValue);
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				if (isDebugEnabled)
					logger.debug("parseDsn() - Line: " + line);
				line = line.trim();
				if (line.toLowerCase().startsWith("final-recipient:")) {
					// "Final-Recipient" ":" address-type ";" generic-address
					// address-type = rfc822 / unknown
					StringTokenizer st = new StringTokenizer(line, " ;");
					while (st.hasMoreTokens()) {
						String token = st.nextToken().trim();
						if (token.indexOf("@") > 0) {
							msgBean.setFinalRcpt(token);
							logger.info("parseDsn() - Final_Recipient found: ==>" + token + "<==");
							break;
						}
					}
				}
				else if (line.toLowerCase().startsWith("original-recipient:")) {
					// "Original-Recipient" ":" address-type ";" generic-address
					StringTokenizer st = new StringTokenizer(line, " ;");
					while (st.hasMoreTokens()) {
						String token = st.nextToken().trim();
						if (token.indexOf("@") > 0) {
							msgBean.setOrigRcpt(token);
							logger.info("parseDsn() - Original_Recipient found: ==>" + token
									+ "<==");
							break;
						}
					}
				}
				else if (line.toLowerCase().startsWith("action:")) {
					/**
					 * "Action" ":" action-value = 
					 * 1) failed - could not be delivered to the recipient.
					 * 2) delayed - the reporting MTA has so far been unable to deliver
					 * 	or relay the message.
					 * 3) delivered - the message was successfully delivered.
					 * 4) relayed - the message has been relayed or gatewayed.
					 * 5) expanded - delivered and forwarded by reporting MTA to multiple
					 * 	additional recipient addresses.
					 */ 
					String action = line.substring(7).trim();
					msgBean.setDsnAction(action);
					if (isDebugEnabled)
						logger.debug("parseDsn() - Action found: ==>" + action + "<==");
				}
				else if (line.toLowerCase().startsWith("status:")) {
					// "Status" ":" status-code (digit "." 1*3digit "." 1*3 digit)
					String status = line.substring(7).trim();
					if (status.indexOf(" ") > 0) {
						status = status.substring(0, status.indexOf(" "));
					}
					msgBean.setDsnStatus(status);
					if (isDebugEnabled)
						logger.debug("parseDsn() - Status found: ==>" + status + "<==");
				}
				else if (line.toLowerCase().startsWith("diagnostic-code:")) {
					// "Diagnostic-Code" ":" diagnostic-code
					String diagcode = line.substring(16).trim();
					msgBean.setDiagnosticCode(diagcode);
					if (isDebugEnabled)
						logger.debug("parseDsn() - Diagnostic-Code: found: ==>" + diagcode + "<==");
				}
			}
		}
		catch (IOException e) {
			logger.error("IOException caught during parseDsn()", e);
		}
	}

	/**
	 * parse message/rfc822 to retrieve original email properties: final
	 * recipient, original subject and original SMTP message-id.
	 * 
	 * @param rfc_text -
	 *            rfc822 text
	 * @param msgBean -
	 *            MessageBean object
	 * @return true if all three properties were found
	 */
	private boolean parseRfc(String rfc_text, MessageBean msgBean) {
		// retrieve original To address
		ByteArrayInputStream bais = new ByteArrayInputStream(rfc_text.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		int lineCount = 0;
		boolean gotToAddr = false, gotSubj = false, gotSmtpId = false;
			// allows to quit scan once all three headers are found
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				if (isDebugEnabled)
					logger.debug("parseRfc() - Line: " + line);
				line = line.trim();
				if (line.toLowerCase().startsWith("to:")) {
					// "To" ":" generic-address
					String token = line.substring(3).trim();
					if (StringUtil.isEmpty(msgBean.getFinalRcpt())) {
						msgBean.setFinalRcpt(token);
					}
					else if (EmailAddrUtil.compareEmailAddrs(msgBean.getFinalRcpt(), token) != 0) {
						logger.error("parseRfc() - Final_Rcpt from RFC822: " + token
								+ " is different from DSN's: " + msgBean.getFinalRcpt());
					}
					logger.info("parseRfc() - Final_Recipient(RFC822 To) found: ==>" + token
							+ "<==");
					gotToAddr = true;
				}
				else if (line.toLowerCase().startsWith("subject:")) {
					// "Subject" ":" subject text
					String token = line.substring(8).trim();
					if (StringUtil.isEmpty(msgBean.getOrigSubject())) {
						msgBean.setOrigSubject(token);
					}
					logger.info("parseRfc() - Original_Subject(RFC822 To) found: ==>" + token
							+ "<==");
					gotSubj = true;
				}
				else if (line.toLowerCase().startsWith("message-id:")) {
					// "Message-Id" ":" SMTP message id
					String token = line.substring(11).trim();
					if (StringUtil.isEmpty(msgBean.getSmtpMessageId())) {
						msgBean.setRfcMessageId(token);
					}
					logger.info("parseRfc() - Smtp Message-Id(RFC822 To) found: ==>" + token
							+ "<==");
					gotSmtpId = true;
				}
				if (gotToAddr && gotSubj && gotSmtpId) {
					return true;
				}
				if (++lineCount > 100 && line.indexOf(":") < 0) {
					break; // check if it's a header after 100 lines
				}
			} // end of while
		}
		catch (IOException e) {
			logger.error("IOException caught during parseRfc()", e);
		}
		return false;
	}

	/**
	 * parses email body and X-Header for Email Id.
	 * 
	 * @param msgBean
	 *            MessageBean instance containing email properties
	 * @return Email Id extracted or null.
	 * @throws NumberFormatException
	 *             if Email Id is invalid
	 */
	private Long parseEmailId(MessageBean msgBean) throws NumberFormatException {
		String emailId = null;
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		
		// look up email id from message body, example:
		// System Email Id: 10.1234567890.0
		try {
			emailId = parser.parseMsg(msgBean.getBody());
		}
		catch (NumberFormatException e) {
			logger.error("NumberFormatException caught", e);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			logger.error("ArrayIndexOutOfBoundsException caught", e);
		}

		if (emailId == null) {
			// look up X-Headers for email id
			try {
				emailId = parser.parseHeaders(msgBean.getHeaders());
			}
			catch (NumberFormatException e) {
				logger.error("NumberFormatException caught", e);
			}
		}
		
		Long msgRefId = null;
		if (emailId != null) {
			try {
				msgRefId = Long.valueOf(emailId);
				// message id found
				logger.info("parseEmailId() - MsgRefId found: " + msgRefId);
			}
			catch (Exception e) { // should never happen
				logger.error("parseEmailId() - Programming Error, invalid EmailId: " + emailId
						+ ", please investigate", e);
				throw new NumberFormatException(e.toString());
			}
		}

		return msgRefId;
	}

	public RuleLoader getRuleLoader() {
		return ruleLoader;
	}

	public EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}

	public MsgInboxDao getMsgInboxDao() {
		return msgInboxDao;
	}

	public MsgRenderedDao getMsgRenderedDao() {
		return msgRenderedDao;
	}

	public static void main(String[] args) {
		try {
			MessageParser parser = (MessageParser) SpringUtil.getAppContext().getBean("messageParser");
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
				mBean.setTo(InternetAddress.parse("abc@domain.com", false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject("A Exception occured");
			mBean.setValue(new Date()+ " 5.2.2 Invalid user account.");
			mBean.setMailboxUser("testUser");
			String ruleName = parser.parse(mBean);
			System.out.println("### RuleName: " + ruleName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
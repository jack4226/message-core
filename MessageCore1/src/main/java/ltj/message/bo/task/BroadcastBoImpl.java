package ltj.message.bo.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.HtmlConverter;
import ltj.message.bean.MessageBean;
import ltj.message.bo.template.RenderUtil;
import ltj.message.constant.MobileCarrier;
import ltj.message.constant.VariableName;
import ltj.message.dao.customer.CustomerDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.dao.inbox.MsgClickCountDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.PhoneNumberUtil;
import ltj.message.vo.CustomerVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;
import ltj.message.vo.emailaddr.TemplateRenderVo;

@Component("broadcastBo")
@Scope(value="prototype")
@Lazy(value=true)
public class BroadcastBoImpl extends TaskBaseAdaptor {
	static final Logger logger = LogManager.getLogger(BroadcastBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailingListDao mailingListDao;
	@Autowired
	private EmailSubscrptDao emailSubscrptDao;
	@Autowired
	private MsgClickCountDao msgClickCountDao;
	@Autowired
	private CustomerDao customerDao;

	/**
	 * Send the email to the addresses on the Mailing List.
	 * 
	 * @param msgBean -
	 *            message to broadcast
	 * @return a Long value representing number of addresses the message has
	 *         been sent to.
	 */
	public Long process(MessageBean msgBean) throws DataValidationException, AddressException, JMSException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (msgBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (msgBean.getMsgId()==null) {
			throw new DataValidationException("MsgId is null");
		}
		if (!RuleNameEnum.BROADCAST.name().equals(msgBean.getRuleName())) {
			throw new DataValidationException("Invalid Rule Name: " + msgBean.getRuleName());
		}
		if (getArgumentList(taskArguments).size() > 0) {
			// mailing list from MessageBean takes precedence
			if (msgBean.getMailingListId() == null) {
				msgBean.setMailingListId(taskArguments[0]);
			}
		}
		if (msgBean.getMailingListId() == null) {
			throw new DataValidationException("Mailing List was not provided.");
		}
		
		long mailsSent = 0;
		Boolean saveEmbedEmailId = msgBean.getEmBedEmailId();
		String listId = msgBean.getMailingListId();
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List " + listId + " not found.");
		}
		else if (!listVo.isActive()) {
			logger.warn("MailingList " + listId + " is not active.");
			return Long.valueOf(0);
		}
		String _from = listVo.getEmailAddr();
		String dispName = listVo.getDisplayName();
		if (dispName != null && dispName.trim().length() > 0) {
			_from = dispName + "<" + _from + ">";
		}
		logger.info("Broadcasting to Mailing List: " + listId + ", From: " + _from);
		Address[] from = InternetAddress.parse(_from);
		// set FROM to list address
		msgBean.setFrom(from);
		// get message body from body node
		String bodyText = null;
		if (msgBean.getBodyNode() != null) {
			bodyText = new String(msgBean.getBodyNode().getValue());
		}
		if (bodyText == null) {
			throw new DataValidationException("Message body is empty.");
		}
		msgClickCountDao.updateStartTime(msgBean.getMsgId());
		// extract variables from message body
		List<String> varNames = RenderUtil.retrieveVariableNames(bodyText);
		if (isDebugEnabled) {
			logger.debug("Body VariableType names: " + varNames);
		}
		// extract variables from message subject
		String subjText = msgBean.getSubject() == null ? "" : msgBean.getSubject();
		List<String> subjVarNames = RenderUtil.retrieveVariableNames(subjText);
		if (!subjVarNames.isEmpty()) {
			varNames.addAll(subjVarNames);
			if (isDebugEnabled) {
				logger.debug("Subject VariableType names: " + subjVarNames);
			}
		}
		// get subscribers
		List<EmailSubscrptVo> subs = null;
		if (msgBean.getToCustomersOnly()) {
			subs = emailSubscrptDao.getSubscribersWithCustomerRecord(listId);
		}
		else if (msgBean.getToProspectsOnly()) {
			subs = emailSubscrptDao.getSubscribersWithoutCustomerRecord(listId);
		}
		else {
			subs = emailSubscrptDao.getSubscribers(listId);
		}
		// sending email to each subscriber
		setTargetToMailSender();
		for (EmailSubscrptVo sub : subs) {
			mailsSent += constructAndSendMessage(msgBean, sub, listVo, bodyText, subjVarNames, saveEmbedEmailId, false);
			if (Boolean.TRUE.equals(listVo.getIsSendText())) {
				mailsSent += constructAndSendMessage(msgBean, sub, listVo, bodyText, subjVarNames, saveEmbedEmailId, true);
			}
		}
		if (mailsSent > 0 && msgBean.getMsgId() != null) {
			// update sent count to the Broadcasted message
			msgClickCountDao.updateSentCount(msgBean.getMsgId(), (int) mailsSent);
		}
		return Long.valueOf(mailsSent);
	}
	
	private int constructAndSendMessage(MessageBean msgBean, EmailSubscrptVo sub, MailingListVo listVo, String bodyText,
			List<String> varNames, Boolean saveEmbedEmailId, boolean isText)
			throws JMSException, DataValidationException {
		String listId = msgBean.getMailingListId();
		String subjText = msgBean.getSubject() == null ? "" : msgBean.getSubject();
		Address[] to = null;
		String toAddress = null;
		try {
			if (isText) {
				CustomerVo custVo = customerDao.getByEmailAddrId(sub.getEmailAddrId());
				if (custVo != null && StringUtils.isNotBlank(custVo.getMobilePhone())
						&& StringUtils.isNotBlank(custVo.getMobileCarrier())) {
					try {
						MobileCarrier mc = MobileCarrier.getByValue(custVo.getMobileCarrier());
						String phone = PhoneNumberUtil.convertTo10DigitNumber(custVo.getMobilePhone());
						if (StringUtils.isNotBlank(mc.getCountry())) {
							phone = mc.getCountry() + phone;
						}
						toAddress = phone+"@"+mc.getText();
						to = InternetAddress.parse(toAddress);
					}
					catch (IllegalArgumentException e) {
						logger.error("Mobile carrier (" + custVo.getMobileCarrier() + ") not found in enum MobileCarrier!");
						// TODO notify programming
					}
				}
				if (to == null) {
					return 0;
				}
			}
			else {
				toAddress = sub.getEmailAddr();
				to = InternetAddress.parse(toAddress);
			}
		}
		catch (AddressException e) {
			logger.error("Invalid TO address, ignored: " + toAddress, e);
			return 0;
		}
		/*
		String mailingAddr = StringUtil.removeDisplayName(listVo.getEmailAddr(), true);
		if (sub.getEmailAddr().toLowerCase().indexOf(mailingAddr) >= 0) {
			logger.warn("Loop occurred, ignore mailing list address: " + sub.getEmailAddr());
			continue;
		}
		*/
		Map<String, String> variables = new HashMap<String, String>();
		if (msgBean.getMsgId() != null) {
			String varName = VariableName.MailingListVariableName.BroadcastMsgId.name();
			variables.put(varName, String.valueOf(msgBean.getMsgId()));
		}
		logger.info("Sending Broadcast Email to: " + toAddress);
		TemplateRenderVo renderVo = null;
		renderVo = RenderUtil.renderEmailText(toAddress, variables, subjText, bodyText, listId, varNames);
		// set TO to subscriber address
		msgBean.setTo(to);
		String body = renderVo.getBody();
		if ("text/html".equals(msgBean.getBodyContentType()) && Boolean.FALSE.equals(sub.getAcceptHtml())
				|| isText) {
			// convert to plain text
			try {
				body = HtmlConverter.getInstance().convertToText(body);
				msgBean.getBodyNode().setContentType("text/plain");
			}
			catch (ParserException e) {
				logger.error("Failed to convert from html to plain text for: " + body);
				logger.error("ParserException caught", e);
			}
		}
		msgBean.getBodyNode().setValue(body);
		msgBean.setSubject(renderVo.getSubject());
		if (isText) { // do not embed email id in text message
			msgBean.setEmBedEmailId(Boolean.FALSE);
		}
		else {
			msgBean.setEmBedEmailId(saveEmbedEmailId);
			emailSubscrptDao.updateSentCount(sub.getEmailAddrId(), listId);
		}
		// write to mail sender queue
		String jmsMsgId = jmsProcessor.writeMsg(msgBean);
		if (isDebugEnabled) {
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		}
		int mailsSent = msgBean.getTo().length;
		return mailsSent;
	}

}

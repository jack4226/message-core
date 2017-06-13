package ltj.message.bo.inbox;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.BodypartBean;
import ltj.message.bean.BodypartUtil;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanBuilder;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bean.MessageNode;
import ltj.message.bean.MsgHeader;
import ltj.message.bo.customer.CustomerBo;
import ltj.message.bo.mailsender.MessageBodyBuilder;
import ltj.message.constant.AddressType;
import ltj.message.constant.CarrierCode;
import ltj.message.constant.Constants;
import ltj.message.constant.MLDeliveryType;
import ltj.message.constant.MsgDirection;
import ltj.message.constant.StatusId;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.inbox.MsgAttachmentDao;
import ltj.message.dao.inbox.MsgActionLogDao;
import ltj.message.dao.inbox.MsgAddressDao;
import ltj.message.dao.inbox.MsgClickCountDao;
import ltj.message.dao.inbox.MsgHeaderDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.dao.inbox.MsgRfcFieldDao;
import ltj.message.dao.outbox.DeliveryStatusDao;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.message.dao.outbox.MsgSequenceDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.PrintUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.inbox.MsgAttachmentVo;
import ltj.message.vo.inbox.MsgActionLogVo;
import ltj.message.vo.inbox.MsgAddressVo;
import ltj.message.vo.inbox.MsgClickCountVo;
import ltj.message.vo.inbox.MsgHeaderVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgRfcFieldVo;
import ltj.vo.outbox.DeliveryStatusVo;
import ltj.vo.outbox.MsgRenderedVo;
import ltj.vo.outbox.MsgStreamVo;

/**
 * save email data and properties into database.
 */
@Component("msgInboxBo")
public class MsgInboxBoImpl implements MsgInboxBo {
	static final Logger logger = Logger.getLogger(MsgInboxBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgSequenceDao msgSequenceDao;
	@Autowired
	private MsgInboxDao msgInboxDao;
	@Autowired
	private MsgAttachmentDao msgAttachmentDao;
	@Autowired
	private MsgAddressDao msgAddressDao;
	@Autowired
	private MsgHeaderDao msgHeaderDao;
	@Autowired
	private MsgRfcFieldDao msgRfcFieldDao;
	@Autowired
	private EmailAddressDao emailAddressDao;
	@Autowired
	private MsgStreamDao msgStreamDao;
	@Autowired
	private DeliveryStatusDao deliveryStatusDao;
	@Autowired
	private MsgRenderedDao msgRenderedDao;
	@Autowired
	private MsgActionLogDao msgActionLogDao;
	@Autowired
	private MsgClickCountDao msgClickCountDao;

	static final String LF = System.getProperty("line.separator", "\n");

	/**
	 * save an email to database.
	 * 
	 * @param msgBean
	 *            MessageBean instance containing email properties
	 * @return primary key (=msgId) of the record inserted
	 * @throws DataValidationException 
	 * @throws SQLException
	 *             if SQL error occurred
	 */
	public long saveMessage(MessageBean msgBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering saveMessage() method..." + LF + msgBean);
		}
		if (msgBean == null) {
			throw new DataValidationException("Input MessageBean is null");
		}
		if (msgBean.getRuleName() == null) {
			throw new DataValidationException("MessageBean.getRuleName() returns a null");
		}
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());

		MsgInboxVo msgVo = new MsgInboxVo();

		/* First, save email addresses to the database */
		EmailAddressVo fromAddrVo = findSertEmailAddr(msgBean.getFrom());
		if (fromAddrVo != null) { // should always be true
			msgVo.setFromAddrId(fromAddrVo.getEmailAddrId());
		}
		EmailAddressVo toAddrVo = findSertEmailAddr(msgBean.getTo());
		if (toAddrVo != null) { // should always be true
			msgVo.setToAddrId(toAddrVo.getEmailAddrId());
		}
		EmailAddressVo replaToVo = findSertEmailAddr(msgBean.getReplyto());
		Long replyToAddrId = replaToVo == null ? null : replaToVo.getEmailAddrId();
		msgVo.setReplyToAddrId(replyToAddrId);
		/* end of email addresses */
		
		long msgId = msgSequenceDao.findNextValue();
		msgBean.setMsgId(Long.valueOf(msgId));
		logger.info("saveMessage() - MsgId to be saved: " + msgId + ", From MailReader: " + msgBean.getIsReceived());
		msgVo.setMsgId(msgId);
		msgVo.setMsgRefId(msgBean.getMsgRefId());
		if (msgBean.getCarrierCode() == null) {
			logger.warn("saveMessage() - carrierCode field is null, set to S (SMTP)");
			msgBean.setCarrierCode(CarrierCode.SMTPMAIL.value());
		}
		msgVo.setCarrierCode(StringUtils.left(msgBean.getCarrierCode(), 1));
		msgVo.setMsgSubject(StringUtils.left(msgBean.getSubject(), 255));
		msgVo.setMsgPriority(MessageBeanUtil.getMsgPriority(msgBean.getPriority()));
		Timestamp ts = msgBean.getSendDate() == null ? updtTime : new Timestamp(msgBean.getSendDate().getTime());
		msgVo.setReceivedTime(ts);
		
		Calendar cal = Calendar.getInstance();
		if (msgBean.getPurgeAfter() != null) {
			cal.add(Calendar.MONTH, msgBean.getPurgeAfter());
		}
		else {
			cal.add(Calendar.MONTH, 12); // purge in 12 months - default
		}
		// set purge Date
		msgVo.setPurgeDate(new java.sql.Date(cal.getTime().getTime()));

		// find LeadMsgId. Also find client id if it's from MailReader
		if (msgBean.getMsgRefId() != null) {
			MsgInboxVo origVo = msgInboxDao.getByPrimaryKey(msgBean.getMsgRefId());
			if (origVo == null) { // could be deleted by User or purged by Purge routine
				logger.warn("saveMessage() - MsgInbox record not found by MsgRefId: " + msgBean.getMsgRefId());
			}
			else {
				msgVo.setLeadMsgId(origVo.getLeadMsgId());
				if (msgBean.getIsReceived()) { // from MailReader
					// code has been moved to MessageParser.parse()
				}
			}
		}
		if (msgVo.getLeadMsgId() < 0) {
			// default to myself
			msgVo.setLeadMsgId(msgBean.getMsgId());
		}
		// end LeadMsgId
		
		msgVo.setMsgContentType(StringUtils.left(msgBean.getContentType(), 100));
		String contentType = msgBean.getBodyContentType();
		msgVo.setBodyContentType(StringUtils.left(contentType, 50));
		
		if (msgBean.getIsReceived()) {
			/* from MailReader */
			msgVo.setMsgDirection(MsgDirection.RECEIVED.value());
			msgVo.setStatusId(StatusId.OPENED.value());
			msgVo.setSmtpMessageId(StringUtils.left(msgBean.getSmtpMessageId(), 255));
			String msgBody = msgBean.getBody();
			msgVo.setMsgBody(msgBody);
			msgVo.setMsgBodySize(msgBody == null ? 0 : msgBody.length());
			// update "Last Received" time
			if (msgVo.getFromAddrId() != null && fromAddrVo != null) {
				long minutesInMillis = 10 * 60 * 1000; // 10 minutes
				if (fromAddrVo.getLastRcptTime() == null
						|| fromAddrVo.getLastRcptTime().getTime() < (updtTime.getTime() - minutesInMillis)) {
					// XXX revisit this, it have caused concurrency issues and deadlocks
					emailAddressDao.updateLastRcptTime(msgVo.getFromAddrId());
				}
			}
		}
		else {
			/* from MailSender */
			msgVo.setMsgDirection(MsgDirection.SENT.value());
			msgVo.setSmtpMessageId(null);
			msgVo.setDeliveryTime(null); // delivery time
			msgVo.setStatusId(StatusId.PENDING.value());
			if (msgBean.getRenderId() != null && msgRenderedDao.getByPrimaryKey(msgBean.getRenderId()) != null) {
				msgVo.setRenderId(msgBean.getRenderId());
			}
			msgVo.setOverrideTestAddr(msgBean.getOverrideTestAddr());
			// check original message body's content type
			if (msgBean.getOriginalMail() != null) {
				String origContentType = msgBean.getOriginalMail().getBodyContentType();
				if (contentType.indexOf("html") < 0 && origContentType.indexOf("html") >= 0) {
					// reset content type to original's
					msgVo.setBodyContentType(StringUtils.left(origContentType, 50));
				}
			}
			/* Rebuild the Message Body, generate Email_Id from MsgId */
			String msgBody = MessageBodyBuilder.getBody(msgBean);
			/* end of rebuild */
			msgVo.setMsgBody(msgBody);
			msgVo.setMsgBodySize(msgBody == null ? 0 : msgBody.length());
			BodypartBean bodyNode = msgBean.getBodyNode();
			// update MessageBean.body with Email_Id
			if (bodyNode == null) {
				logger.fatal("saveMessage() - Programming error: bodyNode is null");
				msgBean.setContentType(msgVo.getMsgContentType());
				msgBean.setBody(msgVo.getMsgBody());
			}
			else {
				bodyNode.setContentType(msgVo.getBodyContentType());
				bodyNode.setValue(msgVo.getMsgBody().getBytes());
			}
			// update "Last Sent" time
			if (msgVo.getToAddrId() != null && toAddrVo != null) {
				long minutesInMillis = 10 * 60 * 1000; // 10 minutes
				if (toAddrVo.getLastSentTime() == null
						|| toAddrVo.getLastSentTime().getTime() < (updtTime.getTime() - minutesInMillis)) {
					emailAddressDao.updateLastSentTime(msgVo.getToAddrId());
				}
			}
		}
		
		msgVo.setClientId(StringUtils.left(msgBean.getClientId(), 16));
		msgVo.setCustId(StringUtils.left(msgBean.getCustId(), CustomerBo.CUSTOMER_ID_MAX_LEN));
		msgVo.setRuleName(StringUtils.left(msgBean.getRuleName(), 26));
		
		msgVo.setUpdtTime(updtTime);
		msgVo.setUpdtUserId(StringUtils.left(Constants.DEFAULT_USER_ID, 10));
		msgVo.setLockTime(null); // lock time
		msgVo.setLockId(null); // lock id
		
		// retrieve attachment count and size, and gathers delivery reports
		BodypartUtil.retrieveAttachments(msgBean);
		
		List<MessageNode> aNodes = msgBean.getAttachments();
		msgVo.setAttachmentCount(aNodes == null ? 0 : aNodes.size());
		int attachmentSize = 0;
		if (aNodes != null) {
			for (MessageNode mNode : aNodes) {
				BodypartBean aNode = mNode.getBodypartNode();
				int _size = aNode.getValue() == null ? 0 : aNode.getValue().length;
				attachmentSize += _size;
			}
		}
		msgVo.setAttachmentSize(attachmentSize);
		if (isDebugEnabled) {
			logger.debug("Message to insert" + LF + PrintUtil.prettyPrint(msgVo));
		}
		// save to message_inbox table
		msgInboxDao.insert(msgVo);
		
		// insert click count record for Broadcasting e-mail
		if (RuleNameEnum.BROADCAST.name().equals(msgBean.getRuleName())) {
			MsgClickCountVo msgClickCountVo = new MsgClickCountVo();
			msgClickCountVo.setMsgId(msgId);
			// msgBean.getMailingListId() should always returns a value. just for safety.
			String listId = msgBean.getMailingListId() == null ? "" : msgBean.getMailingListId();
			msgClickCountVo.setListId(listId);
			if (msgBean.getToCustomersOnly()) {
				msgClickCountVo.setDeliveryOption(MLDeliveryType.CUSTOMERS_ONLY.value());
			}
			else if (msgBean.getToProspectsOnly()) {
				msgClickCountVo.setDeliveryOption(MLDeliveryType.PROSPECTS_ONLY.value());
			}
			else {
				msgClickCountVo.setDeliveryOption(MLDeliveryType.ALL_ON_LIST.value());
			}
			msgClickCountVo.setSentCount(0);
			msgClickCountVo.setClickCount(0);
			msgClickCountDao.insert(msgClickCountVo);
		}
		
		// save message headers
		List<MsgHeader> headers = msgBean.getHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.size(); i++) {
				MsgHeader header = headers.get(i);
				MsgHeaderVo msgHeaderVo= new MsgHeaderVo();
				msgHeaderVo.setMsgId(msgVo.getMsgId());
				msgHeaderVo.setHeaderName(StringUtils.left(header.getName(),100));
				msgHeaderVo.setHeaderValue(header.getValue());
				msgHeaderVo.setHeaderSeq(i+1);
				msgHeaderDao.insert(msgHeaderVo);
			}
		}

		// save attachments
		if (aNodes != null && aNodes.size() > 0) {
			for (int i = 0; i < aNodes.size(); i++) {
				MessageNode mNode = aNodes.get(i);
				BodypartBean aNode = mNode.getBodypartNode();
				MsgAttachmentVo attchVo = new MsgAttachmentVo();
				attchVo.setMsgId(msgVo.getMsgId());
				attchVo.setAttchmntDepth(mNode.getLevel());
				attchVo.setAttchmntSeq(i+1);
				attchVo.setAttchmntName(StringUtils.left(aNode.getDescription(),100));
				attchVo.setAttchmntType(StringUtils.left(aNode.getContentType(),100));
				attchVo.setAttchmntDisp(StringUtils.left(aNode.getDisposition(),100));
				attchVo.setAttchmntValue(aNode.getValue());
				msgAttachmentDao.insert(attchVo);
			}
		}
		
		// save RFC fields
		if (msgBean.getReport() != null) {
			MessageNode mNode = msgBean.getReport();
			BodypartBean aNode = mNode.getBodypartNode();
			MsgRfcFieldVo msgRfcFieldVo = new MsgRfcFieldVo();
			msgRfcFieldVo.setMsgId(msgVo.getMsgId());
			msgRfcFieldVo.setRfcType(StringUtils.left(aNode.getContentType(),30));
			
			msgRfcFieldVo.setRfcStatus(StringUtils.left(msgBean.getDsnStatus(),30));
			msgRfcFieldVo.setRfcAction(StringUtils.left(msgBean.getDsnAction(),30));
			msgRfcFieldVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
			msgRfcFieldVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
			msgRfcFieldVo.setOrigRcpt(StringUtils.left(msgBean.getOrigRcpt(),255));
			//rfcFieldsVo.setOrigMsgSubject(StringUtil.cut(msgBean.getOrigSubject(),255));
			//rfcFieldsVo.setMessageId(StringUtil.cut(msgBean.getMessageId(),255));
			msgRfcFieldVo.setDsnText(msgBean.getDsnText());
			msgRfcFieldVo.setDsnRfc822(msgBean.getDiagnosticCode()); // TODO: revisit
			msgRfcFieldVo.setDlvrStatus(msgBean.getDsnDlvrStat());
			msgRfcFieldDao.insert(msgRfcFieldVo);
		}
		
		if (msgBean.getRfc822() != null) {
			MessageNode mNode = msgBean.getRfc822();
			BodypartBean aNode = mNode.getBodypartNode();
			MsgRfcFieldVo msgRfcFieldVo = new MsgRfcFieldVo();
			msgRfcFieldVo.setMsgId(msgVo.getMsgId());
			msgRfcFieldVo.setRfcType(StringUtils.left(aNode.getContentType(),30));
			
			//rfcFieldsVo.setRfcStatus(StringUtil.cut(msgBean.getDsnStatus(),30));
			//rfcFieldsVo.setRfcAction(StringUtil.cut(msgBean.getDsnAction(),30));
			msgRfcFieldVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
			msgRfcFieldVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
			//rfcFieldsVo.setOrigRcpt(StringUtil.cut(msgBean.getOrigRcpt(),255));
			msgRfcFieldVo.setOrigMsgSubject(StringUtils.left(msgBean.getOrigSubject(),255));
			msgRfcFieldVo.setMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			msgRfcFieldVo.setDsnText(msgBean.getDsnText());
			msgRfcFieldVo.setDsnRfc822(msgBean.getDsnRfc822());
			//rfcFieldsVo.setDlvrStatus(msgBean.getDsnDlvrStat());
			msgRfcFieldDao.insert(msgRfcFieldVo);
		}
		
		// we could have found a final recipient without delivery reports
		if (msgBean.getReport() == null && msgBean.getRfc822() == null) {
			if (StringUtil.isNotEmpty(msgBean.getFinalRcpt()) || StringUtil.isNotEmpty(msgBean.getOrigRcpt())) {
				MsgRfcFieldVo msgRfcFieldVo = new MsgRfcFieldVo();
				msgRfcFieldVo.setMsgId(msgVo.getMsgId());
				msgRfcFieldVo.setRfcType(StringUtils.left(msgBean.getContentType(),30));
					// we don't have content type, so just stick one here.
				msgRfcFieldVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
				msgRfcFieldVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
				msgRfcFieldVo.setOrigRcpt(StringUtils.left(msgBean.getOrigRcpt(),255));
				msgRfcFieldDao.insert(msgRfcFieldVo);
			}
		}
		
		// save addresses
		saveAddress(msgBean.getFrom(), AddressType.FROM_ADDR, msgVo.getMsgId());
		saveAddress(msgBean.getTo(), AddressType.TO_ADDR, msgVo.getMsgId());
		saveAddress(msgBean.getReplyto(), AddressType.REPLYTO_ADDR, msgVo.getMsgId());
		saveAddress(msgBean.getCc(), AddressType.CC_ADDR, msgVo.getMsgId());
		saveAddress(msgBean.getBcc(), AddressType.BCC_ADDR, msgVo.getMsgId());
		
		// save message raw stream if received by MailReader
		if (msgBean.getHashMap().containsKey(MessageBeanBuilder.MSG_RAW_STREAM) && msgBean.getIsReceived()) {
			// save raw stream for in-bound mails only
			// out-bound raw stream is saved by MailSender class
			MsgStreamVo msgStreamVo = new MsgStreamVo();
			msgStreamVo.setMsgId(msgVo.getMsgId());
			msgStreamVo.setFromAddrId(msgVo.getFromAddrId());
			msgStreamVo.setToAddrId(msgVo.getToAddrId());
			msgStreamVo.setMsgSubject(msgVo.getMsgSubject());
			msgStreamVo.setMsgStream((byte[]) msgBean.getHashMap().get(MessageBeanBuilder.MSG_RAW_STREAM));
			msgStreamDao.insert(msgStreamVo);
		}
		
		if (isDebugEnabled) {
			logger.debug("saveMessage() - Message has been saved to database, MsgId: " + msgVo.getMsgId());
		}
		return msgVo.getMsgId();
	}
	
	/**
	 * TODO not implemented yet
	 * @param msgBean
	 */
	public void saveMessageFlowLogs(MessageBean msgBean) {
		MsgActionLogVo msgActionLogVo = new MsgActionLogVo();
		msgActionLogVo.setMsgId(msgBean.getMsgId());
		msgActionLogVo.setMsgRefId(msgBean.getMsgRefId());
		// find lead message id
		if (msgBean.getMsgRefId() != null) {
			List<MsgActionLogVo> list = msgActionLogDao.getByMsgId(msgBean.getMsgRefId());
			if (list == null || list.isEmpty()) {
				logger.error("saveMessageFlowLogs() - record not found for MsgRefId: " + msgBean.getMsgRefId());
			}
			else {
				MsgActionLogVo vo = list.get(0);
				msgActionLogVo.setLeadMsgId(vo.getLeadMsgId());
			}
		}
		if (msgActionLogVo.getLeadMsgId() < 0) {
			msgActionLogVo.setLeadMsgId(msgBean.getMsgId());
		}
		if (StringUtils.isNotBlank(msgBean.getRuleName())) {
			msgActionLogVo.setActionBo(msgBean.getRuleName());
		}
		else {
			msgActionLogVo.setActionBo(RuleNameEnum.SEND_MAIL.name());
		}
		msgActionLogDao.insert(msgActionLogVo);
	}
	
	/**
	 * returns data from MsgInbox, MsgHeaders, Attachments, and RFCFields
	 * 
	 * @param msgId -
	 *            message Id
	 * @return a MsgInboxVo or null if not found
	 */
	public MsgInboxVo getMessageByPK(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo != null) {
			List<MsgAddressVo> msgAddrs = msgAddressDao.getByMsgId(msgId);
			msgInboxVo.setMsgAddrs(msgAddrs);
			List<MsgHeaderVo> msgHeaders = msgHeaderDao.getByMsgId(msgId);
			msgInboxVo.setMsgHeaders(msgHeaders);
			List<MsgAttachmentVo> attachments = msgAttachmentDao.getByMsgId(msgId);
			msgInboxVo.setAttachments(attachments);
			List<MsgRfcFieldVo> rfcFields = msgRfcFieldDao.getByMsgId(msgId);
			msgInboxVo.setRfcFields(rfcFields);
		}
		return msgInboxVo;
	}
	
	/**
	 * Returns data from getMessageByPK plus data from DeliveryStatus,
	 * MsgRendered and MsgStream.
	 * 
	 * @param msgId -
	 *            message Id
	 * @return a MsgInboxVo or null if not found
	 */
	public MsgInboxVo getAllDataByMsgId(long msgId) {
		MsgInboxVo msgInboxVo = getMessageByPK(msgId);
		if (msgInboxVo != null) {
			List<DeliveryStatusVo> deliveryStatus = deliveryStatusDao.getByMsgId(msgId);
			msgInboxVo.setDeliveryStatus(deliveryStatus);
			if (msgInboxVo.getRenderId() != null) {
				MsgRenderedVo msgRenderedVo = msgRenderedDao.getByPrimaryKey(msgInboxVo.getRenderId());
				msgInboxVo.setMsgRenderedVo(msgRenderedVo);
			}
			MsgStreamVo msgStreamVo = msgStreamDao.getByPrimaryKey(msgId);
			msgInboxVo.setMsgStreamVo(msgStreamVo);
		}
		return msgInboxVo;
	}
	
	private void saveAddress(Address[] addrs, AddressType addrType, long msgId) {
		if (addrs == null || addrs.length == 0) {
			return;
		}
		for (int i = 0; i < addrs.length; i++) {
			Address addr = addrs[i];
			if (addr != null) {
				MsgAddressVo addrVo = new MsgAddressVo();
				addrVo.setMsgId(msgId);
				addrVo.setAddrType(addrType.value());
				addrVo.setAddrSeq(i + 1);
				addrVo.setAddrValue(StringUtils.left(addr.toString(), 255));
				try {
					InternetAddress.parse(addrVo.getAddrValue());
				} catch (AddressException e) {
					logger.error("Skip invalid email address: " + addrVo.getAddrValue());
					continue;
				}
				msgAddressDao.insert(addrVo);
			}
		}
	}
	
	// get the first email address from the list and return its EmailAddrId
	private EmailAddressVo findSertEmailAddr(Address[] addrs) {
		EmailAddressVo emailAddressVo = null;
		for (int i = 0; addrs != null && i < addrs.length; i++) {
			Address addr = addrs[i];
			if (addr != null) {
				emailAddressVo = getEmailAddrVo(addr.toString());
				if (emailAddressVo != null) {
					break;
				}
			}
		}
		return emailAddressVo;
	}
	
	private EmailAddressVo getEmailAddrVo(String addr) {
		EmailAddressVo emailAddressVo = null;
		if (StringUtils.isNotBlank(addr)) {
			emailAddressVo = emailAddressDao.findByAddress(addr.trim());
		}
		return emailAddressVo;
	}

	private Long getEmailAddrId(String addr) {
		EmailAddressVo vo = getEmailAddrVo(addr);
		if (vo != null) {
			return vo.getEmailAddrId();
		}
		return null;
	}
}

package ltj.message.bo.inbox;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.mail.Address;

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
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.inbox.AttachmentsDao;
import ltj.message.dao.inbox.MsgActionLogsDao;
import ltj.message.dao.inbox.MsgAddrsDao;
import ltj.message.dao.inbox.MsgClickCountsDao;
import ltj.message.dao.inbox.MsgHeadersDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.dao.inbox.RfcFieldsDao;
import ltj.message.dao.outbox.DeliveryStatusDao;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.message.dao.outbox.MsgSequenceDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.PrintUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.AttachmentsVo;
import ltj.message.vo.inbox.MsgActionLogsVo;
import ltj.message.vo.inbox.MsgAddrsVo;
import ltj.message.vo.inbox.MsgClickCountsVo;
import ltj.message.vo.inbox.MsgHeadersVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.RfcFieldsVo;
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
	private AttachmentsDao attachmentsDao;
	@Autowired
	private MsgAddrsDao msgAddrsDao;
	@Autowired
	private MsgHeadersDao msgHeadersDao;
	@Autowired
	private RfcFieldsDao rfcFieldsDao;
	@Autowired
	private EmailAddrDao emailAddrDao;
	@Autowired
	private MsgStreamDao msgStreamDao;
	@Autowired
	private DeliveryStatusDao deliveryStatusDao;
	@Autowired
	private MsgRenderedDao msgRenderedDao;
	@Autowired
	private MsgActionLogsDao msgActionLogsDao;
	@Autowired
	private MsgClickCountsDao msgClickCountsDao;

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
		long msgId = msgSequenceDao.findNextValue();
		msgBean.setMsgId(Long.valueOf(msgId));
		logger.info("saveMessage() - MsgId to be saved: " + msgId + ", From MailReader: " + msgBean.getIsReceived());
		msgVo.setMsgId(msgId);
		msgVo.setMsgRefId(msgBean.getMsgRefId());
		if (msgBean.getCarrierCode() == null) {
			logger.warn("saveMessage() - carrierCode field is null, set to S (SMTP)");
			msgBean.setCarrierCode(CarrierCode.SMTPMAIL.value());
		}
		msgVo.setCarrierCode(StringUtils.left(msgBean.getCarrierCode(),1));
		msgVo.setMsgSubject(StringUtils.left(msgBean.getSubject(),255));
		msgVo.setMsgPriority(MessageBeanUtil.getMsgPriority(msgBean.getPriority()));
		Timestamp ts = msgBean.getSendDate() == null ? updtTime : new Timestamp(msgBean.getSendDate().getTime());
		msgVo.setReceivedTime(ts);
		
		/* insert email addresses */
		EmailAddrVo fromAddrVo = getEmailAddrVo(msgBean.getFrom());
		if (fromAddrVo != null) { // should always be true
			msgVo.setFromAddrId(fromAddrVo.getEmailAddrId());
		}
		EmailAddrVo toAddrVo = getEmailAddrVo(msgBean.getTo());
		if (toAddrVo != null) { // should always be true
			msgVo.setToAddrId(toAddrVo.getEmailAddrId());
		}
		EmailAddrVo replaToVo = getEmailAddrVo(msgBean.getReplyto());
		Long replyToAddrId = replaToVo == null ? null : replaToVo.getEmailAddrId();
		msgVo.setReplyToAddrId(replyToAddrId);
		/* end of email addresses */
		
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
		
		msgVo.setMsgContentType(StringUtils.left(msgBean.getContentType(),100));
		String contentType = msgBean.getBodyContentType();
		msgVo.setBodyContentType(StringUtils.left(contentType,50));
		
		if (msgBean.getIsReceived()) {
			/* from MailReader */
			msgVo.setMsgDirection(MsgDirection.RECEIVED.value());
			msgVo.setStatusId(StatusId.OPENED.value());
			msgVo.setSmtpMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			String msgBody = msgBean.getBody();
			msgVo.setMsgBody(msgBody);
			msgVo.setMsgBodySize(msgBody == null ? 0 : msgBody.length());
			// update "Last Received" time
			if (msgVo.getFromAddrId() != null && fromAddrVo != null) {
				long minutesInMillis = 10 * 60 * 1000; // 10 minutes
				if (fromAddrVo.getLastRcptTime() == null
						|| fromAddrVo.getLastRcptTime().getTime() < (updtTime.getTime() - minutesInMillis)) {
					emailAddrDao.updateLastRcptTime(msgVo.getFromAddrId());
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
			msgVo.setOverrideTestAddr(msgBean.getOverrideTestAddr() ? Constants.Y : Constants.N);
			// check original message body's content type
			if (msgBean.getOriginalMail() != null) {
				String origContentType = msgBean.getOriginalMail().getBodyContentType();
				if (contentType.indexOf("html") < 0 && origContentType.indexOf("html") >= 0) {
					// reset content type to original's
					msgVo.setBodyContentType(StringUtils.left(origContentType,50));
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
					emailAddrDao.updateLastSentTime(msgVo.getToAddrId());
				}
			}
		}
		
		msgVo.setClientId(StringUtils.left(msgBean.getClientId(),16));
		msgVo.setCustId(StringUtils.left(msgBean.getCustId(),CustomerBo.CUSTOMER_ID_MAX_LEN));
		msgVo.setRuleName(StringUtils.left(msgBean.getRuleName(),26));
		
		msgVo.setUpdtTime(updtTime);
		msgVo.setUpdtUserId(StringUtils.left(Constants.DEFAULT_USER_ID,10));
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
			MsgClickCountsVo msgClickCountsVo = new MsgClickCountsVo();
			msgClickCountsVo.setMsgId(msgId);
			// msgBean.getMailingListId() should always returns a value. just for safety.
			String listId = msgBean.getMailingListId() == null ? "" : msgBean.getMailingListId();
			msgClickCountsVo.setListId(listId);
			if (msgBean.getToCustomersOnly()) {
				msgClickCountsVo.setDeliveryOption(MLDeliveryType.CUSTOMERS_ONLY.value());
			}
			else if (msgBean.getToProspectsOnly()) {
				msgClickCountsVo.setDeliveryOption(MLDeliveryType.PROSPECTS_ONLY.value());
			}
			else {
				msgClickCountsVo.setDeliveryOption(MLDeliveryType.ALL_ON_LIST.value());
			}
			msgClickCountsVo.setSentCount(0);
			msgClickCountsVo.setClickCount(0);
			msgClickCountsDao.insert(msgClickCountsVo);
		}
		
		// save message headers
		List<MsgHeader> headers = msgBean.getHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.size(); i++) {
				MsgHeader header = headers.get(i);
				MsgHeadersVo msgHeadersVo= new MsgHeadersVo();
				msgHeadersVo.setMsgId(msgVo.getMsgId());
				msgHeadersVo.setHeaderName(StringUtils.left(header.getName(),100));
				msgHeadersVo.setHeaderValue(header.getValue());
				msgHeadersVo.setHeaderSeq(i+1);
				msgHeadersDao.insert(msgHeadersVo);
			}
		}

		// save attachments
		if (aNodes!=null && aNodes.size()>0) {
			for (int i=0; i<aNodes.size(); i++) {
				MessageNode mNode = aNodes.get(i);
				BodypartBean aNode = mNode.getBodypartNode();
				AttachmentsVo attchVo = new AttachmentsVo();
				attchVo.setMsgId(msgVo.getMsgId());
				attchVo.setAttchmntDepth(mNode.getLevel());
				attchVo.setAttchmntSeq(i+1);
				attchVo.setAttchmntName(StringUtils.left(aNode.getDescription(),100));
				attchVo.setAttchmntType(StringUtils.left(aNode.getContentType(),100));
				attchVo.setAttchmntDisp(StringUtils.left(aNode.getDisposition(),100));
				attchVo.setAttchmntValue(aNode.getValue());
				attachmentsDao.insert(attchVo);
			}
		}
		
		// save RFC fields
		if (msgBean.getReport() != null) {
			MessageNode mNode = msgBean.getReport();
			BodypartBean aNode = mNode.getBodypartNode();
			RfcFieldsVo rfcFieldsVo = new RfcFieldsVo();
			rfcFieldsVo.setMsgId(msgVo.getMsgId());
			rfcFieldsVo.setRfcType(StringUtils.left(aNode.getContentType(),30));
			
			rfcFieldsVo.setRfcStatus(StringUtils.left(msgBean.getDsnStatus(),30));
			rfcFieldsVo.setRfcAction(StringUtils.left(msgBean.getDsnAction(),30));
			rfcFieldsVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
			rfcFieldsVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
			rfcFieldsVo.setOrigRcpt(StringUtils.left(msgBean.getOrigRcpt(),255));
			//rfcFieldsVo.setOrigMsgSubject(StringUtil.cut(msgBean.getOrigSubject(),255));
			//rfcFieldsVo.setMessageId(StringUtil.cut(msgBean.getMessageId(),255));
			rfcFieldsVo.setDsnText(msgBean.getDsnText());
			rfcFieldsVo.setDsnRfc822(msgBean.getDiagnosticCode()); // TODO: revisit
			rfcFieldsVo.setDlvrStatus(msgBean.getDsnDlvrStat());
			rfcFieldsDao.insert(rfcFieldsVo);
		}
		
		if (msgBean.getRfc822() != null) {
			MessageNode mNode = msgBean.getRfc822();
			BodypartBean aNode = mNode.getBodypartNode();
			RfcFieldsVo rfcFieldsVo = new RfcFieldsVo();
			rfcFieldsVo.setMsgId(msgVo.getMsgId());
			rfcFieldsVo.setRfcType(StringUtils.left(aNode.getContentType(),30));
			
			//rfcFieldsVo.setRfcStatus(StringUtil.cut(msgBean.getDsnStatus(),30));
			//rfcFieldsVo.setRfcAction(StringUtil.cut(msgBean.getDsnAction(),30));
			rfcFieldsVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
			rfcFieldsVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
			//rfcFieldsVo.setOrigRcpt(StringUtil.cut(msgBean.getOrigRcpt(),255));
			rfcFieldsVo.setOrigMsgSubject(StringUtils.left(msgBean.getOrigSubject(),255));
			rfcFieldsVo.setMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			rfcFieldsVo.setDsnText(msgBean.getDsnText());
			rfcFieldsVo.setDsnRfc822(msgBean.getDsnRfc822());
			//rfcFieldsVo.setDlvrStatus(msgBean.getDsnDlvrStat());
			rfcFieldsDao.insert(rfcFieldsVo);
		}
		
		// we could have found a final recipient without delivery reports
		if (msgBean.getReport() == null && msgBean.getRfc822() == null) {
			if (!StringUtil.isEmpty(msgBean.getFinalRcpt()) || !StringUtil.isEmpty(msgBean.getOrigRcpt())) {
				RfcFieldsVo rfcFieldsVo = new RfcFieldsVo();
				rfcFieldsVo.setMsgId(msgVo.getMsgId());
				rfcFieldsVo.setRfcType(StringUtils.left(msgBean.getContentType(),30));
					// we don't have content type, so just stick one here.
				rfcFieldsVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
				rfcFieldsVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
				rfcFieldsVo.setOrigRcpt(StringUtils.left(msgBean.getOrigRcpt(),255));
				rfcFieldsDao.insert(rfcFieldsVo);
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
			// out-bound raw stream is saved in MailSender
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
	 * not implemented yet
	 * @param msgBean
	 */
	public void saveMessageFlowLogs(MessageBean msgBean) {
		MsgActionLogsVo msgActionLogsVo = new MsgActionLogsVo();
		msgActionLogsVo.setMsgId(msgBean.getMsgId());
		msgActionLogsVo.setMsgRefId(msgBean.getMsgRefId());
		// find lead message id
		if (msgBean.getMsgRefId() != null) {
			List<MsgActionLogsVo> list = msgActionLogsDao.getByMsgId(msgBean.getMsgRefId());
			if (list == null || list.size() == 0) {
				logger.error("saveMessageFlowLogs() - record not found for MsgRefId: " + msgBean.getMsgRefId());
			}
			else {
				MsgActionLogsVo vo = list.get(0);
				msgActionLogsVo.setLeadMsgId(vo.getLeadMsgId());
			}
		}
		if (msgActionLogsVo.getLeadMsgId() < 0) {
			msgActionLogsVo.setLeadMsgId(msgBean.getMsgId());
		}
		msgActionLogsVo.setActionBo(RuleNameEnum.SEND_MAIL.name());
		msgActionLogsDao.insert(msgActionLogsVo);
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
		if (msgInboxVo!=null) {
			List<MsgAddrsVo> msgAddrs = msgAddrsDao.getByMsgId(msgId);
			msgInboxVo.setMsgAddrs(msgAddrs);
			List<MsgHeadersVo> msgHeaders = msgHeadersDao.getByMsgId(msgId);
			msgInboxVo.setMsgHeaders(msgHeaders);
			List<AttachmentsVo> attachments = attachmentsDao.getByMsgId(msgId);
			msgInboxVo.setAttachments(attachments);
			List<RfcFieldsVo> rfcFields = rfcFieldsDao.getByMsgId(msgId);
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
				MsgAddrsVo addrVo = new MsgAddrsVo();
				addrVo.setMsgId(msgId);
				addrVo.setAddrType(addrType.value());
				addrVo.setAddrSeq(i + 1);
				addrVo.setAddrValue(StringUtils.left(addr.toString(),255));
				msgAddrsDao.insert(addrVo);
			}
		}
	}
	
	// get the first email address from the list and return its EmailAddrId
	private EmailAddrVo getEmailAddrVo(Address[] addrs) {
		EmailAddrVo emailAddrVo = null;
		for (int i = 0; addrs != null && i < addrs.length; i++) {
			Address addr = addrs[i];
			if (addr != null) {
				emailAddrVo = getEmailAddrVo(addr.toString());
				if (emailAddrVo != null) {
					break;
				}
			}
		}
		return emailAddrVo;
	}
	
	private EmailAddrVo getEmailAddrVo(String addr) {
		EmailAddrVo emailAddrVo = null;
		if (StringUtils.isNotBlank(addr)) {
			emailAddrVo = emailAddrDao.findByAddress(addr.trim());
		}
		return emailAddrVo;
	}

	private Long getEmailAddrId(String addr) {
		EmailAddrVo vo = getEmailAddrVo(addr);
		if (vo != null) {
			return vo.getEmailAddrId();
		}
		return null;
	}
}

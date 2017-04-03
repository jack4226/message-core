package ltj.message.table;

import java.sql.Timestamp;
import java.util.List;

import javax.mail.Part;

import org.springframework.context.ApplicationContext;

import ltj.data.preload.RuleNameEnum;
import ltj.data.preload.SubscriberEnum;
import ltj.message.constant.AddressType;
import ltj.message.constant.CarrierCode;
import ltj.message.constant.Constants;
import ltj.message.constant.MsgDirection;
import ltj.message.constant.StatusId;
import ltj.message.constant.VariableName;
import ltj.message.constant.XHeaderName;
import ltj.message.dao.client.ClientUtil;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.inbox.MsgAttachmentDao;
import ltj.message.dao.inbox.MsgAddressDao;
import ltj.message.dao.inbox.MsgClickCountDao;
import ltj.message.dao.inbox.MsgHeaderDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.dao.inbox.MsgRfcFieldDao;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.message.dao.outbox.MsgSequenceDao;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.message.dao.template.MsgSourceDao;
import ltj.message.dao.template.SubjTemplateDao;
import ltj.message.util.FileUtil;
import ltj.message.vo.ClientVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.inbox.MsgAttachmentVo;
import ltj.message.vo.inbox.MsgAddressVo;
import ltj.message.vo.inbox.MsgClickCountVo;
import ltj.message.vo.inbox.MsgHeaderVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgRfcFieldVo;
import ltj.spring.util.SpringUtil;
import ltj.vo.outbox.MsgRenderedVo;
import ltj.vo.outbox.MsgStreamVo;
import ltj.vo.template.BodyTemplateVo;
import ltj.vo.template.MsgSourceVo;
import ltj.vo.template.SubjTemplateVo;

public class LoadInboxTables {
	static long msgId = -1L;
	MsgSequenceDao msgSequenceDao;
	EmailAddressDao emailAddressDao;
	MsgClickCountDao msgClickCountDao;
	MsgInboxDao msgInboxDao;
	//static AbstractApplicationContext factory = null;
	
	public static void main(String[] args) {
		
		LoadInboxTables loadInboxTables = new LoadInboxTables();
		try {
			loadInboxTables.loadData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public void loadData() {
		loadData(null);
	}
	
	public void loadData(ApplicationContext factory) {
		if (factory == null) {
			factory = SpringUtil.getDaoAppContext();
		}
		msgSequenceDao = factory.getBean(MsgSequenceDao.class);
		emailAddressDao = factory.getBean(EmailAddressDao.class);
		msgClickCountDao = factory.getBean(MsgClickCountDao.class);
		msgInboxDao = factory.getBean(MsgInboxDao.class);
		MsgAddressDao msgAddressDao = factory.getBean(MsgAddressDao.class);
		MsgAttachmentDao msgAttachmentDao = factory.getBean(MsgAttachmentDao.class);
		MsgHeaderDao msgHeaderDao = factory.getBean(MsgHeaderDao.class);
		MsgRfcFieldDao msgRfcFieldDao = factory.getBean(MsgRfcFieldDao.class);
		MsgStreamDao msgStreamDao = factory.getBean(MsgStreamDao.class);
		
		msgId = load(msgInboxDao);
		load(msgAddressDao);
		load(msgAttachmentDao);
		load(msgHeaderDao);
		load(msgRfcFieldDao);
		MsgInboxVo inbox = msgInboxDao.getByPrimaryKey(msgId);
		loadMessageStream(msgStreamDao, inbox);
		loadRenderTables(factory);
	}
	
	long load(MsgInboxDao msgInboxDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		String fromAddr = SubscriberEnum.Subscriber.Subscriber1.getAddress();
		EmailAddressVo fromAddrVo = emailAddressDao.findByAddress(fromAddr);
		
		ClientVo clientVo = ClientUtil.getDefaultClientVo();
		String toAddr = clientVo.getReturnPathLeft() + "@" + clientVo.getDomainName();
		EmailAddressVo toAddrVo = emailAddressDao.findByAddress(toAddr);
		
		long msgId = msgSequenceDao.findNextValue();
		MsgInboxVo in = new MsgInboxVo();
		in.setMsgId(msgId);
		in.setMsgRefId(null);
		in.setLeadMsgId(msgId);
		in.setCarrierCode(CarrierCode.SMTPMAIL.value());
		in.setMsgDirection(MsgDirection.RECEIVED.value());
		in.setMsgSubject("Test Subject");
		in.setMsgPriority("2 (Normal)");
		in.setReceivedTime(updtTime);
		in.setFromAddrId(fromAddrVo.getEmailAddrId());
		in.setReplyToAddrId(null);
		in.setToAddrId(toAddrVo.getEmailAddrId());
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setCustId(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setLockTime(null);
		in.setLockId(null);
		in.setRuleName(RuleNameEnum.GENERIC.name());
		in.setMsgContentType("multipart/mixed");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Message Body");

		msgInboxDao.insert(in);
		
		toAddrVo = emailAddressDao.findByAddress(Constants.DEMOLIST1_ADDR);

		msgId = msgSequenceDao.findNextValue();
		in.setMsgId(msgId);
		in.setMsgRefId(null);
		in.setLeadMsgId(msgId);
		in.setCarrierCode(CarrierCode.SMTPMAIL.value());
		in.setMsgDirection(MsgDirection.SENT.value());
		in.setMsgSubject("Test Broadcast Subject");
		in.setMsgPriority("2 (Normal)");
		in.setReceivedTime(updtTime);
		in.setFromAddrId(toAddrVo.getEmailAddrId());
		in.setReplyToAddrId(null);
		in.setToAddrId(toAddrVo.getEmailAddrId());
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setCustId(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setLockTime(null);
		in.setLockId(null);
		in.setRuleName(RuleNameEnum.BROADCAST.name());
		in.setMsgContentType("text/plain");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Broadcast Message Body");
		in.setStatusId(StatusId.CLOSED.value());
		in.setFlagged(true);

		msgInboxDao.insert(in);

		MsgClickCountVo in2 = new MsgClickCountVo();
		
		in2.setMsgId(msgId);
		in2.setListId(Constants.DEMOLIST1_NAME);
		in2.setStartTime(updtTime);
		in2.setSentCount(1);
		in2.setOpenCount(1);
		in2.setClickCount(1);
		in2.setLastOpenTime(updtTime);
		in2.setLastClickTime(updtTime);

		msgClickCountDao.insert(in2);

		System.out.println("load() completed.\n");
		return in.getMsgId();
	}
	
	void load(MsgAddressDao msgAddressDao) {
		List<MsgInboxVo> msgList = msgInboxDao.getRecent(100);
		
		int rowsInserted = 0;
		for (MsgInboxVo vo : msgList) {
			MsgAddressVo in = new MsgAddressVo();
			
			in.setMsgId(vo.getMsgId());
			in.setAddrType(AddressType.FROM_ADDR.value());
			in.setAddrSeq(1);
			EmailAddressVo addrvo = emailAddressDao.getByAddrId(vo.getFromAddrId());
			in.setAddrValue(addrvo.getEmailAddr());

			msgAddressDao.insert(in);
			rowsInserted++;
			
			in.setAddrType(AddressType.TO_ADDR.value());
			in.setAddrSeq(2);
			addrvo = emailAddressDao.getByAddrId(vo.getToAddrId());
			in.setAddrValue(addrvo.getEmailAddr());

			msgAddressDao.insert(in);
			rowsInserted++;
		}

		System.out.println("load() completed, rows inserted = " + rowsInserted);
	}
	
	void load(MsgAttachmentDao attachmentrsDao) {
		MsgAttachmentVo in = new MsgAttachmentVo();
		
		in.setMsgId(msgId);
		in.setAttchmntDepth(1);
		in.setAttchmntSeq(1);
		in.setAttchmntName("test.txt");
		in.setAttchmntType("text/plain; name=\"test.txt\"");
		in.setAttchmntDisp(Part.ATTACHMENT);
		in.setAttchmntValue("Test blob content goes here.".getBytes());

		attachmentrsDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(MsgHeaderDao msgHeaderDao) {
		MsgHeaderVo in = new MsgHeaderVo();
		
		in.setMsgId(msgId);
		in.setHeaderSeq(1);
		in.setHeaderName(XHeaderName.MAILER.value());
		in.setHeaderValue("MailSender");

		msgHeaderDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(MsgRfcFieldDao msgRfcFieldDao) {
		MsgRfcFieldVo in = new MsgRfcFieldVo();
		
		in.setMsgId(msgId);
		in.setRfcType(VariableName.RFC822.value());
		in.setRfcStatus(null);
		in.setRfcAction(null);
		in.setFinalRcpt(null);
		in.setFinalRcptId(null);
		in.setOrigRcpt(null);
		in.setOrigMsgSubject("original subject");
		in.setMessageId("messageId");
		in.setDsnText("dsn Text");
		in.setDsnRfc822("dsn Rfc822");
		in.setDlvrStatus(null);

		msgRfcFieldDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	private void loadMessageStream(MsgStreamDao dao, MsgInboxVo inbox) {
		// test insert
		MsgStreamVo strm1 = new MsgStreamVo();
		strm1.setMsgId(inbox.getMsgId());
		strm1.setMsgSubject(inbox.getMsgSubject());
		strm1.setFromAddrId(inbox.getFromAddrId());
		strm1.setToAddrId(inbox.getToAddrId());
		strm1.setMsgStream(loadFromSamples("BouncedMail_1.txt"));
		dao.insert(strm1);
	}
	
	protected byte[] loadFromSamples(String fileName) {
		return FileUtil.loadFromFile("bouncedmails", fileName);
	}

	
	private void loadRenderTables(ApplicationContext factory) {
		MsgRenderedDao renderedDao = factory.getBean(MsgRenderedDao.class);
		MsgSourceDao msgSourceDao = factory.getBean(MsgSourceDao.class);
		BodyTemplateDao bodyTmpltDao = factory.getBean(BodyTemplateDao.class);
		SubjTemplateDao subjTmpltDao = factory.getBean(SubjTemplateDao.class);
		
		List<MsgSourceVo> srcList = msgSourceDao.getAll();
		MsgRenderedVo vo = new MsgRenderedVo();
		vo.setMsgSourceId(srcList.get(0).getMsgSourceId());
		vo.setClientId(Constants.DEFAULT_CLIENTID);
		List<BodyTemplateVo> bodyTmpltList = bodyTmpltDao.getByClientId(Constants.DEFAULT_CLIENTID);
		List<SubjTemplateVo> subjTmpltList = subjTmpltDao.getByClientId(Constants.DEFAULT_CLIENTID);;
		vo.setBodyTemplateId(bodyTmpltList.get(0).getTemplateId());
		vo.setSubjTemplateId(subjTmpltList.get(0).getTemplateId());
		vo.setStartTime(new Timestamp(System.currentTimeMillis()));
		vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		renderedDao.insert(vo);
	}

}
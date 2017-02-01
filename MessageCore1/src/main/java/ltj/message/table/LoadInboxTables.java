package ltj.message.table;

import java.sql.Timestamp;
import java.util.List;

import javax.mail.Part;

import org.springframework.context.ApplicationContext;

import ltj.message.constant.CarrierCode;
import ltj.message.constant.Constants;
import ltj.message.constant.EmailAddressType;
import ltj.message.constant.MsgDirectionCode;
import ltj.message.constant.MsgStatusCode;
import ltj.message.constant.RuleNameType;
import ltj.message.constant.VariableName;
import ltj.message.constant.XHeaderName;
import ltj.message.dao.client.ClientUtil;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.inbox.AttachmentsDao;
import ltj.message.dao.inbox.MsgAddrsDao;
import ltj.message.dao.inbox.MsgClickCountsDao;
import ltj.message.dao.inbox.MsgHeadersDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.dao.inbox.RfcFieldsDao;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.message.dao.outbox.MsgSequenceDao;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.message.dao.template.MsgSourceDao;
import ltj.message.dao.template.SubjTemplateDao;
import ltj.message.util.FileUtil;
import ltj.message.vo.ClientVo;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.AttachmentsVo;
import ltj.message.vo.inbox.MsgAddrsVo;
import ltj.message.vo.inbox.MsgClickCountsVo;
import ltj.message.vo.inbox.MsgHeadersVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.RfcFieldsVo;
import ltj.spring.util.SpringUtil;
import ltj.vo.outbox.MsgRenderedVo;
import ltj.vo.outbox.MsgStreamVo;
import ltj.vo.template.BodyTemplateVo;
import ltj.vo.template.MsgSourceVo;
import ltj.vo.template.SubjTemplateVo;

public class LoadInboxTables {
	static long msgId = -1L;
	MsgSequenceDao msgSequenceDao;
	EmailAddrDao emailAddrDao;
	MsgClickCountsDao msgClickCountsDao;
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
		emailAddrDao = factory.getBean(EmailAddrDao.class);
		msgClickCountsDao = factory.getBean(MsgClickCountsDao.class);
		MsgInboxDao msgInboxDao = factory.getBean(MsgInboxDao.class);
		MsgAddrsDao msgAddrsDao = factory.getBean(MsgAddrsDao.class);
		AttachmentsDao attachmentsDao = factory.getBean(AttachmentsDao.class);
		MsgHeadersDao msgHeadersDao = factory.getBean(MsgHeadersDao.class);
		RfcFieldsDao rfcFieldsDao = factory.getBean(RfcFieldsDao.class);
		MsgStreamDao msgStreamDao = factory.getBean(MsgStreamDao.class);
		
		msgId = load(msgInboxDao);
		load(msgAddrsDao);
		load(attachmentsDao);
		load(msgHeadersDao);
		load(rfcFieldsDao);
		MsgInboxVo inbox = msgInboxDao.getByPrimaryKey(msgId);
		loadMessageStream(msgStreamDao, inbox);
		loadRenderTables(factory);
	}
	
	long load(MsgInboxDao msgInboxDao) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		MsgInboxVo in = new MsgInboxVo();
		
		long msgId = msgSequenceDao.findNextValue();
		in.setMsgId(msgId);
		in.setMsgRefId(null);
		in.setLeadMsgId(msgId);
		in.setCarrierCode(CarrierCode.SMTPMAIL);
		in.setMsgDirection(MsgDirectionCode.MSG_RECEIVED);
		in.setMsgSubject("Test Subject");
		in.setMsgPriority("2 (Normal)");
		in.setReceivedTime(updtTime);
		in.setFromAddrId(Long.valueOf(1));
		in.setReplyToAddrId(null);
		ClientVo clientVo = ClientUtil.getDefaultClientVo();
		String addr = clientVo.getReturnPathLeft() + "@" + clientVo.getDomainName();
		EmailAddrVo addrVo = emailAddrDao.findByAddress(addr);
		in.setToAddrId(addrVo.getEmailAddrId());
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setCustId(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setLockTime(null);
		in.setLockId(null);
		in.setRuleName(RuleNameType.GENERIC.toString());
		in.setMsgContentType("multipart/mixed");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Message Body");

		msgInboxDao.insert(in);
		
		msgId = msgSequenceDao.findNextValue();
		in.setMsgId(msgId);
		in.setMsgRefId(null);
		in.setLeadMsgId(msgId);
		in.setCarrierCode(CarrierCode.SMTPMAIL);
		in.setMsgDirection(MsgDirectionCode.MSG_SENT);
		in.setMsgSubject("Test Broadcast Subject");
		in.setMsgPriority("2 (Normal)");
		in.setReceivedTime(updtTime);
		addrVo = emailAddrDao.findByAddress("demolist1@localhost");
		in.setFromAddrId(addrVo.getEmailAddrId());
		in.setReplyToAddrId(null);
		in.setToAddrId(addrVo.getEmailAddrId());
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setCustId(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setLockTime(null);
		in.setLockId(null);
		in.setRuleName(RuleNameType.BROADCAST.toString());
		in.setMsgContentType("text/plain");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Broadcast Message Body");
		in.setStatusId(MsgStatusCode.CLOSED);

		msgInboxDao.insert(in);

		MsgClickCountsVo in2 = new MsgClickCountsVo();
		
		in2.setMsgId(msgId);
		in2.setListId("SMPLLST1");
		in2.setSentCount(1);
		in2.setOpenCount(1);
		in2.setClickCount(1);
		in2.setLastOpenTime(updtTime);
		in2.setLastClickTime(updtTime);

		msgClickCountsDao.insert(in2);

		System.out.println("load() completed.\n");
		return in.getMsgId();
	}
	
	void load(MsgAddrsDao msgAddrsDao) {
		MsgAddrsVo in = new MsgAddrsVo();
		
		in.setMsgId(msgId);
		in.setAddrType(EmailAddressType.FROM_ADDR);
		in.setAddrSeq(1);
		in.setAddrValue("test@test.com");

		msgAddrsDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(AttachmentsDao attachmentrsDao) {
		AttachmentsVo in = new AttachmentsVo();
		
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
	
	void load(MsgHeadersDao msgHeadersDao) {
		MsgHeadersVo in = new MsgHeadersVo();
		
		in.setMsgId(msgId);
		in.setHeaderSeq(1);
		in.setHeaderName(XHeaderName.XHEADER_MAILER);
		in.setHeaderValue("MailSender");

		msgHeadersDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(RfcFieldsDao rfcFieldsDao) {
		RfcFieldsVo in = new RfcFieldsVo();
		
		in.setMsgId(msgId);
		in.setRfcType(VariableName.RFC822);
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

		rfcFieldsDao.insert(in);

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
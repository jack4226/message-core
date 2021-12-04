package ltj.message.bo.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanBuilder;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bean.MsgHeader;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.inbox.MsgInboxBo;
import ltj.message.bo.outbox.MsgOutboxBo;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.idtokens.EmailIdParser;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.spring.util.SpringAppConfig;
import ltj.spring.util.SpringJmsConfig;
import ltj.vo.outbox.MsgStreamVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringAppConfig.class, SpringJmsConfig.class})
// deprecated since 4.2
//@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
@Rollback(true)
public class BoTestBase {
	protected static final Logger logger = LogManager.getLogger(BoTestBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();
	protected final static String LF = System.getProperty("line.separator","\n");
	protected static final long renderId = 1L;

	@Resource
	protected MsgInboxBo msgInboxBo;
	@Resource
	protected MsgOutboxBo msgOutboxBo;
	@Resource
	protected MsgStreamDao msgStreamDao;
	@Resource
	protected MessageParser parser;
	@Resource
	protected MsgInboxDao msgInboxDao;
	@Resource
	protected EmailAddressDao emailAddressDao;
	
	protected static long WaitTimeInMillis = 2 * 1000L;
	
	protected static boolean enableJunitRunClasses = false;
	
	@BeforeClass
	public static void prepare() {
	}
	
	@Before
	public void checkMsgrendered() throws AddressException, DataValidationException, ParseException {
		if (msgOutboxBo.getMessageByPK(renderId) == null && enableJunitRunClasses) {
			/* 
			 * XXX this will fail with following errors from Spring Framework:
			 * java.lang.IllegalStateException: Cannot start a new transaction without ending the existing transaction.
			 */
			Result result = JUnitCore.runClasses(MsgOutboxBoTest.class);
			for (Failure failure : result.getFailures()) {
				logger.error(failure.toString());
			}
			if (result.getFailureCount() > 0) {
				fail("Test failed, number of failures = " + result.getFailureCount());
			}
		}
	}
	
	protected Message createMimeMessage(byte[] mailStream) throws MessagingException {
		Message msg = MessageBeanUtil.createMimeMessage(mailStream);
		return msg;
	}

	protected MessageBean createMessageBean(Message msg) throws MessagingException {
		MessageBean msgBean = MessageBeanBuilder.processPart(msg, null);
		return msgBean;
	}
	
	protected MessageBean buildMessageBeanFromMsgStream() throws Exception {
		MsgStreamVo msgStreamVo = msgStreamDao.getRandomRecord();
		if (msgStreamVo == null) {
			addMsgToInbox();
			msgStreamVo = msgStreamDao.getLastRecord();
		}
		assertNotNull(msgStreamVo);
		Message msg = createMimeMessage(msgStreamVo.getMsgStream());
		MessageBean messageBean = createMessageBean(msg);
		messageBean.setMsgId(msgStreamVo.getMsgId()); // to be converted as MsgRefId
		if (messageBean.getMsgRefId() == null) {
			messageBean.setMsgRefId(messageBean.getMsgId());
		}
		Address[] fromAddr = {new InternetAddress("botest.from@test.com")};
		messageBean.setFrom(fromAddr);
		Address[] toAddr = {new InternetAddress("botest.to@test.com")};
		messageBean.setTo(toAddr);
		messageBean.setFinalRcpt("botest.finalrcpt@test.com");
		return messageBean;
	}

	protected MsgInboxVo selectMsgInboxByMsgId(long msgId) {
		MsgInboxVo vo = msgInboxDao.getByPrimaryKey(msgId);
		if (vo!=null) {
			System.out.println("MsgInboxDao - selectMsgInboxByMsgId: "+LF+vo);
		}
		return vo;
	}

	protected List<MsgInboxWebVo> selectMsgInboxByMsgRefId(long msgRefId) {
		List<MsgInboxWebVo> list = msgInboxDao.getByMsgRefId(msgRefId);
		for (MsgInboxWebVo vo : list) {
			logger.info("MsgInboxDao - selectMsgInboxByMsgRefId: "+LF+vo);
		}
		return list;
	}

	protected EmailAddressVo selectEmailAddrByAddress(String emailAddr) {
		EmailAddressVo addrVo = emailAddressDao.findByAddress(emailAddr);
		logger.info("EmailAddressDao - selectEmailAddrByAddress: "+LF+addrVo);
		return addrVo;
	}

	protected MessageBean addMsgToInbox() throws Exception {
		long msgRefId = 1L;
		try {
			MessageBean messageBean = msgOutboxBo.getMessageByPK(renderId);
			assertNotNull(messageBean);
			if (isDebugEnabled) {
				logger.debug("MessageBean returned:" + LF + messageBean);
			}
			parser.parse(messageBean);
			
			// build MsgHeader
			MsgHeader header = new MsgHeader();
			header.setName(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
			header.setValue(EmailIdParser.getDefaultParser().wrapupEmailId4XHdr(msgRefId));
			List<MsgHeader> headers = new ArrayList<MsgHeader>();
			headers.add(header);
			messageBean.setHeaders(headers);
			
			if (isDebugEnabled) {
				logger.debug("MessageBean After:" + LF + messageBean);
			}
			if (messageBean.getRuleName()==null) {
				messageBean.setRuleName(RuleNameEnum.GENERIC.name());
			}
			long msgId = msgInboxBo.saveMessage(messageBean);
			logger.info("msgInboxBo.saveMessage - MsgId returned: " + msgId);
			MsgInboxVo vo = msgInboxBo.getAllDataByMsgId(msgId);
			assertNotNull(vo);
			MessageBean msgBean = MessageBeanBuilder.createMessageBean(vo);
			return msgBean;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/*
	 * Define assignable rule names
	 */
	protected final static RuleNameEnum[] RuleNames = {
			RuleNameEnum.HARD_BOUNCE,
			RuleNameEnum.SOFT_BOUNCE,
			RuleNameEnum.MAILBOX_FULL,
			RuleNameEnum.MAIL_BLOCK,
			RuleNameEnum.SPAM_BLOCK,
			RuleNameEnum.VIRUS_BLOCK,
			//RuleNameEnum.CSR_REPLY, // TODO must add "Original Message" to message bean
			RuleNameEnum.RMA_REQUEST
	};
}

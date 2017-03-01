package ltj.message.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskDispatcher;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.CodeType;
import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.SubscriptionVo;
import ltj.message.vo.inbox.MsgInboxVo;

@FixMethodOrder
public class BroadcastTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(BroadcastTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	
	@Resource
	private MessageParser messageParser;
	@Resource
	private TaskDispatcher taskDispatcher;
	@Resource
	private SubscriptionDao subscriptionDao;
	@Resource
	private MailingListDao mailingListDao;
	
	private static String bcstAddr = "testto@localhost";
	private static String suffix = StringUtils.leftPad(new Random().nextInt(10000)+"", 4, '0');
	private static int sizeBefore = 0;

	private static String sbsrAddr = "sbsr" + StringUtils.leftPad(new Random().nextInt(1000)+"", 3, '0') + "@ltj.com";
	private static String listAddr = Constants.DEMOLIST2_ADDR;
	
	@Test
	@Rollback(value=false)
	public void test1() { // broadcast
		EmailAddrVo addrVo = emailAddrDao.findByAddress(bcstAddr);
		List<MsgInboxVo> milist1 = msgInboxDao.getByToAddrId(addrVo.getEmailAddrId());
		sizeBefore = milist1.size();
		
		logger.info("=================================================");
		logger.info("Testing Broadcast ###############################");
		logger.info("=================================================");
		try {
			MessageBean messageBean = new MessageBean();
			messageBean.setIsReceived(true);
			messageBean.setFrom(InternetAddress.parse("support@localhost"));
			messageBean.setTo(InternetAddress.parse(bcstAddr));
			messageBean.setSubject("Test Broadcast message - " + suffix);
			messageBean.setBody("Test Broadcast message body.");
			messageBean.setRuleName(RuleNameEnum.BROADCAST.name());
			messageBean.setMailingListId(Constants.DEMOLIST1_NAME);
			messageBean.setBody("Dear ${CustomerName}:" + LF + messageBean.getBody());
			messageParser.parse(messageBean);
			System.out.println("MessageBean:" + LF + messageBean);
			taskDispatcher.dispatchTasks(messageBean);
		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail();
		}
	}
	
	@Test
	public void test2() {
		// verify results
		EmailAddrVo addrVo = emailAddrDao.getByAddress(bcstAddr);
		assertNotNull(addrVo);
		List<MsgInboxVo> milist2 = msgInboxDao.getByToAddrId(addrVo.getEmailAddrId());
		assertEquals(sizeBefore + 1, milist2.size());
		MsgInboxVo mivo = milist2.get(milist2.size() - 1);
		assertEquals(RuleNameEnum.BROADCAST.name(), mivo.getRuleName());
		assertEquals("Test Broadcast message - " + suffix, mivo.getMsgSubject());
	}
	
	@Test
	@Rollback(value=false)
	public void test3() { // subscribe
		logger.info("=================================================");
		logger.info("Testing Subscribe ###############################");
		logger.info("=================================================");
		try {
			MessageBean messageBean = new MessageBean();
			messageBean.setIsReceived(true);
			messageBean.setFrom(InternetAddress.parse(sbsrAddr));
			messageBean.setTo(InternetAddress.parse(listAddr));
			messageBean.setSubject("subscribe");
			messageBean.setBody("sign me up to the email mailing list");
			messageParser.parse(messageBean);
			taskDispatcher.dispatchTasks(messageBean);
		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail();
		}
	}
	
	@Test
	public void test4() {
		// verify result
		List<MailingListVo> mllist = mailingListDao.getByAddress(listAddr);
		assertFalse(mllist.isEmpty());
		SubscriptionVo subsVo = subscriptionDao.getByAddrAndListId(sbsrAddr, mllist.get(0).getListId());
		assertNotNull(subsVo);
		assertEquals(CodeType.Y.name(), subsVo.getSubscribed());
	}

	@Test
	@Rollback(value=false)
	public void test5() { // unsubscribe
		logger.info("=================================================");
		logger.info("Testing Unsubscribe ###############################");
		logger.info("=================================================");
		try {
			MessageBean messageBean = new MessageBean();
			messageBean.setIsReceived(true);
			messageBean.setFrom(InternetAddress.parse(sbsrAddr));
			messageBean.setTo(InternetAddress.parse(listAddr));
			messageBean.setSubject("unsubscribe");
			messageBean.setBody("remove me from the email mailing list");
			messageParser.parse(messageBean);
			taskDispatcher.dispatchTasks(messageBean);
		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail();
		}
	}
	
	@Test
	public void test6() {
		// verify result
		List<MailingListVo> mllist = mailingListDao.getByAddress(listAddr);
		assertFalse(mllist.isEmpty());
		SubscriptionVo subsVo = subscriptionDao.getByAddrAndListId(sbsrAddr, mllist.get(0).getListId());
		assertNotNull(subsVo);
		assertEquals(CodeType.N.name(), subsVo.getSubscribed());
	}
}

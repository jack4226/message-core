package ltj.message.test;

import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskDispatcher;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.RuleNameType;

@FixMethodOrder
public class BroadcastTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(BroadcastTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	
	//@Resource
	//private MsgInboxBo msgInboxBo;
	@Resource
	private MessageParser messageParser;
	@Resource
	private TaskDispatcher taskDispatcher;
	
	private static String sbsrAddr = "sbsr" + StringUtils.leftPad(new Random().nextInt(1000)+"", 3, '0') + "@localhost";
	
	@Test
	@Rollback(value=false)
	public void test1() throws Exception { // broadcast
		logger.info("=================================================");
		logger.info("Testing Broadcast ###############################");
		logger.info("=================================================");
		MessageBean messageBean = new MessageBean();
		messageBean.setIsReceived(true);
		messageBean.setFrom(InternetAddress.parse("support@localhost"));
		messageBean.setTo(InternetAddress.parse("testto@localhost"));
		messageBean.setSubject("Test Broadcast message");
		messageBean.setBody("Test Broadcast message body.");
		messageBean.setRuleName(RuleNameType.BROADCAST.toString());
		messageBean.setMailingListId("SMPLLST1");
		messageBean.setBody("Dear ${CustomerName}:" + LF + messageBean.getBody());
		messageParser.parse(messageBean);
		System.out.println("MessageBean:" + LF + messageBean);
		taskDispatcher.dispatchTasks(messageBean);
	}
	
	@Test
	//@Rollback(value=false)
	public void test2() throws Exception { // subscribe
		logger.info("=================================================");
		logger.info("Testing Subscribe ###############################");
		logger.info("=================================================");
		MessageBean messageBean = new MessageBean();
		messageBean.setIsReceived(true);
		messageBean.setFrom(InternetAddress.parse(sbsrAddr));
		messageBean.setTo(InternetAddress.parse("demolist1@localhost"));
		messageBean.setSubject("subscribe");
		messageBean.setBody("sign me up to the email mailing list");
		messageParser.parse(messageBean);
		taskDispatcher.dispatchTasks(messageBean);
	}

	@Test
	//@Rollback(value=false)
	public void test3() throws Exception { // unsubscribe
		logger.info("=================================================");
		logger.info("Testing Unsubscribe ###############################");
		logger.info("=================================================");
		MessageBean messageBean = new MessageBean();
		messageBean.setIsReceived(true);
		messageBean.setFrom(InternetAddress.parse(sbsrAddr));
		messageBean.setTo(InternetAddress.parse("demolist1@localhost"));
		messageBean.setSubject("unsubscribe");
		messageBean.setBody("remove me from the email mailing list");
		messageParser.parse(messageBean);
		taskDispatcher.dispatchTasks(messageBean);
	}
}

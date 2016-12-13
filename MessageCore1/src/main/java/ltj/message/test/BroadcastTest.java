package ltj.message.test;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskDispatcher;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.RuleNameType;

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
	
	@Test
	@Rollback(false)
	public void broadcast() throws Exception {
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
	public void subscribe() throws Exception {
		logger.info("=================================================");
		logger.info("Testing Subscribe ###############################");
		logger.info("=================================================");
		MessageBean messageBean = new MessageBean();
		messageBean.setIsReceived(true);
		messageBean.setFrom(InternetAddress.parse("test@test.com"));
		messageBean.setTo(InternetAddress.parse("jwang@localhost"));
		messageBean.setSubject("subscribe");
		messageBean.setBody("sign me up to the email mailing list");
		messageParser.parse(messageBean);
		taskDispatcher.dispatchTasks(messageBean);
	}

	@Test
	public void unsubscribe() throws Exception {
		logger.info("=================================================");
		logger.info("Testing Unsubscribe ###############################");
		logger.info("=================================================");
		MessageBean messageBean = new MessageBean();
		messageBean.setIsReceived(true);
		messageBean.setFrom(InternetAddress.parse("test@test.com"));
		messageBean.setTo(InternetAddress.parse("jwang@localhost"));
		messageBean.setSubject("unsubscribe");
		messageBean.setBody("remove mefrom the email mailing list");
		messageParser.parse(messageBean);
		taskDispatcher.dispatchTasks(messageBean);
	}
}

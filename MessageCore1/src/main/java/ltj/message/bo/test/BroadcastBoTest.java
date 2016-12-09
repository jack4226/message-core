package ltj.message.bo.test;

import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.jbatch.queue.JmsProcessor;
import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.bo.TaskScheduler;
import ltj.message.bo.template.RenderBo;
import ltj.message.bo.template.RenderRequest;
import ltj.message.bo.template.RenderResponse;
import ltj.message.bo.template.RenderVariable;
import ltj.message.constant.Constants;
import ltj.message.constant.RuleNameType;
import ltj.message.exception.DataValidationException;

/*** Please start MailEngine and MailReader before running this test ***/
public class BroadcastBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo broadcastBo;
	@Resource
	private TaskBaseBo mailingListRegExBo;
	@Resource
	private RenderBo util;
	@Test
	@Rollback(false)
	public void broadcast() throws Exception {
		try {
			MessageBean msgBean = buildMessageBeanFromMsgStream();
			MessageBean messageBean = render();
			messageBean.setMsgId(msgBean.getMsgId());
			messageBean.setTo(msgBean.getTo());
			messageBean.setFinalRcpt(msgBean.getFinalRcpt());
			messageBean.setRuleName(RuleNameType.BROADCAST.toString());
			messageBean.setBody("Dear ${CustomerName}:\n" + messageBean.getBody());
			if (isDebugEnabled) {
				logger.debug("MessageBean created:" + LF + messageBean);
			}
			JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory()
					.getBean("jmsProcessor");
			broadcastBo.setJmsProcessor(jmsProcessor);
			broadcastBo.setTaskArguments("SMPLLST2");
			broadcastBo.process(messageBean);
			String regEx = (String) mailingListRegExBo.process(messageBean);
			logger.info("RegEx: " + regEx + ", " + "test-list@domain.com".matches(regEx));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private MessageBean render() throws AddressException, DataValidationException, ParseException {
		RenderRequest req = new RenderRequest(
				"WeekendDeals",
				Constants.DEFAULT_CLIENTID,
				new Timestamp(new java.util.Date().getTime()),
				new HashMap<String, RenderVariable>()
				);
		RenderResponse rsp = util.getRenderedEmail(req);
		assertNotNull(rsp);
		logger.info(rsp);
		return rsp.getMessageBean();
	}

	public void broadcastRuleEngine() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setRuleName(RuleNameType.BROADCAST.toString());
		messageBean.setMailingListId("SMPLLST1");
		messageBean.setBody("Dear ${CustomerName}:\n" + messageBean.getBody());
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		JmsProcessor jmsProcessor = (JmsProcessor) TaskScheduler.getMailSenderFactory().getBean(
				"jmsProcessor");
		// send the bean back to Rule Engine input queue
		//jmsProcessor.setQueueName(""); // TODO set queue name
		String jmsMsgId = jmsProcessor.writeMsg(messageBean);
		logger.info("Jms Message Id returned: " + jmsMsgId);
	}
}

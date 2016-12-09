package ltj.message.task;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.bo.template.RenderBo;
import ltj.message.bo.template.RenderRequest;
import ltj.message.bo.template.RenderResponse;
import ltj.message.bo.template.RenderVariable;
import ltj.message.bo.test.BoTestBase;
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
	public void broadcast() {
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
			broadcastBo.getJmsProcessor().setQueueName("mailSenderInput");
			broadcastBo.setTaskArguments("SMPLLST2");
			broadcastBo.process(messageBean);
			String regEx = (String) mailingListRegExBo.process(messageBean);
			logger.info("RegEx: " + regEx + ", " + "test-list@domain.com".matches(regEx));
			// TODO verify results
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
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

	@Test
	public void broadcastRuleEngine() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setRuleName(RuleNameType.BROADCAST.toString());
		messageBean.setMailingListId("SMPLLST1");
		messageBean.setBody("Dear ${CustomerName}:\n" + messageBean.getBody());
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		// send the bean back to Rule Engine input queue
		broadcastBo.getJmsProcessor().setQueueName("mailReaderOutput"); // TODO get from properties
		String jmsMsgId = broadcastBo.getJmsProcessor().writeMsg(messageBean);
		logger.info("Jms Message Id returned: " + jmsMsgId);
		// TODO verify results
	}
}

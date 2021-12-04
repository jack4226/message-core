package ltj.message.bo.task;

import java.util.List;

import javax.jms.JMSException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.data.preload.QueueNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MsgHeader;
import ltj.message.constant.XHeaderName;
import ltj.message.exception.DataValidationException;

@Component("assignRuleNameBo")
@Scope(value="prototype")
@Lazy(value=true)
public class AssignRuleNameBoImpl extends TaskBaseAdaptor {
	static final Logger logger = LogManager.getLogger(AssignRuleNameBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * reset the rule name to the value from TaskArguments field and re-queue
	 * the message to ruleEnginInput queue.
	 * 
	 * @see ltj.message.bo.task.TaskBaseBo#process(ltj.message.bean.MessageBean)
	 * 
	 * @return the JMS Message Id (a String) from the message that were sent to
	 *         the ruleEngineInput queue.
	 */
	public Object process(MessageBean messageBean) throws DataValidationException, JMSException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (getArgumentList().size() == 0) {
			throw new DataValidationException("Arguments is not valued, can't proceed");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		// save original Rule Name to an X-header
		if (messageBean.getRuleName() != null) {
			List<MsgHeader> headers = messageBean.getHeaders();
			MsgHeader newHeader = new MsgHeader();
			newHeader.setName(XHeaderName.ORIG_RULE_NAME.value());
			newHeader.setValue(messageBean.getRuleName());
			headers.add(newHeader);
		}
		// Assign a new Rule Name
		messageBean.setRuleName(getArgumentList().get(0));
		messageBean.setIsReceived(true);
		if (messageBean.getMsgRefId() == null) {
			// append to the thread
			messageBean.setMsgRefId(messageBean.getMsgId());
		}
		// send the bean back to Rule Engine input queue
		jmsProcessor.setQueueName(QueueNameEnum.MAIL_READER_OUTPUT.getQueueName());

		
		String correlid = "AssignRuleNameBo." + (messageBean.getMsgRefId() == null ? "-1" : messageBean.getMsgRefId());
			// set correlation id. To be used in the future.
		String jmsMsgId = jmsProcessor.writeMsg(messageBean, correlid, false);
		/*
		 * jmsMsgId returned from the 1st message could be used as correlation id
		 * by the subsequent messages in the same group
		 */
		if (isDebugEnabled) {
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		}
		return jmsMsgId;
	}
}

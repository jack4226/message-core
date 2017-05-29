package ltj.message.bo.task;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.data.preload.QueueNameEnum;
import ltj.jbatch.queue.JmsProcessor;

@Component("taskBaseBo")
@Scope(value="prototype")
public abstract class TaskBaseAdaptor implements TaskBaseBo {

	@Autowired
	protected JmsProcessor jmsProcessor;
	
	protected String[] taskArguments;
	
	@Override
	public JmsProcessor getJmsProcessor() {
		return jmsProcessor;
	}
	
	@Override
	public String[] getTaskArguments() {
		return taskArguments;
	}
	
	@Override
	public void setTaskArguments(String... taskArguments) {
		this.taskArguments = taskArguments;
	}
	
	protected void setTargetToMailSender() {
		// send MailSender queue
		jmsProcessor.setQueueName(QueueNameEnum.MAIL_SENDER_INPUT.getQueueName());
	}
	
	protected void setTargetToRuleEngine() {
		// send RuleEngine queue
		jmsProcessor.setQueueName(QueueNameEnum.MAIL_READER_OUTPUT.getQueueName());
	}
	
	protected void setTargetToCsrWorkQueue() {
		jmsProcessor.setQueueName(QueueNameEnum.CUSTOMER_CARE_INPUT.getQueueName());
	}
	
	protected void setTargetQueue(String queueName) {
		jmsProcessor.setQueueName(queueName);
	}
	
	/*
	 * Define convenience methods
	 */
	public List<String> getArgumentList() {
		return getArgumentList(taskArguments);
	}
	
	public static List<String> getArgumentList(String... taskArguments) {
		List<String> list = new ArrayList<String>();
		if (taskArguments != null) {
			for (String token : taskArguments) {
				if (token != null && token.trim().length() > 0) {
					list.add(token);
				}
			}
		}
		return list;
	}
}

package ltj.message.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.jbatch.queue.JmsProcessor;

@Component("taskBaseBo")
@Scope(value="prototype")
public abstract class TaskBaseAdaptor implements TaskBaseBo {

	@Autowired
	protected JmsProcessor jmsProcessor;
	
	protected String taskArguments;
	
	@Override
	public JmsProcessor getJmsProcessor() {
		return jmsProcessor;
	}
	
	@Override
	public String getTaskArguments() {
		return taskArguments;
	}
	
	@Override
	public void setTaskArguments(String taskArguments) {
		this.taskArguments = taskArguments;
	}
	
	protected void setTargetToMailSender() {
		// send MailSender queue
		jmsProcessor.setQueueName("mailSenderInput");
	}
	
	protected void setTargetToRuleEngine() {
		// send RuleEngine queue
		jmsProcessor.setQueueName("mailReaderOutput");
	}
	
	protected void setTargetToCsrWorkQueue() {
		jmsProcessor.setQueueName("customerCareInput");
	}
	
	protected void setTargetToCsrWorkQueue(String queueName) {
		jmsProcessor.setQueueName(queueName);
	}
	
	/*
	 * Define convenience methods
	 */
	public List<String> getArgumentList() {
		return getArgumentList(taskArguments);
	}
	
	public static List<String> getArgumentList(String taskArguments) {
		ArrayList<String> list = new ArrayList<String>();
		if (taskArguments != null) {
			StringTokenizer st = new StringTokenizer(taskArguments, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token != null && token.trim().length() > 0)
					list.add(token);
			}
		}
		return list;
	}
}

package ltj.message.bo.task;

import javax.jms.JMSException;
import javax.mail.MessagingException;

import ltj.jbatch.queue.JmsProcessor;
import ltj.message.bean.MessageBean;
import ltj.message.exception.DataValidationException;

public interface TaskBaseBo {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public Object process(MessageBean messageBean) throws DataValidationException, MessagingException, JMSException;
	
	public JmsProcessor getJmsProcessor();

	public String[] getTaskArguments();

	public void setTaskArguments(String... taskArguments);
}

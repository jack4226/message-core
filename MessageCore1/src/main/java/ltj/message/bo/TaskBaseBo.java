package ltj.message.bo;

import java.io.IOException;

import javax.jms.JMSException;
import javax.mail.MessagingException;

import ltj.jbatch.queue.JmsProcessor;
import ltj.message.bean.MessageBean;
import ltj.message.exception.DataValidationException;

public interface TaskBaseBo {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public Object process(MessageBean messageBean) throws DataValidationException,
			MessagingException, JMSException, IOException;
	
	public JmsProcessor getJmsProcessor();

	public String getTaskArguments();

	public void setTaskArguments(String taskArguments);
}

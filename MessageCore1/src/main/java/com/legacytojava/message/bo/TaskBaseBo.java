package com.legacytojava.message.bo;

import java.io.IOException;

import javax.jms.JMSException;
import javax.mail.MessagingException;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.exception.DataValidationException;

import ltj.jbatch.queue.JmsProcessor;

public interface TaskBaseBo {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public Object process(MessageBean messageBean) throws DataValidationException,
			MessagingException, JMSException, IOException;
	
	public void setJmsProcessor(JmsProcessor jmsProcessor);

	public String getTaskArguments();

	public void setTaskArguments(String taskArguments);
}

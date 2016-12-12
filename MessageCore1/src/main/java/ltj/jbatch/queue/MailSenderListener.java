package ltj.jbatch.queue;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ltj.jbatch.smtp.SmtpException;
import ltj.message.bean.MessageBean;
import ltj.message.bo.mailsender.MailSenderBoImpl;
import ltj.message.exception.DataValidationException;

public class MailSenderListener implements MessageListener {
	static final Logger logger = Logger.getLogger(MailSenderListener.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Autowired
	private JmsProcessor jmsProcessor;
	@Autowired
	private MailSenderBoImpl mailSenderBo;
	
	private @Value("${errorOutput.Queue}") String errorQueueName;
	
	public MailSenderListener() {
		logger.info("Entering construct...");
	}
	
	@Override
	public void onMessage(Message message) {
		logger.info("JMS Message Received: " + message);
		jmsProcessor.setQueueName(errorQueueName);
		long start = System.currentTimeMillis();
		if (message instanceof ObjectMessage) {
			try {
				String JmsMessageId = message.getJMSMessageID();
				Object obj = ((ObjectMessage)message).getObject();
				if (obj instanceof MessageBean) {
					MessageBean messageBean = (MessageBean) obj;
					try {
						mailSenderBo.process(messageBean);
					} catch (MessagingException e) {
						logger.error("onMessage() - MessageFormatException caught", e);
						jmsProcessor.writeMsg(messageBean, JmsMessageId, true);
					} catch (IOException e) {
						logger.error("onMessage() - IOException caught", e);
						jmsProcessor.writeMsg(messageBean, JmsMessageId, true);
					} catch (SmtpException e) {
						logger.error("onMessage() - SmtpException caught", e);
						throw new RuntimeException(e);
					} catch (InterruptedException e) {
						logger.error("onMessage() - InterruptedException caught", e);
						throw new RuntimeException(e);
					} catch (DataValidationException e) {
						logger.error("onMessage() - DataValidationException caught", e);
						jmsProcessor.writeMsg(messageBean, JmsMessageId, true);
					}
				}
				else {
					// Not a MessageBean instance
					logger.warn("Message object is not a MessageBean, cless name: " + obj.getClass().getName());
					jmsProcessor.writeMsg(message, JmsMessageId, true);
				}
			} catch (JMSException je) {
				logger.error("onMessage() - JMSException caught", je);
				throw new RuntimeException(je);
			}
			finally {
				/* Message processed, update processing time */
				long proc_time = System.currentTimeMillis() - start;
				logger.info("onMessage() ended. Time spent in milliseconds: " + proc_time);
			}
		}
		else {
			// Not an Object Message
			logger.warn("Message received is not an ObjectMessage: " + message.getClass().getName());
		}
	}

}

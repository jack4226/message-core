package ltj.jbatch.queue;

import java.io.ByteArrayOutputStream;

import javax.jms.BytesMessage;
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
	
	private @Value("${mailSenderOutput.Queue}") String mailSenderOutputQueue;
	private @Value("${errorOutput.Queue}") String errorQueueName;
	
	public MailSenderListener() {
		logger.info("Entering construct...");
	}
	
	@Override
	public void onMessage(Message message) {
		logger.info("JMS Message Received: " + message);
		jmsProcessor.setQueueName(mailSenderOutputQueue);
		long start = System.currentTimeMillis();
		try {
			String JmsMessageId = message.getJMSMessageID();
			if (message instanceof ObjectMessage) {
				Object obj = ((ObjectMessage)message).getObject();
				if (obj instanceof MessageBean) {
					MessageBean messageBean = (MessageBean) obj;
					logger.info("An ObjectMessage received.");
					try {
						mailSenderBo.process(messageBean);
					} catch (MessagingException e) {
						logger.error("onMessage() - MessageFormatException caught", e);
						jmsProcessor.writeMsg(messageBean, JmsMessageId, true);
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
			}
			else if (message instanceof BytesMessage) {
				BytesMessage msg = (BytesMessage) message;
				logger.info("A BytesMessage received.");
				byte[] buffer = new byte[1024];
				int len = 0;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ( (len = msg.readBytes(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				byte[] mailStream = baos.toByteArray();
				try {
					mailSenderBo.process(mailStream);
				} catch (MessagingException e) {
					logger.error("onMessage() - MessageFormatException caught", e);
					jmsProcessor.writeMsg(mailStream, JmsMessageId, true);
				} catch (DataValidationException e) {
					logger.error("onMessage() - DataValidationException caught", e);
					jmsProcessor.writeMsg(mailStream, JmsMessageId, true);
				}
			}
			else {
				// Not an Object Message nor a Bytes Message
				logger.warn("Message received is not an ObjectMessage nor a BytesMessage: " + message.getClass().getName());
				jmsProcessor.writeMsg(message, JmsMessageId, true);
			}
		} catch (SmtpException e) {
			logger.error("onMessage() - SmtpException caught", e);
			throw new RuntimeException(e);
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

}

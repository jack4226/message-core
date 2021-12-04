package ltj.message.bo.mailsender;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.ObjectMessage;
import javax.mail.Address;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ltj.jbatch.app.Processor;
import ltj.jbatch.pool.NamedPools;
import ltj.jbatch.queue.JmsProcessor;
import ltj.jbatch.smtp.SmtpConnection;
import ltj.jbatch.smtp.SmtpException;
import ltj.message.bean.MessageBean;
import ltj.message.exception.DataValidationException;

/**
 * @deprecated - replaced by MailSenderBoImpl
 * process queue messages handed over by QueueListener
 * 
 * @author Administrator
 */
public class MailSenderProcessor extends MailSenderBase implements Processor {
	static final Logger logger = LogManager.getLogger(MailSenderProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private JmsProcessor jmsProcessor;
	private JmsTransactionManager jmsTransactionManager;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * must use constructor without any parameters
	 */
	public MailSenderProcessor() {
		logger.info("Entering constructor...");
	}

	/**
	 * process request. Either a ObjectMessage contains a MessageBean or a
	 * BytesMessage contains a SMTP raw stream.
	 * 
	 * @param req -
	 *            a JMS message
	 * @throws JMSException 
	 * @throws InterruptedException 
	 * @throws SmtpException 
	 * @throws MessagingException 
	 */
	public void process(Object req) throws JMSException, MessagingException, SmtpException, InterruptedException {
		if (req == null) {
			logger.error("a null request was received.");
			return;
		}
		if (!(req instanceof Message)) {
			logger.error("Request received was not a JMS Message.");
			throw new IllegalArgumentException("Request was not a JMS Message as expected.");
		}
		
		// define transaction properties
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus tStatus = jmsTransactionManager.getTransaction(definition);

		boolean isJmsMsgWritten = false;
		// defined here to be used in catch blocks
		MessageBean msgBean = null;
		Message message = (Message) req;
		try {
			if (message instanceof ObjectMessage) {
				Object msgObj = ((ObjectMessage) message).getObject();
				if (msgObj == null) {
					throw new MessageFormatException("Object Message is Null.");
				}
				if (msgObj instanceof MessageBean) {
					msgBean = (MessageBean) msgObj;
					logger.info("A MessageBean object received.");
					
					process(msgBean);
				}
				else {
					logger.error("message was not a MessageBean as expected" + LF + message);
					jmsProcessor.writeJmsMsg(message, true);
					isJmsMsgWritten = true;
				}
			}
			else if (message instanceof BytesMessage) {
				// SMTP raw stream
				BytesMessage msg = (BytesMessage) message;
				logger.info("A BytesMessage received.");
				byte[] buffer = new byte[1024];
				int len = 0;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ( (len = msg.readBytes(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				byte[] mailStream = baos.toByteArray();
				
				process(mailStream);
			}
			else {
				logger.error("message was not a message type as expected" + LF + message);
				jmsProcessor.writeJmsMsg(message, true);
				isJmsMsgWritten = true;
			}
			// issue commit if any message has been written to queue.
			if (isJmsMsgWritten) {
				jmsTransactionManager.commit(tStatus);
				logger.info("Messages written to Queue were committed.");
			}
		}
		catch (DataValidationException | MessagingException | MessageFormatException | NullPointerException | IndexOutOfBoundsException | NumberFormatException ex) {
			// unrecoverable exception caught, send to error queue and commit.
			logger.error(ex.getClass().getSimpleName() + " caught", ex);
			jmsProcessor.writeJmsMsg(message, true);
			jmsTransactionManager.commit(tStatus);
		}
		catch (JMSException je) {
			logger.error("JMSException caught", je);
			// other JMS error, roll back and exit
			tStatus.setRollbackOnly();
			logger.error("JMSException caught", je);
			Exception e = je.getLinkedException();
			if (e != null) {
				logger.error("linked errortion", e);
			}
			throw je;
		}
		catch (SmtpException se) {
			logger.error("SmtpException caught", se);
			// SMTP error, roll back and exit
			tStatus.setRollbackOnly();
			throw se;
		}
	}

	/**
	 * Send the email off. <p>
	 * SMTP server properties are retrieved from database. 
	 * 
	 * @param msg -
	 *            message
	 * @param isSecure -
	 *            send via secure SMTP server when true
	 * @param errors -
	 *            contains delivery errors if any
	 * @throws InterruptedException
	 * @throws SmtpException
	 * @throws MessagingException
	 */
	public void sendMail(javax.mail.Message msg, boolean isSecure, Map<String, Address[]> errors)
			throws MessagingException, SmtpException {
		NamedPools smtp = SmtpWrapperUtil.getSmtpNamedPools();
		NamedPools secu = SmtpWrapperUtil.getSecuNamedPools();
		/* Send Message */
		SmtpConnection smtp_conn = null;
		if (isSecure && !secu.isEmpty() || smtp.isEmpty()) {
			try {
				smtp_conn = (SmtpConnection) secu.getConnection();
				smtp_conn.sendMail(msg, errors);
			}
			finally {
				if (smtp_conn != null) {
					secu.returnConnection(smtp_conn);
				}
			}
		}
		else {
			try {
				smtp_conn = (SmtpConnection) smtp.getConnection();
				smtp_conn.sendMail(msg, errors);
			}
			finally {
				if (smtp_conn != null) {
					smtp.returnConnection(smtp_conn);
				}
			}
		}
	}

	/**
	 * Send the email off via unsecured SMTP server. <p>
	 * SMTP server properties are retrieved from database. 
	 * 
	 * @param msg -
	 *            message
	 * @throws InterruptedException 
	 * @throws SmtpException 
	 * @throws MessagingException 
	 */
	public void sendMail(javax.mail.Message msg, Map<String, Address[]> errors)
			throws MessagingException, SmtpException {
		NamedPools smtp = SmtpWrapperUtil.getSmtpNamedPools();
		if (smtp.isEmpty()) {
			smtp = SmtpWrapperUtil.getSecuNamedPools();
		}
		SmtpConnection smtp_conn = null;
		try {
			smtp_conn = (SmtpConnection) smtp.getConnection();
			smtp_conn.sendMail(msg, errors);
		}
		finally {
			if (smtp_conn != null) {
				smtp.returnConnection(smtp_conn);
			}
		}
	}
	
	public JmsProcessor getJmsProcessor() {
		return jmsProcessor;
	}

	public void setJmsProcessor(JmsProcessor jmsProcessor) {
		this.jmsProcessor = jmsProcessor;
	}

	public JmsTransactionManager getJmsTransactionManager() {
		return jmsTransactionManager;
	}

	public void setJmsTransactionManager(JmsTransactionManager transactionManager) {
		this.jmsTransactionManager = transactionManager;
	}
}
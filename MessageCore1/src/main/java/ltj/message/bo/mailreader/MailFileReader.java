package ltj.message.bo.mailreader;

import java.util.List;

import javax.jms.JMSException;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ltj.jbatch.queue.JmsProcessor;
import ltj.message.bean.BodypartUtil;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bean.MessageNode;
import ltj.message.constant.CarrierCodeType;
import ltj.message.util.FileUtil;
import ltj.spring.util.SpringUtil;

public class MailFileReader {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MailFileReader.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public static void main(String[] args){
		String filePath = "SavedMailStreams/aim/mail17_bounced.txt";
		filePath = "SavedMailStreams/aim/mail18.txt";
		filePath = "SavedMailStreams/mailreader/rfc3798_MDN_sample.txt";
		//filePath = "SavedMailStreams/mailreader/rfc3464_DSN_sample4.txt";
		//filePath = "SavedMailStreams/mailreader/hard_bounce.txt";
		try {
			MailFileReader fReader = new MailFileReader();
			MessageBean msgBean = fReader.start(filePath);
			//MessageBean msgBean = fReader.readMessageBean(filePath);
			List<MessageNode> mNodes = BodypartUtil.retrieveAttachments(msgBean);
			logger.info("Number of Attachments: " + mNodes.size());
			logger.info("******************************");
			//logger.info("MessageBean created:" + LF + msgBean);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		System.exit(0);
	}
	
	MessageBean start(String filePath) throws MessagingException, JMSException {
		JmsProcessor jmsProcessor = (JmsProcessor) SpringUtil.getAppContext().getBean("jmsProcessor");
		jmsProcessor.setQueueName("mailReaderOutput");

		MessageBean msgBean = readMessageBean(filePath);
		msgBean.setCarrierCode(CarrierCodeType.SMTPMAIL_CODE.value());
		
		try {
			String msgId = jmsProcessor.writeMsg(msgBean, "MailReader");
			logger.info("JMS message written. Message Id returned: " + msgId);
			return msgBean;
		}
		catch (JMSException e) {
			logger.error("JMSException caught", e);
			throw e;
		}
	}
	
	private MessageBean readMessageBean(String filePath) throws MessagingException {
		byte[] mailStream = FileUtil.loadFromFile(filePath);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
	
	MessageBean start_v0(String filePath) throws MessagingException, JMSException {
		JmsTransactionManager jmsTransactionManager = (JmsTransactionManager) SpringUtil.getAppContext()
				.getBean("jmsTransactionManager");
		JmsProcessor jmsProcessor = (JmsProcessor) SpringUtil.getAppContext().getBean("jmsProcessor");
		
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		jmsProcessor.setQueueName("mailReaderOutput");

		MessageBean msgBean = readMessageBean(filePath);
		msgBean.setCarrierCode(CarrierCodeType.SMTPMAIL_CODE.value());
		
		TransactionStatus tStatus = null;
		try {
			tStatus = jmsTransactionManager.getTransaction(definition);
			String msgId = jmsProcessor.writeMsg(msgBean, "MailReader");
			logger.info("JMS message written. Message Id returned: " + msgId);
			jmsTransactionManager.commit(tStatus);
			
			return msgBean;
		}
		catch (JMSException e) {
			logger.error("JMSException caught", e);
			tStatus.setRollbackOnly();
			throw e;
		}
	}
}

package ltj.message.bo.mailreader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.jms.JMSException;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Transport;

import org.apache.log4j.Logger;

import ltj.jbatch.app.RunnableProcessor;
import ltj.jbatch.queue.JmsProcessor;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanBuilder;
import ltj.message.constant.MailCodeType;
import ltj.message.constant.Constants;
import ltj.message.vo.MailBoxVo;

/**
 * process email's handed over by MailReader class.
 * 
 * @author Administrator
 */
public class MailProcessor extends RunnableProcessor {
	static final Logger logger = Logger.getLogger(MailProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static Logger duplicateReport = Logger.getLogger("ltj.report.duplicate");

	private final JmsProcessor jmsProcessor;
	private final MailBoxVo mailBoxVo;
	private final DuplicateCheckDao duplicateCheck;
	
	//volatile boolean keepRunning = true;
	private final int MAX_INBOUND_BODY_SIZE = 150 * 1024;
	private final int MAX_INBOUND_CMPT_SIZE = 1024 * 1024;

	final static String LF = System.getProperty("line.separator", "\n");

	/**
	 * must use constructor with following parameters
	 */
	public MailProcessor(JmsProcessor jmsProcessor, MailBoxVo mailBoxVo, DuplicateCheckDao duplicateCheck) {
		this.jmsProcessor = jmsProcessor;
		this.mailBoxVo = mailBoxVo;
		this.duplicateCheck = duplicateCheck;
	}

	/**
	 * process request
	 * 
	 * @param req -
	 *            request object, must be a JavaMail Message array.
	 * @throws JMSException 
	 * @throws MessagingException
	 *             if any error
	 */
	public void process(Object req) throws JMSException, MessagingException {
		logger.info("Entering process() method...");

		if (req != null && req instanceof Message[]) {
			Message[] msgs = (Message[]) req;
			// Just dump out the new messages and set the delete flags
			for (int i = 0; i < msgs.length; i++) {
				if (msgs[i] != null && !msgs[i].isSet(Flags.Flag.SEEN) && !msgs[i].isSet(Flags.Flag.DELETED)) {
					try {
						processPart(msgs[i]);
					}
					catch (IllegalStateException e) {
						logger.error("IllegalStateException caught: " + e.getMessage());
						continue;
					}
				}
				// release the instance for GC, not working w/pop3
				// msgs[i]=null;
			}
		}
		else {
			logger.error("Request is not Message[] as expected: " + req);
		}
	}

	/**
	 * process message part
	 * 
	 * @param p -
	 *            part
	 * @throws MessagingException 
	 * @throws JMSException 
	 *             if any error
	 */
	MessageBean processPart(Part p) throws MessagingException, JMSException {
		long start_tms = System.currentTimeMillis();
		
		// parse the MimeMessage to MessageBean
		MessageBean msgBean = MessageBeanBuilder.processPart(p, mailBoxVo.getToAddrDomain());
		msgBean.setIsReceived(true);
		
		// mailbox carrierCode
		msgBean.setCarrierCode(mailBoxVo.getCarrierCode());
		// internal mail only flag
		msgBean.setInternalOnly(Constants.YES.equalsIgnoreCase(mailBoxVo.getInternalOnly()));
		// mailbox SSL flag
		msgBean.setUseSecureServer(Constants.YES.equalsIgnoreCase(mailBoxVo.getUseSsl()));
		// MailBox Host Address
		msgBean.setMailboxHost(mailBoxVo.getHostName());
		// MailBox User Id
		msgBean.setMailboxUser(mailBoxVo.getUserId());
		// MailBox Name
		msgBean.setMailboxName(mailBoxVo.getMailBoxDesc());
		// Folder Name
		msgBean.setFolderName(mailBoxVo.getFolderName());
		// to_plain_text indicator, default to "no"
		msgBean.setToPlainText("yes".equalsIgnoreCase(mailBoxVo.getToPlainText()));
		
		// for prototype only. Remove it.
		if (msgBean.getCarrierCode().toUpperCase().startsWith(MailCodeType.READONLY.value())) {
			msgBean.setCarrierCode(MailCodeType.SMTPMAIL.value());
		}
		// get original body w/o possible HTML to text conversion
		String body = msgBean.getBody(true);
		String contentType = msgBean.getBodyContentType();

		// check message body and component size
		boolean tooLarge = false;
		if (body.length() > MAX_INBOUND_BODY_SIZE) {
			tooLarge = true;
			logger.warn("Message body size exceeded limit: " + body.length());
		}
		if (msgBean.getComponentsSize().size() > 0) {
			for (int i = 0; i < msgBean.getComponentsSize().size(); i++) {
				Integer objSize = (Integer) msgBean.getComponentsSize().get(i);
				if (objSize.intValue() > MAX_INBOUND_CMPT_SIZE) {
					tooLarge = true;
					logger.warn("Message component(" + i + ") exceeded limit: " + objSize.intValue());
					break;
				}
			}
		}
		
		if (tooLarge) {
			try {
				// return the mail
				Message reply = new MailReaderReply().composeReply((Message) p, body, contentType);
				Transport.send(reply);
				logger.error("The email message has been rejected due to its size");
			}
			catch (MessagingException e) {
				logger.error("MessagingException caught during reply, drop the email", e);
			}
		}
		else { // email size within the limit
			boolean isDuplicate = false;
			// check for duplicate
			if (msgBean.getSmtpMessageId() != null && "yes".equalsIgnoreCase(mailBoxVo.getCheckDuplicate())) {
				isDuplicate = duplicateCheck.isDuplicate(msgBean.getSmtpMessageId());
			}
			else {
				logger.error("SMTP Message-id is null, FROM: " + msgBean.getFromAsString());
			}
			// end of check
			if (isDuplicate) {
				logger.error("Duplicate Message received, messageId: " + msgBean.getSmtpMessageId());

				// write raw stream to logging file
				if ("yes".equalsIgnoreCase(mailBoxVo.getLogDuplicate())) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try {
						p.writeTo(baos);
					} catch (IOException e) {
						logger.error("IOException caught, ignored", e);
					}
					duplicateReport.info("<========== Message-id: " + msgBean.getSmtpMessageId() + ", DateTime: "
							+ (new Date()) + " ==========>");
					duplicateReport.info(baos.toString());
					logger.error("The duplicate Message has been written to report file");
				}
			}
			else { // write to ruleEngine input queue
				String msgId = jmsProcessor.writeMsg(msgBean, "MailReader");
				logger.info("JMS message written. Message Id returned: " + msgId);
			}
			// jmsProcessor.writeMsg("Test error message", "MailReader",true);
		}
		logger.info("Number of attachments: " + msgBean.getAttachCount());

		// message has been sent, delete it from mail box
		// keep the message if it's from notes
		if (!MailCodeType.READONLY.value().equalsIgnoreCase(mailBoxVo.getCarrierCode())) {
			((Message) p).setFlag(Flags.Flag.DELETED, true);
			// may throw MessageingException, stop MailReader to
			// prevent from producing duplicate messages
		}

		long time_spent = System.currentTimeMillis()- start_tms;
		logger.info("Msg from " + msgBean.getFromAsString() + " processed, milliseconds: " + time_spent);
		
		return msgBean;
	}

	public JmsProcessor getJmsProcessor() {
		return jmsProcessor;
	}

	public MailBoxVo getMailboxProperties() {
		return mailBoxVo;
	}
}
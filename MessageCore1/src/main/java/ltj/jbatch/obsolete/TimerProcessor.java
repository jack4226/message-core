package ltj.jbatch.obsolete;

import javax.jms.JMSException;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.jbatch.app.RunnableProcessor;
import ltj.message.bo.mailreader.DuplicateCheckDao;
import ltj.message.bo.mailreader.DuplicateCheckJdbcDao;
import ltj.spring.util.SpringUtil;

/**
 * test processor
 */
public class TimerProcessor extends RunnableProcessor {
	static final Logger logger = LogManager.getLogger(TimerProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private DuplicateCheckDao duplicateCheck = null;

	// must use constructor without any parameters
	public TimerProcessor() {
		logger.info("Entering Constructor...");
	}

	public void process(Object req) throws JMSException, MessagingException {
		logger.info("Entering process() method...");
		if (req == null) {
			logger.warn("request object is null.");
			return;
		}
		if (req instanceof java.util.TimerTask) {
			logger.info("process(): A Timertask received.");
			// purge aged records from MSGIDDUP table
			try {
				getDuplicateCheck().process(req);
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				throw new MessagingException(e.toString());
			}
		}
		else {
			logger.info("process(): received a " + req.getClass().getName());
		}
	}

	private DuplicateCheckDao getDuplicateCheck() {
		if (duplicateCheck == null) {
			duplicateCheck = (DuplicateCheckDao) SpringUtil.getDaoAppContext().getBean("duplicateCheck");
			((DuplicateCheckJdbcDao)duplicateCheck).setPurgeAfter("24");
		}
		return duplicateCheck;
	}
}
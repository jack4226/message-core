package ltj.message.bo.mailreader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;

import org.apache.log4j.Logger;

import ltj.jbatch.app.RunnableProcessor;
import ltj.jbatch.app.SpringUtil;
import ltj.jbatch.queue.JmsProcessor;
import ltj.message.constant.MailProtocol;
import ltj.message.constant.MailServerType;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.MailBoxVo;

/**
 * <pre>
 * Monitors the given mailbox for new e-mails
 * - initialize required objects.
 * - open a mail session, initialize store and folder objects.
 * - for each email in the opened folder
 * 	. parse the mail header and body into custom components
 * 	. create a MessageBean with these components
 * 	. extract attachments if there are any, and save to the MessageBean
 * 	. write the MessageBean to a output queue
 * 	. set DELETE flag for the message and issue folder.close()
 * </pre>
 */
public class MailReaderBoImpl extends RunnableProcessor implements Serializable, ConnectionListener, StoreListener {
	private static final long serialVersionUID = -9061869821061961065L;
	private static final Logger logger = Logger.getLogger(MailReaderBoImpl.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	protected final MailBoxVo mailBoxVo;
	protected final String LF = System.getProperty("line.separator", "\n");
	protected final String processorName;

	final Session session;
	
	private Store store = null;
	private final boolean debugSession = false;

	protected int MAX_CLIENTS = 0;
	protected int MESSAGE_COUNT = 0; // when > 0 -> total messages to read in this run
	private final int MAX_MESSAGE_COUNT = 6000;
	private final int MAX_READ_PER_PASS = 500;
	private int RETRY_MAX = 10; // , default to 10, -1 -> infinite retry
	private final int readPerPass;
	private int freq;
	private int messagesProcessed = 0;

	private Folder folder = null;
	private final int[] retry_freq = { 5, 10, 10, 20, 20, 20, 30, 30, 30, 30, 60, 60, 60, 60, 60 };
		// in seconds
	private final int RETRY_FREQ = 120; // in seconds
	private final int MAX_NUM_THREADS = 20; // limit to 20 threads
	private int sleepFor = 0;

	/**
	 * create a MailReaderBoImpl instance
	 * 
	 * @param mailBoxVo -
	 *            mailbox properties
	 * @param factory -
	 *            spring framework application factory
	 * @param ruleEngineFactory -
	 *            spring framework application factory for ruleEngineBean
	 * @throws MessagingException
	 */
	public MailReaderBoImpl(MailBoxVo vo) {
		this.mailBoxVo = vo;

		logger.info("in Constructor - MailBox Properties:" + LF + mailBoxVo);
		processorName = mailBoxVo.getProcessorName(); // MailProcessor
		if (processorName == null) {
			throw new IllegalArgumentException("processor name must be defined in mailbox Properties");
		}
		MESSAGE_COUNT = mailBoxVo.getMessageCount();

		MAX_CLIENTS = mailBoxVo.getThreads();
		MAX_CLIENTS = MAX_CLIENTS > MAX_NUM_THREADS ? MAX_NUM_THREADS : MAX_CLIENTS;
		MAX_CLIENTS = MAX_CLIENTS <= 0 ? 1 : MAX_CLIENTS; // sanity check
		
		int MIN_WAIT = 2 * 1000; // default = 2 seconds
		int MAX_WAIT = 120 * 1000; // up to two minutes

		Integer temp;
		if ((temp = mailBoxVo.getMinimumWait()) != null) {
			MIN_WAIT = Math.abs(temp.intValue() * 1000);
		}
		if ((temp = mailBoxVo.getRetryMax()) != null) {
			RETRY_MAX = temp.intValue();
		}
		logger.info("Minimum wait in seconds = " + MIN_WAIT / 1000);
		logger.info("Maximum number of retries = " + RETRY_MAX);

		// number of e-mails (="readPerPass") to process per read
		int read_per_pass = mailBoxVo.getReadPerPass();
		read_per_pass = read_per_pass <= 0 ? 5 : read_per_pass; // default to 5
		readPerPass = read_per_pass; // final - accessible by inner class

		// issue a read to the mailbox in every "freq" MILLIseconds
		freq = MIN_WAIT + readPerPass * 100;
		freq = freq > MAX_WAIT ? MAX_WAIT : freq;
			// wait for up to MAX_WAIT seconds between reads.
		logger.info("Wait between reads in milliseconds: " + freq);
		
		// enable RFC2231 support in parameter lists, since javamail 1.4
		// Since very few existing programs support RFC2231, disable it for now
		/*
		System.setProperty("mail.mime.encodeparameters", "true");
		System.setProperty("mail.mime.decodeparameters", "true");
		System.setProperty("mail.mime.encodefilename", "true");
		System.setProperty("mail.mime.decodefilename", "true");
		*/
		
		// to make the reader more tolerable
		System.setProperty("mail.mime.multipart.ignoremissingendboundary", "true");
		System.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true");
		
		Properties m_props = (Properties) System.getProperties().clone();
		m_props.setProperty("mail.debug", "true");
		m_props.setProperty("mail.debug.quote", "true");

		/*
		 * POP3 - properties of com.sun.mail.pop3 
		 * mailbox can be accessed via URL: pop3://user:password@host:port/INBOX
		 */
		// set timeouts in milliseconds. default for both is infinite
		// Socket connection timeout
		m_props.setProperty("mail.pop3.connectiontimeout", "900000");
		// Socket I/O timeout
		m_props.setProperty("mail.pop3.timeout", "750000");
		// m_props.setProperty("mail.pop3.rsetbeforequit","true");
		/* issue RSET before QUIT, default: false */

		/* IMAP - properties of com.sun.mail.imap */
		// set timeouts in milliseconds. default for both is infinite
		// Socket connection timeout
		m_props.setProperty("mail.imap.connectiontimeout", "900000");
		// Socket I/O timeout
		m_props.setProperty("mail.imap.timeout", "750000");
		
		// Certain IMAP servers do not implement the IMAP Partial FETCH
		// functionality properly
		// set Partial fetch to false to workaround exchange server 5.5 bug
		m_props.setProperty("mail.imap.partialfetch","false");
		
		// If your version of Exchange doesn't implement POP3 properly, you need to tell JavaMail 
		// to forget about TOP headers by setting the mail.pop3.forgettopheaders property to true.
		if (MailServerType.EXCH.equalsIgnoreCase(mailBoxVo.getServerType())) {
			m_props.setProperty("mail.pop3.forgettopheaders","true");
		}
		
		// Get a Session object
		if ("yes".equalsIgnoreCase(mailBoxVo.getUseSsl())) {
			m_props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			m_props.setProperty("mail.pop3.socketFactory.fallback", "false");
			m_props.setProperty("mail.pop3.port", mailBoxVo.getPortNumber()+"");
			m_props.setProperty("mail.pop3.socketFactory.port", mailBoxVo.getPortNumber()+"");
			session = Session.getInstance(m_props);
		}
		else {
			session = Session.getInstance(m_props, null);
		}
	}

	/**
	 * run the MailReader, invoke Application plug-in to process e-mails.
	 */
	public void run() {
		logger.info("thread " + Thread.currentThread().getName() + " running");
		try {
			readMail(mailBoxVo.isFromTimer());
		}
		catch (MessagingException e) {
			logger.fatal("MessagingException caught, exiting...", e);
			throw new RuntimeException(e.getMessage());
		}
		catch (DataValidationException e) {
			logger.fatal("DataValidationException caught, exiting...", e);
			throw new RuntimeException(e.getMessage());
		}
		catch (JMSException e) {
			logger.fatal("JMSException caught, exiting...", e);
			throw new RuntimeException(e.getMessage());
		}
		finally {
			try {
				if (folder != null && folder.isOpen()) {
					folder.close(false);
				}
				store.close();
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}
		}
		logger.info("MailReader thread " + Thread.currentThread().getName() + " ended");
	}
	
	public void process(Object req) throws JMSException, MessagingException {
		// dummy method to satisfy super class
	}
	
	/**
	 * run the MailReaderBoImpl, invoke Application plug-in to process e-mails.
	 * 
	 * @param fromTimer -
	 *            true if called from EJBTimer
	 * @throws MessagingException
	 * @throws JMSException
	 * @throws ExecutionException
	 * @throws DataValidationException
	 */
	public void readMail(boolean fromTimer)
			throws MessagingException, JMSException, DataValidationException {
		session.setDebug(true); // DON'T CHANGE THIS
		if (fromTimer) {
			MESSAGE_COUNT = 500; // not to starve other mailbox readers
			messagesProcessed = 0; // reset this count
		}
		logger.info("MESSAGE_COUNT has been set to " + MESSAGE_COUNT);
		String protocol = mailBoxVo.getProtocol();
		if (!MailProtocol.IMAP.equalsIgnoreCase(protocol) && !MailProtocol.POP3.equalsIgnoreCase(protocol)) {
			throw new DataValidationException("Invalid protocol " + protocol);
		}
		if (store == null) {
			try {
				// Get a Store object
				store = session.getStore(protocol);
				store.addConnectionListener(this);
				store.addStoreListener(this);
			}
			catch (NoSuchProviderException pe) {
				logger.fatal("NoSuchProviderException caught during session.getStore()", pe);
				throw pe;
			}
		}
		try {
			connect(store, 0, RETRY_MAX); // could fail due to authentication error
			folder = getFolder(store, 0, 1); // retry once on folder
			// reset debug mode
			session.setDebug(debugSession);
			// only IMAP support MessageCountListener
			if (MailProtocol.IMAP.equalsIgnoreCase(protocol)) {
				final String _folder = mailBoxVo.getFolderName();
				// Add messageCountListener to listen to new messages from IMAP
				// server
				addMsgCountListener(folder, _folder, fromTimer);
			}
			if (MailProtocol.POP3.equalsIgnoreCase(protocol)) {
				pop3(fromTimer);
			}
			else { // IMAP protocol
				imap(fromTimer);
			}
		}
		finally {
			try {
				if (folder != null && folder.isOpen()) {
					folder.close(false);
				}
				store.close();
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}
		}
		if (isDebugEnabled) {
			logger.debug("MailReaderBoImpl ended");
		}
	} // end of run()

	private void pop3(boolean fromTimer) throws MessagingException, JMSException {
		final String _user = mailBoxVo.getUserId();
		final String _host = mailBoxVo.getHostName();
		final String _folder = mailBoxVo.getFolderName();
		boolean keepRunning = true;
		int retries = 0;
		do {
			try {
				if (folder.isOpen()) {
					folder.close(false);
				}
			}
			catch (MessagingException em) {
				logger.error("MessagingException caught during folder.close()", em);
			}
			try {
				Thread.sleep(waitTime(freq)); // exit if interrupted
				// reopen the folder in order to pick up the new messages
				folder.open(Folder.READ_WRITE);
			}
			catch (InterruptedException e) {
				logger.warn("pop3 thread interrupted, exiting...");
				keepRunning = false;
				break;
			}
			catch (MessagingException e) {
				logger.error("Failed to open folder " + _user + "@" + _host + ":" + _folder);
				logger.error("MessagingException caught", e);
				if (retries++ < RETRY_MAX || RETRY_MAX < 0) {
					// wait for a while and try to reopen the folder
					if (retries < retry_freq.length) {
						sleepFor = retry_freq[retries];
					}
					else {
						sleepFor = RETRY_FREQ;
					}
					logger.error("Failed to open folder, retry(=" + retries + ") in " + sleepFor + " seconds");
					try {
						Thread.sleep(sleepFor * 1000);
					} catch (InterruptedException e1) {
						keepRunning = false; // terminate if interrupted
						break;
					}
					continue;
				}
				else {
					logger.fatal("All retries failed for " + _user + "@" + _host + ":" + _folder);
					throw e;
				}
			}
			if (retries > 0) {
				logger.error("Opened " + _user + "@" + _host + ":" + _folder + " after " + retries + " retries");
				retries = 0; // reset retry counter
			}
			long start_tms = System.currentTimeMillis();
			int msgCount = 0;
			if ((msgCount = folder.getMessageCount()) > 0) {
				logger.info(mailBoxVo.getUserId() + "'s " + _folder + " has " + msgCount + " messages.");
				// "readPerPass" is used so the flagged messages will be
				// purged more often
				int msgToProc = Math.min(msgCount, readPerPass);
				// if we can't keep up, process more messages in each cycle
				if (msgCount > msgToProc * 50) {
					msgToProc *= 50;
				}
				else if (msgCount > msgToProc * 10) {
					msgToProc *= 10;
				}
				else if (msgCount > msgToProc * 5) {
					msgToProc *= 5;
				}
				msgToProc = msgToProc > MAX_READ_PER_PASS ? MAX_READ_PER_PASS : msgToProc;
				logger.info("number of messages to be processed in this cycle: " + msgToProc);
				if (MESSAGE_COUNT > 0 && msgCount > MESSAGE_COUNT * 2) {
					// bump up MESSAGE_COUNT to process more in this cycle
					MESSAGE_COUNT *= (int) Math.floor(msgCount / MESSAGE_COUNT);
					MESSAGE_COUNT = MESSAGE_COUNT > MAX_MESSAGE_COUNT ? MAX_MESSAGE_COUNT
							: MESSAGE_COUNT;
					logger.info("MESSAGE_COUNT has been bumped up to: " + MESSAGE_COUNT);
				}
				Message[] msgs = null;
				try {
					msgs = folder.getMessages(1, msgToProc);
				}
				catch (IndexOutOfBoundsException ie) {
					logger.error("IndexOutOfBoundsException caught, retry with getMessages()", ie);
					msgs = folder.getMessages();
					logger.info("Retry with folder.getMessages() is successful.");
				}
				execute(msgs); // process the messages
				folder.close(true); // "true" to delete the flagged messages
				logger.info(msgs.length + " messages have been purged from pop3 mailbox.");
				messagesProcessed += msgs.length;
				long proc_time = System.currentTimeMillis() - start_tms;
				if (isDebugEnabled) {
					logger.debug(msgs.length+ " messages processed, time taken: " + proc_time);
				}
				if (MESSAGE_COUNT > 0 && messagesProcessed > MESSAGE_COUNT) {
					keepRunning = false;
				}
			}
			else if (fromTimer) { // no messages in INBOX
				keepRunning = false;
			}
		} while (keepRunning); // end of do-while
	}
	
	private void imap(boolean fromTimer) throws MessagingException, JMSException {
		boolean keepRunning = true;
		folder.open(Folder.READ_WRITE);
		/*
		 * fix for iPlanet: iPlanet wouldn't pick up the existing
		 * messages, the MessageCountListener may not be implemented
		 * correctly for iPlanet.
		 */
		if (folder.getMessageCount() > 0) {
			logger.info(mailBoxVo.getUserId() + "'s " + mailBoxVo.getFolderName() + " has " + folder.getMessageCount()
					+ " messages.");
			long start_tms = System.currentTimeMillis();
			Message msgs[] = folder.getMessages();
			execute(msgs);
			folder.expunge(); // remove messages marked as DELETED
			logger.info(msgs.length + " messages have been expunged from imap mailbox.");
			long proc_time = System.currentTimeMillis() - start_tms;
			if (isDebugEnabled) {
				logger.debug(msgs.length+ " messages processed, time took: " + proc_time);
			}
		}
		/* end of fix for iPlanet */
		while (keepRunning) {
			try {
				Thread.sleep(waitTime(freq)); // sleep for "freq" milliseconds
			}
			catch (InterruptedException e) {
				logger.warn("imap thread interrupted, exiting...");
				keepRunning = false;
				continue;
			}
			// This is to force the IMAP server to send us
			// EXISTS notifications.
			int msgCount = folder.getMessageCount();
			if (msgCount == 0 && fromTimer) {
				keepRunning = false;
			}
		}
	}
	
	/**
	 * Add messageCountListener to listen to new messages for IMAP.
	 * 
	 * @param folder -
	 *            a Folder object
	 * @param _folder -
	 *            folder name
	 */
	private void addMsgCountListener(final Folder folder, final String _folder, final boolean fromTimer) {
		folder.addMessageCountListener(new MessageCountAdapter() {
			private final Logger logger = Logger.getLogger(MessageCountAdapter.class);
			public void messagesAdded(MessageCountEvent ev) {
				Message[] msgs = ev.getMessages();
				logger.info("Got " + msgs.length + " new messages from " + _folder);
				long start_tms = System.currentTimeMillis();
				try {
					execute(msgs);
					// remove messages marked DELETED
					folder.expunge();
					logger.info(msgs.length + " messages have been expunged from imap mailbox.");
					messagesProcessed += msgs.length;
				}
				catch (MessagingException ex) {
					logger.fatal("MessagingException caught", ex);
					throw new RuntimeException(ex.getMessage());
				}
				catch (JMSException ex) {
					logger.fatal("JMSException caught", ex);
					throw new RuntimeException(ex.getMessage());
				}
				finally {
					long proc_time = System.currentTimeMillis() - start_tms;
					if (isDebugEnabled) {
						logger.debug(msgs.length+ " messages processed, time took: " + proc_time);
					}
					//update(MetricsLogger.PROC_TIME, (int) proc_time, msgs.length);
				}
				if (MESSAGE_COUNT > 0 && messagesProcessed > MESSAGE_COUNT) {
					Thread.currentThread().interrupt();
				}
			}
		}); // end of IMAP folder.addMessageCountListener
	}
	
	private long waitTime(int freq) {
		return freq;
	}
	
	/**
	 * process e-mails using MailProcessor, and the results will be sent to
	 * ruleEngineInput queue by the MailProcessor.
	 * 
	 * @param msgs -
	 *            messages to be processed.
	 * @throws InterruptedException
	 * @throws MessagingException
	 * @throws JMSException
	 */
	private void execute(Message[] msgs) throws JMSException, MessagingException {
		if (msgs == null || msgs.length == 0) {
			return;
		}
		try {
			SpringUtil.beginTransaction();
			
			JmsProcessor jmsProcessor = SpringUtil.getAppContext().getBean(JmsProcessor.class);
			jmsProcessor.setQueueName("mailReaderOutput"); // TODO get from properties
			DuplicateCheckDao duplicateCheck = SpringUtil.getAppContext().getBean(DuplicateCheckDao.class);
			MailProcessor processor = new MailProcessor(jmsProcessor, mailBoxVo, duplicateCheck);
			processor.process(msgs);
			
			SpringUtil.commitTransaction();
		}
		catch (JMSException | MessagingException e) {
			SpringUtil.rollbackTransaction();
			throw e;
		}
	}
	
	/**
	 * implement ConnectionListener interface
	 * 
	 * @param e -
	 *            Connection event
	 */
	public void opened(ConnectionEvent e) {
		if (isDebugEnabled)
			logger.debug(">>> ConnectionListener: connection opened()");
	}

	/**
	 * implement ConnectionListener interface
	 * 
	 * @param e -
	 *            Connection event
	 */
	public void disconnected(ConnectionEvent e) {
		logger.info(">>> ConnectionListener: connection disconnected()");
	}

	/**
	 * implement ConnectionListener interface
	 * 
	 * @param e -
	 *            Connection event
	 */
	public void closed(ConnectionEvent e) {
		if (isDebugEnabled)
			logger.debug(">>> ConnectionListener: connection closed()");
	}

	public void notification(StoreEvent e) {
		if (isDebugEnabled)
			logger.debug(">>> StoreListener: notification event: " + e.getMessage());
	}
	
	/* end of the implementation */

	/**
	 * connect to Store, with retry logic
	 * 
	 * @param store
	 *            Store object
	 * @param retries
	 *            number of retries performed
	 * @param RETRY_MAX
	 *            number of retries to be performed before throwing Exception
	 * @throws MessagingException 
	 *             when retries reached the RETRY_MAX
	 */
	void connect(Store store, int retries, int RETRY_MAX) throws MessagingException {
		int portnbr = mailBoxVo.getPortNumber();
		// -1 to use the default port
		if (isDebugEnabled) {
			logger.debug("Port used: " + portnbr);
		}
		if (retries > 0) { // retrying, close store first
			try {
				store.close();
			}
			catch (MessagingException e) {
				logger.error("Exception caught during store.close on retry", e);
			}
		}
		try {
			// connect
			store.connect(mailBoxVo.getHostName(), portnbr, mailBoxVo.getUserId(), mailBoxVo.getUserPswd());
		}
		catch (MessagingException me) {
			if (retries < RETRY_MAX || RETRY_MAX < 0) {
				if (retries < retry_freq.length) {
					sleepFor = retry_freq[retries];
				}
				else {
					sleepFor = RETRY_FREQ;
				}
				logger.error("Failed to connect to store, retry(=" + retries + ") in " + sleepFor + " seconds");
				try {
					Thread.sleep(sleepFor * 1000);
				}
				catch (InterruptedException e) {
					logger.warn("InterruptedException caught", e);
				}
				connect(store, ++retries, RETRY_MAX);
			}
			else {
				logger.fatal("Exception caught during store.connect, all retries failed...");
				throw me;
			}
		}
	}

	/**
	 * retrieve Folder with retry logic
	 * 
	 * @param store
	 *            Store object
	 * @param retries
	 *            number of retries performed
	 * @param RETRY_MAX
	 *            number of retries to be performed before throwing Exception
	 * @return Folder instance
	 * @throws MessagingException 
	 *             when retries reached RETRY_MAX
	 */
	Folder getFolder(Store store, int retries, int RETRY_MAX) throws MessagingException {
		try {
			// Open a Folder
			//folder = store.getDefaultFolder();
			folder = store.getFolder(mailBoxVo.getFolderName());

			if (folder == null || !folder.exists()) {
				throw new MessagingException("Invalid folder " + mailBoxVo.getFolderName());
			}
		}
		catch (MessagingException me) {
			if (retries < RETRY_MAX || RETRY_MAX < 0) {
				if (retries < retry_freq.length) {
					sleepFor = retry_freq[retries];
				}
				else {
					sleepFor = RETRY_FREQ;
				}
				logger.error("Failed to get folder, retry(=" + retries + ") in " + sleepFor + " seconds");
				try {
					Thread.sleep(sleepFor * 1000);
				}
				catch (InterruptedException e) {
					logger.warn("InterruptedException caught", e);
				}
				return getFolder(store, ++retries, RETRY_MAX);
			}
			else {
				logger.fatal("Exception caught during store.getFolder, all retries failed");
				throw me;
			}
		}
		return folder;
	}
	
	/**
	 * return MailReader metrics, used by MBean
	 * 
	 * @return a List containing metrics data
	 */
	public List<String> getStatus() {
		List<String> v = new ArrayList<String>();
		if (mailBoxVo != null) {
			v.add("MailReaderBoImpl: user=" + mailBoxVo.getUserId() + ", host="
					+ mailBoxVo.getHostName() + ", #Threads=" + MAX_CLIENTS);
			v.add(mailBoxVo.toString());
		}
		return v;
	}

	public MailBoxVo getMailBoxVo() {
		return mailBoxVo;
	}
}
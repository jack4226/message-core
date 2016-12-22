package ltj.jbatch.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ltj.message.bo.mailreader.MailReaderBoImpl;
import ltj.message.dao.mailbox.MailBoxDao;
import ltj.message.vo.MailBoxVo;
import ltj.spring.util.SpringUtil;

@Component
public class MailReaderTaskExr {
	static final Logger logger = Logger.getLogger(MailReaderTaskExr.class);
	
	@Autowired
	private TaskExecutor taskExecutor;
	@Autowired
	private MailBoxDao mailBoxDao;
	
	boolean runWithFixedThreadPool = true;
	
	private final List<Future<?>> futureList = new ArrayList<>();
	private ExecutorService executor;
	
	// for test only, set to true to read from test user accounts
	public static boolean readTestUserAccounts = false;
	public static int testStartUser = 0;
	public static int testEndUser = 25;
	// end of test variables
	
	@Scheduled(initialDelay=20000, fixedDelay=30000) // delay for 20 seconds for testing
	public void startMailReaders() {
		logger.info("startMailReaders() - entering...");
		
		if (readTestUserAccounts) { // for test only
			readTestUserAccounts(testStartUser, testEndUser);
			return;
		}
		
		List<MailBoxVo> mailBoxList = mailBoxDao.getAll(true);
		logger.info("Number of mailbox to start: " + mailBoxList.size());
		//ExecutorService executor = null;
		try {
			if (runWithFixedThreadPool) {
				executor = Executors.newFixedThreadPool(mailBoxList.size());
			}
			else {
				executor = new ThreadPoolExecutor(5, mailBoxList.size(), 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(15));
			}
			//List<Future<?>> futureList = new ArrayList<>();
			for (MailBoxVo vo : mailBoxList) {
				vo.setFromTimer(true);
				MailReaderBoImpl reader = new MailReaderBoImpl(vo);
				try {
					Thread.sleep(new Random().nextInt(1000));
				} catch (InterruptedException e) {}
				Future<?> future = executor.submit(reader);
				futureList.add(future);
			}
			for (Future<?> future : futureList) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Exception caught during future.get()", e);
				}
			}
		}
		finally {
			if (executor != null) {
				executor.shutdown();
			}
		}
	}
	
	@PreDestroy
	public void cancelTasks() {
		logger.warn("Entering @PreDestroy cancelTasks() method...");
		for (Future<?> future : futureList) {
			if (!future.isDone() || !future.isCancelled()) {
				future.cancel(true);
			}
		}
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
		}
	}
	
	private void readTestUserAccounts(int startIdx, int endIdx) {
		ExecutorService executor = new ThreadPoolExecutor(5, 25, 2000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(75));
		List<Future<?>> futureList = new ArrayList<>();
		try {
			for (int i = startIdx; i < endIdx; i++) {
				String suffix = StringUtils.leftPad((i % 100) + "", 2, "0");
				String user = "user" + suffix;
				MailBoxVo vo = new MailBoxVo();
				vo.setUserId(user);
				vo.setUserPswd(user);
				vo.setHostName("localhost");
				vo.setProtocol("pop3");
				vo.setReadPerPass(10);
				vo.setUseSsl("no");
				vo.setFromTimer(true);
				MailReaderBoImpl reader = new MailReaderBoImpl(vo);
				try {
					Thread.sleep(new Random().nextInt(1000));
				} catch (InterruptedException e) {}
				Future<?> future = executor.submit(reader);
				futureList.add(future);
			}
			for (Future<?> future : futureList) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Exception caught during future.get()", e);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			executor.shutdown();
		}
	}
	
	
	void testRunWithSpringTaskExecutor() {
		boolean readUserAccts = true;
		boolean readConfAccts = false;
		// read from user accounts
		if (readUserAccts) {
			readTestUserAccounts(0, 100);
		}
		// read from configured accounts
		if (readConfAccts) {
			List<MailBoxVo> mailBoxList = mailBoxDao.getAll(true);
			for (MailBoxVo vo : mailBoxList) {
				vo.setFromTimer(true);
				MailReaderBoImpl reader = new MailReaderBoImpl(vo);
				taskExecutor.execute(reader);
			}
		}
	}
	
	void testReadFromOneMailbox() {
		MailBoxVo vo = mailBoxDao.getByPrimaryKey("jwang", "localhost");
		if (vo == null) return;
		vo.setFromTimer(true);
		MailReaderBoImpl reader = new MailReaderBoImpl(vo);
		try {
				//reader.start();
				//reader.join();
				reader.readMail(vo.isFromTimer());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static boolean testSpringTaskExecutor = true;
	static boolean testReadFromOneMailbox = false;
	
	public static void main(String[] args) {
		AbstractApplicationContext factory = SpringUtil.getAppContext();
		
		if (testSpringTaskExecutor) {
			MailReaderTaskExr taskExr = factory.getBean(MailReaderTaskExr.class);
			taskExr.testRunWithSpringTaskExecutor();
		}
		
		if (testReadFromOneMailbox) {
			MailReaderTaskExr taskExr = factory.getBean(MailReaderTaskExr.class);
			taskExr.testReadFromOneMailbox();
		}
		
		try {
			Thread.sleep(40000L);
		} catch (InterruptedException e) {
			// ignore
		}
		System.exit(0);
	}

}

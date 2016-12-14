package ltj.message.bo.mailreader;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ltj.jbatch.app.SpringUtil;
import ltj.message.dao.mailbox.MailBoxDao;
import ltj.message.vo.MailBoxVo;

@Component
public class MailReaderTaskExr {
	static final Logger logger = Logger.getLogger(MailReaderTaskExr.class);
	
	@Autowired
	private TaskExecutor taskExecutor;
	@Autowired
	private MailBoxDao mailBoxDao;
	
	boolean runWithFixedThreadPool = false;
	
	@Scheduled(initialDelay=1000, fixedDelay=5000)
	public void startMailReaders() {
		logger.info("startMailReaders() - entering...");
		List<MailBoxVo> mailBoxList = mailBoxDao.getAll(true);
		ExecutorService executor = null; 
		if (runWithFixedThreadPool) {
			Executors.newFixedThreadPool(mailBoxList.size());
		}
		else {
			executor = new ThreadPoolExecutor(5, mailBoxList.size(), 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(15));
		}
		for (MailBoxVo vo : mailBoxList) {
			vo.setFromTimer(true);
			MailReaderBoImpl reader = new MailReaderBoImpl(vo);
			Future<?> future = executor.submit(reader);
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Exception caught during future.get()", e);
			}
		}
		executor.shutdown();
	}
	
	
	void testRunWithSpringTaskExecutor() {
		List<MailBoxVo> mailBoxList = mailBoxDao.getAll(true);
		for (MailBoxVo vo : mailBoxList) {
			vo.setFromTimer(true);
			MailReaderBoImpl reader = new MailReaderBoImpl(vo);
			taskExecutor.execute(reader);
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
	
	static boolean testSpringTaskExecutor = false;
	static boolean testReadFromOneMailbox = false;
	
	public static void main(String[] args) {
		AbstractApplicationContext factory = SpringUtil.getAppContext();
		
		if (testSpringTaskExecutor) {
			MailReaderTaskExr taskExr = factory.getBean(MailReaderTaskExr.class);
			taskExr.startMailReaders();
		}
		
		if (testReadFromOneMailbox) {
			MailReaderTaskExr taskExr = factory.getBean(MailReaderTaskExr.class);
			taskExr.testReadFromOneMailbox();
		}
		
		try {
			Thread.sleep(60000L);
		} catch (InterruptedException e) {
			// ignore
		}
		System.exit(0);
	}

}

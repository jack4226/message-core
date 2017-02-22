package ltj.spring.util;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class SpringUtil {
	static final Logger logger = Logger.getLogger(SpringUtil.class);

	private static AnnotationConfigApplicationContext appConfCtx = null;
	
	private static AbstractApplicationContext daoConfCtx = null;
	
	private static AnnotationConfigApplicationContext taskConfCtx = null;
	
	private SpringUtil() {}
	
	public static AbstractApplicationContext getAppContext() {
		if (appConfCtx == null) {
			logger.info("getAppContext() - load application and datasource config");
			appConfCtx = new AnnotationConfigApplicationContext();
			appConfCtx.register(SpringAppConfig.class, SpringJmsConfig.class);
			appConfCtx.refresh();
			appConfCtx.registerShutdownHook();
		}
		return appConfCtx;
	}
	
	public static AbstractApplicationContext getDaoAppContext() {
		if (appConfCtx != null) {
			return appConfCtx;
		}
		else if (daoConfCtx == null) {
			logger.info("getDaoAppContext() - load datasource config");
			daoConfCtx = new AnnotationConfigApplicationContext(SpringAppConfig.class);
			daoConfCtx.registerShutdownHook();
		}
		return daoConfCtx;
	}
	
	public static AbstractApplicationContext getTaskAppContext() {
		if (taskConfCtx == null) {
			logger.info("getTaskAppContext() - load application, datasource, and task config");
			taskConfCtx = new AnnotationConfigApplicationContext();
			taskConfCtx.register(SpringAppConfig.class, SpringJmsConfig.class, SpringTaskConfig.class);
			taskConfCtx.refresh();
			taskConfCtx.registerShutdownHook();
		}
		return taskConfCtx;
	}
	
	public static void shutDownConfigContexts() {
		if (appConfCtx != null) {
			appConfCtx.stop();
			appConfCtx.close();
		}
		if (daoConfCtx != null) {
			daoConfCtx.stop();
			daoConfCtx.close();
		}
		if (taskConfCtx != null) {
			taskConfCtx.stop();
			taskConfCtx.close();
		}
	}
	
	private static final ThreadLocal<Stack<PlatformTransactionManager>> txmgrThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<Stack<TransactionStatus>> statusThreadLocal = new ThreadLocal<>();
	
	public static void beginTransaction() {
		int level = txmgrThreadLocal.get() == null ? 0 : txmgrThreadLocal.get().size();
		logger.info("In beginTransaction()... level = " + level);
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("service_"+ TX_COUNTER.get().incrementAndGet());
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		if (txmgrThreadLocal.get() == null) {
			txmgrThreadLocal.set(new Stack<PlatformTransactionManager>());
		}
		if (statusThreadLocal.get() == null) {
			statusThreadLocal.set(new Stack<TransactionStatus>());
		}
		txmgrThreadLocal.get().push(txmgr);
		statusThreadLocal.get().push(status);
	}

	public static void commitTransaction() {
		int level = txmgrThreadLocal.get() == null ? 0 : txmgrThreadLocal.get().size() - 1;
		logger.info("In commitTransaction()... level = " + level);
		if (txmgrThreadLocal.get() == null || txmgrThreadLocal.get().isEmpty()) {
			throw new IllegalStateException("No transaction manager is in progress.");
		}
		if (statusThreadLocal.get() == null || statusThreadLocal.get().isEmpty()) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		PlatformTransactionManager txmgr = txmgrThreadLocal.get().pop();
		TransactionStatus status = statusThreadLocal.get().pop();
		try {
			if (!status.isCompleted()) {
				txmgr.commit(status);
			}
		}
		finally {
		}
	}

	public static void rollbackTransaction() {
		if (txmgrThreadLocal.get() == null || txmgrThreadLocal.get().isEmpty()) {
			throw new IllegalStateException("No transaction manager is in progress.");
		}
		if (statusThreadLocal.get() == null || statusThreadLocal.get().isEmpty()) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		PlatformTransactionManager txmgr = txmgrThreadLocal.get().pop();
		TransactionStatus status = statusThreadLocal.get().pop();
		try {
			if (!status.isCompleted()) {
				txmgr.rollback(status);
			}
		}
		finally {
		}
	}

	public static void clearTransaction() {
		if (statusThreadLocal.get() != null && !statusThreadLocal.get().isEmpty()) {
			TransactionStatus status = statusThreadLocal.get().pop();
			if (status != null) {
				status.setRollbackOnly();
			}
			statusThreadLocal.get().clear();
		}
		if (txmgrThreadLocal.get() != null) {
			txmgrThreadLocal.get().clear();
		}
		txmgrThreadLocal.remove();
		statusThreadLocal.remove();
	}
	
	public static boolean isInTransaction() {
		return TransactionSynchronizationManager.isActualTransactionActive();
	}

	private static final ThreadLocal<AtomicInteger> TX_COUNTER = new ThreadLocal<AtomicInteger>() {
		public AtomicInteger initialValue() {
			return new AtomicInteger(1);
		}
	};

	public static boolean isRunningInJunitTest() {
		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		for (int i = traces.length - 1; i > 0; i--) {
			StackTraceElement trace = traces[i];
			if (StringUtils.startsWith(trace.getClassName(), "org.junit.runners")) {
				// org.junit.runners.ParentRunner.run(ParentRunner.java:363)
				return true;
			}
			else if (StringUtils.contains(trace.getClassName(), "junit.runner")) {
				// org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:192)
				return true;
			}
		}
		return false;
	}

}

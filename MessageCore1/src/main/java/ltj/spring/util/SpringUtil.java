package ltj.spring.util;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;

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
	
	private static final ThreadLocal<Stack<TransTuple>> transThreadLocal = new ThreadLocal<>();
	
	public static void beginTransaction() {
		int level = transThreadLocal.get() == null ? 0 : transThreadLocal.get().size();
		logger.info("In beginTransaction()... level = " + level);
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("service_"+ TX_COUNTER.get().incrementAndGet());
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		TransTuple tuple = new TransTuple(txmgr, status);
		if (transThreadLocal.get() == null) {
			transThreadLocal.set(new Stack<TransTuple>());
		}
		transThreadLocal.get().push(tuple);
	}

	public static void commitTransaction() {
		int level = transThreadLocal.get() == null ? 0 : transThreadLocal.get().size() - 1;
		logger.info("In commitTransaction()... level = " + level);
		if (transThreadLocal.get() == null || transThreadLocal.get().isEmpty()) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		TransTuple tuple = transThreadLocal.get().pop();
		try {
			if (!tuple.status.isCompleted()) {
				tuple.txmgr.commit(tuple.status);
			}
		}
		finally {
		}
	}

	public static void rollbackTransaction() {
		if (transThreadLocal.get() == null || transThreadLocal.get().isEmpty()) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		TransTuple tuple = transThreadLocal.get().pop();
		try {
			if (!tuple.status.isCompleted()) {
				tuple.txmgr.rollback(tuple.status);
			}
		}
		finally {
		}
	}

	public static void clearTransaction() {
		if (transThreadLocal.get() != null && !transThreadLocal.get().isEmpty()) {
			TransTuple tuple = transThreadLocal.get().pop();
			if (tuple.status != null) {
				tuple.status.setRollbackOnly();
			}
			transThreadLocal.get().clear();
		}
		if (transThreadLocal.get() != null) {
			transThreadLocal.remove();
		}
	}
	
	public static boolean isInTransaction() {
		return TransactionSynchronizationManager.isActualTransactionActive();
	}

	private static class TransTuple {
		final PlatformTransactionManager txmgr;
		final TransactionStatus status;
		
		TransTuple(@NotNull PlatformTransactionManager txmgr, @NotNull TransactionStatus status) {
			this.txmgr = txmgr;
			this.status = status;
		}
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

package ltj.spring.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
	
	private static final ThreadLocal<PlatformTransactionManager> txmgrThreadLocal = new ThreadLocal<PlatformTransactionManager>();
	private static final ThreadLocal<TransactionStatus> statusThreadLocal = new ThreadLocal<TransactionStatus>();
	
	public static void beginTransaction() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("service_"+ TX_COUNTER.get().incrementAndGet());
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getDaoAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		txmgrThreadLocal.set(txmgr);
		statusThreadLocal.set(status);
	}

	public static void commitTransaction() {
		PlatformTransactionManager txmgr = txmgrThreadLocal.get();
		TransactionStatus status = statusThreadLocal.get();
		if (txmgr==null || status==null) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		if (!status.isCompleted()) {
			txmgr.commit(status);
		}
		txmgrThreadLocal.remove();
		statusThreadLocal.remove();
	}

	public static void rollbackTransaction() {
		PlatformTransactionManager txmgr = txmgrThreadLocal.get();
		TransactionStatus status = statusThreadLocal.get();
		if (txmgr==null || status==null) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		if (!status.isCompleted()) {
			txmgr.rollback(status);
		}
		txmgrThreadLocal.remove();
		statusThreadLocal.remove();
	}

	public static void clearTransaction() {
		PlatformTransactionManager txmgr = txmgrThreadLocal.get();
		TransactionStatus status = statusThreadLocal.get();
		if (txmgr!=null && status!=null) {
			rollbackTransaction();
		}
	}

	private static final ThreadLocal<AtomicInteger> TX_COUNTER = new ThreadLocal<AtomicInteger>() {
		public AtomicInteger initialValue() {
			return new AtomicInteger(1);
		}
	};

}

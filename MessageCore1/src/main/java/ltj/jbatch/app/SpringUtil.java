package ltj.jbatch.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ltj.message.util.ServiceLocator;
import ltj.spring.util.SpringAppConfig;
import ltj.spring.util.SpringJmsConfig;

public final class SpringUtil {
	static final Logger logger = Logger.getLogger(SpringUtil.class);
	
	private static AbstractApplicationContext applContext = null;

	private static AbstractApplicationContext daoAppCtx = null;
	
	private static AnnotationConfigApplicationContext configCtx = null;
	
	private SpringUtil() {}
	
	public static AbstractApplicationContext getAppContext() {
		if (configCtx == null) {
			logger.info("getAppContext() - load application and datasource config");
			configCtx = new AnnotationConfigApplicationContext();
			configCtx.register(SpringAppConfig.class, SpringJmsConfig.class);
			configCtx.refresh();
			configCtx.registerShutdownHook();
		}
		return configCtx;
	}
	
	public static AbstractApplicationContext getDaoAppContext() {
		if (configCtx != null) {
			return configCtx;
		}
		else if (daoAppCtx == null) {
			logger.info("getDaoAppContext() - load datasource config");
			daoAppCtx = new AnnotationConfigApplicationContext(SpringAppConfig.class);
			daoAppCtx.registerShutdownHook();
		}
		return daoAppCtx;
	}
	
	
	/**
	 * If it's running in JBoss server, it loads a set of xmls using JNDI's,
	 * otherwise loads a set of xmls using mysql data source.
	 * @return ApplicationContext
	 */
	public static AbstractApplicationContext getAppContext_v0() {
		if (applContext == null) {
			String[] fileNames = null;
			if (isRunningInJBoss()) {
				logger.info("getAppContext() - Running under JBoss, load server xmls");
				fileNames = getServerConfigXmlFiles();
			}
			else {
				if (JbMain.getNumberOfThreadAtStart() < 0) {
					logger.info("getAppContext() - running standalone, load java config");
					if (configCtx == null) {
						configCtx = new AnnotationConfigApplicationContext();
						configCtx.register(SpringAppConfig.class, SpringJmsConfig.class);
						configCtx.refresh();
					}
					return configCtx;
				}
				else {
					logger.info("getAppContext() - running batch standalone, load batch xmls");
					fileNames = getBatchConfigXmlFiles();
				}
			}
			applContext = new ClassPathXmlApplicationContext(fileNames);
		} 
		return applContext;
	}

	private static boolean ListJBossJndiNames = false;
	
	public static boolean isRunningInJBoss() {
		try {
			// see if it's running under JBoss by doing a JNDI lookup
			ServiceLocator.getDataSource("java:jboss/MessageDS");
			logger.info("isRunningInJBoss() - Running in JBoss.");
			if (ListJBossJndiNames) {
				ServiceLocator.listContext("java:comp");
				ServiceLocator.listContext("java:/queue");
				ServiceLocator.listContext("java:comp/env/jms");
				ServiceLocator.listContext("java:comp/env/jdbc");
				ServiceLocator.listContext("java:jboss");
			}
			return true;
		}
		catch (javax.naming.NamingException e) {
			logger.info("isRunningInJBoss() - Running standalone.");
		}
		return false;
	}

	/**
	 * This method is intended to be used by table creation classes.
	 * @return ApplicationContext
	 */
	public static AbstractApplicationContext getDaoAppContext_v0() {
		if (applContext != null) {
			return applContext;
		}
		else if (daoAppCtx == null) {
			if (isRunningInJBoss()) {
				logger.info("getDaoAppContext() - Running under JBoss, load jndi_ds xmls");
				List<String> fnames = new ArrayList<String>();
				fnames.add("classpath*:spring-common-config.xml");
				fnames.add("classpath*:spring-jmsqueue_jee-config.xml");
				daoAppCtx = new ClassPathXmlApplicationContext(fnames.toArray(new String[]{}));
			}
			else {
				logger.info("getDaoAppContext() - running standalone, load mysql_ds java config");
				daoAppCtx = new AnnotationConfigApplicationContext(SpringAppConfig.class);
			}
		}
		return daoAppCtx;
	}

	private static String[] getBatchConfigXmlFiles() {
		ClassLoader loader = JbMain.class.getClassLoader();
		List<String> cfgFileNames = new ArrayList<String>();
		cfgFileNames.add("classpath:spring-common-config.xml");
		cfgFileNames.add("classpath:spring-mysql-config.xml");
		cfgFileNames.add("classpath:spring-jmsqueue_rmt-config.xml");
		cfgFileNames.add("classpath:spring-jbatch-config.xml");
		URL mreader = loader.getResource("spring-jbatch-mailreader.xml");
		URL msender = loader.getResource("spring-jbatch-mailsender.xml");
		URL testsvrs = loader.getResource("spring-jbatch-testservers.xml");
		if (mreader != null) {
			cfgFileNames.add("classpath:spring-jbatch-mailreader.xml");
		}
		else if (msender != null) {
			cfgFileNames.add("classpath:spring-jbatch-mailsender.xml");
			URL smtp = loader.getResource("spring-jbatch-smtp.xml");
			if (smtp != null) {
				cfgFileNames.add("classpath:spring-jbatch-smtp.xml");
			}
		}
		else if (testsvrs != null) {
			cfgFileNames.add("classpath:spring-jbatch-testservers.xml");
		}
		String[] cfgFiles = cfgFileNames.toArray(new String[]{});
		return cfgFiles;
	}

	public static String[] getServerConfigXmlFiles() {
		List<String> cfgFileNames = new ArrayList<String>();
		cfgFileNames.add("classpath*:spring-common-config.xml");
		cfgFileNames.add("classpath*:spring-jmsqueue_jee-config.xml");
		return cfgFileNames.toArray(new String[]{});
	}

	public static String[] getStandaloneConfigXmlFiles() {
		List<String> cfgFileNames = new ArrayList<String>();
		cfgFileNames.add("classpath:spring-common-config.xml");
		cfgFileNames.add("classpath:spring-jmsqueue_rmt-config.xml");
		cfgFileNames.add("classpath:spring-mysql-config.xml");
		return cfgFileNames.toArray(new String[]{});
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

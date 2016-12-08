package ltj.message.dao.abstrct;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import ltj.jbatch.app.SpringUtil;
import ltj.spring.util.SpringAppConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringAppConfig.class})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public abstract class DaoTestBase {
	protected static final Logger logger = Logger.getLogger(DaoTestBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();
	protected final static String LF = System.getProperty("line.separator","\n");
	
	protected static AbstractApplicationContext factory;
	
	@BeforeClass
	public static void prepare() {
		factory = SpringUtil.getAppContext();
	}
}

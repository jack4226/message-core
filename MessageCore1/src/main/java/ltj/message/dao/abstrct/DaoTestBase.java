package ltj.message.dao.abstrct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ltj.spring.util.SpringAppConfig;
import ltj.spring.util.SpringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringAppConfig.class})
@Transactional
@Rollback(true)
public abstract class DaoTestBase {
	protected static final Logger logger = LogManager.getLogger(DaoTestBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();
	protected final static String LF = System.getProperty("line.separator","\n");
	
	protected static AbstractApplicationContext factory;
	
	@BeforeClass
	public static void prepare() {
		factory = SpringUtil.getAppContext();
	}
}

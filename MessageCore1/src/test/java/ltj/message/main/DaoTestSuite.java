package ltj.message.main;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ltj.message.dao.MsgActionTest.class,
	ltj.message.dao.MsgActionDetailTest.class
})
public class DaoTestSuite {
	//nothing
}

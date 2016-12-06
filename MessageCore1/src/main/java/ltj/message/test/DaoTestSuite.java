package ltj.message.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ltj.message.dao.action.MsgActionTest.class,
	ltj.message.dao.action.MsgActionDetailTest.class
})
public class DaoTestSuite {
	//nothing
}

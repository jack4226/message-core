package ltj.message.main;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ltj.message.bo.test.RuleEngineTest;

public class RunClassesTest {
	
	static boolean runRuleEngineTest = true;
	
	@Test
	public void test1() {
		if (runRuleEngineTest) {
			try {
				Result result = JUnitCore.runClasses(RuleEngineTest.class);
				for (Failure failure : result.getFailures()) {
					System.err.println(failure.toString());
				}
			}
			catch (Exception e) {
				fail();
			}
		}
	}


}

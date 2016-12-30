package ltj.message.bo;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import ltj.message.bo.rule.RulesDataBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.dao.rule.RuleDao;
import ltj.message.util.PrintUtil;
import ltj.message.vo.rule.RuleVo;

public class RuleDataBoTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(RuleDataBoTest.class);
	
	@Resource
	private RulesDataBo rulesDataBo;
	
	@Resource
	private RuleDao ruleDao;;

	private List<RuleVo> ruleList;
	
	@Before
	public void setup() {
		ruleList = ruleDao.getActiveRules();
	}
	
	@Test
	public void test() {
		List<RuleVo> rules = rulesDataBo.getCurrentRules();
		for (RuleVo vo : rules) {
			assertTrue(containsRule(vo.getRuleName()));
			logger.info(PrintUtil.prettyPrint(vo, 2));
		}
	}

	boolean containsRule(String ruleName) {
		for (RuleVo vo : ruleList) {
			if (ruleName.equals(vo.getRuleName())) {
				return true;
			}
		}
		return false;
	}
}

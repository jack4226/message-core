package ltj.message.bo;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import ltj.message.bo.rule.RulesDataBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.RuleNameType;
import ltj.message.dao.rule.RuleDao;
import ltj.message.util.PrintUtil;
import ltj.message.vo.rule.RuleElementVo;
import ltj.message.vo.rule.RuleVo;

public class RuleDataBoTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(RuleDataBoTest.class);
	
	@Resource
	private RulesDataBo rulesDataBo;
	
	@Resource
	private RuleDao ruleDao;;

	private List<RuleVo> ruleList;
	private String postmasterEexclusions;
	private String subsTargerText;
	
	@Before
	public void setup() {
		ruleList = ruleDao.getActiveRules();
		for (RuleVo vo : ruleList) {
			if (RuleNameType.HARD_BOUNCE.name().equals(vo.getRuleName())) {
				for (RuleElementVo elmVo : vo.getRuleElementVos()) {
					if ("excludingPostmastersBo".equals(elmVo.getExclListProc())) {
						postmasterEexclusions = elmVo.getExclusions();
						break;
					}
				}
			}
			else if (RuleNameType.SUBSCRIBE.name().equals(vo.getRuleName())) {
				for (RuleElementVo elmVo : vo.getRuleElementVos()) {
					if ("mailingListRegExBo".equals(elmVo.getTargetProc())) {
						subsTargerText = elmVo.getTargetText();
					}
				}
			}
		}
		assertNotNull(postmasterEexclusions);
		assertNotNull(subsTargerText);
	}
	
	@Test
	public void test() {
		List<RuleVo> rules = rulesDataBo.getCurrentRules();
		String all_exclusions = null;
		String new_targetText = null;
		for (RuleVo vo : rules) {
			assertTrue(containsRule(vo.getRuleName()));
			if (RuleNameType.HARD_BOUNCE.name().equals(vo.getRuleName())) {
				logger.info(PrintUtil.prettyPrint(vo, 2));
				for (RuleElementVo elmVo : vo.getRuleElementVos()) {
					if ("excludingPostmastersBo".equals(elmVo.getExclListProc())) {
						all_exclusions = elmVo.getExclusions();
						break;
					}
				}
			}
			else if (RuleNameType.SUBSCRIBE.name().equals(vo.getRuleName())) {
				logger.info(PrintUtil.prettyPrint(vo, 2));
				for (RuleElementVo elmVo : vo.getRuleElementVos()) {
					if ("mailingListRegExBo".equals(elmVo.getTargetProc())) {
						new_targetText = elmVo.getTargetText();
					}
				}
			}
			else {
				//logger.info(PrintUtil.prettyPrint(vo, 2));
			}
		}
		// verify exclusions
		assertNotNull(all_exclusions);
		assertTrue(StringUtils.contains(all_exclusions, postmasterEexclusions));
		assertNotEquals(all_exclusions, postmasterEexclusions);
		// verify target text override 
		assertNotNull(new_targetText);
		assertNotEquals(subsTargerText, new_targetText);
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

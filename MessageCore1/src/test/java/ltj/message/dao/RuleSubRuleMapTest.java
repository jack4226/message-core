package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.data.preload.RuleNameEnum;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.rule.RuleSubRuleMapDao;
import ltj.message.vo.rule.RuleSubRuleMapVo;

public class RuleSubRuleMapTest extends DaoTestBase {
	@Resource
	private RuleSubRuleMapDao ruleSubRuleMapDao;
	
	final String testRuleName = RuleNameEnum.HARD_BOUNCE.name();
	final String testSubRuleName = "MailboxFull_Body_Match";
	
	@Test
	public void testSubRule() {
		try {
			List<RuleSubRuleMapVo> list = selectByRuleName(testRuleName);
			assertTrue(list.size()>0);
			RuleSubRuleMapVo vo = insert(list, testSubRuleName);
			assertNotNull(vo);
			RuleSubRuleMapVo vo2 = selectByPrimaryKey(vo.getRuleName(), vo.getSubRuleName());
			assertNotNull(vo2);
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<RuleSubRuleMapVo> selectByRuleName(String ruleName) {
		List<RuleSubRuleMapVo> list = ruleSubRuleMapDao.getByRuleName(ruleName);
		System.out.println("RuleSubRuleMapDao - selectByRuleName: rules found " + list.size());
		return list;
	}

	private RuleSubRuleMapVo selectByPrimaryKey(String ruleName, String subRuleName) {
		RuleSubRuleMapVo vo = ruleSubRuleMapDao.getByPrimaryKey(ruleName, subRuleName);
		System.out.println("RuleSubRuleMapDao - selectByPrimaryKey: " + LF + vo);
		return vo;
	}

	private int update(RuleSubRuleMapVo ruleSubRuleMapVo) {
		ruleSubRuleMapVo.setSubRuleSeq(ruleSubRuleMapVo.getSubRuleSeq());
		int rows = ruleSubRuleMapDao.update(ruleSubRuleMapVo);
		System.out.println("RuleSubRuleMapDao - update: rows updated " + rows);
		return rows;
	}

	private RuleSubRuleMapVo insert(List<RuleSubRuleMapVo> list, String subRuleName) {
		if (list.size() > 0) {
			RuleSubRuleMapVo ruleSubRuleMapVo = list.get(list.size() - 1);
			ruleSubRuleMapVo.setSubRuleName(subRuleName);
			ruleSubRuleMapVo.setSubRuleSeq(ruleSubRuleMapVo.getSubRuleSeq() + 1);
			int rows = ruleSubRuleMapDao.insert(ruleSubRuleMapVo);
			System.out.println("RuleSubRuleMapDao - insertSubRule: rows inserted " + rows + LF + ruleSubRuleMapVo);
			return ruleSubRuleMapVo;
		}
		return null;
	}

	private int deleteByPrimaryKey(RuleSubRuleMapVo vo) {
		int rowsDeleted = ruleSubRuleMapDao.deleteByPrimaryKey(vo.getRuleName(),
				vo.getSubRuleName());
		System.out.println("RuleSubRuleMapDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}

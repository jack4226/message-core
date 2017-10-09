package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ltj.data.preload.RuleNameEnum;
import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.rule.RuleLogicDao;
import ltj.message.vo.rule.RuleLogicVo;

public class RuleLogicTest extends DaoTestBase {
	@Autowired
	private RuleLogicDao ruleLogicDao;
	
	final static String testRuleName = RuleNameEnum.Executable_Attachment.name();

	@Test
	public void testRuleLogic() {
		try {
			List<RuleLogicVo> listActive = selectActiveRules();
			assertTrue(listActive.size() > 0);
			List<RuleLogicVo> listAll = selectAllRules();
			assertTrue(listAll.size() > 0);
			RuleLogicVo vo = selectByPrimaryKey(testRuleName);
			assertNotNull(vo);
			RuleLogicVo vo2 = insert(vo);
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setRuleName(vo2.getRuleName());
			vo.setOrigRuleName(vo2.getOrigRuleName());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(1, rowsUpdated);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(1, rowsDeleted);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testGetBuiltinRules() {
		List<RuleLogicVo> list1 = ruleLogicDao.getAll(true);
		assertFalse(list1.isEmpty());
		
		boolean hasSubrule = false;
		for (RuleLogicVo vo: list1) {
			if (ruleLogicDao.getHasSubRules(vo.getRuleName())) {
				hasSubrule = true;
			}
		}
		assertTrue(hasSubrule);
				
		List<RuleLogicVo> list2 = ruleLogicDao.getAllSubRules(false);
		assertFalse(list2.isEmpty());
		int size2 = list2.size();
		
		list2 = ruleLogicDao.getAllSubRules(true);
		assertTrue(size2 > list2.size());
		
		List<String> list3 = ruleLogicDao.getBuiltinRuleNames4Web();
		assertFalse(list3.isEmpty());
		logger.info("Builtin Rule Names: " + list3);
		
		List<String> list4 = ruleLogicDao.getCustomRuleNames4Web();
		assertFalse(list4.isEmpty());
		logger.info("Custom Rule Names: " + list4);
	}

	private RuleLogicVo selectByPrimaryKey(String ruleName) {
		RuleLogicVo vo = ruleLogicDao.getByRuleName(ruleName);
		if (vo != null) {
			logger.info("RuleLogicDao - selectByPrimaryKey: " + LF + vo);
		}
		return vo;
	}

	private List<RuleLogicVo> selectActiveRules() {
		List<RuleLogicVo> list = ruleLogicDao.getActiveRules();
		if (!list.isEmpty()) {
			logger.info("RuleLogicDao - selectActiveRules: " + list.size() + LF + list.get(0));
		}
		return list;
	}

	private List<RuleLogicVo> selectAllRules() {
		List<RuleLogicVo> listBuiltIn = ruleLogicDao.getAll(true);
		if (!listBuiltIn.isEmpty()) {
			logger.info("RuleLogicDao - selectAllRules - Builtin rules: " + listBuiltIn.size());
		}
		List<RuleLogicVo> listCustom = ruleLogicDao.getAll(false);
		if (!listCustom.isEmpty()) {
			logger.info("RuleLogicDao - selectAllRules - Custom rules: " + listCustom.size());
		}
		List<RuleLogicVo> listAll = new ArrayList<RuleLogicVo>();
		listAll.addAll(listBuiltIn);
		listAll.addAll(listCustom);
		return listAll;
	}

	private int update(RuleLogicVo ruleLogicVo) {
		if (StatusId.ACTIVE.value().equals(ruleLogicVo.getStatusId())) {
			ruleLogicVo.setStatusId(StatusId.ACTIVE.value());
		}
		int rows = ruleLogicDao.update(ruleLogicVo);
		logger.info("RuleLogicDao - update: rows updated " + rows);
		return rows;
	}

	private RuleLogicVo insert(RuleLogicVo ruleLogicVo) {
		ruleLogicVo.setRuleName(ruleLogicVo.getRuleName()+"_v2");
		int rows = ruleLogicDao.insert(ruleLogicVo);
		logger.info("RuleLogicDao - insert: rows inserted " + rows + LF + ruleLogicVo);
		return ruleLogicVo;
	}

	private int deleteByPrimaryKey(RuleLogicVo vo) {
		logger.info("RuleLogicDao - deleteByPrimaryKey: " + vo.getRuleName());
		int rowsDeleted = ruleLogicDao.deleteByPrimaryKey(vo.getRuleName());
		logger.info("RuleLogicDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}

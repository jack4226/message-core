package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.template.GlobalVariableDao;
import ltj.vo.template.GlobalVariableVo;

public class GlobalVariableTest extends DaoTestBase {
	@Resource
	private GlobalVariableDao globalVariableDao;
	Timestamp updtTime = new Timestamp(System.currentTimeMillis());
	final String testVariableName = "CurrentDate";

	@Test
	public void testGlobalVariable() {
		try {
			List<GlobalVariableVo> list = selectByVariableName(testVariableName);
			assertTrue(list.size()>0);
			GlobalVariableVo vo1 = selectByPrimaryKey(list.get(list.size()-1));
			assertNotNull(vo1);
			GlobalVariableVo vo2 = globalVariableDao.getByBestMatch(vo1.getVariableName(), vo1.getStartTime());
			assertNotNull(vo2);
			GlobalVariableVo vo3 = insert(vo1.getVariableName());
			assertNotNull(vo3);
			vo1.setRowId(vo3.getRowId());
			vo1.setStartTime(vo3.getStartTime());
			assertTrue(vo1.equalsTo(vo3));
			int rowsUpdated = update(vo3);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo3);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<GlobalVariableVo> selectByVariableName(String varbleName) {
		List<GlobalVariableVo> variables = globalVariableDao.getByVariableName(varbleName);
		for (Iterator<GlobalVariableVo> it = variables.iterator(); it.hasNext();) {
			GlobalVariableVo vo = it.next();
			logger.info("GlobalVariableDao - selectByVariableName: " + LF + vo);
		}
		return variables;
	}

	private GlobalVariableVo selectByPrimaryKey(GlobalVariableVo _vo) {
		GlobalVariableVo vo = globalVariableDao.getByPrimaryKey(_vo.getVariableName(), _vo.getStartTime());
		if (vo!=null) {
			logger.info("GlobalVariableDao - selectByPrimaryKey: " + LF + vo);
		}
		return vo;
	}

	private int update(GlobalVariableVo globalVariableVo) {
		globalVariableVo.setVariableValue(updtTime.toString());
		int rows = globalVariableDao.update(globalVariableVo);
		logger.info("GlobalVariableDao - update: rows upadted " + rows);
		return rows;
	}

	private GlobalVariableVo insert(String varbleName) {
		List<GlobalVariableVo> list = globalVariableDao.getByVariableName(varbleName);
		if (list.size() > 0) {
			GlobalVariableVo vo = list.get(list.size() - 1);
			vo.setStartTime(new Timestamp(new java.util.Date().getTime()));
			int rows = globalVariableDao.insert(vo);
			logger.info("GlobalVariableDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(vo);
		}
		return null;
	}

	private int deleteByPrimaryKey(GlobalVariableVo vo) {
		int rowsDeleted = globalVariableDao.deleteByPrimaryKey(vo.getVariableName(),
				vo.getStartTime());
		logger.info("GlobalVariableDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.template.TemplateVariableDao;
import ltj.vo.template.TemplateVariableVo;

public class TemplateVariableTest extends DaoTestBase {
	@Resource
	private TemplateVariableDao templateVariableDao;
	
	static Timestamp updtTime = new Timestamp(System.currentTimeMillis());
	final static String testVariableName = "CurrentDate";
	
	@Test
	public void testTemplateVariable() {
		try {
			List<TemplateVariableVo> list = selectByVariableName(testVariableName);
			assertTrue(list.size() > 0);
			TemplateVariableVo vo1 = selectByPrimaryKey(list.get(list.size() - 1));
			assertNotNull(vo1);
			TemplateVariableVo vo2 = templateVariableDao.getByBestMatch(vo1.getTemplateId(), vo1.getClientId(),
					vo1.getVariableName(), vo1.getStartTime());
			assertNotNull(vo2);
			TemplateVariableVo vo3 = insert(vo1.getVariableName());
			assertNotNull(vo3);
			vo1.setRowId(vo3.getRowId());
			vo1.setTemplateId(vo3.getTemplateId());
			assertTrue(vo1.equalsTo(vo3));
			int rows = update(vo3);
			assertEquals(1, rows);
			rows = deleteByPrimaryKey(vo3);
			assertEquals(1, rows);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<TemplateVariableVo> selectByVariableName(String vname) {
		List<TemplateVariableVo> variables = templateVariableDao.getByVariableName(vname);
		for (Iterator<TemplateVariableVo> it = variables.iterator(); it.hasNext();) {
			TemplateVariableVo templateVariableVo = it.next();
			logger.info("TemplateVariableDao - selectByVariableName: " + LF + templateVariableVo);
		}
		return variables;
	}

	private TemplateVariableVo selectByPrimaryKey(TemplateVariableVo vo) {
		TemplateVariableVo tmplt = templateVariableDao.getByPrimaryKey(vo.getTemplateId(),
				vo.getClientId(), vo.getVariableName(), vo.getStartTime());
		if (tmplt!=null) {
			logger.info("TemplateVariableDao - selectByPrimaryKey: " + LF + tmplt);
		}
		return tmplt;
	}

	private int update(TemplateVariableVo vo) {
		vo.setVariableValue(updtTime.toString());
		int rows = templateVariableDao.update(vo);
		logger.info("TemplateVariableDao - update: rows updated " + rows);
		return rows;
	}

	private TemplateVariableVo insert(String vname) {
		List<TemplateVariableVo> list = templateVariableDao.getByVariableName(vname);
		if (list.size() > 0) {
			TemplateVariableVo templateVariableVo = list.get(list.size() - 1);
			templateVariableVo.setTemplateId(templateVariableVo.getTemplateId() + "_v2");
			int rows = templateVariableDao.insert(templateVariableVo);
			logger.info("TemplateVariableDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(templateVariableVo);
		}
		return null;
	}

	private int deleteByPrimaryKey(TemplateVariableVo vo) {
		int rows = templateVariableDao.deleteByPrimaryKey(vo.getTemplateId(), vo.getClientId(),
				vo.getVariableName(), vo.getStartTime());
		logger.info("TemplateVariableDao - deleteByPrimaryKey: Rows Deleted: " + rows);
		return rows;
	}
}

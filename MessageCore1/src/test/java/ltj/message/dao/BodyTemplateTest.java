package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.vo.template.BodyTemplateVo;

public class BodyTemplateTest extends DaoTestBase {
	@Resource
	private BodyTemplateDao bodyTemplateDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testTemplateId = "WeekendDeals";

	final static String TestTmpltValue = "Dear customer, here is a list of great deals on gardening tools provided to you by ${company.url}";
	
	@Test
	public void testBodyTemplate() {
		try {
			List<BodyTemplateVo> list = selectByTemplateId(testTemplateId);
			assertTrue(list.size()>0);
			BodyTemplateVo vo1 = selectByPrimaryKey(list.get(list.size()-1));
			assertNotNull(vo1);
			BodyTemplateVo vo2 = bodyTemplateDao.getByBestMatch(vo1.getTemplateId(), vo1.getClientId(),
					vo1.getStartTime());
			assertNotNull(vo2);
			BodyTemplateVo vo3 = insert(testTemplateId);
			assertNotNull(vo3);
			vo1.setRowId(vo3.getRowId());
			vo1.setTemplateId(vo3.getTemplateId());
			assertTrue(vo1.equalsTo(vo3));
			int rowsUpdated = update(vo3);
			assertEquals(rowsUpdated, 1);
			BodyTemplateVo vo4 = bodyTemplateDao.getByRowId(vo3.getRowId());
			assertNotNull(vo4);
			assertEquals(TestTmpltValue, vo4.getTemplateValue());
			int rowsDeleted = deleteByPrimaryKey(vo3);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<BodyTemplateVo> selectByTemplateId(String templateId) {
		List<BodyTemplateVo> variables = bodyTemplateDao.getByTemplateId(templateId);
		for (Iterator<BodyTemplateVo> it = variables.iterator(); it.hasNext();) {
			BodyTemplateVo bodyTemplateVo = it.next();
			logger.info("BodyTemplateDao - selectByTemplateId: " + LF + bodyTemplateVo);
		}
		return variables;
	}

	private BodyTemplateVo selectByPrimaryKey(BodyTemplateVo vo) {
		BodyTemplateVo bodyvo = bodyTemplateDao.getByPrimaryKey(vo.getTemplateId(),
				vo.getClientId(), vo.getStartTime());
		if (bodyvo!=null) {
			logger.info("BodyTemplateDao - selectByPrimaryKey: " + LF + bodyvo);
		}
		return bodyvo;
	}

	private int update(BodyTemplateVo bodyTemplateVo) {
		bodyTemplateVo.setTemplateValue(TestTmpltValue);
		int rows = bodyTemplateDao.update(bodyTemplateVo);
		logger.info("BodyTemplateDao - update: rows updated " + rows);
		return rows;
	}

	private BodyTemplateVo insert(String templateId) {
		List<BodyTemplateVo> list = bodyTemplateDao.getByTemplateId(templateId);
		if (list.size() > 0) {
			BodyTemplateVo vo = list.get(list.size() - 1);
			vo.setTemplateId(vo.getTemplateId()+"_v2");
			int rows = bodyTemplateDao.insert(vo);
			logger.info("BodyTemplateDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(vo);
		}
		return null;
	}

	private int deleteByPrimaryKey(BodyTemplateVo vo) {
		int rowsDeleted = bodyTemplateDao.deleteByPrimaryKey(vo.getTemplateId(), vo.getClientId(),
				vo.getStartTime());
		logger.info("BodyTemplateDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}

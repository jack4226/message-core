package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.template.SubjTemplateDao;
import ltj.vo.template.SubjTemplateVo;

public class SubjTemplateTest extends DaoTestBase {
	@Resource
	private SubjTemplateDao subjTemplateDao;
	
	Timestamp updtTime = new Timestamp(System.currentTimeMillis());
	final String testTemplateId = "WeekendDeals";

	@Test
	public void testSubtemplate() {
		try {
			List<SubjTemplateVo> list = selectByTemplateId(testTemplateId);
			assertTrue(list.size()>0);
			SubjTemplateVo vo = selectByPrimaryKey(list.get(list.size()-1));
			assertNotNull(vo);
			SubjTemplateVo vo2 = insert(testTemplateId);
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setTemplateId(vo2.getTemplateId());
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

	private List<SubjTemplateVo> selectByTemplateId(String templateId) {
		List<SubjTemplateVo> variables = subjTemplateDao.getByTemplateId(templateId);
		for (Iterator<SubjTemplateVo> it = variables.iterator(); it.hasNext();) {
			SubjTemplateVo subjTemplateVo = it.next();
			System.out.println("RuleElementDao - selectByTemplateId: " + LF + subjTemplateVo);
		}
		return variables;
	}

	private SubjTemplateVo selectByPrimaryKey(SubjTemplateVo vo) {
		SubjTemplateVo subjvo = subjTemplateDao.getByPrimaryKey(vo.getTemplateId(), vo.getClientId(), vo.getStartTime());
		if (subjvo!=null) {
			System.out.println("RuleElementDao - selectByPrimaryKey: " + LF + subjvo);
		}
		return subjvo;
	}

	private int update(SubjTemplateVo subjTemplateVo) {
		subjTemplateVo.setTemplateValue("Weekend Deals at mydot.com");
		int rows = subjTemplateDao.update(subjTemplateVo);
		System.out.println("RuleElementDao - update: rows updated " + rows);
		return rows;
	}

	private SubjTemplateVo insert(String templateId) {
		List<SubjTemplateVo> list = subjTemplateDao.getByTemplateId(templateId);
		if (list.size() > 0) {
			SubjTemplateVo vo = list.get(list.size() - 1);
			vo.setTemplateId(vo.getTemplateId()+"_v2");
			int rows = subjTemplateDao.insert(vo);
			System.out.println("RuleElementDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(vo);
		}
		return null;
	}

	private int deleteByPrimaryKey(SubjTemplateVo vo) {
		int rows = subjTemplateDao.deleteByPrimaryKey(vo.getTemplateId(), vo.getClientId(),
				vo.getStartTime());
		System.out.println("RuleElementDao - deleteByPrimaryKey: Rows Deleted: " + rows);
		return rows;
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.client.ReloadFlagsDao;
import ltj.message.vo.ReloadFlagsVo;

public class ReloadFlagsTest extends DaoTestBase {
	@Resource
	private ReloadFlagsDao reloadDao;
	
	@Test
	@Rollback(value=true)
	public void testReloadFlags() {
		ReloadFlagsVo vo = select();
		assertNotNull(vo);
		int rowsUpdated = update(vo);
		assertEquals(1, rowsUpdated);
		rowsUpdated = recordsChanged();
		assertTrue(rowsUpdated >= 5);
	}
	
	private ReloadFlagsVo select() {
		ReloadFlagsVo flags = reloadDao.select();
		logger.info("ReloadFlagsDao - select: "+flags);
		return flags;
	}
	
	private int update(ReloadFlagsVo vo) {
		int rows = 0;
		if (vo!=null) {
			vo.setClients(vo.getClients() + 1);
			vo.setRules(vo.getRules() + 1);
			vo.setActions(vo.getActions() + 1);
			vo.setTemplates(vo.getTemplates() + 1);
			vo.setSchedules(vo.getSchedules() + 1);
			rows = reloadDao.update(vo);
			logger.info("ReloadFlagsDao - update: rows updated "+rows);
		}
		return rows;
	}
	
	private int recordsChanged() {
		int rows = reloadDao.updateClientReloadFlag();
		rows += reloadDao.updateRuleReloadFlag();
		rows += reloadDao.updateActionReloadFlag();
		rows += reloadDao.updateTemplateReloadFlag();
		rows += reloadDao.updateScheduleReloadFlag();
		return rows;
	}
}

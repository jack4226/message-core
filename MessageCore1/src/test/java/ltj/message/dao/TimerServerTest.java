package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.timer.TimerServerDao;
import ltj.message.vo.TimerServerVo;

public class TimerServerTest extends DaoTestBase {
	@Resource
	private TimerServerDao timerServerDao;
	
	@Test
	public void testTimerServer1() {
		try {
			List<TimerServerVo> list = selectAll();
			assertTrue(list.size() > 0);
			TimerServerVo vo = selectByServerName(list.get(0).getServerName());
			assertNotNull(vo);
			TimerServerVo vo2 = insert(vo.getServerName());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setServerName(vo2.getServerName());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(1, rows);
			rows = delete(vo2);
			assertEquals(1, rows);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testTimerServer2() {
		try {
			List<TimerServerVo> list = selectAll();
			assertTrue(list.size() > 0);
			TimerServerVo vo = timerServerDao.getByPrimaryKey(list.get(0).getRowId());
			assertNotNull(vo);
			TimerServerVo vo2 = insert(vo.getServerName());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setServerName(vo2.getServerName());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(1, rows);
			rows = timerServerDao.deleteByPrimaryKey(vo2.getRowId());
			assertEquals(1, rows);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<TimerServerVo> selectAll() {
		List<TimerServerVo> timerServeres = timerServerDao.getAll(false);
		for (Iterator<TimerServerVo> it=timerServeres.iterator(); it.hasNext();) {
			TimerServerVo timerServerVo = it.next();
			logger.info("TimerServerDao - selectAll: " + LF + timerServerVo);
		}
		return timerServeres;
	}
	
	private TimerServerVo selectByServerName(String serverName) {
		TimerServerVo vo2 = timerServerDao.getByServerName(serverName);
		if (vo2 != null) {
			logger.info("TimerServerDao - selectByPrimaryKey: " + LF + vo2);
		}
		return vo2;
	}

	private int update(TimerServerVo timerServerVo) {
		if (StatusId.ACTIVE.value().equals(timerServerVo.getStatusId())) {
			timerServerVo.setStatusId(StatusId.ACTIVE.value());
		}
		int rows = timerServerDao.update(timerServerVo);
		logger.info("TimerServerDao - update: rows updated " + rows);
		return rows;
	}
	
	private int delete(TimerServerVo timerServerVo) {
		int rowsDeleted = timerServerDao.deleteByServerName(timerServerVo.getServerName());
		logger.info("TimerServerDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	private TimerServerVo insert(String serverName) {
		TimerServerVo vo = timerServerDao.getByServerName(serverName);
		if (vo != null) {
			vo.setServerName(vo.getServerName() + "_v2");
			int rows = timerServerDao.insert(vo);
			logger.info("TimerServerDao - insert: rows inserted " + rows);
			return selectByServerName(vo.getServerName());
		}
		return null;
	}
}

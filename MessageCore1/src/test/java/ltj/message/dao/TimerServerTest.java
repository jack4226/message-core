package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.StatusIdCode;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.timer.TimerServerDao;
import ltj.message.vo.TimerServerVo;

public class TimerServerTest extends DaoTestBase {
	@Resource
	private TimerServerDao timerServerDao;
	
	@Test
	public void testTimerServer() {
		try {
			List<TimerServerVo> list = selectAll();
			assertTrue(list.size()>0);
			TimerServerVo vo = selectByPrimaryKey(list.get(0).getServerName());
			assertNotNull(vo);
			TimerServerVo vo2 = insert(vo.getServerName());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setServerName(vo2.getServerName());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(rows,1);
			rows = delete(vo2);
			assertEquals(rows,1);
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
			System.out.println("TimerServerDao - selectAll: "+LF+timerServerVo);
		}
		return timerServeres;
	}
	
	private TimerServerVo selectByPrimaryKey(String serverName) {
		TimerServerVo vo2 = timerServerDao.getByPrimaryKey(serverName);
		if (vo2 != null)
			System.out.println("TimerServerDao - selectByPrimaryKey: "+LF+vo2);
		return vo2;
	}

	private int update(TimerServerVo timerServerVo) {
		if (StatusIdCode.ACTIVE.equals(timerServerVo.getStatusId())) {
			timerServerVo.setStatusId(StatusIdCode.ACTIVE);
		}
		int rows = timerServerDao.update(timerServerVo);
		System.out.println("TimerServerDao - update: rows updated "+rows);
		return rows;
	}
	
	private int delete(TimerServerVo timerServerVo) {
		int rowsDeleted = timerServerDao.deleteByPrimaryKey(timerServerVo.getServerName());
		System.out.println("TimerServerDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	private TimerServerVo insert(String serverName) {
		TimerServerVo vo = timerServerDao.getByPrimaryKey(serverName);
		if (vo!=null) {
			vo.setServerName(vo.getServerName()+"_v2");
			int rows = timerServerDao.insert(vo);
			System.out.println("TimerServerDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(vo.getServerName());
		}
		return null;
	}
}

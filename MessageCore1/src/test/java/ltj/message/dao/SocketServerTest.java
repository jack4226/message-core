package ltj.message.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.servers.SocketServerDao;
import ltj.message.vo.SocketServerVo;

public class SocketServerTest extends DaoTestBase {
	@Autowired
	private SocketServerDao socketServerDao;
	
	@Test
	public void testSocketServer1() {
		try {
			List<SocketServerVo> svrs = socketServerDao.getAll(true);
			assertFalse(svrs.isEmpty());
			SocketServerVo vo1 = socketServerDao.getByServerName(svrs.get(0).getServerName());
			assertNotNull(vo1);
			int rowsUpdated  = update(vo1);
			assertTrue(rowsUpdated > 0);
			SocketServerVo vo2 = insert();
			int rowsDeleted = delete(vo2);
			assertTrue(rowsDeleted > 0);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testSocketServer2() {
		try {
			List<SocketServerVo> svrs = socketServerDao.getAll(true);
			assertFalse(svrs.isEmpty());
			SocketServerVo vo1 = socketServerDao.getByPrimaryKey(svrs.get(0).getRowId());
			assertNotNull(vo1);
			int rowsUpdated  = update(vo1);
			assertTrue(rowsUpdated > 0);
			SocketServerVo vo2 = insert();
			int rowsDeleted = socketServerDao.deleteByPrimaryKey(vo2.getRowId());
			assertTrue(rowsDeleted > 0);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private int update(SocketServerVo vo) {
		if (!StatusId.ACTIVE.value().equals(vo.getStatusId())) {
			vo.setStatusId(StatusId.ACTIVE.value());
		}
		int rows = socketServerDao.update(vo);
		logger.info("SocketServerDao - update: "+vo);
		return rows;
	}
	
	private int delete(SocketServerVo socketServerVo) {
		int rowsDeleted = socketServerDao.deleteByServerName(socketServerVo.getServerName());
		logger.info("SocketServerDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private SocketServerVo insert() {
		List<SocketServerVo> socketServeres = socketServerDao.getAll(false);
		if (socketServeres.size()>0) {
			SocketServerVo socketServerVo = socketServeres.get(socketServeres.size()-1);
			socketServerVo.setServerName(socketServerVo.getServerName()+"_v2");
			socketServerVo.setSocketPort(socketServerVo.getSocketPort()+10);
			socketServerDao.insert(socketServerVo);
			logger.info("SocketServerDao - insert: "+socketServerVo);
			return socketServerVo;
		}
		throw new IllegalStateException("socket_server table is empty.");
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.template.ClientVariableDao;
import ltj.vo.template.ClientVariableVo;

public class ClientVariableTest extends DaoTestBase {
	@Resource
	private ClientVariableDao clientVariableDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testVariableName = "CurrentDate";

	@Test
	public void testClientVariables() {
		try {
			List<ClientVariableVo> lst1 = selectByVariableName(testVariableName);
			assertTrue(lst1.size()>0);
			List<ClientVariableVo> lst2 = selectByClientId(lst1.get(0).getClientId());
			assertTrue(lst2.size()>0);
			ClientVariableVo vo1 = selectByPromaryKey(lst2.get(lst2.size()-1));
			assertNotNull(vo1);
			ClientVariableVo vo2 = clientVariableDao.getByBestMatch(vo1.getClientId(), vo1.getVariableName(), vo1.getStartTime());
			assertNotNull(vo2);
			ClientVariableVo vo3 = insert(lst2.get(lst2.size()-1).getClientId());
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

	private List<ClientVariableVo> selectByVariableName(String varbleName) {
		List<ClientVariableVo> variables = clientVariableDao.getByVariableName(varbleName);
		for (Iterator<ClientVariableVo> it = variables.iterator(); it.hasNext();) {
			ClientVariableVo vo = it.next();
			logger.info("ClientVariableDao - selectByVariableName: " + LF + vo);
		}
		return variables;
	}

	private List<ClientVariableVo> selectByClientId(String clientId) {
		List<ClientVariableVo> list = clientVariableDao.getCurrentByClientId(clientId);
		logger.info("ClientVariableDao - selectByClientId: rows returned: " + list.size());
		return list;
	}

	private ClientVariableVo selectByPromaryKey(ClientVariableVo vo) {
		ClientVariableVo client = clientVariableDao.getByPrimaryKey(vo.getClientId(), vo.getVariableName(), vo.getStartTime());
		if (client!=null) {
			logger.info("ClientVariableDao - selectByPromaryKey: " + LF + client);
		}
		return client;
	}
	private int update(ClientVariableVo clientVariableVo) {
		clientVariableVo.setVariableValue(updtTime.toString());
		int rows = clientVariableDao.update(clientVariableVo);
		logger.info("ClientVariableDao - update: rows updated " + rows);
		return rows;
	}

	private ClientVariableVo insert(String clientId) {
		List<ClientVariableVo> list = clientVariableDao.getCurrentByClientId(clientId);
		if (list.size() > 0) {
			ClientVariableVo vo = list.get(list.size() - 1);
			vo.setStartTime(new Timestamp(new java.util.Date().getTime()));
			int rows = clientVariableDao.insert(vo);
			logger.info("ClientVariableDao - insert: rows inserted " + rows);
			return selectByPromaryKey(vo);
		}
		return null;
	}

	private int deleteByPrimaryKey(ClientVariableVo vo) {
		int rowsDeleted = clientVariableDao.deleteByPrimaryKey(vo.getClientId(),
				vo.getVariableName(), vo.getStartTime());
		logger.info("ClientVariableDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}

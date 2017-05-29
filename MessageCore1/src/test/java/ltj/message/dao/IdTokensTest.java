package ltj.message.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.client.ClientDao;
import ltj.message.dao.idtokens.IdTokensDao;
import ltj.message.vo.ClientVo;
import ltj.message.vo.IdTokensVo;

public class IdTokensTest extends DaoTestBase {
	@Resource
	private IdTokensDao idTokensDao;
	@Resource
	private ClientDao clientDao;
	
	private static String testClientId = "JBatchCorp";
	
	@Test
	public void insertUpdateDelete() {
		try {
			IdTokensVo vo = insert();
			assertNotNull(vo);
			IdTokensVo vo2 = selectByClientId(vo.getClientId());
			assertNotNull(vo2);
			vo2.setOrigUpdtTime(vo.getOrigUpdtTime());
			vo2.setUpdtTime(vo.getUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			IdTokensVo vo = new IdTokensVo();
			vo.setClientId(testClientId);
			delete(vo);
		}
	}
	
	private IdTokensVo selectByClientId(String clientId) {
		IdTokensVo vo = idTokensDao.getByClientId(clientId);
		if (vo != null) {
			logger.info("IdTokensDao: selectByClientId "+vo);
		}
		return vo;
	}

	private int update(IdTokensVo idTokensVo) {
		IdTokensVo vo = idTokensDao.getByClientId(idTokensVo.getClientId());
		vo.setDescription("For Test SenderId");
		int rows = idTokensDao.update(vo);
		logger.info("IdTokensDao: update "+rows+"\n"+vo);
		return rows;
	}
	private IdTokensVo insert() {
		if (clientDao.getByClientId(testClientId)==null) {
			List<ClientVo> list = clientDao.getAll();
			testClientId = list.get(0).getClientId();
		}
		List<IdTokensVo> list = idTokensDao.getAll();
		for (IdTokensVo vo : list) {
			if (testClientId.equals(vo.getClientId())) {
				delete(vo);
			}
			vo.setClientId(testClientId);
			idTokensDao.insert(vo);
			logger.info("IdTokensDao: insert "+vo);
			return vo;
		}
		return null;
	}
	private int delete(IdTokensVo idTokensVo) {
		int rowsDeleted = idTokensDao.delete(idTokensVo.getClientId());
		logger.info("IdTokensDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
}

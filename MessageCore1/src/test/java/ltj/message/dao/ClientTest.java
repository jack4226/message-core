package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Calendar;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.jbatch.common.TimestampUtil;
import ltj.message.constant.Constants;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.client.ClientDao;
import ltj.message.vo.ClientVo;

public class ClientTest extends DaoTestBase {
	@Resource
	private ClientDao clientDao;
	final String DefaultClientId = Constants.DEFAULT_CLIENTID;

	@Test
	@Rollback(true)
	public void insertSelectDelete() {
		try {
			ClientVo vo = insert();
			assertNotNull(vo);
			assertTrue(vo.getClientId().endsWith("_v2"));
			assertTrue(vo.getDomainName().endsWith(".v2"));
			ClientVo vo2 = select(vo);
			assertNotNull(vo2);
			// sync-up next four fields since differences are expected
			vo.setSystemId(vo2.getSystemId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setPrimaryKey(vo2.getPrimaryKey());
			// end of sync-up
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			delete(vo);
		}
		catch (Exception e) {
			ClientVo v = new ClientVo();
			v.setClientId(DefaultClientId + "_v2");
			delete(v);
			fail();
		}
	}

	private ClientVo select(ClientVo vo) {
		ClientVo vo2 = clientDao.getByClientId(vo.getClientId());
		if (vo2 != null) {
			System.out.println("ClientDao - select: "+vo2);
		}
		return vo2;
	}
	
	private int update(ClientVo vo) {
		ClientVo clientVo = clientDao.getByClientId(vo.getClientId());
		int rows = 0;
		if (clientVo!=null) {
			clientVo.setStatusId("A");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -31);
			String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp(cal.getTime()));
			System.out.println("SystemId: " + systemId);
			clientVo.setSystemId(systemId);
			rows = clientDao.update(clientVo);
			System.out.println("ClientDao - update: rows updated: "+rows);
		}
		return rows;
	}
	
	private int delete(ClientVo vo) {
		int rowsDeleted = clientDao.delete(vo.getClientId());
		System.out.println("ClientDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private ClientVo insert() {
		ClientVo clientVo = clientDao.getByClientId(DefaultClientId);
		if (clientVo!=null) {
			clientVo.setClientId(clientVo.getClientId()+"_v2");
			clientVo.setDomainName(clientVo.getDomainName()+".v2");
			clientDao.insert(clientVo);
			System.out.println("ClientDao - insert: "+clientVo);
			return clientVo;
		}
		return null;
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.smtp.SmtpServerDao;
import ltj.message.vo.SmtpConnVo;

public class SmtpServerTest extends DaoTestBase {
	@Resource
	private SmtpServerDao smtpServerDao;
	
	@Test
	public void testSmtpServer() {
		try {
			List<SmtpConnVo> lst1 = selectAll(true);
			assertTrue(lst1.size()>0);
			List<SmtpConnVo> lst2 = selectAll(false);
			assertTrue(lst2.size()>0);
			SmtpConnVo vo = selectByPrimaryKey(lst2.get(0).getServerName());
			assertNotNull(vo);
			SmtpConnVo vo2 = insert(lst2.get(0).getServerName());
			int rowsUpdated = update(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setServerName(vo2.getServerName());
			assertTrue(vo.equalsTo(vo2));
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo2.getServerName());
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private List<SmtpConnVo> selectAll(boolean forTrial) {
		List<SmtpConnVo> smtpServeres;
		if (forTrial) {
			smtpServeres = smtpServerDao.getAllForTrial(false);
			for (Iterator<SmtpConnVo> it=smtpServeres.iterator(); it.hasNext();) {
				SmtpConnVo smtpConnVo = it.next();
				System.out.println("SmtpServerDao - getAllForTrial(): "+LF+smtpConnVo);
			}
		}
		else {
			smtpServeres = smtpServerDao.getAll(false);
			for (Iterator<SmtpConnVo> it=smtpServeres.iterator(); it.hasNext();) {
				SmtpConnVo smtpConnVo = it.next();
				System.out.println("SmtpServerDao - selectAll(): "+LF+smtpConnVo);
			}
		}
		return smtpServeres;
	}
	
	public SmtpConnVo selectByPrimaryKey(String serverName) {
		SmtpConnVo vo2 = smtpServerDao.getByPrimaryKey(serverName);
		System.out.println("SmtpServerDao - selectByPrimaryKey: "+LF+vo2);
		return vo2;
	}
	
	private int update(SmtpConnVo smtpConnVo) {
		if (StatusId.ACTIVE.value().equals(smtpConnVo.getStatusId())) {
			smtpConnVo.setStatusId(StatusId.ACTIVE.value());
		}
		int rows = smtpServerDao.update(smtpConnVo);
		System.out.println("SmtpServerDao - update: rows updated " + rows );
		return rows;
	}
	
	private int delete(String serverName) {
		int rowsDeleted = smtpServerDao.deleteByPrimaryKey(serverName);
		System.out.println("SmtpServerDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	private SmtpConnVo insert(String serverName) {
		SmtpConnVo smtpConnVo = smtpServerDao.getByPrimaryKey(serverName);
		if (smtpConnVo != null) {
			smtpConnVo.setServerName(smtpConnVo.getServerName()+"_test");
			int rows = smtpServerDao.insert(smtpConnVo);
			System.out.println("SmtpServerDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(smtpConnVo.getServerName());
		}
		return null;
	}
}

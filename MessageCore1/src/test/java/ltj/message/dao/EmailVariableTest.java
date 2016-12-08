package ltj.message.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.vo.emailaddr.EmailVariableVo;

public class EmailVariableTest extends DaoTestBase {
	@Resource
	private EmailVariableDao emailVariableDao;
	final String insertVariableName = "CustomerName";
	
	@Test 
	public void insertSelectDelete() {
		try {
			List<EmailVariableVo> list = selectAll();
			assertTrue(list.size()>0);
			list = selectAllForTrial();
			assertTrue(list.size()>0);
			EmailVariableVo vo = insert();
			assertNotNull(vo);
			EmailVariableVo vo2 = selectByName(vo);
			assertNotNull(vo2);
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			EmailVariableVo vo = new EmailVariableVo();
			vo.setVariableName(insertVariableName + "_v2");
			delete(vo);
		}
	}

	private List<EmailVariableVo> selectAll() {
		List<EmailVariableVo> list = emailVariableDao.getAll();
		System.out.println("EmailVariableDao - selectAll() - size:  "+list.size());
		return list;
	}
	
	private List<EmailVariableVo> selectAllForTrial() {
		List<EmailVariableVo> list = emailVariableDao.getAllForTrial();
		System.out.println("EmailVariableDao - getAllForTrial() - size: " + list.size());
		return list;
	}
	
	private EmailVariableVo selectByName(EmailVariableVo vo) {
		EmailVariableVo emailVariable = emailVariableDao.getByName(vo.getVariableName());
		System.out.println("EmailVariableDao - selectByName: "+LF+emailVariable);
		return emailVariable;
	}
	
	private int update(EmailVariableVo vo) {
		int rowsUpdated = 0;
		if (vo != null) {
			vo.setTableName("Customers");
			rowsUpdated = emailVariableDao.update(vo);
			System.out.println("EmailVariableDao - rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int delete(EmailVariableVo vo) {
		int rowsDeleted = emailVariableDao.deleteByName(vo.getVariableName());
		System.out.println("EmailVariableDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private EmailVariableVo insert() {
		EmailVariableVo vo = emailVariableDao.getByName(insertVariableName);
		if (vo != null) {
			vo.setVariableName(vo.getVariableName()+"_v2");
			emailVariableDao.insert(vo);
			System.out.println("EmailVariableDao - insert: "+LF+vo);
			return vo;
		}
		else {
			return null;
		}
	}
}

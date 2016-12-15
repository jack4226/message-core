package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.emailaddr.UnsubCommentsDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.UnsubCommentsVo;

public class UnsubCommentsTest extends DaoTestBase {
	final String listId = "SMPLLST1";
	final String emailAddr = "jsmith@test.com";
	final int testRowId = 1;
	@Resource
	private UnsubCommentsDao unsubCommentsDao;
	@Resource
	private EmailAddrDao emailAddrDao;

	@Test
	@Rollback(value=true)
	public void testUnsubComments() {
		List<UnsubCommentsVo> list = selectAll();
		if (list.size()==0) {
			insert();
			list = selectAll();
		}
		assertTrue(list.size()>0);
		UnsubCommentsVo vo = selectByPrimaryKey(list.get(0).getRowId());
		assertNotNull(vo);
		UnsubCommentsVo vo2 = insert();
		assertNotNull(vo2);
		vo.setAddTime(vo2.getAddTime());
		vo.setRowId(vo2.getRowId());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		List<UnsubCommentsVo> list2 = selectByEmailAddrId(vo2.getEmailAddrId());
		assertTrue(list2.size()>list.size());
		int rowsDeleted = delete(vo2.getRowId());
		assertEquals(rowsDeleted, 1);
	}
	
	private UnsubCommentsVo selectByPrimaryKey(int rowId) {
		UnsubCommentsVo unsubComments = unsubCommentsDao.getByPrimaryKey(rowId);
		System.out.println("UnsubCommentsDao - selectByPrimaryKey: "+LF+unsubComments);
		return unsubComments;
	}
	
	private List<UnsubCommentsVo> selectAll() {
		List<UnsubCommentsVo> list = unsubCommentsDao.getAll();
		System.out.println("UnsubCommentsDao - getAll() - size: " + list.size());
		list = unsubCommentsDao.getAll();
		for (UnsubCommentsVo vo : list) {
			System.out.println("UnsubCommentsDao - select All: "+LF+vo);
			break;
		}
		return list;
	}
	
	private int update(UnsubCommentsVo vo) {
		vo.setComments(vo.getComments() + LF + " new comments.");
		int rowsUpdated = unsubCommentsDao.update(vo);
		System.out.println("UnsubCommentsDao - rows updated: "+rowsUpdated);
		return rowsUpdated;
	}
	
	private int delete(int rowId) {
		int rowsDeleted = unsubCommentsDao.deleteByPrimaryKey(rowId);
		System.out.println("UnsubCommentsDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private UnsubCommentsVo insert() {
		List<UnsubCommentsVo> list = unsubCommentsDao.getAll();
		if (list.size() > 0) {
			UnsubCommentsVo vo = list.get(list.size() - 1);
			vo.setComments("Test Comments.");
			unsubCommentsDao.insert(vo);
			System.out.println("UnsubCommentsDao - insert: "+LF+vo);
			return vo;
		}
		else {
			UnsubCommentsVo vo = new UnsubCommentsVo();
			vo.setComments("Test Comments.");
			EmailAddrVo addr = emailAddrDao.getByAddress(emailAddr);
			vo.setEmailAddrId(addr.getEmailAddrId());
			vo.setListId(listId);
			unsubCommentsDao.insert(vo);
			System.out.println("UnsubCommentsDao - insert: (empty table) "+LF+vo);
			return vo;
		}
	}

	private List<UnsubCommentsVo> selectByEmailAddrId(long emailAddrId) {
		List<UnsubCommentsVo> list = unsubCommentsDao.getByEmailAddrId(emailAddrId);
		for (UnsubCommentsVo vo2 : list) {
			System.out.println("UnsubCommentsDao - selectByEmailAddrId: rows returned "+list.size()+LF+vo2);			
		}
		return list;
	}
	
}

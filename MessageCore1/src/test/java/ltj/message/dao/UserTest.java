package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.StatusIdCode;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.user.UserDao;
import ltj.message.vo.UserVo;

public class UserTest extends DaoTestBase {
	@Resource
	private UserDao userDao;
	
	@Test
	public void testUser() {
		try {
			List<UserVo> list = selectAll();
			assertTrue(list.size()>0);
			UserVo vo = selectByPrimaryKey(list.get(0).getUserId());
			assertNotNull(vo);
			UserVo vo2 = insert(vo.getUserId());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setUserId(vo2.getUserId());
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
	
	private List<UserVo> selectAll() {
		List<UserVo> users = userDao.getAll(true);
		for (Iterator<UserVo> it=users.iterator(); it.hasNext();) {
			UserVo userVo = it.next();
			System.out.println("UserDao - selectAll: "+LF+userVo);
		}
		return users;
	}
	
	private UserVo selectByPrimaryKey(String userId) {
		UserVo vo2 = userDao.getByPrimaryKey(userId);
		if (vo2 != null) {
			System.out.println("UserDao - selectByPrimaryKey: "+LF+vo2);
		}
		return vo2;
	}
	private int update(UserVo userVo) {
		if (StatusIdCode.ACTIVE.equals(userVo.getStatusId())) {
			userVo.setStatusId(StatusIdCode.ACTIVE);
		}
		int rows = userDao.update(userVo);
		System.out.println("UserDao - update: rows updated "+rows);
		return rows;
	}
	
	private int delete(UserVo userVo) {
		int rowsDeleted = userDao.deleteByPrimaryKey(userVo.getUserId());
		System.out.println("UserDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	private UserVo insert(String userId) {
		UserVo userVo = userDao.getByPrimaryKey(userId);
		if (userVo!=null) {
			userVo.setUserId(userVo.getUserId()+"_v2");
			int rows = userDao.insert(userVo);
			System.out.println("UserDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(userVo.getUserId());
		}
		return null;
	}
}

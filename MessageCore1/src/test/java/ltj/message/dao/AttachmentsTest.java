package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.Constants;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.AttachmentsDao;
import ltj.message.vo.inbox.AttachmentsVo;

public class AttachmentsTest extends DaoTestBase {
	@Resource
	private AttachmentsDao attachmentsDao;
	
	static long testMsgId = 2L;
	
	@Test
	public void insertUpdateDelete() {
		try {
			assertTrue(attachmentsDao.getRandomRecord().size() > 0);
			List<AttachmentsVo> list = selectByMsgId(testMsgId);
			if (list.isEmpty()) {
				list = attachmentsDao.getRandomRecord();
				assertTrue(list.size() > 0);
				testMsgId = list.get(0).getMsgId();
			}
			AttachmentsVo vo = insert(testMsgId);
			assertNotNull(vo);
			List<AttachmentsVo> list2 = selectByMsgId(testMsgId);
			assertTrue(list2.size() == (list.size() + 1));
			AttachmentsVo vo2 = selectByPrimaryKey(vo);
			assertNotNull(vo2);
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			deleteLast(testMsgId);
			e.printStackTrace();
			fail();
		}
	}
	
	private List<AttachmentsVo> selectByMsgId(long msgId) {
		List<AttachmentsVo> list = attachmentsDao.getByMsgId(msgId);
		for (Iterator<AttachmentsVo> it=list.iterator(); it.hasNext();) {
			AttachmentsVo attachmentsVo = it.next();
			System.out.println("AttachmentsDao - selectByMsgId: "+LF+attachmentsVo);
		}
		return list;
	}
	
	private AttachmentsVo selectByPrimaryKey(AttachmentsVo vo) {
		AttachmentsVo attachmentsVo = (AttachmentsVo) attachmentsDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAttchmntDepth(), vo.getAttchmntSeq());
		System.out.println("AttachmentsDao - selectByPrimaryKey: "+LF+attachmentsVo);
		return attachmentsVo;
	}
	
	private int update(AttachmentsVo vo) {
		AttachmentsVo attachmentsVo = (AttachmentsVo) attachmentsDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAttchmntDepth(), vo.getAttchmntSeq());
		int rowsUpdated = 0;
		if (attachmentsVo!=null) {
			attachmentsVo.setAttchmntType("text/plain");
			attachmentsVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			attachmentsVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			rowsUpdated = attachmentsDao.update(attachmentsVo);
			System.out.println("AttachmentsDao - update: "+LF+attachmentsVo);
		}
		return rowsUpdated;
	}
	
	private int deleteByPrimaryKey(AttachmentsVo vo) {
		int rowsDeleted = attachmentsDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAttchmntDepth(),
				vo.getAttchmntSeq());
		System.out.println("AttachmentsDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private AttachmentsVo insert(long msgId) {
		List<AttachmentsVo> list = (List<AttachmentsVo>)attachmentsDao.getByMsgId(msgId);
		if (list.size()>0) {
			AttachmentsVo attachmentsVo = list.get(list.size()-1);
			attachmentsVo.setAttchmntSeq(attachmentsVo.getAttchmntSeq()+1);
			attachmentsDao.insert(attachmentsVo);
			System.out.println("AttachmentsDao - insert: "+LF+attachmentsVo);
			return attachmentsVo;
		}
		return null;
	}

	private void deleteLast(long msgId) {
		List<AttachmentsVo> list = (List<AttachmentsVo>)attachmentsDao.getByMsgId(msgId);
		if (list.size()>1) {
			AttachmentsVo attachmentsVo = list.get(list.size()-1);
			int rows = deleteByPrimaryKey(attachmentsVo);
			System.out.println("AttachmentsDao - deleteLast: "+rows);
		}
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.Constants;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgAttachmentDao;
import ltj.message.vo.inbox.MsgAttachmentVo;

public class MsgAttachmentTest extends DaoTestBase {
	@Resource
	private MsgAttachmentDao msgAttachmentDao;
	
	static long testMsgId = 2L;
	
	@Test
	public void insertUpdateDelete() {
		try {
			assertTrue(msgAttachmentDao.getRandomRecord().size() > 0);
			List<MsgAttachmentVo> list = selectByMsgId(testMsgId);
			if (list.isEmpty()) {
				list = msgAttachmentDao.getRandomRecord();
				assertTrue(list.size() > 0);
				testMsgId = list.get(0).getMsgId();
			}
			MsgAttachmentVo vo = insert(testMsgId);
			assertNotNull(vo);
			List<MsgAttachmentVo> list2 = selectByMsgId(testMsgId);
			assertEquals(list2.size(), (list.size() + 1));
			MsgAttachmentVo vo2 = selectByPrimaryKey(vo);
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
	
	private List<MsgAttachmentVo> selectByMsgId(long msgId) {
		List<MsgAttachmentVo> list = msgAttachmentDao.getByMsgId(msgId);
		for (Iterator<MsgAttachmentVo> it=list.iterator(); it.hasNext();) {
			MsgAttachmentVo msgAttachmentVo = it.next();
			logger.info("MsgAttachmentDao - selectByMsgId: "+LF+msgAttachmentVo);
		}
		return list;
	}
	
	private MsgAttachmentVo selectByPrimaryKey(MsgAttachmentVo vo) {
		MsgAttachmentVo msgAttachmentVo = msgAttachmentDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAttchmntDepth(), vo.getAttchmntSeq());
		logger.info("MsgAttachmentDao - selectByPrimaryKey: "+LF+msgAttachmentVo);
		return msgAttachmentVo;
	}
	
	private int update(MsgAttachmentVo vo) {
		MsgAttachmentVo msgAttachmentVo = msgAttachmentDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAttchmntDepth(), vo.getAttchmntSeq());
		int rowsUpdated = 0;
		if (msgAttachmentVo!=null) {
			msgAttachmentVo.setAttchmntType("text/plain");
			msgAttachmentVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			msgAttachmentVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			rowsUpdated = msgAttachmentDao.update(msgAttachmentVo);
			logger.info("MsgAttachmentDao - update: "+LF+msgAttachmentVo);
		}
		return rowsUpdated;
	}
	
	private int deleteByPrimaryKey(MsgAttachmentVo vo) {
		int rowsDeleted = msgAttachmentDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAttchmntDepth(),
				vo.getAttchmntSeq());
		logger.info("MsgAttachmentDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgAttachmentVo insert(long msgId) {
		List<MsgAttachmentVo> list = msgAttachmentDao.getByMsgId(msgId);
		if (list.size()>0) {
			MsgAttachmentVo msgAttachmentVo = list.get(list.size()-1);
			msgAttachmentVo.setAttchmntSeq(msgAttachmentVo.getAttchmntSeq()+1);
			msgAttachmentDao.insert(msgAttachmentVo);
			logger.info("MsgAttachmentDao - insert: "+LF+msgAttachmentVo);
			return msgAttachmentVo;
		}
		return null;
	}

	private void deleteLast(long msgId) {
		List<MsgAttachmentVo> list = msgAttachmentDao.getByMsgId(msgId);
		if (list.size()>1) {
			MsgAttachmentVo msgAttachmentVo = list.get(list.size()-1);
			int rows = deleteByPrimaryKey(msgAttachmentVo);
			logger.info("MsgAttachmentDao - deleteLast: "+rows);
		}
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgClickCountsDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgUnsubCommentsDao;
import ltj.message.vo.inbox.MsgClickCountsVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgUnsubCommentsVo;

public class MsgUnsubCommentsTest extends DaoTestBase {
	@Resource
	private MsgUnsubCommentsDao msgUnsubCommentsDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	@Resource
	private MsgClickCountsDao clickCountDao;
	
	@Test
	@Rollback(value=false)
	public void testUnsubComments() {
		// insertToEmptyTable();
		List<MsgUnsubCommentsVo> list = selectFirst100();
		if (list.size() == 0) {
			assertNotNull(insertEmpty());
			list = selectFirst100();
		}
		assertTrue(list.size() > 0);
		assertNotNull(insertAnother());
		MsgUnsubCommentsVo vo = select(list.get(0).getRowId());
		assertNotNull(vo);
		MsgUnsubCommentsVo vo2 = insert(vo);
		assertNotNull(vo2);
		vo.setAddTime(vo2.getAddTime());
		vo.setRowId(vo2.getRowId());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = delete(vo2.getRowId());
		assertEquals(rowsDeleted, 1);
	}
	
	private MsgUnsubCommentsVo select(int rowId) {
		MsgUnsubCommentsVo msgUnsubComments = msgUnsubCommentsDao.getByPrimaryKey(rowId);
		logger.info("MsgUnsubCommentsDao - select: "+msgUnsubComments);
		return msgUnsubComments;
	}
	
	private List<MsgUnsubCommentsVo> selectFirst100() {
		List<MsgUnsubCommentsVo> list = msgUnsubCommentsDao.getFirst100();
		logger.info("MsgUnsubCommentsDao - getAll() - size: " + list.size());
		list = msgUnsubCommentsDao.getFirst100();
		for (MsgUnsubCommentsVo vo : list) {
			logger.info("MsgUnsubCommentsDao - select All: "+vo);
			break;
		}
		return list;
	}
	
	private int update(MsgUnsubCommentsVo vo) {
		vo.setComments(vo.getComments() + LF + "Some new comments.");
		int rowsUpdated = msgUnsubCommentsDao.update(vo);
		logger.info("MsgUnsubCommentsDao - rows updated: "+rowsUpdated);
		return rowsUpdated;
	}
	
	private int delete(int rowId) {
		int rowsDeleted = msgUnsubCommentsDao.deleteByPrimaryKey(rowId);
		logger.info("MsgUnsubCommentsDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgUnsubCommentsVo insert(MsgUnsubCommentsVo vo) {
		vo.setComments("Another test Comment.");
		int rows = msgUnsubCommentsDao.insert(vo);
		logger.info("MsgUnsubCommentsDao - insertAnother: rows inserted "+rows+LF+vo);
		return vo;
	}
	private MsgUnsubCommentsVo insertAnother() {
		List<MsgUnsubCommentsVo> list = msgUnsubCommentsDao.getFirst100();
		if (list.size() > 0) {
			int idx = new Random().nextInt(list.size());
			MsgUnsubCommentsVo vo = list.get(idx);
			vo.setComments("Another test Comment - " + StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0'));
			int rows = msgUnsubCommentsDao.insert(vo);
			logger.info("MsgUnsubCommentsDao - insertEmpty: rows inserted "+rows);
			return vo;
		}
		else {
			return null;
		}
	}
	private MsgUnsubCommentsVo insertEmpty() {
		MsgUnsubCommentsVo vo = new MsgUnsubCommentsVo();
		vo.setComments("Test Comments.");
		MsgClickCountsVo countvo = clickCountDao.getRandomRecord();
		assertNotNull(countvo);
		Long msgId = countvo.getMsgId(); //2L;
		MsgInboxVo inboxvo = msgInboxDao.getByPrimaryKey(msgId);
		assertNotNull(inboxvo);
		vo.setMsgId(msgId);
		vo.setEmailAddrId(inboxvo.getFromAddrId());
		//String listId = Constants.DEMOLIST1_NAME;
		vo.setListId(countvo.getListId());
		int rows = msgUnsubCommentsDao.insert(vo);
		logger.info("UnsubCommentsDao - insert: (empty table) "+rows);
		return vo;
	}

}

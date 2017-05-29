package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgClickCountDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgUnsubCmntDao;
import ltj.message.vo.inbox.MsgClickCountVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgUnsubCmntVo;

public class MsgUnsubCmntTest extends DaoTestBase {
	@Resource
	private MsgUnsubCmntDao msgUnsubCmntDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	@Resource
	private MsgClickCountDao clickCountDao;
	
	@Test
	@Rollback(value=false)
	public void testUnsubComments() {
		// insertToEmptyTable();
		List<MsgUnsubCmntVo> list = selectFirst100();
		if (list.size() == 0) {
			assertNotNull(insertEmpty());
			list = selectFirst100();
		}
		assertTrue(list.size() > 0);
		assertNotNull(insertAnother());
		MsgUnsubCmntVo vo = select(list.get(0).getRowId());
		assertNotNull(vo);
		MsgUnsubCmntVo vo2 = insert(vo);
		assertNotNull(vo2);
		vo.setAddTime(vo2.getAddTime());
		vo.setRowId(vo2.getRowId());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = delete(vo2.getRowId());
		assertEquals(rowsDeleted, 1);
	}
	
	private MsgUnsubCmntVo select(int rowId) {
		MsgUnsubCmntVo msgUnsubComments = msgUnsubCmntDao.getByPrimaryKey(rowId);
		logger.info("MsgUnsubCmntDao - select: "+msgUnsubComments);
		return msgUnsubComments;
	}
	
	private List<MsgUnsubCmntVo> selectFirst100() {
		List<MsgUnsubCmntVo> list = msgUnsubCmntDao.getFirst100();
		logger.info("MsgUnsubCmntDao - getAll() - size: " + list.size());
		list = msgUnsubCmntDao.getFirst100();
		for (MsgUnsubCmntVo vo : list) {
			logger.info("MsgUnsubCmntDao - select All: "+vo);
			break;
		}
		return list;
	}
	
	private int update(MsgUnsubCmntVo vo) {
		vo.setComments(vo.getComments() + LF + "Some new comments.");
		int rowsUpdated = msgUnsubCmntDao.update(vo);
		logger.info("MsgUnsubCmntDao - rows updated: "+rowsUpdated);
		return rowsUpdated;
	}
	
	private int delete(int rowId) {
		int rowsDeleted = msgUnsubCmntDao.deleteByPrimaryKey(rowId);
		logger.info("MsgUnsubCmntDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgUnsubCmntVo insert(MsgUnsubCmntVo vo) {
		vo.setComments("Another test Comment.");
		int rows = msgUnsubCmntDao.insert(vo);
		logger.info("MsgUnsubCmntDao - insertAnother: rows inserted "+rows+LF+vo);
		return vo;
	}
	private MsgUnsubCmntVo insertAnother() {
		List<MsgUnsubCmntVo> list = msgUnsubCmntDao.getFirst100();
		if (list.size() > 0) {
			int idx = new Random().nextInt(list.size());
			MsgUnsubCmntVo vo = list.get(idx);
			vo.setComments("Another test Comment - " + StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0'));
			int rows = msgUnsubCmntDao.insert(vo);
			logger.info("MsgUnsubCmntDao - insertEmpty: rows inserted "+rows);
			return vo;
		}
		else {
			return null;
		}
	}
	private MsgUnsubCmntVo insertEmpty() {
		MsgUnsubCmntVo vo = new MsgUnsubCmntVo();
		vo.setComments("Test Comments.");
		MsgClickCountVo countvo = clickCountDao.getRandomRecord();
		assertNotNull(countvo);
		Long msgId = countvo.getMsgId(); //2L;
		MsgInboxVo inboxvo = msgInboxDao.getByPrimaryKey(msgId);
		assertNotNull(inboxvo);
		vo.setMsgId(msgId);
		vo.setEmailAddrId(inboxvo.getFromAddrId());
		//String listId = Constants.DEMOLIST1_NAME;
		vo.setListId(countvo.getListId());
		int rows = msgUnsubCmntDao.insert(vo);
		logger.info("EmailUnsubCmntDao - insert: (empty table) "+rows);
		return vo;
	}

}

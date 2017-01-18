package ltj.message.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgClickCountsDao;
import ltj.message.vo.inbox.MsgClickCountsVo;

public class MsgClickCountsTest extends DaoTestBase {
	@Resource
	private MsgClickCountsDao msgClickCountsDao;

	@Test
	public void insertUpdate() {
		try {
			MsgClickCountsVo vo = selectRecord();
			assertNotNull(vo);
			MsgClickCountsVo vo2 = selectByPrimaryKey(vo.getMsgId());
			assertNotNull(vo2);
			vo2.setComplaintCount(vo.getComplaintCount());
			vo2.setUnsubscribeCount(vo.getUnsubscribeCount());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertTrue(rows > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private MsgClickCountsVo selectRecord() {
		MsgClickCountsVo actions = msgClickCountsDao.getRandomRecord();
		if (actions != null) {
			System.out.println("selectRecord - : " + LF + actions);
			return actions;
		}
		return null;
	}

	private MsgClickCountsVo selectByPrimaryKey(long msgId) {
		MsgClickCountsVo vo = (MsgClickCountsVo) msgClickCountsDao.getByPrimaryKey(msgId);
		System.out.println("selectByPrimaryKey - " + LF + vo);
		return vo;
	}

	private int update(MsgClickCountsVo msgClickCountsVo) {
		int rows = 0;
		msgClickCountsVo.setSentCount(msgClickCountsVo.getSentCount() + 1);
		rows += msgClickCountsDao.update(msgClickCountsVo);
		rows += msgClickCountsDao.updateOpenCount(msgClickCountsVo.getMsgId());
		rows += msgClickCountsDao.updateClickCount(msgClickCountsVo.getMsgId());
		rows += msgClickCountsDao.updateUnsubscribeCount(msgClickCountsVo.getMsgId(), 1);
		rows += msgClickCountsDao.updateComplaintCount(msgClickCountsVo.getMsgId(), 1);
		rows += msgClickCountsDao.updateClickCount(msgClickCountsVo.getMsgId());
		System.out.println("update: rows updated " + rows + LF + msgClickCountsVo);
		return rows;
	}

	void deleteByPrimaryKey(MsgClickCountsVo vo) {
		try {
			int rowsDeleted = msgClickCountsDao.deleteByPrimaryKey(vo.getMsgId());
			System.out.println("deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	MsgClickCountsVo insert() {
		MsgClickCountsVo msgClickCountsVo = msgClickCountsDao.getRandomRecord();
		if (msgClickCountsVo != null) {
			msgClickCountsVo.setMsgId(msgClickCountsVo.getMsgId() + 1);
			msgClickCountsDao.insert(msgClickCountsVo);
			System.out.println("insert: " + LF + msgClickCountsVo);
			return msgClickCountsVo;
		}
		return null;
	}
}

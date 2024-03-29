package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.template.MsgSourceDao;
import ltj.vo.template.MsgSourceVo;

public class MsgSourceTest extends DaoTestBase {
	@Resource
	private MsgSourceDao msgSourceDao;
	Timestamp updtTime = new Timestamp(System.currentTimeMillis());
	final String testMsgSourceId = "WeekendDeals";
	
	final static String TestDesc = "message source for WeekendDeals";

	@Test
	public void testMsgCource() {
		try {
			MsgSourceVo vo1 = selectByPrimaryKey(testMsgSourceId);
			assertNotNull(vo1);
			List<MsgSourceVo> list = selectByFromAddrId(vo1.getFromAddrId());
			assertTrue(list.size()>0);
			vo1 = selectByPrimaryKey(testMsgSourceId);
			MsgSourceVo vo2 = insert(testMsgSourceId);
			assertNotNull(vo2);
			vo1.setRowId(vo2.getRowId());
			vo1.setMsgSourceId(vo2.getMsgSourceId());
			vo1.setUpdtTime(vo2.getUpdtTime());
			vo1.setOrigUpdtTime(vo2.getOrigUpdtTime());
			assertTrue(vo1.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(1, rowsUpdated);
			MsgSourceVo vo3 = msgSourceDao.getByPrimaryKey(vo2.getMsgSourceId());
			assertNotNull(vo3);
			assertEquals(TestDesc, vo3.getDescription());
			assertEquals(true, vo3.getExcludeIdToken());
			assertEquals(true, vo3.getSaveMsgStream());
			assertEquals(true, vo3.getArchiveInd());
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(1, rowsDeleted);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private MsgSourceVo selectByPrimaryKey(String msgSourceId) {
		MsgSourceVo msgSourceVo = msgSourceDao.getByPrimaryKey(msgSourceId);
		if (msgSourceVo!=null) {
			logger.info("MsgSourceDao - selectByPrimaryKey: " + LF + msgSourceVo);
		}
		return msgSourceVo;
	}

	private List<MsgSourceVo> selectByFromAddrId(long addrId) {
		List<MsgSourceVo> list = msgSourceDao.getByFromAddrId(addrId);
		for (MsgSourceVo vo : list) {
			logger.info("MsgSourceDao - selectByFromAddrId: " + LF + vo);
		}
		return list;
	}

	private int update(MsgSourceVo msgSourceVo) {
		msgSourceVo.setDescription(TestDesc);
		msgSourceVo.setExcludeIdToken(true);
		msgSourceVo.setArchiveInd(true);
		msgSourceVo.setSaveMsgStream(true);
		int rows = msgSourceDao.update(msgSourceVo);
		logger.info("MsgSourceDao - update: rows updated " + rows);
		return rows;
	}

	private MsgSourceVo insert(String msgSourceId) {
		MsgSourceVo vo = msgSourceDao.getByPrimaryKey(msgSourceId);;
		if (vo!=null) {
			vo.setMsgSourceId(vo.getMsgSourceId()+"_v2");
			int rows = msgSourceDao.insert(vo);
			logger.info("MsgSourceDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(vo.getMsgSourceId());
		}
		return null;
	}

	private int deleteByPrimaryKey(MsgSourceVo vo) {
		int rowsDeleted = msgSourceDao.deleteByPrimaryKey(vo.getMsgSourceId());
		logger.info("MsgSourceDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}

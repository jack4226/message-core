package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.RfcFieldsDao;
import ltj.message.vo.inbox.RfcFieldsVo;

public class RfcFieldsTest extends DaoTestBase {
	@Resource
	private RfcFieldsDao rfcFieldsDao;
	
	static long testMsgId = 2L;
	static String testRfcType = "RFC822";
	
	@Test
	public void testRfcFields() {
		List<RfcFieldsVo> list = selectByMsgId(testMsgId);
		if (list.isEmpty()) {
			list = rfcFieldsDao.getRandomRecord();
			assertTrue(list.size() > 0);
			testMsgId = list.get(0).getMsgId();
			testRfcType = list.get(0).getRfcType();
		}
		assertTrue(rfcFieldsDao.getRandomRecord().size() > 0);
		RfcFieldsVo vo = selectByPrimaryKey(testMsgId, testRfcType);
		assertNotNull(vo);
		RfcFieldsVo vo2 = insert(vo.getMsgId());
		assertNotNull(vo2);
		vo.setRfcType(vo2.getRfcType());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = deleteByPrimaryKey(vo2.getMsgId(), vo2.getRfcType());
		assertEquals(rowsDeleted, 1);
	}
	
	private List<RfcFieldsVo> selectByMsgId(long msgId) {
		List<RfcFieldsVo> actions = rfcFieldsDao.getByMsgId(msgId);
		for (Iterator<RfcFieldsVo> it=actions.iterator(); it.hasNext();) {
			RfcFieldsVo rfcFieldsVo = it.next();
			logger.info("RfcFieldsDao - selectByMsgId: "+LF+rfcFieldsVo);
		}
		return actions;
	}
	
	private RfcFieldsVo selectByPrimaryKey(long msgId, String rfcType) {
		RfcFieldsVo rfcFieldsVo = (RfcFieldsVo)rfcFieldsDao.getByPrimaryKey(msgId,"RFC822");
		logger.info("RfcFieldsDao - selectByPrimaryKey: "+LF+rfcFieldsVo);
		return rfcFieldsVo;
	}
	
	private int update(RfcFieldsVo rfcFieldsVo) {
		rfcFieldsVo.setDlvrStatus(rfcFieldsVo.getDlvrStatus()+".");
		int rows = rfcFieldsDao.update(rfcFieldsVo);
		logger.info("RfcFieldsDao - update: "+LF+rfcFieldsVo);
		return rows;
	}
	
	private int deleteByPrimaryKey(long msgId, String rfcType) {
		int rowsDeleted = rfcFieldsDao.deleteByPrimaryKey(msgId, rfcType);
		logger.info("RfcFieldsDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private RfcFieldsVo insert(long msgId) {
		List<RfcFieldsVo> list = (List<RfcFieldsVo>)rfcFieldsDao.getByMsgId(msgId);
		if (list.size()>0) {
			RfcFieldsVo rfcFieldsVo = list.get(list.size()-1);
			rfcFieldsVo.setRfcType("DSNTEXT");
			int rows = rfcFieldsDao.insert(rfcFieldsVo);
			logger.info("RfcFieldsDao - insert: rows inserted "+rows+LF+rfcFieldsVo);
			return rfcFieldsVo;
		}
		return null;
	}
}

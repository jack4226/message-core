package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgRfcFieldDao;
import ltj.message.vo.inbox.MsgRfcFieldVo;

public class MsgRfcFieldTest extends DaoTestBase {
	@Resource
	private MsgRfcFieldDao msgRfcFieldDao;
	
	static long testMsgId = 2L;
	static String testRfcType = "RFC822";
	
	@Test
	public void testRfcFields() {
		List<MsgRfcFieldVo> list = selectByMsgId(testMsgId);
		if (list.isEmpty()) {
			list = msgRfcFieldDao.getRandomRecord();
			assertTrue(list.size() > 0);
			testMsgId = list.get(0).getMsgId();
			testRfcType = list.get(0).getRfcType();
		}
		assertTrue(msgRfcFieldDao.getRandomRecord().size() > 0);
		MsgRfcFieldVo vo = selectByPrimaryKey(testMsgId, testRfcType);
		assertNotNull(vo);
		MsgRfcFieldVo vo2 = insert(vo.getMsgId());
		assertNotNull(vo2);
		vo.setRfcType(vo2.getRfcType());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(1, rowsUpdated);
		int rowsDeleted = deleteByPrimaryKey(vo2.getMsgId(), vo2.getRfcType());
		assertEquals(1, rowsDeleted);
	}
	
	private List<MsgRfcFieldVo> selectByMsgId(long msgId) {
		List<MsgRfcFieldVo> actions = msgRfcFieldDao.getByMsgId(msgId);
		for (Iterator<MsgRfcFieldVo> it=actions.iterator(); it.hasNext();) {
			MsgRfcFieldVo msgRfcFieldVo = it.next();
			logger.info("MsgRfcFieldDao - selectByMsgId: "+LF+msgRfcFieldVo);
		}
		return actions;
	}
	
	private MsgRfcFieldVo selectByPrimaryKey(long msgId, String rfcType) {
		MsgRfcFieldVo msgRfcFieldVo = (MsgRfcFieldVo)msgRfcFieldDao.getByPrimaryKey(msgId,"RFC822");
		logger.info("MsgRfcFieldDao - selectByPrimaryKey: "+LF+msgRfcFieldVo);
		return msgRfcFieldVo;
	}
	
	private int update(MsgRfcFieldVo msgRfcFieldVo) {
		msgRfcFieldVo.setDlvrStatus(msgRfcFieldVo.getDlvrStatus()+".");
		int rows = msgRfcFieldDao.update(msgRfcFieldVo);
		logger.info("MsgRfcFieldDao - update: "+LF+msgRfcFieldVo);
		return rows;
	}
	
	private int deleteByPrimaryKey(long msgId, String rfcType) {
		int rowsDeleted = msgRfcFieldDao.deleteByPrimaryKey(msgId, rfcType);
		logger.info("MsgRfcFieldDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgRfcFieldVo insert(long msgId) {
		List<MsgRfcFieldVo> list = (List<MsgRfcFieldVo>)msgRfcFieldDao.getByMsgId(msgId);
		if (list.size()>0) {
			MsgRfcFieldVo msgRfcFieldVo = list.get(list.size()-1);
			msgRfcFieldVo.setRfcType("DSNTEXT");
			int rows = msgRfcFieldDao.insert(msgRfcFieldVo);
			logger.info("MsgRfcFieldDao - insert: rows inserted "+rows+LF+msgRfcFieldVo);
			return msgRfcFieldVo;
		}
		return null;
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgHeaderDao;
import ltj.message.vo.inbox.MsgHeaderVo;

public class MsgHeaderTest extends DaoTestBase {
	@Resource
	private MsgHeaderDao msgHeaderDao;
	
	static long testMsgId = 2L;

	@Test
	public void testMsgHeaders() {
		try {
			List<MsgHeaderVo> lst1 = selectByMsgId(testMsgId);
			if (lst1.isEmpty()) {
				lst1 = msgHeaderDao.getRandomRecord();
				assertTrue(lst1.size() > 0);
				testMsgId = lst1.get(0).getMsgId();
			}
			assertTrue(msgHeaderDao.getRandomRecord().size() > 0);
			MsgHeaderVo vo1 = lst1.get(0);
			MsgHeaderVo vo0 = selectByPrimaryKey(vo1.getMsgId(), vo1.getHeaderSeq());
			assertNotNull(vo0);
			assertTrue(vo1.equalsTo(vo0));
			MsgHeaderVo vo2 = insert(testMsgId);
			assertNotNull(vo2);
			vo1.setHeaderSeq(vo2.getHeaderSeq());
			assertTrue(vo1.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private List<MsgHeaderVo> selectByMsgId(long msgId) {
		List<MsgHeaderVo> actions = msgHeaderDao.getByMsgId(msgId);
		for (Iterator<MsgHeaderVo> it=actions.iterator(); it.hasNext();) {
			MsgHeaderVo msgHeaderVo = it.next();
			logger.info("MsgHeaderDao - selectByMsgId: "+LF+msgHeaderVo);
		}
		return actions;
	}
	
	private MsgHeaderVo selectByPrimaryKey(long msgId, int seq) {
		MsgHeaderVo msgHeaderVo = (MsgHeaderVo)msgHeaderDao.getByPrimaryKey(msgId,seq);
		logger.info("MsgHeaderDao - selectByPrimaryKey: "+LF+msgHeaderVo);
		return msgHeaderVo;
	}
	
	private int update(MsgHeaderVo msgHeaderVo) {
		msgHeaderVo.setHeaderValue(msgHeaderVo.getHeaderValue()+".");
		int rows = msgHeaderDao.update(msgHeaderVo);
		logger.info("MsgHeaderDao - update: rows updated "+rows);
		return rows;
	}
	
	private int deleteByPrimaryKey(MsgHeaderVo vo) {
		int rowsDeleted = msgHeaderDao.deleteByPrimaryKey(vo.getMsgId(),vo.getHeaderSeq());
		logger.info("MsgHeaderDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgHeaderVo insert(long msgId) {
		List<MsgHeaderVo> list = (List<MsgHeaderVo>)msgHeaderDao.getByMsgId(msgId);
		if (list.size()>0) {
			MsgHeaderVo msgHeaderVo = list.get(list.size()-1);
			msgHeaderVo.setHeaderSeq(msgHeaderVo.getHeaderSeq()+1);
			int rows = msgHeaderDao.insert(msgHeaderVo);
			logger.info("MsgHeaderDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(msgHeaderVo.getMsgId(), msgHeaderVo.getHeaderSeq());
		}
		return null;
	}
}

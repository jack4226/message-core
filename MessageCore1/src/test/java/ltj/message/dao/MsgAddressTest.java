package ltj.message.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.AddressType;
import ltj.message.constant.Constants;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgAddressDao;
import ltj.message.vo.inbox.MsgAddressVo;

public class MsgAddressTest extends DaoTestBase {
	@Resource
	private MsgAddressDao msgAddressDao;
	
	static long testMsgId = 2L;
	static String testAddrType = AddressType.FROM_ADDR.value();

	@Test
	public void insertUpdateDelete() {
		try {
			assertTrue(msgAddressDao.getRandomRecord().size() > 0);
			List<MsgAddressVo> list = selectByMsgId(testMsgId);
			if (list.isEmpty()) {
				list = msgAddressDao.getRandomRecord();
				assertTrue(list.size() > 0);
				testMsgId = list.get(0).getMsgId();
				testAddrType = list.get(0).getAddrType();
			}
			else {
				testAddrType = list.get(0).getAddrType();
			}
			List<MsgAddressVo> list2 = selectByMsgIdAndType(testMsgId, testAddrType);
			assertTrue(list2.size() > 0);
			MsgAddressVo vo = insert(testMsgId, testAddrType);
			assertNotNull(vo);
			List<MsgAddressVo> list3 = selectByMsgIdAndType(testMsgId, testAddrType);
			assertTrue(list3.size() == (list2.size() + 1));
			MsgAddressVo vo2 = selectByPrimaryKey(vo);
			assertNotNull(vo2);
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			deleteLast(testMsgId, testAddrType);
			e.printStackTrace();
		}
	}
	
	private List<MsgAddressVo> selectByMsgId(long msgId) {
		List<MsgAddressVo> actions = msgAddressDao.getByMsgId(msgId);
		for (Iterator<MsgAddressVo> it=actions.iterator(); it.hasNext();) {
			MsgAddressVo msgAddressVo = it.next();
			logger.info("MsgAddressDao - selectByMsgId: "+LF+msgAddressVo);
		}
		return actions;
	}
	
	private MsgAddressVo selectByPrimaryKey(MsgAddressVo vo) {
		MsgAddressVo msgAddressVo = msgAddressDao.getByPrimaryKey(vo.getMsgId(), vo.getAddrType(), vo.getAddrSeq());
		logger.info("MsgAddressDao - selectByPrimaryKey: "+LF+msgAddressVo);
		return vo;
	}
	
	private List<MsgAddressVo> selectByMsgIdAndType(long msgId, String type) {
		List<MsgAddressVo> actions  = msgAddressDao.getByMsgIdAndType(msgId, type);
		for (Iterator<MsgAddressVo> it=actions.iterator(); it.hasNext();) {
			MsgAddressVo msgAddressVo = it.next();
			logger.info("MsgAddressDao - selectByMsgIdAndType: "+LF+msgAddressVo);
		}
		return actions;
	}
	
	private int update(MsgAddressVo vo) {
		MsgAddressVo msgAddressVo = msgAddressDao.getByPrimaryKey(vo.getMsgId(), vo.getAddrType(), vo.getAddrSeq());
		int rowsUpdated = 0;
		if (msgAddressVo!=null) {
			msgAddressVo.setAddrValue("more."+msgAddressVo.getAddrValue());
			msgAddressVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			msgAddressVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			rowsUpdated = msgAddressDao.update(msgAddressVo);
			logger.info("MsgAddressDao - update: "+LF+msgAddressVo);
		}
		return rowsUpdated;
	}
	
	private int deleteByPrimaryKey(MsgAddressVo vo) {
		int rowsDeleted = msgAddressDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAddrType(), vo.getAddrSeq());
		logger.info("MsgAddressDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgAddressVo insert(long msgId, String addrType) {
		List<MsgAddressVo> list = msgAddressDao.getByMsgIdAndType(msgId, addrType);
		if (list.size()>0) {
			MsgAddressVo msgAddressVo = list.get(list.size()-1);
			msgAddressVo.setAddrSeq(msgAddressVo.getAddrSeq()+1);
			msgAddressDao.insert(msgAddressVo);
			logger.info("MsgAddressDao - insert: "+LF+msgAddressVo);
			return msgAddressVo;
		}
		return null;
	}

	private void deleteLast(long msgId, String addrType) {
		List<MsgAddressVo> list = msgAddressDao.getByMsgIdAndType(msgId, addrType);
		if (list.size() > 1) {
			MsgAddressVo vo = list.get(list.size()-1);
			int rows = msgAddressDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAddrType(), vo.getAddrSeq());
			logger.info("MsgAddressDao - deleteLast: "+rows);
		}
	}
}

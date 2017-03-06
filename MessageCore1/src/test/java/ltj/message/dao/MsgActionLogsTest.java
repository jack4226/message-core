package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.data.preload.RuleActionDetailEnum;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgActionLogsDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.vo.inbox.MsgActionLogsVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.message.vo.inbox.SearchFieldsVo;

public class MsgActionLogsTest extends DaoTestBase {
	@Resource
	private MsgActionLogsDao msgActionLogsDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	
	final static String testActionId = RuleActionDetailEnum.CLOSE.name();
	
	@Test
	@Rollback(value=false)
	public void testMsgActionDetail() {
		try {
			List<MsgActionLogsVo> list = msgActionLogsDao.getRandomRecord();
			if (list.isEmpty()) {
				assertEquals(1, insert());
				list = msgActionLogsDao.getRandomRecord();
			}
			assertTrue(list.size() > 0);
			long msgId = list.get(0).getMsgId();
			Long msgRefId = list.get(0).getMsgRefId();
			List<MsgActionLogsVo> list2 = selectByMsgId(msgId);
			assertFalse(list2.isEmpty());
			MsgActionLogsVo vo = selectByPrimaryKey(msgId, msgRefId);
			assertNotNull(vo);
			int rowsInserted = insertMultiple();
			assertTrue(rowsInserted >= 0);
			list = msgActionLogsDao.getRandomRecord();
			int idx = new Random().nextInt(list.size());
			MsgActionLogsVo vo2 = list.get(idx);
			int rows = update(vo2);
			assertEquals(rows, 1);
			rows = deleteByPrimaryKey(vo2);
			assertEquals(rows, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private MsgActionLogsVo selectByPrimaryKey(long rowId, Long msgRefId) {
		MsgActionLogsVo vo = msgActionLogsDao.getByPrimaryKey(rowId, msgRefId);
		if (vo!=null) {
			logger.info("MsgActionLogsDao - selectByPrimaryKey: "+LF+vo);
		}
		return vo;
	}
	
	private List<MsgActionLogsVo> selectByMsgId(long msgId) {
		List<MsgActionLogsVo> vos = msgActionLogsDao.getByMsgId(msgId);
		if (!vos.isEmpty()) {
			logger.info("MsgActionLogsDao - selectByMsgId: "+vos.get(0));
		}
		return vos;
	}
	
	private int update(MsgActionLogsVo msgActionLogsVo) {
		msgActionLogsVo.setActionBo("SUSPEND");
		int rows = msgActionLogsDao.update(msgActionLogsVo);
		logger.info("MsgActionLogsDao - update: "+rows);
		return rows;
	}
	
	private int insert() {
		MsgInboxVo inboxVo = msgInboxDao.getRandomRecord();
		MsgActionLogsVo vo = new MsgActionLogsVo();
		vo.setMsgId(inboxVo.getMsgId());
		vo.setMsgRefId(inboxVo.getMsgRefId() == null ? inboxVo.getMsgId() : inboxVo.getMsgRefId());
		vo.setActionBo(testActionId);
		vo.setLeadMsgId(inboxVo.getLeadMsgId());
		int rowsInserted = msgActionLogsDao.insert(vo);
		return rowsInserted;
	}
	
	private int insertMultiple() {
		SearchFieldsVo srchvo = new SearchFieldsVo();
		srchvo.setPageSize(100);
		srchvo.setMsgType(null);
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(srchvo);
		assertFalse(list.isEmpty());
		int rowsInserted = 0;
		for (MsgInboxWebVo webvo : list) {
			if (webvo.getMsgRefId() == null) {
				continue;
			}
			MsgActionLogsVo exist = msgActionLogsDao.getByPrimaryKey(webvo.getMsgId(), webvo.getMsgRefId());
			if (exist == null) {
				MsgActionLogsVo vo = new MsgActionLogsVo();
				vo.setMsgId(webvo.getMsgId());
				vo.setMsgRefId(webvo.getMsgRefId());
				vo.setActionBo(testActionId);
				vo.setLeadMsgId(webvo.getLeadMsgId());
				rowsInserted += msgActionLogsDao.insert(vo);
			}
		}
		return rowsInserted;
	}
	

	private int deleteByPrimaryKey(MsgActionLogsVo vo) {
		int rows = msgActionLogsDao.deleteByPrimaryKey(vo.getMsgId(), vo.getMsgRefId());
		logger.info("MsgActionLogsDao - deleteByPrimaryKey: Rows Deleted: "+rows);
		return rows;
	}
}

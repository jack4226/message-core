package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.data.preload.RuleActionDetailEnum;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgActionLogDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.vo.PagingVo;
import ltj.message.vo.inbox.MsgActionLogVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.message.vo.inbox.SearchFieldsVo;

public class MsgActionLogTest extends DaoTestBase {
	@Resource
	private MsgActionLogDao msgActionLogDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	
	final static String testActionId = RuleActionDetailEnum.CLOSE.name();
	
	@Test
	@Rollback(value=false)
	public void testMsgActionDetail() {
		try {
			List<MsgActionLogVo> list = msgActionLogDao.getRandomRecord();
			if (list.isEmpty()) {
				assertEquals(1, insert());
				list = msgActionLogDao.getRandomRecord();
			}
			assertTrue(list.size() > 0);
			long msgId = list.get(0).getMsgId();
			Long msgRefId = list.get(0).getMsgRefId();
			List<MsgActionLogVo> list2 = selectByMsgId(msgId);
			assertFalse(list2.isEmpty());
			MsgActionLogVo vo = selectByPrimaryKey(msgId, msgRefId);
			assertNotNull(vo);
			int rowsInserted = insertMultiple();
			assertTrue(rowsInserted >= 0);
			list = msgActionLogDao.getRandomRecord();
			int idx = new Random().nextInt(list.size());
			MsgActionLogVo vo2 = list.get(idx);
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
	
	private MsgActionLogVo selectByPrimaryKey(long rowId, Long msgRefId) {
		MsgActionLogVo vo = msgActionLogDao.getByPrimaryKey(rowId, msgRefId);
		if (vo!=null) {
			logger.info("MsgActionLogDao - selectByPrimaryKey: "+LF+vo);
		}
		return vo;
	}
	
	private List<MsgActionLogVo> selectByMsgId(long msgId) {
		List<MsgActionLogVo> vos = msgActionLogDao.getByMsgId(msgId);
		if (!vos.isEmpty()) {
			logger.info("MsgActionLogDao - selectByMsgId: "+vos.get(0));
		}
		return vos;
	}
	
	private int update(MsgActionLogVo msgActionLogVo) {
		msgActionLogVo.setActionBo("SUSPEND");
		int rows = msgActionLogDao.update(msgActionLogVo);
		logger.info("MsgActionLogDao - update: "+rows);
		return rows;
	}
	
	private int insert() {
		MsgInboxVo inboxVo = msgInboxDao.getRandomRecord();
		MsgActionLogVo vo = new MsgActionLogVo();
		vo.setMsgId(inboxVo.getMsgId());
		vo.setMsgRefId(inboxVo.getMsgRefId() == null ? inboxVo.getMsgId() : inboxVo.getMsgRefId());
		vo.setActionBo(testActionId);
		vo.setLeadMsgId(inboxVo.getLeadMsgId());
		int rowsInserted = msgActionLogDao.insert(vo);
		return rowsInserted;
	}
	
	private int insertMultiple() {
		SearchFieldsVo srchvo = new SearchFieldsVo(new PagingVo());
		srchvo.getPagingVo().setPageSize(100);
		srchvo.setFolderType(null);
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(srchvo);
		assertFalse(list.isEmpty());
		int rowsInserted = 0;
		for (MsgInboxWebVo webvo : list) {
			if (webvo.getMsgRefId() == null) {
				continue;
			}
			MsgActionLogVo exist = msgActionLogDao.getByPrimaryKey(webvo.getMsgId(), webvo.getMsgRefId());
			if (exist == null) {
				MsgActionLogVo vo = new MsgActionLogVo();
				vo.setMsgId(webvo.getMsgId());
				vo.setMsgRefId(webvo.getMsgRefId());
				vo.setActionBo(testActionId);
				vo.setLeadMsgId(webvo.getLeadMsgId());
				rowsInserted += msgActionLogDao.insert(vo);
			}
		}
		return rowsInserted;
	}
	

	private int deleteByPrimaryKey(MsgActionLogVo vo) {
		int rows = msgActionLogDao.deleteByPrimaryKey(vo.getMsgId(), vo.getMsgRefId());
		logger.info("MsgActionLogDao - deleteByPrimaryKey: Rows Deleted: "+rows);
		return rows;
	}
}

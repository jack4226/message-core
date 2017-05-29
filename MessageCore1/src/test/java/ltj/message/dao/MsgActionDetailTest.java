package ltj.message.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.data.preload.RuleActionDetailEnum;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.action.MsgActionDetailDao;
import ltj.message.vo.action.MsgActionDetailVo;

public class MsgActionDetailTest extends DaoTestBase {
	@Resource
	private MsgActionDetailDao msgActionDetailDao;
	
	final static String testActionId = RuleActionDetailEnum.CLOSE.name();
	
	@Test
	public void testMsgActionDetail() {
		try {
			MsgActionDetailVo vo0 = selectByActionId(RuleActionDetailEnum.SUSPEND.name());
			assertNotNull(vo0);
			vo0 = selectByActionId(RuleActionDetailEnum.SAVE.name());
			assertNotNull(vo0);
			vo0 = selectByActionId(testActionId);
			assertNotNull(vo0);
			MsgActionDetailVo vo = selectByPrimaryKey(vo0.getRowId());
			assertNotNull(vo);
			MsgActionDetailVo vo2 = insert(vo.getActionId());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setActionId(vo2.getActionId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(rows, 1);
			rows = deleteByActionId(vo2);
			assertEquals(rows, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private MsgActionDetailVo selectByPrimaryKey(int rowId) {
		MsgActionDetailVo vo = msgActionDetailDao.getByPrimaryKey(rowId);
		if (vo!=null) {
			logger.info("MsgActionDetailDao - selectByPrimaryKey: "+LF+vo);
		}
		return vo;
	}
	
	private MsgActionDetailVo selectByActionId(String actionId) {
		MsgActionDetailVo vo = msgActionDetailDao.getByActionId(actionId);
		if (vo!=null) {
			logger.info("MsgActionDetailDao - selectByActionId: "+vo);
		}
		return vo;
	}
	
	private int update(MsgActionDetailVo msgActionDetailVo) {
		msgActionDetailVo.setDescription("Close the Email");
		int rows = msgActionDetailDao.update(msgActionDetailVo);
		logger.info("MsgActionDetailDao - update: "+rows);
		return rows;
	}
	
	private MsgActionDetailVo insert(String actionId) {
		MsgActionDetailVo msgActionDetailVo = msgActionDetailDao.getByActionId(actionId);
		if (msgActionDetailVo!=null) {
			msgActionDetailVo.setActionId(msgActionDetailVo.getActionId()+"_v2");
			int rows = msgActionDetailDao.insert(msgActionDetailVo);
			logger.info("MsgActionDetailDao - insert: rows inserted "+rows);
			return selectByActionId(msgActionDetailVo.getActionId());
		}
		return null;
	}
	
	private int deleteByActionId(MsgActionDetailVo vo) {
		int rows = msgActionDetailDao.deleteByActionId(vo.getActionId());
		logger.info("MsgActionDetailDao - deleteByPrimaryKey: Rows Deleted: "+rows);
		return rows;
	}
}

package ltj.message.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.data.preload.RuleNameEnum;
import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.action.MsgActionDao;
import ltj.message.vo.action.MsgActionVo;

public class MsgActionTest extends DaoTestBase {
	@Resource
	private MsgActionDao msgActionDao;
	
	final static String testRuleName = RuleNameEnum.HARD_BOUNCE.name();
	
	@Test
	public void testSelects() {
		List<MsgActionVo> list = selectByRuleName(testRuleName);
		assertTrue(list.size()>0);
		list = selectByBestMatch();
		assertTrue(list.size()>0);
		MsgActionVo vo = selectByUniqueKey(testRuleName, 1, null);
		assertNotNull(vo);
		int rowsDeleted = deleteByRuleName("test");
		assertEquals(rowsDeleted, 0);
	}

	@Test
	@Rollback(value=true)
	public void insertSelectDelete() {
		MsgActionVo msgActionVo = insert();
		assertNotNull(msgActionVo);
		assertEquals(RuleNameEnum.CC_USER.name(), msgActionVo.getRuleName());
		MsgActionVo msgActionVo2 = select(msgActionVo);
		assertNotNull(msgActionVo2);
		assertTrue(msgActionVo.equalsTo(msgActionVo2));
		int rowsUpdated = update(msgActionVo);
		assertEquals(1, rowsUpdated);
		int rowsDeleted = delete(msgActionVo);
		assertEquals(1, rowsDeleted);
	}
	
	private List<MsgActionVo> selectByRuleName(String ruleName) {
		List<MsgActionVo> actions = msgActionDao.getByRuleName(ruleName);
		for (Iterator<MsgActionVo> it=actions.iterator(); it.hasNext();) {
			MsgActionVo msgActionVo = it.next();
			logger.info("MsgActionDao - selectByRuleName: "+LF+msgActionVo);
		}
		return actions;
	}
	
	private MsgActionVo selectByUniqueKey(String ruleName, int actionSeq, String clientId) {
		MsgActionVo msgActionVo = msgActionDao.getMostCurrent(ruleName, actionSeq, clientId);
		logger.info("MsgActionDao - selectByUniqueKey: "+LF+msgActionVo);
		return msgActionVo;
	}
	
	private List<MsgActionVo> selectByBestMatch() {
		List<MsgActionVo> list = msgActionDao.getByBestMatch(RuleNameEnum.GENERIC.name(), null, "JBatchCorp");
		for (int i=0; i<list.size(); i++) {
			MsgActionVo msgActionVo = list.get(i);
			logger.info("MsgActionDao - selectByBestMatch: "+LF+msgActionVo);
		}
		return list;
	}

	private MsgActionVo select(MsgActionVo msgActionVo) {
		MsgActionVo msgActionVo2 = msgActionDao.getByUniqueKey(msgActionVo.getRuleName(),
				msgActionVo.getActionSeq(), msgActionVo.getStartTime(), msgActionVo.getClientId());
		logger.info("MsgActionDao - select: "+msgActionVo2);
		return msgActionVo2;
	}
	
	private int update(MsgActionVo msgActionVo) {
		int rowsUpdated = 0;
		if (msgActionVo!=null) {
			if (!StatusId.ACTIVE.value().equals(msgActionVo.getStatusId())) {
				msgActionVo.setStatusId(StatusId.ACTIVE.value());
			}
			rowsUpdated = msgActionDao.update(msgActionVo);
			logger.info("MsgActionDao - update: Rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int deleteByRuleName(String ruleName) {
		int rowsDeleted = msgActionDao.deleteByRuleName(ruleName);
		logger.info("MsgActionDao - deleteByRuleName: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private int delete(MsgActionVo msgActionVo) {
		int rowsDeleted = msgActionDao.deleteByUniqueKey(msgActionVo.getRuleName(),
				msgActionVo.getActionSeq(), msgActionVo.getStartTime(), msgActionVo.getClientId());
		logger.info("MsgActionDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private MsgActionVo insert() {
		List<MsgActionVo> list = msgActionDao.getByRuleName(RuleNameEnum.CC_USER.name());
		if (list.size()>0) {
			MsgActionVo msgActionVo = list.get(list.size()-1);
			msgActionVo.setActionSeq((msgActionVo.getActionSeq()+1));
			msgActionDao.insert(msgActionVo);
			logger.info("MsgActionDao - insert: "+msgActionVo);
			return msgActionVo;
		}
		return null;
	}
}

package ltj.message.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.constant.RuleNameType;
import ltj.message.constant.StatusIdCode;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.action.MsgActionDao;
import ltj.message.vo.action.MsgActionVo;

public class MsgActionTest extends DaoTestBase {
	// this instance will be dependency injected by name
	@Resource
	private MsgActionDao msgActionDao;
	final String testRuleName = RuleNameType.HARD_BOUNCE.name();
	
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
		assertEquals(RuleNameType.CC_USER.name(), msgActionVo.getRuleName());
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
			System.out.println("MsgActionDao - selectByRuleName: "+LF+msgActionVo);
		}
		return actions;
	}
	
	private MsgActionVo selectByUniqueKey(String ruleName, int actionSeq, String clientId) {
		MsgActionVo msgActionVo = msgActionDao.getMostCurrent(ruleName, actionSeq, clientId);
		System.out.println("MsgActionDao - selectByUniqueKey: "+LF+msgActionVo);
		return msgActionVo;
	}
	
	private List<MsgActionVo> selectByBestMatch() {
		List<MsgActionVo> list = msgActionDao.getByBestMatch(RuleNameType.GENERIC.name(), null, "JBatchCorp");
		for (int i=0; i<list.size(); i++) {
			MsgActionVo msgActionVo = list.get(i);
			System.out.println("MsgActionDao - selectByBestMatch: "+LF+msgActionVo);
		}
		return list;
	}

	private MsgActionVo select(MsgActionVo msgActionVo) {
		MsgActionVo msgActionVo2 = msgActionDao.getByUniqueKey(msgActionVo.getRuleName(),
				msgActionVo.getActionSeq(), msgActionVo.getStartTime(), msgActionVo.getClientId());
		System.out.println("MsgActionDao - select: "+msgActionVo2);
		return msgActionVo2;
	}
	
	private int update(MsgActionVo msgActionVo) {
		int rowsUpdated = 0;
		if (msgActionVo!=null) {
			if (!StatusIdCode.ACTIVE.equals(msgActionVo.getStatusId())) {
				msgActionVo.setStatusId(StatusIdCode.ACTIVE);
			}
			rowsUpdated = msgActionDao.update(msgActionVo);
			System.out.println("MsgActionDao - update: Rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int deleteByRuleName(String ruleName) {
		int rowsDeleted = msgActionDao.deleteByRuleName(ruleName);
		System.out.println("MsgActionDao - deleteByRuleName: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private int delete(MsgActionVo msgActionVo) {
		int rowsDeleted = msgActionDao.deleteByUniqueKey(msgActionVo.getRuleName(),
				msgActionVo.getActionSeq(), msgActionVo.getStartTime(), msgActionVo.getClientId());
		System.out.println("MsgActionDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private MsgActionVo insert() {
		List<MsgActionVo> list = msgActionDao.getByRuleName(RuleNameType.CC_USER.name());
		if (list.size()>0) {
			MsgActionVo msgActionVo = list.get(list.size()-1);
			msgActionVo.setActionSeq((msgActionVo.getActionSeq()+1));
			msgActionDao.insert(msgActionVo);
			System.out.println("MsgActionDao - insert: "+msgActionVo);
			return msgActionVo;
		}
		return null;
	}
}

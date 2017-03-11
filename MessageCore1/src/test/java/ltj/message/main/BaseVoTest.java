package ltj.message.main;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import ltj.message.constant.MsgDirection;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.vo.BaseVo.ChangeLog;
import ltj.message.vo.inbox.MsgInboxVo;

public class BaseVoTest extends DaoTestBase {
	@Resource
	private MsgInboxDao msgInboxDao;

	private static MsgInboxVo testMsgInboxVo = null;
	
	@Before
	public void setup() {
		testMsgInboxVo = msgInboxDao.getRandomRecord();
		assertNotNull(testMsgInboxVo);
	}

	@Test
	public void testEqualsTo() {
		assertNotNull(testMsgInboxVo);
		MsgInboxVo vo = msgInboxDao.getByPrimaryKey(testMsgInboxVo.getMsgId());
		assertNotNull(vo);
		assertTrue(vo.equalsTo(testMsgInboxVo));
		vo.setMsgSubject(vo.getMsgSubject() + " changed ");
		assertFalse(vo.equalsTo(testMsgInboxVo));
	}
	
	@Test
	public void testToString() {
		assertNotNull(testMsgInboxVo);
		MsgInboxVo vo = msgInboxDao.getByPrimaryKey(testMsgInboxVo.getMsgId());
		assertNotNull(vo);
		String tostr = vo.toString();
		logger.info(LF + tostr);
		assertTrue(StringUtils.contains(tostr, "getMsgSubject=" + testMsgInboxVo.getMsgSubject()));
		assertTrue(StringUtils.contains(tostr, "getMsgBody=" + testMsgInboxVo.getMsgBody()));
		if (StringUtils.isNotBlank(testMsgInboxVo.getRuleName())) {
			assertTrue(StringUtils.contains(tostr, "getRuleName=" + testMsgInboxVo.getRuleName()));
		}
	}
	
	@Test
	public void testChangeLogs() {
		assertNotNull(testMsgInboxVo);
		MsgInboxVo vo = msgInboxDao.getByPrimaryKey(testMsgInboxVo.getMsgId());
		assertNotNull(vo);
		vo.setMsgSubject(vo.getMsgSubject() + "_v2");
		if (MsgDirection.RECEIVED.value().equals(vo.getMsgDirection())) {
			vo.setMsgDirection(MsgDirection.SENT.value());
		}
		else {
			vo.setMsgDirection(MsgDirection.RECEIVED.value());
		}
		vo.equalsTo(testMsgInboxVo); // to trigger the population of change logs
		
		String changes = vo.listChanges();
		logger.info("Changes: " + LF + changes);
		
		List<ChangeLog> changeList = vo.getLogList();
		assertEquals(2, changeList.size());
		
		List<String> nameList = new ArrayList<>();
		for (ChangeLog log : changeList) {
			nameList.add(log.getFieldName());
		}
		assertTrue(nameList.contains("MsgSubject"));
		assertTrue(nameList.contains("MsgDirection"));
		
		for (ChangeLog log : changeList) {
			if ("MsgSubject".equals(log.getFieldName())) {
				assertEquals(testMsgInboxVo.getMsgSubject() + "_v2", vo.getMsgSubject());
			}
		}
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.constant.MailCodeType;
import ltj.message.constant.RuleNameType;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgClickCountsDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgUnreadCountDao;
import ltj.message.dao.outbox.MsgSequenceDao;
import ltj.message.util.PrintUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.inbox.MsgClickCountsVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.message.vo.inbox.SearchFieldsVo;

public class MsgInboxTest extends DaoTestBase {
	static final Logger logger = Logger.getLogger(MsgInboxTest.class);
	@Resource
	private MsgUnreadCountDao unreadCountDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	@Resource
	private MsgClickCountsDao msgClickCountsDao;
	@Resource
	private MsgSequenceDao msgSequenceDao;
	
	private static Long testMsgId = null; //2L;
	private static Long testFromAddrId = null; //1L;
	
	@Before
	public void setup() {
		if (testMsgId == null || testFromAddrId == null) {
			MsgInboxVo randomVo = msgInboxDao.getRandomRecord();
			assertNotNull(randomVo);
			testMsgId = randomVo.getMsgId();
			testFromAddrId = randomVo.getFromAddrId();
			assertNotNull(testFromAddrId);
		}
	}
	
	@Test
	@Rollback(value=true)
	public void testMessageInbox() {
		try {
			MsgInboxVo msgInboxVo = selectByMsgId(testMsgId);
			assertNotNull(msgInboxVo);
			List<MsgInboxVo> list = selectByFromAddrId(testFromAddrId);
			assertTrue(list.size() > 0);
			List<MsgInboxVo> list2 = selectByToAddrId(msgInboxVo.getToAddrId());
			assertTrue(list2.size() > 0);
			MsgInboxWebVo webvo = selectInboundGenericMsg();
			int unreadCountBefore = unreadCountDao.selectInboxUnreadCount();
			MsgInboxVo msgvo = insert(webvo.getMsgId());
			int unreadCountAfter = unreadCountDao.selectInboxUnreadCount();
			assertNotNull(msgvo);
			if (msgvo.getReadCount() == 0) {
				//assertEquals(unreadCountAfter, (unreadCountBefore + 1));
				assertTrue(unreadCountAfter >= (unreadCountBefore + 1));
			}
			else {
				assertTrue(unreadCountAfter == unreadCountBefore);
			}
			int rowsUpdated = update(msgvo.getMsgId());
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(msgvo.getMsgId());
			assertEquals(rowsDeleted, 1);
			int unreadCountAfterDelete = unreadCountDao.selectInboxUnreadCount();
			assertTrue(unreadCountAfterDelete >= unreadCountBefore);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	@Rollback(value=true)
	public void testBroadCastAndClickCounts() {
		try {
			MsgInboxWebVo webvo = selectBroadcastMsg();
			assertNotNull(webvo);
			int unreadCountBefore = unreadCountDao.selectInboxUnreadCount();
			MsgInboxVo msgvo = insert(webvo.getMsgId());
			int unreadCountAfter = unreadCountDao.selectInboxUnreadCount();
			//assertEquals(unreadCountAfter, unreadCountBefore);
			assertTrue(unreadCountAfter >= unreadCountBefore);
			assertNotNull(msgvo);
			MsgClickCountsVo ccvo = insertClickCount(msgvo);
			assertNotNull(ccvo);
			MsgClickCountsVo ccvo2 = selectClickCounts(ccvo.getMsgId());
			assertNotNull(ccvo2);
			ccvo2.setComplaintCount(ccvo.getComplaintCount());
			ccvo2.setUnsubscribeCount(ccvo.getUnsubscribeCount());
			assertTrue(ccvo.equalsTo(ccvo2));
			int rowsCCUpdated = updateClickCounts(ccvo2);
			assertTrue(rowsCCUpdated > 0);
			int rowsCCDeleted = deleteClickCounts(ccvo2.getMsgId());
			assertEquals(rowsCCDeleted, 1);
			int rowsDeleted = deleteByPrimaryKey(msgvo.getMsgId());
			assertEquals(rowsDeleted, 1);
			int unreadCountAfterDelete = unreadCountDao.selectInboxUnreadCount();
			//assertEquals(unreadCountAfterDelete, unreadCountBefore);
			assertTrue(unreadCountAfterDelete >= unreadCountBefore);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testWithPaging() {
		SearchFieldsVo vo = new SearchFieldsVo();
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(vo);
		assertFalse(list.isEmpty());
		Random r = new Random();
		
		// get first subject search word
		int idx = r.nextInt(list.size());
		MsgInboxWebVo mivo = list.get(idx);
		String word1 = StringUtil.getRandomWord(mivo.getMsgSubject());
		// get body text for record 1
		MsgInboxVo ivo1 = msgInboxDao.getByPrimaryKey(mivo.getMsgId());
		assertNotNull(ivo1);
		String body1 = ivo1.getMsgBody();
		logger.info("Search record 1:" + PrintUtil.prettyPrint(ivo1));
		
		// get second subject search word
		idx = r.nextInt(list.size());
		mivo = list.get(idx);
		String word2 = StringUtil.getRandomWord(mivo.getMsgSubject());
		// get body text for record 2
		MsgInboxVo ivo2 = msgInboxDao.getByPrimaryKey(mivo.getMsgId());
		assertNotNull(ivo2);
		String body2 = ivo2.getMsgBody();
		logger.info("Search record 2:" + PrintUtil.prettyPrint(ivo2));
		
		// build and set subject search string
		String subjStr = word1;
		if (StringUtils.isNotBlank(word2)) {
			subjStr += "  " + word2;
		}
		subjStr = subjStr.replaceAll("\\p{Punct}", ".");
		vo.setSubject(subjStr);
		
		// build and set search string for body 1
		String bodyStr = "";
		if (StringUtils.isNoneBlank(body1)) {
			List<String> words = StringUtil.getRandomWords(body1);
			if (!words.isEmpty()) {
				for (int i = 0; i < words.size(); i++) {
					bodyStr += " " + StringUtils.trim(words.get(i));
				}
				bodyStr = bodyStr.replaceAll("\\p{Punct}", ".");
				if (StringUtils.isNotBlank(bodyStr)) {
					vo.setBody(bodyStr);
				}
			}
		}
		list = msgInboxDao.getListForWeb(vo);
		assertFalse(list.isEmpty());
		for (MsgInboxWebVo mwvo : list) {
			logger.info("Search Subj/Body1: " + subjStr + "/" + bodyStr + ", Subject: " + mwvo.getMsgSubject());
			assertTrue(StringUtils.containsIgnoreCase(mwvo.getMsgSubject(), word1)
					|| StringUtils.containsIgnoreCase(mwvo.getMsgSubject(), word2));
		}
		
		if (StringUtils.isNoneBlank(body2)) {
			List<String> words = StringUtil.getRandomWords(body2);
			if (!words.isEmpty()) {
				bodyStr = "";
				for (int i = 0; i < words.size(); i++) {
					bodyStr += " " + StringUtils.trim(words.get(i));
				}
				bodyStr = bodyStr.replaceAll("\\p{Punct}", ".");
				if (StringUtils.isNotBlank(bodyStr)) {
					vo.setBody(bodyStr);
					list = msgInboxDao.getListForWeb(vo);
					assertFalse(list.isEmpty());
					for (MsgInboxWebVo mwvo : list) {
						logger.info("Search Subj/Body2: " + subjStr + "/" + bodyStr + ", Subject: " + mwvo.getMsgSubject());
						assertTrue(StringUtils.containsIgnoreCase(mwvo.getMsgSubject(), word1)
								|| StringUtils.containsIgnoreCase(mwvo.getMsgSubject(), word2));
					}
				}
			}
		}
	}
	
	private MsgInboxVo selectByMsgId(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		System.out.println("MsgInboxDao - selectByPrimaryKey: " + LF + msgInboxVo);
		return msgInboxVo;
	}
	
	private List<MsgInboxVo> selectByFromAddrId(long msgId) {
		List<MsgInboxVo> actions = msgInboxDao.getByFromAddrId(msgId);
		for (Iterator<MsgInboxVo> it = actions.iterator(); it.hasNext();) {
			MsgInboxVo vo = it.next();
			System.out.println("MsgInboxDao - selectByFromAddrId: " + vo.getMsgId() + " - " + vo.getFromAddress());
		}
		return actions;
	}

	private List<MsgInboxVo> selectByToAddrId(long msgId) {
		List<MsgInboxVo> actions = msgInboxDao.getByToAddrId(msgId);
		for (Iterator<MsgInboxVo> it = actions.iterator(); it.hasNext();) {
			MsgInboxVo vo = it.next();
			System.out.println("MsgInboxDao - selectByToAddrId: " + vo.getMsgId() + " - " + vo.getToAddress());
		}
		return actions;
	}
	
	private MsgInboxWebVo selectBroadcastMsg() {
		SearchFieldsVo vo = new SearchFieldsVo();
		vo.setRuleName(RuleNameType.BROADCAST.toString());
		vo.setMsgType(SearchFieldsVo.MsgType.Closed);
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(vo);
		for (MsgInboxWebVo webVo : list) {
			System.out.println("MsgInboxWebVo - selectBroadcastMsg: " + LF + webVo);
			return webVo;
		}
		return null;
	}

	private MsgInboxWebVo selectInboundGenericMsg() {
		SearchFieldsVo vo = new SearchFieldsVo();
		vo.setRuleName(RuleNameType.GENERIC.toString());
		vo.setMsgType(SearchFieldsVo.MsgType.Received);
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(vo);
		for (MsgInboxWebVo webVo : list) {
			System.out.println("MsgInboxWebVo - selectInboundGenericMsg: " + LF + webVo);
			return webVo;
		}
		return null;
	}

	private int update(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		int rows = 0;
		if (msgInboxVo!=null) {
			msgInboxVo.setCarrierCode(MailCodeType.SMTPMAIL.value());
			msgInboxVo.setPurgeDate(new java.sql.Date(System.currentTimeMillis()));
			rows = msgInboxDao.update(msgInboxVo);
			System.out.println("MsgInboxDao - update: rows updated:  " + rows);
			System.out.println("InboxUnreadCount: " + unreadCountDao.selectInboxUnreadCount());
		}
		return rows;
	}
	
	private int deleteByPrimaryKey(long msgId) {
		int rowsDeleted = msgInboxDao.deleteByPrimaryKey(msgId);
		System.out.println("MsgInboxDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		System.out.println("InboxUnreadCount: " + unreadCountDao.selectInboxUnreadCount());
		return rowsDeleted;
	}
	
	private MsgInboxVo insert(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo != null) {
			long nextVal = msgSequenceDao.findNextValue();
			msgInboxVo.setMsgId(nextVal);
			System.out.println("InboxUnreadCount before: " + unreadCountDao.selectInboxUnreadCount());
			msgInboxDao.insert(msgInboxVo);
			System.out.println("MsgInboxDao - insert: " + LF + msgInboxVo);
			System.out.println("InboxUnreadCount after: "+ unreadCountDao.selectInboxUnreadCount());
			return msgInboxVo;
		}
		return null;
	}

	private MsgClickCountsVo insertClickCount(MsgInboxVo msgvo) {
		MsgClickCountsVo vo = msgClickCountsDao.getRandomRecord();
		if (vo != null) {
			vo.setMsgId(msgvo.getMsgId());
			msgClickCountsDao.insert(vo);
			System.out.println("insertClickCount: " + LF + vo);
		}
		return vo;
	}

	private MsgClickCountsVo selectClickCounts(long msgId) {
		MsgClickCountsVo vo = msgClickCountsDao.getByPrimaryKey(msgId);
		System.out.println("selectByPrimaryKey - " + LF + vo);
		return vo;
	}

	private int updateClickCounts(MsgClickCountsVo msgClickCountsVo) {
		int rows = 0;
		msgClickCountsVo.setSentCount(msgClickCountsVo.getSentCount() + 1);
		rows += msgClickCountsDao.update(msgClickCountsVo);
		rows += msgClickCountsDao.updateOpenCount(msgClickCountsVo.getMsgId());
		rows += msgClickCountsDao.updateClickCount(msgClickCountsVo.getMsgId());
		rows += msgClickCountsDao.updateUnsubscribeCount(msgClickCountsVo.getMsgId(), 1);
		rows += msgClickCountsDao.updateComplaintCount(msgClickCountsVo.getMsgId(), 1);
		rows += msgClickCountsDao.updateClickCount(msgClickCountsVo.getMsgId());
		System.out.println("updateClickCounts: rows updated: " + rows + LF + msgClickCountsVo);
		return rows;
	}
	
	private int deleteClickCounts(long msgId) {
		int rowsDeleted = msgClickCountsDao.deleteByPrimaryKey(msgId);
		System.out.println("deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
	
}

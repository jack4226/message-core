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

import ltj.data.preload.RuleNameEnum;
import ltj.message.constant.CarrierCode;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgClickCountsDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgUnreadCountDao;
import ltj.message.dao.outbox.MsgSequenceDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.PrintUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.PagingVo.PageAction;
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
	public void testWebSearch() {
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
		
		// test get by from email address
		String addr1 = ivo1.getFromAddress();
		String addr2 = ivo2.getFromAddress();
		vo.setFromAddr(EmailAddrUtil.getEmailDomainName(addr1) + " " + EmailAddrUtil.getEmailUserName(addr2));
		List<MsgInboxWebVo> listAddr = msgInboxDao.getListForWeb(vo);
		assertFalse(listAddr.isEmpty());
		
		// build and set subject search string
		String subjStr = word1;
		if (StringUtils.isNotBlank(word2)) {
			subjStr += "  " + word2;
		}
		subjStr = subjStr.replaceAll("\\p{Punct}", ".");
		vo.setSubject(subjStr);
		
		// build and set search string for body 1
		String bodyStr = "";
		if (StringUtils.isNotBlank(body1)) {
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
		vo.resetPageContext();
		list = msgInboxDao.getListForWeb(vo);
		assertFalse(list.isEmpty());
		for (MsgInboxWebVo mwvo : list) {
			logger.info("Search Subj/Body1: " + subjStr + "/" + bodyStr + ", Subject: " + mwvo.getMsgSubject());
			assertTrue(StringUtils.containsIgnoreCase(mwvo.getMsgSubject(), word1)
					|| StringUtils.containsIgnoreCase(mwvo.getMsgSubject(), word2));
		}
		
		if (StringUtils.isNotBlank(body2)) {
			List<String> words = StringUtil.getRandomWords(body2);
			if (!words.isEmpty()) {
				bodyStr = "";
				for (int i = 0; i < words.size(); i++) {
					bodyStr += " " + StringUtils.trim(words.get(i));
				}
				bodyStr = bodyStr.replaceAll("\\p{Punct}", ".");
				if (StringUtils.isNotBlank(bodyStr)) {
					vo.setBody(bodyStr);
					logger.info("Search by body2: " + bodyStr);
					vo.resetPageContext();
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
	
	@Test
	public void testWithPaging() {
		int testPageSize = 2;
		SearchFieldsVo vo = new SearchFieldsVo();
		vo.setPageSize(testPageSize);
		vo.setMsgType(null);
		// get the first page
		List<MsgInboxWebVo> list1 = msgInboxDao.getListForWeb(vo);
		assertFalse(list1.isEmpty());
		int rows = msgInboxDao.getRowCountForWeb(vo);
		logger.info("Total number of rows = " + rows);
		assertTrue(rows >= list1.size());
		// get it again
		vo.setPageAction(PageAction.CURRENT);
		List<MsgInboxWebVo> list2 = msgInboxDao.getListForWeb(vo);
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			logger.info("vo1:" + PrintUtil.prettyPrint(list1.get(i)));
			assertMsgInboxWebVosSame(list1.get(i), list2.get(i));
		}
		// get the second page
		vo.setPageAction(PageAction.NEXT);
		List<MsgInboxWebVo> list3 = msgInboxDao.getListForWeb(vo);
		if (!list3.isEmpty()) {
			for (int i = 0; i < list3.size(); i++) {
				logger.info("vo3:" + PrintUtil.prettyPrint(list3.get(i)));
			}
			assertTrue(list3.get(0).getMsgId() < list1.get(list1.size() - 1).getMsgId());
			// get the first page
			vo.setPageAction(PageAction.PREVIOUS);
			List<MsgInboxWebVo> list4 = msgInboxDao.getListForWeb(vo);
			assertEquals(list1.size(), list4.size());
			for (int i = 0; i < list4.size(); i++) {
				logger.info("vo4:" + PrintUtil.prettyPrint(list4.get(i)));
				assertMsgInboxWebVosSame(list1.get(i), list4.get(i));
			}
		}
		// get the last page
		vo.setPageAction(PageAction.LAST);
		List<MsgInboxWebVo> list5 = msgInboxDao.getListForWeb(vo);
		assertFalse(list5.isEmpty());
		for (int i = 0; i < list5.size(); i++) {
			logger.info("vo5:" + PrintUtil.prettyPrint(list5.get(i)));
		}
		vo.setPageAction(PageAction.PREVIOUS);
		msgInboxDao.getListForWeb(vo);
		// get it again
		vo.setPageAction(PageAction.NEXT);
		List<MsgInboxWebVo> list6 = msgInboxDao.getListForWeb(vo);
		assertEquals(list5.size(), list6.size());
		for (int i = 0; i < list5.size(); i++) {
			assertMsgInboxWebVosSame(list5.get(i), list6.get(i));
		}
		// back to the first page
		vo.setPageAction(PageAction.FIRST);
		List<MsgInboxWebVo> list7 = msgInboxDao.getListForWeb(vo);
		assertEquals(list1.size(), list7.size());
		for (int i = 0; i < list1.size(); i++) {
			assertMsgInboxWebVosSame(list1.get(i), list7.get(i));
		}
	}
	
	void assertMsgInboxWebVosSame(MsgInboxWebVo vo1, MsgInboxWebVo vo2) {
		assertEquals(vo1.getAttachmentCount(), vo2.getAttachmentCount());
		assertEquals(vo1.getAttachmentSize(), vo2.getAttachmentSize());
		assertEquals(vo1.getFlagged(), vo2.getFlagged());
		assertEquals(vo1.getForwardCount(), vo2.getForwardCount());
		assertEquals(vo1.getFromAddrId(), vo2.getFromAddrId());
		assertEquals(vo1.getFromAddress(), vo2.getFromAddress());
		assertEquals(vo1.getLeadMsgId(), vo2.getLeadMsgId());
		assertEquals(vo1.getMsgBodySize(), vo2.getMsgBodySize());
		assertEquals(vo1.getMsgDirection(), vo2.getMsgDirection());
		assertEquals(vo1.getMsgId(), vo2.getMsgId());
		assertEquals(vo1.getMsgRefId(), vo2.getMsgRefId());
		assertEquals(vo1.getMsgSubject(), vo2.getMsgSubject());
		assertEquals(vo1.getReadCount(), vo2.getReadCount());
		assertEquals(vo1.getReplyCount(), vo2.getReplyCount());
		assertEquals(vo1.getRuleName(), vo2.getRuleName());
		assertEquals(vo1.getStatusId(), vo2.getStatusId());
		assertEquals(vo1.getToAddrId(), vo2.getToAddrId());
		assertEquals(vo1.getReceivedTime(), vo2.getReceivedTime());
	}
	
	private MsgInboxVo selectByMsgId(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		logger.info("selectByPrimaryKey: " + LF + msgInboxVo);
		return msgInboxVo;
	}
	
	private List<MsgInboxVo> selectByFromAddrId(long msgId) {
		List<MsgInboxVo> actions = msgInboxDao.getByFromAddrId(msgId);
		for (Iterator<MsgInboxVo> it = actions.iterator(); it.hasNext();) {
			MsgInboxVo vo = it.next();
			logger.info("selectByFromAddrId: " + vo.getMsgId() + " - " + vo.getFromAddress());
		}
		return actions;
	}

	private List<MsgInboxVo> selectByToAddrId(long msgId) {
		List<MsgInboxVo> actions = msgInboxDao.getByToAddrId(msgId);
		for (Iterator<MsgInboxVo> it = actions.iterator(); it.hasNext();) {
			MsgInboxVo vo = it.next();
			logger.info("selectByToAddrId: " + vo.getMsgId() + " - " + vo.getToAddress());
		}
		return actions;
	}
	
	private MsgInboxWebVo selectBroadcastMsg() {
		SearchFieldsVo vo = new SearchFieldsVo();
		vo.setRuleName(RuleNameEnum.BROADCAST.name());
		vo.setMsgType(SearchFieldsVo.MsgType.Closed);
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(vo);
		for (MsgInboxWebVo webVo : list) {
			logger.info("MsgInboxWebVo - selectBroadcastMsg: " + LF + webVo);
			return webVo;
		}
		return null;
	}

	private MsgInboxWebVo selectInboundGenericMsg() {
		SearchFieldsVo vo = new SearchFieldsVo();
		vo.setRuleName(RuleNameEnum.GENERIC.name());
		vo.setMsgType(SearchFieldsVo.MsgType.Received);
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(vo);
		for (MsgInboxWebVo webVo : list) {
			logger.info("MsgInboxWebVo - selectInboundGenericMsg: " + LF + webVo);
			return webVo;
		}
		return null;
	}

	private int update(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		int rows = 0;
		if (msgInboxVo!=null) {
			msgInboxVo.setCarrierCode(CarrierCode.SMTPMAIL.value());
			msgInboxVo.setPurgeDate(new java.sql.Date(System.currentTimeMillis()));
			rows = msgInboxDao.update(msgInboxVo);
			logger.info("update: rows updated:  " + rows);
			logger.info("InboxUnreadCount: " + unreadCountDao.selectInboxUnreadCount());
		}
		return rows;
	}
	
	private int deleteByPrimaryKey(long msgId) {
		int rowsDeleted = msgInboxDao.deleteByPrimaryKey(msgId);
		logger.info("deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		logger.info("InboxUnreadCount: " + unreadCountDao.selectInboxUnreadCount());
		return rowsDeleted;
	}
	
	private MsgInboxVo insert(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo != null) {
			long nextVal = msgSequenceDao.findNextValue();
			msgInboxVo.setMsgId(nextVal);
			logger.info("InboxUnreadCount before: " + unreadCountDao.selectInboxUnreadCount());
			msgInboxDao.insert(msgInboxVo);
			logger.info("insert: " + LF + msgInboxVo);
			logger.info("InboxUnreadCount after: "+ unreadCountDao.selectInboxUnreadCount());
			return msgInboxVo;
		}
		return null;
	}

	private MsgClickCountsVo insertClickCount(MsgInboxVo msgvo) {
		MsgClickCountsVo vo = msgClickCountsDao.getRandomRecord();
		if (vo != null) {
			vo.setMsgId(msgvo.getMsgId());
			msgClickCountsDao.insert(vo);
			logger.info("insertClickCount: " + LF + vo);
		}
		return vo;
	}

	private MsgClickCountsVo selectClickCounts(long msgId) {
		MsgClickCountsVo vo = msgClickCountsDao.getByPrimaryKey(msgId);
		logger.info("selectByPrimaryKey - " + LF + vo);
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
		logger.info("updateClickCounts: rows updated: " + rows + LF + msgClickCountsVo);
		return rows;
	}
	
	private int deleteClickCounts(long msgId) {
		int rowsDeleted = msgClickCountsDao.deleteByPrimaryKey(msgId);
		logger.info("deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
	
}

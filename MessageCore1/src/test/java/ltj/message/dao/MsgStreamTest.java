package ltj.message.dao;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bo.test.RuleEngineTest;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.vo.outbox.MsgStreamVo;

@FixMethodOrder
public class MsgStreamTest extends DaoTestBase {
	@Resource
	private MsgStreamDao msgStreamDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	@Resource
	private EmailAddrDao emailAddrDao;

	private static MsgStreamVo lastRecord;
	
	@Before
	@Rollback(value=false)
	public void checkMsgStream() throws IOException {
		if (msgStreamDao.getLastRecord() == null) {
			insertStream();
		}
		lastRecord = msgStreamDao.getLastRecord();
	}

	@Rollback(value=false)
	private MsgStreamVo insertStream() {
		MsgInboxVo msgInboxVo = msgInboxDao.getRandomRecord();
		assertNotNull(msgInboxVo);
		MsgStreamVo msgStreamVo = new MsgStreamVo();
		msgStreamVo.setMsgId(msgInboxVo.getMsgId());
		msgStreamVo.setFromAddrId(msgInboxVo.getFromAddrId());
		msgStreamVo.setAddTime(msgInboxVo.getUpdtTime());
		msgStreamVo.setMsgStream(getBouncedMail(1));
		return insert(msgStreamVo);
	}
	
	private byte[] getBouncedMail(int fileNbr) {
		InputStream is = getClass().getResourceAsStream(
				"/ltj/message/bo/inbox/bouncedmails/BouncedMail_" + fileNbr + ".txt");
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[512];
		int len = 0;
		try { 
			while ((len = bis.read(bytes, 0, bytes.length)) > 0) {
				baos.write(bytes, 0, len);
			}
			byte[] mailStream = baos.toByteArray();
			baos.close();
			bis.close();
			return mailStream;
		}
		catch (IOException e) {
			fail();
		}
		return null;
	}
	
	static boolean runRuleEngineTest = false;

	@Test
	public void test1() {
		assertNotNull(lastRecord);
		/*
		 * Received error from Spring framework:
		 * java.lang.IllegalStateException: Cannot start a new transaction without ending the existing transaction.
		 * 
		 * XXX moved to stand alone test class - RunClassesTest.java
		 */
		if (runRuleEngineTest) {
			try {
				Result result = JUnitCore.runClasses(RuleEngineTest.class);
				for (Failure failure : result.getFailures()) {
					System.err.println(failure.toString());
				}
			}
			catch (Exception e) {
				fail();
			}
		}
	}

	@Test
	@Rollback(value=true)
	public void test2() throws MessagingException {
		MsgStreamVo msgStreamVo = selectLastRecord();
		assertNotNull(msgStreamVo);
		MsgStreamVo msgStreamVo2 = selectByPrimaryKey(msgStreamVo.getMsgId());
		assertTrue(msgStreamVo.equalsTo(msgStreamVo2));
		MessageBean msgBean = createMimeMessage(msgStreamVo);
		assertNotNull(msgBean);
		int rowsUpdated = update(msgStreamVo);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = deleteByPrimaryKey(msgStreamVo);
		assertEquals(rowsDeleted, 1);
		msgStreamVo = insert(msgStreamVo);
		assertNotNull(msgStreamVo);
	}
	
	@Test
	public void test3() {
		MsgStreamVo vo = msgStreamDao.getRandomRecord();
		assertNotNull(vo);
		List<MsgStreamVo> list1 = msgStreamDao.getByFromAddrId(vo.getFromAddrId());
		assertFalse(list1.isEmpty());
		assertEquals(vo.getFromAddrId(), list1.get(0).getFromAddrId());
		
		EmailAddrVo addrvo1 = emailAddrDao.getByAddrId(vo.getFromAddrId());
		assertNotNull(addrvo1);
		
		List<MsgStreamVo> list2 = msgStreamDao.getByFromAddress(addrvo1.getEmailAddr());
		assertEquals(list1.size(), list2.size());
		
		List<MsgStreamVo> list3 = msgStreamDao.getByToAddrId(vo.getToAddrId());
		assertFalse(list3.isEmpty());

		EmailAddrVo addrvo2 = emailAddrDao.getByAddrId(vo.getToAddrId());
		assertNotNull(addrvo2);
		
		List<MsgStreamVo> list4 = msgStreamDao.getByToAddress(addrvo2.getEmailAddr());
		assertEquals(list3.size(), list4.size());
		for (int i = 0; i < list3.size(); i++) {
			assertTrue(list3.get(i).equalsTo(list4.get(i)));
		}
	}
	
	
	private MsgStreamVo selectByPrimaryKey(long msgId) {
		MsgStreamVo msgStreamVo = msgStreamDao.getByPrimaryKey(msgId);
		System.out.println("MsgStreamDao - selectByPrimaryKey: "+LF+msgStreamVo);
		return msgStreamVo;
	}
	
	private MsgStreamVo selectLastRecord() {
		MsgStreamVo msgStreamVo = msgStreamDao.getLastRecord();
		System.out.println("MsgStreamDao - selectLastRecord: "+LF+msgStreamVo);
		return msgStreamVo;
	}
	
	private int update(MsgStreamVo msgStreamVo) {
		msgStreamVo.setMsgSubject("Test Subject");
		int rows = msgStreamDao.update(msgStreamVo);
		System.out.println("MsgStreamDao - update: rows updated: "+rows);
		return rows;
	}
	
	private int deleteByPrimaryKey(MsgStreamVo msgStreamVo) {
		int rowsDeleted = msgStreamDao.deleteByPrimaryKey(msgStreamVo.getMsgId());
		System.out.println("MsgStreamDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgStreamVo insert(MsgStreamVo msgStreamVo) {
		int rows=msgStreamDao.insert(msgStreamVo);
		System.out.println("MsgStreamDao - insert: rows inserted " + rows + LF + msgStreamVo);
		return msgStreamVo;
	}
	
	private MessageBean createMimeMessage(MsgStreamVo msgStreamVo) throws MessagingException {
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(msgStreamVo.getMsgStream());
		System.out.println("******************************");
		System.out.println("MessageBean created: " + msgBean);
		return msgBean;
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.vo.outbox.MsgStreamVo;

public class MsgStreamTest extends DaoTestBase {
	@Resource
	private MsgStreamDao msgStreamDao;
	@Resource
	private MsgInboxDao msgInboxDao;

	@Before
	public void checkMsgStream() throws IOException {
		long testMsgId = 2L;
		if (msgStreamDao.getLastRecord()==null) {
			MsgInboxVo msgInboxVo = selectByMsgId(testMsgId);
			MsgStreamVo msgStreamVo = new MsgStreamVo();
			msgStreamVo.setMsgId(msgInboxVo.getMsgId());
			msgStreamVo.setFromAddrId(msgInboxVo.getFromAddrId());
			msgStreamVo.setAddTime(msgInboxVo.getUpdtTime());
			msgStreamVo.setMsgStream(getBouncedMail(1));
			insert(msgStreamVo);
		}
		/*
		 * This did not work, the MsgStream record was inserted, but the
		 * testMsgStream.selectLastRecord() still returns a null.
		 */
//		if (msgStreamDao.getLastRecord()==null) {
//			Result result = JUnitCore.runClasses(RuleEngineTest.class);
//			for (Failure failure : result.getFailures()) {
//				System.err.println(failure.toString());
//			}
//		}
	}
	private MsgInboxVo selectByMsgId(long msgId) {
		MsgInboxVo msgInboxVo = (MsgInboxVo)msgInboxDao.getByPrimaryKey(msgId);
		System.out.println("MsgInboxDao - selectByPrimaryKey: "+LF+msgInboxVo);
		return msgInboxVo;
	}

	byte[] getBouncedMail(int fileNbr) {
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

	@Test
	@Rollback(true)
	public void testMsgStream() throws MessagingException {
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
	
	private MsgStreamVo selectByPrimaryKey(long msgId) {
		MsgStreamVo msgStreamVo = (MsgStreamVo)msgStreamDao.getByPrimaryKey(msgId);
		System.out.println("MsgStreamDao - selectByPrimaryKey: "+LF+msgStreamVo);
		return msgStreamVo;
	}
	
	private MsgStreamVo selectLastRecord() {
		MsgStreamVo msgStreamVo = (MsgStreamVo)msgStreamDao.getLastRecord();
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

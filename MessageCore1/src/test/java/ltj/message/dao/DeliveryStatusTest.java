package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.constant.Constants;
import ltj.message.constant.MLDeliveryType;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.dao.outbox.DeliveryStatusDao;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.vo.outbox.DeliveryStatusVo;

public class DeliveryStatusTest extends DaoTestBase {
	@Resource
	private DeliveryStatusDao deliveryStatusDao;
	@Resource
	private EmailAddressDao emailAddressDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	private static long testMsgId = 3L;
	private String testEmailAddr = Constants.DEMOLIST1_ADDR;

	@Test
	@Rollback(value=true)
	public void testDeliveryStatus() {
		try {
			if (msgInboxDao.getByPrimaryKey(testMsgId) == null) {
				testMsgId = msgInboxDao.getLastRecord().getMsgId();
			}
			List<DeliveryStatusVo> list = selectByMsgId(testMsgId);
			if (list.size() == 0) {
				insert(testMsgId);
			}
			assertTrue(deliveryStatusDao.getRandomRecord().size() > 0);
			list = selectByMsgId(testMsgId);
			assertTrue(list.size() > 0);
			DeliveryStatusVo vo0 = list.get(0);
			DeliveryStatusVo vo = selectByPrimaryKey(vo0.getMsgId(), vo0.getFinalRecipientId());
			assertNotNull(vo);
			DeliveryStatusVo vo2 = insert(vo.getMsgId());
			assertNotNull(vo2);
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2.getMsgId(), vo2.getFinalRecipientId());
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<DeliveryStatusVo> selectByMsgId(long msgId) {
		List<DeliveryStatusVo> actions = deliveryStatusDao.getByMsgId(msgId);
		for (Iterator<DeliveryStatusVo> it = actions.iterator(); it.hasNext();) {
			DeliveryStatusVo deliveryStatusVo = it.next();
			logger.info("DeliveryStatusDao - selectByMsgId: " + LF + deliveryStatusVo);
		}
		return actions;
	}

	private DeliveryStatusVo selectByPrimaryKey(long msgId, long finalRcptId) {
		DeliveryStatusVo deliveryStatusVo = (DeliveryStatusVo) deliveryStatusDao.getByPrimaryKey(
				msgId, finalRcptId);
		logger.info("MsgAttachmentDao - selectByPrimaryKey: " + LF + deliveryStatusVo);
		return deliveryStatusVo;
	}

	private int update(DeliveryStatusVo deliveryStatusVo) {
		deliveryStatusVo.setDeliveryStatus("");
		int rows = deliveryStatusDao.update(deliveryStatusVo);
		logger.info("MsgAttachmentDao - update: " + LF + deliveryStatusVo);
		return rows;
	}

	private int deleteByPrimaryKey(long msgId, long finalRcptId) {
		int rowsDeleted = deliveryStatusDao.deleteByPrimaryKey(msgId, finalRcptId);
		logger.info("DeliveryStatusDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}

	private DeliveryStatusVo insert(long msgId) {
		List<DeliveryStatusVo> list = deliveryStatusDao.getByMsgId(msgId);
		if (list.size() > 0) {
			DeliveryStatusVo vo = list.get(list.size() - 1);
			vo.setFinalRecipientId(vo.getFinalRecipientId() + 1);
			int rows = deliveryStatusDao.insert(vo);
			logger.info("DeliveryStatusDao - insert: rows inserted " + rows + LF + vo);
			return vo;
		}
		else {
			DeliveryStatusVo vo = new DeliveryStatusVo();
			vo.setDeliveryStatus(MLDeliveryType.ALL_ON_LIST.value());
			EmailAddressVo addrVo = selectByAddress(testEmailAddr);
			assertNotNull(addrVo);
			vo.setMsgId(msgId);
			vo.setFinalRecipientId(addrVo.getEmailAddrId());
			vo.setFinalRecipient(addrVo.getEmailAddr());
			vo.setMessageId("<24062053.11229376477123.JavaMail.IAPJKW@TSD-97050>");
			vo.setDsnRfc822("RFC822");
			vo.setDsnText("DESn Text");
			int rows = deliveryStatusDao.insert(vo);
			logger.info("DeliveryStatusDao - insert: rows inserted " + rows + LF + vo);
			return vo;
		}
	}

	private EmailAddressVo selectByAddress(String emailAddr) {
		EmailAddressVo addrVo = emailAddressDao.findByAddress(emailAddr);
		logger.info("EmailAddressDao - selectByAddress: "+LF+addrVo);
		return addrVo;
	}
}

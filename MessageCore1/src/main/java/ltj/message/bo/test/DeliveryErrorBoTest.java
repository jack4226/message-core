package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.dao.outbox.DeliveryStatusDao;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.vo.outbox.DeliveryStatusVo;

public class DeliveryErrorBoTest extends BoTestBase {
	@Resource
	private DeliveryStatusDao deliveryStatusDao;
	@Resource
	private TaskBaseBo deliveryErrorBo;
	private String rfc822Text = "RFC822";
	private String dsnText = "test DSN Text - from BO Test";
	@Test
	@Rollback(true)
	public void deliveryError() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setFinalRcpt("testto@localhost");
		if (messageBean.getMsgRefId()==null) {
			MsgInboxVo vo = selectMsgInboxByMsgId(messageBean.getMsgId());
			assertNotNull(vo);
			messageBean.setMsgRefId(vo.getMsgRefId());
		}
		if (messageBean.getDsnText()==null) {
			messageBean.setDsnText("test DSN Text - from BO Test");
		}
		if (messageBean.getDsnRfc822()==null) {
			messageBean.setDsnRfc822("RFC822");
		}
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		deliveryStatusDao.deleteByMsgId(messageBean.getMsgId()); // make the test repeatable
		deliveryErrorBo.process(messageBean);
		// now verify database record
		List<DeliveryStatusVo> list = deliveryStatusDao.getByMsgId(messageBean.getMsgRefId());
		assertTrue(list.size()>0);
		for (DeliveryStatusVo vo : list) {
			assertEquals(vo.getDsnRfc822(), rfc822Text);
			assertEquals(vo.getDsnText(), dsnText);
		}
	}
}

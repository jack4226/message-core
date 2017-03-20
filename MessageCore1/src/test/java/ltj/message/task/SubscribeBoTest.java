package ltj.message.task;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import javax.mail.internet.InternetAddress;

import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.task.TaskBaseBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.AddressType;
import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;

public class SubscribeBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo subscribeBo ;
	@Resource
	private TaskBaseBo unsubscribeBo;
	@Resource
	private EmailSubscrptDao emailSubscrptDao;
	@Resource
	private MailingListDao mailingListDao;
	
	private String testFromAddress = "test@test.com";
	private String mailingListAddr = Constants.DEMOLIST2_ADDR;
	private enum Action {
		Subscribe, Unsubscribe
	}
	
	@Test
	@Rollback(value=true)
	public void unsubscribe() throws Exception {
		prepare(Action.Subscribe);
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setFrom(InternetAddress.parse(testFromAddress));
		messageBean.setTo(InternetAddress.parse(mailingListAddr));
		messageBean.setSubject("unsubscribe");
		subscribeBo.setTaskArguments("$"+AddressType.FROM_ADDR.value());
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		unsubscribeBo.process(messageBean);
		verifyDataRecord(Constants.N);
	}
	@Test
	@Rollback(value=true)
	public void subscribe() throws Exception {
		prepare(Action.Unsubscribe);
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setFrom(InternetAddress.parse(testFromAddress));
		messageBean.setTo(InternetAddress.parse(mailingListAddr));
		messageBean.setSubject("subscribe");
		subscribeBo.setTaskArguments("$"+AddressType.FROM_ADDR.value());
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		subscribeBo.process(messageBean);
		verifyDataRecord(Constants.Y);
	}
	private void prepare(Action action) {
		EmailAddressVo addrVo = selectEmailAddrByAddress(testFromAddress);
		assertNotNull(addrVo);
		List<MailingListVo> list = mailingListDao.getByAddress(mailingListAddr);
		assertTrue(list.size()>0);
		if (action.equals(Action.Subscribe)) {
			emailSubscrptDao.subscribe(addrVo.getEmailAddrId(), list.get(0).getListId());
		}
		else {
			emailSubscrptDao.unsubscribe(addrVo.getEmailAddrId(), list.get(0).getListId());
		}
	}
	private void verifyDataRecord(String subscribed) {
		EmailAddressVo addrVo = selectEmailAddrByAddress(testFromAddress);
		assertNotNull(addrVo);
		List<MailingListVo> list = mailingListDao.getByAddress(mailingListAddr);
		assertTrue(list.size()>0);
		EmailSubscrptVo vo = emailSubscrptDao.getByAddrAndListId(addrVo.getEmailAddr(), list.get(0).getListId());
		assertNotNull(vo);
		assertEquals(subscribed, vo.getSubscribed());
	}
}

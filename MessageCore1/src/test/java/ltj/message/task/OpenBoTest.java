package ltj.message.task;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.task.TaskBaseBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.StatusId;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.vo.inbox.MsgInboxVo;

public class OpenBoTest extends BoTestBase {

	@Resource
	private TaskBaseBo openBo;
	@Resource
	private MsgInboxDao inboxDao;
	
	@Test
	public void close() throws Exception {
		MessageBean mBean = new MessageBean();
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		String toaddr = "watched_maibox@domain.com";
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ " Test body message.");
		mBean.setMailboxUser("testUser");
		MsgInboxVo minbox = inboxDao.getRandomRecord();
		if (StatusId.OPENED.value().equals(minbox.getStatusId())) {
			minbox.setStatusId(StatusId.CLOSED.value());
			inboxDao.update(minbox);
		}
		mBean.setMsgId(minbox.getMsgId());
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + mBean);
		}
		
		openBo.process(mBean);
		
		// verify results
		MsgInboxVo minbox2 = inboxDao.getByPrimaryKey(mBean.getMsgId());
		assertEquals(StatusId.OPENED.value(), minbox2.getStatusId());
	}

}

package ltj.message.task;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.test.BoTestBase;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.util.PrintUtil;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.MsgInboxVo;

public class SaveBoTest extends BoTestBase {

	@Resource
	private TaskBaseBo saveBo;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private MessageParser parser;
	@Resource
	private EmailAddrDao emailDao;
	
	@Test
	public void saveMessage() throws Exception {
		MessageBean mBean = new MessageBean();
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		String toaddr = "support@localhost";
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ " Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setMailboxUser("testUser");
		
		String ruleName = parser.parse(mBean);
		mBean.setRuleName(ruleName);
		
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + mBean);
		}
		
		Long msgId = (Long) saveBo.process(mBean);
		assertNotNull(msgId);
		
		// verify results
		MsgInboxVo minbox = inboxDao.getByPrimaryKey(msgId);
		assertNotNull(minbox);
		logger.info("Message Inbox: " + PrintUtil.prettyPrint(minbox));
		assertNotNull(minbox.getFromAddrId());
		EmailAddrVo addrvo = emailDao.getByAddrId(minbox.getFromAddrId());
		assertNotNull(addrvo);
		assertEquals(fromaddr, addrvo.getEmailAddr());
		assertEquals(mBean.getSubject(), minbox.getMsgSubject());
		assertEquals(mBean.getBody(), minbox.getMsgBody());
		assertEquals(mBean.getRuleName(), minbox.getRuleName());
	}

}

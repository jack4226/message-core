package ltj.message.task;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.AddressType;
import ltj.message.constant.StatusId;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.vo.emailaddr.EmailAddrVo;

@FixMethodOrder
public class SuspendBoTest extends BoTestBase {

	@Resource
	private TaskBaseBo suspendBo;
	@Resource
	private EmailAddrDao emailDao;
	
	private static String fromAddress = "user" + StringUtils.leftPad(new Random().nextInt(100)+"", 2, '0') + "@localhost";
	private static List<String> addrList = new ArrayList<>();

	@Test
	@Rollback(value=false)
	public void test0() {
		emailAddrDao.findByAddress(fromAddress);
	}

	@Test
	public void test1() throws Exception { // suspend
		MessageBean mBean = new MessageBean();
		String fromaddr = fromAddress;
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
		String finalRcptAddr = "testbounce@test.com";
		mBean.setFinalRcpt(finalRcptAddr);
		suspendBo.setTaskArguments(new String[] {"$"+AddressType.FINAL_RCPT_ADDR.value(), "$"+AddressType.FROM_ADDR.value()});
		addrList.add(fromaddr);
		addrList.add(finalRcptAddr);
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + mBean);
		}
		
		Long addrsSuspended = (Long) suspendBo.process(mBean);
		assertNotNull(addrsSuspended);
		assertTrue(addrsSuspended > 0);
		
		// verify results
		for (String addr : addrList) {
			EmailAddrVo addrvo = emailDao.getByAddress(addr);
			if (addrvo != null) {
				assertEquals(StatusId.SUSPENDED.value(), addrvo.getStatusId());
			}
		}
	}

}

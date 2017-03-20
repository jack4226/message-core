package ltj.message.task;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.task.TaskBaseBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.AddressType;
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.idtokens.EmailIdParser;
import ltj.message.vo.emailaddr.EmailAddressVo;

@FixMethodOrder
public class BounceBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo bounceBo;
	
	@Resource
	private EmailAddressDao emailAddressDao;
	
	private static String fromAddress = "user" + StringUtils.leftPad(new Random().nextInt(100)+"", 2, '0') + "@localhost";
	private static MessageBean messageBean;
	
	private static Map<String, Integer> CountMap = new HashMap<>();
	
	@Test
	@Rollback(value=false)
	public void test0() {
		// insert the address if it does not exist
		emailAddressDao.findByAddress(fromAddress);
	}
	
	@Test
	@Rollback(value=false)
	public void test1() throws Exception {
		messageBean = new MessageBean();
		String toaddr = "testto@localhost";
		try {
			messageBean.setFrom(InternetAddress.parse(fromAddress, false));
			messageBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		messageBean.setSubject("A Exception occured");
		messageBean.setValue(new Date()+ " Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		messageBean.setMailboxUser("testUser");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(messageBean.getBody());
		if (StringUtils.isNumeric(id)) {
			messageBean.setMsgRefId(Long.parseLong(id));
		}
		messageBean.setFinalRcpt("testbounce@test.com");
		bounceBo.setTaskArguments(new String[] {"$"+AddressType.FINAL_RCPT_ADDR.value(), "$"+AddressType.FROM_ADDR.value()});
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		// get before counts
		EmailAddressVo fromVo = emailAddressDao.getByAddress(messageBean.getFromAsString());
		assertNotNull(fromVo);
		CountMap.put(messageBean.getFromAsString(), fromVo.getBounceCount());
		EmailAddressVo finalRcptVo = emailAddressDao.getByAddress(messageBean.getFinalRcpt());
		if (finalRcptVo != null) {
			CountMap.put(messageBean.getFinalRcpt(), finalRcptVo.getBounceCount());
		}
		else {
			CountMap.put(messageBean.getFinalRcpt(), -1);
		}
		// invoke task scheduler
		Long rowsUpdated = (Long) bounceBo.process(messageBean);
		assertNotNull(rowsUpdated);
		assertTrue(rowsUpdated > 0);
		
		// verify results
		verifyBounceCount(messageBean.getFromAsString());
		verifyBounceCount(messageBean.getFinalRcpt());
	}
	
	
	@Test
	public void test2() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		bounceBo.setTaskArguments(new String[] {"$"+AddressType.FINAL_RCPT_ADDR.value(), "$"+AddressType.FROM_ADDR.value()});
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		// get "before" bounce count for From address
		EmailAddressVo addrVo = emailAddressDao.findByAddress(messageBean.getFromAsString());
		assertNotNull(addrVo);
		CountMap.put(messageBean.getFromAsString(), addrVo.getBounceCount());
		// end of "before" bounce count
		Long addrsUpdated = (Long) bounceBo.process(messageBean);
		assertTrue(addrsUpdated > 0);
		// now verify the database record
		verifyBounceCount(messageBean.getFromAsString());
	}

	private void verifyBounceCount(String address) {
		EmailAddressVo addr = emailAddressDao.getByAddress(address);
		if (addr != null) {
			Integer before = CountMap.get(address);
			assertNotNull(before);
			assertTrue(before < addr.getBounceCount());
			if (addr.getBounceCount() >= Constants.BOUNCE_SUSPEND_THRESHOLD) {
				assertTrue(StatusId.SUSPENDED.value().equals(addr.getStatusId()));
			}
		}
	}
}

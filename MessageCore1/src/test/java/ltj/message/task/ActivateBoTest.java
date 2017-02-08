package ltj.message.task;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.AddressType;
import ltj.message.constant.StatusIdCode;
import ltj.message.vo.emailaddr.EmailAddrVo;

public class ActivateBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo activateBo;

	@Test
	public void activate() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		activateBo.setTaskArguments(new String[] {"$"+AddressType.FINAL_RCPT_ADDR.value(), "$"+AddressType.FROM_ADDR.value()});
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		deActivateAddress(messageBean.getFromAsString());
		deActivateAddress(messageBean.getFinalRcpt());
		Long addrsActivated = (Long) activateBo.process(messageBean);
		assertTrue(addrsActivated > 0);
		// now verify the database record
		EmailAddrVo addrVo = selectEmailAddrByAddress(messageBean.getFromAsString());
		assertNotNull(addrVo);
		assertTrue(StatusIdCode.ACTIVE.equals(addrVo.getStatusId()));
		if (StringUtils.isNotBlank(messageBean.getFinalRcpt())) {
			addrVo = selectEmailAddrByAddress(messageBean.getFinalRcpt());
			assertNotNull(addrVo);
			assertTrue(StatusIdCode.ACTIVE.equals(addrVo.getStatusId()));
		}
	}

	private int deActivateAddress(String address) {
		if (address==null) {
			return 0;
		}
		EmailAddrVo vo = emailAddrDao.findByAddress(address);
		vo.setStatusId(StatusIdCode.INACTIVE);
		int rows = emailAddrDao.update(vo);
		EmailAddrVo vo2 = selectEmailAddrByAddress(address);
		assertTrue(StatusIdCode.INACTIVE.equals(vo2.getStatusId()));
		return rows;
	}
}

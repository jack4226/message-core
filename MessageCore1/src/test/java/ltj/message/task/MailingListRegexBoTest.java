package ltj.message.task;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.vo.emailaddr.MailingListVo;

public class MailingListRegexBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo mailingListRegExBo;
	@Resource
	private MailingListDao mailingListDao;
	
	@Test
	public void mailingListRegEx() throws Exception {
		List<String> addrList = new ArrayList<>();
		
		List<MailingListVo> list = mailingListDao.getAll(true);
		assertFalse(list.isEmpty());
		for (MailingListVo vo : list) {
			String emailAddr = EmailAddrUtil.removeDisplayName(vo.getEmailAddr(), true);
			String[] items = emailAddr.split("[\\.\\-]");
			addrList.addAll(Arrays.asList(items));
		}
		
		MessageBean messageBean = new MessageBean();
		
		String regex = (String) mailingListRegExBo.process(messageBean);
		assertNotNull(regex);
		
		logger.info("Mailing List Regex: " + regex);
		logger.info("Mailing List address items: " + addrList);
		for (String addr : addrList) {
			assertTrue(StringUtils.contains(regex, addr));
		}
	}
}

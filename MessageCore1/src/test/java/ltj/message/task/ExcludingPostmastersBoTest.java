package ltj.message.task;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.task.TaskBaseBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.Constants;
import ltj.message.dao.client.ClientDao;
import ltj.message.vo.ClientVo;

public class ExcludingPostmastersBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo excludingPostmastersBo;
	@Resource
	private ClientDao clientDao;
	
	@Test
	public void excludingPostmasters() throws Exception {
		List<String> addrList = new ArrayList<>();
		
		ClientVo vo = clientDao.getByClientId(Constants.DEFAULT_CLIENTID);
		assertNotNull(vo);
		assertNotNull(vo.getDomainName());
		addrList.add("postmaster@" + vo.getDomainName().trim());
		if (vo.getContactEmail() != null) {
			addrList.add(vo.getContactEmail());
		}
		
		MessageBean messageBean = new MessageBean();
		
		String addrs = (String) excludingPostmastersBo.process(messageBean);
		assertNotNull(addrs);
		
		logger.info("Postmaster addresses: " + addrs);
		for (String addr : addrList) {
			assertTrue(StringUtils.contains(addrs, addr));
		}
	}
}

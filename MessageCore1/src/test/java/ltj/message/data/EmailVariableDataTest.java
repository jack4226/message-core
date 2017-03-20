package ltj.message.data;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ltj.message.bo.template.RenderUtil;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.customer.CustomerDao;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.exception.DataValidationException;
import ltj.message.external.VariableResolver;
import ltj.message.vo.CustomerVo;
import ltj.message.vo.emailaddr.EmailVariableVo;

public class EmailVariableDataTest extends DaoTestBase {

	@Resource
	private EmailVariableDao emailVariableDao;
	@Resource
	private CustomerDao custDao;
	
	@Test
	public void testQueries() {
		List<EmailVariableVo> list = emailVariableDao.getAll();
		assertTrue(list.size() > 0);
		for (EmailVariableVo vo : list) {
			if (StringUtils.isNotBlank(vo.getVariableQuery())) {
				try {
					String result = emailVariableDao.getByQuery(vo.getVariableQuery(), 1L);
					assertNotNull(result);
				}
				catch (Exception e) {
					logger.error("Exception caught", e);
					fail();
				}
			}
		}
	}
	
	@Test
	public void loadClasses() {
		List<EmailVariableVo> list = emailVariableDao.getAll();
		assertTrue(list.size() > 0);
		for (EmailVariableVo vo : list) {
			if (StringUtils.isNotBlank(vo.getVariableProc())) {
				try {
					Class<?> proc = Class.forName(vo.getVariableProc());
					try {
						Object obj = proc.newInstance();
						if (!(obj instanceof VariableResolver)) {
							fail("VariableType class is not a VariableResolver");
						}
						else {
							VariableResolver job = (VariableResolver) obj;
							List<CustomerVo> custList = custDao.getFirst100();
							assertFalse(custList.isEmpty());
							String result = job.process(custList.get(0).getEmailAddrId());
							assertNotNull(result);
							assertNull(job.process(999999L));
						}
					}
					catch (Exception e) {
						fail("Failed to initialize the class.");
					}
				}
				catch (ClassNotFoundException e) {
					fail("Class: " + "\"" + vo.getVariableProc() + "\" Not Found");
				}
			}
		}
	}
	
	@Test
	public void checkVariableLoop() {
		List<EmailVariableVo> list = emailVariableDao.getAll();
		assertTrue(list.size() > 0);
		for (EmailVariableVo vo : list) {
			if (StringUtils.isNotBlank(vo.getDefaultValue())) {
				try {
					RenderUtil.checkVariableLoop(vo.getDefaultValue(), vo.getVariableName());
				}
				catch (DataValidationException e) {
					fail("Variable loop found!");
				}
			}
		}
	}
}

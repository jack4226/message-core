package ltj.message.bo;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.message.bo.customer.CustomerSignupBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.Constants;
import ltj.message.dao.customer.CustomerDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.CustomerVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;

public class CustomerSignupBoTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(CustomerSignupBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	
	@Resource
	private CustomerSignupBo signUpBo;
	
	@Resource
	private CustomerDao customerDao;
	@Resource
	private EmailSubscrptDao subscrDao;
	
	@Test
	public void testCustomerSignupBo() throws Exception {
		String listId = Constants.DEMOLIST1_NAME;
		try {
			CustomerVo vo = customerDao.getByCustId("test");
			vo.setCustId(vo.getCustId()+"_v2");
			CustomerVo vo2 = customerDao.getByCustId(vo.getCustId());
			if (vo2!=null) { // delete the customer record to make the test repeatable
				customerDao.delete(vo2.getCustId());
			}
			vo.setEmailAddr("jwang@localhost");
			int rows = signUp(vo, listId);
			assertEquals(rows, 1);
			rows = removeFromList(vo.getEmailAddr(), listId);
			assertEquals(rows, 1);
			rows = addToList(vo.getEmailAddr(), listId);
			assertEquals(rows, 1);
			
			EmailSubscrptVo subVo = subscrDao.getByAddrAndListId(vo.getEmailAddr(), listId);
			assertNotNull(subVo);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	int signUp(CustomerVo vo, String listId) throws DataValidationException {
		int rows = signUpBo.signUpAndSubscribe(vo, listId);
		logger.info("Number of rows added: " + rows);
		return rows;
	}

	int addToList(String emailAddr, String listId) throws DataValidationException {
		int rows = signUpBo.addToList(emailAddr, listId);
		logger.info("Number of rows added: " + rows);
		return rows;
	}

	int removeFromList(String emailAddr, String listId) throws DataValidationException {
		int rows = signUpBo.removeFromList(emailAddr, listId);
		logger.info("Number of rows removed: " + rows);
		return rows;
	}
}

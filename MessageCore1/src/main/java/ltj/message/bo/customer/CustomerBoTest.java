package ltj.message.bo.customer;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bo.test.BoTestBase;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.CustomerVo;

public class CustomerBoTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(CustomerBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private CustomerBo customerBo;
	@Test
	@Rollback(true)
	public void testCustomerBo() {
		try {
			CustomerVo vo = selectByCustId("test");
			assertNotNull(vo);
			CustomerVo vo2 = insert(vo);
			assertNotNull(vo2);
			vo.setCustId(vo2.getCustId());
			vo.setOrigCustId(vo2.getOrigCustId());
			vo.setPrimaryKey(vo2.getPrimaryKey());
			vo.setEmailAddr(vo2.getEmailAddr());
			vo.setEmailAddrId(vo2.getEmailAddrId());
			vo.setRowId(vo2.getRowId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(rows, 1);
			rows = delete(vo2);
			assertEquals(rows, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	CustomerVo selectByCustId(String custId) throws DataValidationException {
		CustomerVo vo = customerBo.getByCustId(custId);
		if (vo!=null) {
			logger.info("selectByCustId" + LF + vo);
		}
		return vo;
	}
	CustomerVo insert(CustomerVo vo) throws DataValidationException {
		vo.setCustId(vo.getCustId()+"_v2");
		vo.setEmailAddr("test@test.com");
		int rows = customerBo.insert(vo);
		logger.info("Number of customers added: " +rows);
		return selectByCustId(vo.getCustId());
	}
	int update(CustomerVo vo) throws DataValidationException {
		vo.setCityName("Raleigh");
		int rows = customerBo.update(vo);
		logger.info("Number of customers updated: " + rows);
		return rows;
	}
	int delete(CustomerVo vo) throws DataValidationException {
		int rows = customerBo.delete(vo.getCustId());
		logger.info("Number of customers deleted: " + rows);
		return rows;
	}
}

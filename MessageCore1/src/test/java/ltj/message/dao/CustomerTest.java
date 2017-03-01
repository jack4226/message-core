package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ltj.message.constant.Constants;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.customer.CustomerDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.PrintUtil;
import ltj.message.vo.CustomerVo;
import ltj.message.vo.PagingCustVo;
import ltj.message.vo.PagingVo.PageAction;

public class CustomerTest extends DaoTestBase {
	@Resource
	private CustomerDao customerDao;
	
	final static String defaultCustId = "test";
	static String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');

	@Test
	public void insertSelectDelete() {
		try {
			List<CustomerVo> list = selectByClientId(Constants.DEFAULT_CLIENTID);
			assertTrue(list.size() > 0);
			long emailAddrId = list.get(0).getEmailAddrId();
			assertTrue(emailAddrId > 0L);
			CustomerVo vo0 = selectByEmailAddrId(emailAddrId);
			assertNotNull(vo0);
			CustomerVo vo = insert();
			assertNotNull(vo);
			CustomerVo vo2 = selectByCustId(vo);
			assertNotNull(vo2);
			// sync-up next 3 fields since differences are expected
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setPrimaryKey(vo2.getPrimaryKey());
			// end of sync-up
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			CustomerVo vo = new CustomerVo();
			vo.setCustId(defaultCustId + "_" + suffix);
			delete(vo);
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testSearchWithPaging() {
		int testPageSize = 2;
		PagingCustVo vo = new PagingCustVo();
		vo.setPageSize(testPageSize);
		List<CustomerVo> list1 = customerDao.getCustomersWithPaging(vo);
		assertFalse(list1.isEmpty());
		
		vo.setPageAction(PageAction.CURRENT);
		List<CustomerVo> list2 = customerDao.getCustomersWithPaging(vo);
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			assertCustomerVosAreSame(list1.get(i), list2.get(i));
		}
		
		vo.setPageAction(PageAction.NEXT);
		List<CustomerVo> list3 = customerDao.getCustomersWithPaging(vo);
		if (!list3.isEmpty()) {
			logger.info("Next page found, page size = " + list3.size());
			assertTrue(list3.get(0).getCustId().compareTo(list1.get(list1.size() - 1).getCustId()) > 0);
			vo.setPageAction(PageAction.PREVIOUS);
			List<CustomerVo> list4 = customerDao.getCustomersWithPaging(vo);
			assertEquals(list1.size(), list4.size());
			for (int i = 0; i < list1.size(); i++) {
				assertCustomerVosAreSame(list1.get(i), list4.get(i));
			}
		}
		
		vo.setPageAction(PageAction.LAST);
		List<CustomerVo> list5 = customerDao.getCustomersWithPaging(vo);
		assertFalse(list5.isEmpty());
		
		vo.setPageAction(PageAction.FIRST);
		List<CustomerVo> list6 = customerDao.getCustomersWithPaging(vo);
		assertEquals(list1.size(), list6.size());
		for (int i = 0; i < list1.size(); i++) {
			assertCustomerVosAreSame(list1.get(i), list6.get(i));
		}
		
		CustomerVo cust = list1.get(0);
		vo.resetPageContext();
		vo.setClientId(Constants.DEFAULT_CLIENTID);
		vo.setLastName(StringUtils.lowerCase(cust.getLastName()));
		vo.setFirstName(StringUtils.upperCase(cust.getFirstName()));
		vo.setEmailAddr(EmailAddrUtil.getEmailDomainName(cust.getEmailAddr()));
		
		list1 = customerDao.getCustomersWithPaging(vo);
		assertFalse(list1.isEmpty());
		for (CustomerVo custvo : list1) {
			logger.info("Customer search result: " + PrintUtil.prettyPrint(custvo, 2));
		}
	}
	
	void assertCustomerVosAreSame(CustomerVo vo1, CustomerVo vo2) {
		assertEquals(vo1.getClientId(), vo2.getClientId());
		assertEquals(vo1.getBirthDate(), vo2.getBirthDate());
		assertEquals(vo1.getCityName(), vo2.getCityName());
		assertEquals(vo1.getCustId(), vo2.getCustId());
		assertEquals(vo1.getCountry(), vo2.getCountry());
		assertEquals(vo1.getDayPhone(), vo2.getDayPhone());
		assertEquals(vo1.getEmailAddr(), vo2.getEmailAddr());
		assertEquals(vo1.getFirstName(), vo2.getFirstName());
		assertEquals(vo1.getLastName(), vo2.getLastName());
		assertEquals(vo1.getRowId(), vo2.getRowId());
		assertEquals(vo1.getEveningPhone(), vo2.getEveningPhone());
		assertEquals(vo1.getMiddleName(), vo2.getMiddleName());
		assertEquals(vo1.getMobilePhone(), vo2.getMobilePhone());
		assertEquals(vo1.getMsgDetail(), vo2.getMsgDetail());
		assertEquals(vo1.getPostalCode(), vo2.getPostalCode());
		assertEquals(vo1.getProfession(), vo2.getProfession());
		assertEquals(vo1.getSsnNumber(), vo2.getSsnNumber());
		assertEquals(vo1.getStateCode(), vo2.getStateCode());
		assertEquals(vo1.getStatusId(), vo2.getStatusId());
		assertEquals(vo1.getStreetAddress(), vo2.getStreetAddress());
		assertEquals(vo1.getZipCode4(), vo2.getZipCode4());
	}
	
	private CustomerVo selectByCustId(CustomerVo vo) {
		CustomerVo customer = customerDao.getByCustId(vo.getCustId());
		if (customer != null) {
			logger.info("CustomerDao - selectByCustId: " + LF + customer);
		}
		return customer;
	}
	
	private CustomerVo selectByEmailAddrId(long emailId) {
		CustomerVo vo = customerDao.getByEmailAddrId(emailId);
		if (vo != null) {
			logger.info("CustomerDao - selectEmailAddrId: "+LF+vo);
		}
		return vo;
	}
	
	private List<CustomerVo> selectByClientId(String clientId) {
		List<CustomerVo> list = (List<CustomerVo>) customerDao.getByClientId(clientId);
		for (int i = 0; i < list.size(); i++) {
			CustomerVo customer = list.get(i);
			logger.info("CustomerDao - selectClientId: " + LF + customer);
		}
		return list;
	}
	
	private int update(CustomerVo vo) {
		CustomerVo customer = customerDao.getByCustId(vo.getCustId());
		int rows = 0;
		if (customer != null) {
			customer.setStatusId("A");
			rows = customerDao.update(customer);
			logger.info("CustomerDao - update: rows updated: " + rows);
		}
		return rows;
	}
	
	private CustomerVo insert() {
		CustomerVo customer = customerDao.getByCustId(defaultCustId);
		if (customer != null) {
			customer.setCustId(customer.getCustId() + "_" + suffix);
			customer.setEmailAddr("test." + suffix + "@" + EmailAddrUtil.getEmailDomainName(customer.getEmailAddr()));
			customer.setBirthDate(new java.util.Date());
			customerDao.insert(customer);
			logger.info("CustomerDao - insert: " + customer);
			return customer;
		}
		return null;
	}
	
	private int delete(CustomerVo customerVo) {
		int rowsDeleted = customerDao.delete(customerVo.getCustId());
		logger.info("CustomerDao - delete: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}

package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

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

public class CustomerTest extends DaoTestBase {
	@Resource
	private CustomerDao customerDao;
	final String defaultCustId = "test";

	@Test
	public void insertSelectDelete() {
		try {
			List<CustomerVo> list = selectByClientId(Constants.DEFAULT_CLIENTID);
			assertTrue(list.size()>0);
			CustomerVo vo0 = selectByEmailAddrId(1L);
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
			vo.setCustId(defaultCustId+"_1");
			delete(vo);
		}
	}

	@Test
	public void testSearchWithPaging() {
		PagingCustVo vo = new PagingCustVo();
		List<CustomerVo> custList = customerDao.getCustomersWithPaging(vo);
		assertFalse(custList.isEmpty());
		
		CustomerVo cust = custList.get(0);
		vo.setClientId(Constants.DEFAULT_CLIENTID);
		vo.setLastName(StringUtils.lowerCase(cust.getLastName()));
		vo.setFirstName(StringUtils.upperCase(cust.getFirstName()));
		vo.setEmailAddr(EmailAddrUtil.getEmailDomainName(cust.getEmailAddr()));
		
		custList = customerDao.getCustomersWithPaging(vo);
		assertFalse(custList.isEmpty());
		for (CustomerVo custvo : custList) {
			System.out.println("Customer search result: " + PrintUtil.prettyPrint(custvo, 2));
		}
	}
	
	
	private CustomerVo selectByCustId(CustomerVo vo) {
		CustomerVo customer = customerDao.getByCustId(vo.getCustId());
		if (customer!=null) {
			System.out.println("CustomerDao - selectByCustId: "+LF+customer);
		}
		return customer;
	}
	
	private CustomerVo selectByEmailAddrId(long emailId) {
		CustomerVo vo = customerDao.getByEmailAddrId(emailId);
		if (vo != null) {
			System.out.println("CustomerDao - selectEmailAddrId: "+LF+vo);
		}
		return vo;
	}
	
	private List<CustomerVo> selectByClientId(String clientId) {
		List<CustomerVo> list = (List<CustomerVo>)customerDao.getByClientId(clientId);
		for (int i=0; i<list.size(); i++) {
			CustomerVo customer = list.get(i);
			System.out.println("CustomerDao - selectClientId: "+LF+customer);
		}
		return list;
	}
	
	private int update(CustomerVo vo) {
		CustomerVo customer = customerDao.getByCustId(vo.getCustId());
		int rows = 0;
		if (customer!=null) {
			customer.setStatusId("A");
			rows = customerDao.update(customer);
			System.out.println("CustomerDao - update: rows updated: "+ rows);
		}
		return rows;
	}
	
	private CustomerVo insert() {
		CustomerVo customer = customerDao.getByCustId(defaultCustId);
		if (customer!=null) {
			customer.setCustId(customer.getCustId()+"_1");
			customer.setEmailAddr("test."+customer.getEmailAddr());
			customer.setBirthDate(new java.util.Date());
			customerDao.insert(customer);
			System.out.println("CustomerDao - insert: "+customer);
			return customer;
		}
		return null;
	}
	
	private int delete(CustomerVo customerVo) {
		int rowsDeleted = customerDao.delete(customerVo.getCustId());
		System.out.println("CustomerDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
}

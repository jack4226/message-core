package com.legacytojava.message.jpa.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.jpa.model.GlobalVariable;
import com.legacytojava.message.jpa.service.GlobalVariableService;
import com.legacytojava.message.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class GlobalVariableTest {
	static Logger logger = Logger.getLogger(GlobalVariableTest.class);
	
	final String testVariableName = "CurrentDate";
	
	@BeforeClass
	public static void GlobalVariablePrepare() {
	}

	@Autowired
	GlobalVariableService service;

	@Test
	public void globalVariableService1() {
		GlobalVariable var1 = service.getByBestMatch(testVariableName, new Date(System.currentTimeMillis()));
		assertNotNull(var1);
		logger.info("GlobalVariable: " + StringUtil.prettyPrint(var1));

		GlobalVariable var2 = service.getByPrimaryKey(var1.getVariableName(), var1.getStartTime());
		assertTrue(var1.equals(var2));

		List<GlobalVariable> list1 = service.getCurrent();
		assertFalse(list1.isEmpty());
		
		List<GlobalVariable> list2 = service.getByVariableName(var1.getVariableName());
		assertFalse(list2.isEmpty());
		
		// test insert
		Date newTms = new Date(System.currentTimeMillis());
		GlobalVariable var3 = createNewInstance(var2);
		var3.setStartTime(newTms);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(var3.getVariableName(), newTms));
		// end of test insert
		
		service.delete(var3);
		assertNull(service.getByPrimaryKey(var3.getVariableName(), var3.getStartTime()));
		
		// test getByStatusid
		List<GlobalVariable> list3 = service.getByStatusId(var3.getStatusId());
		assertFalse(list3.isEmpty());
		for (GlobalVariable rec : list3) {
			logger.info(StringUtil.prettyPrint(rec));
		}
		
		// test deleteByVariableName
		GlobalVariable var4 = createNewInstance(var2);
		var4.setVariableName(var2.getVariableName() + "_v4");
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(var4.getVariableName()));
		assertNull(service.getByPrimaryKey(var4.getVariableName(), var4.getStartTime()));

		// test deleteByPrimaryKey
		GlobalVariable var5 = createNewInstance(var2);
		var5.setVariableName(var2.getVariableName() + "_v5");
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(var5.getVariableName(), var5.getStartTime()));
		assertNull(service.getByPrimaryKey(var5.getVariableName(), var5.getStartTime()));
		
		// test update
		GlobalVariable var6 = createNewInstance(var2);
		var6.setVariableName(var2.getVariableName() + "_v6");
		service.insert(var6);
		assertNotNull(service.getByPrimaryKey(var6.getVariableName(), var6.getStartTime()));
		var6.setVariableValue("new test value");
		service.update(var6);
		GlobalVariable var_updt = service.getByRowId(var6.getRowId());
		assertTrue("new test value".equals(var_updt.getVariableValue()));
		// end of test update
		
		service.delete(var6);
		try {
			service.getByRowId(var6.getRowId());
			fail();
		}
		catch (NoResultException e) {
			// expected
		}
	}
	
	private GlobalVariable createNewInstance(GlobalVariable orig) {
		GlobalVariable dest = new GlobalVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}

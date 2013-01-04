package com.legacytojava.message.jpa.test;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
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
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class GlobalVariableTest {

	final String testVariableName = "CurrentDate";
	
	@BeforeClass
	public static void GlobalVariablePrepare() {
	}

	@Autowired
	GlobalVariableService service;

	@Test
	public void globalVariableService() {
		GlobalVariable var1 = service.getByBestMatch(testVariableName, new Timestamp(System.currentTimeMillis()));
		assertNotNull(var1);
		System.out.println("GlobalVariable: " + var1);

		GlobalVariable var2 = service.getByPrimaryKey(var1.getVariableName(), var1.getStartTime());
		assertNotNull(var1.equals(var2));

		List<GlobalVariable> list1 = service.getByVariableName(var1.getVariableName());
		assertFalse(list1.isEmpty());
		
		List<GlobalVariable> list2 = service.getCurrent();
		assertFalse(list2.isEmpty());
		
		Timestamp newTms = new Timestamp(System.currentTimeMillis());
		GlobalVariable newvar = new GlobalVariable();
		try {
			BeanUtils.copyProperties(newvar, var2);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		newvar.setStartTime(newTms);
		System.out.println(StringUtil.prettyPrint(var2));
		System.out.println(StringUtil.prettyPrint(newvar));
		service.insert(newvar);
		
		assertNotNull(service.getByPrimaryKey(newvar.getVariableName(), newTms));
		assertNotNull(service.getByPrimaryKey(var2.getVariableName(), var2.getStartTime()));
	}
}

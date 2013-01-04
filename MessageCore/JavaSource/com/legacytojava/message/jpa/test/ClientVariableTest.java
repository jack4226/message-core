package com.legacytojava.message.jpa.test;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.jpa.model.ClientVariable;
import com.legacytojava.message.jpa.service.ClientVariableService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class ClientVariableTest {

	final String testVariableName = "CurrentDate";
	final String testClientId = Constants.DEFAULT_CLIENTID;
	
	@BeforeClass
	public static void ClientVariablePrepare() {
	}

	@Autowired
	ClientVariableService service;

	@Test
	public void globalVariableService() {
		ClientVariable var1 = service.getByBestMatch(testClientId, testVariableName, new Timestamp(System.currentTimeMillis()));
		assertNotNull(var1);
		System.out.println("ClientVariable: " + var1);

		ClientVariable var2 = service.getByPrimaryKey(var1.getClientId(), var1.getVariableName(), var1.getStartTime());
		assertNotNull(var1.equals(var2));

		List<ClientVariable> list1 = service.getByVariableName(var1.getVariableName());
		assertFalse(list1.isEmpty());
		
		List<ClientVariable> list2 = service.getCurrentByClientId(testClientId);
		assertFalse(list2.isEmpty());
	}
}

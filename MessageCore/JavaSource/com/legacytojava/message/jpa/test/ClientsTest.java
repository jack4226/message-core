package com.legacytojava.message.jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.jpa.model.Clients;
import com.legacytojava.message.jpa.service.ClientsService;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class ClientsTest {

	@BeforeClass
	public static void ClientsPrepare() {
	}

	@Autowired
	ClientsService service;

	@Test
	public void ClientsService() {
		List<Clients> list = service.getAll();
		assertFalse(list.isEmpty());
		
		Clients client = service.getByClientId(Constants.DEFAULT_CLIENTID);
		assertNotNull(client);
		
		assertTrue(client.getSystemId().equals(service.getSystemId()));
		assertTrue(client.getSystemKey().equals(service.getSystemKey()));

		client.setUpdtUserId("JpaTest");
		service.update(client);
		
		Clients tkn = service.getByClientId(Constants.DEFAULT_CLIENTID);
		assertTrue("JpaTest".equals(tkn.getUpdtUserId()));
		
//		tkn.setClientId(Constants.DEFAULT_CLIENTID + "_2");
//		service.insert(tkn);
//		
//		Clients tkn2 = service.getByClientId(tkn.getClientId());
//		assertNotNull(tkn2);
//		
//		service.delete(tkn2.getClientId());
	}
}

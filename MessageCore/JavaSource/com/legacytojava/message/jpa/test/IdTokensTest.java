package com.legacytojava.message.jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.jpa.model.IdTokens;
import com.legacytojava.message.jpa.service.IdTokensService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class IdTokensTest {

	@PersistenceContext
	private EntityManager entityManager;

	@BeforeClass
	public static void IdTokensPrepare() {
	}

	@Test
	public void selectIdTokens() {
		Query q = entityManager.createQuery("select t from IdTokens t");
		@SuppressWarnings("unchecked")
		List<IdTokens> tokens = q.getResultList();
		for (IdTokens token : tokens) {
			System.out.println(token);
		}
		System.out.println("Size: " + tokens.size());
		entityManager.close();
		assertFalse(tokens.isEmpty());
	}

	@Autowired
	IdTokensService service;

	@Test
	public void idTokensService() {
		IdTokens idTokens = service.getByClientId("System");
		assertNotNull(idTokens);
		
		List<IdTokens> list = service.getAll();
		assertFalse(list.isEmpty());
		
		idTokens.setUpdtUserId("JpaTest");
		service.update(idTokens);
		
		IdTokens tkn = service.getByClientId("System");
		assertTrue("JpaTest".equals(tkn.getUpdtUserId()));
		
		tkn.setClientId("JBatchCorp");
		service.insert(tkn);
		
		IdTokens tkn2 = service.getByClientId("JBatchCorp");
		assertNotNull(tkn2);
		
		service.delete(tkn2.getClientId());
	}
}

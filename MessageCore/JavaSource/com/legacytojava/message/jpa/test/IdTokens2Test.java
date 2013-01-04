package com.legacytojava.message.jpa.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.jpa.model.IdTokens;
import com.legacytojava.message.jpa.service.IdTokensService;

public class IdTokens2Test {

	private static final String PERSISTENCE_UNIT_NAME = "message_core";

	private static PlatformTransactionManager txmgr;
	private static TransactionStatus status;
	
	@BeforeClass
	public static void IdTokensPrepare() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("idtokens_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		status = txmgr.getTransaction(def);
	}

	@AfterClass
	public static void IdTokenTeardown() {
		txmgr.rollback(status);
	}

	/*
	 * load entity manager factory by spring as a spring bean
	 */
	@Test(expected=RollbackException.class)
	public void springEntityManager() {
		//EntityManagerFactory emf = (EntityManagerFactory) JbMain.getBatchAppContext().getBean("entityManagerFactory");
		EntityManagerFactory emf = SpringUtil.getAppContext().getBean(LocalContainerEntityManagerFactoryBean.class).getObject();
		EntityManager entityManager = emf.createEntityManager();
		// Read the existing entries and write to console
		Query q = entityManager.createQuery("select t from IdTokens t");
		@SuppressWarnings("unchecked")
		List<IdTokens> tokens = q.getResultList();
		try {
			for (IdTokens token : tokens) {
				System.out.println(token);
				// update record
				entityManager.getTransaction().begin();
				if ("SysAdmin".equalsIgnoreCase(token.getUpdtUserId())) {
					token.setUpdtUserId("admin");
				}
				else {
					token.setUpdtUserId("SysAdmin");
				}
				/*
				 * UpdtTime field is used for Optimistic Locking
				 */
				token.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
				entityManager.persist(token);
				entityManager.getTransaction().commit();
			}
			System.out.println("Size: " + tokens.size());
		}
		finally {
			entityManager.close();
		}
	}

	@Test
	public void idTokensService1() {
		IdTokensService service = (IdTokensService) SpringUtil.getAppContext().getBean("idTokensService");

		List<IdTokens> list = service.getAll();
		assertFalse(list.isEmpty());

		IdTokens idTokens = service.getByClientId("System");
		assertNotNull(idTokens);
		
		idTokens.setUpdtUserId("JpaTest");
		service.update(idTokens);
		
		IdTokens tkn = service.getByClientId(idTokens.getClientId());
		assertTrue("JpaTest".equals(tkn.getUpdtUserId()));
		
		tkn.setClientId("JBatchCorp");
		service.insert(tkn);
		
		IdTokens tkn2 = service.getByClientId(tkn.getClientId());
		assertNotNull(tkn2);
		
		service.delete(tkn2.getClientId());
	}
	
	@Test(expected=javax.persistence.NoResultException.class)
	public void idTokensService2() {
		IdTokensService service = (IdTokensService) SpringUtil.getAppContext().getBean("idTokensService");

		IdTokens tkn = service.getByClientId("System");
		assertNotNull(tkn);
		
		tkn.setClientId("JBatchCorp");
		service.insert(tkn);
		
		IdTokens tkn2 = service.getByClientId(tkn.getClientId());
		assertNotNull(tkn2);
		
		service.delete(tkn2.getClientId());
		service.getByClientId(tkn2.getClientId());
	}

	/* 
	 * !!! load entity manager factory by EclipseLink from persistence.xml
	 */
	@Ignore
	public void persistenceXmlfile() {
		HashMap<Object,Object> properties = new HashMap<Object,Object>();
		properties.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, "META-INF/jpa-persistence.xml");
		//properties.put(PersistenceUnitProperties.CLASSLOADER, this.getClass().getClassLoader());
		
		EntityManagerFactory emf = new PersistenceProvider().createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
		EntityManager entityManager = emf.createEntityManager();
		// Read the existing entries and write to console
		Query q = entityManager.createQuery("select t from IdTokens t");
		@SuppressWarnings("unchecked")
		List<IdTokens> tokens = q.getResultList();
		for (IdTokens token : tokens) {
			System.out.println(token);
			// update record
			entityManager.getTransaction().begin();
			if ("SysAdmin".equalsIgnoreCase(token.getUpdtUserId())) {
				token.setUpdtUserId("admin");
			}
			else {
				token.setUpdtUserId("SysAdmin");
			}
			token.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
			entityManager.persist(token);
			entityManager.getTransaction().commit();
		}
		System.out.println("Size: " + tokens.size());

		entityManager.close();
	}

}

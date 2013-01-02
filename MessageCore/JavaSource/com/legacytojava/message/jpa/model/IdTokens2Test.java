package com.legacytojava.message.jpa.model;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import junit.framework.TestCase;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.jbatch.JbMain;
import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.jpa.service.IdTokensService;

@Repository
@Transactional
public class IdTokens2Test extends TestCase {

	private static final String PERSISTENCE_UNIT_NAME = "message_core";

	@BeforeClass
	public static void IdTokensPrepare() {
		JbMain.getInstance();
	}

	/*
	 * load entity manager factory by spring as a spring bean
	 */
	@Test
	public void testSpringEntityManager() {
		//EntityManagerFactory emf = (EntityManagerFactory) JbMain.getBatchAppContext().getBean("entityManagerFactory");
		EntityManagerFactory emf = SpringUtil.getAppContext().getBean(LocalContainerEntityManagerFactoryBean.class).getObject();
		EntityManager entityManager = emf.createEntityManager();
		// Read the existing entries and write to console
		Query q = entityManager.createQuery("select t from IdTokens t");
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

	@Test
	@Transactional(propagation=Propagation.REQUIRED)
	public void testIdTokensService() {
		IdTokensService service = (IdTokensService) SpringUtil.getAppContext().getBean("idTokensService");
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

	/* 
	 * !!! load entity manager factory by EclipseLink from persistence.xml
	 */
	@Test
	public void restPersistenceXmlfile() {
		HashMap<Object,Object> properties = new HashMap<Object,Object>();
		properties.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, "META-INF/jpa-persistence.xml");
		//properties.put(PersistenceUnitProperties.CLASSLOADER, this.getClass().getClassLoader());
		
		EntityManagerFactory emf = new PersistenceProvider().createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
		EntityManager entityManager = emf.createEntityManager();
		// Read the existing entries and write to console
		Query q = entityManager.createQuery("select t from IdTokens t");
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

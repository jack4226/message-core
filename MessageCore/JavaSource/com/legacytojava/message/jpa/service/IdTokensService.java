package com.legacytojava.message.jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.jpa.model.IdTokens;

@Component("idTokensService")
@Transactional(propagation=Propagation.REQUIRED)
public class IdTokensService {
	static Logger logger = Logger.getLogger(IdTokensService.class);
	
	@PersistenceContext(unitName="message_core")
	EntityManager em;

	public IdTokens getByClientId(String clientId) {
		try {
			Query query = em.createQuery("select t from IdTokens t where t.clientId = :clientId");
			query.setParameter("clientId", clientId);
			IdTokens idTokens = (IdTokens) query.getSingleResult();
			em.lock(idTokens, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return idTokens;
		}
		finally {
		}
	}
	
	public List<IdTokens> getAll() {
		try {
			Query query = em.createQuery("select t from IdTokens t");
			@SuppressWarnings("unchecked")
			List<IdTokens> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(String clientId) {
		try {
			IdTokens record = getByClientId(clientId);
			if (record != null) {
				em.remove(record);
			}
		}
		finally {
		}
	}

	public void insert(IdTokens idTokens) {
		try {
			em.persist(idTokens);
		}
		finally {
		}
	}
	
	public void update(IdTokens idTokens) {
		try {
			em.persist(idTokens);
		}
		catch (OptimisticLockException e) {
			logger.error("OptimisticLockException caught", e);
			throw e;
		}
		finally {
		}
	}
}
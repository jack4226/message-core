package com.legacytojava.message.jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.jpa.model.Clients;
import com.legacytojava.message.util.StringUtil;

@Component("clientsService")
@Transactional(propagation=Propagation.REQUIRED)
public class ClientsService {
	static Logger logger = Logger.getLogger(ClientsService.class);
	
	@PersistenceContext(unitName="message_core")
	EntityManager em;

	public Clients getByClientId(String clientId) {
		try {
			Query query = em.createQuery("select t from Clients t where t.clientId = :clientId");
			query.setParameter("clientId", clientId);
			Clients client = (Clients) query.getSingleResult();
			em.lock(client, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return client;
		}
		finally {
		}
	}
	
	public Clients getByDomainName(String domainName) {
		try {
			Query query = em.createQuery("select t from Clients t where t.domainName = :domainName");
			query.setParameter("domainName", domainName);
			Clients clients = (Clients) query.getSingleResult();
			return clients;
		}
		finally {
		}
	}

	public List<Clients> getAll() {
		try {
			Query query = em.createQuery("select t from Clients t");
			@SuppressWarnings("unchecked")
			List<Clients> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public String getSystemId() {
		try {
			Query query = em.createQuery("select t.systemId from Clients t where t.clientId = :clientId");
			query.setParameter("clientId", Constants.DEFAULT_CLIENTID);
			String systemId = (String) query.getSingleResult();
			return systemId;
		}
		finally {
		}		
	}

	public String getSystemKey() {
		try {
			Query query = em.createQuery("select t.systemKey from Clients t where t.clientId = :clientId");
			query.setParameter("clientId", Constants.DEFAULT_CLIENTID);
			String systemKey = (String) query.getSingleResult();
			return systemKey;
		}
		finally {
		}		
	}

	public void delete(String clientId) {
		try {
			Clients record = getByClientId(clientId);
			if (record != null) {
				em.remove(record);
			}
		}
		finally {
		}
	}

	@Autowired
	private ReloadFlagsService reloadFlagsService;
	public void insert(Clients client) {
		try {
			validateClient(client);
			em.persist(client);
			reloadFlagsService.updateClientReloadFlag();
		}
		finally {
		}
	}
	
	public void update(Clients client) {
		insert(client);
	}
	
	private void validateClient(Clients client) {
		if (client.getUseTestAddress()) {
			if (StringUtil.isEmpty(client.getTestToAddr())) {
				throw new IllegalStateException("Test TO Address was null");
			}
		}
		if (client.getIsVerpAddressEnabled()) {
			if (StringUtil.isEmpty(client.getVerpInboxName())) {
				throw new IllegalStateException("VERP bounce inbox name was null");
			}
			if (StringUtil.isEmpty(client.getVerpRemoveInbox())) {
				throw new IllegalStateException("VERP remove inbox name was null");
			}
		}
	}

}

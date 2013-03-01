package com.legacytojava.message.jpa.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.jpa.model.ClientVariable;

@Component("clientVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class ClientVariableService {
	static Logger logger = Logger.getLogger(ClientVariableService.class);
	
	@PersistenceContext(unitName="MessageDB")
	EntityManager em;

	public ClientVariable getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"ClientVariable t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			ClientVariable record = (ClientVariable) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public ClientVariable getByPrimaryKey(String clientId, String variableName, Date startTime) {
		String sql = 
			"select t " +
			"from " +
				"ClientVariable t where t.clientId=:clientId and t.variableName=:variableName";
		if (startTime!=null) {
			sql += " and t.startTime=:starTtime ";
		}
		else {
			sql += " and t.startTime is null ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("clientId", clientId);
			query.setParameter("variableName", variableName);
			if (startTime != null) {
				query.setParameter("starTtime", startTime);
			}
			@SuppressWarnings("unchecked")
			List<ClientVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}

	public ClientVariable getByBestMatch(String clientId, String variableName, Date startTime) {
		if (startTime!=null) {
			startTime = new Date(System.currentTimeMillis());
		}
		String sql = 
				"select t " +
				"from " +
					"ClientVariable t where t.clientId=:clientId and t.variableName=:variableName " +
					" and (t.startTime<=:startTime or t.startTime is null) " +
					" order by t.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			query.setParameter("startTime", startTime);
			query.setParameter("clientId", clientId);
			@SuppressWarnings("unchecked")
			List<ClientVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}

	public List<ClientVariable> getByVariableName(String variableName) {
		String sql = 
				"select t " +
				" from " +
					" ClientVariable t where t.variableName=:variableName " +
				" order by t.clientId, t.startTime asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			@SuppressWarnings("unchecked")
			List<ClientVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<ClientVariable> getCurrentByClientId(String clientId) {
		String sql = 
				"select a.* " +
					" from ClientVariable a " +
					" inner join ( " +
					"  select b.clientId, b.variableName as variableName, max(b.startTime) as maxTime " +
					"   from ClientVariable b " +
					"   where b.statusId = ? and b.startTime<=? and b.clientId=? " +
					"   group by b.variableName " +
					" ) as c " +
					"  on a.variableName=c.variableName and a.startTime=c.maxTime and a.clientId=c.clientId " +
					" order by a.rowid asc ";
		try {
			Query query = em.createNativeQuery(sql, ClientVariable.class);
			query.setParameter(1, StatusIdCode.ACTIVE);
			query.setParameter(2, new Date(System.currentTimeMillis()));
			query.setParameter(3, clientId);
			@SuppressWarnings("unchecked")
			List<ClientVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public void delete(ClientVariable var) {
		if (var == null) return;
		try {
			em.remove(var);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(String clientId, String variableName, Date startTime) {
		String sql = 
				"delete from ClientVariable t where t.variableName=:variableName and t.startTime=:startTime " +
				" and t.clientId=:clientId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			query.setParameter("startTime", startTime);
			query.setParameter("clientId", clientId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByVariableName(String variableName) {
		String sql = 
				"delete from ClientVariable t where t.variableName=:variableName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByClientId(String clientId) {
		String sql = 
				"delete from ClientVariable t where t.clientId=:clientId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("clientId", clientId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(ClientVariable var) {
		insert(var);
	}

	public void insert(ClientVariable var) {
		try {
			em.persist(var);
			em.flush();
		}
		finally {
		}
	}

}

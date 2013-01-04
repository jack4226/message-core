package com.legacytojava.message.jpa.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.jpa.model.GlobalVariable;

@Component("globalVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class GlobalVariableService {
	static Logger logger = Logger.getLogger(GlobalVariableService.class);
	
	@PersistenceContext(unitName="message_core")
	EntityManager em;

	public GlobalVariable getByPrimaryKey(String variableName, Timestamp startTime) {
		String sql = 
			"select t " +
			"from " +
				"GlobalVariable t where t.variableName=:variableName";
		if (startTime!=null) {
			sql += " and t.startTime=:startTime ";
		}
		else {
			sql += " and t.startTime is null ";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			if (startTime != null) {
				query.setParameter("startTime", startTime);
			}
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}

	public GlobalVariable getByBestMatch(String variableName, Timestamp startTime) {
		if (startTime!=null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		String sql = 
				"select t " +
				"from " +
					"GlobalVariable t where t.variableName=:variableName " +
					" and (t.startTime<=:startTime or t.startTime is null) " +
					" order by t.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			query.setParameter("startTime", startTime);
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.setMaxResults(1).getResultList();
			if (!list.isEmpty()) {
				return list.get(0);
			}
			return null;
		}
		finally {
		}
	}
	public List<GlobalVariable> getByVariableName(String variableName) {
		String sql = 
				"select t " +
				" from " +
					" GlobalVariable t where t.variableName=:variableName " +
				" order by t.startTime asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<GlobalVariable> getCurrent() {
		String sql = 
				"select a.* " +
					" from GlobalVariable a " +
					" inner join ( " +
					"  select b.variableName as variableName, max(b.startTime) as maxTime " +
					"   from GlobalVariable b " +
					"   where b.statusId = ? and b.startTime<=? " +
					"   group by b.variableName " +
					" ) as c " +
					"  on a.variableName=c.variableName and a.startTime=c.maxTime " +
					" order by a.variableName asc ";
		try {
			Query query = em.createNativeQuery(sql, GlobalVariable.class);
			query.setParameter(1, StatusIdCode.ACTIVE);
			query.setParameter(2, new Timestamp(System.currentTimeMillis()));
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<GlobalVariable> getByStatusId(String statusId) {
		String sql = 
				"select t " +
					" from GlobalVariable t " +
					" where t.statusId = :statusId and t.startTime<=:startTime" +
					" order by t.variableName asc, t.startTime desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("statusId", statusId);
			query.setParameter("starTtime", new Timestamp(System.currentTimeMillis()));
			@SuppressWarnings("unchecked")
			List<GlobalVariable> list = query.getResultList();
			List<GlobalVariable> list2 = new ArrayList<GlobalVariable>();
			String varName = null;
			for (Iterator<GlobalVariable> it=list.iterator(); it.hasNext(); ) {
				GlobalVariable var = it.next();
				if (!var.getVariableName().equals(varName)) {
					list2.add(var);
					varName = var.getVariableName();
				}
			}
			return list2;
		}
		finally {
		}
	}

	public void deleteByPrimaryKey(String variableName, Timestamp startTime) {
		try {
			GlobalVariable record = getByPrimaryKey(variableName, startTime);
			if (record != null) {
				em.remove(record);
			}
		}
		finally {
		}
	}

	public int deleteByVariableName(String variableName) {
		String sql = 
				"delete from GlobalVariable t where t.variableName=:variableName ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("variableName", variableName);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(GlobalVariable var) {
		insert(var);
	}

	public void insert(GlobalVariable var) {
		try {
			em.persist(var);
			em.flush(); // Not required, useful for seeing what is happening
		}
		finally {
		}
	}

}

package com.legacytojava.message.jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.jpa.model.ReloadFlags;

@Component("reloadFlagsService")
@Transactional(propagation=Propagation.REQUIRED)
public class ReloadFlagsService {
	static Logger logger = Logger.getLogger(ReloadFlagsService.class);
	
	@PersistenceContext(unitName="MessageDB")
	EntityManager em;

	public ReloadFlags select() {
		return selectWithRepair(0);
	}
	
	private ReloadFlags selectWithRepair(int retry) {
		try {
			Query query = em.createQuery("select t from ReloadFlags t");
			@SuppressWarnings("unchecked")
			List<ReloadFlags> list = query.setMaxResults(1).getResultList();
			if (list.size() > 0) {
				return list.get(0);
			}
			else if (retry < 1) {
				repair();
				return selectWithRepair(++retry);
			}
			else {
				throw new RuntimeException("Internal error, contact programming.");
			}
		}
		finally {
		}
	}
	
	private void repair() {
		ReloadFlags record = new ReloadFlags();
		update(record);
	}

	public void update(ReloadFlags record) {
		try {
			em.persist(record);
		}
		finally {
		}
	}
	
	public void updateClientReloadFlag() {
		ReloadFlags record = select();
		record.setClients(record.getClients()+1);
		update(record);
	}

	public void updateRuleReloadFlag() {
		ReloadFlags record = select();
		record.setRules(record.getRules()+1);
		update(record);
	}

	public void updateActionReloadFlag() {
		ReloadFlags record = select();
		record.setActions(record.getActions()+1);
		update(record);
	}

	public void updateTemplateReloadFlag() {
		ReloadFlags record = select();
		record.setTemplates(record.getTemplates()+1);
		update(record);
	}

	public void updateScheduleReloadFlag() {
		ReloadFlags record = select();
		record.setSchedules(record.getSchedules()+1);
		update(record);
	}

}

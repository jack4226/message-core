package ltj.message.dao.rule;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.bo.rule.RuleBase;
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.dao.client.ReloadFlagsDao;
import ltj.message.vo.rule.RuleLogicVo;

@Component("ruleLogicDao")
public class RuleLogicJdbcDao extends AbstractDao implements RuleLogicDao {
	
	private String getSelectClause() {
		String select = 
				"select " +
					"r.RowId, " +
					"r.RuleName, " +
					"r.RuleSeq, " +
					"r.RuleType, " +
					"r.StatusId, " +
					"r.StartTime, " +
					"r.MailType, " +
					"r.RuleCategory, " +
					"r.IsSubRule, " +
					"r.BuiltinRule, " +
					"r.Description, " +
					"count(s.SubRuleName) as SubRuleCount " +
				" from RuleLogic r " +
					" left outer join RuleSubRuleMap s on r.RuleName=s.RuleName ";
		return select;
	}
	
	private String getGroupByClause() {
		String groupBy = " group by " +
				"r.RowId, " +
				"r.RuleName, " +
				"r.RuleSeq, " +
				"r.RuleType, " +
				"r.StatusId, " +
				"r.StartTime, " +
				"r.MailType, " +
				"r.RuleCategory, " +
				"r.IsSubRule, " +
				"r.BuiltinRule, " +
				"r.Description ";
		return groupBy;
	}

	@Override
	public RuleLogicVo getByPrimaryKey(String ruleName, int ruleSeq) {
		String sql = getSelectClause() +
			" where r.ruleName=? and r.RuleSeq=? " +
			getGroupByClause();
		
		Object[] parms = new Object[] {ruleName};
		try {
			RuleLogicVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<RuleLogicVo> getByRuleName(String ruleName) {
		String sql = getSelectClause() +
			" where r.ruleName=? " +
			getGroupByClause() +
			" order by r.RuleSeq ";
		
		Object[] parms = new Object[] {ruleName};
		
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	@Override
	public int getNextRuleSequence() {
		String sql = 
			"select max(RuleSeq) from RuleLogic";

		int nextSeq = getJdbcTemplate().queryForObject(sql, Integer.class);
		return (nextSeq + 1);
	}
	
	@Override
	public List<RuleLogicVo> getActiveRules() {
		String sql = getSelectClause() +
			" where r.statusId=? and r.startTime<=? " +
			getGroupByClause() +
			" order by r.ruleCategory asc, r.ruleSeq asc, r.ruleName asc ";
		Object[] parms = new Object[] {StatusId.ACTIVE.value(), new Timestamp(System.currentTimeMillis())};
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	@Override
	public List<RuleLogicVo> getAll(boolean builtInRule) {
		String sql = getSelectClause();
		
		if (builtInRule) {
			sql += " where r.BuiltInRule=? and r.IsSubRule!='" + Constants.Y + "' ";
		}
		else {
			sql += " where r.BuiltInRule!=? ";
		}
		sql += getGroupByClause();
		sql += " order by r.ruleCategory asc, r.ruleSeq asc, r.ruleName asc ";
		List<String> fields = new ArrayList<>();
		fields.add(Constants.Y);
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, fields.toArray(), 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	@Override
	public List<RuleLogicVo> getAllSubRules(boolean excludeBuiltIn) {
		String sql = 
			"select *, 0 as SubRuleCount " +
			" from RuleLogic " +
				" where IsSubRule=? ";
		List<String> fields = new ArrayList<>();
		fields.add(Constants.Y);
		if (excludeBuiltIn) {
			sql += " and BuiltInRule!=? ";
			fields.add(Constants.Y);
		}
		
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, fields.toArray(),
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	@Override
	public List<String> getBuiltinRuleNames4Web() {
		String sql = 
			"select distinct(RuleName) " +
			" from RuleLogic " +
			" where BuiltInRule=? and IsSubRule!=? and RuleCategory=? " +
			" order by RuleName ";
		
		List<String> fields = new ArrayList<>();
		fields.add(Constants.Y);
		fields.add(Constants.Y);
		fields.add(RuleBase.MAIN_RULE);
		List<String> list = getJdbcTemplate().queryForList(sql, fields.toArray(), String.class);
		return list;
	}
	
	@Override
	public List<String> getCustomRuleNames4Web() {
		String sql = 
			"select distinct(RuleName) " +
			" from RuleLogic " +
			" where BuiltInRule!=? and IsSubRule!=? and RuleCategory=? " +
			" order by RuleName ";

		List<String> fields = new ArrayList<>();
		fields.add(Constants.Y);
		fields.add(Constants.Y);
		fields.add(RuleBase.MAIN_RULE);
		List<String> list = getJdbcTemplate().queryForList(sql, fields.toArray(), String.class);
		return list;
	}
	
	@Override
	public synchronized int update(RuleLogicVo ruleLogicVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleLogicVo);
		String sql = MetaDataUtil.buildUpdateStatement("RuleLogic", ruleLogicVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleLogicVo.setOrigRuleName(ruleLogicVo.getRuleName());
		ruleLogicVo.setOrigRuleSeq(ruleLogicVo.getRuleSeq());
		updateReloadFlags();
		return rowsUpadted;
	}
	
	@Override
	public synchronized int deleteByPrimaryKey(String ruleName, int ruleSeq) {
		String sql = 
			"delete from RuleLogic where RuleName=? and RuleSeq=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(ruleName);
		fields.add(ruleSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	@Override
	public synchronized int insert(RuleLogicVo ruleLogicVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleLogicVo);
		String sql = MetaDataUtil.buildInsertStatement("RuleLogic", ruleLogicVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleLogicVo.setRowId(retrieveRowId());
		ruleLogicVo.setOrigRuleName(ruleLogicVo.getRuleName());
		ruleLogicVo.setOrigRuleSeq(ruleLogicVo.getRuleSeq());
		updateReloadFlags();
		return rowsInserted;
	}
	
	private void updateReloadFlags() {
		getReloadFlagsDao().updateRuleReloadFlag();
	}

	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}
}

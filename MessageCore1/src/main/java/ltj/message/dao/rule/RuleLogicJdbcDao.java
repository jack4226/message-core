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
					"r.row_id, " +
					"r.rule_name, " +
					"r.rule_seq, " +
					"r.rule_type, " +
					"r.status_id, " +
					"r.start_time, " +
					"r.mail_type, " +
					"r.rule_category, " +
					"r.sub_rule, " +
					"r.built_in_rule, " +
					"r.description, " +
					"count(s.sub_rule_name) as SubRuleCount " +
				" from rule_logic r " +
					" left outer join rule_subrule_map s on r.rule_name=s.rule_name ";
		return select;
	}
	
	private String getGroupByClause() {
		String groupBy = " group by " +
				"r.row_id, " +
				"r.rule_name, " +
				"r.rule_seq, " +
				"r.rule_type, " +
				"r.status_id, " +
				"r.start_time, " +
				"r.mail_type, " +
				"r.rule_category, " +
				"r.sub_rule, " +
				"r.built_in_rule, " +
				"r.description ";
		return groupBy;
	}

	@Override
	public RuleLogicVo getByPrimaryKey(String ruleName, int ruleSeq) {
		String sql = getSelectClause() +
			" where r.rule_name=? and r.rule_seq=? " +
			getGroupByClause();
		
		Object[] parms = new Object[] {ruleName, ruleSeq};
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
	public RuleLogicVo getByRowId(int rowId) {
		String sql = getSelectClause() +
				" where r.row_id=? " +
				getGroupByClause();
			
		Object[] parms = new Object[] {rowId};
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
	public RuleLogicVo getByRuleName(String ruleName) {
		String sql = getSelectClause() +
			" where r.rule_name=? ";
		
		Object[] parms = new Object[] {ruleName};
		
		RuleLogicVo vo = getJdbcTemplate().queryForObject(sql, parms, 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return vo;
	}
	
	@Override
	public int getNextRuleSequence() {
		String sql = 
			"select max(rule_seq) from rule_logic";

		int nextSeq = getJdbcTemplate().queryForObject(sql, Integer.class);
		return (nextSeq + 1);
	}
	
	@Override
	public List<RuleLogicVo> getActiveRules() {
		String sql = getSelectClause() +
			" where r.status_id=? and r.start_time<=? " +
			getGroupByClause() +
			" order by r.rule_category asc, r.rule_seq asc, r.rule_name asc ";
		Object[] parms = new Object[] {StatusId.ACTIVE.value(), new Timestamp(System.currentTimeMillis())};
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	@Override
	public List<RuleLogicVo> getAll(boolean builtInRule) {
		String sql = getSelectClause();
		
		if (builtInRule) {
			sql += " where r.built_in_rule=? and r.sub_rule!=true ";
		}
		else {
			sql += " where r.built_in_rule!=? ";
		}
		sql += getGroupByClause();
		sql += " order by r.rule_category asc, r.rule_seq asc, r.rule_name asc ";
		List<Object> fields = new ArrayList<>();
		fields.add(true);
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, fields.toArray(), 
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	@Override
	public List<RuleLogicVo> getAllSubRules(boolean excludeBuiltIn) {
		String sql = 
			"select *, 0 as SubRuleCount " +
			" from rule_logic " +
				" where sub_rule=? ";
		List<Object> fields = new ArrayList<>();
		fields.add(true);
		if (excludeBuiltIn) {
			sql += " and built_in_rule!=? ";
			fields.add(true);
		}
		
		List<RuleLogicVo> list = getJdbcTemplate().query(sql, fields.toArray(),
				new BeanPropertyRowMapper<RuleLogicVo>(RuleLogicVo.class));
		return list;
	}
	
	@Override
	public boolean getHasSubRules(String ruleName) {
		String sql = 
			"select count(*) from rule_logic rl, rule_subrule_map m "
			+ "where rl.rule_name = m.rule_name "
			+ "and rl.rule_name = ?";
		List<Object> fields = new ArrayList<>();
		fields.add(ruleName);
		
		int rows = getJdbcTemplate().queryForObject(sql, fields.toArray(), Integer.class);
		return (rows > 0);
	}
	
	@Override
	public List<String> getBuiltinRuleNames4Web() {
		String sql = 
			"select distinct(rule_name) " +
			" from rule_logic " +
			" where built_in_rule=? and sub_rule!=? and rule_category=? " +
			" order by rule_name ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(true);
		fields.add(true);
		fields.add(RuleBase.MAIN_RULE);
		List<String> list = getJdbcTemplate().queryForList(sql, fields.toArray(), String.class);
		return list;
	}
	
	@Override
	public List<String> getCustomRuleNames4Web() {
		String sql = 
			"select distinct(rule_name) " +
			" from rule_logic " +
			" where built_in_rule!=? and sub_rule!=? and rule_category=? " +
			" order by rule_name ";

		List<Object> fields = new ArrayList<>();
		fields.add(true);
		fields.add(true);
		fields.add(RuleBase.MAIN_RULE);
		List<String> list = getJdbcTemplate().queryForList(sql, fields.toArray(), String.class);
		return list;
	}
	
	@Override
	public synchronized int update(RuleLogicVo ruleLogicVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleLogicVo);
		String sql = MetaDataUtil.buildUpdateStatement("rule_logic", ruleLogicVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleLogicVo.setOrigRuleName(ruleLogicVo.getRuleName());
		ruleLogicVo.setOrigRuleSeq(ruleLogicVo.getRuleSeq());
		updateReloadFlags();
		return rowsUpadted;
	}
	
	@Override
	public synchronized int deleteByPrimaryKey(String ruleName) {
		String sql = 
			"delete from rule_logic where rule_name=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(ruleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	@Override
	public synchronized int insert(RuleLogicVo ruleLogicVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleLogicVo);
		String sql = MetaDataUtil.buildInsertStatement("rule_logic", ruleLogicVo);
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

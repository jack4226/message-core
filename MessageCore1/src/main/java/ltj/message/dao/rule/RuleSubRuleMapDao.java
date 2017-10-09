package ltj.message.dao.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.dao.client.ReloadFlagsDao;
import ltj.message.vo.rule.RuleSubRuleMapVo;

@Component("ruleSubRuleMapDao")
public class RuleSubRuleMapDao extends AbstractDao {
	
	public RuleSubRuleMapVo getByPrimaryKey(String ruleName, String subRuleName) {
		String sql = 
			"select * " +
			"from " +
				"rule_subrule_map where rule_name=? and sub_rule_name=? ";
		
		Object[] parms = new Object[] {ruleName, subRuleName};
		try {
			RuleSubRuleMapVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleSubRuleMapVo>(RuleSubRuleMapVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RuleSubRuleMapVo> getByRuleName(String ruleName) {
		String sql = 
			"select * " +
			" from " +
				" rule_subrule_map where rule_name=? " +
			" order by sub_rule_seq asc ";
		
		Object[] parms = new Object[] {ruleName};
		List<RuleSubRuleMapVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleSubRuleMapVo>(RuleSubRuleMapVo.class));
		return list;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, String subRuleName) {
		String sql = 
			"delete from rule_subrule_map where rule_name=? and sub_rule_name=? ";
		
		List<String> fields = new ArrayList<>();
		fields.add(ruleName);
		fields.add(subRuleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from rule_subrule_map where rule_name=? ";
		
		List<String> fields = new ArrayList<>();
		fields.add(ruleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int update(RuleSubRuleMapVo ruleSubRuleMapVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleSubRuleMapVo);
		String sql = MetaDataUtil.buildUpdateStatement("rule_subrule_map", ruleSubRuleMapVo);
		int rowsUpdated = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		updateReloadFlags();
		return rowsUpdated;
	}
	
	public synchronized int insert(RuleSubRuleMapVo ruleSubRuleMapVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleSubRuleMapVo);
		String sql = MetaDataUtil.buildInsertStatement("rule_subrule_map", ruleSubRuleMapVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleSubRuleMapVo.setRowId(retrieveRowId());
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

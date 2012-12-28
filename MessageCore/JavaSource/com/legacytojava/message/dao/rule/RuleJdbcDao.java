package com.legacytojava.message.dao.rule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.legacytojava.message.vo.rule.RuleElementVo;
import com.legacytojava.message.vo.rule.RuleLogicVo;
import com.legacytojava.message.vo.rule.RuleSubRuleMapVo;
import com.legacytojava.message.vo.rule.RuleVo;

public class RuleJdbcDao implements RuleDao {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private RuleLogicDao ruleLogicDao;
	private RuleElementDao ruleElementDao;
	private RuleSubRuleMapDao ruleSubRuleMapDao;
	
	private static final class RuleMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			RuleVo ruleVo = new RuleVo();
			
			ruleVo.setRuleName(rs.getString("RuleName"));
			
			RuleLogicVo ruleLogicVo = new RuleLogicVo();
			ruleLogicVo.setRuleName(rs.getString("RuleName"));
			ruleLogicVo.setRuleSeq(rs.getInt("RuleSeq"));
			ruleLogicVo.setRuleType(rs.getString("RuleType"));
			ruleLogicVo.setStatusId(rs.getString("StatusId"));
			ruleLogicVo.setStartTime(rs.getTimestamp("StartTime"));
			ruleLogicVo.setMailType(rs.getString("MailType"));
			ruleLogicVo.setRuleCategory(rs.getString("RuleCategory"));
			ruleLogicVo.setIsSubRule(rs.getString("IsSubRule"));
			
			RuleElementVo ruleElementVo = new RuleElementVo();
			ruleElementVo.setRuleName(rs.getString("RuleName"));
			ruleElementVo.setElementSeq(rs.getInt("ElementSeq"));
			ruleElementVo.setDataName(rs.getString("DataName"));
			ruleElementVo.setHeaderName(rs.getString("HeaderName"));
			ruleElementVo.setCriteria(rs.getString("Criteria"));
			ruleElementVo.setCaseSensitive(rs.getString("CaseSensitive"));
			ruleElementVo.setTargetText(rs.getString("TargetText"));
			ruleElementVo.setExclusions(rs.getString("Exclusions"));
			ruleElementVo.setExclListProc(rs.getString("ExclListProc"));
			ruleElementVo.setDelimiter(rs.getString("Delimiter"));
			
			ruleVo.setRuleLogicVo(ruleLogicVo);
			ruleVo.getRuleElementVos().add(ruleElementVo);
			return ruleVo;
		}
	}

	public RuleVo getByPrimaryKey(String ruleName) {
		String sql = 
			"select " +
			"  logic.*, element.* " +
			" from ruleLogic as logic " +
			"  join ruleElement as element on logic.ruleName=element.ruleName "+
			" where logic.ruleName=? " +
			" order by element.elementSeq ";
		
		Object[] parms = new Object[] {ruleName};
		
		List<?> list = jdbcTemplate.query(sql, parms, new RuleMapper());
		if (list == null || list.size() == 0) {
			return null;
		}

		RuleVo vo = null;
		for (int i = 0; i < list.size(); i++) {
			RuleVo ruleVo = (RuleVo) list.get(i);
			if (vo == null) {
				vo = ruleVo;
			}
			else {
				vo.getRuleElementVos().addAll(ruleVo.getRuleElementVos());
			}
		}
		List<RuleSubRuleMapVo> subRules = getRuleSubRuleMapDao().getByRuleName(vo.getRuleName());
		vo.getRuleSubRuleVos().addAll(subRules);
		return (RuleVo) vo;
	}
	
	/**
	 * returns a list of RuleVo's
	 */
	public List<RuleVo> getActiveRules() {
		List<RuleLogicVo> ruleLogics = getRuleLogicDao().getActiveRules();
		List<RuleVo> ruleVos = new ArrayList<RuleVo>();
		for (int i = 0; i < ruleLogics.size(); i++) {
			RuleLogicVo ruleLogicVo = ruleLogics.get(i);
			RuleVo ruleVo = new RuleVo();
			ruleVo.setRuleName(ruleLogicVo.getRuleName());
			ruleVo.setRuleLogicVo(ruleLogicVo);
			List<RuleElementVo> elements = getRuleElementDao().getByRuleName(
					ruleLogicVo.getRuleName());
			ruleVo.getRuleElementVos().addAll(elements);
			List<RuleSubRuleMapVo> subRules = getRuleSubRuleMapDao().getByRuleName(
					ruleLogicVo.getRuleName());
			ruleVo.getRuleSubRuleVos().addAll(subRules);
			ruleVos.add(ruleVo);
		}
		return ruleVos;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}
	
	public RuleSubRuleMapDao getRuleSubRuleMapDao() {
		return ruleSubRuleMapDao;
	}

	public void setRuleSubRuleMapDao(RuleSubRuleMapDao ruleSubRuleMapDao) {
		this.ruleSubRuleMapDao = ruleSubRuleMapDao;
	}

	public RuleElementDao getRuleElementDao() {
		return ruleElementDao;
	}

	public void setRuleElementDao(RuleElementDao ruleElementDao) {
		this.ruleElementDao = ruleElementDao;
	}

	public RuleLogicDao getRuleLogicDao() {
		return ruleLogicDao;
	}

	public void setRuleLogicDao(RuleLogicDao ruleLogicDao) {
		this.ruleLogicDao = ruleLogicDao;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}

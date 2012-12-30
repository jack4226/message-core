package com.legacytojava.message.dao.rule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.rule.RuleSubRuleMapVo;

public class RuleSubRuleMapJdbcDao implements RuleSubRuleMapDao {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	private static final class RuleSubRuleMapMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			RuleSubRuleMapVo ruleSubRuleMapVo = new RuleSubRuleMapVo();
			
			ruleSubRuleMapVo.setRowId(rs.getInt("RowId"));
			ruleSubRuleMapVo.setRuleName(rs.getString("RuleName"));
			ruleSubRuleMapVo.setSubRuleName(rs.getString("SubRuleName"));
			ruleSubRuleMapVo.setSubRuleSeq(rs.getInt("SubRuleSeq"));
			
			return ruleSubRuleMapVo;
		}
	}

	public RuleSubRuleMapVo getByPrimaryKey(String ruleName, String subRuleName) {
		String sql = 
			"select * " +
			"from " +
				"RuleSubRuleMap where ruleName=? and subRuleName=? ";
		
		Object[] parms = new Object[] {ruleName, subRuleName};
		
		List<?> list = jdbcTemplate.query(sql, parms, new RuleSubRuleMapMapper());
		if (list.size()>0)
			return (RuleSubRuleMapVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<RuleSubRuleMapVo> getByRuleName(String ruleName) {
		String sql = 
			"select * " +
			" from " +
				" RuleSubRuleMap where ruleName=? " +
			" order by subRuleSeq asc ";
		
		Object[] parms = new Object[] {ruleName};
		List<RuleSubRuleMapVo> list = (List<RuleSubRuleMapVo>)jdbcTemplate.query(sql, parms, new RuleSubRuleMapMapper());
		return list;
	}
	
	public synchronized int deleteByPrimaryKey(String ruleName, String subRuleName) {
		String sql = 
			"delete from RuleSubRuleMap where ruleName=? and subRuleName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		fields.add(subRuleName);
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from RuleSubRuleMap where ruleName=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(ruleName);
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int update(RuleSubRuleMapVo ruleSubRuleMapVo) {
		String sql = 
			"update RuleSubRuleMap set " +
			"SubRuleSeq=?, " +
			"RuleName=?, " +
			"SubRuleName=? " +
			" where" +
				" RowId=?";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleSubRuleMapVo.getSubRuleSeq());
		fields.add(ruleSubRuleMapVo.getRuleName());
		fields.add(ruleSubRuleMapVo.getSubRuleName());
		fields.add(ruleSubRuleMapVo.getRowId());
		
		int rowsUpdated = jdbcTemplate.update(sql, fields.toArray());
		updateReloadFlags();
		return rowsUpdated;
	}
	
	public synchronized int insert(RuleSubRuleMapVo ruleSubRuleMapVo) {
		String sql = 
			"INSERT INTO RuleSubRuleMap (" +
			"RuleName, " +
			"SubRuleName, " +
			"SubRuleSeq " +
			") VALUES (" +
				" ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(ruleSubRuleMapVo.getRuleName());
		fields.add(ruleSubRuleMapVo.getSubRuleName());
		fields.add(ruleSubRuleMapVo.getSubRuleSeq());
		
		int rowsInserted = jdbcTemplate.update(sql, fields.toArray());
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
	
	protected int retrieveRowId() {
		return jdbcTemplate.queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}
}

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
import ltj.message.vo.rule.RuleElementVo;

@Component("ruleElementDao")
public class RuleElementJdbcDao extends AbstractDao implements RuleElementDao {
	
	@Override
	public RuleElementVo getByPrimaryKey(String ruleName, int elementSeq) {
		String sql = 
			"select * " +
			"from RuleElement " +
				" where ruleName=? and elementSeq=?";
		
		Object[] parms = new Object[] {ruleName, elementSeq};
		try {
			RuleElementVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RuleElementVo>(RuleElementVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<RuleElementVo> getAll() {
		String sql = 
			"select * " +
			" from " +
				" RuleElement " +
			" order by ruleName asc, elementSeq asc ";
		List<RuleElementVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<RuleElementVo>(RuleElementVo.class));
		return list;
	}
	
	@Override
	public List<RuleElementVo> getByRuleName(String ruleName) {
		String sql = 
			"select * " +
			" from " +
				" RuleElement " +
				" where ruleName = ? " +
			" order by elementSeq asc ";
		Object[] parms = new Object[] { ruleName };
		List<RuleElementVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RuleElementVo>(RuleElementVo.class));
		return list;
	}
	
	@Override
	public synchronized int update(RuleElementVo ruleElementVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleElementVo);
		String sql = MetaDataUtil.buildUpdateStatement("RuleElement", ruleElementVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		updateReloadFlags();
		return rowsUpadted;
	}
	
	@Override
	public synchronized int deleteByPrimaryKey(String ruleName, int elementSeq) {
		String sql = 
			"delete from RuleElement where ruleName=? and elementSeq=?";
		
		List<Object> fields = new ArrayList<>();
		fields.add(ruleName);
		fields.add(elementSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	@Override
	public synchronized int deleteByRuleName(String ruleName) {
		String sql = 
			"delete from RuleElement where ruleName=?";
		
		List<String> fields = new ArrayList<>();
		fields.add(ruleName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		updateReloadFlags();
		return rowsDeleted;
	}
	
	@Override
	public synchronized int insert(RuleElementVo ruleElementVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(ruleElementVo);
		String sql = MetaDataUtil.buildInsertStatement("RuleElement", ruleElementVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		ruleElementVo.setRowId(retrieveRowId());
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

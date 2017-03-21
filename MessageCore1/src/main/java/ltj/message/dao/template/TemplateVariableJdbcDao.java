package ltj.message.dao.template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.vo.template.TemplateVariableVo;

@Component("templateVariableDao")
public class TemplateVariableJdbcDao extends AbstractDao implements TemplateVariableDao {
	
	private static final Map<String, List<TemplateVariableVo>> currentVariablesCache = new HashMap<>();
	
	@Override
	public TemplateVariableVo getByPrimaryKey(String templateId, String clientId, String variableName,
			Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"template_variable where templateId=? and variableName=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(templateId);
		keys.add(variableName);
		if (clientId==null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and clientId=? ";
			keys.add(clientId);
		}
		if (startTime==null) {
			sql += " and startTime is null ";
		}
		else {
			sql += " and startTime=? ";
			keys.add(startTime);
		}
		
		Object[] parms = keys.toArray();
		try {
			TemplateVariableVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public TemplateVariableVo getByBestMatch(String templateId, String clientId, String variableName,
			Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"template_variable where templateId=? and variableName=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(templateId);
		keys.add(variableName);
		if (clientId==null) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and (clientId=? or clientId is null) ";
			keys.add(clientId);
		}
		if (startTime!=null) {
			startTime = new Timestamp(new java.util.Date().getTime());
		}
		sql += " and (startTime<=? or startTime is null) ";
		keys.add(startTime);
		sql += " order by clientId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public List<TemplateVariableVo> getByVariableName(String variableName) {
		String sql = 
			"select * " +
			" from " +
				" template_variable where variableName=? " +
			" order by templateId, clientId, startTime asc ";
		Object[] parms = new Object[] {variableName};
		List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
		return list;
	}
	
	@Override
	public List<TemplateVariableVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" template_variable where clientId=? " +
			" order by templateId, variableName, startTime ";
		Object[] parms = new Object[] { clientId };
		List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
		return list;
	}
	
	@Override
	public List<TemplateVariableVo> getCurrentByTemplateId(String templateId, String clientId) {
		if (!currentVariablesCache.containsKey(templateId+"."+clientId)) {
			String sql = 
				"select * " +
				" from template_variable as a " +
				" inner join ( " +
				"  select b.templateid, b.clientid, b.variablename, max(b.starttime) as maxtime " +
				"   from template_variable b " +
				"   where b.statusid=? and b.starttime<=? " +
				"    and b.templateid=? and b.clientid=? " +
				"   group by b.templateid, b.clientid, b.variablename " +
				" ) as c " +
				"  on a.variablename=c.variablename and a.starttime=c.maxtime " +
				"    and a.templateid=c.templateid and a.clientid=c.clientid " +
				" order by a.variableName asc ";
			Object[] parms = new Object[] { StatusId.ACTIVE.value(),
					new Timestamp(new java.util.Date().getTime()), templateId, clientId };
			List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms, 
					new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
			currentVariablesCache.put(templateId + "." + clientId, list);
		}
		
		List<TemplateVariableVo> list = currentVariablesCache.get(templateId+"."+clientId);
		return list;
	}
	
	@Override
	public List<TemplateVariableVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" template_variable where templateId=? " +
			" order by clientId, variableName, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<TemplateVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<TemplateVariableVo>(TemplateVariableVo.class));
		return list;
	}
	
	@Override
	public int update(TemplateVariableVo templateVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(templateVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("template_variable", templateVariableVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		if (rowsUpadted>0) {
			currentVariablesCache.remove(templateVariableVo.getTemplateId() + "." + templateVariableVo.getClientId());
		}
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(String templateId, String clientId, String variableName, Timestamp startTime) {
		String sql = 
			"delete from template_variable where templateId=? and clientId=? and variableName=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		fields.add(clientId);
		fields.add(variableName);
		if (startTime!=null) {
			sql += " and startTime=? ";
			fields.add(startTime);
		}
		else {
			sql += " and startTime is null ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted>0) {
			currentVariablesCache.remove(templateId+"."+clientId);
		}
		return rowsDeleted;
	}
	
	@Override
	public int deleteByVariableName(String variableName) {
		String sql = 
			"delete from template_variable where variableName=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted > 0) {
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	@Override
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from template_variable where clientId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted > 0) {
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	@Override
	public int deleteByTemplateId(String templateId) {
		String sql = 
			"delete from template_variable where templateId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted > 0) {
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	@Override
	public int insert(TemplateVariableVo templateVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(templateVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("template_variable", templateVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		templateVariableVo.setRowId(retrieveRowId());
		if (rowsInserted>0) {
			currentVariablesCache.remove(templateVariableVo.getTemplateId() + "." + templateVariableVo.getClientId());
		}
		return rowsInserted;
	}
}

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
				"template_variable where template_id=? and variable_name=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(templateId);
		keys.add(variableName);
		if (clientId==null) {
			sql += " and client_id is null ";
		}
		else {
			sql += " and client_id=? ";
			keys.add(clientId);
		}
		if (startTime==null) {
			sql += " and start_time is null ";
		}
		else {
			sql += " and start_time=? ";
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
				"template_variable where template_id=? and variable_name=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(templateId);
		keys.add(variableName);
		if (clientId==null) {
			sql += " and client_id is null ";
		}
		else {
			sql += " and (client_id=? or client_id is null) ";
			keys.add(clientId);
		}
		if (startTime!=null) {
			startTime = new Timestamp(new java.util.Date().getTime());
		}
		sql += " and (start_time<=? or start_time is null) ";
		keys.add(startTime);
		sql += " order by client_id desc, start_time desc ";
		
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
	public TemplateVariableVo getByRowId(long rowId) {
		String sql = "select * from template_variable where row_id=?";
		Object[] parms = new Object[] {rowId};
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
	public List<TemplateVariableVo> getByVariableName(String variableName) {
		String sql = 
			"select * " +
			" from " +
				" template_variable where variable_name=? " +
			" order by template_id, client_id, start_time asc ";
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
				" template_variable where client_id=? " +
			" order by template_id, variable_name, start_time ";
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
				"  select b.template_id, b.client_id, b.variable_name, max(b.start_time) as MaxTime " +
				"   from template_variable b " +
				"   where b.status_id=? and b.start_time<=? " +
				"    and b.template_id=? and b.client_id=? " +
				"   group by b.template_id, b.client_id, b.variable_name " +
				" ) as c " +
				"  on a.variable_name=c.variable_name and a.start_time=c.MaxTime " +
				"    and a.template_id=c.template_id and a.client_id=c.client_id " +
				" order by a.variable_name asc ";
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
				" template_variable where template_id=? " +
			" order by client_id, variable_name, start_time asc ";
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
			"delete from template_variable where template_id=? and client_id=? and variable_name=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		fields.add(clientId);
		fields.add(variableName);
		if (startTime!=null) {
			sql += " and start_time=? ";
			fields.add(startTime);
		}
		else {
			sql += " and start_time is null ";
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
			"delete from template_variable where variable_name=? ";
		
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
			"delete from template_variable where client_id=? ";
		
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
			"delete from template_variable where template_id=? ";
		
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

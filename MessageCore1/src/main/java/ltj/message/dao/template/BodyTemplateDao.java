package ltj.message.dao.template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.vo.template.BodyTemplateVo;

@Component("bodyTemplateDao")
public class BodyTemplateDao extends AbstractDao {
	
	public BodyTemplateVo getByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"body_template where template_id=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(templateId);
		if (StringUtils.isNotBlank(clientId)) {
			sql += " and client_id=? ";
			keys.add(clientId);
		}
		else {
			sql += " and client_id is null ";
		}
		if (startTime != null) {
			sql += " and start_time=? ";
			keys.add(startTime);
		}
		else {
			sql += " start_time is null ";
		}
		
		Object[] parms = keys.toArray();
		try {
			BodyTemplateVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public BodyTemplateVo getByBestMatch(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"body_template where template_id=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(templateId);
		if (StringUtils.isBlank(clientId)) {
			sql += " and client_id is null ";
		}
		else {
			sql += " and (client_id=? or client_id is null) ";
			keys.add(clientId);
		}
		if (startTime == null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		sql += " and (start_time<=? or start_time is null) ";
		keys.add(startTime);
		
		sql += " order by client_id desc, start_time desc ";
		
		Object[] parms = keys.toArray();
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	public BodyTemplateVo getByRowId(long rowId) {
		String sql = "select * from body_template where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			BodyTemplateVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<BodyTemplateVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" body_template where template_id=? " +
			" order by client_id, start_time asc ";
		Object[] parms = new Object[] {templateId};
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		return list;
	}
	
	public List<BodyTemplateVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" body_template where client_id=? " +
			" order by template_id, start_time asc ";
		Object[] parms = new Object[] {clientId};
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		return list;
	}
	
	public int update(BodyTemplateVo bodyTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(bodyTemplateVo);
		String sql = MetaDataUtil.buildUpdateStatement("body_template", bodyTemplateVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"delete from body_template where template_id=? and client_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		fields.add(clientId);
		if (startTime != null) {
			sql += " and start_time=? ";
			fields.add(startTime);
		}
		else {
			sql += " and start_time is null ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByTemplateId(String templateId) {
		String sql = 
			"delete from body_template where template_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from body_template where client_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(BodyTemplateVo bodyTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(bodyTemplateVo);
		String sql = MetaDataUtil.buildInsertStatement("body_template", bodyTemplateVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		bodyTemplateVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
}

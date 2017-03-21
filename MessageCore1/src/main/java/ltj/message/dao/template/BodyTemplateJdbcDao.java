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
public class BodyTemplateJdbcDao extends AbstractDao implements BodyTemplateDao {
	
	@Override
	public BodyTemplateVo getByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"body_template where templateId=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(templateId);
		if (StringUtils.isNotBlank(clientId)) {
			sql += " and clientId=? ";
			keys.add(clientId);
		}
		else {
			sql += " and clientId is null ";
		}
		if (startTime != null) {
			sql += " and startTime=? ";
			keys.add(startTime);
		}
		else {
			sql += " startTime is null ";
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
	
	@Override
	public BodyTemplateVo getByBestMatch(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"body_template where templateId=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(templateId);
		if (StringUtils.isBlank(clientId)) {
			sql += " and clientId is null ";
		}
		else {
			sql += " and (clientId=? or clientId is null) ";
			keys.add(clientId);
		}
		if (startTime == null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		sql += " and (startTime<=? or startTime is null) ";
		keys.add(startTime);
		
		sql += " order by clientId desc, startTime desc ";
		
		Object[] parms = keys.toArray();
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public List<BodyTemplateVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" body_template where templateId=? " +
			" order by clientId, startTime asc ";
		Object[] parms = new Object[] {templateId};
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		return list;
	}
	
	@Override
	public List<BodyTemplateVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" body_template where clientId=? " +
			" order by templateId, startTime asc ";
		Object[] parms = new Object[] {clientId};
		List<BodyTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<BodyTemplateVo>(BodyTemplateVo.class));
		return list;
	}
	
	@Override
	public int update(BodyTemplateVo bodyTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(bodyTemplateVo);
		String sql = MetaDataUtil.buildUpdateStatement("body_template", bodyTemplateVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"delete from body_template where templateId=? and clientId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		fields.add(clientId);
		if (startTime != null) {
			sql += " and startTime=? ";
			fields.add(startTime);
		}
		else {
			sql += " and startTime is null ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByTemplateId(String templateId) {
		String sql = 
			"delete from body_template where templateId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from body_template where clientId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(BodyTemplateVo bodyTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(bodyTemplateVo);
		String sql = MetaDataUtil.buildInsertStatement("body_template", bodyTemplateVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		bodyTemplateVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
}

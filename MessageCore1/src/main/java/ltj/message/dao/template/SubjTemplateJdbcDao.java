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
import ltj.vo.template.SubjTemplateVo;

@Component("subjTemplateDao")
public class SubjTemplateJdbcDao extends AbstractDao implements SubjTemplateDao {

	@Override
	public SubjTemplateVo getByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"subj_template where template_id=? and client_id=? ";
		
		Object[] parms;
		if (startTime != null) {
			sql += " and start_time<=? ";
			parms = new Object[] {templateId, clientId, startTime};
		}
		else {
			sql += " and start_time is null ";
			parms = new Object[] {templateId, clientId};
		}
		sql += " order by start_time asc ";
		try {
			SubjTemplateVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public SubjTemplateVo getByBestMatch(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"subj_template where template_id=? ";
		
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
		List<SubjTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public SubjTemplateVo getByRowId(long rowId) {
		String sql = "select * from subj_template where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			SubjTemplateVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<SubjTemplateVo> getByTemplateId(String templateId) {
		String sql = 
			"select * " +
			" from " +
				" subj_template where template_id=? " +
			" order by client_id, start_time asc ";
		Object[] parms = new Object[] {templateId};
		List<SubjTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
		return list;
	}
	
	@Override
	public List<SubjTemplateVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
			" from " +
				" subj_template where client_id=? " +
			" order by template_id, start_time asc ";
		Object[] parms = new Object[] {clientId};
		List<SubjTemplateVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<SubjTemplateVo>(SubjTemplateVo.class));
		return list;
	}
	
	@Override
	public int update(SubjTemplateVo subjTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(subjTemplateVo);
		String sql = MetaDataUtil.buildUpdateStatement("subj_template", subjTemplateVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(String templateId, String clientId, Timestamp startTime) {
		String sql = 
			"delete from subj_template where template_id=? and client_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		fields.add(clientId);
		if (startTime!=null) {
			sql += " and start_time=? ";
			fields.add(startTime);
		}
		else {
			sql += " and start_time is null ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByTemplateId(String templateId) {
		String sql = 
			"delete from subj_template where template_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(templateId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from subj_template where client_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(SubjTemplateVo subjTemplateVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(subjTemplateVo);
		String sql = MetaDataUtil.buildInsertStatement("subj_template", subjTemplateVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		subjTemplateVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}

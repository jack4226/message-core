package ltj.message.dao.smtp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.SmtpConnVo;

@Component("smtpServerDao")
public class SmtpServerJdbcDao extends AbstractDao implements SmtpServerDao {
	
	@Override
	public SmtpConnVo getByServerName(String serverName) {
		String sql = "select * from smtp_server where server_name=?";
		Object[] parms = new Object[] {serverName};
		try {
			SmtpConnVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public SmtpConnVo getByPrimaryKey(long rowId) {
		String sql = "select * from smtp_server where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			SmtpConnVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<SmtpConnVo> getAll(boolean onlyActive) {
		List<String> keys = new ArrayList<String>();
		String sql = "select * from smtp_server ";
		if (onlyActive) {
			sql += " where status_id=? ";
			keys.add(StatusId.ACTIVE.value());
		}
		sql += " order by server_name ";
		List<SmtpConnVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		return list;
	}
	
	@Override
	public List<SmtpConnVo> getAllForTrial(boolean onlyActive) {
		List<String> keys = new ArrayList<>();
		String sql = "select * from smtp_server ";
		if (onlyActive) {
			sql += " where status_id=? ";
			keys.add(StatusId.ACTIVE.value());
		}
		sql += " order by row_id limit 1 ";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<SmtpConnVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	@Override
	public List<SmtpConnVo> getByServerType(String serverType, boolean onlyActive) {
		List<String> keys = new ArrayList<>();
		keys.add(serverType);
		String sql = "select * from smtp_server where server_type=?";
		if (onlyActive) {
			sql += " and status_id=? ";
			keys.add(StatusId.ACTIVE.value());
		}
		sql += " order by server_name ";
		List<SmtpConnVo> list = getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		return list;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SmtpConnVo> getBySslFlag(boolean useSSL, boolean onlyActive) {
		List<Object> keys = new ArrayList<>();
		keys.add(useSSL);
		String sql = "select * from smtp_server where use_ssl=?";
		if (onlyActive) {
			sql += " and status_id=? ";
			keys.add(StatusId.ACTIVE.value());
		}
		sql += " order by row_id ";
		List<?> list = (List<?>)getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		return (List<SmtpConnVo>) list;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SmtpConnVo> getBySslFlagForTrial(boolean useSSL, boolean onlyActive) {
		List<Object> keys = new ArrayList<>();
		keys.add(useSSL);
		String sql = "select * from smtp_server where use_ssl=?";
		if (onlyActive) {
			sql += " and status_id=? ";
			keys.add(StatusId.ACTIVE.value());
		}
		sql += " order by row_id limit 1 ";
		List<?> list = (List<?>)getJdbcTemplate().query(sql, keys.toArray(), 
				new BeanPropertyRowMapper<SmtpConnVo>(SmtpConnVo.class));
		return (List<SmtpConnVo>) list;
	}

	@Override
	public int update(SmtpConnVo smtpConnVo) {
		smtpConnVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(smtpConnVo);
		String sql = MetaDataUtil.buildUpdateStatement("smtp_server", smtpConnVo);
		if (smtpConnVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByServerName(String serverName) {
		String sql = "delete from smtp_server where server_name=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByPrimaryKey(long rowId) {
		String sql = "delete from smtp_server where row_id=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(SmtpConnVo smtpConnVo) {
		smtpConnVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(smtpConnVo);
		String sql = MetaDataUtil.buildInsertStatement("smtp_server", smtpConnVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		smtpConnVo.setRowId(retrieveRowId());
		smtpConnVo.setOrigUpdtTime(smtpConnVo.getUpdtTime());
		return rowsInserted;
	}
}

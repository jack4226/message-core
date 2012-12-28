package com.legacytojava.message.dao.emailaddr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.vo.emailaddr.EmailVariableVo;

public class EmailVariableJdbcDao implements EmailVariableDao {
	static final Logger logger = Logger.getLogger(EmailVariableJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private static final class EmailVariableMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			EmailVariableVo emailVariableVo = new EmailVariableVo();
			
			emailVariableVo.setRowId(rs.getInt("RowId"));
			emailVariableVo.setVariableName(rs.getString("VariableName"));
			emailVariableVo.setVariableType(rs.getString("VariableType"));
			emailVariableVo.setTableName(rs.getString("TableName"));
			emailVariableVo.setColumnName(rs.getString("ColumnName"));
			emailVariableVo.setStatusId(rs.getString("StatusId"));
			emailVariableVo.setIsBuiltIn(rs.getString("IsBuiltIn"));
			emailVariableVo.setDefaultValue(rs.getString("DefaultValue"));
			emailVariableVo.setVariableQuery(rs.getString("VariableQuery"));
			emailVariableVo.setVariableProc(rs.getString("VariableProc"));
			
			return emailVariableVo;
		}
	}
	
	public EmailVariableVo getByName(String variableName) {
		String sql = "select * from EmailVariable where VariableName=:variableName";
		SqlParameterSource namedParameters = new MapSqlParameterSource("variableName", variableName);
		List<?> list = (List<?>)namedParameterJdbcTemplate.query(sql, namedParameters, new EmailVariableMapper());
		if (list.size()>0) {
			return (EmailVariableVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailVariableVo> getAll() {
		String sql = "select * from EmailVariable " +
		" order by RowId";
		List<EmailVariableVo> list = (List<EmailVariableVo>)jdbcTemplate.query(sql, new EmailVariableMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailVariableVo> getAllForTrial() {
		String sql = "select * from EmailVariable " +
		" order by RowId" +
		" limit 50";
		int fetchSize = jdbcTemplate.getFetchSize();
		int maxRows = jdbcTemplate.getMaxRows();
		jdbcTemplate.setFetchSize(50);
		jdbcTemplate.setMaxRows(50);
		List<EmailVariableVo> list = (List<EmailVariableVo>)jdbcTemplate.query(sql, new EmailVariableMapper());
		jdbcTemplate.setFetchSize(fetchSize);
		jdbcTemplate.setMaxRows(maxRows);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailVariableVo> getAllCustomVariables() {
		String sql = "select * from EmailVariable " +
			" where IsBuiltIn!='" + Constants.YES_CODE + "' " +
			" order by RowId";
		List<EmailVariableVo> list = (List<EmailVariableVo>)jdbcTemplate.query(sql, new EmailVariableMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailVariableVo> getAllBuiltinVariables() {
		String sql = "select * from EmailVariable " +
			" where IsBuiltIn='" + Constants.YES_CODE + "' " +
			" order by RowId";
		List<EmailVariableVo> list = (List<EmailVariableVo>)jdbcTemplate.query(sql, new EmailVariableMapper());
		return list;
	}
	
	/**
	 * returns query result as string or null if not found.
	 */
	@SuppressWarnings("unchecked")
	public String getByQuery(String query, long addrId) {
		Object[] parms = new Object[] {addrId};
		List<String> list = (List<String>)jdbcTemplate.queryForList(query, parms, String.class);
		if (list.size() == 0) return null;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			String item = list.get(i);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(item);
		}
		return sb.toString();
	}
	
	public int update(EmailVariableVo emailVariableVo) {

		String sql = "update EmailVariable set " +
			"VariableName=:variableName," +
			"VariableType=:variableType," +
			"TableName=:tableName," +
			"ColumnName=:columnName," +
			"StatusId=:statusId," +
			"IsBuiltIn=:isBuiltIn," +
 			"DefaultValue=:defaultValue," +
			"VariableQuery=:variableQuery," +
			"VariableProc=:variableProc " +
			" where RowId=:rowId";
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailVariableVo);

		int rowsUpadted = namedParameterJdbcTemplate.update(sql, namedParameters);
		
		return rowsUpadted;
	}
	
	public int deleteByName(String variableName) {
		String sql = "delete from EmailVariable where VariableName=:variableName";
		Map<String,?> namedParameters = Collections.singletonMap("variableName", variableName);
		int rowsDeleted = namedParameterJdbcTemplate.update(sql, namedParameters);
		return rowsDeleted;
	}
	
	public int insert(EmailVariableVo emailVariableVo) {
		String sql = "INSERT INTO EmailVariable (" +
			"VariableName," +
			"VariableType," +
			"TableName," +
			"ColumnName," +
			"StatusId," +
			"IsBuiltIn," +
			"DefaultValue," +
			"VariableQuery," +
			"VariableProc " +
			") VALUES (" +
				" :variableName, :variableType, :tableName, :columnName, :statusId, :isBuiltIn, :defaultValue, :variableQuery, :variableProc " +
				")";
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailVariableVo);
		int rowsInserted = namedParameterJdbcTemplate.update(sql, namedParameters);
		emailVariableVo.setRowId(retrieveRowId());
		return rowsInserted;
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
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}
}

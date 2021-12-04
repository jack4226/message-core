package ltj.message.dao.emailaddr;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.emailaddr.EmailVariableVo;

@Component("emailVariableDao")
public class EmailVariableJdbcDao extends AbstractDao implements EmailVariableDao {
	static final Logger logger = LogManager.getLogger(EmailVariableJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Override
	public EmailVariableVo getByName(String variableName) {
		String sql = "select * from email_variable where variable_name=:variableName";
		SqlParameterSource namedParameters = new MapSqlParameterSource("variableName", variableName);
		try {
			EmailVariableVo vo = getNamedParameterJdbcTemplate().queryForObject(sql, namedParameters, 
					new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<EmailVariableVo> getAll() {
		String sql = "select * from email_variable " +
		" order by row_id";
		List<EmailVariableVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
		return list;
	}
	
	@Override
	public List<EmailVariableVo> getAllForTrial() {
		String sql = "select * from email_variable " +
		" order by row_id" +
		" limit 50";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(50);
		getJdbcTemplate().setMaxRows(50);
		List<EmailVariableVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	@Override
	public List<EmailVariableVo> getAllCustomVariables() {
		String sql = "select * from email_variable " +
			" where is_built_in!=? " +
			" order by row_id";
		Object[] parms = new Object[] {true};
		List<EmailVariableVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
		return list;
	}
	
	@Override
	public List<EmailVariableVo> getAllBuiltinVariables() {
		String sql = "select * from email_variable " +
			" where is_built_in=? " +
			" order by row_id";
		Object[] parms = new Object[] {true};
		List<EmailVariableVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailVariableVo>(EmailVariableVo.class));
		return list;
	}
	
	/**
	 * returns query result as string or null if not found.
	 */
	@Override
	public String getByQuery(String query, long addrId) {
		Object[] parms = new Object[] {addrId};
		List<String> list = (List<String>)getJdbcTemplate().queryForList(query, parms, String.class);
		if (list.size() == 0) {
			return null;
		}
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
	
	@Override
	public int update(EmailVariableVo emailVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("email_variable", emailVariableVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		
		return rowsUpadted;
	}
	
	@Override
	public int deleteByName(String variableName) {
		String sql = "delete from email_variable where variable_name=:variableName";
		Map<String,?> namedParameters = Collections.singletonMap("variableName", variableName);
		int rowsDeleted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsDeleted;
	}
	
	@Override
	public int insert(EmailVariableVo emailVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("email_variable", emailVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		emailVariableVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}

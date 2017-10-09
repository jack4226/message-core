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
import ltj.vo.template.ClientVariableVo;

@Component("clientVariableDao")
public class ClientVariableDao extends AbstractDao {
	
	private static final Map<String, List<ClientVariableVo>> currentVariablesCache = new HashMap<>();
	
	public ClientVariableVo getByPrimaryKey(String clientId, String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"client_variable where client_id=? and variable_name=? ";
		
		Object[] parms;
		if (startTime!=null) {
			sql += " and start_time=? ";
			parms = new Object[] {clientId,variableName,startTime};
		}
		else {
			sql += " and start_time is null ";
			parms = new Object[] {clientId,variableName};
		}
		try {
			ClientVariableVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public ClientVariableVo getByBestMatch(String clientId, String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"client_variable where client_id=? and variable_name=? ";
		
		List<Object> keys = new ArrayList<>();
		keys.add(clientId);
		keys.add(variableName);
		if (startTime!=null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		sql += " and (start_time<=? or start_time is null) ";
		keys.add(startTime);
		sql += " order by start_time desc ";
		
		Object[] parms = keys.toArray();
		List<ClientVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	public ClientVariableVo getByRowId(long rowId) {
		String sql = "select * from client_variable where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			ClientVariableVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<ClientVariableVo> getByVariableName(String variableName) {
		String sql = 
			"select * " +
			" from " +
				" client_variable where variable_name=? " +
			" order by client_id, start_time asc ";
		Object[] parms = new Object[] {variableName};
		List<ClientVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
		return list;
	}
	
	public List<ClientVariableVo> getCurrentByClientId(String clientId) {
		if (!currentVariablesCache.containsKey(clientId)) {
			String sql = 
				"select * " +
					" from client_variable a " +
					" inner join ( " +
					"  select b.client_id, b.variable_name, max(b.start_time) as MaxTime " +
					"   from client_variable b " +
					"   where b.status_id=? and b.start_time<=? " +
					"    and b.client_id=? " +
					"   group by b.variable_name " +
					" ) as c " +
					"  on a.variable_name=c.variable_name and a.start_time=c.MaxTime " +
					"    and a.client_id=c.client_id " +
					" order by a.row_id asc ";
			Object[] parms = new Object[] { StatusId.ACTIVE.value(), new Timestamp(System.currentTimeMillis()), clientId };
			List<ClientVariableVo> list = getJdbcTemplate().query(sql, parms,
					new BeanPropertyRowMapper<ClientVariableVo>(ClientVariableVo.class));
			currentVariablesCache.put(clientId, list);
		}
		
		List<ClientVariableVo> list = currentVariablesCache.get(clientId);
		return list;
	}
	
	public int update(ClientVariableVo clientVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(clientVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("client_variable", clientVariableVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		if (rowsUpadted > 0) {
			currentVariablesCache.remove(clientVariableVo.getClientId());
		}
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String clientId, String variableName, Timestamp startTime) {
		String sql = 
			"delete from client_variable where client_id=? and variable_name=? ";
		
		List<Object> fields = new ArrayList<>();
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
		if (rowsDeleted > 0) {
			currentVariablesCache.remove(clientId);
		}
		return rowsDeleted;
	}
	
	public int deleteByVariableName(String variableName) {
		String sql = 
			"delete from client_variable where variable_name=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted > 0) {
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	public int deleteByClientId(String clientId) {
		String sql = 
			"delete from client_variable where client_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(clientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted > 0) {
			currentVariablesCache.remove(clientId);
		}
		return rowsDeleted;
	}
	
	public int insert(ClientVariableVo clientVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(clientVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("client_variable", clientVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		clientVariableVo.setRowId(retrieveRowId());
		if (rowsInserted > 0) {
			currentVariablesCache.remove(clientVariableVo.getClientId());
		}
		return rowsInserted;
	}
	
}

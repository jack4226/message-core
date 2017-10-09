package ltj.message.dao.template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.vo.template.GlobalVariableVo;

@Component("globalVariableDao")
public class GlobalVariableDao extends AbstractDao {
	
	private static final List<GlobalVariableVo> currentVariablesCache = new ArrayList<>();
	
	public GlobalVariableVo getByPrimaryKey(String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"global_variable where variable_name=? ";
		
		Object[] parms;
		if (startTime!=null) {
			sql += " and start_time=? ";
			parms = new Object[] {variableName, startTime};
		}
		else {
			sql += " and start_time is null ";
			parms = new Object[] {variableName};
		}
		try {
			GlobalVariableVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<GlobalVariableVo>(GlobalVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public GlobalVariableVo getByBestMatch(String variableName, Timestamp startTime) {
		String sql = 
			"select * " +
			"from " +
				"global_variable where variable_name=? ";
		
		Object[] parms;
		if (startTime!=null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		sql += " and (start_time<=? or start_time is null) ";
		sql += " order by start_time desc ";
		
		parms = new Object[] {variableName, startTime};
		List<GlobalVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<GlobalVariableVo>(GlobalVariableVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	public GlobalVariableVo getByRowId(long rowId) {
		String sql = "select * from global_variable where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			GlobalVariableVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<GlobalVariableVo>(GlobalVariableVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<GlobalVariableVo> getByVariableName(String variableName) {
		String sql = 
			"select * " +
			" from " +
				" global_variable where variable_name=? " +
			" order by start_time asc ";
		Object[] parms = new Object[] {variableName};
		List<GlobalVariableVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<GlobalVariableVo>(GlobalVariableVo.class));
		return list;
	}
	
	public List<GlobalVariableVo> getCurrent() {
		if (currentVariablesCache.size() == 0) {
			String sql = 
				"select * " +
					" from global_variable a " +
					" inner join ( " +
					"  select b.variable_name, max(b.start_time) as MaxTime " +
					"   from global_variable b " +
					"   where b.status_id = ? and b.start_time<=? " +
					"   group by b.variable_name " +
					" ) as c " +
					"  on a.variable_name=c.variable_name and a.start_time=c.MaxTime " +
					" order by a.variable_name asc ";
			Object[] parms = new Object[] { StatusId.ACTIVE.value(), new Timestamp(new java.util.Date().getTime()) };
			List<GlobalVariableVo> list = getJdbcTemplate().query(sql, parms, 
					new BeanPropertyRowMapper<GlobalVariableVo>(GlobalVariableVo.class));
			currentVariablesCache.addAll(list);
		}
		
		List<GlobalVariableVo> list = new ArrayList<GlobalVariableVo>();
		list.addAll(currentVariablesCache);
		return list;
	}
	
	public List<GlobalVariableVo> getByStatusId(String statusId) {
		String sql = 
			"select * " +
				" from global_variable " +
				" where status_id = ? and start_time<=?" +
				" order by variable_name asc, start_time desc ";
		Object[] parms = new Object[] { statusId, new Timestamp(new java.util.Date().getTime()) };
		List<GlobalVariableVo> list =  getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<GlobalVariableVo>(GlobalVariableVo.class));
		ArrayList<GlobalVariableVo> list2 = new ArrayList<GlobalVariableVo>();
		String varName = null;
		for (Iterator<GlobalVariableVo> it=list.iterator(); it.hasNext(); ) {
			GlobalVariableVo vo = it.next();
			if (!vo.getVariableName().equals(varName)) {
				list2.add(vo);
				varName = vo.getVariableName();
			}
		}
		return list2;
	}
	
	public int update(GlobalVariableVo globalVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(globalVariableVo);
		String sql = MetaDataUtil.buildUpdateStatement("global_variable", globalVariableVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		if (rowsUpadted > 0) {
			currentVariablesCache.clear();
		}
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String variableName, Timestamp startTime) {
		String sql = 
			"delete from global_variable where variable_name=? ";
		
		List<Object> fields = new ArrayList<>();
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
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	public int deleteByVariableName(String variableName) {
		String sql = 
			"delete from global_variable where variable_name=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(variableName);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted > 0) {
			currentVariablesCache.clear();
		}
		return rowsDeleted;
	}
	
	public int insert(GlobalVariableVo globalVariableVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(globalVariableVo);
		String sql = MetaDataUtil.buildInsertStatement("global_variable", globalVariableVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		globalVariableVo.setRowId(retrieveRowId());
		if (rowsInserted > 0) {
			currentVariablesCache.clear();
		}
		return rowsInserted;
	}
}

package ltj.message.dao.user;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.UserVo;

@Component("userDao")
public class UserDao extends AbstractDao {
	
	public UserVo getByUserId(String userId) {
		String sql = "select * from user_tbl where user_id=?";
		Object[] parms = new Object[] {userId};
		try {
			UserVo vo = getJdbcTemplate().queryForObject(sql, parms, new BeanPropertyRowMapper<UserVo>(UserVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public UserVo getByPrimaryKey(long rowId) {
		String sql = "select * from user_tbl where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			UserVo vo = getJdbcTemplate().queryForObject(sql, parms, new BeanPropertyRowMapper<UserVo>(UserVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public UserVo getForLogin(String userId, String password) {
		String sql = "select * from user_tbl where user_id=? and password=?";
		Object[] parms = new Object[] {userId, password};
		try {
			UserVo vo = getJdbcTemplate().queryForObject(sql, parms, new BeanPropertyRowMapper<UserVo>(UserVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<UserVo> getFirst100(boolean onlyActive) {
		
		String sql = "select * from user_tbl ";
		if (onlyActive) {
			sql += " where status_id='" + StatusId.ACTIVE.value() + "'";
		}
		sql += " limit 100";
		List<UserVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<UserVo>(UserVo.class));
		return list;
	}
	
	public int update(UserVo userVo) {
		if (userVo.getCreateTime()==null) {
			userVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(userVo);
		String sql = MetaDataUtil.buildUpdateStatement("user_tbl", userVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int update4Web(UserVo userVo) {
		Object[] parms = {
				userVo.getSessionId(),
				userVo.getLastVisitTime(),
				userVo.getHits(),
				userVo.getRowId()
				};
		
		String sql = "update user_tbl set " +
			"session_id=?," +
			"last_visit_time=?," +
			"hits=?" +
			" where row_id=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByUserId(String userId) {
		String sql = "delete from user_tbl where user_id=?";
		Object[] parms = new Object[] {userId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByPrimaryKey(long rowId) {
		String sql = "delete from user_tbl where row_id=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(UserVo userVo) {
		if (userVo.getCreateTime()==null) {
			userVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(userVo);
		String sql = MetaDataUtil.buildInsertStatement("user_tbl", userVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		userVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}

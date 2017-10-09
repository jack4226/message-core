package ltj.message.dao.servers;

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
import ltj.message.vo.TimerServerVo;

@Component("timerServerDao")
public class TimerServerDao extends AbstractDao {
	
	public TimerServerVo getByServerName(String serverName) {
		String sql = "select * from timer_server where server_name=?";
		Object[] parms = new Object[] {serverName};
		try {
			TimerServerVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<TimerServerVo>(TimerServerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public TimerServerVo getByPrimaryKey(long rowId) {
		String sql = "select * from timer_server where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			TimerServerVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<TimerServerVo>(TimerServerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<TimerServerVo> getAll(boolean onlyActive) {
		
		String sql = "select * from timer_server ";
		if (onlyActive) {
			sql += " where status_id='" + StatusId.ACTIVE.value() + "'";
		}
		List<TimerServerVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<TimerServerVo>(TimerServerVo.class));
		return list;
	}
	
	public int update(TimerServerVo timerServerVo) {
		if (timerServerVo.getUpdtTime()==null) {
			timerServerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(timerServerVo);
		String sql = MetaDataUtil.buildUpdateStatement("timer_server", timerServerVo);
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByServerName(String serverName) {
		String sql = "delete from timer_server where server_name=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}

	public int deleteByPrimaryKey(long rowId) {
		String sql = "delete from timer_server where row_id=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(TimerServerVo timerServerVo) {
		if (timerServerVo.getUpdtTime()==null) {
			timerServerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(timerServerVo);
		String sql = MetaDataUtil.buildInsertStatement("timer_server", timerServerVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		timerServerVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}

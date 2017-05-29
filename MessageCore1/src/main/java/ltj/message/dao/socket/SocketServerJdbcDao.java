package ltj.message.dao.socket;

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
import ltj.message.vo.SocketServerVo;

@Component("socketServerDao")
public class SocketServerJdbcDao extends AbstractDao implements SocketServerDao {
	
	@Override
	public SocketServerVo getByServerName(String serverName) {
		String sql = "select * from socket_server where server_name=?";
		Object[] parms = new Object[] {serverName};
		try {
			SocketServerVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SocketServerVo>(SocketServerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public SocketServerVo getByPrimaryKey(long rowId) {
		String sql = "select * from socket_server where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			SocketServerVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SocketServerVo>(SocketServerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<SocketServerVo> getAll(boolean onlyActive) {
		
		String sql = "select * from socket_server ";
		if (onlyActive) {
			sql += " where status_id='" + StatusId.ACTIVE.value() + "'";
		}
		List<SocketServerVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<SocketServerVo>(SocketServerVo.class));
		return list;
	}
	
	@Override
	public int update(SocketServerVo socketServerVo) {
		socketServerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(socketServerVo);
		String sql = MetaDataUtil.buildUpdateStatement("socket_server", socketServerVo);
		if (socketServerVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		socketServerVo.setOrigUpdtTime(socketServerVo.getUpdtTime());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByServerName(String serverName) {
		String sql = "delete from socket_server where server_name=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByPrimaryKey(long rowId) {
		String sql = "delete from socket_server where row_id=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(SocketServerVo socketServerVo) {
		socketServerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(socketServerVo);
		String sql = MetaDataUtil.buildInsertStatement("socket_server", socketServerVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		socketServerVo.setRowId(retrieveRowId());
		socketServerVo.setOrigUpdtTime(socketServerVo.getUpdtTime());
		return rowsInserted;
	}
	
}

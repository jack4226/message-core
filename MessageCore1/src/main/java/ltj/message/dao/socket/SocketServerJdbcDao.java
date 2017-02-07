package ltj.message.dao.socket;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.SocketServerVo;

@Component("socketServerDao")
public class SocketServerJdbcDao extends AbstractDao implements SocketServerDao {
	
	@Override
	public SocketServerVo getByPrimaryKey(String serverName) {
		String sql = "select * from SocketServers where ServerName=?";
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
	public List<SocketServerVo> getAll(boolean onlyActive) {
		
		String sql = "select * from SocketServers ";
		if (onlyActive) {
			sql += " where StatusId='A'";
		}
		List<SocketServerVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<SocketServerVo>(SocketServerVo.class));
		return list;
	}
	
	@Override
	public int update(SocketServerVo socketServerVo) {
		socketServerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(socketServerVo);
		String sql = MetaDataUtil.buildUpdateStatement("SocketServers", socketServerVo);
		if (socketServerVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		socketServerVo.setOrigUpdtTime(socketServerVo.getUpdtTime());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(String serverName) {
		String sql = "delete from SocketServers where ServerName=?";
		Object[] parms = new Object[] {serverName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(SocketServerVo socketServerVo) {
		socketServerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(socketServerVo);
		String sql = MetaDataUtil.buildInsertStatement("SocketServers", socketServerVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		socketServerVo.setRowId(retrieveRowId());
		socketServerVo.setOrigUpdtTime(socketServerVo.getUpdtTime());
		return rowsInserted;
	}
	
}

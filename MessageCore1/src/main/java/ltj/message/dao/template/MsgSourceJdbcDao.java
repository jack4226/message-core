package ltj.message.dao.template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.vo.template.MsgSourceVo;

@Component("msgSourceDao")
public class MsgSourceJdbcDao extends AbstractDao implements MsgSourceDao {
	
	@Override
	public MsgSourceVo getByPrimaryKey(String msgSourceId) {
		String sql = 
			"select * " +
			"from " +
				"MsgSource where msgSourceId=? ";
		
		Object[] parms = new Object[] {msgSourceId};
		try {
			MsgSourceVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgSourceVo>(MsgSourceVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgSourceVo> getByFromAddrId(long fromAddrId) {
		String sql = 
			"select * " +
			" from " +
				" MsgSource where fromAddrId=? ";
		Object[] parms = new Object[] {Long.valueOf(fromAddrId)};
		List<MsgSourceVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgSourceVo>(MsgSourceVo.class));
		return list;
	}
	
	@Override
	public List<MsgSourceVo> getAll() {
		String sql = 
			"select * " +
			" from " +
				" MsgSource ";
		List<MsgSourceVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgSourceVo>(MsgSourceVo.class));
		return list;
	}
	
	@Override
	public int update(MsgSourceVo msgSourceVo) {
		msgSourceVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgSourceVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgSource", msgSourceVo);

		if (msgSourceVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgSourceVo.setOrigUpdtTime(msgSourceVo.getUpdtTime());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(String msgSourceId) {
		String sql = 
			"delete from MsgSource where msgSourceId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgSourceId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByFromAddrId(long fromAddrId) {
		String sql = 
			"delete from MsgSource where fromAddrId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(Long.valueOf(fromAddrId));
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgSourceVo msgSourceVo) {
		msgSourceVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgSourceVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgSource", msgSourceVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgSourceVo.setRowId(retrieveRowId());
		msgSourceVo.setOrigUpdtTime(msgSourceVo.getUpdtTime());
		return rowsInserted;
	}

}

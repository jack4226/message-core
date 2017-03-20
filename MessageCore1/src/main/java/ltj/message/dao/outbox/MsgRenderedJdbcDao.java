package ltj.message.dao.outbox;

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
import ltj.vo.outbox.MsgRenderedVo;

@Component("msgRenderedDao")
public class MsgRenderedJdbcDao extends AbstractDao implements MsgRenderedDao {
	
	@Override
	public MsgRenderedVo getByPrimaryKey(long renderId) {
		String sql = 
			"select * " +
			"from " +
				"msg_rendered where renderId=? ";
		
		Object[] parms = new Object[] {renderId};
		try {
			MsgRenderedVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public MsgRenderedVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"msg_rendered where renderId=(select max(RenderId) from msg_rendered) ";
		
		List<MsgRenderedVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public MsgRenderedVo getRandomRecord() {
		String sql = 
			"select * " +
			"from " +
				"msg_rendered where renderId >= (RAND() *(select max(RenderId) from msg_rendered)) " +
				 "order by renderId limit 1 ";
		
		List<MsgRenderedVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public List<MsgRenderedVo> getByMsgSourceId(String msgSourceId) {
		String sql = 
			"select * " +
			" from " +
				" msg_rendered where msgSourceId=? " +
			" order by renderId";
		Object[] parms = new Object[] {msgSourceId};
		List<MsgRenderedVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgRenderedVo>(MsgRenderedVo.class));
		return list;
	}
	
	@Override
	public int update(MsgRenderedVo msgRenderedVo) {
		msgRenderedVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgRenderedVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_rendered", msgRenderedVo);
		if (msgRenderedVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long renderId) {
		String sql = 
			"delete from msg_rendered where renderId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(renderId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgRenderedVo msgRenderedVo) {
		msgRenderedVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgRenderedVo);
		String sql = MetaDataUtil.buildInsertStatement("msg_rendered", msgRenderedVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgRenderedVo.setRenderId(getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class));
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsInserted;
	}
}

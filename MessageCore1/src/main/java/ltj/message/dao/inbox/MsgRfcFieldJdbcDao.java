package ltj.message.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.inbox.MsgRfcFieldVo;

@Component("msgRfcFieldDao")
public class MsgRfcFieldJdbcDao extends AbstractDao implements MsgRfcFieldDao {
	
	@Override
	public MsgRfcFieldVo getByPrimaryKey(long msgId, String rfcType) {
		String sql = 
			"select * " +
			"from " +
				"msg_rfc_field where msgid=? and rfcType=? ";
		
		Object[] parms = new Object[] {msgId, rfcType};
		try {
			MsgRfcFieldVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgRfcFieldVo>(MsgRfcFieldVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgRfcFieldVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" msg_rfc_field where msgId=? " +
			" order by rfcType";
		Object[] parms = new Object[] {msgId};
		List<MsgRfcFieldVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgRfcFieldVo>(MsgRfcFieldVo.class));
		return list;
	}
	
	@Override
	public List<MsgRfcFieldVo> getRandomRecord() {
		String sql = 
				"select * " +
				" from " +
					" msg_rfc_field where msgId >= (RAND() * (select max(msgId) from msg_rfc_field)) " +
				" order by msgId limit 1 ";
		List<MsgRfcFieldVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgRfcFieldVo>(MsgRfcFieldVo.class));
		if (list.size() > 0) {
			return getByMsgId(list.get(0).getMsgId());
		}
		return list;
	}
	
	@Override
	public int update(MsgRfcFieldVo msgRfcFieldVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgRfcFieldVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_rfc_field", msgRfcFieldVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, String rfcType) {
		String sql = 
			"delete from msg_rfc_field where msgid=? and rfcType=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		fields.add(rfcType);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from msg_rfc_field where msgid=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgRfcFieldVo msgRfcFieldVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgRfcFieldVo);
		String sql = MetaDataUtil.buildInsertStatement("msg_rfc_field", msgRfcFieldVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

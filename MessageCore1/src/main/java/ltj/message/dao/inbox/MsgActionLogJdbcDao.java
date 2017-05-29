package ltj.message.dao.inbox;

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
import ltj.message.vo.inbox.MsgActionLogVo;

@Component("msgActionLogDao")
public class MsgActionLogJdbcDao extends AbstractDao implements MsgActionLogDao {
	
	@Override
	public MsgActionLogVo getByPrimaryKey(long msgId, Long msgRefId) {
		String sql = 
			"select * " +
			"from " +
				"msg_action_log where msg_id=? ";
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		if (msgRefId == null) {
			sql += "and msg_ref_id is null ";
		}
		else {
			fields.add(msgRefId);
			sql += "and msg_ref_id=? ";
		}
		try {
			MsgActionLogVo vo = getJdbcTemplate().queryForObject(sql, fields.toArray(),
					new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgActionLogVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" msg_action_log where msg_id=? " +
			" order by msg_ref_id ";
		Object[] parms = new Object[] {msgId};
		List<MsgActionLogVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
		return list;
	}
	
	@Override
	public List<MsgActionLogVo> getByLeadMsgId(long leadMsgId) {
		String sql = 
			"select * " +
			" from " +
				" msg_action_log where lead_msg_id=? " +
			" order by addr_time";
		Object[] parms = new Object[] {leadMsgId};
		List<MsgActionLogVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
		return list;
	}
	
	@Override
	public List<MsgActionLogVo> getRandomRecord() {
		String sql = 
				"select * " +
				" from msg_action_log " +
					" where msg_id >= (RAND() * (select max(msg_id) from msg_action_log)) " +
				" order by msg_id limit 1 ";
		List<MsgActionLogVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgActionLogVo>(MsgActionLogVo.class));
		if (list.size() > 0) {
			return getByMsgId(list.get(0).getMsgId());
		}
		return list;
	}
	
	@Override
	public int update(MsgActionLogVo msgActionLogVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionLogVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_action_log", msgActionLogVo);
		if (msgActionLogVo.getMsgRefId() == null) {
			sql += " and msg_ref_id is null ";
		}
		else {
			sql += " and msg_ref_id=:msgRefId ";
		}
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, Long msgRefId) {
		String sql = 
			"delete from msg_action_log where msg_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		if (msgRefId == null) {
			sql += "and msg_ref_id is null ";
		}
		else {
			fields.add(msgRefId);
			sql += "and msg_ref_id=? ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from msg_action_log where msg_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByLeadMsgId(long leadMsgId) {
		String sql = 
			"delete from msg_action_log where lead_msg_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(leadMsgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgActionLogVo msgActionLogVo) {
		Timestamp addTime = new Timestamp(System.currentTimeMillis());
		msgActionLogVo.setAddTime(addTime);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionLogVo);
		String sql = MetaDataUtil.buildInsertStatement("msg_action_log", msgActionLogVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

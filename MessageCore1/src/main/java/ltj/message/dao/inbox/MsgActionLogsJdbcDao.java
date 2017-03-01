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
import ltj.message.vo.inbox.MsgActionLogsVo;

@Component("msgActionLogsDao")
public class MsgActionLogsJdbcDao extends AbstractDao implements MsgActionLogsDao {
	
	@Override
	public MsgActionLogsVo getByPrimaryKey(long msgId, Long msgRefId) {
		String sql = 
			"select * " +
			"from " +
				"MsgActionLogs where msgId=? ";
		List<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		if (msgRefId == null) {
			sql += "and msgRefId is null ";
		}
		else {
			fields.add(msgRefId);
			sql += "and msgRefId=? ";
		}
		try {
			MsgActionLogsVo vo = getJdbcTemplate().queryForObject(sql, fields.toArray(),
					new BeanPropertyRowMapper<MsgActionLogsVo>(MsgActionLogsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgActionLogsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgActionLogs where msgId=? " +
			" order by msgRefId ";
		Object[] parms = new Object[] {msgId};
		List<MsgActionLogsVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionLogsVo>(MsgActionLogsVo.class));
		return list;
	}
	
	@Override
	public List<MsgActionLogsVo> getByLeadMsgId(long leadMsgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgActionLogs where leadMsgId=? " +
			" order by addrTime";
		Object[] parms = new Object[] {leadMsgId};
		List<MsgActionLogsVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgActionLogsVo>(MsgActionLogsVo.class));
		return list;
	}
	
	@Override
	public List<MsgActionLogsVo> getRandomRecord() {
		String sql = 
				"select * " +
				" from " +
					" MsgActionLogs where msgId >= (RAND() * (select max(msgId) from MsgActionLogs)) " +
				" order by msgId limit 1 ";
		List<MsgActionLogsVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgActionLogsVo>(MsgActionLogsVo.class));
		if (list.size() > 0) {
			return getByMsgId(list.get(0).getMsgId());
		}
		return list;
	}
	
	@Override
	public int update(MsgActionLogsVo msgActionLogsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionLogsVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgActionLogs", msgActionLogsVo);
		if (msgActionLogsVo.getMsgRefId() == null) {
			sql += " and MsgRefId is null ";
		}
		else {
			sql += " and MsgRefId=:msgRefId ";
		}
		
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, Long msgRefId) {
		String sql = 
			"delete from MsgActionLogs where msgId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		if (msgRefId == null) {
			sql += "and MsgRefId is null ";
		}
		else {
			fields.add(msgRefId);
			sql += "and msgRefId=? ";
		}
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from MsgActionLogs where msgId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByLeadMsgId(long leadMsgId) {
		String sql = 
			"delete from MsgActionLogs where leadMsgId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(leadMsgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgActionLogsVo msgActionLogsVo) {
		Timestamp addTime = new Timestamp(System.currentTimeMillis());
		msgActionLogsVo.setAddTime(addTime);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgActionLogsVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgActionLogs", msgActionLogsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

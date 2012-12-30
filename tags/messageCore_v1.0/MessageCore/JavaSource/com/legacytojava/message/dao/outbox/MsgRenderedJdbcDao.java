package com.legacytojava.message.dao.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.legacytojava.message.vo.outbox.MsgRenderedVo;

public class MsgRenderedJdbcDao implements MsgRenderedDao {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	private static final class MsgRenderedMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgRenderedVo msgRenderedVo = new MsgRenderedVo();
			
			msgRenderedVo.setRenderId(rs.getLong("RenderId"));
			msgRenderedVo.setMsgSourceId(rs.getString("MsgSourceId"));
			msgRenderedVo.setSubjTemplateId(rs.getString("SubjTemplateId"));
			msgRenderedVo.setBodyTemplateId(rs.getString("BodyTemplateId"));
			msgRenderedVo.setStartTime(rs.getTimestamp("StartTime"));
			msgRenderedVo.setClientId(rs.getString("ClientId"));
			msgRenderedVo.setCustId(rs.getString("CustId"));
			msgRenderedVo.setPurgeAfter((Integer)rs.getObject("PurgeAfter"));
			msgRenderedVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			msgRenderedVo.setUpdtUserId(rs.getString("UpdtUserId"));
			msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
			return msgRenderedVo;
		}
	}

	public MsgRenderedVo getByPrimaryKey(long renderId) {
		String sql = 
			"select * " +
			"from " +
				"MsgRendered where renderId=? ";
		
		Object[] parms = new Object[] {renderId};
		List<?> list = (List<?>)jdbcTemplate.query(sql, parms, new MsgRenderedMapper());
		if (list.size()>0)
			return (MsgRenderedVo)list.get(0);
		else
			return null;
	}
	
	public MsgRenderedVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"MsgRendered where renderId=(select max(RenderId) from MsgRendered) ";
		
		List<?> list = (List<?>)jdbcTemplate.query(sql, new MsgRenderedMapper());
		if (list.size()>0)
			return (MsgRenderedVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgRenderedVo> getByMsgSourceId(String msgSourceId) {
		String sql = 
			"select * " +
			" from " +
				" MsgRendered where msgSourceId=? " +
			" order by renderId";
		Object[] parms = new Object[] {msgSourceId};
		List<MsgRenderedVo> list = (List<MsgRenderedVo>)jdbcTemplate.query(sql, parms, new MsgRenderedMapper());
		return list;
	}
	
	
	public int update(MsgRenderedVo msgRenderedVo) {
		msgRenderedVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgRenderedVo.getMsgSourceId());
		fields.add(msgRenderedVo.getSubjTemplateId());
		fields.add(msgRenderedVo.getBodyTemplateId());
		fields.add(msgRenderedVo.getStartTime());
		fields.add(msgRenderedVo.getClientId());
		fields.add(msgRenderedVo.getCustId());
		fields.add(msgRenderedVo.getPurgeAfter());
		fields.add(msgRenderedVo.getUpdtTime());
		fields.add(msgRenderedVo.getUpdtUserId());
		fields.add(msgRenderedVo.getRenderId());
		
		String sql =
			"update MsgRendered set " +
				"MsgSourceId=?, " +
				"SubjTemplateId=?, " +
				"BodyTemplateId=?, " +
				"StartTime=?, " +
				"ClientId=?, " +
				"CustId=?, " +
				"PurgeAfter=?, " +
				"UpdtTime=?, " +
				"UpdtUserId=? " +
			" where " +
				" renderId=? ";
		
		if (msgRenderedVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgRenderedVo.getOrigUpdtTime());
		}
		int rowsUpadted = jdbcTemplate.update(sql, fields.toArray());
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long renderId) {
		String sql = 
			"delete from MsgRendered where renderId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderId+"");
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgRenderedVo msgRenderedVo) {
		String sql = 
			"INSERT INTO MsgRendered (" +
				"MsgSourceId, " +
				"SubjTemplateId, " +
				"BodyTemplateId, " +
				"StartTime, " +
				"ClientId, " +
				"CustId, " +
				"PurgeAfter, " +
				"UpdtTime, " +
				"UpdtUserId " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				")";
		
		msgRenderedVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgRenderedVo.getMsgSourceId());
		fields.add(msgRenderedVo.getSubjTemplateId());
		fields.add(msgRenderedVo.getBodyTemplateId());
		fields.add(msgRenderedVo.getStartTime());
		fields.add(msgRenderedVo.getClientId());
		fields.add(msgRenderedVo.getCustId());
		fields.add(msgRenderedVo.getPurgeAfter());
		fields.add(msgRenderedVo.getUpdtTime());
		fields.add(msgRenderedVo.getUpdtUserId());
		
		int rowsInserted = jdbcTemplate.update(sql, fields.toArray());
		msgRenderedVo.setRenderId(jdbcTemplate.queryForInt(getRowIdSql()));
		msgRenderedVo.setOrigUpdtTime(msgRenderedVo.getUpdtTime());
		return rowsInserted;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}

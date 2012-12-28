package com.legacytojava.message.dao.inbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.legacytojava.message.vo.inbox.MsgAddrsVo;

public class MsgAddrsJdbcDao implements MsgAddrsDao {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	private static final class MsgAddrsMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgAddrsVo msgAddrsVo = new MsgAddrsVo();
			
			msgAddrsVo.setMsgId(rs.getLong("MsgId"));
			msgAddrsVo.setAddrType(rs.getString("AddrType"));
			msgAddrsVo.setAddrSeq(rs.getInt("AddrSeq"));
			msgAddrsVo.setAddrValue(rs.getString("AddrValue"));
			
			return msgAddrsVo;
		}
	}

	public MsgAddrsVo getByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"select * " +
			"from " +
				"MsgAddrs where msgid=? and addrType=? and addrSeq=? ";
		
		Object[] parms = new Object[] {msgId+"",addrType,addrSeq+""};
		List<?> list = (List<?>)jdbcTemplate.query(sql, parms, new MsgAddrsMapper());
		if (list.size()>0)
			return (MsgAddrsVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgAddrsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgAddrs where msgId=? " +
			" order by addrType, addrSeq";
		Object[] parms = new Object[] {msgId+""};
		List<MsgAddrsVo> list =  (List<MsgAddrsVo>)jdbcTemplate.query(sql, parms, new MsgAddrsMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgAddrsVo> getByMsgIdAndType(long msgId, String addrType) {
		String sql = 
			"select * " +
			" from " +
				" MsgAddrs where msgId=? and addrType=? " +
			" order by addrSeq";
		Object[] parms = new Object[] {msgId+"",addrType};
		List<MsgAddrsVo> list =  (List<MsgAddrsVo>)jdbcTemplate.query(sql, parms, new MsgAddrsMapper());
		return list;
	}
	
	public int update(MsgAddrsVo msgAddrsVo) {
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgAddrsVo.getAddrValue());
		fields.add(msgAddrsVo.getMsgId()+"");
		fields.add(msgAddrsVo.getAddrType());
		fields.add(msgAddrsVo.getAddrSeq()+"");
		
		String sql =
			"update MsgAddrs set " +
				"AddrValue=? " +
			" where " +
				" msgid=? and addrType=? and addrSeq=?  ";
		
		int rowsUpadted = jdbcTemplate.update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"delete from MsgAddrs where msgid=? and addrType=? and addrSeq=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		fields.add(addrType);
		fields.add(addrSeq+"");
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from MsgAddrs where msgid=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		
		int rowsDeleted = jdbcTemplate.update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgAddrsVo msgAddrsVo) {
		String sql = 
			"INSERT INTO MsgAddrs (" +
			"MsgId, " +
			"AddrType, " +
			"AddrSeq, " +
			"Addrvalue " +
			") VALUES (" +
				" ?, ?, ?, ? " +
				")";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgAddrsVo.getMsgId()+"");
		fields.add(msgAddrsVo.getAddrType());
		fields.add(msgAddrsVo.getAddrSeq()+"");
		fields.add(msgAddrsVo.getAddrValue());
		
		int rowsInserted = jdbcTemplate.update(sql, fields.toArray());
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

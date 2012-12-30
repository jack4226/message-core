package com.legacytojava.message.dao.smtp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.legacytojava.message.vo.MailSenderVo;

public class MailSenderPropsJdbcDao implements MailSenderPropsDao {
	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;

	private static final class MailSenderPropsMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MailSenderVo mailSenderVo = new MailSenderVo();
			
			mailSenderVo.setRowId(rs.getInt("RowId"));
			mailSenderVo.setInternalLoopback(rs.getString("InternalLoopback"));
			mailSenderVo.setExternalLoopback(rs.getString("ExternalLoopback"));
			mailSenderVo.setUseTestAddr(rs.getString("UseTestAddr"));
			mailSenderVo.setTestFromAddr(rs.getString("TestFromAddr"));
			mailSenderVo.setTestToAddr(rs.getString("TestToAddr"));
			mailSenderVo.setTestReplytoAddr(rs.getString("TestReplytoAddr"));
			mailSenderVo.setIsVerpEnabled(rs.getString("IsVerpEnabled"));
			mailSenderVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			mailSenderVo.setUpdtUserId(rs.getString("UpdtUserId"));
			mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
			return mailSenderVo;
		}
	}
	
	public MailSenderVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * from MailSenderProps where rowId=?";
		Object[] parms = new Object[] {rowId};
		List<?> list = jdbcTemplate.query(sql, parms, new MailSenderPropsMapper());
		if (list.size()>0) {
			return (MailSenderVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<MailSenderVo> getAll() {
		
		String sql = "select * from MailSenderProps ";
		List<MailSenderVo> list = (List<MailSenderVo>)jdbcTemplate.query(sql, new MailSenderPropsMapper());
		return list;
	}
	
	public int update(MailSenderVo mailSenderVo) {
		mailSenderVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(mailSenderVo.getInternalLoopback());
		keys.add(mailSenderVo.getExternalLoopback());
		keys.add(mailSenderVo.getUseTestAddr());
		keys.add(mailSenderVo.getTestFromAddr());
		keys.add(mailSenderVo.getTestToAddr());
		keys.add(mailSenderVo.getTestReplytoAddr());
		keys.add(mailSenderVo.getIsVerpEnabled());
		keys.add(mailSenderVo.getUpdtTime());
		keys.add(mailSenderVo.getUpdtUserId());
		keys.add(mailSenderVo.getRowId());
		
		String sql = "update MailSenderProps set " +
			"InternalLoopback=?," +
			"ExternalLoopback=?," +
			"UseTestAddr=?," +
			"TestFromAddr=?," +
			"TestToAddr=?," +
			"TestReplytoAddr=?," +
			"IsVerpEnabled=?," +
			"UpdtTime=?," +
			"UpdtUserId=? " +
			" where RowId=?";
		
		if (mailSenderVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(mailSenderVo.getOrigUpdtTime());
		}
		int rowsUpadted = jdbcTemplate.update(sql, keys.toArray());
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from MailSenderProps where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = jdbcTemplate.update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MailSenderVo mailSenderVo) {
		mailSenderVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		Object[] parms = {
				mailSenderVo.getInternalLoopback(),
				mailSenderVo.getExternalLoopback(),
				mailSenderVo.getUseTestAddr(),
				mailSenderVo.getTestFromAddr(),
				mailSenderVo.getTestToAddr(),
				mailSenderVo.getTestReplytoAddr(),
				mailSenderVo.getIsVerpEnabled(),
				mailSenderVo.getUpdtTime(),
				mailSenderVo.getUpdtUserId()
			};
		
		String sql = "INSERT INTO MailSenderProps (" +
			"InternalLoopback," +
			"ExternalLoopback," +
			"UseTestAddr," +
			"TestFromAddr," +
			"TestToAddr," +
			"TestReplytoAddr," +
			"IsVerpEnabled," +
			"UpdtTime," +
			"UpdtUserId " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		int rowsInserted = jdbcTemplate.update(sql, parms);
		mailSenderVo.setRowId(retrieveRowId());
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return jdbcTemplate.queryForInt(getRowIdSql());
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

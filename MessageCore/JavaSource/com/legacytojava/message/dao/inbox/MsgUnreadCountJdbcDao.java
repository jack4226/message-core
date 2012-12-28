package com.legacytojava.message.dao.inbox;

import java.util.ArrayList;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

public class MsgUnreadCountJdbcDao implements MsgUnreadCountDao {
	protected static final Logger logger = Logger.getLogger(MsgUnreadCountJdbcDao.class);
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	public int updateInboxUnreadCount(int delta) {
		String sql = 
			"update MsgUnreadCount set InboxUnreadCount = (InboxUnreadCount + " + delta + ")";
		int rowsUpdated = jdbcTemplate.update(sql);
		return rowsUpdated;
	}

	public int updateSentUnreadCount(int delta) {
		String sql = 
			"update MsgUnreadCount set SentUnreadCount = (SentUnreadCount + " + delta + ")";
		int rowsUpdated = jdbcTemplate.update(sql);
		return rowsUpdated;
	}

	public int resetInboxUnreadCount(int inboxUnreadCount) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(inboxUnreadCount);

		String sql = "update MsgUnreadCount set " +
				"InboxUnreadCount=?";
		
		int rowsUpdated = jdbcTemplate.update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int resetSentUnreadCount(int sentUnreadCount) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(sentUnreadCount);

		String sql = "update MsgUnreadCount set " +
				"SentUnreadCount=?";
		
		int rowsUpdated = jdbcTemplate.update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int resetUnreadCounts(int inboxUnreadCount, int sentUnreadCount) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(inboxUnreadCount);
		fields.add(sentUnreadCount);

		String sql = "update MsgUnreadCount set " +
				"InboxUnreadCount=?," +
				"SentUnreadCount=?";
		
		int rowsUpdated = jdbcTemplate.update(sql, fields.toArray());
		return rowsUpdated;
	}

	public int selectInboxUnreadCount() {
		String sql = "select InboxUnreadCount from MsgUnreadCount";
		int unreadCount = jdbcTemplate.queryForInt(sql);
		return unreadCount;
	}

	public int selectSentUnreadCount() {
		String sql = "select SentUnreadCount from MsgUnreadCount";
		int unreadCount = jdbcTemplate.queryForInt(sql);
		return unreadCount;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}
}

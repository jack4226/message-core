package com.legacytojava.message.dao.customer;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

public class CustSequenceJdbcDao implements CustSequenceDao {
	protected static final Logger logger = Logger.getLogger(CustSequenceJdbcDao.class);
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	public long findNextValue() {
		/* simulate a sequence table */
		String sql1 = "update CustSequence set seqId = LAST_INSERT_ID(seqId + 1)";
		String sql2 = "select LAST_INSERT_ID()";
		try {
			jdbcTemplate.update(sql1);
			long nextValue = jdbcTemplate.queryForLong(sql2);
			return nextValue;
		}
		catch (Exception e) {
			logger.error("Exception caught, repair the table.", e);
			return repair();
		}
	}
	
	/*
	 * perform delete and insert to eliminate multiple rows
	 */
	private long repair() {
		logger.info("repair() - perform delete and insert...");
		String sql = 
			"select max(SeqId) from CustSequence ";
		long currValue = jdbcTemplate.queryForLong(sql);
		sql = "delete from CustSequence";
		jdbcTemplate.update(sql);
		sql = "insert into CustSequence (SeqId) values(" +(currValue + 1)+ ")";
		jdbcTemplate.update(sql);
		sql = "select LAST_INSERT_ID()";
		long nextValue = jdbcTemplate.queryForLong(sql);
		return nextValue;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}
}

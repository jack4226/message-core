package com.legacytojava.message.bo.mailreader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.SpringUtil;

/**
 * This class is used to check duplicate messages. It uses derby database as its
 * persistent storage. For every email received, method isDuplicate() should be
 * called with its SMTP message-id, and the return should be evaluated to see if
 * it's a duplicate message.
 */
@Component("duplicateCheck")
public class DuplicateCheckJdbcDao implements DuplicateCheckDao {
	static final Logger logger = Logger.getLogger(DuplicateCheckJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource mysqlDataSource;
	private String purgeAfter = "24";

	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	/**
	DROP INDEX MSGIDDUP_index;
	DROP TABLE MSGIDDUP;
	CREATE TABLE MSGIDDUP (
		message_id varchar(200) not null primary key,
		add_time timestamp not null);
	CREATE INDEX MSGIDDUP_index ON MSGIDDUP (
		add_time);
	
	// to connect to embedded CloudScape
	use ("jdbc:cloudscape:dbcache;create=true");
	*/
	
	public static void main(String[] args) {
		DuplicateCheckDao dCheck = (DuplicateCheckDao) SpringUtil.getDaoAppContext()
				.getBean("duplicateCheck");
		try {
			String msgId = "1223344556788990";
			boolean isDuplicate = dCheck.isDuplicate(msgId);
			System.out.println("Is " + msgId + " duplicate? " + isDuplicate);
			
			dCheck.process(new PurgeTask());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private static class PurgeTask extends java.util.TimerTask {
        public void run() {
            logger.info("Time's up!");
        }
    }

	/**
	 * processor entry point called by Timer task
	 * 
	 * @param req -
	 *            a java TimerTask object
	 * @throws Exception
	 *             if any error
	 */
	public void process(Object req) throws Exception {
		if (req instanceof java.util.TimerTask) {
			// perform purge (on MSGIDDUP table) hourly
			logger.info("Prepare to purge aged records...");
			int hours=0;
			try	{
				hours=Integer.parseInt(purgeAfter);
			}
			catch (NumberFormatException e)	{
				hours=24;
			}
			purge((hours<1?1:hours)); // purge records older than 24 hours
		}
	}
	
	/**
	 * @param msg_id -
	 *            smtp message id
	 * @return - true - duplicate key
	 */
	public synchronized boolean isDuplicate(String msg_id) {
		String sql = "insert into MSGIDDUP values "
				+ "(?"
				+ ", ?"
				+ ")";
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msg_id);
		fields.add(new java.sql.Timestamp(System.currentTimeMillis()));
		try {
			getJdbcTemplate().update(sql, fields.toArray());
		}
		catch(DuplicateKeyException dke) {
			logger.error("DuplicateKeyException caught: " + dke.getMessage());
			return true;
		}
		catch (BadSqlGrammarException e) {
			logger.error("Exception caught during insert", e);
			if (e.getMessage().toLowerCase().indexOf("does not exist") >= 0 /* Derby */
					|| e.getMessage().toLowerCase().indexOf("doesn't exist") >= 0 /* MySQL */) {
				// table does not exist, create one.
				try {
					String sql_create =
						"create table MSGIDDUP ("
						+ "message_id varchar(200) not null primary key "
						+ ",add_time timestamp not null"
						+ ")";
					getJdbcTemplate().update(sql_create);
					logger.info("DuplicateCheck: table MSGIDDUP and its index created.");
				}
				catch (Exception se) {
					logger.error("Failed to create MSGIDDUP table/index", se);
				}
			}
			else if (e.getMessage().indexOf("duplicate key") >= 0 /* Derby */
					|| e.getMessage().indexOf("Duplicate entry") >= 0 /* MySQL */) {
				// deprecated - should be caught by DuplicateKeyException
				return true;
			}
		}
		return false;
	}
	
	/**
	 * purge records older than specified hours
	 * 
	 * @param hours -
	 *            records older than the hours will be purged
	 */
	public synchronized void purge(int hours) {
		logger.info("purge() - purge records older than " + hours + " hours...");
		// prepare for delete of aged records
		String sql =
			"delete from MSGIDDUP"
			+ " where ADD_TIME < ?";
		
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.HOUR, -hours);
		Date go_back=calendar.getTime();

		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(new java.sql.Timestamp(go_back.getTime()));
		int rows = getJdbcTemplate().update(sql, fields.toArray());
		logger.info("purge() - number of records purged: "+rows);
	}

	public String getPurgeAfter() {
		return purgeAfter;
	}

	public void setPurgeAfter(String purgeAfter) {
		this.purgeAfter = purgeAfter;
	}
}

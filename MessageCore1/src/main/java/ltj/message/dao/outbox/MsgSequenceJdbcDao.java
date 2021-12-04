package ltj.message.dao.outbox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;

@Component("msgSequenceDao")
public class MsgSequenceJdbcDao extends AbstractDao implements MsgSequenceDao {
	protected static final Logger logger = LogManager.getLogger(MsgSequenceJdbcDao.class);
	
	@Override
	public long findNextValue() {
		/* simulate a sequence table */
		String sql1 = "update msg_sequence set seq_id = LAST_INSERT_ID(seq_id + 1)";
		String sql2 = "select LAST_INSERT_ID()";
		try {
			getJdbcTemplate().update(sql1);
			long nextValue = getJdbcTemplate().queryForObject(sql2, Long.class);
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
			"select max(seq_id) from msg_sequence ";
		long currValue = getJdbcTemplate().queryForObject(sql, Long.class);
		sql = "delete from msg_sequence";
		getJdbcTemplate().update(sql);
		sql = "insert into msg_sequence (seq_id) values(" +(currValue + 1)+ ")";
		getJdbcTemplate().update(sql);
		sql = "select LAST_INSERT_ID()";
		long nextValue = getJdbcTemplate().queryForObject(sql, Long.class);
		return nextValue;
	}
}

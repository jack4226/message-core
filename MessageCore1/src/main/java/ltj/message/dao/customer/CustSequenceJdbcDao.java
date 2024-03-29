package ltj.message.dao.customer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import ltj.message.dao.abstrct.AbstractDao;

@Repository
@Component("custSequenceDao")
public class CustSequenceJdbcDao extends AbstractDao implements CustSequenceDao {
	protected static final Logger logger = LogManager.getLogger(CustSequenceJdbcDao.class);
	
	@Override
	public long findNextValue() {
		/* simulate a sequence table */
		String sql1 = "update cust_sequence set seq_id = LAST_INSERT_ID(seq_id + 1)";
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
			"select max(seq_id) from cust_sequence ";
		long currValue = getJdbcTemplate().queryForObject(sql, Long.class);
		sql = "delete from cust_sequence";
		getJdbcTemplate().update(sql);
		sql = "insert into cust_sequence (seq_id) values(" +(currValue + 1)+ ")";
		getJdbcTemplate().update(sql);
		sql = "select LAST_INSERT_ID()";
		long nextValue = getJdbcTemplate().queryForObject(sql, Long.class);
		return nextValue;
	}
}

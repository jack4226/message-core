package ltj.message.dao.emailaddr;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.PagingAddrVo;
import ltj.message.vo.PagingVo;
import ltj.message.vo.emailaddr.EmailAddressVo;

@Component("emailAddressDao")
public class EmailAddressJdbcDao extends AbstractDao implements EmailAddressDao {
	static final Logger logger = Logger.getLogger(EmailAddressJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Override
	public EmailAddressVo getByAddrId(long addrId) {
		String sql = "select *, EmailAddr as CurrEmailAddr, UpdtTime as OrigUpdtTime " +
				"from email_address where emailAddrId=?";
		Object[] parms = new Object[] { addrId };
		try {
			EmailAddressVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public EmailAddressVo getByAddress(String address) {
		String sql = "select *, EmailAddr as CurrEmailAddr, UpdtTime as OrigUpdtTime " +
				"from email_address where EmailAddr=?";
		String emailAddress = EmailAddrUtil.removeDisplayName(address);
		Object[] parms = new Object[] { emailAddress };
		List<EmailAddressVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public EmailAddressVo getRandomRecord() {
		String sql = 
				"select * from email_address " +
				" where EmailAddrId >= (RAND() * (select max(EmailAddrId) from email_address)) " +
				" order by EmailAddrId limit 1 ";
			
		List<EmailAddressVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		if (list.size() > 0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}

	@Override
	public int getEmailAddressCount(PagingAddrVo vo) {
		List<Object> parms = new ArrayList<>();
		String whereSql = buildWhereClause(vo, parms);
		String sql = "select count(*) from email_address a "
				+ " LEFT OUTER JOIN customer_tbl b on a.EmailAddrId=b.email_addr_id "
				+ " LEFT OUTER JOIN email_subscrpt c on a.EmailAddrId=c.EmailAddrId "
				+ whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}

	@Override
	public List<EmailAddressVo> getEmailAddrsWithPaging(PagingAddrVo vo) {
		List<Object> parms = new ArrayList<>();
		String whereSql = buildWhereClause(vo, parms);
		/*
		 * paging logic, sort by Email Address
		 */
		String fetchOrder = "asc";
		int pageSize = vo.getPageSize();
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		} else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getStrIdLast() != null) {
				whereSql += CRIT[parms.size()] + " a.EmailAddr > ? ";
				parms.add(vo.getStrIdLast());
			}
		} else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getStrIdFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.EmailAddr < ? ";
				parms.add(vo.getStrIdFirst());
				fetchOrder = "desc";
			}
		} else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			int rows = getEmailAddressCount(vo);
			pageSize = rows % vo.getPageSize();
			if (pageSize == 0) {
				pageSize = Math.min(rows, vo.getPageSize());
			}
			fetchOrder = "desc";
//			List<EmailAddressVo> lastList = new ArrayList<EmailAddressVo>();
//			vo.setPageAction(PagingVo.PageAction.NEXT);
//			while (true) {
//				List<EmailAddressVo> nextList = getEmailAddrsWithPaging(vo);
//				if (!nextList.isEmpty()) {
//					lastList = nextList;
//					vo.setStrIdLast(nextList.get(nextList.size() - 1).getEmailAddr());
//				} else {
//					break;
//				}
//			}
//			return lastList;
		} else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getStrIdFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.EmailAddr >= ? ";
				parms.add(vo.getStrIdFirst());
			}
		}
		String sql = "select a.EmailAddrId, a.EmailAddr, a.OrigEmailAddr, a.StatusId, "
				+ " a.StatusChangeTime, a.StatusChangeUserId, a.BounceCount, "
				+ " a.LastBounceTime, a.LastSentTime, a.LastRcptTime, a.AcceptHtml, "
				+ " a.UpdtTime, a.UpdtUserId, a.EmailAddr as CurrEmailAddr, a.UpdtTime as OrigUpdtTime, "
				+ " b.cust_id, b.first_name, b.middle_name, b.last_name, "
				+ " sum(c.SentCount) as SentCount, sum(c.OpenCount) as OpenCount, "
				+ " sum(c.ClickCount) as ClickCount "
				+ "from email_address a "
				+ " LEFT OUTER JOIN customer_tbl b on a.EmailAddrId=b.email_addr_id "
				+ " LEFT OUTER JOIN email_subscrpt c on a.EmailAddrId=c.EmailAddrId "
				+ whereSql
				+ "group by "
				+ " a.EmailAddrId, a.EmailAddr, a.OrigEmailAddr, a.StatusId, a.StatusChangeTime, "
				+ " a.StatusChangeUserId, a.BounceCount, a.LastBounceTime, a.LastSentTime, "
				+ " a.LastRcptTime, a.AcceptHtml, a.UpdtTime, a.UpdtUserId, "
				+ " b.cust_id, b.first_name, b.middle_name, b.last_name "
				+ " order by a.EmailAddr "
				+ fetchOrder
				+ " limit "
				+ pageSize;
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<EmailAddressVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if ("desc".equals(fetchOrder)) {
			// reverse the list
			Collections.reverse(list);
		}
		if (!list.isEmpty()) {
			vo.setStrIdFirst(list.get(0).getEmailAddr());
			vo.setStrIdLast(list.get(list.size() - 1).getEmailAddr());
		}
		return list;
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and " };

	private String buildWhereClause(PagingAddrVo vo, List<Object> parms) {
		String whereSql = "";
		if (!StringUtil.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " a.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		// search by address
		if (StringUtils.isNotBlank(vo.getEmailAddr())) {
			String addr = vo.getEmailAddr().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " a.OrigEmailAddr LIKE ? ";
				parms.add("%" + addr + "%");
			} else {
				//String regex = (addr + "").replaceAll("[ ]+", ".+");
				String regex = (addr + "").replaceAll("[ ]+", "|"); // any word
				whereSql += CRIT[parms.size()] + " a.OrigEmailAddr REGEXP ? ";
				parms.add(regex);
			}
		}
//		if (parms.isEmpty()) { // make sure the parameter list is not empty
//			whereSql += CRIT[parms.size()] + " a.EmailAddrId >= ? ";
//			parms.add(Long.valueOf(0));
//		}
		return whereSql;
	}

	@Override
	public long getEmailAddrIdForPreview() {
		String sql = "SELECT min(e.emailaddrid) as emailaddrid "
				+ " FROM email_address e, customer_tbl c "
				+ " where e.emailaddrid=c.email_addr_id ";
		Long emailAddrId = getJdbcTemplate().queryForObject(sql, Long.class);
		if (emailAddrId == null) {
			sql = "SELECT min(e.emailaddrid) as emailaddrid "
					+ " FROM email_address e ";
			emailAddrId = (Long) getJdbcTemplate().queryForObject(sql, Long.class);
		}
		if (emailAddrId == null) {
			emailAddrId = Long.valueOf(0);
		}
		return emailAddrId.longValue();
	}

	/**
	 * find by email address. if it does not exist, add it to database.
	 */
	// @Transactional(isolation=Isolation.REPEATABLE_READ,propagation=Propagation.REQUIRED)
	/*
	 * Standard JPA does not support custom isolation levels.
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	/*
	 * Just did the test. The Isolation level is still set to READ COMMITTED
	 * inside the DAO method with @Transactional set as you described it. JDBC
	 * Statement javadocs also states that isolation level can only be set when
	 * the transaction is started, not after. For now I've just created a method
	 * that check for the isolation level and throws a RuntimeException if it's
	 * not SERIALIZABLE. But this requires an extra trip to the DB to query the
	 * current isolation level. � David Parks
	 */
	@Override
	public EmailAddressVo findByAddress(String address) {
		return findByAddress(address, 0);
		// return findByAddressSP(address);
	}

	private static Object lock = new Object();

	/*
	 * We can also apply "synchronized" at method level, and that will eliminate
	 * concurrency issue as this class is a singleton. (!!!Tried under JBoss and
	 * it failed to eliminate concurrency issue for some reason.)
	 * 
	 * Update: I was running JUnit tests (SendMailBoTest.java) from another JVM
	 * to feed the MailSender queue which is consumed by MailSender MDB running
	 * in JBoss. So the concurrency issue was really happening between two JVMs,
	 * that's why "synchronized" block did not work.
	 * 
	 * Update: running MailSenderListener from JBoss, multiple MDB sessions were
	 * started, and each MDB session probably had its own instance as the method
	 * was obviously running in parallel, applying "synchronized" at the method
	 * level did not help. The JMS messages were then rolled back by MDB, and
	 * were processed successfully after one or two re-deliveries.
	 */
	private EmailAddressVo findByAddress(String address, int retries) {
		EmailAddressVo vo = getByAddress(address);
		if (vo == null) { // record not found, insert one
			synchronized (lock) {
				// concurrency issue still pops up but it is much better
				// controlled
				Timestamp updtTime = new Timestamp(System.currentTimeMillis());
				EmailAddressVo emailAddressVo = new EmailAddressVo();
				emailAddressVo.setEmailAddr(address);
				emailAddressVo.setBounceCount(0);
				emailAddressVo.setStatusId(StatusId.ACTIVE.value());
				emailAddressVo.setStatusChangeTime(updtTime);
				emailAddressVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
				emailAddressVo.setAcceptHtml(Constants.Y);
				emailAddressVo.setUpdtTime(updtTime);
				emailAddressVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
				try {
					insert(emailAddressVo);
					return emailAddressVo;
				} catch (DuplicateKeyException e) {
					logger.error("findByAddress() - DuplicateKeyException caught", e);
					if (retries < 0) {
						// retry once may overcome concurrency issue. (the retry
						// never worked and the reason might be that it is under
						// a same transaction). So no retry from now on.
						logger.info("findByAddress() - duplicate key error, retry...");
						return findByAddress(address, retries + 1);
					} else {
						throw e;
					}
				} catch (DataIntegrityViolationException e) {
					// shouldn't reach here, should be caught by DuplicateKeyException
					logger.error("findByAddress() - DataIntegrityViolationException caught", e);
					String err = e.toString() + "";
					if (err.toLowerCase().indexOf("duplicate entry") > 0 && retries < 0) {
						// retry once may overcome concurrency issue. (the retry
						// never worked and the reason might be that it is under
						// a same transaction). So no retry from now on.
						logger.info("findByAddress() - duplicate key error, retry...");
						return findByAddress(address, retries + 1);
					} else {
						throw e;
					}
				} catch (CannotAcquireLockException e) {
					logger.error("findByAddress() - CannotAcquireLockException caught", e);
					throw e;
				}
			} // end of synchronized block
		} else { // found a record
			return vo;
		}
	}

	EmailAddressVo findByAddress_deadlock(String address, int retries) {
		EmailAddressVo vo = getByAddress(address);
		if (vo == null) { // record not found, insert one
			Timestamp updtTime = new Timestamp(System.currentTimeMillis());
			EmailAddressVo emailAddressVo = new EmailAddressVo();
			emailAddressVo.setEmailAddr(address);
			emailAddressVo.setBounceCount(0);
			emailAddressVo.setStatusId(StatusId.ACTIVE.value());
			emailAddressVo.setStatusChangeTime(updtTime);
			emailAddressVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
			emailAddressVo.setAcceptHtml(Constants.Y);
			emailAddressVo.setUpdtTime(updtTime);
			emailAddressVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			// concurrency issue still pops up but it is much better controlled
			synchronized (lock) {
				insert(emailAddressVo, true);
				// return emailAddrVo;
			} // end of synchronized block
			return getByAddress(address);
		} else { // found a record
			return vo;
		}
	}

	private static class FindByAddressProcedure extends StoredProcedure {
		public FindByAddressProcedure(DataSource dataSource, String sprocName) {
			super(dataSource, sprocName);
			// declareParameter(new SqlReturnResultSet("rs", new
			// EmailAddrMapper()));
			declareParameter(new SqlParameter("iEmailAddr", Types.VARCHAR));
			declareParameter(new SqlParameter("iOrigEmailAddr", Types.VARCHAR));
			declareParameter(new SqlOutParameter("oEmailAddrId", Types.DECIMAL));
			declareParameter(new SqlOutParameter("oEmailAddr", Types.VARCHAR));
			declareParameter(new SqlOutParameter("oOrigEmailAddr", Types.VARCHAR));
			declareParameter(new SqlOutParameter("oStatusId", Types.CHAR));
			declareParameter(new SqlOutParameter("oStatusChangeTime", Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oStatusChangeUserId", Types.VARCHAR));
			declareParameter(new SqlOutParameter("oBounceCount", Types.INTEGER));
			declareParameter(new SqlOutParameter("oLastBounceTime", Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oLastSentTime", Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oLastRcptTime", Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oAcceptHtml", Types.CHAR));
			declareParameter(new SqlOutParameter("oUpdtTime", Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oUpdtUserId", Types.VARCHAR));
			compile();
		}

		public Map<String, Object> execute(String address) {
			Map<String, Object> inputs = new HashMap<String, Object>();
			inputs.put("iEmailAddr", EmailAddrUtil.removeDisplayName(address));
			inputs.put("iOrigEmailAddr", address);
			Map<String, Object> output = super.execute(inputs);
			return output;
		}
	}

	/*
	 * The stored procedure did not resolve the "Duplicate Key" problem.
	 */
	EmailAddressVo findByAddressSP(String address) {
		FindByAddressProcedure sp = new FindByAddressProcedure(getJdbcTemplate().getDataSource(), "FindByAddress");
		Map<String, Object> map = sp.execute(address);
		EmailAddressVo vo = new EmailAddressVo();
		vo.setEmailAddrId(((BigDecimal) map.get("oEmailAddrId")).longValue());
		vo.setEmailAddr((String) map.get("oOrigEmailAddr"));
		vo.setStatusId((String) map.get("oStatusId"));
		vo.setStatusChangeTime((Timestamp) map.get("oStatusChangeTime"));
		vo.setStatusChangeUserId((String) map.get("oStatusChangeUserId"));
		vo.setBounceCount(((Integer) map.get("oBounceCount")));
		vo.setLastBounceTime((Timestamp) map.get("oLastBounceTime"));
		vo.setLastSentTime((Timestamp) map.get("oLastSentTime"));
		vo.setLastRcptTime((Timestamp) map.get("oLastRcptTime"));
		vo.setAcceptHtml((String) map.get("oAcceptHtml"));
		vo.setUpdtTime((Timestamp) map.get("oUpdtTime"));
		vo.setUpdtUserId((String) map.get("oUpdtUserId"));

		vo.setCurrEmailAddr(vo.getEmailAddr());
		vo.setOrigUpdtTime(vo.getUpdtTime());
		return vo;
	}

	@Override
	public EmailAddressVo getFromByMsgId(Long msgId) {
		String sql = "select a.*, b.RuleName from email_address a "
				+ " inner join msg_inbox b on a.EmailAddrId = b.FromAddrId "
				+ " where" + " b.MsgId = ?";
		Object[] parms = new Object[] { msgId };
		List<EmailAddressVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	@Override
	public EmailAddressVo getToByMsgId(Long msgId) {
		String sql = "select a.*, b.RuleName from email_address a "
				+ " inner join msg_inbox b on a.EmailAddrId = b.ToAddrId "
				+ " where" + " b.MsgId = ?";
		Object[] parms = new Object[] { msgId };
		List<EmailAddressVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	@Override
	public EmailAddressVo saveEmailAddress(String address) {
		return findByAddress(address);
	}

	@Override
	public int update(EmailAddressVo emailAddressVo) {
		emailAddressVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		emailAddressVo.setOrigEmailAddr(emailAddressVo.getEmailAddr());
		emailAddressVo.setEmailAddr(EmailAddrUtil.removeDisplayName(emailAddressVo.getEmailAddr()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailAddressVo);
		String sql = MetaDataUtil.buildUpdateStatement("email_address", emailAddressVo);
		if (emailAddressVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}

		int rowsUpadted = 0;
		try { 
			rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
			emailAddressVo.setCurrEmailAddr(emailAddressVo.getEmailAddr());
			emailAddressVo.setOrigUpdtTime(emailAddressVo.getUpdtTime());
		}
		catch (DeadlockLoserDataAccessException e) {
			logger.error("DeadlockLoserDataAccessException caught", e) ;
			// TODO what to do with this?
		}
		return rowsUpadted;
	}

	@Override
	public int updateLastRcptTime(long addrId) {
		List<Object> keys = new ArrayList<>();
		keys.add(new Timestamp(System.currentTimeMillis()));
		keys.add(addrId);

		String sql = "update email_address set " + " LastRcptTime=? "
				+ " where emailAddrId=?";

		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}

	@Override
	public int updateLastSentTime(long addrId) {
		List<Object> keys = new ArrayList<>();
		keys.add(new Timestamp(System.currentTimeMillis()));
		keys.add(addrId);

		String sql = "update email_address set " + " LastSentTime=? "
				+ " where emailAddrId=?";

		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}

	@Override
	public int updateAcceptHtml(long addrId, boolean acceptHtml) {
		List<Object> keys = new ArrayList<>();
		keys.add(acceptHtml ? Constants.Y : Constants.N);
		keys.add(addrId);

		String sql = "update email_address set " + " AcceptHtml=? "
				+ " where emailAddrId=?";

		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}

	@Override
	public int updateBounceCount(long emailAddrId, int count) {
		String sql = "update email_address set BounceCount=?, UpdtTime=? where emailAddrId=?";
		Object[] parms = new Object[] { count, new Timestamp(System.currentTimeMillis()), emailAddrId };
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	@Override
	public int updateBounceCount(EmailAddressVo emailAddressVo) {
		emailAddressVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		List<Object> keys = new ArrayList<>();

		String sql = "update email_address set BounceCount=?,";
		emailAddressVo.setBounceCount(emailAddressVo.getBounceCount() + 1);
		keys.add(emailAddressVo.getBounceCount());

		if (emailAddressVo.getBounceCount() > Constants.BOUNCE_SUSPEND_THRESHOLD) {
			if (!StatusId.SUSPENDED.value().equals(emailAddressVo.getStatusId())) {
				emailAddressVo.setStatusId(StatusId.SUSPENDED.value());
				if (!StringUtil.isEmpty(emailAddressVo.getUpdtUserId())) {
					emailAddressVo.setStatusChangeUserId(emailAddressVo.getUpdtUserId());
				} else {
					emailAddressVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
				}
				emailAddressVo.setStatusChangeTime(emailAddressVo.getUpdtTime());
				sql += "StatusId=?," + "StatusChangeUserId=?," + "StatusChangeTime=?,";
				keys.add(emailAddressVo.getStatusId());
				keys.add(emailAddressVo.getStatusChangeUserId());
				keys.add(emailAddressVo.getStatusChangeTime());
			}
		}

		sql += "UpdtTime=?," + "UpdtUserId=? " + " where emailAddrId=?";

		keys.add(emailAddressVo.getUpdtTime());
		keys.add(emailAddressVo.getUpdtUserId());
		keys.add(emailAddressVo.getEmailAddrId());

		if (emailAddressVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(emailAddressVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		emailAddressVo.setOrigUpdtTime(emailAddressVo.getUpdtTime());
		return rowsUpadted;
	}

	@Override
	public int deleteByAddrId(long addrId) {
		String sql = "delete from email_address where emailAddrId=?";
		Object[] parms = new Object[] { addrId + "" };
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}

	@Override
	public int deleteByAddress(String address) {
		String sql = "delete from email_address where emailAddr=?";
		Object[] parms = new Object[] { address };
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}

	@Override
	public int insert(EmailAddressVo emailAddressVo) {
		return insert(emailAddressVo, true);
	}

	/*
	 * When "withUpdate" is true, the SQL will perform update if record already
	 * exists, but the "update" will not return the RowId. Use with caution.
	 */
	private int insert(EmailAddressVo emailAddressVo, boolean withUpdate) {
		emailAddressVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		emailAddressVo.setOrigEmailAddr(emailAddressVo.getEmailAddr());
		emailAddressVo.setEmailAddr(EmailAddrUtil.removeDisplayName(emailAddressVo.getEmailAddr()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailAddressVo);
		String sql = MetaDataUtil.buildInsertStatement("email_address", emailAddressVo);
		if (withUpdate) {
			sql += " ON duplicate KEY UPDATE UpdtTime=:updtTime ";
		}

		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		emailAddressVo.setEmailAddrId(retrieveRowId());
		emailAddressVo.setCurrEmailAddr(emailAddressVo.getEmailAddr());
		emailAddressVo.setOrigUpdtTime(emailAddressVo.getUpdtTime());
		return rowsInserted;
	}

	/**
	 * use KeyHolder to get generated Row Id.
	 */
	public int insert_keyholder(final EmailAddressVo emailAddressVo, final boolean withUpdate) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int rowsInserted = getJdbcTemplate().update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				emailAddressVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
				ArrayList<Object> keys = new ArrayList<Object>();
				keys.add(EmailAddrUtil.removeDisplayName(emailAddressVo.getEmailAddr()));
				keys.add(emailAddressVo.getEmailAddr());
				keys.add(emailAddressVo.getStatusId());
				keys.add(emailAddressVo.getStatusChangeTime());
				keys.add(emailAddressVo.getStatusChangeUserId());
				keys.add(emailAddressVo.getBounceCount());
				keys.add(emailAddressVo.getLastBounceTime());
				keys.add(emailAddressVo.getLastSentTime());
				keys.add(emailAddressVo.getLastRcptTime());
				keys.add(emailAddressVo.getAcceptHtml());
				keys.add(emailAddressVo.getUpdtTime());
				keys.add(emailAddressVo.getUpdtUserId());

				String sql = "INSERT INTO email_address (" + "EmailAddr,"
						+ "OrigEmailAddr," + "StatusId," + "StatusChangeTime,"
						+ "StatusChangeUserId," + "BounceCount,"
						+ "LastBounceTime," + "LastSentTime," + "LastRcptTime,"
						+ "AcceptHtml," + "UpdtTime," + "UpdtUserId "
						+ ") VALUES (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
						+ ",?, ? " + ")";
				if (withUpdate) {
					sql += " ON duplicate KEY UPDATE UpdtTime=?";
					keys.add(emailAddressVo.getUpdtTime());
				}
				PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
				for (int i = 0; i < keys.size(); i++) {
					ps.setObject(i + 1, keys.get(i));
				}
				return ps;
			}
		}, keyHolder);
		Number number = keyHolder.getKey();
		emailAddressVo.setEmailAddrId(number.longValue());
		emailAddressVo.setCurrEmailAddr(emailAddressVo.getEmailAddr());
		emailAddressVo.setOrigUpdtTime(emailAddressVo.getUpdtTime());
		return rowsInserted;
	}
}
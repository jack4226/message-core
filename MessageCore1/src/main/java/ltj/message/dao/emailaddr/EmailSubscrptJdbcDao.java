package ltj.message.dao.emailaddr;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.PagingSbsrVo;
import ltj.message.vo.PagingVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;

@Component("emailSubscrptDao")
public class EmailSubscrptJdbcDao extends AbstractDao implements EmailSubscrptDao {
	static final Logger logger = Logger.getLogger(EmailSubscrptJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressDao emailAddressDao;
	
	/**
	 * Set Subscribed field to "Yes". Called when an subscription is received
	 * from an email address.
	 */
	@Override
	public int subscribe(long addrId, String listId) {
		EmailSubscrptVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo == null) { // insert
			vo = new EmailSubscrptVo();
			vo.setEmailAddrId(addrId);
			vo.setListId(listId);
			vo.setSubscribed(Constants.Y);
			rowsAffected = insert(vo);
		}
		else { // update
			if (!(Constants.Y.equals(vo.getSubscribed()))) {
				vo.setSubscribed(Constants.Y);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	@Override
	public int subscribe(String addr, String listId) {
		EmailAddressVo addrVo = getEmailAddressDao().findByAddress(addr);
		return subscribe(addrVo.getEmailAddrId(), listId);
	}
	
	/**
	 * Set Subscribed field to "No". Called when a removal request is received
	 * from either an email address or a public subscription request web page.
	 */
	@Override
	public int unsubscribe(long addrId, String listId) {
		EmailSubscrptVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo != null) { // update
			if (!Constants.N.equals(vo.getSubscribed())) {
				vo.setSubscribed(Constants.N);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}

	@Override
	public int unsubscribe(String addr, String listId) {
		EmailAddressVo addrVo = getEmailAddressDao().getByAddress(addr);
		if (addrVo != null) {
			return unsubscribe(addrVo.getEmailAddrId(), listId);
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Set Subscribed field to "Pending". Called when a subscription is received
	 * from a public subscription request web page.
	 */
	@Override
	public int optInRequest(long addrId, String listId) {
		EmailSubscrptVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo == null) { // insert
			vo = new EmailSubscrptVo();
			vo.setEmailAddrId(addrId);
			vo.setListId(listId);
			vo.setSubscribed(StatusId.PENDING.value());
			rowsAffected = insert(vo);
		}
		else { // update
			if (!Constants.Y.equals(vo.getSubscribed())
					&& !StatusId.PENDING.value().equals(vo.getSubscribed())) {
				vo.setSubscribed(StatusId.PENDING.value());
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	@Override
	public int optInRequest(String addr, String listId) {
		EmailAddressVo addrVo = getEmailAddressDao().findByAddress(addr);
		return optInRequest(addrVo.getEmailAddrId(), listId);
	}
	
	/**
	 * Set Subscribed field to "Yes". Called when a subscription is received
	 * from public subscription confirmation web page.
	 */
	@Override
	public int optInConfirm(long addrId, String listId) {
		EmailSubscrptVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo != null) { // update
			if (StatusId.PENDING.value().equals(vo.getSubscribed())) {
				vo.setSubscribed(Constants.Y);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	@Override
	public int optInConfirm(String addr, String listId) {
		EmailAddressVo addrVo = getEmailAddressDao().findByAddress(addr);
		return optInConfirm(addrVo.getEmailAddrId(), listId);
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };
	
	@Override
	public int getSubscriberCount(PagingSbsrVo vo) {
		List<Object> parms = new ArrayList<>();
		String whereSql = buildWhereClause(vo, parms);
		String sql = 
			"select count(*) " +
			" from email_subscrpt a " +
				" join email_address b on a.EmailAddrId=b.EmailAddrId "
				+ " LEFT OUTER JOIN customer_tbl c on a.EmailAddrId=c.EmailAddrId " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	@Override
	public List<EmailSubscrptVo> getSubscribersWithPaging(PagingSbsrVo vo) {
		List<Object> parms = new ArrayList<>();
		String whereSql = buildWhereClause(vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "asc";
		int pageSize = vo.getPageSize();
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getNbrIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId > ? ";
				parms.add(vo.getNbrIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getNbrIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId < ? ";
				parms.add(vo.getNbrIdFirst());
				fetchOrder = "desc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			int rows = getSubscriberCount(vo);
			pageSize = rows % vo.getPageSize();
			if (pageSize == 0) {
				pageSize = Math.min(rows, vo.getPageSize());
			}
			fetchOrder = "desc";
//			List<EmailSubscrptVo> lastList = new ArrayList<EmailSubscrptVo>();
//			vo.setPageAction(PagingVo.PageAction.NEXT);
//			while (true) {
//				List<EmailSubscrptVo> nextList = getSubscribersWithPaging(vo);
//				if (!nextList.isEmpty()) {
//					lastList = nextList;
//					vo.setNbrIdLast(nextList.get(nextList.size() - 1).getEmailAddrId());
//				}
//				else {
//					break;
//				}
//			}
//			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getNbrIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId >= ? ";
				parms.add(vo.getNbrIdFirst());
			}
		}
		String sql = 
			"select a.EmailAddrId, " +
				" b.OrigEmailAddr as EmailAddr, " +
				" b.AcceptHtml, " +
				" a.SentCount, " +
				" a.LastSentTime, " +
				" a.OpenCount, " +
				" a.LastOpenTime, " +
				" a.ClickCount, " +
				" a.LastClickTime, " +
				" a.ListId, " +
				" a.Subscribed," +
				" a.CreateTime, " +
				" c.FirstName, " +
				" c.LastName, " +
				" c.MiddleName " +
			" from email_subscrpt a" +
				" JOIN email_address b ON a.EmailAddrId=b.EmailAddrId " +
				" LEFT OUTER JOIN customer_tbl c on a.EmailAddrId=c.EmailAddrId " +
			whereSql +
			" order by a.EmailAddrId " + fetchOrder +
			" limit " + pageSize;
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<EmailSubscrptVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if ("desc".equals(fetchOrder)) {
			// reverse the list
			Collections.reverse(list);
		}
		if (!list.isEmpty()) {
			vo.setNbrIdFirst(list.get(0).getEmailAddrId());
			vo.setNbrIdLast(list.get(list.size() - 1).getEmailAddrId());
		}
		return list;
	}
	
	private String buildWhereClause(PagingSbsrVo vo, List<Object> parms) {
		String whereSql = "";
		if (StringUtils.isNotBlank(vo.getListId())) {
			whereSql = CRIT[parms.size()] + " a.ListId = ? ";
			parms.add(vo.getListId().trim());
		}
		if (!StringUtil.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " b.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		if (vo.getSubscribed() != null) {
			whereSql += CRIT[parms.size()] + " a.Subscribed = ? ";
			parms.add(vo.getSubscribed() ? Constants.Y : Constants.N);
		}
		// search by address
		if (StringUtils.isNotBlank(vo.getEmailAddr())) {
			String addr = vo.getEmailAddr().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " b.OrigEmailAddr LIKE ? ";
				parms.add("%" + addr + "%");
			}
			else {
				String regex = (addr + "").replaceAll("[ ]+", "|"); // any word
				whereSql += CRIT[parms.size()] + " b.OrigEmailAddr REGEXP ? ";
				parms.add(regex);
			}
		}
		return whereSql;
	}
	
	@Override
	public List<EmailSubscrptVo> getSubscribers(String listId) {
		String sql = 
			"select a.EmailAddrId, " +
				" b.OrigEmailAddr as EmailAddr, " +
				" b.AcceptHtml, " +
				" a.SentCount, " +
				" a.LastSentTime, " +
				" a.OpenCount, " +
				" a.LastOpenTime, " +
				" a.ClickCount, " +
				" a.LastClickTime, " +
				" a.ListId, " +
				" a.Subscribed," +
				" a.CreateTime, " +
				" c.FirstName, " +
				" c.LastName, " +
				" c.MiddleName " +
			" from email_subscrpt a" +
				" JOIN email_address b ON a.EmailAddrId=b.EmailAddrId " +
				" LEFT OUTER JOIN customer_tbl c on a.EmailAddrId=c.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? ";
		Object[] parms = new Object[] {listId, StatusId.ACTIVE.value(), Constants.Y};
		List<EmailSubscrptVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
		return list;
	}
	
	@Override
	public List<EmailSubscrptVo> getSubscribersWithCustomerRecord(String listId) {
		String sql = 
			"select a.EmailAddrId, " +
				" b.OrigEmailAddr as EmailAddr, " +
				" b.AcceptHtml, " +
				" a.SentCount, " +
				" a.LastSentTime, " +
				" a.OpenCount, " +
				" a.LastOpenTime, " +
				" a.ClickCount, " +
				" a.LastClickTime, " +
				" a.ListId, " +
				" a.Subscribed," +
				" a.CreateTime, " +
				" c.FirstName, " +
				" c.LastName, " +
				" c.MiddleName " +
			" from email_subscrpt a" +
				" JOIN email_address b ON a.EmailAddrId=b.EmailAddrId " +
				" JOIN customer_tbl c on a.EmailAddrId=c.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? ";
		Object[] parms = new Object[] {listId, StatusId.ACTIVE.value(), Constants.Y};
		List<EmailSubscrptVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
		return list;
	}
	
	@Override
	public List<EmailSubscrptVo> getSubscribersWithoutCustomerRecord(String listId) {
		String sql = 
			"select a.EmailAddrId, " +
				" b.OrigEmailAddr as EmailAddr, " +
				" b.AcceptHtml, " +
				" a.SentCount, " +
				" a.LastSentTime, " +
				" a.OpenCount, " +
				" a.LastOpenTime, " +
				" a.ClickCount, " +
				" a.LastClickTime, " +
				" a.ListId, " +
				" a.Subscribed," +
				" a.CreateTime " +
			" from email_subscrpt a " +
				" JOIN email_address b ON a.EmailAddrId=b.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? " +
				" and not exists (select 1 from customer_tbl where EmailAddrId=b.EmailAddrId) ";
		Object[] parms = new Object[] {listId, StatusId.ACTIVE.value(), Constants.Y};
		List<EmailSubscrptVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
		return list;
	}
	
	@Override
	public EmailSubscrptVo getByPrimaryKey(long addrId, String listId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from email_subscrpt a " +
				" JOIN email_address b ON a.EmailAddrId=b.EmailAddrId " +
				" where a.EmailAddrId=? and a.ListId=?";
		Object[] parms = new Object[] {addrId, listId};
		try {
			EmailSubscrptVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public EmailSubscrptVo getRandomRecord() {
		String sql = 
				"select * " +
				"from " +
					"email_subscrpt where EmailAddrId >= (RAND() * (select max(EmailAddrId) from email_subscrpt)) " +
				" order by EmailAddrId limit 1 ";
			
		List<EmailSubscrptVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
		if (list.size() > 0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}
	
	@Override
	public EmailSubscrptVo getByAddrAndListId(String addr, String listId) {
		String sql = "SELECT a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from email_subscrpt a " +
				" JOIN email_address b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.ListId=? and b.EmailAddr=?";
		Object[] parms = new Object[] {listId, addr};
		try {
			EmailSubscrptVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}

	}
	
	@Override
	public List<EmailSubscrptVo> getByAddrId(long addrId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from email_subscrpt a " +
				" JOIN email_address b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.EmailAddrId=?";
		Object[] parms = new Object[] {addrId};
		List<EmailSubscrptVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
		return list;
	}
	
	@Override
	public List<EmailSubscrptVo> getByListId(String listId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from email_subscrpt a " +
				" JOIN email_address b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.ListId=?";
		Object[] parms = new Object[] {listId};
		List<EmailSubscrptVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailSubscrptVo>(EmailSubscrptVo.class));
		return list;
	}

	@Override
	public int update(EmailSubscrptVo emailSubscrptVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailSubscrptVo);
		String sql = MetaDataUtil.buildUpdateStatement("email_subscrpt", emailSubscrptVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int updateSentCount(long emailAddrId, String listId) {
		List<Object> keys = new ArrayList<>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update email_subscrpt set " +
			"SentCount=SentCount+1, " +
			"LastSentTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int updateOpenCount(long emailAddrId, String listId) {
		List<Object> keys = new ArrayList<>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update email_subscrpt set " +
			"OpenCount=OpenCount+1, " +
			"LastOpenTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int updateClickCount(long emailAddrId, String listId) {
		List<Object> keys = new ArrayList<>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update email_subscrpt set " +
			"Clickcount=ClickCount+1, " +
			"LastClickTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long addrid, String listId) {
		String sql = "delete from email_subscrpt where EmailAddrId=? and ListId=?";
		Object[] parms = new Object[] {addrid, listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByAddrId(long addrId) {
		String sql = "delete from email_subscrpt where EmailAddrId=?";
		Object[] parms = new Object[] {addrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByListId(String listId) {
		String sql = "delete from email_subscrpt where ListId=?";
		Object[] parms = new Object[] {listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(EmailSubscrptVo emailSubscrptVo) {
		if (emailSubscrptVo.getCreateTime()==null) {
			emailSubscrptVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailSubscrptVo);
		String sql = MetaDataUtil.buildInsertStatement("email_subscrpt", emailSubscrptVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
	private EmailAddressDao getEmailAddressDao() {
		return emailAddressDao;
	}
}

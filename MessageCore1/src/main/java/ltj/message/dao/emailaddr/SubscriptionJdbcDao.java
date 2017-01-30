package ltj.message.dao.emailaddr;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.Constants;
import ltj.message.constant.MsgStatusCode;
import ltj.message.constant.StatusIdCode;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.PagingVo;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.SubscriptionVo;

@Component("subscriptionDao")
public class SubscriptionJdbcDao extends AbstractDao implements SubscriptionDao {
	static final Logger logger = Logger.getLogger(SubscriptionJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddrDao emailAddrDao;
	
	/**
	 * Set Subscribed field to "Yes". Called when an subscription is received
	 * from an email address.
	 */
	@Override
	public int subscribe(long addrId, String listId) {
		SubscriptionVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo == null) { // insert
			vo = new SubscriptionVo();
			vo.setEmailAddrId(addrId);
			vo.setListId(listId);
			vo.setSubscribed(Constants.YES_CODE);
			rowsAffected = insert(vo);
		}
		else { // update
			if (!(Constants.YES_CODE.equals(vo.getSubscribed()))) {
				vo.setSubscribed(Constants.YES_CODE);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	@Override
	public int subscribe(String addr, String listId) {
		EmailAddrVo addrVo = getEmailAddrDao().findByAddress(addr);
		return subscribe(addrVo.getEmailAddrId(), listId);
	}
	
	/**
	 * Set Subscribed field to "No". Called when a removal request is received
	 * from either an email address or a public subscription request web page.
	 */
	@Override
	public int unsubscribe(long addrId, String listId) {
		SubscriptionVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo != null) { // update
			if (!Constants.NO_CODE.equals(vo.getSubscribed())) {
				vo.setSubscribed(Constants.NO_CODE);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}

	@Override
	public int unsubscribe(String addr, String listId) {
		EmailAddrVo addrVo = getEmailAddrDao().getByAddress(addr);
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
		SubscriptionVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo == null) { // insert
			vo = new SubscriptionVo();
			vo.setEmailAddrId(addrId);
			vo.setListId(listId);
			vo.setSubscribed(MsgStatusCode.PENDING);
			rowsAffected = insert(vo);
		}
		else { // update
			if (!Constants.YES_CODE.equals(vo.getSubscribed())
					&& !MsgStatusCode.PENDING.equals(vo.getSubscribed())) {
				vo.setSubscribed(MsgStatusCode.PENDING);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	@Override
	public int optInRequest(String addr, String listId) {
		EmailAddrVo addrVo = getEmailAddrDao().findByAddress(addr);
		return optInRequest(addrVo.getEmailAddrId(), listId);
	}
	
	/**
	 * Set Subscribed field to "Yes". Called when a subscription is received
	 * from public subscription confirmation web page.
	 */
	@Override
	public int optInConfirm(long addrId, String listId) {
		SubscriptionVo vo = getByPrimaryKey(addrId, listId);
		int rowsAffected = 0;
		if (vo != null) { // update
			if (MsgStatusCode.PENDING.equals(vo.getSubscribed())) {
				vo.setSubscribed(Constants.YES_CODE);
				rowsAffected = update(vo);
			}
		}
		return rowsAffected;
	}
	
	@Override
	public int optInConfirm(String addr, String listId) {
		EmailAddrVo addrVo = getEmailAddrDao().findByAddress(addr);
		return optInConfirm(addrVo.getEmailAddrId(), listId);
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };
	
	@Override
	public int getSubscriberCount(String listId, PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(listId, vo, parms);
		String sql = 
			"select count(*) " +
			" from Subscription a " +
				" join EmailAddr b on a.EmailAddrId=b.EmailAddrId " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	@Override
	public List<SubscriptionVo> getSubscribersWithPaging(String listId, PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(listId, vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "asc";
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId > ? ";
				parms.add(vo.getIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId < ? ";
				parms.add(vo.getIdFirst());
				fetchOrder = "desc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<SubscriptionVo> lastList = new ArrayList<SubscriptionVo>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<SubscriptionVo> nextList = getSubscribersWithPaging(listId, vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setIdLast(nextList.get(nextList.size() - 1).getEmailAddrId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.EmailAddrId >= ? ";
				parms.add(vo.getIdFirst());
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
			" from Subscription a" +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" LEFT OUTER JOIN Customers c on a.EmailAddrId=c.EmailAddrId " +
			whereSql +
			" order by a.EmailAddrId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}
	
	private String buildWhereClause(String listId, PagingVo vo, List<Object> parms) {
		String whereSql = CRIT[parms.size()] + " a.ListId = ? ";
		parms.add(listId);
		if (!StringUtil.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " b.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		//whereSql += CRIT[parms.size()] + " a.Subscribed = ? ";
		//parms.add(Constants.YES_CODE);
		// search by address
		if (vo.getSearchString() != null && vo.getSearchString().trim().length() > 0) {
			String addr = vo.getSearchString().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " b.OrigEmailAddr LIKE '%" + addr + "%' ";
			}
			else {
				String regex = StringUtil.replaceAll(addr, " ", ".+");
				whereSql += CRIT[parms.size()] + " b.OrigEmailAddr REGEXP '" + regex + "' ";
			}
		}
		return whereSql;
	}
	
	@Override
	public List<SubscriptionVo> getSubscribers(String listId) {
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
			" from Subscription a" +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" LEFT OUTER JOIN Customers c on a.EmailAddrId=c.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? ";
		Object[] parms = new Object[] {listId, StatusIdCode.ACTIVE, Constants.YES_CODE};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}
	
	@Override
	public List<SubscriptionVo> getSubscribersWithCustomerRecord(String listId) {
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
			" from Subscription a" +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" JOIN Customers c on a.EmailAddrId=c.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? ";
		Object[] parms = new Object[] {listId, StatusIdCode.ACTIVE, Constants.YES_CODE};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}
	
	@Override
	public List<SubscriptionVo> getSubscribersWithoutCustomerRecord(String listId) {
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
			" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
			" where a.ListId=? " +
				" and b.StatusId=? " +
				" and a.Subscribed=? " +
				" and not exists (select 1 from Customers where EmailAddrId=b.EmailAddrId) ";
		Object[] parms = new Object[] {listId, StatusIdCode.ACTIVE, Constants.YES_CODE};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}
	
	@Override
	public SubscriptionVo getByPrimaryKey(long addrId, String listId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" where a.EmailAddrId=? and a.ListId=?";
		Object[] parms = new Object[] {addrId, listId};
		try {
			SubscriptionVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public SubscriptionVo getRandomRecord() {
		String sql = 
				"select * " +
				"from " +
					"Subscription where EmailAddrId >= (RAND() * (select max(EmailAddrId) from Subscription)) order by EmailAddrId limit 1 ";
			
		List<SubscriptionVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		if (list.size() > 0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}
	
	@Override
	public SubscriptionVo getByAddrAndListId(String addr, String listId) {
		String sql = "SELECT a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.ListId=? and b.EmailAddr=?";
		Object[] parms = new Object[] {listId, addr};
		try {
			SubscriptionVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}

	}
	
	@Override
	public List<SubscriptionVo> getByAddrId(long addrId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.EmailAddrId=?";
		Object[] parms = new Object[] {addrId};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}
	
	@Override
	public List<SubscriptionVo> getByListId(String listId) {
		String sql = "select a.*, " +
				" b.EmailAddr, " +
				" b.AcceptHtml " +
				" from Subscription a " +
				" JOIN EmailAddr b ON a.EmailAddrId=b.EmailAddrId " +
				" and a.ListId=?";
		Object[] parms = new Object[] {listId};
		List<SubscriptionVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriptionVo>(SubscriptionVo.class));
		return list;
	}

	@Override
	public int update(SubscriptionVo subscriptionVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(subscriptionVo);
		String sql = MetaDataUtil.buildUpdateStatement("Subscription", subscriptionVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int updateSentCount(long emailAddrId, String listId) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update Subscription set " +
			"SentCount=SentCount+1, " +
			"LastSentTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int updateOpenCount(long emailAddrId, String listId) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update Subscription set " +
			"OpenCount=OpenCount+1, " +
			"LastOpenTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int updateClickCount(long emailAddrId, String listId) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(new Timestamp(new java.util.Date().getTime()));
		keys.add(emailAddrId);
		keys.add(listId);

		String sql = "update Subscription set " +
			"Clickcount=ClickCount+1, " +
			"LastClickTime=? " +
			" where EmailAddrId=? and ListId=?";
		
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long addrid, String listId) {
		String sql = "delete from Subscription where EmailAddrId=? and ListId=?";
		Object[] parms = new Object[] {addrid, listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByAddrId(long addrId) {
		String sql = "delete from Subscription where EmailAddrId=?";
		Object[] parms = new Object[] {addrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByListId(String listId) {
		String sql = "delete from Subscription where ListId=?";
		Object[] parms = new Object[] {listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(SubscriptionVo subscriptionVo) {
		if (subscriptionVo.getCreateTime()==null) {
			subscriptionVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(subscriptionVo);
		String sql = MetaDataUtil.buildInsertStatement("Subscription", subscriptionVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
	private EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}
}

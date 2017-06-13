package ltj.message.dao.emailaddr;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.emailaddr.MailingListVo;

@Component("mailingListDao")
public class MailingListJdbcDao extends AbstractDao implements MailingListDao {
	static final Logger logger = Logger.getLogger(MailingListJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private String getSelectClause() {
		String select = "select " +
				" a.row_id, " +
				" a.list_id, " +
				" a.display_name, " +
				" a.acct_user_name, " +
				" c.domain_name, " + 
				" a.description, " +
				" a.client_id, " +
				" a.status_id, " +
				" a.is_built_in, " +
				" a.is_send_text, " +
				" a.create_time, " +
				" a.list_master_email_addr, " +
				" '' as Subscribed, " +
				" a.list_id as OrigListId, " +
				" sum(b.sent_count) as sent_count, sum(b.open_count) as open_count," +
				" sum(b.click_count) as click_count " +
				"from mailing_list a " +
				" LEFT OUTER JOIN email_subscrpt b on a.list_id = b.list_id " +
				" JOIN client_tbl c on a.client_id = c.client_id ";
		return select;
	}

	private String getGroupByClause() {
		String groupBy = "group by " +
				" a.row_id, " +
				" a.list_id, " +
				" a.display_name, " +
				" a.acct_user_name, " +
				" c.domain_name, " + 
				" a.description, " +
				" a.client_id, " +
				" a.status_id, " +
				" a.is_built_in, " +
				" a.is_send_text, " +
				" a.create_time, " +
				" a.list_master_email_addr ";
		return groupBy;
	}

	@Override
	public MailingListVo getByListId(String listId) {
		String sql = getSelectClause() +
				" where a.list_id = ? " +
				getGroupByClause();
		Object[] parms = new Object[] {listId};
		try {
			MailingListVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public MailingListVo getByRowId(int rowId) {
		String sql = getSelectClause() +
				" where a.row_id = ? " +
				getGroupByClause();
		Object[] parms = new Object[] {rowId};
		try {
			MailingListVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MailingListVo> getByAddress(String emailAddr) {
		emailAddr = emailAddr == null ? "" : emailAddr; // just for safety
		String acctUserName = emailAddr;
		String domainName = null;
		int atSignPos = emailAddr.indexOf("@");
		if (atSignPos >= 0) {
			acctUserName = emailAddr.substring(0, atSignPos);
			domainName = emailAddr.substring(atSignPos + 1);
		}
		String sql = getSelectClause() +
			" where a.acct_user_name = ? ";
		if (domainName != null && domainName.trim().length() > 0) {
			sql += " and c.domain_name = '" + domainName + "' ";
		}
		sql += getGroupByClause();
		Object[] parms = new Object[] {acctUserName};
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		return list;
	}
	
	@Override
	public List<MailingListVo> getAll(boolean onlyActive) {
		List<Object> parms = new ArrayList<>();
		String sql = getSelectClause();
		if (onlyActive) {
			sql += " where a.status_id = ? ";
			parms.add(StatusId.ACTIVE.value());
		}
		sql += getGroupByClause();
		sql += " order by a.row_id ";
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		return list;
	}

	@Override
	public List<MailingListVo> getAllForTrial(boolean onlyActive) {
		List<Object> parms = new ArrayList<>();
		String sql = getSelectClause();
		if (onlyActive) {
			sql += " where a.status_id = ? ";
			parms.add(StatusId.ACTIVE.value());
		}
		sql += getGroupByClause();
		sql += " order by a.row_id limit 5";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(5);
		getJdbcTemplate().setMaxRows(5);
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}

	@Override
	public List<MailingListVo> getSubscribedLists(long emailAddrId) {
		String sql = "SELECT " +
			" m.*, " +
			" c.domain_name, " +
			" s.subscribed, " +
			" s.sent_count, " +
			" s.open_count, " +
			" s.click_count " +
			" FROM mailing_list m, email_subscrpt s, client_tbl c " +
			" where m.list_id=s.list_id " +
			" and m.client_id=c.client_id " +
			" and s.email_addr_id=? ";
		Object[] parms = new Object[] {emailAddrId};
		List<MailingListVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MailingListVo>(MailingListVo.class));
		return list;
	}

	@Override
	public int update(MailingListVo mailingListVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailingListVo);
		String sql = MetaDataUtil.buildUpdateStatement("mailing_list", mailingListVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailingListVo.setOrigListId(mailingListVo.getListId());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByListId(String listId) {
		String sql = "delete from mailing_list where list_id=?";
		Object[] parms = new Object[] {listId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByAddress(String emailAddr) {
		String sql = "delete from mailing_list where email_addr=?";
		Object[] parms = new Object[] {emailAddr};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(MailingListVo mailingListVo) {
		mailingListVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailingListVo);
		String sql = MetaDataUtil.buildInsertStatement("mailing_list", mailingListVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailingListVo.setRowId(retrieveRowId());
		mailingListVo.setOrigListId(mailingListVo.getListId());
		return rowsInserted;
	}
}

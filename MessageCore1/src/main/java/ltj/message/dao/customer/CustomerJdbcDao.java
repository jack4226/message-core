package ltj.message.dao.customer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import ltj.jbatch.common.PasswordUtil;
import ltj.jbatch.common.PasswordUtil.PasswordTuple;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.CustomerVo;
import ltj.message.vo.PagingCustVo;
import ltj.message.vo.PagingVo;
import ltj.message.vo.emailaddr.EmailAddressVo;

@Repository
@Component("customerDao")
public class CustomerJdbcDao extends AbstractDao implements CustomerDao {

	@Override
	public CustomerVo getByCustId(String custId) {
		String sql = 
			"select *, cust_id as OrigCustId, updt_time as OrigUpdtTime " +
				"from customer_tbl where cust_id=? ";
		
		Object[] parms = new Object[] {custId};
		try {
			CustomerVo vo =  getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<CustomerVo> getByClientId(String clientId) {
		String sql = 
			"select *, cust_id as OrigCustId, updt_time as OrigUpdtTime " +
				"from customer_tbl where client_id=? ";
		Object[] parms = new Object[] {clientId};
		List<CustomerVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		return list;
	}
	
	@Override
	public CustomerVo getByEmailAddrId(long emailAddrId) {
		String sql = 
			"select *, cust_id as OrigCustId, updt_time as OrigUpdtTime " +
			" from customer_tbl where email_addr_id=? ";
		Object[] parms = new Object[] {Long.valueOf(emailAddrId)};
		List<CustomerVo> list =getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		if (list == null || list.isEmpty()) {
			return null;
		}
		else {
			return list.get(0);
		}
	}
	
	@Override
	public CustomerVo getByEmailAddress(String emailAddr) {
		String sql = 
			"select a.*, a.cust_id as OrigCustId, a.updt_time as OrigUpdtTime " +
			" from customer_tbl a, email_address b " +
			" where a.email_addr_id=b.email_addr_id " +
			" and a.email_addr=? ";
		Object[] parms = new Object[] {EmailAddrUtil.removeDisplayName(emailAddr)};
		List<CustomerVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		if (list == null || list.isEmpty()) {
			return null;
		}
		else {
			return list.get(0);
		}
	}
	
	@Override
	public List<CustomerVo> getFirst100() {
		String sql = 
			"select *, cust_id as OrigCustId, updt_time as OrigUpdtTime " +
				"from customer_tbl limit 100";
		
		List<CustomerVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		return list;
	}
	
	@Override
	public int getCustomerCount(PagingCustVo vo) {
		List<Object> parms = new ArrayList<>();
		String whereSql = buildWhereClause(vo, parms);
		String sql = 
			"select count(*) from customer_tbl a " 
			+ " LEFT OUTER JOIN email_address b on a.email_addr_id=b.email_addr_id "
			+ whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	@Override
	public List<CustomerVo> getCustomersWithPaging(PagingCustVo vo) {
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
			if (vo.getSearchObjLast() != null) {
				whereSql += CRIT[parms.size()] + " a.cust_id > ? ";
				parms.add(vo.getSearchObjLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getSearchObjFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.cust_id < ? ";
				parms.add(vo.getSearchObjFirst());
				fetchOrder = "desc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			int rows = getCustomerCount(vo);
			pageSize = rows % vo.getPageSize();
			if (pageSize == 0) {
				pageSize = Math.min(rows, vo.getPageSize());
			}
			fetchOrder = "desc";
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getSearchObjFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.cust_id >= ? ";
				parms.add(vo.getSearchObjFirst());
			}
		}
		String sql = 
			"select a.*, a.cust_id as OrigCustId, a.updt_time as OrigUpdtTime, " +
			"b.status_id as EmailStatusId, b.bounce_count, b.accept_html " +
			" from customer_tbl a " +
				" LEFT OUTER JOIN email_address b on a.email_addr_id=b.email_addr_id " +
			whereSql +
			" order by a.cust_id " + fetchOrder +
			" limit " + pageSize;
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<CustomerVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if ("desc".equals(fetchOrder)) {
			// reverse the list
			Collections.reverse(list);
		}
		if (!list.isEmpty()) {
			vo.setSearchObjFirst(list.get(0).getCustId());
			vo.setSearchObjLast(list.get(list.size() - 1).getCustId());
		}
		return list;
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };
	
	private String buildWhereClause(PagingCustVo vo, List<Object> parms) {
		String whereSql = "";
		if (!StringUtil.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " a.status_id = ? ";
			parms.add(vo.getStatusId());
		}
		if (!StringUtil.isEmpty(vo.getClientId())) {
			whereSql += CRIT[parms.size()] + " lower(a.client_id) = ? ";
			parms.add(vo.getClientId().toLowerCase());
		}
		if (!StringUtil.isEmpty(vo.getSsnNumber())) {
			whereSql += CRIT[parms.size()] + " a.ssn_number = ? ";
			parms.add(vo.getSsnNumber());
		}
		if (!StringUtil.isEmpty(vo.getLastName())) {
			whereSql += CRIT[parms.size()] + " lower(a.last_name) = ? ";
			parms.add(vo.getLastName().toLowerCase());
		}
		if (!StringUtil.isEmpty(vo.getFirstName())) {
			whereSql += CRIT[parms.size()] + " lower(a.first_name) = ? ";
			parms.add(vo.getFirstName().toLowerCase());
		}
		if (!StringUtil.isEmpty(vo.getDayPhone())) {
			whereSql += CRIT[parms.size()] + " a.day_phone = ? ";
			parms.add(vo.getDayPhone());
		}
		// search by email address
		if (!StringUtil.isEmpty(vo.getEmailAddr())) {
			String addr = vo.getEmailAddr().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " a.email_addr LIKE ? ";
				parms.add("%" + addr + "%");
			}
			else {
				//String regex = StringUtil.replaceAll(addr, " ", ".+");
				String regex = (addr + "").replaceAll("[ ]+", "|"); // any word
				whereSql += CRIT[parms.size()] + " a.email_addr REGEXP ? ";
				parms.add(regex);
			}
		}
		return whereSql;
	}
	
	@Override
	public int update(CustomerVo customerVo) {
		customerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		syncupEmailFields(customerVo);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(customerVo);
		String sql = MetaDataUtil.buildUpdateStatement("customer_tbl", customerVo);

		if (customerVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		customerVo.setOrigUpdtTime(customerVo.getUpdtTime());
		customerVo.setOrigCustId(customerVo.getCustId());
		return rowsUpadted;
	}
	
	@Override
	public int updatePassword(String custId, String newPassword) {
		Timestamp tms = new Timestamp(System.currentTimeMillis());
		PasswordTuple pswd = PasswordUtil.getEncryptedPassword(newPassword);
		String sql = "update customer_tbl set user_password=?, password_salt=?, password_change_time=? "
				+ "where cust_id=? ";
		int rows = getJdbcTemplate().update(sql, new Object[] {pswd.getPassword(), pswd.getSalt(), tms, custId});
		return rows;
	}
	
	@Override
	public int delete(String custId) {
		String sql = 
			"delete from customer_tbl where cust_id=? ";
		
		int rowsDeleted = getJdbcTemplate().update(sql, new Object[] {custId});
		return rowsDeleted;
	}

	@Override
	public int deleteByEmailAddr(String emailAddr) {
		EmailAddressVo addrVo = getEmailAddressDao().getByAddress(emailAddr);
		if (addrVo == null) {
			return 0;
		}
		String sql = 
			"delete from customer_tbl where email_addr_id=? ";
		
		int rowsDeleted = getJdbcTemplate().update(sql, new Object[] {addrVo.getEmailAddrId()});
		return rowsDeleted;
	}

	@Override
	public int insert(CustomerVo customerVo) {
		customerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		syncupEmailFields(customerVo);
		if (StringUtils.isNotBlank(customerVo.getUserPassword())) {
			if (StringUtils.isBlank(customerVo.getPasswordSalt())) {
				PasswordTuple pswd = PasswordUtil.getEncryptedPassword(customerVo.getUserPassword());
				customerVo.setUserPassword(pswd.getPassword());
				customerVo.setPasswordSalt(pswd.getSalt());
			}
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(customerVo);
		String sql = MetaDataUtil.buildInsertStatement("customer_tbl", customerVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		customerVo.setRowId(retrieveRowId());
		customerVo.setOrigUpdtTime(customerVo.getUpdtTime());
		customerVo.setOrigCustId(customerVo.getCustId());
		return rowsInserted;
	}
	
	private void syncupEmailFields(CustomerVo vo) {
		if (!StringUtil.isEmpty(vo.getEmailAddr())) {
			EmailAddressVo addrVo = getEmailAddressDao().findByAddress(vo.getEmailAddr());
			vo.setEmailAddrId(addrVo.getEmailAddrId());
		}
		else {
			EmailAddressVo addrVo = getEmailAddressDao().getByAddrId(vo.getEmailAddrId());
			if (addrVo != null) {
				vo.setEmailAddr(addrVo.getEmailAddr());
			}
		}
	}
	
	@Autowired
	private EmailAddressDao emailAddressDao;
	private EmailAddressDao getEmailAddressDao() {
		return emailAddressDao;
	}
}

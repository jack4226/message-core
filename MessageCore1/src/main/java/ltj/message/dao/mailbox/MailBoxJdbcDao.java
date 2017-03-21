package ltj.message.dao.mailbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.dao.client.ClientDao;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.vo.ClientVo;
import ltj.message.vo.MailBoxVo;

@Component("mailBoxDao")
public class MailBoxJdbcDao extends AbstractDao implements MailBoxDao {
	
	@Autowired
	private ClientDao clientDao;
	private ClientDao getClientDao() {
		return clientDao;
	}
	
	private String getClientDomains() {
		// retrieve matching domains from Client table
		List<ClientVo> vos = getClientDao().getAll();
		String toDomains = "";
		for (int i = 0; i < vos.size(); i++) {
			if (i > 0) {
				toDomains += ",";
			}
			toDomains += vos.get(i).getDomainName();
		}
		return toDomains;
	}
	
	@Override
	public MailBoxVo getByPrimaryKey(String userId, String hostName) {
		String sql = "select *, '" + getClientDomains() + "' as ToAddrDomain, " +
				"CONCAT(HostName, '.', UserId) as ServerName, UpdtTime as OrigUpdtTime " +
				"from mail_box where UserId=? and HostName=?";
		Object[] parms = new Object[] {userId, hostName};
		try {
			MailBoxVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MailBoxVo>(MailBoxVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MailBoxVo> getAll(boolean onlyActive) {
		List<String> keys = new ArrayList<>();
		String sql = "select *, '" + getClientDomains() + "' as ToAddrDomain, " +
				"CONCAT(HostName, '.', UserId) as ServerName, UpdtTime as OrigUpdtTime " +
				"from mail_box ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusId.ACTIVE.value());
		}
		sql += " order by HostName, UserId ";
		List<MailBoxVo> list = getJdbcTemplate().query(sql, keys.toArray(),
				new BeanPropertyRowMapper<MailBoxVo>(MailBoxVo.class));
		return list;
	}
	
	@Override
	public List<MailBoxVo> getAllForTrial(boolean onlyActive) {
		List<String> keys = new ArrayList<>();
		String sql = "select *, '" + getClientDomains() + "' as ToAddrDomain, " +
				"CONCAT(HostName, '.', UserId) as ServerName, UpdtTime as OrigUpdtTime " +
				"from mail_box ";
		if (onlyActive) {
			sql += " where StatusId=? ";
			keys.add(StatusId.ACTIVE.value());
		}
		sql += " order by RowId limit 1";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(1);
		getJdbcTemplate().setMaxRows(1);
		List<MailBoxVo> list = getJdbcTemplate().query(sql, keys.toArray(),
				new BeanPropertyRowMapper<MailBoxVo>(MailBoxVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}
	
	@Override
	public int update(MailBoxVo mailBoxVo) {
		mailBoxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailBoxVo);
		String sql = MetaDataUtil.buildUpdateStatement("mail_box", mailBoxVo);
		if (mailBoxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailBoxVo.setOrigUpdtTime(mailBoxVo.getUpdtTime());
		// insert/update EmailAddr record
		getEmailAddressDao().findByAddress(mailBoxVo.getUserId() + "@" + mailBoxVo.getHostName());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(String userId, String hostName) {
		String sql = "delete from mail_box where UserId=? and HostName=?";
		Object[] parms = new Object[] {userId, hostName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(MailBoxVo mailBoxVo) {
		mailBoxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailBoxVo);
		String sql = MetaDataUtil.buildInsertStatement("mail_box", mailBoxVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailBoxVo.setRowId(retrieveRowId());
		mailBoxVo.setOrigUpdtTime(mailBoxVo.getUpdtTime());
		// insert mailbox address to EmailAddr table
		getEmailAddressDao().findByAddress(mailBoxVo.getUserId() + "@" + mailBoxVo.getHostName());
		return rowsInserted;
	}
	
	@Autowired
	private EmailAddressDao emailAddressDao = null;
	private EmailAddressDao getEmailAddressDao() {
		return emailAddressDao;
	}
}

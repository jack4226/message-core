package ltj.message.dao.emailaddr;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.emailaddr.EmailUnsubCmntVo;

@Component("emailUnsubCmntDao")
public class EmailUnsubCmntJdbcDao extends AbstractDao implements EmailUnsubCmntDao {
	static final Logger logger = Logger.getLogger(EmailUnsubCmntJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Override
	public EmailUnsubCmntVo getByPrimaryKey(int rowId){
		String sql = "select * from email_unsub_cmnt where RowId=?";
		Object[] parms = new Object[] {rowId};
		try {
			EmailUnsubCmntVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<EmailUnsubCmntVo>(EmailUnsubCmntVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<EmailUnsubCmntVo> getFirst100() {
		String sql = "select * from email_unsub_cmnt " +
		" order by RowId limit 100";
		List<EmailUnsubCmntVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<EmailUnsubCmntVo>(EmailUnsubCmntVo.class));
		return list;
	}
	
	@Override
	public List<EmailUnsubCmntVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from email_unsub_cmnt " +
			" where EmailAddrId=" + emailAddrId +
			" order by RowId";
		List<EmailUnsubCmntVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<EmailUnsubCmntVo>(EmailUnsubCmntVo.class));
		return list;
	}
	
	@Override
	public List<EmailUnsubCmntVo> getByListId(String listId) {
		String sql = "select * from email_unsub_cmnt " +
			" where ListId='" + listId + "' " +
			" order by RowId";
		List<EmailUnsubCmntVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<EmailUnsubCmntVo>(EmailUnsubCmntVo.class));
		return list;
	}
	
	@Override
	public int update(EmailUnsubCmntVo emailUnsubCmntVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailUnsubCmntVo);
		String sql = MetaDataUtil.buildUpdateStatement("email_unsub_cmnt", emailUnsubCmntVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(int rowId) {
		String sql = "delete from email_unsub_cmnt where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByEmailAddrId(long emailAddrId) {
		String sql = "delete from email_unsub_cmnt where EmailAddrId=?";
		Object[] parms = new Object[] {emailAddrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(EmailUnsubCmntVo emailUnsubCmntVo) {
		emailUnsubCmntVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(emailUnsubCmntVo);
		String sql = MetaDataUtil.buildInsertStatement("email_unsub_cmnt", emailUnsubCmntVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		emailUnsubCmntVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}

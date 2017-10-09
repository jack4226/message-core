package ltj.message.dao.servers;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.MailSenderVo;

@Component("mailSenderPropsDao")
public class MailSenderPropsDao extends AbstractDao {
	
	public MailSenderVo getByPrimaryKey(int rowId) {
		String sql = 
			"select * from mail_sender_props where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			MailSenderVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MailSenderVo>(MailSenderVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MailSenderVo> getAll() {
		
		String sql = "select * from mail_sender_props ";
		List<MailSenderVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MailSenderVo>(MailSenderVo.class));
		return list;
	}
	
	public int update(MailSenderVo mailSenderVo) {
		mailSenderVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailSenderVo);
		String sql = MetaDataUtil.buildUpdateStatement("mail_sender_props", mailSenderVo);
		if (mailSenderVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(int rowId) {
		String sql = 
			"delete from mail_sender_props where row_id=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MailSenderVo mailSenderVo) {
		mailSenderVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mailSenderVo);
		String sql = MetaDataUtil.buildInsertStatement("mail_sender_props", mailSenderVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mailSenderVo.setRowId(retrieveRowId());
		mailSenderVo.setOrigUpdtTime(mailSenderVo.getUpdtTime());
		return rowsInserted;
	}
}

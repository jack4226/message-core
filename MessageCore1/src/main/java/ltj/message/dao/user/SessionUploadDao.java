package ltj.message.dao.user;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.SessionUploadVo;

@Component("sessionUploadDao")
public class SessionUploadDao extends AbstractDao {
	
	public SessionUploadVo getByPrimaryKey(String sessionId, int sessionSeq) {
		String sql = "select * from session_upload where session_id=? and session_seq=?";
		Object[] parms = new Object[] {sessionId, sessionSeq};
		try {
			SessionUploadVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<SessionUploadVo>(SessionUploadVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<SessionUploadVo> getBySessionId(String sessionId) {
		String sql = "select * from session_upload where session_id=?";
		Object[] parms = new Object[] {sessionId};
		List<SessionUploadVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SessionUploadVo>(SessionUploadVo.class));
		return list;
	}
	
	/**
	 * SessionValue (blob) is not returned from this method. But
	 * SessionUploadVo.fileSize is populated with file size.
	 */
	public List<SessionUploadVo> getBySessionId4Web(String sessionId) {
		List<SessionUploadVo> list = getBySessionId(sessionId);
		for (int i = 0; i < list.size(); i++) {
			SessionUploadVo vo = list.get(i);
			if (vo.getSessionValue() != null) {
				vo.setFileSize(vo.getSessionValue().length);
				vo.setSessionValue(null);
			}
			else {
				vo.setFileSize(0);
			}
		}
		return list;
	}
	
	public List<SessionUploadVo> getByUserId(String userId) {
		String sql = "select * from session_upload where user_id=?";
		Object[] parms = new Object[] {userId};
		List<SessionUploadVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SessionUploadVo>(SessionUploadVo.class));
		return list;
	}
	
	public int update(SessionUploadVo sessVo) {
		if (sessVo.getCreateTime() == null) {
			sessVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(sessVo);
		String sql = MetaDataUtil.buildUpdateStatement("session_upload", sessVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(String sessionId, int sessionSeq) {
		String sql = "delete from session_upload where session_id=? and session_seq=?";
		Object[] parms = new Object[] {sessionId, sessionSeq};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}

	public int deleteBySessionId(String sessionId) {
		String sql = "delete from session_upload where session_id=?";
		Object[] parms = new Object[] {sessionId,};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByUserId(String userId) {
		String sql = "delete from session_upload where user_id=?";
		Object[] parms = new Object[] {userId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteExpired(int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -minutes); // roll back time
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		String sql = "delete from session_upload where create_time<?";
		Object[] parms = new Object[] {now};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;	
	}
	
	public int deleteAll() {
		String sql = "delete from session_upload";
		int rowsDeleted = getJdbcTemplate().update(sql);
		return rowsDeleted;	
	}

	public int insert(SessionUploadVo sessVo) {
		sessVo.setCreateTime(new Timestamp(System.currentTimeMillis()));
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(sessVo);
		String sql = MetaDataUtil.buildInsertStatement("session_upload", sessVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
	final static Object jvmLocker = new Object();
	
	public int insertLast(SessionUploadVo sessVo) {
		synchronized (jvmLocker) {
			String lastSeq = "select max(session_seq) from session_upload where session_id = '" + sessVo.getSessionId() + "'";
			int sessSeq = getJdbcTemplate().queryForObject(lastSeq, Integer.class) + 1;
			sessVo.setSessionSeq(sessSeq);
			return insert(sessVo);
		}
	}
}

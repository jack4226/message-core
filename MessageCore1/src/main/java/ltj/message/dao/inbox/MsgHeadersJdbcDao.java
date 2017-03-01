package ltj.message.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.vo.inbox.MsgHeadersVo;

@Component("msgHeadersDao")
public class MsgHeadersJdbcDao extends AbstractDao implements MsgHeadersDao {
	
	@Override
	public MsgHeadersVo getByPrimaryKey(long msgId, int headerSeq) {
		String sql = 
			"select * " +
			"from " +
				"MsgHeaders where msgid=? and headerSeq=? ";
		
		Object[] parms = new Object[] {msgId, headerSeq};
		try {
			MsgHeadersVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgHeadersVo>(MsgHeadersVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgHeadersVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgHeaders where msgId=? " +
			" order by headerSeq";
		Object[] parms = new Object[] {msgId};
		List<MsgHeadersVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgHeadersVo>(MsgHeadersVo.class));
		return list;
	}
	
	@Override
	public List<MsgHeadersVo> getRandomRecord() {
		String sql = 
				"select * " +
				" from " +
					" MsgHeaders where msgId >= (RAND() * (select max(msgId) from MsgHeaders)) " +
				" order by msgId limit 1 ";
		List<MsgHeadersVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgHeadersVo>(MsgHeadersVo.class));
		if (list.size() > 0) {
			return getByMsgId(list.get(0).getMsgId());
		}
		return list;
	}
	
	@Override
	public int update(MsgHeadersVo msgHeadersVo) {
		
		List<Object> fields = new ArrayList<>();
		fields.add(StringUtils.left(msgHeadersVo.getHeaderName(), 100));
		fields.add(msgHeadersVo.getHeaderValue());
		fields.add(msgHeadersVo.getMsgId());
		fields.add(msgHeadersVo.getHeaderSeq());
		
		String sql =
			"update MsgHeaders set " +
				"HeaderName=?, " +
				"HeaderValue=? " +
			" where " +
				" msgid=? and headerSeq=?  ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, int headerSeq) {
		String sql = 
			"delete from MsgHeaders where msgid=? and headerSeq=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		fields.add(headerSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from MsgHeaders where msgid=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgHeadersVo msgHeadersVo) {
		String sql = 
			"INSERT INTO MsgHeaders (" +
			"MsgId, " +
			"HeaderSeq, " +
			"HeaderName, " +
			"HeaderValue " +
			") VALUES (" +
				" ?, ?, ?, ? " +
				")";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgHeadersVo.getMsgId());
		fields.add(msgHeadersVo.getHeaderSeq());
		fields.add(StringUtils.left(msgHeadersVo.getHeaderName(), 100));
		fields.add(msgHeadersVo.getHeaderValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
}

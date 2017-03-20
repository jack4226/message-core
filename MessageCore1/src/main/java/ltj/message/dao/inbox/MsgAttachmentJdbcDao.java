package ltj.message.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.inbox.MsgAttachmentVo;

@Component("msgAttachmentDao")
public class MsgAttachmentJdbcDao extends AbstractDao implements MsgAttachmentDao {

	@Override
	public MsgAttachmentVo getByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"select * " +
			"from " +
				"msg_attachment where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		Object[] parms = new Object[] {msgId, attchmntDepth, attchmntSeq};
		try {
			MsgAttachmentVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgAttachmentVo>(MsgAttachmentVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgAttachmentVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" msg_attachment where msgId=? " +
			" order by attchmntDepth, attchmntSeq";
		Object[] parms = new Object[] {msgId};
		List<MsgAttachmentVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgAttachmentVo>(MsgAttachmentVo.class));
		return list;
	}
	
	@Override
	public List<MsgAttachmentVo> getRandomRecord() {
		String sql = 
				"select * " +
				" from " +
					" msg_attachment where msgId >= (RAND() * (select max(msgId) from msg_attachment)) " +
				" order by msgId limit 1 ";

		List<MsgAttachmentVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgAttachmentVo>(MsgAttachmentVo.class));
		if (list.size() > 0) {
			return getByMsgId(list.get(0).getMsgId());
		}
		return list;
	}
	
	@Override
	public int update(MsgAttachmentVo msgAttachmentVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAttachmentVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_attachment", msgAttachmentVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, int attchmntDepth, int attchmntSeq) {
		String sql = 
			"delete from msg_attachment where msgid=? and attchmntDepth=? and attchmntSeq=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		fields.add(attchmntDepth);
		fields.add(attchmntSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from msg_attachment where msgid=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgAttachmentVo msgAttachmentVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAttachmentVo);
		String sql = MetaDataUtil.buildInsertStatement("msg_attachment", msgAttachmentVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

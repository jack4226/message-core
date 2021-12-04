package ltj.message.dao.inbox;

import java.sql.Timestamp;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.inbox.MsgUnsubCmntVo;

@Component("msgUnsubCmntDao")
public class MsgUnsubCmntJdbcDao extends AbstractDao implements MsgUnsubCmntDao {
	static final Logger logger = LogManager.getLogger(MsgUnsubCmntJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Override
	public MsgUnsubCmntVo getByPrimaryKey(int rowId){
		String sql = "select * from msg_unsub_cmnt where row_id=?";
		Object[] parms = new Object[] {rowId};
		try {
			MsgUnsubCmntVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgUnsubCmntVo>(MsgUnsubCmntVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgUnsubCmntVo> getFirst100() {
		String sql = "select * from msg_unsub_cmnt " +
		" order by row_id limit 100 ";
		List<MsgUnsubCmntVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgUnsubCmntVo>(MsgUnsubCmntVo.class));
		return list;
	}
	
	@Override
	public List<MsgUnsubCmntVo> getByMsgId(long msgId) {
		String sql = "select * from msg_unsub_cmnt " +
			" where msg_id=" + msgId +
			" order by row_id";
		List<MsgUnsubCmntVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgUnsubCmntVo>(MsgUnsubCmntVo.class));
		return list;
	}
	
	@Override
	public List<MsgUnsubCmntVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from msg_unsub_cmnt " +
			" where email_addr_id=" + emailAddrId +
			" order by row_id";
		List<MsgUnsubCmntVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgUnsubCmntVo>(MsgUnsubCmntVo.class));
		return list;
	}
	
	@Override
	public List<MsgUnsubCmntVo> getByListId(String listId) {
		String sql = "select * from msg_unsub_cmnt " +
			" where list_id='" + listId + "' " +
			" order by row_id";
		List<MsgUnsubCmntVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgUnsubCmntVo>(MsgUnsubCmntVo.class));
		return list;
	}
	
	@Override
	public int update(MsgUnsubCmntVo msgUnsubCmntVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgUnsubCmntVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_unsub_cmnt", msgUnsubCmntVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(int rowId) {
		String sql = "delete from msg_unsub_cmnt where row_id=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = "delete from msg_unsub_cmnt where msg_id=?";
		Object[] parms = new Object[] {msgId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByEmailAddrId(long emailAddrId) {
		String sql = "delete from msg_unsub_cmnt where email_addr_id=?";
		Object[] parms = new Object[] {emailAddrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgUnsubCmntVo msgUnsubCmntVo) {
		msgUnsubCmntVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgUnsubCmntVo);
		String sql = MetaDataUtil.buildInsertStatement("msg_unsub_cmnt", msgUnsubCmntVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgUnsubCmntVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}

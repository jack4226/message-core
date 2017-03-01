package ltj.message.dao.inbox;

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
import ltj.message.vo.inbox.MsgUnsubCommentsVo;

@Component("msgUnsubCommentsDao")
public class MsgUnsubCommentsJdbcDao extends AbstractDao implements MsgUnsubCommentsDao {
	static final Logger logger = Logger.getLogger(MsgUnsubCommentsJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Override
	public MsgUnsubCommentsVo getByPrimaryKey(int rowId){
		String sql = "select * from MsgUnsubComments where RowId=?";
		Object[] parms = new Object[] {rowId};
		try {
			MsgUnsubCommentsVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgUnsubCommentsVo>(MsgUnsubCommentsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgUnsubCommentsVo> getFirst100() {
		String sql = "select * from MsgUnsubComments " +
		" order by RowId limit 100 ";
		List<MsgUnsubCommentsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgUnsubCommentsVo>(MsgUnsubCommentsVo.class));
		return list;
	}
	
	@Override
	public List<MsgUnsubCommentsVo> getByMsgId(long msgId) {
		String sql = "select * from MsgUnsubComments " +
			" where MsgId=" + msgId +
			" order by RowId";
		List<MsgUnsubCommentsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgUnsubCommentsVo>(MsgUnsubCommentsVo.class));
		return list;
	}
	
	@Override
	public List<MsgUnsubCommentsVo> getByEmailAddrId(long emailAddrId) {
		String sql = "select * from MsgUnsubComments " +
			" where EmailAddrId=" + emailAddrId +
			" order by RowId";
		List<MsgUnsubCommentsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgUnsubCommentsVo>(MsgUnsubCommentsVo.class));
		return list;
	}
	
	@Override
	public List<MsgUnsubCommentsVo> getByListId(String listId) {
		String sql = "select * from MsgUnsubComments " +
			" where ListId='" + listId + "' " +
			" order by RowId";
		List<MsgUnsubCommentsVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgUnsubCommentsVo>(MsgUnsubCommentsVo.class));
		return list;
	}
	
	@Override
	public int update(MsgUnsubCommentsVo msgUnsubCommentsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgUnsubCommentsVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgUnsubComments", msgUnsubCommentsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(int rowId) {
		String sql = "delete from MsgUnsubComments where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = "delete from MsgUnsubComments where MsgId=?";
		Object[] parms = new Object[] {msgId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int deleteByEmailAddrId(long emailAddrId) {
		String sql = "delete from MsgUnsubComments where EmailAddrId=?";
		Object[] parms = new Object[] {emailAddrId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgUnsubCommentsVo msgUnsubCommentsVo) {
		msgUnsubCommentsVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgUnsubCommentsVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgUnsubComments", msgUnsubCommentsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgUnsubCommentsVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}

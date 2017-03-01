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
import ltj.message.vo.inbox.MsgAddrsVo;

@Component("msgAddrsDao")
public class MsgAddrsJdbcDao extends AbstractDao implements MsgAddrsDao {

	@Override
	public MsgAddrsVo getByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"select * " +
			"from " +
				"MsgAddrs where msgid=? and addrType=? and addrSeq=? ";
		
		Object[] parms = new Object[] {msgId, addrType, addrSeq};
		try {
			MsgAddrsVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgAddrsVo>(MsgAddrsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgAddrsVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgAddrs where msgId=? " +
			" order by addrType, addrSeq";
		Object[] parms = new Object[] {msgId};
		List<MsgAddrsVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgAddrsVo>(MsgAddrsVo.class));
		return list;
	}
	
	@Override
	public List<MsgAddrsVo> getByMsgIdAndType(long msgId, String addrType) {
		String sql = 
			"select * " +
			" from " +
				" MsgAddrs where msgId=? and addrType=? " +
			" order by addrSeq";
		Object[] parms = new Object[] {msgId, addrType};
		List<MsgAddrsVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgAddrsVo>(MsgAddrsVo.class));
		return list;
	}
	
	@Override
	public List<MsgAddrsVo> getRandomRecord() {
		String sql = 
				"select * " +
				" from " +
					" MsgAddrs where msgId >= (RAND() * (select max(msgId) from MsgAddrs)) " +
				" order by msgId limit 1 ";
			
		List<MsgAddrsVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<MsgAddrsVo>(MsgAddrsVo.class));
		if (list.size() > 0) {
			return getByMsgId(list.get(0).getMsgId());
		}
		return list;
	}
	
	@Override
	public int update(MsgAddrsVo msgAddrsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAddrsVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgAddrs", msgAddrsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"delete from MsgAddrs where msgid=? and addrType=? and addrSeq=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		fields.add(addrType);
		fields.add(addrSeq);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from MsgAddrs where msgid=? ";
		
		List<Long> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgAddrsVo msgAddrsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAddrsVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgAddrs", msgAddrsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

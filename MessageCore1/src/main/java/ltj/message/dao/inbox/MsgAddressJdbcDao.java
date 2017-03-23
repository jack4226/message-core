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
import ltj.message.util.EmailAddrUtil;
import ltj.message.vo.inbox.MsgAddressVo;

@Component("msgAddressDao")
public class MsgAddressJdbcDao extends AbstractDao implements MsgAddressDao {

	@Override
	public MsgAddressVo getByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"select * " +
			"from " +
				"msg_address where msg_id=? and addr_type=? and addr_seq=? ";
		
		Object[] parms = new Object[] {msgId, addrType, addrSeq};
		try {
			MsgAddressVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgAddressVo>(MsgAddressVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgAddressVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" msg_address where msg_id=? " +
			" order by addr_type, addr_seq";
		Object[] parms = new Object[] {msgId};
		List<MsgAddressVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgAddressVo>(MsgAddressVo.class));
		return list;
	}
	
	@Override
	public List<MsgAddressVo> getByMsgIdAndType(long msgId, String addrType) {
		String sql = 
			"select * " +
			" from " +
				" msg_address where msg_id=? and addr_type=? " +
			" order by addr_seq";
		Object[] parms = new Object[] {msgId, addrType};
		List<MsgAddressVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgAddressVo>(MsgAddressVo.class));
		return list;
	}
	
	@Override
	public List<MsgAddressVo> getRandomRecord() {
		String sql = 
				"select * " +
				" from msg_address " +
					" where msg_id >= (RAND() * (select max(msg_id) from msg_address)) " +
				" order by msg_id limit 1 ";
			
		List<MsgAddressVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<MsgAddressVo>(MsgAddressVo.class));
		if (list.size() > 0) {
			return getByMsgId(list.get(0).getMsgId());
		}
		return list;
	}
	
	@Override
	public int update(MsgAddressVo msgAddressVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAddressVo);
		msgAddressVo.setAddrValue(EmailAddrUtil.removeDisplayName(msgAddressVo.getAddrValue()));
		String sql = MetaDataUtil.buildUpdateStatement("msg_address", msgAddressVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, String addrType, int addrSeq) {
		String sql = 
			"delete from msg_address where msg_id=? and addr_type=? and addr_seq=? ";
		
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
			"delete from msg_address where msg_id=? ";
		
		List<Long> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgAddressVo msgAddressVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgAddressVo);
		msgAddressVo.setAddrValue(EmailAddrUtil.removeDisplayName(msgAddressVo.getAddrValue()));
		String sql = MetaDataUtil.buildInsertStatement("msg_address", msgAddressVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

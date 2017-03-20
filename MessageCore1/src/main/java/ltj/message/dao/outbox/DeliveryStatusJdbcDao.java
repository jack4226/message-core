package ltj.message.dao.outbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.vo.outbox.DeliveryStatusVo;

@Component("deliveryStatusDao")
public class DeliveryStatusJdbcDao extends AbstractDao implements DeliveryStatusDao {
	
	@Override
	public DeliveryStatusVo getByPrimaryKey(long msgId, long finalRecipientId) {
		String sql = 
			"select * " +
			"from " +
				"delivery_status where msgid=? and finalRecipientId=? ";
		
		Object[] parms = new Object[] {msgId, finalRecipientId};
		try {
			DeliveryStatusVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<DeliveryStatusVo>(DeliveryStatusVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<DeliveryStatusVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" delivery_status where msgId=? " +
			" order by finalRecipient";
		Object[] parms = new Object[] {msgId};
		List<DeliveryStatusVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<DeliveryStatusVo>(DeliveryStatusVo.class));
		return list;
	}
	
	@Override
	public List<DeliveryStatusVo> getRandomRecord() {
		String sql = 
				"select * " +
				" from " +
					" delivery_status where msgId >= (RAND() * (select max(msgId) from delivery_status)) " +
				" order by msgId limit 1";
		List<DeliveryStatusVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<DeliveryStatusVo>(DeliveryStatusVo.class));
		if (list.size() > 0) {
			return getByMsgId(list.get(0).getMsgId());
		}
		return list;
	}
	
	@Override
	public int update(DeliveryStatusVo deliveryStatusVo) {
		if (deliveryStatusVo.getAddTime()==null) {
			deliveryStatusVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deliveryStatusVo);
		String sql = MetaDataUtil.buildUpdateStatement("delivery_status", deliveryStatusVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId, long finalRecipientId) {
		String sql = 
			"delete from delivery_status where msgid=? and finalRecipientId=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		fields.add(finalRecipientId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from delivery_status where msgid=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	/**
	 * First delete the record to be inserted, then insert the record. This is a
	 * workaround to avoid DataIntegrityViolationException if the record to be
	 * inserted already exists in the database.
	 * <p>
	 * 
	 * Once the DataIntegrityViolationException is generated, spring will also
	 * set the global rollback-only flag to true, causing the entire transaction
	 * to fail.
	 */
	final static Object jvmLocker = new Object();
	@Override
	public int insertWithDelete(DeliveryStatusVo deliveryStatusVo) {
		synchronized (jvmLocker) {
			deleteByPrimaryKey(deliveryStatusVo.getMsgId(), deliveryStatusVo.getFinalRecipientId());
			return insert(deliveryStatusVo);
		}
	}
	
	@Override
	public int insert(DeliveryStatusVo deliveryStatusVo) {
		if (deliveryStatusVo.getAddTime()==null) {
			deliveryStatusVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deliveryStatusVo);
		String sql = MetaDataUtil.buildInsertStatement("delivery_status", deliveryStatusVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

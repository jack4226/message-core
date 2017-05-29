package ltj.message.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.vo.outbox.MsgStreamVo;

@Component("msgStreamDao")
public class MsgStreamJdbcDao extends AbstractDao implements MsgStreamDao {
	
	@Override
	public MsgStreamVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"msg_stream where msg_id=? ";
		
		Object[] parms = new Object[] {msgId};
		try {
			MsgStreamVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgStreamVo> getByFromAddrId(long fromAddrId) {
		String sql = 
			"select * " +
			"from " +
				"msg_stream where from_addr_id=? ";
		
		Object[] parms = new Object[] {fromAddrId};
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return list;
	}
	
	@Override
	public List<MsgStreamVo> getByToAddrId(long toAddrId) {
		String sql = 
			"select m.* " +
			"from msg_stream m " +
				" join msg_address s on s.msg_id=m.msg_id and s.addr_type='To' " +
				" join email_address e on e.email_addr=s.addr_value and e.email_addr_id=? ";
		
		Object[] parms = new Object[] {toAddrId};
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return list;
	}
	
	@Override
	public List<MsgStreamVo> getByFromAddress(String address) {
		String sql = 
			"select m.* " +
			"from " +
				"msg_stream m join email_address e on e.email_addr_id=m.from_addr_id " + 
			" where e.email_addr=? ";
		
		Object[] parms = new Object[] {address};
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return list;
	}
	
	@Override
	public List<MsgStreamVo> getByToAddress(String address) {
		String sql = 
			"select m.* " +
			"from " +
				"msg_stream m join msg_address s on s.msg_id=m.msg_id " +
				" join email_address e on e.email_addr=s.addr_value and s.addr_type='To' " +
			" where e.email_addr=? ";
		
		Object[] parms = new Object[] {address};
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return list;
	}

	@Override
	public MsgStreamVo getLastRecord() {
		String sql = 
			"select * " +
			"from " +
				"msg_stream where msg_id = (select max(msg_id) from msg_stream) ";
		
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	/* 
	 * Select a random row:
	 * 
	 Select a random row with MySQL:
		SELECT column FROM table
		ORDER BY RAND()
		LIMIT 1
		
	Select a random row with PostgreSQL:
		SELECT column FROM table
		ORDER BY RANDOM()
		LIMIT 1
		
	Select a random row with Microsoft SQL Server:
		SELECT TOP 1 column FROM table
		ORDER BY NEWID()
		
	Select a random row with IBM DB2
		SELECT column, RAND() as IDX 
		FROM table 
		ORDER BY IDX FETCH FIRST 1 ROWS ONLY
		
	Select a random record with Oracle:
		SELECT column FROM
		( SELECT column FROM table
		ORDER BY dbms_random.value )
		WHERE rownum = 1
	 */
	@Override
	public MsgStreamVo getRandomRecord() {
		String sql = 
			"select * " +
			"from " +
				"msg_stream order by RAND() limit 1 ";
		
		List<MsgStreamVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public int update(MsgStreamVo msgStreamVo) {
		
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		List<Object> fields = new ArrayList<>();
		fields.add(msgStreamVo.getFromAddrId());
		fields.add(msgStreamVo.getToAddrId());
		fields.add(msgStreamVo.getMsgSubject());
		fields.add(msgStreamVo.getAddTime());
		fields.add(msgStreamVo.getMsgStream());
		fields.add(msgStreamVo.getMsgId());
		
		String sql =
			"update msg_stream set " +
				"from_addr_id=?, " +
				"to_addr_id=?, " +
				"msg_subject=?, " +
				"add_time=?, " +
				"msg_stream=? " +
			" where " +
				" msg_id=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from msg_stream where msg_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgStreamVo msgStreamVo) {
		String sql = 
			"INSERT INTO msg_stream (" +
				"msg_id, " +
				"from_addr_id, " +
				"to_addr_id, " +
				"msg_subject, " +
				"add_time, " +
				"msg_stream " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? " +
				")";
		
		if (msgStreamVo.getAddTime()==null) {
			msgStreamVo.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		List<Object> fields = new ArrayList<>();
		fields.add(msgStreamVo.getMsgId());
		fields.add(msgStreamVo.getFromAddrId());
		fields.add(msgStreamVo.getToAddrId());
		fields.add(msgStreamVo.getMsgSubject());
		fields.add(msgStreamVo.getAddTime());
		fields.add(msgStreamVo.getMsgStream());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
}

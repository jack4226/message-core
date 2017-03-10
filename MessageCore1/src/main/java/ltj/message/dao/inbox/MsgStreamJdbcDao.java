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
				"MsgStream where msgid=? ";
		
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
				"MsgStream where fromAddrId=? ";
		
		Object[] parms = new Object[] {fromAddrId};
		List<MsgStreamVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgStreamVo>(MsgStreamVo.class));
		return list;
	}
	
	@Override
	public List<MsgStreamVo> getByToAddrId(long toAddrId) {
		String sql = 
			"select m.* " +
			"from MsgStream m " +
				" join MsgAddrs s on s.msgId=m.msgId and s.addrType='To' " +
				" join EmailAddr e on e.emailAddr=s.addrValue and e.emailAddrId=? ";
		
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
				"MsgStream m join EmailAddr e on e.emailAddrId=m.fromAddrId " + 
			" where e.emailAddr=? ";
		
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
				"MsgStream m join MsgAddrs s on s.msgId=m.msgId " +
				" join EmailAddr e on e.emailAddr=s.addrValue and s.addrType='To' " +
			" where e.emailAddr=? ";
		
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
				"MsgStream where msgid = (select max(MsgId) from MsgStream) ";
		
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
				"MsgStream order by RAND() limit 1 ";
		
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
			"update MsgStream set " +
				"FromAddrId=?, " +
				"ToAddrId=?, " +
				"MsgSubject=?, " +
				"AddTime=?, " +
				"MsgStream=? " +
			" where " +
				" msgid=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from MsgStream where msgid=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgStreamVo msgStreamVo) {
		String sql = 
			"INSERT INTO MsgStream (" +
				"MsgId, " +
				"FromAddrId, " +
				"ToAddrId, " +
				"MsgSubject, " +
				"AddTime, " +
				"MsgStream " +
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

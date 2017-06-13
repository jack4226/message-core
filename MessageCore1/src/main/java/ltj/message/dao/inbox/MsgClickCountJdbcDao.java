package ltj.message.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.PagingCountVo;
import ltj.message.vo.PagingVo;
import ltj.message.vo.PagingVo.PagingContext;
import ltj.message.vo.inbox.MsgClickCountVo;

@Component("msgClickCountDao")
public class MsgClickCountJdbcDao extends AbstractDao implements MsgClickCountDao {
	
	@Override
	public MsgClickCountVo getRandomRecord() {
		String sql = 
			"select * " +
			"from msg_click_count " +
				" where msg_id >= (RAND() * (select max(msg_id) from msg_click_count)) " +
			" order by msg_id limit 1 ";
		
		List<MsgClickCountVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgClickCountVo>(MsgClickCountVo.class));
		if (list.size() > 0) {
			return list.get(0);
		}
		else {
			return null;
		}
	}
	
	@Override
	public int getMsgCountForWeb() {
		String sql = 
			"select count(*) " +
			"from " +
				"msg_click_count where sent_count > 0 and start_time is not null ";
		int count = getJdbcTemplate().queryForObject(sql, Integer.class);
		return count;
	}
	
	@Override
	public MsgClickCountVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"msg_click_count where msg_id=? ";
		
		Object[] parms = new Object[] {msgId};
		try {
			MsgClickCountVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgClickCountVo>(MsgClickCountVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public int getBroadcastsCount(PagingCountVo vo) {
		List<Object> parms = new ArrayList<>();
		String whereSql = buildWhereClause(vo, parms);
		String sql = 
				"select count(*) " +
				" from msg_click_count a "
				+ " join msg_inbox m on m.msg_id=a.msg_id "
				+ " join email_address e on e.email_addr_id=m.from_addr_id " +
				whereSql +
				" and a.start_time is not null ";
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	@Override
	public List<MsgClickCountVo> getBroadcastsWithPaging(PagingCountVo vo) {
		List<Object> parms = new ArrayList<>();
		String whereSql = buildWhereClause(vo, parms);
		/*
		 * paging logic
		 */
		int rows = 0;
		if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			rows = getBroadcastsCount(vo);
		}
		PagingContext ctx = PagingVo.getPagingWhereSql(vo, CRIT, parms, rows, "a.msg_id");
		whereSql += ctx.whereSql;
		
		String sql = 
			"select a.*, e.email_addr_id, e.email_addr as from_addr " +
			" from msg_click_count a "
			+ " join msg_inbox m on m.msg_id=a.msg_id "
			+ " join email_address e on e.email_addr_id=m.from_addr_id " +
			whereSql +
			" and a.start_time is not null " +
			" order by a.msg_id " + ctx.fetchOrder +
			" limit " + ctx.pageSize;
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<MsgClickCountVo> list = getJdbcTemplate().query(sql, parms.toArray(), 
				new BeanPropertyRowMapper<MsgClickCountVo>(MsgClickCountVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if ("asc".equals(ctx.fetchOrder)) {
			// reverse the list
			Collections.reverse(list);
		}
		if (!list.isEmpty()) {
			vo.setSearchObjFirst(list.get(0).getMsgId());
			vo.setSearchObjLast(list.get(list.size() - 1).getMsgId());
		}
		return list;
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and " };

	private String buildWhereClause(PagingCountVo vo, List<Object> parms) {
		String whereSql = "";
//		if (StringUtil.isNotEmpty(vo.getStatusId())) {
//			whereSql += CRIT[parms.size()] + " a.status_id = ? ";
//			parms.add(vo.getStatusId());
//		}
		if (vo.getSentCount() != null) {
			whereSql += CRIT[parms.size()] + " a.sent_count >= ? ";
			parms.add(vo.getSentCount());
		}
		if (vo.getOpenCount() != null) {
			whereSql += CRIT[parms.size()] + " a.open_count >= ? ";
			parms.add(vo.getOpenCount());
		}
		if (vo.getClickCount() != null) {
			whereSql += CRIT[parms.size()] + " a.click_count >= ? ";
			parms.add(vo.getClickCount());
		}
		// search by address
		if (StringUtils.isNotBlank(vo.getFromEmailAddr())) {
			String addr = vo.getFromEmailAddr().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " e.orig_email_addr LIKE ? ";
				parms.add("%" + addr + "%");
			} else {
				String regex = (addr + "").replaceAll("[ ]+", "|"); // any word
				whereSql += CRIT[parms.size()] + " e.orig_email_addr REGEXP ? ";
				parms.add(regex);
			}
		}
		return whereSql;
	}

	@Override
	public int update(MsgClickCountVo msgClickCountVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgClickCountVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_click_count", msgClickCountVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int updateSentCount(long msgId, int count) {
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"sent_count=sent_count+" + count +
				", end_time=now() " +
			" where " +
				" msg_id=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int updateOpenCount(long msgId, int count) {
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"open_count=open_count+" + count +
				", last_open_time=now() " +
			" where " +
				" msg_id=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int updateOpenCount(long msgId) {
		return updateOpenCount(msgId, 1);
	}

	@Override
	public int updateClickCount(long msgId, int count) {
		Timestamp currTime = new Timestamp(System.currentTimeMillis());
		List<Object> fields = new ArrayList<>();
		fields.add(currTime);
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"click_count=click_count+" + count +
				" ,last_click_time=? " +
			" where " +
				" msg_id=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int updateClickCount(long msgId) {
		return updateClickCount(msgId, 1);
	}

	@Override
	public int updateReferalCount(long msgId, int count) {
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"referral_count=referral_count+" + count +
			" where " +
				" msg_id=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}

	@Override
	public int updateReferalCount(long msgId) {
		return updateReferalCount(msgId, 1);
	}

	@Override
	public int updateStartTime(long msgId) {
		Timestamp currTime = new Timestamp(System.currentTimeMillis());
		List<Object> fields = new ArrayList<>();
		fields.add(currTime);
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"start_time=? " +
			" where " +
				" msg_id=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int updateUnsubscribeCount(long msgId, int count) {
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"unsubscribe_count=unsubscribe_count+" + count +
			" where " +
				" msg_id=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int updateComplaintCount(long msgId, int count) {
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"complaint_count=complaint_count+" + count +
			" where " +
				" msg_id=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from msg_click_count where msg_id=? ";
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgClickCountVo msgClickCountVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgClickCountVo);
		String sql = MetaDataUtil.buildInsertStatement("msg_click_count", msgClickCountVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

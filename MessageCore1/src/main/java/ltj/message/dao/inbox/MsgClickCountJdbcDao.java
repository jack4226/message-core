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
import ltj.message.util.StringUtil;
import ltj.message.vo.PagingCountVo;
import ltj.message.vo.PagingVo;
import ltj.message.vo.inbox.MsgClickCountVo;

@Component("msgClickCountDao")
public class MsgClickCountJdbcDao extends AbstractDao implements MsgClickCountDao {
	
	@Override
	public MsgClickCountVo getRandomRecord() {
		String sql = 
			"select * " +
			"from msg_click_count " +
				" where MsgId >= (RAND() * (select max(MsgId) from msg_click_count)) " +
			" order by MsgId limit 1 ";
		
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
				"msg_click_count where SentCount > 0 and StartTime is not null ";
		int count = getJdbcTemplate().queryForObject(sql, Integer.class);
		return count;
	}
	
	@Override
	public MsgClickCountVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"msg_click_count where msgid=? ";
		
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
				+ " join msg_inbox m on m.MsgId=a.MsgId "
				+ " join email_address e on e.email_addr_id=m.FromAddrid " +
				whereSql +
				" and a.StartTime is not null ";
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
		String fetchOrder = "desc";
		int pageSize = vo.getPageSize();
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getNbrIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId < ? ";
				parms.add(vo.getNbrIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getNbrIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId > ? ";
				parms.add(vo.getNbrIdFirst());
				fetchOrder = "asc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			int rows = getBroadcastsCount(vo);
			pageSize = rows % vo.getPageSize();
			if (pageSize == 0) {
				pageSize = Math.min(rows, vo.getPageSize());
			}
			fetchOrder = "asc";
//			List<MsgClickCountVo> lastList = new ArrayList<MsgClickCountVo>();
//			vo.setPageAction(PagingVo.PageAction.NEXT);
//			while (true) {
//				List<MsgClickCountVo> nextList = getBroadcastsWithPaging(vo);
//				if (!nextList.isEmpty()) {
//					lastList = nextList;
//					vo.setNbrIdLast(nextList.get(nextList.size() - 1).getMsgId());
//				}
//				else {
//					break;
//				}
//			}
//			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getNbrIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId <= ? ";
				parms.add(vo.getNbrIdFirst());
			}
		}
		
		String sql = 
			"select a.*, e.email_addr_id, e.email_addr as fromAddr " +
			" from msg_click_count a "
			+ " join msg_inbox m on m.MsgId=a.MsgId "
			+ " join email_address e on e.email_addr_id=m.FromAddrid " +
			whereSql +
			" and a.StartTime is not null " +
			" order by a.MsgId " + fetchOrder +
			" limit " + pageSize;
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<MsgClickCountVo> list = getJdbcTemplate().query(sql, parms.toArray(), 
				new BeanPropertyRowMapper<MsgClickCountVo>(MsgClickCountVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if ("asc".equals(fetchOrder)) {
			// reverse the list
			Collections.reverse(list);
		}
		if (!list.isEmpty()) {
			vo.setNbrIdFirst(list.get(0).getMsgId());
			vo.setNbrIdLast(list.get(list.size() - 1).getMsgId());
		}
		return list;
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and " };

	private String buildWhereClause(PagingCountVo vo, List<Object> parms) {
		String whereSql = "";
		if (!StringUtil.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " a.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		if (vo.getSentCount() != null) {
			whereSql += CRIT[parms.size()] + " a.SentCount >= ? ";
			parms.add(vo.getSentCount());
		}
		if (vo.getOpenCount() != null) {
			whereSql += CRIT[parms.size()] + " a.OpenCount >= ? ";
			parms.add(vo.getOpenCount());
		}
		if (vo.getClickCount() != null) {
			whereSql += CRIT[parms.size()] + " a.ClickCount >= ? ";
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
				"SentCount=SentCount+" + count +
				", EndTime=now() " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int updateOpenCount(long msgId, int count) {
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"OpenCount=OpenCount+" + count +
				", LastOpenTime=now() " +
			" where " +
				" MsgId=? ";
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
				"ClickCount=ClickCount+" + count +
				" ,LastClickTime=? " +
			" where " +
				" MsgId=? ";
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
				"ReferralCount=ReferralCount+" + count +
			" where " +
				" MsgId=? ";
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
				"StartTime=? " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int updateUnsubscribeCount(long msgId, int count) {
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"UnsubscribeCount=UnsubscribeCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int updateComplaintCount(long msgId, int count) {
		List<Object> fields = new ArrayList<>();
		fields.add(msgId);
		String sql =
			"update msg_click_count set " +
				"ComplaintCount=ComplaintCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from msg_click_count where msgid=? ";
		
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

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
import ltj.message.vo.inbox.MsgClickCountsVo;

@Component("msgClickCountsDao")
public class MsgClickCountsJdbcDao extends AbstractDao implements MsgClickCountsDao {
	
	@Override
	public MsgClickCountsVo getRandomRecord() {
		String sql = 
			"select * " +
			"from " +
				"MsgClickCounts where MsgId >= (RAND() * (select max(MsgId) from MsgClickCounts)) order by MsgId limit 1 ";
		
		List<MsgClickCountsVo> list = getJdbcTemplate().query(sql,
				new BeanPropertyRowMapper<MsgClickCountsVo>(MsgClickCountsVo.class));
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
				"MsgClickCounts where SentCount > 0 and StartTime is not null ";
		int count = getJdbcTemplate().queryForObject(sql, Integer.class);
		return count;
	}
	
	@Override
	public MsgClickCountsVo getByPrimaryKey(long msgId) {
		String sql = 
			"select * " +
			"from " +
				"MsgClickCounts where msgid=? ";
		
		Object[] parms = new Object[] {msgId};
		try {
			MsgClickCountsVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<MsgClickCountsVo>(MsgClickCountsVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public List<MsgClickCountsVo> getBroadcastsWithPaging(PagingCountVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "desc";
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
			List<MsgClickCountsVo> lastList = new ArrayList<MsgClickCountsVo>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<MsgClickCountsVo> nextList = getBroadcastsWithPaging(vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setNbrIdLast(nextList.get(nextList.size() - 1).getMsgId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getNbrIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId <= ? ";
				parms.add(vo.getNbrIdFirst());
			}
		}
		
		String sql = 
			"select a.*, e.EmailAddrId, e.EmailAddr as fromAddr " +
			" from MsgClickCounts a "
			+ " join MsgInbox m on m.MsgId=a.MsgId "
			+ " join EmailAddr e on e.EmailAddrId=m.FromAddrid " +
			whereSql +
			" and a.StartTime is not null " +
			" order by a.MsgId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<MsgClickCountsVo> list = getJdbcTemplate().query(sql, parms.toArray(), 
				new BeanPropertyRowMapper<MsgClickCountsVo>(MsgClickCountsVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
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
				whereSql += CRIT[parms.size()] + " e.OrigEmailAddr LIKE ? ";
				parms.add("%" + addr + "%");
			} else {
				String regex = (addr + "").replaceAll("[ ]+", "|"); // any word
				whereSql += CRIT[parms.size()] + " e.OrigEmailAddr REGEXP ? ";
				parms.add(regex);
			}
		}
		return whereSql;
	}

	@Override
	public int update(MsgClickCountsVo msgClickCountsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgClickCountsVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgClickCounts", msgClickCountsVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsUpadted;
	}
	
	@Override
	public int updateSentCount(long msgId, int count) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
				"SentCount=SentCount+" + count +
				", EndTime=now() " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	@Override
	public int updateOpenCount(long msgId, int count) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
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
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(currTime);
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
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
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
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
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(currTime);
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
				"StartTime=? " +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int updateUnsubscribeCount(long msgId, int count) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
				"UnsubscribeCount=UnsubscribeCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int updateComplaintCount(long msgId, int count) {
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		String sql =
			"update MsgClickCounts set " +
				"ComplaintCount=ComplaintCount+" + count +
			" where " +
				" MsgId=? ";
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;		
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId) {
		String sql = 
			"delete from MsgClickCounts where msgid=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgClickCountsVo msgClickCountsVo) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgClickCountsVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgClickCounts", msgClickCountsVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
}

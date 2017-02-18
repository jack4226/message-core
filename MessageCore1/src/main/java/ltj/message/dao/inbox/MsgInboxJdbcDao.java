package ltj.message.dao.inbox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import ltj.message.constant.Constants;
import ltj.message.constant.MsgDirection;
import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.message.vo.inbox.SearchFieldsVo;

@Component("msgInboxDao")
public class MsgInboxJdbcDao extends AbstractDao implements MsgInboxDao {

	@Override
	public MsgInboxVo getByPrimaryKey(long msgId) {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			"from " +
				"MsgInbox " +
			" where msgId=? ";
		Object[] parms = new Object[] {msgId};
		try {
			MsgInboxVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public MsgInboxVo getLastRecord() {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			"from " +
				"MsgInbox " +
			" where msgId = (select max(MsgId) from MsgInbox) ";
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public MsgInboxVo getRandomRecord() {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			"from " +
				"MsgInbox where msgId >= (RAND() * (select max(msgId) from MsgInbox)) " +
			" order by msgId limit 1 ";
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public MsgInboxVo getLastReceivedRecord() {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			"from " +
				"MsgInbox " +
			" where msgId = (select max(MsgId) from MsgInbox where MsgDirection = 'R') ";
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public MsgInboxVo getLastSentRecord() {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			"from " +
				"MsgInbox " +
			" where msgId = (select max(MsgId) from MsgInbox where MsgDirection = 'S') ";
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public List<MsgInboxWebVo> getByLeadMsgId(long leadMsgId) {
		String sql = 
			"select *, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			" from " +
				" MsgInbox " +
			" where leadMsgId=? " +
			" order by msgId";
		Object[] parms = new Object[] {leadMsgId};
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		return list;
	}
	
	@Override
	public List<MsgInboxWebVo> getByMsgRefId(long msgRefId) {
		String sql = 
			"select *, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			" from " +
				" MsgInbox " +
			" where MsgRefId=? " +
			" order by msgId";
		Object[] parms = new Object[] {msgRefId};
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		return list;
	}
	
	@Override
	public List<MsgInboxVo> getByFromAddrId(long addrId) {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			" from " +
				" MsgInbox " +
			" where fromAddrId=? " +
			" order by msgId";
		Object[] parms = new Object[] {addrId};
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return list;
	}
	
	@Override
	public List<MsgInboxVo> getByToAddrId(long addrId) {
		String sql = 
			"select *, UpdtTime as OrigUpdtTime, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			" from " +
				" MsgInbox " +
			" where toAddrId=? " +
			" order by msgId";
		Object[] parms = new Object[] {addrId};
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return list;
	}
	
	@Override
	public List<MsgInboxVo> getRecent(int days) {
		if (days < 0) days = 365 * 20; // retrieve all mails
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR,  - days);
		return getRecent(cal.getTime());
	}
	
	/**
	 * retrieve up to 100 rows
	 */
	@Override
	public List<MsgInboxVo> getRecent(Date date) {
		if (date == null) {
			date = new java.util.Date();
		}
		String sql = 
			"select *, UpdtTime as OrigUpdtTime, ReadCount as OrigReadCount, StatusId as OrigStatusId " +
			" from " +
				" MsgInbox " +
			" where receivedTime>=? " +
			" order by receivedTime desc limit 100";
		Object[] parms = new Object[] {new Timestamp(date.getTime())};
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return list;
	}
	
	@Override
	public int getInboxUnreadCount() {
		return getMsgUnreadCountDao().selectInboxUnreadCount();
	}
	
	@Override
	public int getSentUnreadCount() {
		return getMsgUnreadCountDao().selectSentUnreadCount();
	}
	
	@Override
	public int getAllUnreadCount() {
		return getInboxUnreadCount() + getSentUnreadCount();
	}
	
	@Override
	public int resetInboxUnreadCount() {
		String sql = 
			"select count(*) " +
			" from " +
				" MsgInbox " +
			" where ReadCount=0 " +
				" and MsgDirection=? " +
				" and (StatusId is null OR StatusId!=?) ";
		List<Object> parms = new ArrayList<Object>();
		parms.add(MsgDirection.RECEIVED.value());
		parms.add(StatusId.CLOSED.value());
		int inboxUnreadCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		getMsgUnreadCountDao().resetInboxUnreadCount(inboxUnreadCount);
		return inboxUnreadCount;
	}
	
	@Override
	public int resetSentUnreadCount() {
		String sql = 
			"select count(*) " +
			" from " +
				" MsgInbox " +
			" where ReadCount=0 " +
				" and MsgDirection=? " +
				" and (StatusId is null OR StatusId!=?) ";
		List<Object> parms = new ArrayList<Object>();
		parms.add(MsgDirection.SENT.value());
		parms.add(StatusId.CLOSED.value());
		int sentUnreadCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		getMsgUnreadCountDao().resetSentUnreadCount(sentUnreadCount);
		return sentUnreadCount;
	}
	
	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };

	@Override
	public int getRowCountForWeb(SearchFieldsVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = getWhereSqlForWeb(vo, parms);
		String sql = 
			"SELECT count(*) " +
			" FROM MsgInbox a " + 
			" JOIN EmailAddr b ON a.FromAddrId=b.EmailAddrId " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	@Override
	public List<MsgInboxWebVo> getListForWeb(SearchFieldsVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = getWhereSqlForWeb(vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "desc";
		if (vo.getPageAction().equals(SearchFieldsVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.NEXT)) {
			if (vo.getMsgIdLast() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId < ? ";
				parms.add(vo.getMsgIdLast());
			}
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.PREVIOUS)) {
			if (vo.getMsgIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId > ? ";
				parms.add(vo.getMsgIdFirst());
				fetchOrder = "asc";
			}
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.LAST)) {
			List<MsgInboxWebVo> lastList = new ArrayList<MsgInboxWebVo>();
			vo.setPageAction(SearchFieldsVo.PageAction.NEXT);
			while (true) {
				List<MsgInboxWebVo> nextList = getListForWeb(vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setMsgIdLast(nextList.get(nextList.size() - 1).getMsgId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(SearchFieldsVo.PageAction.CURRENT)) {
			if (vo.getMsgIdFirst() > -1) {
				whereSql += CRIT[parms.size()] + " a.MsgId <= ? ";
				parms.add(vo.getMsgIdFirst());
			}
		}
		// build SQL
		String sql = 
			"SELECT " +
				"MsgId, " +
				"MsgRefId, " +
				"LeadMsgId, " +
				"MsgSubject, " +
				"ReceivedTime, " +
				"FromAddrId, " +
				"ToAddrId, " +
				"RuleName, " +
				"ReadCount, " +
				"ReplyCount, " +
				"ForwardCount, " +
				"Flagged, " +
				"MsgDirection, " +
				"a.StatusId, " +
				"AttachmentCount, " +
				"AttachmentSize, " +
				"MsgBodySize, " +
				"ReadCount as OrigReadCount, " +
				"a.StatusId as OrigStatusId " +
			" FROM " +
				"MsgInbox a " +
				" JOIN EmailAddr b ON a.FromAddrId=b.EmailAddrId " +
				whereSql +
			" order by MsgId " + fetchOrder +
			" limit " + vo.getPageSize();
		// set result set size
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(SearchFieldsVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}
	
	private String getWhereSqlForWeb(SearchFieldsVo vo, List<Object> parms) {
		String whereSql = "";
		// Closed?
		String closed = null;
		if (vo.getMsgType() != null) {
			if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Closed)) {
				closed = StatusId.CLOSED.value();
			}
		}
		if (closed != null) {
			whereSql += CRIT[parms.size()] + " a.StatusId = ? ";
			parms.add(StatusId.CLOSED.value());
		}
		else {
			whereSql += CRIT[parms.size()] + " a.StatusId != ? ";
			parms.add(StatusId.CLOSED.value());
		}
		// msgDirection
		String direction = null;
		if (vo.getMsgType() != null) {
			if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Received)) {
				direction = MsgDirection.RECEIVED.value();
			}
			else if (vo.getMsgType().equals(SearchFieldsVo.MsgType.Sent)) {
				direction = MsgDirection.SENT.value();
			}
		}
		if (direction != null && closed == null) { // and not closed
			whereSql += CRIT[parms.size()] + " a.MsgDirection = ? ";
			parms.add(direction);
		}
		// ruleName
		if (StringUtils.isNotBlank(vo.getRuleName())) {
			if (!SearchFieldsVo.RuleName.All.toString().equals(vo.getRuleName())) {
				whereSql += CRIT[parms.size()] + " a.RuleName = ? ";
				parms.add(vo.getRuleName());
			}
		}
		// toAddress
		if (vo.getToAddrId() != null) {
			whereSql += CRIT[parms.size()] + " a.ToAddrId = ? ";
			parms.add(vo.getToAddrId());
		}
		// fromAddress
		if (vo.getFromAddrId() != null) {
			whereSql += CRIT[parms.size()] + " a.FromAddrId = ? ";
			parms.add(vo.getFromAddrId());
		}
		// readCount
		if (vo.getRead() != null) {
			if (vo.getRead().booleanValue()) {
				whereSql += CRIT[parms.size()] + " a.ReadCount > ? ";
			}
			else {
				whereSql += CRIT[parms.size()] + " a.ReadCount <= ? ";
			}
			parms.add(0);
		}
		// msgFlag
		if (vo.getFlagged() != null && vo.getFlagged().booleanValue()) {
			whereSql += CRIT[parms.size()] + " a.Flagged = ? ";
			parms.add(Constants.Y);
		}
		// subject
		if (StringUtils.isNotBlank(vo.getSubject())) {
			String subj = vo.getSubject().trim();
			if (subj.indexOf(" ") < 0) { // a single word
				whereSql += CRIT[parms.size()] + " a.MsgSubject LIKE '%" + subj + "%' ";
			}
			else {
				String regex = (subj + "").replaceAll("[ ]+", "|");
				whereSql += CRIT[parms.size()] + " a.MsgSubject REGEXP '" + regex + "' ";
			}
		}
		// body
		if (StringUtils.isNotBlank(vo.getBody())) {
			String body = vo.getBody().trim();
			if (body.indexOf(" ") < 0) { // a single word
				whereSql += CRIT[parms.size()] + " a.MsgBody LIKE '%" + body + "%' ";
			}
			else {
				// ".+" or "[[:space:]].*" or "([[:space:]]+|[[:space:]].+[[:space:]])"
				String regex = (body + "").replaceAll("[ ]+", "[[:space:]].*");
				whereSql += CRIT[parms.size()] + " a.MsgBody REGEXP '" + regex + "' ";
			}
		}
		// from address
		if (StringUtils.isNotBlank(vo.getFromAddr()) && vo.getFromAddrId() == null) {
			String from = vo.getFromAddr().trim();
			if (from.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " b.OrigEmailAddr LIKE '%" + from + "%' ";
			}
			else {
				//String regex = (from + "").replaceAll("[ ]+", ".+");
				String regex = (from + "").replaceAll("[ ]+", "|");
				whereSql += CRIT[parms.size()] + " b.OrigEmailAddr REGEXP '" + regex + "' ";
			}
		}
		
		return whereSql;
	}

	@Override
	public int update(MsgInboxWebVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgInboxVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgInbox", msgInboxVo);
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}

	@Override
	public int updateCounts(MsgInboxWebVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.getFlagged());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update MsgInbox set " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"ReadCount=?, " +
				"ReplyCount=?, " +
				"ForwardCount=?, " +
				"Flagged=? " +
			" where " +
				" msgId=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgInboxVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}

	private void adjustUnreadCounts(MsgInboxWebVo msgInboxVo) {
		if (!StatusId.CLOSED.value().equals(msgInboxVo.getOrigStatusId())) { // Was Open
			if (msgInboxVo.getOrigReadCount() == 0 && msgInboxVo.getReadCount() > 0) {
				updateCounts(msgInboxVo, -1);
			}
			else if (msgInboxVo.getOrigReadCount() > 0 && msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, 1);
			}
			else if (StatusId.CLOSED.value().equals(msgInboxVo.getStatusId()) && msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, -1);
			}
		}
		else { // Was Closed
			if (!StatusId.CLOSED.value().equals(msgInboxVo.getStatusId()) && msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, 1);
			}
		}
	}
	
	private void adjustUnreadCounts(MsgInboxVo msgInboxVo) {
		if (!StatusId.CLOSED.value().equals(msgInboxVo.getOrigStatusId())) { // Was Open
			if (msgInboxVo.getOrigReadCount() == 0 && msgInboxVo.getReadCount() > 0) {
				updateCounts(msgInboxVo, -1);
			}
			else if (msgInboxVo.getOrigReadCount() > 0 && msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, 1);
			}
			else if (StatusId.CLOSED.value().equals(msgInboxVo.getStatusId()) && msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, -1);
			}
		}
		else { // Was Closed
			if (!StatusId.CLOSED.value().equals(msgInboxVo.getStatusId()) && msgInboxVo.getReadCount() == 0) {
				updateCounts(msgInboxVo, 1);
			}
		}
	}
	
	private void updateCounts(MsgInboxVo msgInboxVo, int count) {
		if (MsgDirection.RECEIVED.value().equals(msgInboxVo.getMsgDirection())) {
			getMsgUnreadCountDao().updateInboxUnreadCount(count);
		}
		else if (MsgDirection.SENT.value().equals(msgInboxVo.getMsgDirection())) {
			getMsgUnreadCountDao().updateSentUnreadCount(count);
		}
	}
	
	private void updateCounts(MsgInboxWebVo msgInboxVo, int count) {
		if (MsgDirection.RECEIVED.value().equals(msgInboxVo.getMsgDirection())) {
			getMsgUnreadCountDao().updateInboxUnreadCount(count);
		}
		else if (MsgDirection.SENT.value().equals(msgInboxVo.getMsgDirection())) {
			getMsgUnreadCountDao().updateSentUnreadCount(count);
		}
	}
	
	@Override
	public int updateCounts(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.getFlagged());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update MsgInbox set " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"ReadCount=?, " +
				"ReplyCount=?, " +
				"ForwardCount=?, " +
				"Flagged=? " +
			" where " +
				" msgId=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgInboxVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}

	@Override
	public int updateStatusId(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getStatusId());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update MsgInbox set " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"StatusId=? " +
			" where " +
				" msgId=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			fields.add(msgInboxVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
		}
		return rowsUpadted;
	}
	
	@Override
	public int updateStatusIdByLeadMsgId(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getStatusId());
		
		fields.add(msgInboxVo.getLeadMsgId());
		
		String sql =
			"update MsgInbox set " +
				"UpdtTime=?, " +
				"UpdtUserId=?, " +
				"StatusId=? " +
			" where " +
				" LeadMsgId=? ";

		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}
	
	@Override
	public int update(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgInboxVo);
		String sql = MetaDataUtil.buildUpdateStatement("MsgInbox", msgInboxVo);
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		if (rowsUpadted > 0) {
			adjustUnreadCounts(msgInboxVo);
			msgInboxVo.setOrigReadCount(msgInboxVo.getReadCount());
			msgInboxVo.setOrigStatusId(msgInboxVo.getStatusId());
			msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		}
		return rowsUpadted;
	}
	
	@Override
	public int deleteByPrimaryKey(long msgId) {
		MsgInboxVo msgInboxVo = getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			return 0;
		}
		
		String sql = 
			"delete from MsgInbox where msgId=? "; // TODO only refMsgId is null or orphaned
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(msgId);
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		if (rowsDeleted > 0 && msgInboxVo.getOrigReadCount() == 0
				&& !StatusId.CLOSED.value().equals(msgInboxVo.getStatusId())) {
			if (MsgDirection.RECEIVED.value().equals(msgInboxVo.getMsgDirection())) {
				getMsgUnreadCountDao().updateInboxUnreadCount(-1);
			}
			else if (MsgDirection.SENT.value().equals(msgInboxVo.getMsgDirection())) {
				getMsgUnreadCountDao().updateSentUnreadCount(-1);
			}
		}
		return rowsDeleted;
	}
	
	@Override
	public int insert(MsgInboxVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgInboxVo);
		String sql = MetaDataUtil.buildInsertStatement("MsgInbox", msgInboxVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		msgInboxVo.setOrigUpdtTime(msgInboxVo.getUpdtTime());
		//msgInboxVo.setMsgId(getJdbcTemplate().queryForInt(getRowIdSql()));
		if (rowsInserted > 0 && msgInboxVo.getReadCount() == 0
				&& !StatusId.CLOSED.value().equals(msgInboxVo.getStatusId())) {
			if (MsgDirection.RECEIVED.value().equals(msgInboxVo.getMsgDirection())) {
				getMsgUnreadCountDao().updateInboxUnreadCount(1);
			}
			else if (MsgDirection.SENT.value().equals(msgInboxVo.getMsgDirection())) {
				getMsgUnreadCountDao().updateSentUnreadCount(1);
			}
		}
		return rowsInserted;
	}

	@Autowired
	private MsgUnreadCountDao msgUnreadCountDao;
	
	MsgUnreadCountDao getMsgUnreadCountDao() {
		return msgUnreadCountDao;
	}
}

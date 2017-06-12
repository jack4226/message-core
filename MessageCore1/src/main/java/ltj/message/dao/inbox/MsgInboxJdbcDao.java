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

import ltj.data.preload.FolderEnum;
import ltj.message.constant.AddressType;
import ltj.message.constant.MsgDirection;
import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.vo.PagingVo.PageAction;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.message.vo.inbox.SearchFieldsVo;

@Component("msgInboxDao")
public class MsgInboxJdbcDao extends AbstractDao implements MsgInboxDao {

	@Autowired
	private MsgUnreadCountDao msgUnreadCountDao;
	
	@Override
	public MsgInboxVo getByPrimaryKey(long msgId) {
		String sql = 
			"select *, updt_time as OrigUpdtTime, read_count as OrigReadCount, status_id as OrigStatusId " +
			"from " +
				"msg_inbox " +
			" where msg_id=? ";
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
			"select *, updt_time as OrigUpdtTime, read_count as OrigReadCount, status_id as OrigStatusId " +
			"from " +
				"msg_inbox " +
			" where msg_id = (select max(msg_id) from msg_inbox) ";
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
			"select *, updt_time as OrigUpdtTime, read_count as OrigReadCount, status_id as OrigStatusId " +
			"from " +
				"msg_inbox where msg_id >= (RAND() * (select max(msg_id) from msg_inbox)) " +
			" order by msg_id limit 1 ";
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
			"select *, updt_time as OrigUpdtTime, read_count as OrigReadCount, status_id as OrigStatusId " +
			"from " +
				"msg_inbox " +
			" where msg_id = (select max(msg_id) from msg_inbox where msg_direction = 'R') ";
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
			"select *, updt_time as OrigUpdtTime, read_count as OrigReadCount, status_id as OrigStatusId " +
			"from " +
				"msg_inbox " +
			" where msg_id = (select max(msg_id) from msg_inbox where msg_direction = 'S') ";
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
			"select *, read_count as OrigReadCount, status_id as OrigStatusId " +
			" from " +
				" msg_inbox " +
			" where lead_msg_id=? " +
			" order by msg_id";
		Object[] parms = new Object[] {leadMsgId};
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		return list;
	}
	
	@Override
	public MsgInboxWebVo getByLeastLeadMsgId() {
		String sql = 
			"select *, read_count as OrigReadCount, status_id as OrigStatusId " +
			" from " +
				" msg_inbox " +
			" where lead_msg_id is not null and msg_id != lead_msg_id " +
			" order by lead_msg_id limit 1";
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		if (list.isEmpty()) {
			return null;
		}
		else {
			return list.get(0);
		}
	}
	
	@Override
	public List<MsgInboxWebVo> getByMsgRefId(long msgRefId) {
		String sql = 
			"select *, read_count as OrigReadCount, status_id as OrigStatusId " +
			" from " +
				" msg_inbox " +
			" where msg_ref_id=? " +
			" order by msg_id";
		Object[] parms = new Object[] {msgRefId};
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		return list;
	}
	
	@Override
	public List<MsgInboxVo> getByFromAddrId(long addrId) {
		return getByToAddrIdAndType(addrId, AddressType.FROM_ADDR);
	}
	
	@Override
	public List<MsgInboxVo> getByToAddrId(long addrId) {
		return getByToAddrIdAndType(addrId, AddressType.TO_ADDR);
	}

	private List<MsgInboxVo> getByToAddrIdAndType(long addrId, AddressType type) {
			String sql = 
			"select a.*, a.updt_time as OrigUpdtTime, a.read_count as OrigReadCount, a.status_id as OrigStatusId " +
			" from msg_inbox a " +
				" join msg_address s on s.msg_id=a.msg_id and s.addr_type=? " +
				" join email_address e on e.email_addr=s.addr_value and e.email_addr_id=? " +
			" order by a.msg_id";
		Object[] parms = new Object[] {type.value(), addrId};
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return list;
	}

	@Override
	public List<MsgInboxVo> getByFromAddress(String address) {
		return getByAddressAndType(address, AddressType.FROM_ADDR);
	}
	
	@Override
	public List<MsgInboxVo> getByToAddress(String address) {
		return getByAddressAndType(address, AddressType.TO_ADDR);
	}
	
	private List<MsgInboxVo> getByAddressAndType(String address, AddressType type) {
		String sql = 
			"select a.*, a.updt_time as OrigUpdtTime, a.read_count as OrigReadCount, a.status_id as OrigStatusId " +
			" from msg_inbox a " +
				" join msg_address s on s.msg_id=a.msg_id and s.addr_type=? " +
				" join email_address e on e.email_addr=s.addr_value and e.email_addr=? " +
			" order by a.msg_id";
		Object[] parms = new Object[] {type.value(), address};
		List<MsgInboxVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<MsgInboxVo>(MsgInboxVo.class));
		return list;
	}
	
	/**
	 * retrieve up to 100 rows
	 */
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
			"select *, updt_time as OrigUpdtTime, read_count as OrigReadCount, status_id as OrigStatusId " +
			" from " +
				" msg_inbox " +
			" where received_time>=? " +
			" order by received_time desc limit 100";
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
				" msg_inbox " +
			" where read_count=0 " +
				" and msg_direction=? " +
				" and (status_id is null OR status_id!=?) ";
		List<Object> parms = new ArrayList<>();
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
				" msg_inbox " +
			" where read_count=0 " +
				" and msg_direction=? " +
				" and (status_id is null OR status_id!=?) ";
		List<Object> parms = new ArrayList<>();
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
		List<Object> parms = new ArrayList<>();
		String whereSql = getWhereSqlForWeb(vo, parms);
		String sql = 
			"SELECT count(*) " +
			" FROM msg_inbox a " + 
			" JOIN email_address b ON a.from_addr_id=b.email_addr_id " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	@Override
	public List<MsgInboxWebVo> getListForWeb(SearchFieldsVo vo) {
		List<Object> parms = new ArrayList<>();
		String whereSql = getWhereSqlForWeb(vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "desc";
		int pageSize = vo.getPagingVo().getPageSize();
		if (vo.getPagingVo().getPageAction().equals(PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPagingVo().getPageAction().equals(PageAction.NEXT)) {
			if (vo.getPagingVo().getSearchObjLast() != null) {
				whereSql += CRIT[parms.size()] + " a.msg_id < ? ";
				parms.add(vo.getPagingVo().getSearchObjLast());
			}
		}
		else if (vo.getPagingVo().getPageAction().equals(PageAction.PREVIOUS)) {
			if (vo.getPagingVo().getSearchObjFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.msg_id > ? ";
				parms.add(vo.getPagingVo().getSearchObjFirst());
				fetchOrder = "asc";
			}
		}
		else if (vo.getPagingVo().getPageAction().equals(PageAction.LAST)) {
			int rows = getRowCountForWeb(vo);
			pageSize = rows % vo.getPagingVo().getPageSize();
			if (pageSize == 0) {
				pageSize = Math.min(rows, vo.getPagingVo().getPageSize());
			}
			fetchOrder = "asc";
		}
		else if (vo.getPagingVo().getPageAction().equals(PageAction.CURRENT)) {
			if (vo.getPagingVo().getSearchObjFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.msg_id <= ? ";
				parms.add(vo.getPagingVo().getSearchObjFirst());
			}
		}
		// build SQL
		String sql = 
			"SELECT " +
				"a.msg_id, " +
				"a.msg_ref_id, " +
				"a.lead_msg_id, " +
				"a.msg_subject, " +
				"a.received_time, " +
				"a.from_addr_id, " +
				"a.to_addr_id, " +
				"a.rule_name, " +
				"a.read_count, " +
				"a.reply_count, " +
				"a.forward_count, " +
				"a.flagged, " +
				"a.msg_direction, " +
				"a.status_id, " +
				"a.attachment_count, " +
				"a.attachment_size, " +
				"a.msg_body_size, " +
				"a.read_count as OrigReadCount, " +
				"a.status_id as OrigStatusId " +
			" FROM " +
				"msg_inbox a " +
				" JOIN email_address b ON a.from_addr_id=b.email_addr_id " +
				whereSql +
			" order by a.msg_id " + fetchOrder +
			" limit " + pageSize;
		// set result set size
		getJdbcTemplate().setFetchSize(vo.getPagingVo().getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPagingVo().getPageSize());
		List<MsgInboxWebVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<MsgInboxWebVo>(MsgInboxWebVo.class));
		if ("asc".equals(fetchOrder)) {
			// reverse the list
			Collections.reverse(list);
		}
		if (!list.isEmpty()) { // && !vo.getPageAction().equals(PageAction.CURRENT)) {
			vo.getPagingVo().setSearchObjFirst(list.get(0).getMsgId());
			vo.getPagingVo().setSearchObjLast(list.get(list.size() - 1).getMsgId());
		}
		return list;
	}
	
	private String getWhereSqlForWeb(SearchFieldsVo vo, List<Object> parms) {
		String whereSql = "";
		// Closed?
		String closed = null;
		if (vo.getFolderType() != null) {
			if (vo.getFolderType().equals(FolderEnum.Closed)) {
				closed = StatusId.CLOSED.value();
			}
		}
		if (closed != null) {
			whereSql += CRIT[parms.size()] + " a.status_id = ? ";
			parms.add(StatusId.CLOSED.value());
		}
		else {
			whereSql += CRIT[parms.size()] + " a.status_id != ? ";
			parms.add(StatusId.CLOSED.value());
		}
		// msgDirection
		String direction = null;
		if (vo.getFolderType() != null) {
			if (vo.getFolderType().equals(FolderEnum.Inbox)) {
				direction = MsgDirection.RECEIVED.value();
			}
			else if (vo.getFolderType().equals(FolderEnum.Sent)) {
				direction = MsgDirection.SENT.value();
			}
		}
		if (direction != null && closed == null) { // and not closed
			whereSql += CRIT[parms.size()] + " a.msg_direction = ? ";
			parms.add(direction);
		}
		// ruleName
		if (StringUtils.isNotBlank(vo.getRuleName())) {
			if (!SearchFieldsVo.RuleName.All.name().equals(vo.getRuleName())) {
				whereSql += CRIT[parms.size()] + " a.rule_name = ? ";
				parms.add(vo.getRuleName());
			}
		}
		// toAddress
		if (vo.getToAddrId() != null) {
			whereSql += CRIT[parms.size()] + " a.to_addr_id = ? ";
			parms.add(vo.getToAddrId());
		}
		// fromAddress
		if (vo.getFromAddrId() != null) {
			whereSql += CRIT[parms.size()] + " a.from_addr_id = ? ";
			parms.add(vo.getFromAddrId());
		}
		// readCount
		if (vo.getRead() != null) {
			if (vo.getRead()) {
				whereSql += CRIT[parms.size()] + " a.read_count > ? ";
			}
			else {
				whereSql += CRIT[parms.size()] + " a.read_count <= ? ";
			}
			parms.add(0);
		}
		// msgFlag
		if (vo.getFlagged() != null && vo.getFlagged()) {
			whereSql += CRIT[parms.size()] + " a.flagged = ? ";
			parms.add(true);
		}
		// subject
		if (StringUtils.isNotBlank(vo.getSubject())) {
			String subj = vo.getSubject().trim();
			if (subj.indexOf(" ") < 0) { // a single word
				whereSql += CRIT[parms.size()] + " a.msg_subject LIKE ? ";
				parms.add("%" + subj + "%");
			}
			else {
				String regex = (subj + "").replaceAll("[ ]+", "|"); // match any word
				whereSql += CRIT[parms.size()] + " a.msg_subject REGEXP ? ";
				parms.add(regex);
			}
		}
		// body
		if (StringUtils.isNotBlank(vo.getBody())) {
			String body = vo.getBody().trim();
			if (body.indexOf(" ") < 0) { // a single word
				whereSql += CRIT[parms.size()] + " a.msg_body LIKE ? ";
				parms.add("%" + body + "%");
			}
			else {
				// ".+" or "[[:space:]].*" or "([[:space:]]+|[[:space:]].+[[:space:]])"
				String regex = (body + "").replaceAll("[ ]+", "[[:space:]].*");
				whereSql += CRIT[parms.size()] + " a.msg_body REGEXP ? ";
				parms.add(regex);
			}
		}
		// from address
		if (StringUtils.isNotBlank(vo.getFromAddr()) && vo.getFromAddrId() == null) {
			String from = vo.getFromAddr().trim();
			if (from.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " b.orig_email_addr LIKE ? ";
				parms.add("%" + from + "%");
			}
			else {
				//String regex = (from + "").replaceAll("[ ]+", ".+");
				String regex = (from + "").replaceAll("[ ]+", "|");
				whereSql += CRIT[parms.size()] + " b.orig_email_addr REGEXP ? ";
				parms.add(regex);
			}
		}
		
		return whereSql;
	}

	@Override
	public int update(MsgInboxWebVo msgInboxVo) {
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(msgInboxVo);
		String sql = MetaDataUtil.buildUpdateStatement("msg_inbox", msgInboxVo);
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=:origUpdtTime ";
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
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.isFlagged());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update msg_inbox set " +
				"updt_time=?, " +
				"updt_user_id=?, " +
				"read_count=?, " +
				"reply_count=?, " +
				"forward_count=?, " +
				"flagged=? " +
			" where " +
				" msg_id=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=?";
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
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		
		fields.add(msgInboxVo.getReadCount());
		fields.add(msgInboxVo.getReplyCount());
		fields.add(msgInboxVo.getForwardCount());
		fields.add(msgInboxVo.isFlagged());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update msg_inbox set " +
				"updt_time=?, " +
				"updt_user_id=?, " +
				"read_count=?, " +
				"reply_count=?, " +
				"forward_count=?, " +
				"flagged=? " +
			" where " +
				" msg_id=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=?";
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
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getStatusId());
		
		fields.add(msgInboxVo.getMsgId());
		
		String sql =
			"update msg_inbox set " +
				"updt_time=?, " +
				"updt_user_id=?, " +
				"status_id=? " +
			" where " +
				" msg_id=? ";
		
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=?";
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
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		
		List<Object> fields = new ArrayList<>();
		fields.add(msgInboxVo.getUpdtTime());
		fields.add(msgInboxVo.getUpdtUserId());
		fields.add(msgInboxVo.getStatusId());
		
		fields.add(msgInboxVo.getLeadMsgId());
		
		String sql =
			"update msg_inbox set " +
				"updt_time=?, " +
				"updt_user_id=?, " +
				"status_id=? " +
			" where " +
				" lead_msg_id=? ";

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
		String sql = MetaDataUtil.buildUpdateStatement("msg_inbox", msgInboxVo);
		if (msgInboxVo.getOrigUpdtTime() != null) {
			sql += " and updt_time=:origUpdtTime ";
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
			"delete from msg_inbox where msg_id=? "; // TODO only refMsgId is null or orphaned
		
		List<Object> fields = new ArrayList<>();
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
		String sql = MetaDataUtil.buildInsertStatement("msg_inbox", msgInboxVo);
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

	MsgUnreadCountDao getMsgUnreadCountDao() {
		return msgUnreadCountDao;
	}
}

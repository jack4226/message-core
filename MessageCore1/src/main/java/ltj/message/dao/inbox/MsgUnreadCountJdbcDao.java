package ltj.message.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;

@Component("msgUnreadCountDao")
public class MsgUnreadCountJdbcDao extends AbstractDao implements MsgUnreadCountDao {
	protected static final Logger logger = Logger.getLogger(MsgUnreadCountJdbcDao.class);
	
	@Override
	public int updateInboxUnreadCount(int delta) {
		String sql = 
			"update msg_unread_count set InboxUnreadCount = (InboxUnreadCount + " + delta + ")";
		int rowsUpdated = getJdbcTemplate().update(sql);
		return rowsUpdated;
	}

	@Override
	public int updateSentUnreadCount(int delta) {
		String sql = 
			"update msg_unread_count set SentUnreadCount = (SentUnreadCount + " + delta + ")";
		int rowsUpdated = getJdbcTemplate().update(sql);
		return rowsUpdated;
	}

	@Override
	public int resetInboxUnreadCount(int inboxUnreadCount) {
		List<Object> fields = new ArrayList<>();
		fields.add(inboxUnreadCount);

		String sql = "update msg_unread_count set " +
				"InboxUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	@Override
	public int resetSentUnreadCount(int sentUnreadCount) {
		List<Object> fields = new ArrayList<>();
		fields.add(sentUnreadCount);

		String sql = "update msg_unread_count set " +
				"SentUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	@Override
	public int resetUnreadCounts(int inboxUnreadCount, int sentUnreadCount) {
		List<Object> fields = new ArrayList<>();
		fields.add(inboxUnreadCount);
		fields.add(sentUnreadCount);

		String sql = "update msg_unread_count set " +
				"InboxUnreadCount=?," +
				"SentUnreadCount=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	@Override
	public int selectInboxUnreadCount() {
		String sql = "select InboxUnreadCount from msg_unread_count";
		int unreadCount = getJdbcTemplate().queryForObject(sql, Integer.class);
		return unreadCount;
	}

	@Override
	public int selectSentUnreadCount() {
		String sql = "select SentUnreadCount from msg_unread_count";
		int unreadCount = getJdbcTemplate().queryForObject(sql, Integer.class);
		return unreadCount;
	}
}

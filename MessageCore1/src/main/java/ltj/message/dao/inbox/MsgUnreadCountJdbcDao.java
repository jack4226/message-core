package ltj.message.dao.inbox;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;

@Component("msgUnreadCountDao")
public class MsgUnreadCountJdbcDao extends AbstractDao implements MsgUnreadCountDao {
	protected static final Logger logger = LogManager.getLogger(MsgUnreadCountJdbcDao.class);
	
	@Override
	public int updateInboxUnreadCount(int delta) {
		String sql = 
			"update msg_unread_count set inbox_unread_count = (inbox_unread_count + " + delta + ")";
		int rowsUpdated = getJdbcTemplate().update(sql);
		return rowsUpdated;
	}

	@Override
	public int updateSentUnreadCount(int delta) {
		String sql = 
			"update msg_unread_count set sent_unread_count = (sent_unread_count + " + delta + ")";
		int rowsUpdated = getJdbcTemplate().update(sql);
		return rowsUpdated;
	}

	@Override
	public int resetInboxUnreadCount(int inboxUnreadCount) {
		List<Object> fields = new ArrayList<>();
		fields.add(inboxUnreadCount);

		String sql = "update msg_unread_count set " +
				"inbox_unread_count=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	@Override
	public int resetSentUnreadCount(int sentUnreadCount) {
		List<Object> fields = new ArrayList<>();
		fields.add(sentUnreadCount);

		String sql = "update msg_unread_count set " +
				"sent_unread_count=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	@Override
	public int resetUnreadCounts(int inboxUnreadCount, int sentUnreadCount) {
		List<Object> fields = new ArrayList<>();
		fields.add(inboxUnreadCount);
		fields.add(sentUnreadCount);

		String sql = "update msg_unread_count set " +
				"inbox_unread_count=?," +
				"sent_unread_count=?";
		
		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}

	@Override
	public int selectInboxUnreadCount() {
		String sql = "select inbox_unread_count from msg_unread_count";
		int unreadCount = getJdbcTemplate().queryForObject(sql, Integer.class);
		return unreadCount;
	}

	@Override
	public int selectSentUnreadCount() {
		String sql = "select sent_unread_count from msg_unread_count";
		int unreadCount = getJdbcTemplate().queryForObject(sql, Integer.class);
		return unreadCount;
	}
}

package ltj.message.table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.user.SessionUploadDao;
import ltj.message.dao.user.UserDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.SessionUploadVo;
import ltj.message.vo.UserVo;
import ltj.message.vo.inbox.SearchFieldsVo;
import ltj.spring.util.SpringUtil;

public class UserTable extends CreateTableBase {
	/**
	 * Creates a new instance of MailTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public UserTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE user_session");
			System.out.println("Dropped user_session Table...");
		} catch (SQLException e) {}
		try {
			stm.execute("DROP TABLE session_upload");
			System.out.println("Dropped session_upload Table...");
		} catch (SQLException e) {}
		try {
			stm.execute("DROP TABLE user_tbl");
			System.out.println("Dropped user_tbl Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException {
		createUserTable();
		createUserSessionTable();
		createSessionUploadTable();
	}

	void createUserTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE user_tbl ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"UserId varchar(20) NOT NULL, " + 
			"Password varchar(32) NOT NULL, " +
			"SessionId varchar(50), " +
			"FirstName varchar(32), " +
			"LastName varchar(32), " +
			"MiddleInit char(1), " +
			"CreateTime datetime(3) NOT NULL, " +
			"LastVisitTime datetime(3), " +
			"hits Integer NOT NULL DEFAULT 0, " +
			"StatusId char(1) NOT NULL, " + // A - active, I - Inactive
			"Role varchar(5) NOT NULL, " + // admin/user
			"EmailAddr varchar(255), " +
			"DefaultFolder varchar(8), " + // All/Received/Sent/Closed
			"DefaultRuleName varchar(26), " + // All/...
			"DefaultToAddr varchar(255), " +
			"ClientId varchar(16) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (ClientId) REFERENCES client_tbl(ClientId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"Constraint UNIQUE INDEX (UserId) " +
			") ENGINE=InnoDB");
			System.out.println("Created user_tbl Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createSessionUploadTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE session_upload ( " +
			"SessionId varchar(50) NOT NULL, " + 
			"SessionSeq int NOT NULL, " +
			"FileName varchar(100) NOT NULL, " +
			"ContentType varchar(100), " +
			"UserId varchar(20) NOT NULL, " +
			"CreateTime datetime(3) NOT NULL, " +
			"SessionValue mediumblob, " +
			"INDEX (UserId), " +
			"PRIMARY KEY (SessionId, SessionSeq) " +
			") ENGINE=InnoDB");
			System.out.println("Created session_upload Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	/*
	 * DAO not implemented yet
	 */
	void createUserSessionTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE user_session ( " +
			"SessionId varchar(50) NOT NULL, " + 
			"SessionName varchar(50), " +
			"SessionValue text, " +
			"UserRowId Integer NOT NULL, " +
			"CreateTime datetime(3) NOT NULL, " +
			"INDEX (UserRowId), " +
			"PRIMARY KEY (SessionId, SessionName), " +
			"FOREIGN KEY (UserRowId) REFERENCES user_tbl(RowId) ON DELETE CASCADE ON UPDATE CASCADE " +
			") ENGINE=InnoDB");
			System.out.println("Created user_session Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		loadTestUsers();
		loadTestSessionUploads();
		loadTestUserSessions();
	}
	
	void loadTestUsers() {
		try {
			UserVo vo = new UserVo();
			vo.setUserId("admin");
			vo.setPassword("admin");
			vo.setFirstName("default");
			vo.setLastName("admin");
			vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
			vo.setStatusId(StatusId.ACTIVE.value());
			vo.setRole(Constants.ADMIN_ROLE);
			vo.setDefaultFolder(SearchFieldsVo.MsgType.Received.name()); //"All");
			vo.setClientId(Constants.DEFAULT_CLIENTID);
			
			int rows = getUserDao().insert(vo);
		
			vo.setUserId("user");
			vo.setPassword("user");
			vo.setFirstName("default");
			vo.setLastName("user");
			vo.setRole(Constants.USER_ROLE);
			
			rows += getUserDao().insert(vo);
			System.out.println("Number of users inserted = " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void loadTestSessionUploads() {
		try {
			SessionUploadVo vo = new SessionUploadVo();
			vo.setSessionId("test_session_id");
			vo.setSessionSeq(0);
			vo.setFileName("test1.txt");
			vo.setContentType("text/plain");
			vo.setUserId("user");
			vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
			vo.setSessionValue("test upload text 1".getBytes());
			
			int rows = getSessionUploadDao().insert(vo);
			
			vo.setSessionSeq(1);
			vo.setFileName("test2.txt");
			vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
			vo.setSessionValue("test upload text 2".getBytes());
			rows += getSessionUploadDao().insert(vo);
			
			System.out.println("Number of session upload inserted = " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void loadTestUserSessions() throws SQLException {
		List<UserVo> users = getUserDao().getFirst100(false);
		if (users.isEmpty()) {
			System.err.println("user_tbl is empty!");
			return;
		}
		UserVo user = users.get(0);
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO user_session " +
				"(SessionId," +
				"SessionName," +
				"SessionValue," +
				"UserRowId," +
				"CreateTime) " +
				"VALUES (?, ?, ?, ?, ?)"); 
			
			ps.setString(1, "test_session_id");
			ps.setString(2, "test1");
			ps.setBytes(3, "test user session text 1".getBytes());
			ps.setInt(4, user.getRowId());
			ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			ps.execute();
			
			ps.setString(1, "test_session_id");
			ps.setString(2, "test2");
			ps.setBytes(3, "test user session text 2".getBytes());
			ps.setInt(4, user.getRowId());
			ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows to user_session...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	private UserDao userDao;
	private SessionUploadDao uploadDao;
	
	UserDao getUserDao() {
		if (userDao == null) {
			userDao = SpringUtil.getDaoAppContext().getBean(UserDao.class);
		}
		return userDao;
	}
	SessionUploadDao getSessionUploadDao() {
		if (uploadDao == null) {
			uploadDao = SpringUtil.getDaoAppContext().getBean(SessionUploadDao.class);
		}
		return uploadDao;
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			UserTable ct = new UserTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
			ct.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
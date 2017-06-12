package ltj.message.table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import ltj.data.preload.FolderEnum;
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.user.SessionUploadDao;
import ltj.message.dao.user.UserDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.SessionUploadVo;
import ltj.message.vo.UserVo;
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
			"row_id int AUTO_INCREMENT not null, " +
			"user_id varchar(20) NOT NULL, " + 
			"password varchar(32) NOT NULL, " +
			"session_id varchar(50), " +
			"first_name varchar(32), " +
			"last_name varchar(32), " +
			"middle_init char(1), " +
			"create_time datetime(3) NOT NULL, " +
			"last_visit_time datetime(3), " +
			"hits Integer NOT NULL DEFAULT 0, " +
			"status_id char(1) NOT NULL, " + // A - active, I - Inactive
			"role varchar(5) NOT NULL, " + // admin/user
			"email_addr varchar(255), " +
			"default_folder varchar(8), " + // All/Received/Sent/Closed
			"default_rule_name varchar(26), " + // All/...
			"default_to_addr varchar(255), " +
			"client_id varchar(16) NOT NULL, " +
			"CONSTRAINT user_pkey PRIMARY KEY (row_id), " +
			"FOREIGN KEY user_fk_client_id (client_id) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"Constraint UNIQUE INDEX user_ix_user_id (user_id) " +
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
			"row_id int AUTO_INCREMENT not null, " +
			"session_id varchar(50) NOT NULL, " + 
			"session_seq int NOT NULL, " +
			"file_name varchar(100) NOT NULL, " +
			"content_type varchar(100), " +
			"user_id varchar(20) NOT NULL, " +
			"create_time datetime(3) NOT NULL, " +
			"session_value mediumblob, " +
			"CONSTRAINT session_upload_pkey PRIMARY KEY (row_id), " +
			"INDEX session_upload_ix_user_id (user_id), " +
			"CONSTRAINT UNIQUE INDEX session_upload_ix_id_seq (session_id, session_seq) " +
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
			"row_id int AUTO_INCREMENT not null, " +
			"session_id varchar(50) NOT NULL, " + 
			"session_name varchar(50), " +
			"session_value text, " +
			"user_row_id Integer NOT NULL, " +
			"create_time datetime(3) NOT NULL, " +
			"CONSTRAINT user_session_pkey PRIMARY KEY (row_id), " +
			"INDEX user_session_ix_user_row_id (user_row_id), " +
			"CONSTRAINT UNIQUE INDEX user_session_ix_id_seq (session_id, session_name), " +
			"FOREIGN KEY user_session_fk_user_row_id (user_row_id) REFERENCES user_tbl(row_id) ON DELETE CASCADE ON UPDATE CASCADE " +
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
			vo.setDefaultFolder(FolderEnum.Inbox.name()); //"All");
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
				"(session_id," +
				"session_name," +
				"session_value," +
				"user_row_id," +
				"create_time) " +
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
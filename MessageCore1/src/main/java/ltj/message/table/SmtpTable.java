package ltj.message.table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.data.preload.SmtpServerEnum;
import ltj.message.constant.Constants;
import ltj.message.constant.MailServerType;
import ltj.message.constant.StatusId;
import ltj.message.main.CreateTableBase;

public class SmtpTable extends CreateTableBase {
	/**
	 * Creates a new instance of MailTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public SmtpTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE mail_sender_props");
			System.out.println("Dropped mail_sender_props Table...");
		} catch (SQLException e) {}
		try {
			stm.execute("DROP TABLE smtp_server");
			System.out.println("Dropped smtp_server Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException {
		/*
	 	- smtpHost: smtp host domain name or ip address
	 	- smtpPort: smtp port, default = 25
		- serverName: smtp server name
		- useSsl: yes/no, set smtpport to 465 (or -1) if yes
		- useAuth: /yes/no
		- userId: userid to login to smtp server
		- userpswd: password for the user
		- persistence: yes/no, default to yes
	 	- serverType: smtp/exch, default to smtp
		- threads: number of connections to be created, default=1
	 	- retries: number, 0(default) - no retry, n - for n minutes, -1 - infinite
	 	- retryFreq: number, 5(default): every 5 minutes, n: every n minutes
	 	- alertAfter: number, send alert after the number of retries has been attempted
	 	- alertLevel: infor/error/fatal/nolog
		- messageCount: number of messages to send before stopping the process, 0=unlimited
		*/
		try {
			stm.execute("CREATE TABLE smtp_server ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ServerName varchar(50) NOT NULL, " +
			"SmtpHost varchar(100) NOT NULL, " +
			"SmtpPort integer NOT NULL, " +
			"Description varchar(100), " +
			"UseSsl varchar(3) NOT NULL, " +
			"UseAuth varchar(3), " +
			"UserId varchar(30) NOT NULL, " + 
			"UserPswd varchar(30) NOT NULL, " +
			"Persistence varchar(3) NOT NULL, " +
			"StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " +
			"ServerType varchar(5) DEFAULT '" + MailServerType.SMTP.value() + "', " +
			"Threads integer NOT NULL, " +
			"Retries integer NOT NULL, " +
			"RetryFreq integer NOT NULL, " +
			"AlertAfter integer, " +
			"AlertLevel varchar(5), " +
			"MessageCount integer NOT NULL, " +
			"UpdtTime datetime(3) NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (ServerName) " +
			") ENGINE=InnoDB");
			System.out.println("Created smtp_server Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
		
		/*
		- InternalLoopback - internal email address for loop back
		- ExternalLoopback - external email address for loop back
		- UseTestAddr: yes/no: to override the TO address with the value from TestToAddr
		- TestToAddr: use it as the TO address when UseTestAddr is yes
		*/
		try {
			stm.execute("CREATE TABLE mail_sender_props ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"InternalLoopback varchar(100) NOT NULL, " +
			"ExternalLoopback varchar(100) NOT NULL, " +
			"UseTestAddr varchar(3) NOT NULL, " +
			"TestFromAddr varchar(255), " +
			"TestToAddr varchar(255) NOT NULL, " +
			"TestReplytoAddr varchar(255), " + 
			"IsVerpEnabled varchar(3) NOT NULL, " +
			"UpdtTime datetime(3) NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId) " +
			") ENGINE=InnoDB");
			System.out.println("Created mail_sender_props Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		insertReleaseSmtpData();
		insertTestSmtpData();
		insertMailSenderData();
	}
	
	public void loadReleaseData() throws SQLException {
		insertReleaseSmtpData();
		insertMailSenderData();
	}
	
	private String insertSql = 
		"INSERT INTO smtp_server " +
			"(SmtpHost," +
			"SmtpPort," +
			"ServerName," +
			"Description, " +
			"UseSsl," +
			"UserId," +
			"UserPswd," +
			"Persistence," +
			"StatusId," +
			"ServerType," +
			"Threads," +
			"Retries," +
			"RetryFreq," +
			"AlertAfter," +
			"AlertLevel," +
			"MessageCount," +
			"UpdtTime," +
			"UpdtUserId) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				", ?, ?, ?, ?, ?, ?, ?, ?)";

	void insertReleaseSmtpData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(insertSql);
			
			for (SmtpServerEnum ss : SmtpServerEnum.values()) {
				if (ss.isTestOnly()) {
					continue;
				}
				ps.setString(1, ss.getSmtpHost()); // smtpHost
				ps.setInt(2, ss.getSmtpPort()); // smtpPort
				ps.setString(3, ss.getServerName()); // server name
				ps.setString(4, ss.getDescription()); // description
				ps.setString(5, ss.isUseSsl() ? Constants.YES : Constants.NO); // use ssl
				ps.setString(6, ss.getUserId()); // user id
				ps.setString(7, ss.getUserPswd()); // user password
				ps.setString(8, ss.isPersistence() ? Constants.YES : Constants.NO); // persistence
				ps.setString(9, ss.getStatus().value()); // status id
				ps.setString(10, ss.getServerType().value()); // server type
				ps.setInt(11, ss.getNumberOfThreads()); // Threads
				ps.setInt(12, ss.getMaximumRetries()); // retries
				ps.setInt(13, ss.getRetryFreq()); // retry freq
				ps.setInt(14, ss.getAlertAfter()); // alertAfter
				ps.setString(15, ss.getAlertLevel()); // alert level
				ps.setInt(16, ss.getMessageCount()); // message count
				ps.setTimestamp(17, new Timestamp(System.currentTimeMillis()));
				ps.setString(18, Constants.DEFAULT_USER_ID);
				ps.execute();
			}

			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void insertTestSmtpData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(insertSql);
			
			for (SmtpServerEnum ss : SmtpServerEnum.values()) {
				if (ss.isTestOnly()) {
					ps.setString(1, ss.getSmtpHost()); // smtpHost
					ps.setInt(2, ss.getSmtpPort()); // smtpPort
					ps.setString(3, ss.getServerName()); // server name
					ps.setString(4, ss.getDescription()); // description
					ps.setString(5, ss.isUseSsl() ? Constants.YES : Constants.NO); // use ssl
					ps.setString(6, ss.getUserId()); // user id
					ps.setString(7, ss.getUserPswd()); // user password
					ps.setString(8, ss.isPersistence() ? Constants.YES : Constants.NO); // persistence
					ps.setString(9, ss.getStatus().value()); // status id
					ps.setString(10, ss.getServerType().value()); // server type
					ps.setInt(11, ss.getNumberOfThreads()); // Threads
					ps.setInt(12, ss.getMaximumRetries()); // retries
					ps.setInt(13, ss.getRetryFreq()); // retry freq
					ps.setInt(14, ss.getAlertAfter()); // alertAfter
					ps.setString(15, ss.getAlertLevel()); // alert level
					ps.setInt(16, ss.getMessageCount()); // message count
					ps.setTimestamp(17, new Timestamp(System.currentTimeMillis()));
					ps.setString(18, Constants.DEFAULT_USER_ID);
					ps.execute();
				}
			}


			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void UpdateSmtpData4Prod() throws SQLException {
		String sql = "update smtp_server set StatusId = ? where ServerName = ?";
		try {
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setString(1, StatusId.INACTIVE.value()); // status id
			ps.setString(2, SmtpServerEnum.SUPPORT.getServerName()); // server name
			ps.execute();
			
			ps.setString(1, StatusId.ACTIVE.value()); // status id
			ps.setString(2, SmtpServerEnum.DynMailRelay.getServerName()); // server name
			ps.execute();
			
			ps.setString(1, StatusId.INACTIVE.value()); // status id
			ps.setString(2, SmtpServerEnum.EXCHANGE.getServerName()); // server name
			ps.execute();
			
			ps.close();
			System.out.println("SmtpTable: Smtp records updated.");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertMailSenderData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO mail_sender_props " +
				"(InternalLoopback," +
				"ExternalLoopback," +
				"UseTestAddr," +
				"TestFromAddr, " +
				"TestToAddr," +
				"TestReplytoAddr," +
				"IsVerpEnabled," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

			ps.setString(1, "testto@localhost");
			ps.setString(2, "inbox@lagacytojava.com");
			ps.setString(3, Constants.YES);
			ps.setString(4, "testfrom@localhost");
			ps.setString(5, "testto@localhost");
			ps.setString(6, "testreplyto@localhost");
			ps.setString(7, Constants.NO);
			ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
			ps.setString(9, "SysAdmin");
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			SmtpTable ct = new SmtpTable();
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
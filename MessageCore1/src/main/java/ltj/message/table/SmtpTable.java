package ltj.message.table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.data.preload.SmtpServerEnum;
import ltj.message.constant.Constants;
import ltj.message.constant.MailServerType;
import ltj.message.constant.StatusId;
import ltj.message.dao.smtp.MailSenderPropsDao;
import ltj.message.dao.smtp.SmtpServerDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.MailSenderVo;
import ltj.message.vo.SmtpConnVo;
import ltj.spring.util.SpringUtil;

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
			"row_id int AUTO_INCREMENT not null, " +
			"server_name varchar(50) NOT NULL, " +
			"smtp_host varchar(100) NOT NULL, " +
			"smtp_port integer NOT NULL, " +
			"description varchar(100), " +
			"use_ssl varchar(3) NOT NULL, " +
			"use_auth varchar(3), " +
			"user_id varchar(30) NOT NULL, " + 
			"user_pswd varchar(30) NOT NULL, " +
			"persistence varchar(3) NOT NULL, " +
			"status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " +
			"server_type varchar(5) DEFAULT '" + MailServerType.SMTP.value() + "', " +
			"threads integer NOT NULL, " +
			"retries integer NOT NULL, " +
			"retry_freq integer NOT NULL, " +
			"alert_after integer, " +
			"alert_level varchar(5), " +
			"message_count integer NOT NULL, " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id char(10) NOT NULL, " +
			"CONSTRAINT smtp_server_pkey PRIMARY KEY (row_id), " +
			"UNIQUE INDEX smtp_server_ix_server_name (server_name) " +
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
			"row_id int AUTO_INCREMENT not null, " +
			"internal_loopback varchar(100) NOT NULL, " +
			"external_loopback varchar(100) NOT NULL, " +
			"use_test_addr varchar(3) NOT NULL, " +
			"test_from_addr varchar(255), " +
			"test_to_addr varchar(255) NOT NULL, " +
			"test_replyto_addr varchar(255), " + 
			"is_verp_enabled varchar(3) NOT NULL, " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id char(10) NOT NULL, " +
			"CONSTRAINT mail_sender_props_pkey PRIMARY KEY (row_id) " +
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
	
	void insertReleaseSmtpData() throws SQLException {
		SmtpServerDao dao = SpringUtil.getDaoAppContext().getBean(SmtpServerDao.class);
		
		int rows = 0;
		for (SmtpServerEnum ss : SmtpServerEnum.values()) {
			if (ss.isTestOnly()) {
				continue;
			}
			try {
				SmtpConnVo vo = new SmtpConnVo();
				vo.setSmtpHost(ss.getSmtpHost());
				vo.setSmtpPort(ss.getSmtpPort());
				vo.setServerName(ss.getServerName());
				vo.setDescription(ss.getDescription());
				vo.setUseSsl(ss.isUseSsl() ? Constants.YES : Constants.NO); // use ssl
				vo.setUserId(ss.getUserId());
				vo.setUserPswd(ss.getUserPswd());
				vo.setPersistence(ss.isPersistence() ? Constants.YES : Constants.NO); // persistence
				vo.setStatusId(ss.getStatus().value()); // status id
				vo.setServerType(ss.getServerType().value()); // server type
				vo.setThreads(ss.getNumberOfThreads()); // Threads
				vo.setRetries(ss.getMaximumRetries()); // retries
				vo.setRetryFreq(ss.getRetryFreq()); // retry frequency
				vo.setAlertAfter(ss.getAlertAfter());
				vo.setAlertLevel(ss.getAlertLevel());
				vo.setMessageCount(ss.getMessageCount());
				vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
				vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
				
				rows += dao.insert(vo);
				
			} catch (Exception e) {
				System.err.println("SQL Error: " + e.getMessage());
				throw e;
			}
		}
		System.out.println("Number of rows inserted to smtp_server: " + rows);
	}

	void insertTestSmtpData() throws SQLException {
		SmtpServerDao dao = SpringUtil.getDaoAppContext().getBean(SmtpServerDao.class);
		
		int rows = 0;
		for (SmtpServerEnum ss : SmtpServerEnum.values()) {
			if (ss.isTestOnly()) {
				try {
					SmtpConnVo vo = new SmtpConnVo();
					vo.setSmtpHost(ss.getSmtpHost());
					vo.setSmtpPort(ss.getSmtpPort());
					vo.setServerName(ss.getServerName());
					vo.setDescription(ss.getDescription());
					vo.setUseSsl(ss.isUseSsl() ? Constants.YES : Constants.NO); // use ssl
					vo.setUserId(ss.getUserId());
					vo.setUserPswd(ss.getUserPswd());
					vo.setPersistence(ss.isPersistence() ? Constants.YES : Constants.NO); // persistence
					vo.setStatusId(ss.getStatus().value()); // status id
					vo.setServerType(ss.getServerType().value()); // server type
					vo.setThreads(ss.getNumberOfThreads()); // Threads
					vo.setRetries(ss.getMaximumRetries()); // retries
					vo.setRetryFreq(ss.getRetryFreq()); // retry frequency
					vo.setAlertAfter(ss.getAlertAfter());
					vo.setAlertLevel(ss.getAlertLevel());
					vo.setMessageCount(ss.getMessageCount());
					vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
					vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
					
					rows += dao.insert(vo);
					
				} catch (Exception e) {
					System.err.println("SQL Error: " + e.getMessage());
					throw e;
				}
			}
		}
		System.out.println("Number of rows inserted to smtp_server: " + rows);

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
		MailSenderPropsDao dao = SpringUtil.getDaoAppContext().getBean(MailSenderPropsDao.class);
		
		try {
			MailSenderVo vo = new MailSenderVo();
			vo.setInternalLoopback("testto@localhost");
			vo.setExternalLoopback("inbox@lagacytojava.com");
			vo.setUseTestAddr(Constants.YES);
			vo.setTestFromAddr("testfrom@localhost");
			vo.setTestToAddr("testto@localhost");
			vo.setTestReplytoAddr("testreplyto@localhost");
			vo.setIsVerpEnabled(Constants.NO);
			vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			vo.setUpdtUserId("SysAdmin");
			
			int rows = dao.insert(vo);
			System.out.println("Number of rows inserted to mail_sender_props: " + rows);
		} catch (Exception e) {
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
package ltj.message.table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ltj.message.constant.CarrierCode;
import ltj.data.preload.MailInboxEnum;
import ltj.message.constant.Constants;
import ltj.message.constant.MailProtocol;
import ltj.message.constant.MailServerType;
import ltj.message.constant.StatusId;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.mailbox.MailBoxDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.MailBoxVo;
import ltj.spring.util.SpringUtil;

public class MailboxTable extends CreateTableBase {
	/**
	 * Creates a new instance of MailTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public MailboxTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE mail_box");
			System.out.println("Dropped mail_box Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException {
		/*
		 Required properties for mailbox
			- UserId: mailbox user name
			- UserPswd: mailbox password
		 	- HostName: host domain name or IP address
		 	- PortNumber: port number, default to -1
			- FolderName: folder name, default to INBOX
		 	- Protocol: pop3/imap
		 	- CarrierCode: I - Internet mail, W - web-mail, U - keeping mail in in-box
		 	- InternalOnly: Y - internal mail, N - otherwise
		 	- MailBoxDesc: mailbox description
		 	- StatusId: A - Active, I - Inactive
			- ReadPerPass: maximum number of messages read per processing cycle
		 	- UseSsl: yes/no, set port number to 995 if yes
			- Threads:	number of threads per mailbox, default to 1 (experimental).
		 	- RetryMax: maximum number of retries for connection, default to 5
			- MinimumWait: time to wait between cycles, default to 2 second
							for exchange server, it must be 10 or greater
			- MessageCount: for test only, number of messages to be processed. 0=unlimited.
		 	- ToPlainText: yes/no, convert message body from HTML to plain text, default to no
			- ToAddrDomain: Derived from Client_Data table. If present, scan "received" address
			 				chain until a match is found. The email address with the matching 
			 				domain becomes the real TO address.
			- CheckDuplicate: yes/no, check for duplicates, only one is processed
			- AlertDuplicate: yes/no, send out alert if found duplicate
			- LogDuplicate: yes/no, log duplicate messages
			- PurgeDupsAfter: purge duplicate messages after certain hours
			- ProcessorName: Spring processor id / processor class name
		*/
		try {
			stm.execute("CREATE TABLE mail_box ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"UserId varchar(30) NOT NULL, " + 
			"UserPswd varchar(32) NOT NULL, " +
			"HostName varchar(100) NOT NULL, " +
			"PortNumber integer NOT NULL, " +
			"Protocol char(4) NOT NULL, " +
			"ServerType varchar(5) DEFAULT '" + MailServerType.SMTP.value() + "', " +
			"FolderName varchar(30), " +
			"MailBoxDesc varchar(50), " +
			"StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " +
			"CarrierCode char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.value() + "', " +
			"InternalOnly varchar(3), " +
			"ReadPerPass integer NOT NULL, " +
			"UseSsl varchar(3) NOT NULL, " +
			"Threads integer NOT NULL, " +
			"RetryMax integer, " +
			"MinimumWait integer, " +
			"MessageCount integer NOT NULL, " +
			"ToPlainText varchar(3), " +
			"ToAddrDomain varchar(500), " +
			"CheckDuplicate varchar(3), " +
			"AlertDuplicate varchar(3), " +
			"LogDuplicate varchar(3), " +
			"PurgeDupsAfter integer, " +
			"ProcessorName varchar(100) NOT NULL, " +
			"UpdtTime datetime(3) NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (UserId, HostName) " +
			") ENGINE=InnoDB");
			System.out.println("Created mail_box Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	private String insertSql = 
		"INSERT INTO mail_box " +
			"(UserId," +
			"UserPswd," +
			"HostName," +
			"PortNumber," +
			"Protocol," +
			"FolderName," +
			"MailBoxDesc," +
			"StatusId," +
			"CarrierCode," +
			"InternalOnly," +
			"ReadPerPass," +
			"UseSsl," +
			"Threads," +
			"RetryMax," +
			"MinimumWait," +
			"MessageCount," +
			"ToPlainText," +
			"ToAddrDomain," +
			"CheckDuplicate," +
			"AlertDuplicate," +
			"LogDuplicate," +
			"PurgeDupsAfter," +
			"ProcessorName," +
			"UpdtTime," +
			"UpdtUserId) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				", ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				", ?, ?, ?, ?, ?)";

	public void loadTestData() throws SQLException {
		try {
			insertReleaseMailbox();
			insertTestMailboxes();
			System.out.println("Inserted all rows...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadReleaseData() throws SQLException {
		try {
			insertReleaseMailbox();
			System.out.println("Inserted all rows...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void insertReleaseMailbox() throws SQLException {
		PreparedStatement ps = con.prepareStatement(insertSql);
		
		for (MailInboxEnum in : MailInboxEnum.values()) {
			if (StringUtils.isAllUpperCase(in.name())) {
				ps.setString(1, in.getUserId());
				ps.setString(2, in.getUserPswd());
				ps.setString(3, in.getHostName());
				ps.setInt(4, in.getPort());
				ps.setString(5, in.getProtocol().value());
				ps.setString(6, "INBOX");
				ps.setString(7, in.getDescription());
				ps.setString(8, in.getStatus().value());
				ps.setString(9, CarrierCode.SMTPMAIL.value());
				ps.setString(10, in.getIsInternalOnly() == null ? null : in.getIsInternalOnly() ? Constants.YES : Constants.NO);
				ps.setInt(11, in.getReadPerPass()); // ReadPerPass
				ps.setString(12, in.isUseSsl() ? Constants.YES : Constants.NO);
				ps.setInt(13, in.getNumberOfThreads()); // Threads
				ps.setInt(14, in.getMaximumRetries() == null ? 5 : in.getMaximumRetries()); // RetryMax
				ps.setInt(15, in.getMinimumWait() == null ? 10 : in.getMinimumWait()); // MinimumWait
				ps.setInt(16, in.getMessageCount());
				ps.setString(17, in.getIsToPlainText() == null ? null : in.getIsToPlainText() ? Constants.YES : Constants.NO);
				ps.setString(18, in.getToAddressDomain());
				ps.setString(19, in.getIsCheckDuplicate() == null ? null : in.getIsCheckDuplicate() ? Constants.YES : Constants.NO);
				ps.setString(20, in.getIsAlertDuplicate() == null ? null : in.getIsAlertDuplicate() ? Constants.YES : Constants.NO);
				ps.setString(21, in.getIsLogDuplicate() == null ? null : in.getIsLogDuplicate() ? Constants.YES : Constants.NO);
				ps.setInt(22, in.getPurgeDupsAfter());
				ps.setString(23, "mailProcessor");
				ps.setTimestamp(24, new Timestamp(System.currentTimeMillis()));
				ps.setString(25, Constants.DEFAULT_USER_ID);
				ps.execute();
			}
		}
		ps.close();
		
		int rows = 0;
		for (MailInboxEnum in : MailInboxEnum.values()) {
			if (StringUtils.isAllUpperCase(in.name())) {
				MailBoxVo vo = new MailBoxVo();
				vo.setUserId(in.getUserId());
				vo.setUserPswd(in.getUserPswd());
				vo.setHostName(in.getHostName());
				vo.setPortNumber(in.getPort());
				vo.setProtocol(in.getProtocol().value());
				vo.setFolderName("INBOX");
				vo.setDescription(in.getDescription());
				vo.setStatusId(in.getStatus().value());
				vo.setCarrierCode(CarrierCode.SMTPMAIL.value());
				vo.setInternalOnly(in.getIsInternalOnly() == null ? null : in.getIsInternalOnly() ? Constants.YES : Constants.NO);
				vo.setReadPerPass(in.getReadPerPass());
				vo.setUseSsl(in.isUseSsl() ? Constants.YES : Constants.NO);
				vo.setThreads(in.getNumberOfThreads());
				vo.setRetryMax(in.getMaximumRetries() == null ? 5 : in.getMaximumRetries()); // RetryMax
				vo.setMinimumWait(in.getMinimumWait() == null ? 10 : in.getMinimumWait()); // MinimumWait
				vo.setMessageCount(in.getMessageCount());
				vo.setToPlainText(in.getIsToPlainText() == null ? null : in.getIsToPlainText() ? Constants.YES : Constants.NO);
				vo.setToAddrDomain(in.getToAddressDomain());
				vo.setCheckDuplicate(in.getIsCheckDuplicate() == null ? null : in.getIsCheckDuplicate() ? Constants.YES : Constants.NO);
				vo.setAlertDuplicate(in.getIsAlertDuplicate() == null ? null : in.getIsAlertDuplicate() ? Constants.YES : Constants.NO);
				vo.setLogDuplicate(in.getIsLogDuplicate() == null ? null : in.getIsLogDuplicate() ? Constants.YES : Constants.NO);
				vo.setPurgeDupsAfter(in.getPurgeDupsAfter());
				vo.setProcessorName("mailProcessor");
				vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
				vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
				
				rows += getMailBoxDao().insert(vo);
			}
		}
		System.out.println("Number of rows inserted to mail_box: " + rows);
	}

	void insertTestMailboxes() throws SQLException {
		PreparedStatement ps = con.prepareStatement(insertSql);

		for (MailInboxEnum in : MailInboxEnum.values()) {
			if (StringUtils.isAllLowerCase(in.name())) {
				ps.setString(1, in.getUserId());
				ps.setString(2, in.getUserPswd());
				ps.setString(3, in.getHostName());
				ps.setInt(4, in.getPort());
				ps.setString(5, in.getProtocol().value());
				ps.setString(6, "INBOX");
				ps.setString(7, in.getDescription());
				ps.setString(8, in.getStatus().value());
				ps.setString(9, CarrierCode.SMTPMAIL.value());
				ps.setString(10, in.getIsInternalOnly() == null ? null : in.getIsInternalOnly() ? Constants.YES : Constants.NO);
				ps.setInt(11, in.getReadPerPass()); // ReadPerPass
				ps.setString(12, in.isUseSsl() ? Constants.YES : Constants.NO);
				ps.setInt(13, in.getNumberOfThreads()); // Threads
				ps.setInt(14, in.getMaximumRetries() == null ? 5 : in.getMaximumRetries()); // RetryMax
				ps.setInt(15, in.getMinimumWait() == null ? 10 : in.getMinimumWait()); // MinimumWait
				ps.setInt(16, in.getMessageCount());
				ps.setString(17, in.getIsToPlainText() == null ? null : in.getIsToPlainText() ? Constants.YES : Constants.NO);
				ps.setString(18, in.getToAddressDomain());
				ps.setString(19, in.getIsCheckDuplicate() == null ? null : in.getIsCheckDuplicate() ? Constants.YES : Constants.NO);
				ps.setString(20, in.getIsAlertDuplicate() == null ? null : in.getIsAlertDuplicate() ? Constants.YES : Constants.NO);
				ps.setString(21, in.getIsLogDuplicate() == null ? null : in.getIsLogDuplicate() ? Constants.YES : Constants.NO);
				ps.setInt(22, in.getPurgeDupsAfter());
				ps.setString(23, "mailProcessor");
				ps.setTimestamp(24, new Timestamp(System.currentTimeMillis()));
				ps.setString(25, Constants.DEFAULT_USER_ID);
				ps.execute();
			}
		}

		ps.close();
	}

	void insertTestUsers() {
		MailBoxVo vo = new MailBoxVo();
		vo.setHostName("localhost");
		vo.setPortNumber(-1);
		vo.setProtocol(MailProtocol.POP3.value());
		vo.setFolderName("INBOX");
		vo.setMailBoxDesc("Test User");
		vo.setStatusId(StatusId.ACTIVE.value());
		vo.setCarrierCode(CarrierCode.SMTPMAIL.value());
		vo.setInternalOnly(Constants.NO);
		vo.setReadPerPass(5);
		vo.setUseSsl(Constants.NO);
		vo.setThreads(2);
		vo.setRetryMax(5);
		vo.setMinimumWait(10);
		vo.setMessageCount(-1);
		vo.setToPlainText(Constants.NO);
		vo.setToAddrDomain("localhost");
		vo.setCheckDuplicate(Constants.YES);
		vo.setAlertDuplicate(Constants.YES);
		vo.setLogDuplicate(Constants.YES);
		vo.setProcessorName("mailProcessor");
		vo.setPurgeDupsAfter(Integer.valueOf(6));
		vo.setUpdtUserId("SysAdmin");
		int rowsInserted = 0;
		for (int i = 0; i < 100; i++) {
			String user = "user" + StringUtils.leftPad(i + "", 2, "0");
			vo.setUserId(user);
			vo.setUserPswd(user);
			rowsInserted += getMailBoxDao().insert(vo);
		}
		System.out.println("Users inserted: " + rowsInserted);
	}
	
	void deleteTestUsers() {
		int rowsDeleted = 0;
		for (int i = 0; i < 100; i++) {
			String user = "user" + StringUtils.leftPad(i + "", 2, "0");
			rowsDeleted += getMailBoxDao().deleteByPrimaryKey(user, "localhost");
		}
		System.out.println("Users deleted: " + rowsDeleted);
	}
	
	public void updateEmailAddrTable() {
		List<MailBoxVo> mailBoxes = getMailBoxDao().getAll(false);
		int count = 0;
		for (MailBoxVo mailbox : mailBoxes) {
			getEmailAddrdao().findByAddress(mailbox.getUserId() + "@" + mailbox.getHostName());
			count ++;
		}
		System.out.println("Inserted/Upadted EmailAddr records: " + count);
	}
	
	private MailBoxDao mailBoxDao = null;
	private MailBoxDao getMailBoxDao() {
		if (mailBoxDao == null) {
			mailBoxDao = SpringUtil.getDaoAppContext().getBean(MailBoxDao.class);
		}
		return mailBoxDao;
	}
	
	private EmailAddressDao emailAddressDao = null;
	private EmailAddressDao getEmailAddrdao() {
		if (emailAddressDao == null) {
			emailAddressDao = SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddressDao;
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			MailboxTable ct = new MailboxTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
			//ct.deleteTestUsers();
			//ct.insertTestUsers();
			ct.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package ltj.message.table;

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
import ltj.message.dao.servers.MailBoxDao;
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
			"row_id int AUTO_INCREMENT not null, " +
			"user_id varchar(30) NOT NULL, " + 
			"user_pswd varchar(32) NOT NULL, " +
			"host_name varchar(100) NOT NULL, " +
			"port_number integer NOT NULL, " +
			"protocol char(4) NOT NULL, " +
			"server_type varchar(5) DEFAULT '" + MailServerType.SMTP.value() + "', " +
			"folder_name varchar(30), " +
			"mail_box_desc varchar(50), " +
			"status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " +
			"carrier_code char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.value() + "', " +
			"internal_only boolean, " +
			"read_per_pass integer NOT NULL, " +
			"use_ssl boolean NOT NULL DEFAULT false, " +
			"threads integer NOT NULL, " +
			"retry_max integer, " +
			"minimum_wait integer, " +
			"message_count integer NOT NULL, " +
			"to_plain_text boolean, " +
			"to_addr_domain varchar(500), " +
			"check_duplicate boolean, " +
			"alert_duplicate boolean, " +
			"log_duplicate boolean, " +
			"purge_dups_after integer, " +
			"processor_name varchar(100) NOT NULL, " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id char(10) NOT NULL, " +
			"CONSTRAINT mail_box_pkey PRIMARY KEY (row_id), " +
			"UNIQUE INDEX mail_box_ix_usrid_hstnm (user_id, host_name) " +
			") ENGINE=InnoDB");
			System.out.println("Created mail_box Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
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
				vo.setInternalOnly(in.getIsInternalOnly());
				vo.setReadPerPass(in.getReadPerPass()); // ReadPerPass
				vo.setUseSsl(in.isUseSsl());
				vo.setThreads(in.getNumberOfThreads()); // Threads
				vo.setRetryMax(in.getMaximumRetries() == null ? 5 : in.getMaximumRetries()); // RetryMax
				vo.setMinimumWait(in.getMinimumWait() == null ? 10 : in.getMinimumWait()); // MinimumWait
				vo.setMessageCount(in.getMessageCount());
				vo.setToPlainText(in.getIsToPlainText());
				vo.setToAddrDomain(in.getToAddressDomain());
				vo.setCheckDuplicate(in.getIsCheckDuplicate());
				vo.setAlertDuplicate(in.getIsAlertDuplicate());
				vo.setLogDuplicate(in.getIsLogDuplicate());
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
		int rows = 0;
		for (MailInboxEnum in : MailInboxEnum.values()) {
			if (StringUtils.isAllLowerCase(in.name())) {
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
				vo.setInternalOnly(in.getIsInternalOnly());
				vo.setReadPerPass(in.getReadPerPass()); // ReadPerPass
				vo.setUseSsl(in.isUseSsl());
				vo.setThreads(in.getNumberOfThreads()); // Threads
				vo.setRetryMax(in.getMaximumRetries() == null ? 5 : in.getMaximumRetries()); // RetryMax
				vo.setMinimumWait(in.getMinimumWait() == null ? 10 : in.getMinimumWait()); // MinimumWait
				vo.setMessageCount(in.getMessageCount());
				vo.setToPlainText(in.getIsToPlainText());
				vo.setToAddrDomain(in.getToAddressDomain());
				vo.setCheckDuplicate(in.getIsCheckDuplicate());
				vo.setAlertDuplicate(in.getIsAlertDuplicate());
				vo.setLogDuplicate(in.getIsLogDuplicate());
				vo.setPurgeDupsAfter(in.getPurgeDupsAfter());
				vo.setProcessorName("mailProcessor");
				vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
				vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
				
				rows += getMailBoxDao().insert(vo);
			}
		}
		System.out.println("Number of rows inserted to mail_box: " + rows);
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
		vo.setInternalOnly(Boolean.FALSE);
		vo.setReadPerPass(5);
		vo.setUseSsl(false);
		vo.setThreads(2);
		vo.setRetryMax(5);
		vo.setMinimumWait(10);
		vo.setMessageCount(-1);
		vo.setToPlainText(Boolean.FALSE);
		vo.setToAddrDomain("localhost");
		vo.setCheckDuplicate(Boolean.TRUE);
		vo.setAlertDuplicate(Boolean.TRUE);
		vo.setLogDuplicate(Boolean.TRUE);
		vo.setProcessorName("mailProcessor");
		vo.setPurgeDupsAfter(Integer.valueOf(6));
		vo.setUpdtUserId("SysAdmin");
		int rows = 0;
		for (int i = 0; i < 100; i++) {
			String user = "user" + StringUtils.leftPad(i + "", 2, "0");
			vo.setUserId(user);
			vo.setUserPswd(user);
			rows += getMailBoxDao().insert(vo);
		}
		System.out.println("Number of test users inserted to mail_box: " + rows);
	}
	
	void deleteTestUsers() {
		int rows = 0;
		for (int i = 0; i < 100; i++) {
			String user = "user" + StringUtils.leftPad(i + "", 2, "0");
			rows += getMailBoxDao().deleteByPrimaryKey(user, "localhost");
		}
		System.out.println("Number of test users deleted: " + rows);
	}
	
	public void updateEmailAddrTable() {
		List<MailBoxVo> mailBoxes = getMailBoxDao().getAll(false);
		int count = 0;
		for (MailBoxVo mailbox : mailBoxes) {
			getEmailAddressdao().findByAddress(mailbox.getUserId() + "@" + mailbox.getHostName());
			count ++;
		}
		System.out.println("Inserted/Upadted email_address records: " + count);
	}
	
	private MailBoxDao mailBoxDao = null;
	private MailBoxDao getMailBoxDao() {
		if (mailBoxDao == null) {
			mailBoxDao = SpringUtil.getDaoAppContext().getBean(MailBoxDao.class);
		}
		return mailBoxDao;
	}
	
	private EmailAddressDao emailAddressDao = null;
	private EmailAddressDao getEmailAddressdao() {
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
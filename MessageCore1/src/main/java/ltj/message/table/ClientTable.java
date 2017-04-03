package ltj.message.table;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import ltj.jbatch.common.ProductKey;
import ltj.jbatch.obsolete.ProductUtil;
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.client.ClientDao;
import ltj.message.main.CreateTableBase;
import ltj.message.util.TimestampUtil;
import ltj.message.vo.ClientVo;
import ltj.spring.util.SpringUtil;

public class ClientTable extends CreateTableBase {
	/**
	 * Creates a new instance of ClientTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public ClientTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE reload_flags");
			System.out.println("Dropped reload_flags Table...");
		}
		catch (Exception e) {
		}		
		try {
			stm.execute("DROP TABLE client_tbl");
			System.out.println("Dropped client_tbl Table...");
		}
		catch (Exception e) {
		}		
	}
	
	public void createTables() throws SQLException {
		createClientTable();
		createReloadFlagsTable();
	}
	
	void createClientTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE client_tbl ( "
					+ "row_id int AUTO_INCREMENT not null, "
					+ "client_id varchar(16) NOT NULL, "
					+ "client_name varchar(40) NOT NULL, "
					+ "client_type char(1), " // TBD
					+ "domain_name varchar(100) NOT NULL, " 
						// used by VERP and System E-Mails to set Return-Path
					+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " // 'A' or 'I'
					+ "irs_tax_id varchar(10), " // IRS Tax Id
					+ "web_site_url varchar(100), "
					+ "save_raw_msg boolean NOT NULL DEFAULT true, " 
						// save SMTP message stream to msg_stream? used by RuleEngine
					+ "contact_name varchar(60), "
					+ "contact_phone varchar(18), "
					+ "contact_email varchar(255) NOT NULL, "
					// for rule engine, email addresses used by ACTIONS
					+ "security_email varchar(255) NOT NULL, "
					+ "custcare_email varchar(255) NOT NULL, "
					+ "rma_dept_email varchar(255) NOT NULL, "
					+ "spam_cntrl_email varchar(255) NOT NULL, "
					+ "cha_rsp_hndlr_email varchar(255) NOT NULL, "
					// for mail sender, embed EmailId to the bottom of email
					+ "embed_email_id boolean NOT NULL DEFAULT true, "
					+ "return_path_left varchar(50) NOT NULL, "
					// for mail sender, define testing addresses
					+ "use_test_addr boolean NOT NULL DEFAULT false, "
					+ "test_from_addr varchar(255), "
					+ "test_to_addr varchar(255), "
					+ "test_replyto_addr varchar(255), "
					// for mail sender, use VERP in Return-Path?
					+ "is_verp_enabled boolean NOT NULL DEFAULT false, "
					+ "verp_sub_domain varchar(50), " // sub domain used by VERP bounces
					/*
					 * we do not need to define a separate VERP domain. If the
					 * domain in Return-Path is different from the one in FROM
					 * address, it could trigger SPAM filter.
					 */
					+ "verp_inbox_name varchar(50), " // mailbox name for VERP bounces
					+ "verp_remove_inbox varchar(50), " // mailbox name for VERP un-subscribe
					// store time stamp when the table is initially loaded
					+ "system_id varchar(40) NOT NULL DEFAULT ' ', "
					+ "system_key varchar(30), "
					// Begin -> not implemented yet
					+ "dikm char(1), " // DIKM support - S:Send/R:Receive/B:Both/N:None
					+ "domain_key char(1), " // DomainKey support
					+ "key_file_path varchar(200), " // Private Key file location
					+ "spf char(1), " // SPF check Y/N
					// <- End
					+ "updt_time datetime(3) NOT NULL, "
					+ "updt_user_id char(10) NOT NULL, "
					+ "CONSTRAINT client_pkey PRIMARY KEY (row_id), "
					//+ "UNIQUE INDEX client_ix_domain_name (DomainName), "
					+ "UNIQUE INDEX client_ix_client_id (client_id) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created client_tbl Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createReloadFlagsTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE reload_flags ( "
					+ "row_id int AUTO_INCREMENT not null, "
					+ "clients int NOT NULL, "
					+ "rules int NOT NULL, "
					+ "actions int NOT NULL, "
					+ "templates int NOT NULL, "
					+ "schedules int NOT NULL, "
					+ "CONSTRAINT reload_flags_pkey PRIMARY KEY (row_id) "
					+ ") ENGINE=MyISAM");
			System.out.println("Created reload_flags Table...");
			stm.execute("INSERT INTO reload_flags VALUES(1,0,0,0,0,0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadTestData() throws SQLException {
		try {
			insertSystemDefault(true);
			insertJBatch();
			System.out.println("Inserted all rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadReleaseData() throws SQLException {
		try {
			insertSystemDefault(false);
			System.out.println("Inserted all rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void insertSystemDefault(boolean loadTestData) throws SQLException {
		ClientDao dao = SpringUtil.getDaoAppContext().getBean(ClientDao.class);
		
		ClientVo vo = new ClientVo();
		vo.setClientId(Constants.DEFAULT_CLIENTID);
		vo.setClientName("Emailsphere Demo");
		vo.setClientType(null);
		if (loadTestData) {
			vo.setDomainName("localhost");
		}
		else {
			vo.setDomainName("espheredemo.com");
		}
		vo.setStatusId(StatusId.ACTIVE.value());
		vo.setIrsTaxId("0000000000");
		vo.setWebSiteUrl("http://localhost:8080/MsgUI/publicsite");
		vo.setSaveRawMsg(true); // save raw stream
		if (loadTestData) {
			vo.setContactEmail("sitemaster@emailsphere.com");
			vo.setSecurityEmail("security@localhost");
			vo.setCustcareEmail("custcare@localhost");
			vo.setRmaDeptEmail("rma_dept@localhost");
			vo.setSpamCntrlEmail("spam_ctrl@localhost");
			vo.setChaRspHndlrEmail("challenge@localhost");
		}
		else { // release data
			vo.setContactEmail("sitemaster@localhost");
			vo.setSecurityEmail("security@localhost");
			vo.setCustcareEmail("custcare@localhost");
			vo.setRmaDeptEmail("rma_dept@localhost");
			vo.setSpamCntrlEmail("spam_ctrl@localhost");
			vo.setChaRspHndlrEmail("challenge@localhost");
		}
		vo.setEmbedEmailId(true); // Embed EmailId
		vo.setReturnPathLeft("support"); // return-path left
		vo.setUseTestAddr(true); // use testing address
		vo.setTestFromAddr("testfrom@localhost");
		vo.setTestToAddr("testto@localhost");
		vo.setTestReplytoAddr(null);
		vo.setIsVerpEnabled(true); // is VERP enabled
		vo.setVerpSubDomain(null); // VERP sub domain
		vo.setVerpInboxName("bounce"); // VERP bounce mailbox
		vo.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		Calendar cal = Calendar.getInstance();
		//cal.add(Calendar.DAY_OF_YEAR, -31);
		String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp(cal.getTime()));
		vo.setSystemId(systemId);
		vo.setSystemKey(ProductUtil.getProductKeyFromFile());
		vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		int rows = dao.insert(vo);
		System.out.println("client_tbl: default client inserted: " + rows);
	}

	void insertJBatch() throws SQLException {
		ClientDao dao = SpringUtil.getDaoAppContext().getBean(ClientDao.class);
		
		ClientVo vo = new ClientVo();
		vo.setClientId("JBatchCorp");
		vo.setClientName("JBatch Corp. Site");
		vo.setClientType(null);
		vo.setDomainName("jbatch.com"); // domain name
		vo.setStatusId(StatusId.ACTIVE.value());
		vo.setIrsTaxId("0000000000");
		vo.setWebSiteUrl("http://www.jbatch.com");
		vo.setSaveRawMsg(true); // save raw stream
		vo.setContactEmail("sitemaster@jbatch.com");
		vo.setSecurityEmail("security@jbatch.com");
		vo.setCustcareEmail("custcare@jbatch.com");
		vo.setRmaDeptEmail("rma_dept@jbatch.com");
		vo.setSpamCntrlEmail("spam_ctrl@jbatch.com");
		vo.setChaRspHndlrEmail("challenge@jbatch.com");
		vo.setEmbedEmailId(true); // Embed EmailId
		vo.setReturnPathLeft("support"); // return-path left
		vo.setUseTestAddr(false); // use testing address
		vo.setTestFromAddr("testfrom@jbatch.com");
		vo.setTestToAddr("testto@jbatch.com");
		vo.setTestReplytoAddr(null);
		vo.setIsVerpEnabled(false); // is VERP enabled
		vo.setVerpSubDomain(null); // VERP sub domain
		vo.setVerpInboxName("bounce"); // VERP bounce mailbox
		vo.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		vo.setSystemId("");
		vo.setSystemKey(null);
		vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		int rows = dao.insert(vo);
		System.out.println("client_tbl: jbatch.com inserted: " + rows);
	}

	public int updateClient4Prod() {
		ClientDao dao = SpringUtil.getDaoAppContext().getBean(ClientDao.class);
		ClientVo vo = dao.getByClientId(Constants.DEFAULT_CLIENTID);
		vo.setClientName("Emailsphere");
		vo.setClientType(null);
		vo.setDomainName("emailsphere.com"); // domain name
		vo.setStatusId(StatusId.ACTIVE.value());
		vo.setIrsTaxId("0000000000");
		vo.setWebSiteUrl("http://www.emailsphere.com/newsletter");
		vo.setSaveRawMsg(true); // save raw stream
		vo.setContactEmail("sitemaster@emailsphere.com");
		vo.setSecurityEmail("security@emailsphere.com");
		vo.setCustcareEmail("custcare@emailsphere.com");
		vo.setRmaDeptEmail("rma_dept@emailsphere.com");
		vo.setSpamCntrlEmail("spam_.ctrl@emailsphere.com");
		vo.setChaRspHndlrEmail("challenge@emailsphere.com");
		vo.setEmbedEmailId(true); // Embed EmailId 
		vo.setUseTestAddr(false); // use testing address
		vo.setTestFromAddr("testfrom@emailsphere.com");
		vo.setTestToAddr("testto@emailsphere.com");
		//vo.setTestReplytoAddr(null);
		vo.setIsVerpEnabled(true); // is VERP enabled
		//vo.setVerpSubDomain(null); // VERP sub domain
		vo.setVerpInboxName("bounce"); // VERP bounce mailbox
		vo.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		//String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp());
		//vo.setSystemId(systemId);
		vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		int rowsInserted = dao.update(vo);
		if (ProductKey.validateKey(ProductUtil.getProductKeyFromFile())) {
			dao.updateSystemKey(ProductUtil.getProductKeyFromFile());
		}
		System.out.println("ClientTable: Default client updated.");
		return rowsInserted;
	}
	
	/**
	 * update System client to trigger the loading of Client Variables into
	 * ClientVariable table.
	 */
	public void updateAllClients() {
		ClientDao dao = SpringUtil.getDaoAppContext().getBean(ClientDao.class);
		int rowsUpdated = 0;
		List<ClientVo> list = dao.getAll();
		for (ClientVo vo : list) {
			vo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
			rowsUpdated += dao.update(vo);
		}
		ClientVo vo = dao.getByClientId(Constants.DEFAULT_CLIENTID);
		if (vo == null) { // just in case
			try {
				insertSystemDefault(true);
				rowsUpdated++;
				System.out.println("Default Client inserted");
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Number of Clients updated: " + rowsUpdated);
	}
	
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			ClientTable ct = new ClientTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
			//ct.updateAllClients();
			ct.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
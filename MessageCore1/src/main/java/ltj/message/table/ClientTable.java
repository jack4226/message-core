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
					+ "RowId int AUTO_INCREMENT not null, "
					+ "ClientId varchar(16) NOT NULL, "
					+ "ClientName varchar(40) NOT NULL, "
					+ "ClientType char(1), " // TBD
					+ "DomainName varchar(100) NOT NULL, " 
						// used by VERP and System E-Mails to set Return-Path
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " // 'A' or 'I'
					+ "IrsTaxId varchar(10), " // IRS Tax Id
					+ "WebSiteUrl varchar(100), "
					+ "SaveRawMsg char(1) NOT NULL DEFAULT '" + Constants.Y + "', " 
						// save SMTP message stream to msg_stream? used by RuleEngine
					+ "ContactName varchar(60), "
					+ "ContactPhone varchar(18), "
					+ "ContactEmail varchar(255) NOT NULL, "
					// for rule engine, email addresses used by ACTIONS
					+ "SecurityEmail varchar(255) NOT NULL, "
					+ "CustcareEmail varchar(255) NOT NULL, "
					+ "RmaDeptEmail varchar(255) NOT NULL, "
					+ "SpamCntrlEmail varchar(255) NOT NULL, "
					+ "ChaRspHndlrEmail varchar(255) NOT NULL, "
					// for mail sender, embed EmailId to the bottom of email
					+ "EmbedEmailId varchar(3) NOT NULL DEFAULT '" + Constants.YES + "', "
					+ "ReturnPathLeft varchar(50) NOT NULL, "
					// for mail sender, define testing addresses
					+ "UseTestAddr varchar(3) NOT NULL DEFAULT '" + Constants.NO + "', "
					+ "TestFromAddr varchar(255), "
					+ "TestToAddr varchar(255), "
					+ "TestReplytoAddr varchar(255), "
					// for mail sender, use VERP in Return-Path?
					+ "IsVerpEnabled varchar(3) NOT NULL DEFAULT '" + Constants.NO + "', "
					+ "VerpSubDomain varchar(50), " // sub domain used by VERP bounces
					/*
					 * we do not need to define a separate VERP domain. If the
					 * domain in Return-Path is different from the one in FROM
					 * address, it could trigger SPAM filter.
					 */
					+ "VerpInboxName varchar(50), " // mailbox name for VERP bounces
					+ "VerpRemoveInbox varchar(50), " // mailbox name for VERP un-subscribe
					// store time stamp when the table is initially loaded
					+ "SystemId varchar(40) NOT NULL DEFAULT ' ', "
					+ "SystemKey varchar(30), "
					// Begin -> not implemented yet
					+ "Dikm char(1), " // DIKM support - S:Send/R:Receive/B:Both/N:None
					+ "DomainKey char(1), " // DomainKey support
					+ "KeyFilePath varchar(200), " // Private Key file location
					+ "SPF char(1), " // SPF check Y/N
					// <- End
					+ "UpdtTime datetime(3) NOT NULL, "
					+ "UpdtUserId char(10) NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					//+ "UNIQUE INDEX (DomainName), "
					+ "UNIQUE INDEX (ClientId) "
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
					+ "RowId int AUTO_INCREMENT not null, "
					+ "Clients int NOT NULL, "
					+ "Rules int NOT NULL, "
					+ "Actions int NOT NULL, "
					+ "Templates int NOT NULL, "
					+ "Schedules int NOT NULL, "
					+ "PRIMARY KEY (RowId) "
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
		vo.setSaveRawMsg(Constants.Y); // save raw stream
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
		vo.setEmbedEmailId(Constants.YES); // Embed EmailId
		vo.setReturnPathLeft("support"); // return-path left
		vo.setUseTestAddr(Constants.YES); // use testing address
		vo.setTestFromAddr("testfrom@localhost");
		vo.setTestToAddr("testto@localhost");
		vo.setTestReplytoAddr(null);
		vo.setIsVerpEnabled(Constants.YES); // is VERP enabled
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
		
		int rows = dao.insert(vo, true);
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
		vo.setSaveRawMsg(Constants.Y); // save raw stream
		vo.setContactEmail("sitemaster@jbatch.com");
		vo.setSecurityEmail("security@jbatch.com");
		vo.setCustcareEmail("custcare@jbatch.com");
		vo.setRmaDeptEmail("rma_dept@jbatch.com");
		vo.setSpamCntrlEmail("spam_ctrl@jbatch.com");
		vo.setChaRspHndlrEmail("challenge@jbatch.com");
		vo.setEmbedEmailId(Constants.YES); // Embed EmailId
		vo.setReturnPathLeft("support"); // return-path left
		vo.setUseTestAddr(Constants.NO); // use testing address
		vo.setTestFromAddr("testfrom@jbatch.com");
		vo.setTestToAddr("testto@jbatch.com");
		vo.setTestReplytoAddr(null);
		vo.setIsVerpEnabled(Constants.NO); // is VERP enabled
		vo.setVerpSubDomain(null); // VERP sub domain
		vo.setVerpInboxName("bounce"); // VERP bounce mailbox
		vo.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		vo.setSystemId("");
		vo.setSystemKey(null);
		vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		int rows = dao.insert(vo, true);
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
		vo.setSaveRawMsg(Constants.Y); // save raw stream
		vo.setContactEmail("sitemaster@emailsphere.com");
		vo.setSecurityEmail("security@emailsphere.com");
		vo.setCustcareEmail("custcare@emailsphere.com");
		vo.setRmaDeptEmail("rma_dept@emailsphere.com");
		vo.setSpamCntrlEmail("spam_.ctrl@emailsphere.com");
		vo.setChaRspHndlrEmail("challenge@emailsphere.com");
		vo.setEmbedEmailId(Constants.YES); // Embed EmailId 
		vo.setUseTestAddr(Constants.NO); // use testing address
		vo.setTestFromAddr("testfrom@emailsphere.com");
		vo.setTestToAddr("testto@emailsphere.com");
		//vo.setTestReplytoAddr(null);
		vo.setIsVerpEnabled(Constants.YES); // is VERP enabled
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
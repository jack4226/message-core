package ltj.message.table;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import ltj.data.preload.EmailTemplateEnum;
import ltj.data.preload.EmailVariableEnum;
import ltj.data.preload.MailingListEnum;
import ltj.data.preload.SubscriberEnum;
import ltj.message.constant.Constants;
import ltj.message.constant.MLDeliveryType;
import ltj.message.constant.StatusId;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.SchedulesBlob;
import ltj.message.main.CreateTableBase;
import ltj.message.util.BlobUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.spring.util.SpringUtil;

public class EmailAddrTable extends CreateTableBase {

	/**
	 * Creates a new instance of EmailAddrTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public EmailAddrTable() throws ClassNotFoundException, SQLException {
		init();
	}

	public void createTables() throws SQLException {
		createEmailTable();
		createMailingListTable();
		createEmailSubscrptTable();
		createEmailVariableTable();
		createEmailTemplateTable();
		createFindByAddressSP();
		createEmailUnsubCommentTable();
	}

	public void dropTables() {
		try {
			stm.execute("DROP TABLE email_unsub_cmnt");
			System.out.println("Dropped email_unsub_cmnt Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE email_template");
			System.out.println("Dropped email_template Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE email_variable");
			System.out.println("Dropped email_variable Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE email_subscrpt");
			System.out.println("Dropped email_subscrpt Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE mailing_list");
			System.out.println("Dropped mailing_list Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE email_address");
			System.out.println("Dropped email_address Table...");
		}
		catch (SQLException e) {
		}
	}
	
	void createEmailTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE email_address ( "
					+ "EmailAddrId bigint AUTO_INCREMENT NOT NULL, "
					+ "EmailAddr varchar(255) NOT NULL, "
					+ "OrigEmailAddr varchar(255) NOT NULL, "
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " // A - active, S - suspended, I - Inactive
					+ "StatusChangeTime datetime(3), "
					+ "StatusChangeUserId varchar(10), "
					+ "BounceCount decimal(3) NOT NULL DEFAULT 0, "
					+ "LastBounceTime datetime(3), "
					+ "LastSentTime datetime(3), "
					+ "LastRcptTime datetime(3), "
					+ "AcceptHtml char(1) not null default '" + Constants.Y + "', "
					+ "UpdtTime datetime(3) NOT NULL, "
					+ "UpdtUserId char(10) NOT NULL, "
					+ "PRIMARY KEY (EmailAddrId), "
					+ "UNIQUE INDEX (EmailAddr) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created email_address Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMailingListTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE mailing_list ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "ListId varchar(8) NOT NULL, "
					+ "DisplayName varchar(50), "
					+ "AcctUserName varchar(100) NOT NULL, " 
						// left part of email address, right part from client_tbl table's DomainName
					+ "Description varchar(500), "
					+ "ClientId varchar(16) NOT NULL, "
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " 
						// A - active, I - Inactive
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + Constants.N + "', "
					+ "IsSendText char(1), "
					+ "CreateTime datetime(3) NOT NULL, "
					+ "ListMasterEmailAddr varchar(255), "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (ClientId) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (AcctUserName), "
					+ "UNIQUE INDEX (ListId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created mailing_list Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createEmailSubscrptTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE email_subscrpt ( "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8) NOT NULL, "
					+ "Subscribed char(1) NOT NULL, " 
						// Y - subscribed, N - not subscribed, P - Pending Confirmation
					+ "CreateTime datetime(3) NOT NULL, "
					+ "SentCount int NOT NULL DEFAULT 0, "
					+ "LastSentTime datetime(3), "
					+ "OpenCount int NOT NULL DEFAULT 0, "
					+ "LastOpenTime datetime(3), "
					+ "ClickCount int NOT NULL DEFAULT 0, "
					+ "LastClickTime datetime(3), "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES email_address (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "FOREIGN KEY (ListId) REFERENCES mailing_list (ListId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "PRIMARY KEY (EmailAddrId,ListId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created email_subscrpt Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createEmailVariableTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE email_variable ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "VariableName varchar(26) NOT NULL, "
					+ "VariableType char(1) NOT NULL, " 
						// S - system, C - customer (individual)
					+ "TableName varchar(50), " // document only
					+ "ColumnName varchar(50), " // document only
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " 
						// A - active, I - Inactive
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + Constants.N + "', "
					+ "DefaultValue varchar(255), "
					+ "VariableQuery varchar(255), " // 1) provides TO emailAddId as query criteria
													// 2) returns a single field called "ResultStr"
					+ "VariableProc varchar(100), " // when Query is null or returns no result
					+ "PRIMARY KEY (RowId), "
					+ "UNIQUE INDEX (VariableName) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created email_variable Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createEmailTemplateTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE email_template ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "TemplateId varchar(26) NOT NULL, "
					+ "ListId varchar(8) NOT NULL, "
					+ "Subject varchar(255), "
					+ "BodyText mediumtext, "
					+ "IsHtml char(1) NOT NULL DEFAULT '" + Constants.N + "', " // Y or N
					+ "ListType varchar(12) NOT NULL, " // Traditional/Personalized
					+ "DeliveryOption varchar(4) NOT NULL DEFAULT '" + MLDeliveryType.ALL_ON_LIST.value() + "', " // when ListType is Personalized
						// ALL - all on list, CUST - only email addresses with customer record
					+ "SelectCriteria varchar(100), " 
						// additional selection criteria - to be implemented
					+ "EmbedEmailId char(1) NOT NULL DEFAULT '', " // Y, N, or <Blank> - use system default
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + Constants.N + "', "
					+ "Schedules blob, " // store a java object
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (ListId) REFERENCES mailing_list (ListId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "UNIQUE INDEX (TemplateId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created email_template Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createEmailUnsubCommentTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE email_unsub_cmnt ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8), "
					+ "Comments varchar(500) NOT NULL, "
					+ "AddTime datetime(3) NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES email_address (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (EmailAddrId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created email_unsub_cmnt Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}


/* MySQL Stored Procedure:
DELIMITER $$

DROP PROCEDURE IF EXISTS `message`.`FindByAddress` $$
CREATE DEFINER=`email`@`%` PROCEDURE `FindByAddress`(
  IN iEmailAddr VARCHAR(255),
  OUT oEmailAddrId LONG,
  OUT oEmailAddr VARCHAR(255),
  OUT oOrigEmailAddr VARCHAR(255),
  OUT oStatusId CHAR(1),
  OUT oStatusChangeTime DATETIME,
  OUT oStatusChangeUserId VARCHAR(10),
  OUT oBounceCount DECIMAL(3,0),
  OUT oLastBounceTime DATETIME,
  OUT oLastSentTime DATETIME,
  OUT oLastRcptTime DATETIME,
  OUT oAcceptHtml CHAR(1),
  OUT oUpdtTime DATETIME,
  OUT oUpdtUserId VARCHAR(10)
 )
 MODIFIES SQL DATA
BEGIN
  declare pEmailAddrId long default 0;
  declare currTime DATETIME;
  declare pEmailAddr varchar(255) default null;
  select EmailAddrId, EmailAddr, OrigEmailAddr, StatusId, StatusChangeTime, StatusChangeUserId,
          BounceCount, LastBounceTime, LastSentTime, LastRcptTime, AcceptHtml,
          UpdtTime, UpdtUserId
    into oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId,
          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml,
          oUpdtTime, oUpdtUserId
    from email_address where EmailAddr=TRIM(iEmailAddr);
  select now() into currTime;
  if oEmailAddr is NULL then
    insert into email_address (EmailAddr, OrigEmailAddr, StatusChangeTime,
                          StatusChangeUserId, UpdtTime, UpdtUserId)
      values (iEmailAddr, iEmailAddr, currTime, 'StoredProc', currTime, 'StoredProc');
    select last_insert_id() into oEmailAddrId;
    select iEmailAddr, iEmailAddr into oEmailAddr, oOrigEmailAddr;
    select 'A' into oStatusId;
    select currTime, 'StoredProc' into oStatusChangeTime, oStatusChangeUserId;
    select 0 into oBounceCount;
    select null, null, null into oLastBounceTime, oLastSentTime, oLastRcptTime;
    select 'Y' into oAcceptHtml;
    select currTime, 'StoredProc' into oUpdtTime, oUpdtUserId;
  end if;
  select oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId,
          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml,
          oUpdtTime, oUpdtUserId;
END $$

DELIMITER ;
*/
	
	void createFindByAddressSP() throws SQLException {
		try {
			stm.execute(
				"DROP PROCEDURE IF EXISTS `message`.`FindByAddress`"
			);
			stm.execute(
				"CREATE PROCEDURE `FindByAddress`(" + LF +
				"  IN iEmailAddr VARCHAR(255)," + LF +
				"  IN iOrigEmailAddr VARCHAR(255)," + LF +
				"  OUT oEmailAddrId LONG," + LF +
				"  OUT oEmailAddr VARCHAR(255)," + LF +
				"  OUT oOrigEmailAddr VARCHAR(255)," + LF +
				"  OUT oStatusId CHAR(1)," + LF +
				"  OUT oStatusChangeTime DATETIME," + LF +
				"  OUT oStatusChangeUserId VARCHAR(10)," + LF +
				"  OUT oBounceCount DECIMAL(3,0)," + LF +
				"  OUT oLastBounceTime DATETIME," + LF +
				"  OUT oLastSentTime DATETIME," + LF +
				"  OUT oLastRcptTime DATETIME," + LF +
				"  OUT oAcceptHtml CHAR(1)," + LF +
				"  OUT oUpdtTime DATETIME," + LF +
				"  OUT oUpdtUserId VARCHAR(10)" + LF +
				" )" + LF +
				" MODIFIES SQL DATA" + LF +
				"BEGIN" + LF +
				"  declare pEmailAddrId long default 0;" + LF +
				"  declare currTime DATETIME;" + LF +
				"  declare pEmailAddr varchar(255) default null;" + LF +
				"  select EmailAddrId, EmailAddr, OrigEmailAddr, StatusId, StatusChangeTime, StatusChangeUserId," + LF +
				"          BounceCount, LastBounceTime, LastSentTime, LastRcptTime, AcceptHtml," + LF +
				"          UpdtTime, UpdtUserId" + LF +
				"    into oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId," + LF +
				"          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml," + LF +
				"          oUpdtTime, oUpdtUserId" + LF +
				"    from email_address where EmailAddr=TRIM(iEmailAddr);" + LF +
				"  select now() into currTime;" + LF +
				"  if oEmailAddr is NULL then" + LF +
				"    insert into email_address (EmailAddr, OrigEmailAddr, StatusChangeTime," + LF +
				"                          StatusChangeUserId, UpdtTime, UpdtUserId)" + LF +
				"      values (iEmailAddr, iOrigEmailAddr, currTime, 'StoredProc', currTime, 'StoredProc');" + LF +
				"    select last_insert_id() into oEmailAddrId;" + LF +
				"    select iEmailAddr, iEmailAddr into oEmailAddr, oOrigEmailAddr;" + LF +
				"    select 'A' into oStatusId;" + LF +
				"    select currTime, 'StoredProc' into oStatusChangeTime, oStatusChangeUserId;" + LF +
				"    select 0 into oBounceCount;" + LF +
				"    select null, null, null into oLastBounceTime, oLastSentTime, oLastRcptTime;" + LF +
				"    select 'Y' into oAcceptHtml;" + LF +
				"    select currTime, 'StoredProc' into oUpdtTime, oUpdtUserId;" + LF +
				"  end if;" + LF +
				"END "
			);
			System.out.println("Created FindByAddress Stored Procedure...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadTestData() throws SQLException {
		insertEmailAddrs();
		insertMaillingList();
		insertProdMaillingList();
		insertEmailVariable();
		insertEmailTemplate();
		insertProdEmailTemplate();
		insertSubscribers();
	}

	public void loadReleaseData() throws SQLException {
		insertEmailAddrs();
		insertMaillingList();
		insertEmailVariable();
		insertEmailTemplate();
		insertSubscribers();
	}
	
	private void insertEmailAddrs() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO email_address " +
				"(EmailAddr," +
				"OrigEmailAddr," +
				"StatusId," +
				"StatusChangeTime," +
				"StatusChangeUserId," +
				"BounceCount," +
				"LastBounceTime," +
				"LastSentTime," +
				"LastRcptTime," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			for (SubscriberEnum.Subscriber sub : SubscriberEnum.Subscriber.values()) {
				ps.setString(1, sub.getAddress());
				ps.setString(2, sub.getAddress());
				ps.setString(3, StatusId.ACTIVE.value());
				ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				ps.setString(5, "testuser 1");
				ps.setInt(6, 0);
				ps.setTimestamp(7, null);
				ps.setTimestamp(8, null);
				ps.setTimestamp(9, null);
				ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
				ps.setString(11, Constants.DEFAULT_USER_ID);
				ps.execute();
			}
						
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private void insertMaillingList() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO mailing_list " +
				"(ListId," +
				"DisplayName," +
				"AcctUserName," +
				"Description," +
				"ClientId," +
				"StatusId," +
				"IsBuiltIn," +
				"CreateTime)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			
			for (MailingListEnum enu : MailingListEnum.values()) {
				if (enu.isProd() == false) {
					ps.setString(1, enu.name());
					ps.setString(2, enu.getDescription());
					ps.setString(3, enu.getAcctName());
					ps.setString(4, enu.getDescription());
					ps.setString(5, Constants.DEFAULT_CLIENTID);
					ps.setString(6, enu.getStatusId().value());
					ps.setString(7, enu.isBuiltin() ? Constants.Y : Constants.N);
					ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
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

	private void insertProdMaillingList() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO mailing_list " +
				"(ListId," +
				"DisplayName," +
				"AcctUserName," +
				"Description," +
				"ClientId," +
				"StatusId," +
				"IsBuiltIn," +
				"CreateTime)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			
			for (MailingListEnum enu : MailingListEnum.values()) {
				if (enu.isProd() == true) {
					ps.setString(1, enu.name());
					ps.setString(2, enu.getDescription());
					ps.setString(3, enu.getAcctName());
					ps.setString(4, enu.getDescription());
					ps.setString(5, Constants.DEFAULT_CLIENTID);
					ps.setString(6, enu.getStatusId().value());
					ps.setString(7, enu.isBuiltin() ? Constants.Y : Constants.N);
					ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
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

	private void insertSubscribers() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO email_subscrpt " +
				"(EmailAddrId," +
				"ListId," +
				"Subscribed," +
				"CreateTime)" +
				"VALUES (?, ?, ?, ?)");
			
			EmailAddressDao emailDao = SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
			
			for (SubscriberEnum sublst : SubscriberEnum.values()) {
				for (SubscriberEnum.Subscriber sbsr : SubscriberEnum.Subscriber.values()) {
					EmailAddressVo emailVo = emailDao.findByAddress(sbsr.getAddress());
					ps.setLong(1, emailVo.getEmailAddrId());
					ps.setString(2, sublst.getMailingList().name());
					ps.setString(3, Constants.Y);
					ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
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

	private void insertEmailVariable() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO email_variable " +
				"(VariableName," +
				"VariableType," +
				"TableName," +
				"ColumnName," +
				"StatusId," +
				"IsBuiltIn," +
				"DefaultValue," +
				"VariableQuery," +
				"VariableProc)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			for (EmailVariableEnum var : EmailVariableEnum.values()) {
				ps.setString(1, var.name());
				ps.setString(2, var.getVariableType().value());
				ps.setString(3, var.getTableName());
				ps.setString(4, var.getColumnName());
				ps.setString(5, StatusId.ACTIVE.value());
				ps.setString(6, var.isBuiltin() ? Constants.Y : Constants.N);
				ps.setString(7, var.getDefaultValue());
				ps.setString(8, var.getVariableQuery());
				ps.setString(9, var.getVariableProcName());
				ps.execute();
			}
						
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	private void insertEmailTemplate() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO email_template " +
				"(TemplateId," +
				"ListId," +
				"Subject," +
				"BodyText," +
				"IsHtml," +
				"ListType," +
				"DeliveryOption," +
				"IsBuiltIn," +
				"EmbedEmailId," +
				"Schedules)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			for (EmailTemplateEnum tmplt : EmailTemplateEnum.values()) {
				if (tmplt.isProd()) {
					continue;
				}
				ps.setString(1, tmplt.name());
				ps.setString(2, tmplt.getMailingList().name());
				ps.setString(3, tmplt.getSubject());
				ps.setString(4, tmplt.getBodyText());
				ps.setString(5, tmplt.isHtml() ? Constants.Y : Constants.N);
				ps.setString(6, tmplt.getListType().value());
				ps.setString(7, tmplt.getDeliveryType().value());
				ps.setString(8, tmplt.isBuiltin() ? Constants.Y : Constants.N);
				if (tmplt.getIsEmbedEmailId() == null) {
					ps.setString(9, " "); // use system default
				}
				else {
					boolean embedEmailId = tmplt.getIsEmbedEmailId().booleanValue();
					ps.setString(9,  embedEmailId ? Constants.Y : Constants.N);
				}
				SchedulesBlob blob = new SchedulesBlob();
				try {
					byte[] baosarray = BlobUtil.objectToBytes(blob);
					ps.setBytes(10, baosarray);
				}
				catch (IOException e) {
					throw new SQLException("IOException caught - " + e.toString());
				}
				ps.execute();
			}
						
			ps.close();
			System.out.println("Inserted email_template...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	void selectEmailTemplate() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"select * from email_template where TemplateId = 'test template'");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String id = rs.getString("ListId");
				byte[] bytes = rs.getBytes("Schedules");
				try {
					SchedulesBlob blob = (SchedulesBlob) BlobUtil.bytesToObject(bytes);
					System.out.println("ListId: " + id + ", blob: " + blob);
				}
				catch (Exception e) {
					throw new SQLException("Exception caught - " + e.toString());
				}
			}
			ps.close();
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}
	
	private void insertProdEmailTemplate() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO email_template " +
				"(TemplateId," +
				"ListId," +
				"Subject," +
				"BodyText," +
				"IsHtml," +
				"ListType," +
				"DeliveryOption," +
				"IsBuiltIn," +
				"EmbedEmailId," +
				"Schedules)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			for (EmailTemplateEnum tmplt : EmailTemplateEnum.values()) {
				if (tmplt.isProd() == false) {
					continue;
				}
				ps.setString(1, tmplt.name());
				ps.setString(2, tmplt.getMailingList().name());
				ps.setString(3, tmplt.getSubject());
				ps.setString(4, tmplt.getBodyText());
				ps.setString(5, tmplt.isHtml() ? Constants.Y : Constants.N);
				ps.setString(6, tmplt.getListType().value());
				ps.setString(7, tmplt.getDeliveryType().value());
				ps.setString(8, tmplt.isBuiltin() ? Constants.Y : Constants.N);
				if (tmplt.getIsEmbedEmailId() == null) {
					ps.setString(9, " "); // use system default
				}
				else {
					boolean embedEmailId = tmplt.getIsEmbedEmailId().booleanValue();
					ps.setString(9,  embedEmailId ? Constants.Y : Constants.N);
				}
				SchedulesBlob blob = new SchedulesBlob();
				try {
					byte[] baosarray = BlobUtil.objectToBytes(blob);
					ps.setBytes(10, baosarray);
				}
				catch (IOException e) {
					throw new SQLException("IOException caught - " + e.toString());
				}
				ps.execute();
			}
			
			ps.close();
			System.out.println("Inserted Product email_template...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}
	
	/**
	 * to trigger the insert of template id's to MsgDataType table. 
	 */
	public void updateTemplates() {
		int rowsUpdated = 0;
		List<EmailTemplateVo> list = getEmailTemplateDao().getAll();
		for (EmailTemplateVo tmpltVo : list) {
			tmpltVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
			rowsUpdated += getEmailTemplateDao().update(tmpltVo);
		}
		System.out.println("Updated email_template records: " + rowsUpdated);
	}

	private EmailTemplateDao emailTemplateDao = null;
	private EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = SpringUtil.getDaoAppContext().getBean(EmailTemplateDao.class);
		}
		return emailTemplateDao;
	}
	
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			EmailAddrTable ct = new EmailAddrTable();
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
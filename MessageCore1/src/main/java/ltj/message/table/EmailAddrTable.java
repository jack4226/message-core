package ltj.message.table;

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
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.SchedulesBlob;
import ltj.message.main.CreateTableBase;
import ltj.message.util.BlobUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.message.vo.emailaddr.EmailVariableVo;
import ltj.message.vo.emailaddr.MailingListVo;
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
					+ "email_addr_id bigint AUTO_INCREMENT NOT NULL, "
					+ "email_addr varchar(255) NOT NULL, "
					+ "orig_email_addr varchar(255) NOT NULL, "
					+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " // A - active, S - suspended, I - Inactive
					+ "status_change_time datetime(3), "
					+ "status_change_user_id varchar(10), "
					+ "bounce_count decimal(3) NOT NULL DEFAULT 0, "
					+ "last_bounce_time datetime(3), "
					+ "last_sent_time datetime(3), "
					+ "last_rcpt_time datetime(3), "
					+ "accept_html boolean not null default true, "
					+ "updt_time datetime(3) NOT NULL, "
					+ "updt_user_id char(10) NOT NULL, "
					+ "CONSTRAINT email_address_pkey PRIMARY KEY (email_addr_id), "
					+ "UNIQUE INDEX email_address_ix_email_addr (email_addr), "
					+ "UNIQUE INDEX email_address_ix_orig_email_addr (orig_email_addr) "
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
					+ "row_id int AUTO_INCREMENT not null, "
					+ "list_id varchar(8) NOT NULL, "
					+ "display_name varchar(50), "
					+ "acct_user_name varchar(100) NOT NULL, " 
						// left part of email address, right part from client_tbl table's DomainName
					+ "description varchar(500), "
					+ "client_id varchar(16) NOT NULL, "
					+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " 
						// A - active, I - Inactive
					+ "is_built_in boolean NOT NULL DEFAULT false, "
					+ "is_send_text boolean, "
					+ "create_time datetime(3) NOT NULL, "
					+ "list_master_email_addr varchar(255), "
					+ "CONSTRAINT mailing_list_pkey PRIMARY KEY (row_id), "
					+ "FOREIGN KEY mailing_list_fk_client_id (client_id) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX mailing_list_ix_acct_user_name (acct_user_name), "
					+ "UNIQUE INDEX mailing_list_ix_list_id (list_id) "
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
					+ "email_addr_id bigint NOT NULL, "
					+ "list_id varchar(8) NOT NULL, "
					+ "subscribed char(1) NOT NULL, " 
						// Y - subscribed, N - not subscribed, P - Pending Confirmation
					+ "create_time datetime(3) NOT NULL, "
					+ "sent_count int NOT NULL DEFAULT 0, "
					+ "last_sent_time datetime(3), "
					+ "open_count int NOT NULL DEFAULT 0, "
					+ "last_open_time datetime(3), "
					+ "click_count int NOT NULL DEFAULT 0, "
					+ "last_click_time datetime(3), "
					+ "FOREIGN KEY email_subscrpt_fk_email_addr_id (email_addr_id) REFERENCES email_address (email_addr_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX email_subscrpt_ix_email_addr_id (email_addr_id), "
					+ "FOREIGN KEY email_subscrpt_fk_list_id (list_id) REFERENCES mailing_list (list_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "CONSTRAINT email_subscrpt_pkey PRIMARY KEY (email_addr_id,list_id) "
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
					+ "row_id int AUTO_INCREMENT not null, "
					+ "variable_name varchar(26) NOT NULL, "
					+ "variable_type char(1) NOT NULL, " 
						// S - system, C - customer (individual)
					+ "table_name varchar(50), " // document only
					+ "column_name varchar(50), " // document only
					+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " 
						// A - active, I - Inactive
					+ "is_built_in boolean NOT NULL DEFAULT false, "
					+ "default_value varchar(255), "
					+ "variable_query varchar(255), " // 1) provides TO emailAddId as query criteria
													// 2) returns a single field called "ResultStr"
					+ "variable_proc varchar(100), " // when Query is null or returns no result
					+ "CONSTRAINT email_variable_pkey PRIMARY KEY (row_id), "
					+ "UNIQUE INDEX email_variable_ix_varbl_name (variable_name) "
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
					+ "row_id int AUTO_INCREMENT not null, "
					+ "template_id varchar(26) NOT NULL, "
					+ "list_id varchar(8) NOT NULL, "
					+ "subject varchar(255), "
					+ "body_text mediumtext, "
					+ "is_html boolean NOT NULL DEFAULT false, "
					+ "list_type varchar(12) NOT NULL, " // Traditional/Personalized
					+ "delivery_option varchar(4) NOT NULL DEFAULT '" + MLDeliveryType.ALL_ON_LIST.value() + "', " // when ListType is Personalized
						// ALL - all on list, CUST - only email addresses with customer record
					+ "select_criteria varchar(100), " 
						// additional selection criteria - to be implemented
					+ "embed_email_id Boolean default NULL, " // true, false, or null to use system default
					+ "is_built_in boolean NOT NULL DEFAULT false, "
					+ "schedules blob, " // store a java object
					+ "FOREIGN KEY email_template_fk_list_id (list_id) REFERENCES mailing_list (list_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "CONSTRAINT email_template_pkey PRIMARY KEY (row_id), "
					+ "UNIQUE INDEX email_template_ix_tmplt_id (template_id) "
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
					+ "row_id int AUTO_INCREMENT not null, "
					+ "email_addr_id bigint NOT NULL, "
					+ "list_id varchar(8), "
					+ "comments varchar(500) NOT NULL, "
					+ "add_time datetime(3) NOT NULL, "
					+ "CONSTRAINT email_unsub_cmnt_pkey PRIMARY KEY (row_id), "
					+ "FOREIGN KEY email_unsub_cmnt_fk_email_addr_id (email_addr_id) REFERENCES email_address (email_addr_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX email_unsub_cmnt_ix_email_addr_id (email_addr_id) "
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
  OUT oemail_addr_id LONG,
  OUT oEmailAddr VARCHAR(255),
  OUT oOrigEmailAddr VARCHAR(255),
  OUT oStatusId CHAR(1),
  OUT oStatusChangeTime DATETIME,
  OUT oStatusChangeUserId VARCHAR(10),
  OUT oBounceCount DECIMAL(3,0),
  OUT oLastBounceTime DATETIME,
  OUT oLastSentTime DATETIME,
  OUT oLastRcptTime DATETIME,
  OUT oAcceptHtml BOOLEAN,
  OUT oUpdtTime DATETIME,
  OUT oUpdtUserId VARCHAR(10)
 )
 MODIFIES SQL DATA
BEGIN
  declare pEmailAddrId long default 0;
  declare currTime DATETIME;
  declare pEmailAddr varchar(255) default null;
  select email_addr_id, email_addr, orig_email_addr, status_id, status_change_time, status_change_user_id,
          bounce_count, last_bounce_time, last_sent_time, last_rcpt_time, accept_html,
          updt_time, updt_user_id
    into oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId,
          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml,
          oUpdtTime, oUpdtUserId
    from email_address where email_addr=TRIM(iEmailAddr);
  select now() into currTime;
  if oEmailAddr is NULL then
    insert into email_address (email_addr, orig_email_addr, status_change_time,
                          status_change_user_id, updt_time, updt_user_id)
      values (iEmailAddr, iEmailAddr, currTime, 'StoredProc', currTime, 'StoredProc');
    select last_insert_id() into oEmailAddrId;
    select iEmailAddr, iEmailAddr into oEmailAddr, oOrigEmailAddr;
    select 'A' into oStatusId;
    select currTime, 'StoredProc' into oStatusChangeTime, oStatusChangeUserId;
    select 0 into oBounceCount;
    select null, null, null into oLastBounceTime, oLastSentTime, oLastRcptTime;
    select true into oAcceptHtml;
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
				"  OUT oAcceptHtml BOOLEAN," + LF +
				"  OUT oUpdtTime DATETIME," + LF +
				"  OUT oUpdtUserId VARCHAR(10)" + LF +
				" )" + LF +
				" MODIFIES SQL DATA" + LF +
				"BEGIN" + LF +
				"  declare pEmailAddrId long default 0;" + LF +
				"  declare currTime DATETIME;" + LF +
				"  declare pEmailAddr varchar(255) default null;" + LF +
				"  select email_addr_id, email_addr, orig_email_addr, status_id, status_change_time, status_change_user_id," + LF +
				"          bounce_count, last_bounce_time, last_sent_time, last_rcpt_time, accept_html," + LF +
				"          updt_time, updt_user_id" + LF +
				"    into oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId," + LF +
				"          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml," + LF +
				"          oUpdtTime, oUpdtUserId" + LF +
				"    from email_address where email_addr=TRIM(iEmailAddr);" + LF +
				"  select now() into currTime;" + LF +
				"  if oEmailAddr is NULL then" + LF +
				"    insert into email_address (email_addr, orig_email_addr, status_change_time," + LF +
				"                          status_change_user_id, updt_time, updt_user_id)" + LF +
				"      values (iEmailAddr, iOrigEmailAddr, currTime, 'StoredProc', currTime, 'StoredProc');" + LF +
				"    select last_insert_id() into oEmailAddrId;" + LF +
				"    select iEmailAddr, iEmailAddr into oEmailAddr, oOrigEmailAddr;" + LF +
				"    select 'A' into oStatusId;" + LF +
				"    select currTime, 'StoredProc' into oStatusChangeTime, oStatusChangeUserId;" + LF +
				"    select 0 into oBounceCount;" + LF +
				"    select null, null, null into oLastBounceTime, oLastSentTime, oLastRcptTime;" + LF +
				"    select true into oAcceptHtml;" + LF +
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
		EmailAddressDao dao = SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
		try {
			int rows = 0;
			for (SubscriberEnum.Subscriber sub : SubscriberEnum.Subscriber.values()) {
				EmailAddressVo vo = new EmailAddressVo();
				vo.setEmailAddr(sub.getAddress());
				vo.setStatusId(StatusId.ACTIVE.value());
				vo.setStatusChangeTime(new Timestamp(System.currentTimeMillis()));
				vo.setStatusChangeUserId("testuser 1");
				vo.setBounceCount(0);
				vo.setLastBounceTime(null);
				vo.setLastSentTime(null);
				vo.setLastRcptTime(null);
				vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
				vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
				rows += dao.insert(vo);
			}
						
			System.out.println("Inserted all rows to email_address: " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private void insertMaillingList() throws SQLException {
		MailingListDao dao = SpringUtil.getDaoAppContext().getBean(MailingListDao.class);
		try {
			int rows = 0;
			for (MailingListEnum enu : MailingListEnum.values()) {
				if (enu.isProd() == false) {
					MailingListVo vo = new MailingListVo();
					vo.setListId(enu.name());
					vo.setDisplayName(enu.getDisplayName());
					vo.setAcctUserName(enu.getAcctName());
					vo.setDescription(enu.getDescription());
					vo.setClientId(Constants.DEFAULT_CLIENTID);
					vo.setStatusId(enu.getStatusId().value());
					vo.setIsBuiltIn(enu.isBuiltin());
					vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
					
					rows += dao.insert(vo);
				}
			}
			
			System.out.println("Inserted all rows to mailing_list: " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	private void insertProdMaillingList() throws SQLException {
		MailingListDao dao = SpringUtil.getDaoAppContext().getBean(MailingListDao.class);
		try {
			int rows = 0;
			for (MailingListEnum enu : MailingListEnum.values()) {
				if (enu.isProd() == true) {					
					MailingListVo vo = new MailingListVo();
					vo.setListId(enu.name());
					vo.setDisplayName(enu.getDisplayName());
					vo.setAcctUserName(enu.getAcctName());
					vo.setDescription(enu.getDescription());
					vo.setClientId(Constants.DEFAULT_CLIENTID);
					vo.setStatusId(enu.getStatusId().value());
					vo.setIsBuiltIn(enu.isBuiltin());
					vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
					
					rows += dao.insert(vo);
				}
			}

			System.out.println("Inserted all rows to mailing_list: " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private void insertSubscribers() throws SQLException {
		EmailSubscrptDao dao = SpringUtil.getDaoAppContext().getBean(EmailSubscrptDao.class);
		try {
			EmailAddressDao emailDao = SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
			
			int rows = 0;
			for (SubscriberEnum sublst : SubscriberEnum.values()) {
				for (SubscriberEnum.Subscriber sbsr : SubscriberEnum.Subscriber.values()) {
					EmailSubscrptVo vo = new EmailSubscrptVo();
					EmailAddressVo emailVo = emailDao.findByAddress(sbsr.getAddress());
					vo.setEmailAddrId(emailVo.getEmailAddrId());
					vo.setListId(sublst.getMailingList().name());
					vo.setSubscribed(Constants.Y);
					vo.setCreateTime(new Timestamp(System.currentTimeMillis()));
					
					rows += dao.insert(vo);
				}
			}
			
			System.out.println("Inserted all rows to email_subscrpt: " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	private void insertEmailVariable() throws SQLException {
		EmailVariableDao dao = SpringUtil.getDaoAppContext().getBean(EmailVariableDao.class);
		try {
			int rows = 0;
			for (EmailVariableEnum var : EmailVariableEnum.values()) {
				EmailVariableVo vo = new EmailVariableVo();
				vo.setVariableName(var.name());
				vo.setVariableType(var.getVariableType().value());
				vo.setTableName(var.getTableName());
				vo.setColumnName(var.getColumnName());
				vo.setStatusId(StatusId.ACTIVE.value());
				vo.setIsBuiltIn(var.isBuiltin());
				vo.setDefaultValue(var.getDefaultValue());
				vo.setVariableQuery(var.getVariableQuery());
				vo.setVariableProc(var.getVariableProcName());
				rows += dao.insert(vo);
			}
						
			System.out.println("Inserted all rows to email_variable: " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	private void insertEmailTemplate() throws SQLException {
		try {
			int rows = 0;
			for (EmailTemplateEnum tmplt : EmailTemplateEnum.values()) {
				if (tmplt.isProd()) {
					continue;
				}
				EmailTemplateVo vo = new EmailTemplateVo();
				vo.setTemplateId(tmplt.name());
				vo.setListId(tmplt.getMailingList().name());
				vo.setSubject(tmplt.getSubject());
				vo.setBodyText(tmplt.getBodyText());
				vo.setIsHtml(tmplt.isHtml());
				vo.setListType(tmplt.getListType().value());
				vo.setDeliveryOption(tmplt.getDeliveryType().value());
				vo.setIsBuiltIn(tmplt.isBuiltin());
				vo.setEmbedEmailId(tmplt.getIsEmbedEmailId());
				vo.setSchedulesBlob(new SchedulesBlob());
				rows += getEmailTemplateDao().insert(vo);
			}
			
			System.out.println("Inserted all rows to email_template: " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	void selectEmailTemplate() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"select * from email_template where template_id = 'test template'");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String id = rs.getString("list_id");
				byte[] bytes = rs.getBytes("schedules");
				try {
					SchedulesBlob blob = (SchedulesBlob) BlobUtil.bytesToObject(bytes);
					System.out.println("list_id: " + id + ", blob: " + blob);
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
			int rows = 0;
			for (EmailTemplateEnum tmplt : EmailTemplateEnum.values()) {
				if (tmplt.isProd() == false) {
					continue;
				}
				EmailTemplateVo vo = new EmailTemplateVo();
				vo.setTemplateId(tmplt.name());
				vo.setListId(tmplt.getMailingList().name());
				vo.setSubject(tmplt.getSubject());
				vo.setBodyText(tmplt.getBodyText());
				vo.setIsHtml(tmplt.isHtml());
				vo.setListType(tmplt.getListType().value());
				vo.setDeliveryOption(tmplt.getDeliveryType().value());
				vo.setIsBuiltIn(tmplt.isBuiltin());
				vo.setEmbedEmailId(tmplt.getIsEmbedEmailId());
				vo.setSchedulesBlob(new SchedulesBlob());
				rows += getEmailTemplateDao().insert(vo);
			}
			
			System.out.println("Inserted all production rows to email_template: " + rows);
		} catch (Exception e) {
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
			tmpltVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
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
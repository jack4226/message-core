package ltj.message.table;

import java.sql.SQLException;

import ltj.message.constant.CarrierCode;
import ltj.message.constant.Constants;
import ltj.message.constant.MLDeliveryType;
import ltj.message.main.CreateTableBase;

public class InboxTables extends CreateTableBase {
	/**
	 * Creates a new instance of InboxTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public InboxTables() throws ClassNotFoundException, SQLException {
		init();
	}

	public void dropTables() {
		try {
			stm.execute("DROP TABLE msg_unsub_cmnt");
			System.out.println("Dropped msg_unsub_cmnt Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_click_count");
			System.out.println("Dropped msg_click_count Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_action_log");
			System.out.println("Dropped msg_action_log Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE delivery_status");
			System.out.println("Dropped delivery_status Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_attachment");
			System.out.println("Dropped msg_attachment Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_address");
			System.out.println("Dropped msg_address Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_header");
			System.out.println("Dropped msg_header Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_rfc_field");
			System.out.println("Dropped msg_rfc_field Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_stream");
			System.out.println("Dropped msg_stream Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_inbox");
			System.out.println("Dropped msg_inbox Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_unread_count");
			System.out.println("Dropped msg_unread_count Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_sequence");
			System.out.println("Dropped msg_sequence Table...");
		}
		catch (SQLException e) {
		}
		
		try {
			stm.execute("DROP TABLE rander_attachment");
			System.out.println("Dropped rander_attachment Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE render_variable");
			System.out.println("Dropped render_variable Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE render_object");
			System.out.println("Dropped render_object Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE msg_rendered");
			System.out.println("Dropped msg_rendered Table...");
		}
		catch (SQLException e) {
		}
	}

	public void createTables() throws SQLException {
		createMsgRenderedTable();
		createRenderAttachmentTable();
		createRenderVariableTable();
		createRenderObjectTable();
		
		createMsgSequenceTable();
		createMsgUnreadCountTable();
		createMsgInboxTable();
		createMsgAttachmentTable();
		createMsgAddressTable();
		createMsgHeaderTable();
		createMsgRfcFieldTable();
		createMsgStreamTable();
		createDeliveryStatusTable();
		createMsgActionLogTable();
		createMsgClickCountTable();
		createMsgUnsubCommentTable();
	}
	
	public void loadTestData() throws SQLException {
		// dummy method to satisfy the super class
	}
	
	void createMsgRenderedTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_rendered ( " +
			"render_id bigint NOT NULL AUTO_INCREMENT, " +
			"msg_source_id varchar(16) NOT NULL, " +
			"subj_template_id varchar(16) NOT NULL, " +
			"body_template_id varchar(16) NOT NULL, " +
			"start_time datetime(3) NOT NULL," +
			"client_id varchar(16), " +
			"cust_id varchar(16), " +
			"purge_after int, " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id varchar(10) NOT NULL, " +
			"PRIMARY KEY (render_id), " +
			"INDEX (msg_source_id) " +
			") ENGINE=InnoDB"); // row-level locking
			System.out.println("Created msg_rendered Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRenderAttachmentTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE rander_attachment ( " +
			"render_id bigint NOT NULL, " +
			"attchmnt_seq decimal(2) NOT NULL, " + // up to 100 attachments per message
			"attchmnt_name varchar(100), " +
			"attchmnt_type varchar(100), " +
			"attchmnt_disp varchar(100), " +
			"attchmnt_value mediumblob, " +
			"FOREIGN KEY (render_id) REFERENCES msg_rendered (render_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (render_id), " +
			"PRIMARY KEY (render_id,attchmnt_seq)) ENGINE=InnoDB");
			System.out.println("Created rander_attachment Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRenderVariableTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE render_variable ( " +
			"render_id bigint NOT NULL, " +
			"variable_name varchar(26), " +
			"variable_format varchar(50), " +
			"variable_type char(1), " +
			// T - text, N - numeric, D - DateField/time,
			// A - address, X - X-Header, L - LOB(Attachment)
			"variable_value text, " +
			"FOREIGN KEY (render_id) REFERENCES msg_rendered (render_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (render_id), " +
			"PRIMARY KEY (render_id,variable_name)) ENGINE=InnoDB");
			System.out.println("Created render_variable Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRenderObjectTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE render_object ( " +
			"render_id bigint NOT NULL, " +
			"variable_name varchar(26), " +
			"variable_format varchar(50), " +
			"variable_type char(1), " +
			// T - text, N - numeric, D - DateField/time,
			// A - address, X - X-Header, L - LOB(Attachment), C - Collection
			"variable_value mediumblob, " +
			"FOREIGN KEY (render_id) REFERENCES msg_rendered (render_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (render_id), " +
			"PRIMARY KEY (render_id,variable_name)) ENGINE=InnoDB");
			System.out.println("Created render_object Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgSequenceTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_sequence ( " +
			"seq_id bigint NOT NULL " +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created msg_sequence Table...");
			stm.execute("INSERT INTO msg_sequence (seq_id) VALUES(0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgUnreadCountTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_unread_count ( " +
			"inbox_unread_count int NOT NULL, " +
			"sent_unread_count int NOT NULL" +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created msg_unread_count Table...");
			stm.execute("INSERT INTO msg_unread_count (inbox_unread_count,sent_unread_count) VALUES(0,0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgInboxTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_inbox ( " +
			"msg_id bigint NOT NULL, " +
			"msg_ref_id bigint, " + // link to another msg_inbox record (a reply or a bounce)
			"lead_msg_id bigint NOT NULL, " +
			"carrier_code char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.value() + "', " + // S - SmtpMail, W - WebMail
			"msg_direction char(1) NOT NULL, " + // R - Received, S - Sent
			"rule_name varchar(26) NOT NULL, " + // link to rule_logic.rule_name
			"msg_subject varchar(255), " +
			"msg_priority varchar(10), " + // 1 (High)/2 (Normal)/3 (Low)
			"received_time datetime(3) NOT NULL, " +
			"from_addr_id bigint NOT NULL, " + // link to email_address
			"reply_to_addr_id bigint, " + // link to email_address
			"to_addr_id bigint NOT NULL, " + // link to email_address
			"client_id varchar(16), " + // link to client_tbl - derived from OutMsgRefId
			"cust_id varchar(16), " + // link to customer_tbl - derived from OutMsgRefId
			"purge_date Date, " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id varchar(10) NOT NULL, " +
			"lock_time datetime(3), " +
			"lock_id varchar(10), " +
			"read_count int NOT NULL DEFAULT 0, " + // how many times it's been read
			"reply_count int NOT NULL DEFAULT 0, " + // how many times it's been replied
			"forward_count int NOT NULL DEFAULT 0, " + // how many times it's been forwarded
			"flagged char(1) NOT NULL DEFAULT '" + Constants.N + "', " +
			"delivery_time datetime(3), " + // for out-bound messages only, updated by MailSender
			"status_id char(1) NOT NULL, " + // P - pending, D - delivered by MailSender, F - delivery failed, C/O - closed/Open (for received mail)
			"smtp_message_id varchar(255), " + // SMTP message Id, updated by MailSender once delivered
			"render_id bigint, " + // link to a msg_rendered record
			"override_test_addr char(1), " + // Y - tell MailSender to use TO address even under test mode
			"attachment_count smallint NOT NULL DEFAULT 0, " + // for UI performance
			"attachment_size int NOT NULL DEFAULT 0, " + // for UI performance
			"msg_body_size int NOT NULL DEFAULT 0, " + // for UI performance
			"msg_content_type varchar(100) NOT NULL, " +
			"body_content_type varchar(50), " +
			"msg_body mediumtext, " +
			"PRIMARY KEY (msg_id), " +
			"INDEX (lead_msg_id), " +
			"FOREIGN KEY (render_id) REFERENCES msg_rendered (render_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (render_id), " +
			"FOREIGN KEY (from_addr_id) REFERENCES email_address (email_addr_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (from_addr_id), " +
			"FOREIGN KEY (to_addr_id) REFERENCES email_address (email_addr_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (to_addr_id)" +
			") ENGINE=InnoDB");
			System.out.println("Created msg_inbox Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgAttachmentTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_attachment ( " +
			"msg_id bigint NOT NULL, " +
			"attchmnt_depth decimal(2) NOT NULL, " +
			"attchmnt_seq decimal(3) NOT NULL, " +
			"attchmnt_name varchar(100), " +
			"attchmnt_type varchar(100), " +
			"attchmnt_disp varchar(100), " +
			"attchmnt_value mediumblob, " +
			"FOREIGN KEY (msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (msg_id), " +
			"PRIMARY KEY (msg_id,attchmnt_depth,attchmnt_seq)" +
			") ENGINE=InnoDB");
			System.out.println("Created msg_attachment Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgAddressTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_address ( " +
			"msg_id bigint NOT NULL, " +
			"addr_type varchar(7) NOT NULL, " + // from, replyto, to, cc, bcc
			"addr_seq decimal(4) NOT NULL, " +
			"addr_value varchar(255), " +
			"FOREIGN KEY (msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (msg_id), " +
			"PRIMARY KEY (msg_id,addr_type,addr_seq)" +
			") ENGINE=InnoDB");
			System.out.println("Created msg_address Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgHeaderTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_header ( " +
			"msg_id bigint NOT NULL, " +
			"header_seq decimal(4) NOT NULL, " +
			"header_name varchar(100), " +
			"header_value text, " +
			"FOREIGN KEY (msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (msg_id), " +
			"PRIMARY KEY (msg_id,header_seq)" +
			") ENGINE=InnoDB");
			System.out.println("Created msg_header Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgRfcFieldTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_rfc_field ( " +
			"msg_id bigint NOT NULL, " +
			"rfc_type varchar(30) NOT NULL, " +
			"rfc_status varchar(30), " +
			"rfc_action varchar(30), " +
			"final_rcpt varchar(255), " +
			"final_rcpt_id bigint, " +
			"orig_rcpt varchar(255), " +
			"orig_msg_subject varchar(255), " +
			"message_id varchar(255), " +
			"dsn_text text, " +
			"dsn_rfc822 text, " +
			"dlvr_status text, " +
			"FOREIGN KEY (msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (msg_id), " +
			"FOREIGN KEY (final_rcpt_id) REFERENCES email_address (email_addr_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (final_rcpt_id), " +
			"PRIMARY KEY (msg_id,rfc_type)" +
			") ENGINE=InnoDB");
			System.out.println("Created msg_rfc_field Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgStreamTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_stream ( " +
			"msg_id bigint NOT NULL, " +
			"from_addr_id bigint, " +
			"to_addr_id bigint, " +
			"msg_subject varchar(255), " +
			"add_time datetime(3), " +
			"msg_stream mediumblob, " +
			"FOREIGN KEY (msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"PRIMARY KEY (msg_id) " +
			") ENGINE=InnoDB");
			System.out.println("Created msg_stream Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createDeliveryStatusTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE delivery_status ( " +
			"msg_id bigint NOT NULL, " +
			"final_recipient_id bigint NOT NULL, " +
			"final_recipient varchar(255), " +
			"original_recipient_id bigint, " +
			"message_id varchar(255), " + // returned SMTP message id
			"dsn_status varchar(50), " +
			"dsn_reason varchar(255), " +
			"dsn_text text, " +
			"dsn_rfc822 text, " +
			"delivery_status text, " +
			"add_time datetime(3), " +
			"FOREIGN KEY (msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (msg_id), " +
			"FOREIGN KEY (final_recipient_id) REFERENCES email_address (email_addr_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (final_recipient_id), " +
			"PRIMARY KEY (msg_id,final_recipient_id)) ENGINE=InnoDB");
			System.out.println("Created delivery_status Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createMsgActionLogTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_action_log ( " +
			"msg_id bigint NOT NULL, " +
			"msg_ref_id bigint, " + // link to previous message thread
			"lead_msg_id bigint NOT NULL, " + // message that started this thread
			"action_bo varchar(50) NOT NULL, " +
			"parameters varchar(255), " +
			"add_time datetime(3), " +
			"FOREIGN KEY (msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (msg_id), " +
			/* disable following foreign keys for performance reason */ 
			//"FOREIGN KEY (msg_ref_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			//"FOREIGN KEY (lead_msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (lead_msg_id), " +
			"PRIMARY KEY (msg_id, msg_ref_id) " + // use index to make msg_ref_id nullable
			") ENGINE=InnoDB");
			System.out.println("Created msg_action_log Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgClickCountTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_click_count ( " +
			"msg_id bigint NOT NULL, " +
			"list_id varchar(8) NOT NULL, " +
			"delivery_option varchar(4) NOT NULL DEFAULT '" + MLDeliveryType.ALL_ON_LIST.value() + "', " +
			"sent_count int NOT NULL DEFAULT 0, " +
			"open_count int NOT NULL DEFAULT 0, " +
			"click_count int NOT NULL DEFAULT 0, " +
			"last_open_time datetime(3) DEFAULT NULL, " +
			"last_click_time datetime(3) DEFAULT NULL, " +
			"start_time datetime(3) DEFAULT NULL, " +
			"end_time datetime(3) DEFAULT NULL, " +
			"unsubscribe_count int NOT NULL DEFAULT 0, " +
			"complaint_count int NOT NULL DEFAULT 0, " +
			"referral_count int NOT NULL DEFAULT 0, " +
			"FOREIGN KEY (msg_id) REFERENCES msg_inbox (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"PRIMARY KEY (msg_id) " +
			") ENGINE=InnoDB");
			System.out.println("Created msg_click_count Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgUnsubCommentTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_unsub_cmnt ( "
					+ "row_id int AUTO_INCREMENT not null, "
					+ "msg_id bigint NOT NULL, "
					+ "email_addr_id bigint NOT NULL, "
					+ "list_id varchar(8), "
					+ "comments varchar(500) NOT NULL, "
					+ "add_time datetime(3) NOT NULL, "
					+ "PRIMARY KEY (row_id), "
					+ "FOREIGN KEY (msg_id) REFERENCES msg_click_count (msg_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (msg_id), "
					+ "FOREIGN KEY (email_addr_id) REFERENCES email_address (email_addr_id) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (email_addr_id) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created msg_unsub_cmnt Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			InboxTables ct = new InboxTables();
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
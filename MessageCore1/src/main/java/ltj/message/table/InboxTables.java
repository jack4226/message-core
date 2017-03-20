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
			"RenderId bigint NOT NULL AUTO_INCREMENT, " +
			"MsgSourceId varchar(16) NOT NULL, " +
			"SubjTemplateId varchar(16) NOT NULL, " +
			"BodyTemplateId varchar(16) NOT NULL, " +
			"StartTime datetime(3) NOT NULL," +
			"ClientId varchar(16), " +
			"CustId varchar(16), " +
			"PurgeAfter int, " +
			"UpdtTime datetime(3) NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"PRIMARY KEY (RenderId), " +
			"INDEX (MsgSourceId) " +
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
			"RenderId bigint NOT NULL, " +
			"AttchmntSeq decimal(2) NOT NULL, " + // up to 100 attachments per message
			"AttchmntName varchar(100), " +
			"AttchmntType varchar(100), " +
			"AttchmntDisp varchar(100), " +
			"AttchmntValue mediumblob, " +
			"FOREIGN KEY (RenderId) REFERENCES msg_rendered (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,AttchmntSeq)) ENGINE=InnoDB");
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
			"RenderId bigint NOT NULL, " +
			"VariableName varchar(26), " +
			"VariableFormat varchar(50), " +
			"VariableType char(1), " +
			// T - text, N - numeric, D - DateField/time,
			// A - address, X - X-Header, L - LOB(Attachment)
			"VariableValue text, " +
			"FOREIGN KEY (RenderId) REFERENCES msg_rendered (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,VariableName)) ENGINE=InnoDB");
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
			"RenderId bigint NOT NULL, " +
			"VariableName varchar(26), " +
			"VariableFormat varchar(50), " +
			"VariableType char(1), " +
			// T - text, N - numeric, D - DateField/time,
			// A - address, X - X-Header, L - LOB(Attachment), C - Collection
			"VariableValue mediumblob, " +
			"FOREIGN KEY (RenderId) REFERENCES msg_rendered (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,VariableName)) ENGINE=InnoDB");
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
			"SeqId bigint NOT NULL " +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created msg_sequence Table...");
			stm.execute("INSERT INTO msg_sequence (SeqId) VALUES(0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgUnreadCountTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_unread_count ( " +
			"InboxUnreadCount int NOT NULL, " +
			"SentUnreadCount int NOT NULL" +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created msg_unread_count Table...");
			stm.execute("INSERT INTO msg_unread_count (InboxUnreadCount,SentUnreadCount) VALUES(0,0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgInboxTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_inbox ( " +
			"MsgId bigint NOT NULL, " +
			"MsgRefId bigint, " + // link to another msg_inbox record (a reply or a bounce)
			"LeadMsgId bigint NOT NULL, " +
			"CarrierCode char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.value() + "', " + // S - SmtpMail, W - WebMail
			"MsgDirection char(1) NOT NULL, " + // R - Received, S - Sent
			"RuleName varchar(26) NOT NULL, " + // link to RuleLogic.RuleName
			"MsgSubject varchar(255), " +
			"MsgPriority varchar(10), " + // 1 (High)/2 (Normal)/3 (Low)
			"ReceivedTime datetime(3) NOT NULL, " +
			"FromAddrId bigint NOT NULL, " + // link to email_address
			"ReplyToAddrId bigint, " + // link to email_address
			"ToAddrId bigint NOT NULL, " + // link to email_address
			"ClientId varchar(16), " + // link to client_tbl - derived from OutMsgRefId
			"CustId varchar(16), " + // link to customer_tbl - derived from OutMsgRefId
			"PurgeDate Date, " +
			"UpdtTime datetime(3) NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"LockTime datetime(3), " +
			"LockId varchar(10), " +
			"ReadCount int NOT NULL DEFAULT 0, " + // how many times it's been read
			"ReplyCount int NOT NULL DEFAULT 0, " + // how many times it's been replied
			"ForwardCount int NOT NULL DEFAULT 0, " + // how many times it's been forwarded
			"Flagged char(1) NOT NULL DEFAULT '" + Constants.N + "', " +
			"DeliveryTime datetime(3), " + // for out-bound messages only, updated by MailSender
			"StatusId char(1) NOT NULL, " + // P - pending, D - delivered by MailSender, F - delivery failed, C/O - closed/Open (for received mail)
			"SmtpMessageId varchar(255), " + // SMTP message Id, updated by MailSender once delivered
			"RenderId bigint, " + // link to a msg_rendered record
			"OverrideTestAddr char(1), " + // Y - tell MailSender to use TO address even under test mode
			"AttachmentCount smallint NOT NULL DEFAULT 0, " + // for UI performance
			"AttachmentSize int NOT NULL DEFAULT 0, " + // for UI performance
			"MsgBodySize int NOT NULL DEFAULT 0, " + // for UI performance
			"MsgContentType varchar(100) NOT NULL, " +
			"BodyContentType varchar(50), " +
			"MsgBody mediumtext, " +
			"PRIMARY KEY (MsgId), " +
			"INDEX (LeadMsgId), " +
			"FOREIGN KEY (RenderId) REFERENCES msg_rendered (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"FOREIGN KEY (FromAddrId) REFERENCES email_address (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FromAddrId), " +
			"FOREIGN KEY (ToAddrId) REFERENCES email_address (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (ToAddrId)" +
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
			"MsgId bigint NOT NULL, " +
			"AttchmntDepth decimal(2) NOT NULL, " +
			"AttchmntSeq decimal(3) NOT NULL, " +
			"AttchmntName varchar(100), " +
			"AttchmntType varchar(100), " +
			"AttchmntDisp varchar(100), " +
			"AttchmntValue mediumblob, " +
			"FOREIGN KEY (MsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,AttchmntDepth,AttchmntSeq)" +
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
			"MsgId bigint NOT NULL, " +
			"AddrType varchar(7) NOT NULL, " + // from, replyto, to, cc, bcc
			"AddrSeq decimal(4) NOT NULL, " +
			"AddrValue varchar(255), " +
			"FOREIGN KEY (MsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,AddrType,AddrSeq)" +
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
			"MsgId bigint NOT NULL, " +
			"HeaderSeq decimal(4) NOT NULL, " +
			"HeaderName varchar(100), " +
			"HeaderValue text, " +
			"FOREIGN KEY (MsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,HeaderSeq)" +
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
			"MsgId bigint NOT NULL, " +
			"RfcType varchar(30) NOT NULL, " +
			"RfcStatus varchar(30), " +
			"RfcAction varchar(30), " +
			"FinalRcpt varchar(255), " +
			"FinalRcptId bigint, " +
			"OrigRcpt varchar(255), " +
			"OrigMsgSubject varchar(255), " +
			"MessageId varchar(255), " +
			"DsnText text, " +
			"DsnRfc822 text, " +
			"DlvrStatus text, " +
			"FOREIGN KEY (MsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"FOREIGN KEY (FinalRcptId) REFERENCES email_address (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FinalRcptId), " +
			"PRIMARY KEY (MsgId,RfcType)" +
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
			"MsgId bigint NOT NULL, " +
			"FromAddrId bigint, " +
			"ToAddrId bigint, " +
			"MsgSubject varchar(255), " +
			"AddTime datetime(3), " +
			"MsgStream mediumblob, " +
			"PRIMARY KEY (MsgId), " +
			"FOREIGN KEY (MsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE " +
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
			"MsgId bigint NOT NULL, " +
			"FinalRecipientId bigint NOT NULL, " +
			"FinalRecipient varchar(255), " +
			"OriginalRecipientId bigint, " +
			"MessageId varchar(255), " + // returned SMTP message id
			"DsnStatus varchar(50), " +
			"DsnReason varchar(255), " +
			"DsnText text, " +
			"DsnRfc822 text, " +
			"DeliveryStatus text, " +
			"AddTime datetime(3), " +
			"FOREIGN KEY (MsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"FOREIGN KEY (FinalRecipientId) REFERENCES email_address (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FinalRecipientId), " +
			"PRIMARY KEY (MsgId,FinalRecipientId)) ENGINE=InnoDB");
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
			"MsgId bigint NOT NULL, " +
			"MsgRefId bigint, " + // link to previous message thread
			"LeadMsgId bigint NOT NULL, " + // message that started this thread
			"ActionBo varchar(50) NOT NULL, " +
			"Parameters varchar(255), " +
			"AddTime datetime(3), " +
			"FOREIGN KEY (MsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			/* disable following foreign keys for performance reason */ 
			//"FOREIGN KEY (MsgRefId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			//"FOREIGN KEY (LeadMsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (LeadMsgId), " +
			"PRIMARY KEY (MsgId, MsgRefId) " + // use index to make MsgRefId nullable
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
			"MsgId bigint NOT NULL, " +
			"ListId varchar(8) NOT NULL, " +
			"DeliveryOption varchar(4) NOT NULL DEFAULT '" + MLDeliveryType.ALL_ON_LIST.value() + "', " +
			"SentCount int NOT NULL DEFAULT 0, " +
			"OpenCount int NOT NULL DEFAULT 0, " +
			"ClickCount int NOT NULL DEFAULT 0, " +
			"LastOpenTime datetime(3) DEFAULT NULL, " +
			"LastClickTime datetime(3) DEFAULT NULL, " +
			"StartTime datetime(3) DEFAULT NULL, " +
			"EndTime datetime(3) DEFAULT NULL, " +
			"UnsubscribeCount int NOT NULL DEFAULT 0, " +
			"ComplaintCount int NOT NULL DEFAULT 0, " +
			"ReferralCount int NOT NULL DEFAULT 0, " +
			"FOREIGN KEY (MsgId) REFERENCES msg_inbox (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"PRIMARY KEY (MsgId) " +
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
					+ "RowId int AUTO_INCREMENT not null, "
					+ "MsgId bigint NOT NULL, "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8), "
					+ "Comments varchar(500) NOT NULL, "
					+ "AddTime datetime(3) NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (MsgId) REFERENCES msg_click_count (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES email_address (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (MsgId), "
					+ "INDEX (EmailAddrId) "
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
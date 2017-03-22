package ltj.message.table;

import java.sql.SQLException;

import ltj.message.constant.CarrierCode;
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.main.CreateTableBase;

public class TemplateTables extends CreateTableBase {
	/**
	 * Creates a new instance of TemplateTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public TemplateTables() throws ClassNotFoundException, SQLException {
		init();
	}

	public void dropTables() {
		try {
			stm.execute("DROP TABLE msg_source");
			System.out.println("Dropped msg_source Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE template_variable");
			System.out.println("Dropped template_variable Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE client_variable");
			System.out.println("Dropped client_variable Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE global_variable");
			System.out.println("Dropped global_variable Table...");
		}
		
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE subj_template");
			System.out.println("Dropped subj_template Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE body_template");
			System.out.println("Dropped body_template Table...");
		}
		catch (SQLException e) {
		}
	}

	public void createTables() throws SQLException {
		createSubjTemplateTable();
		createBodyTemplateTable();
		createGlobalVariableTable();
		createClientVariableTable();
		createTemplateVariableTable();
		createMsgSourceTable();
	}
	
	public void loadTestData() throws SQLException {
		// dummy method to satisfy super class
	}
	
	void createSubjTemplateTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE subj_template ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "TemplateId varchar(16) NOT NULL, "
				+ "ClientId varchar(16), "
				+ "StartTime timestamp NOT NULL, "
				+ "Description varchar(100), "
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " // A - active, V - verification, I - inactive
				+ "TemplateValue varchar(255), "
				+ "PRIMARY KEY (RowId), "
				+ "FOREIGN KEY (ClientId) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (ClientId), "
				+ "UNIQUE INDEX (TemplateId,ClientId,StartTime)) ENGINE=InnoDB");
			System.out.println("Created subj_template Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createBodyTemplateTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE body_template ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "TemplateId varchar(16) NOT NULL, "
				+ "ClientId varchar(16), "
				+ "StartTime timestamp NOT NULL, "
				+ "Description varchar(100), "
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				+ "ContentType varchar(100) NOT NULL, " // content mime type
				+ "TemplateValue text, "
				+ "PRIMARY KEY (RowId), "
				+ "FOREIGN KEY (ClientId) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (ClientId), "
				+ "UNIQUE INDEX (TemplateId,ClientId,StartTime)) ENGINE=InnoDB");
			System.out.println("Created body_template Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createGlobalVariableTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE global_variable ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp(3) NOT NULL, "
				+ "VariableFormat varchar(50), " 
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - Xheader, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				// A - Active, I - Inactive
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// allow override value to be supplied at runtime, Y/N/M, M=Mandatory
				+ "Required char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// required to be present in body template
				+ "VariableValue varchar(255), "
				+ "PRIMARY KEY (RowId), "
				+ "INDEX (VariableName), "
				+ "UNIQUE INDEX (VariableName,StartTime)) ENGINE=InnoDB");
			System.out.println("Created global_variable Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createClientVariableTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE client_variable ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "ClientId varchar(16) NOT NULL, "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp(3) NOT NULL, "
				+ "VariableFormat varchar(50), "
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - Xheader, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// allow override value to be supplied at runtime
				+ "Required char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// required to present in body template
				+ "VariableValue text, "
				+ "PRIMARY KEY (RowId), "
				+ "FOREIGN KEY (ClientId) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (ClientId), "
				+ "INDEX (VariableName), "
				+ "UNIQUE INDEX (ClientId,VariableName,StartTime)) ENGINE=InnoDB");
			System.out.println("Created client_variable Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createTemplateVariableTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE template_variable ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "TemplateId varchar(16) NOT NULL, "
				+ "ClientId varchar(16), "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp(3) NOT NULL, "
				+ "VariableFormat varchar(50), "
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - X header, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// allow override value to be supplied at runtime
				+ "Required char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// required to present in body template
				+ "VariableValue text, "
				+ "PRIMARY KEY (RowId), "
				+ "INDEX (VariableName), "
				+ "FOREIGN KEY (ClientId) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (ClientId), "
				+ "UNIQUE INDEX (TemplateId,ClientId,VariableName,StartTime)"
				+ ") ENGINE=InnoDB");
			System.out.println("Created template_variable Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgSourceTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_source ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "MsgSourceId varchar(16) NOT NULL, "
				+ "Description varchar(100), "
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				+ "FromAddrId bigint NOT NULL, "
				+ "ReplyToAddrId bigint, "
				+ "SubjTemplateId varchar(16) NOT NULL, "
				+ "BodyTemplateId varchar(16) NOT NULL, "
				+ "TemplateVariableId varchar(16), "
				+ "ExcludingIdToken char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// Y - No email id will be embedded into message
				+ "CarrierCode char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.value() + "', "
				// Internet, WebMail, Internal Routing, ...
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// allow override templates, addrs to be supplied at runtime
				+ "SaveMsgStream char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// Y - save rendered smtp message stream to msg_stream
				+ "ArchiveInd char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// Y - archive the rendered messages
				+ "PurgeAfter int, " // in month
				+ "UpdtTime datetime(3) NOT NULL, "
				+ "UpdtUserId varchar(10) NOT NULL, "
				+ "PRIMARY KEY (RowId), "
				+ "UNIQUE INDEX (MsgSourceId), "
				+ "FOREIGN KEY (FromAddrId) REFERENCES email_address (EmailAddrId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (FromAddrId), "
				+ "FOREIGN KEY (ReplyToAddrId) REFERENCES email_address (EmailAddrId) ON DELETE SET NULL ON UPDATE CASCADE, "
				+ "INDEX (ReplyToAddrId), "
				+ "FOREIGN KEY (TemplateVariableId) REFERENCES template_variable (TemplateId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (TemplateVariableId), "
				+ "FOREIGN KEY (SubjTemplateId) REFERENCES subj_template (TemplateId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (SubjTemplateId), "
				+ "FOREIGN KEY (BodyTemplateId) REFERENCES body_template (TemplateId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (BodyTemplateId) "
				+ ") ENGINE=InnoDB");
			System.out.println("Created msg_source Table...");
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
			TemplateTables ct = new TemplateTables();
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
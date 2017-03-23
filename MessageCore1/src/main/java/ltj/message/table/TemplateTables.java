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
				+ "row_id int AUTO_INCREMENT not null, "
				+ "template_id varchar(16) NOT NULL, "
				+ "client_id varchar(16), "
				+ "start_time timestamp NOT NULL, "
				+ "description varchar(100), "
				+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " // A - active, V - verification, I - inactive
				+ "template_value varchar(255), "
				+ "PRIMARY KEY (row_id), "
				+ "FOREIGN KEY (client_id) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (client_id), "
				+ "UNIQUE INDEX (template_id,client_id,start_time)) ENGINE=InnoDB");
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
				+ "row_id int AUTO_INCREMENT not null, "
				+ "template_id varchar(16) NOT NULL, "
				+ "client_id varchar(16), "
				+ "start_time timestamp NOT NULL, "
				+ "description varchar(100), "
				+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				+ "content_type varchar(100) NOT NULL, " // content mime type
				+ "template_value text, "
				+ "PRIMARY KEY (row_id), "
				+ "FOREIGN KEY (client_id) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (client_id), "
				+ "UNIQUE INDEX (template_id,client_id,start_time)) ENGINE=InnoDB");
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
				+ "row_id int AUTO_INCREMENT not null, "
				+ "variable_name varchar(26) NOT NULL, "
				+ "start_time timestamp(3) NOT NULL, "
				+ "variable_format varchar(50), " 
				+ "variable_type char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - Xheader, L - LOB(Attachment)
				+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				// A - Active, I - Inactive
				+ "allow_override char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// allow override value to be supplied at runtime, Y/N/M, M=Mandatory
				+ "required char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// required to be present in body template
				+ "variable_value varchar(255), "
				+ "PRIMARY KEY (row_id), "
				+ "INDEX (variable_name), "
				+ "UNIQUE INDEX (variable_name,start_time)) ENGINE=InnoDB");
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
				+ "row_id int AUTO_INCREMENT not null, "
				+ "client_id varchar(16) NOT NULL, "
				+ "variable_name varchar(26) NOT NULL, "
				+ "start_time timestamp(3) NOT NULL, "
				+ "variable_format varchar(50), "
				+ "variable_type char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - Xheader, L - LOB(Attachment)
				+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				+ "allow_override char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// allow override value to be supplied at runtime
				+ "required char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// required to present in body template
				+ "variable_value text, "
				+ "PRIMARY KEY (row_id), "
				+ "FOREIGN KEY (client_id) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (client_id), "
				+ "INDEX (variable_name), "
				+ "UNIQUE INDEX (client_id,variable_name,start_time)) ENGINE=InnoDB");
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
				+ "row_id int AUTO_INCREMENT not null, "
				+ "template_id varchar(16) NOT NULL, "
				+ "client_id varchar(16), "
				+ "variable_name varchar(26) NOT NULL, "
				+ "start_time timestamp(3) NOT NULL, "
				+ "variable_format varchar(50), "
				+ "variable_type char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - X header, L - LOB(Attachment)
				+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				+ "allow_override char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// allow override value to be supplied at runtime
				+ "required char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// required to present in body template
				+ "variable_value text, "
				+ "PRIMARY KEY (row_id), "
				+ "INDEX (variable_name), "
				+ "FOREIGN KEY (client_id) REFERENCES client_tbl (client_id) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (client_id), "
				+ "UNIQUE INDEX (template_id,client_id,variable_name,start_time)"
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
				+ "row_id int AUTO_INCREMENT not null, "
				+ "msg_source_id varchar(16) NOT NULL, "
				+ "description varchar(100), "
				+ "status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', "
				+ "from_addr_id bigint NOT NULL, "
				+ "reply_to_addr_id bigint, "
				+ "subj_template_id varchar(16) NOT NULL, "
				+ "body_template_id varchar(16) NOT NULL, "
				+ "template_variable_id varchar(16), "
				+ "excluding_id_token char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// Y - No email id will be embedded into message
				+ "carrier_code char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.value() + "', "
				// Internet, WebMail, Internal Routing, ...
				+ "allow_override char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// allow override templates, addrs to be supplied at runtime
				+ "save_msg_stream char(1) NOT NULL DEFAULT '" + Constants.Y + "', "
				// Y - save rendered smtp message stream to msg_stream
				+ "archive_ind char(1) NOT NULL DEFAULT '" + Constants.N + "', "
				// Y - archive the rendered messages
				+ "purge_after int, " // in month
				+ "updt_time datetime(3) NOT NULL, "
				+ "updt_user_id varchar(10) NOT NULL, "
				+ "PRIMARY KEY (row_id), "
				+ "UNIQUE INDEX (msg_source_id), "
				+ "FOREIGN KEY (from_addr_id) REFERENCES email_address (email_addr_id) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (from_addr_id), "
				+ "FOREIGN KEY (reply_to_addr_id) REFERENCES email_address (email_addr_id) ON DELETE SET NULL ON UPDATE CASCADE, "
				+ "INDEX (reply_to_addr_id), "
				+ "FOREIGN KEY (template_variable_id) REFERENCES template_variable (template_id) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (template_variable_id), "
				+ "FOREIGN KEY (subj_template_id) REFERENCES subj_template (template_id) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (subj_template_id), "
				+ "FOREIGN KEY (body_template_id) REFERENCES body_template (template_id) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (body_template_id) "
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
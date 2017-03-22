package ltj.message.table;

import java.sql.SQLException;

import ltj.message.constant.StatusId;
import ltj.message.main.CreateTableBase;

/**
 * Dependency: RuleBean - this program runs RuleTable first before create its
 * own tables.
 */
public class ActionTables extends CreateTableBase {
	RuleTables ruleTables;
	
	/**
	 * Creates a new instance of ActionTables
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public ActionTables() throws SQLException, ClassNotFoundException {
		ruleTables = new RuleTables();
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE msg_action");
			System.out.println("Dropped msg_action Table...");
		} catch (SQLException e) {}
		try {
			stm.execute("DROP TABLE msg_action_detail");
			System.out.println("Dropped msg_action_detail Table...");
		} catch (SQLException e) {}
		try {
			stm.execute("DROP TABLE msg_data_type");
			System.out.println("Dropped msg_data_type Table...");
		} catch (SQLException e) {}
		
		ruleTables.dropTables();
	}
	
	public void createTables() throws SQLException {
		ruleTables.createTables();
		createActionDataTypeTable();
		createActionDetailTable();
		createActionTable();
	}
	
	public void loadTestData() throws SQLException {
		ruleTables.loadTestData();
	}
	
	void createActionDataTypeTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_data_type ( " +
			"row_id int AUTO_INCREMENT not null, " +
			"data_type varchar(16) NOT NULL, " +
			"data_type_value varchar(100) NOT NULL, " +
			"misc_properties varchar(255), " +
			"PRIMARY KEY (row_id), " +
			"UNIQUE INDEX (data_type, data_type_value) " +
			") ENGINE=InnoDB");
			System.out.println("Created msg_data_type Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createActionDetailTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_action_detail ( " +
			"row_id int AUTO_INCREMENT not null, " +
			"action_id varchar(16) NOT NULL, " +
			"description varchar(100), " +
			"process_bean_id varchar(50) NOT NULL, " +
			"process_class_name varchar(100), " +
			"data_type varchar(16), " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id varchar(10) NOT NULL, " +
			"INDEX (data_type), " +
			"FOREIGN KEY (data_type) REFERENCES msg_data_type (data_type) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (action_id), " +
			"PRIMARY KEY (row_id) " +
			") ENGINE=InnoDB");
			System.out.println("Created msg_action_detail Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createActionTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE msg_action ( " +
			"row_id int AUTO_INCREMENT not null, " +
			"rule_name varchar(26) NOT NULL, " +
			"action_seq int NOT NULL, " +
			"start_time datetime(3) NOT NULL, " +
			"client_id varchar(16), " + 
			"status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " +
			"action_id varchar(16) NOT NULL, " +
			"data_type_values text, " + // maximum size of 65,535, to accommodate template text
			"PRIMARY KEY (row_id), " +
			"FOREIGN KEY (rule_name) REFERENCES rule_logic (rule_name) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX(rule_name), " +
			"FOREIGN KEY (action_id) REFERENCES msg_action_detail (action_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (rule_name, action_seq, start_time, client_id) " +
			") ENGINE=InnoDB");
			System.out.println("Created msg_action Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			ActionTables ct = new ActionTables();
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
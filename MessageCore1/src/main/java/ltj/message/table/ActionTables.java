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
			stm.execute("DROP TABLE MsgAction");
			System.out.println("Dropped MsgAction Table...");
		} catch (SQLException e) {}
		try {
			stm.execute("DROP TABLE MsgActionDetail");
			System.out.println("Dropped MsgActionDetail Table...");
		} catch (SQLException e) {}
		try {
			stm.execute("DROP TABLE MsgDataType");
			System.out.println("Dropped MsgDataType Table...");
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
			stm.execute("CREATE TABLE MsgDataType ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"DataType varchar(16) NOT NULL, " +
			"DataTypeValue varchar(100) NOT NULL, " +
			"MiscProperties varchar(255), " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (DataType, DataTypeValue) " +
			") ENGINE=InnoDB");
			System.out.println("Created MsgDataType Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createActionDetailTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MsgActionDetail ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ActionId varchar(16) NOT NULL, " +
			"Description varchar(100), " +
			"ProcessBeanId varchar(50) NOT NULL, " +
			"ProcessClassName varchar(100), " +
			"DataType varchar(16), " +
			"UpdtTime datetime(3) NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"INDEX (DataType), " +
			"FOREIGN KEY (DataType) REFERENCES MsgDataType (DataType) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (ActionId), " +
			"PRIMARY KEY (RowId) " +
			") ENGINE=InnoDB");
			System.out.println("Created MsgActionDetail Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createActionTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MsgAction ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"ActionSeq int NOT NULL, " +
			"StartTime datetime(3) NOT NULL, " +
			"ClientId varchar(16), " + 
			"StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " +
			"ActionId varchar(16) NOT NULL, " +
			"DataTypeValues text, " + // maximum size of 65,535, to accommodate template text
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RuleLogic (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX(RuleName), " +
			"FOREIGN KEY (ActionId) REFERENCES MsgActionDetail (ActionId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (RuleName, ActionSeq, StartTime, ClientId) " +
			") ENGINE=InnoDB");
			System.out.println("Created MsgAction Table...");
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
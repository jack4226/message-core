package ltj.message.table;

/**
 * The RuleBean referenced by ActionBean with a FOREIGN key restraint.
 */
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.data.preload.RuleElementEnum;
import ltj.data.preload.RuleNameEnum;
import ltj.data.preload.RuleSubruleMapEnum;
import ltj.message.bo.rule.RuleBase;
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.main.CreateTableBase;

public class RuleTables extends CreateTableBase {
	/**
	 * Creates a new instance of RuleTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public RuleTables() throws ClassNotFoundException, SQLException {
		init();
	}

	public void dropTables() {
		try {
			stm.execute("DROP TABLE RuleSubruleMap");
			System.out.println("Dropped RuleSubruleMap Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE RuleElement");
			System.out.println("Dropped RuleElement Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE RuleLogic");
			System.out.println("Dropped RuleLogic Table...");
		}
		catch (SQLException e) {
		}
	}
	
	public void createTables() throws SQLException {
		createRuleLogicTable();
		createRuleElementTable();
		createRuleSubRuleMapTable();
	}
	
	public void loadTestData() throws SQLException {
		insertRuleLogicData();
		insertRuleElementData();
		insertRuleSubRuleMapData();
	}
	
	void createRuleLogicTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RuleLogic ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"RuleSeq int NOT NULL, " +
			"RuleType varchar(8) NOT NULL, " + // simple/or/and/none
			"StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " +
			"StartTime datetime(3) NOT NULL, " +
			"MailType varchar(8) NOT NULL, " + // smtpmail, webmail, ...
			"RuleCategory char(1) DEFAULT '" + RuleBase.MAIN_RULE + "', " + // E - Pre Scan, 'M' - Main Rule, P - Post Scan
			"IsSubRule char(1) NOT NULL DEFAULT '" + Constants.N + "', " +
			"builtInRule char(1) NOT NULL DEFAULT '" + Constants.N + "', " +
			"Description varchar(255), " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (RuleName, RuleSeq) " + // use index to allow update to rule name
			") ENGINE=InnoDB");
			System.out.println("Created RuleLogic Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createRuleElementTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RuleElement ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"ElementSeq int NOT NULL, " +
			"DataName varchar(26) NOT NULL, " +
			"HeaderName varchar(50), " + // X-Header name if DataName is X-Header
			"Criteria varchar(16) NOT NULL, " +
			"CaseSensitive char(1) NOT NULL DEFAULT '" + Constants.N + "', " + // Y/N
			"TargetText varchar(2000), " + 
			"TargetProc varchar(100), " +
			"Exclusions text, " + // delimited
			"ExclListProc varchar(100), " + // valid bean id
			"Delimiter char(5) DEFAULT ',', " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RuleLogic (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX(RuleName), " +
			"UNIQUE INDEX (RuleName, ElementSeq) " +
			") ENGINE=InnoDB");
			System.out.println("Created RuleElement Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createRuleSubRuleMapTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RuleSubruleMap ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"SubRuleName varchar(26) NOT NULL, " +
			"SubRuleSeq int NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RuleLogic (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"FOREIGN KEY (SubRuleName) REFERENCES RuleLogic (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (RuleName, SubRuleName) " +
			") ENGINE=InnoDB");
			System.out.println("Created RuleSubruleMap Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertRuleLogicData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO RuleLogic (" +
					"RuleName, " +
					"RuleSeq, " +
					"RuleType, " + 
					"StatusId, " +
					"StartTime, " +
					"MailType, " + 
					"RuleCategory, " + 
					"IsSubRule, " +
					"builtInRule, " +
					"Description) " +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			int seq = 100;
			for (RuleNameEnum rl : RuleNameEnum.getBuiltinRules()) {
				ps.setString(1, rl.name());
				ps.setInt(2, ++seq);
				ps.setString(3, rl.getRuleType().value());
				ps.setString(4, StatusId.ACTIVE.value());
				ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
				ps.setString(6, Constants.SMTP_MAIL);
				ps.setString(7, rl.getRuleCategory().value());
				ps.setString(8, rl.isSubrule() ? Constants.Y : Constants.N); // sub rule?
				ps.setString(9, rl.isBuiltin() ? Constants.Y : Constants.N); // built-in rule?
				ps.setString(10, rl.getDescription());
				ps.execute();
			}
 			
			seq = 200;
			for (RuleNameEnum rl : RuleNameEnum.getCustomRules()) {
				ps.setString(1, rl.name());
				if (rl.equals(RuleNameEnum.Unattended_Mailbox)) {
					ps.setInt(2, 0);
				}
				else {
					ps.setInt(2, ++seq);
				}
				ps.setString(3, rl.getRuleType().value());
				ps.setString(4, StatusId.ACTIVE.value());
				ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
				ps.setString(6, Constants.SMTP_MAIL);
				ps.setString(7, rl.getRuleCategory().value());
				ps.setString(8, rl.isSubrule() ? Constants.Y : Constants.N); // sub rule?
				ps.setString(9, rl.isBuiltin() ? Constants.Y : Constants.N); // built-in rule?
				ps.setString(10, rl.getDescription());
				ps.execute();
			}
 			
			seq = 225;
			for (RuleNameEnum rl : RuleNameEnum.getSubRules()) {
				ps.setString(1, rl.name());
				if (rl.equals(RuleNameEnum.Unattended_Mailbox)) {
					ps.setInt(2, 0);
				}
				else {
					ps.setInt(2, ++seq);
				}
				ps.setString(3, rl.getRuleType().value());
				ps.setString(4, StatusId.ACTIVE.value());
				ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
				ps.setString(6, Constants.SMTP_MAIL);
				ps.setString(7, rl.getRuleCategory().value());
				ps.setString(8, rl.isSubrule() ? Constants.Y : Constants.N); // sub rule?
				ps.setString(9, rl.isBuiltin() ? Constants.Y : Constants.N); // built-in rule?
				ps.setString(10, rl.getDescription());
				ps.execute();
			}

			ps.close();
			System.out.println("Inserted all RuleLogic rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertRuleElementData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO RuleElement (" +
					"RuleName, " +
					"ElementSeq , " +
					"DataName, " +
					"HeaderName, " + 
					"Criteria, " +
					"CaseSensitive, " + 
					"TargetText, " + 
					"TargetProc, " +
					"Exclusions, " + 
					"ExclListProc, " + 
					"Delimiter) " +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			

			for (RuleElementEnum re : RuleElementEnum.values()) {
				ps.setString(1, re.getRuleName().name());
				ps.setInt(2, re.getRuleSequence());
				ps.setString(3, re.getRuleDataName().getValue());
				ps.setString(4, re.getXheaderNameEnum() == null ? null : re.getXheaderNameEnum().value());
				ps.setString(5, re.getRuleCriteria().value());
				ps.setString(6, re.isCaseSensitive() ? Constants.Y : Constants.N);
				ps.setString(7, re.getTargetText());
				ps.setString(8, re.getTargetProcName());
				ps.setString(9, re.getExclusions());
				ps.setString(10, re.getExclListProcName());
				ps.setString(11, re.getDelimiter());
				ps.execute();
			}
			
			ps.close();
			System.out.println("Inserted all RuleElement rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void insertRuleSubRuleMapData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO RuleSubruleMap (" +
					"RuleName, " +
					"SubRuleName, " +
					"SubRuleSeq) " +
				" VALUES (?, ?, ?)");
			
			for (RuleSubruleMapEnum sub : RuleSubruleMapEnum.values()) {
				ps.setString(1, sub.getRuleName().name());
				ps.setString(2, sub.getSubruleName().name());
				ps.setInt(3, sub.getSequence());
				ps.execute();
			}
			
			ps.close();
			System.out.println("Inserted all RuleSubRuleMap rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param args the command line arguments
	 */
//	public static void main(String[] args) {
//		try {
//			RuleTables ct = new RuleTables();
//			ct.dropTables();
//			ct.createTables();
//			ct.insertData();
//			ct.wrapup();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
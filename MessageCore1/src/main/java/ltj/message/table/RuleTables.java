package ltj.message.table;

/**
 * The RuleBean referenced by ActionBean with a FOREIGN key restraint.
 */
import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.data.preload.RuleElementEnum;
import ltj.data.preload.RuleNameEnum;
import ltj.data.preload.RuleSubruleMapEnum;
import ltj.message.bo.rule.RuleBase;
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.rule.RuleElementDao;
import ltj.message.dao.rule.RuleLogicDao;
import ltj.message.dao.rule.RuleSubRuleMapDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.rule.RuleElementVo;
import ltj.message.vo.rule.RuleLogicVo;
import ltj.message.vo.rule.RuleSubRuleMapVo;
import ltj.spring.util.SpringUtil;

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
			stm.execute("DROP TABLE rule_subrule_map");
			System.out.println("Dropped rule_subrule_map Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE rule_element");
			System.out.println("Dropped rule_element Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE rule_logic");
			System.out.println("Dropped rule_logic Table...");
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
			stm.execute("CREATE TABLE rule_logic ( " +
			"row_id int AUTO_INCREMENT not null, " +
			"rule_name varchar(26) NOT NULL, " +
			"rule_seq int NOT NULL, " +
			"rule_type varchar(8) NOT NULL, " + // simple/or/and/none
			"status_id char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.value() + "', " +
			"start_time datetime(3) NOT NULL, " +
			"mail_type varchar(8) NOT NULL, " + // smtpmail, webmail, ...
			"rule_category char(1) DEFAULT '" + RuleBase.MAIN_RULE + "', " + // E - Pre Scan, 'M' - Main Rule, P - Post Scan
			"sub_rule boolean NOT NULL DEFAULT false, " +
			"built_in_rule boolean NOT NULL DEFAULT false, " +
			"description varchar(255), " +
			"CONSTRAINT rule_logic_pkey PRIMARY KEY (row_id), " +
			"UNIQUE INDEX rule_logic_ix_rulnm (rule_name) " + // use index to allow update to rule name
			") ENGINE=InnoDB");
			System.out.println("Created rule_logic Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createRuleElementTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE rule_element ( " +
			"row_id int AUTO_INCREMENT not null, " +
			"rule_name varchar(26) NOT NULL, " +
			"element_seq int NOT NULL, " +
			"data_name varchar(26) NOT NULL, " +
			"header_name varchar(50), " + // X-Header name if DataName is X-Header
			"criteria varchar(16) NOT NULL, " +
			"case_sensitive boolean NOT NULL DEFAULT false, " +
			"target_text varchar(2000), " + 
			"target_proc varchar(100), " +
			"exclusions text, " + // delimited
			"excl_list_proc varchar(100), " + // valid bean id
			"delimiter char(5) DEFAULT ',', " +
			"CONSTRAINT rule_element_pkey PRIMARY KEY (row_id), " +
			"FOREIGN KEY rule_element_fk_rule_name (rule_name) REFERENCES rule_logic (rule_name) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX rule_element_ix_rule_name (rule_name), " +
			"UNIQUE INDEX rule_element_ix_rulnm_elmsq (rule_name, element_seq) " +
			") ENGINE=InnoDB");
			System.out.println("Created rule_element Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createRuleSubRuleMapTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE rule_subrule_map ( " +
			"row_id int AUTO_INCREMENT not null, " +
			"rule_name varchar(26) NOT NULL, " +
			"sub_rule_name varchar(26) NOT NULL, " +
			"sub_rule_seq int NOT NULL, " +
			"CONSTRAINT rul_subrule_map_pkey PRIMARY KEY (row_id), " +
			"FOREIGN KEY rule_subrule_map_fk_rule_name (rule_name) REFERENCES rule_logic (rule_name) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX rule_subrule_map_ix_rule_name (rule_name), " +
			"FOREIGN KEY rule_subrule_map_fk_sub_rule_name (sub_rule_name) REFERENCES rule_logic (rule_name) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX rule_subrule_map_ix_sub_rule_name (sub_rule_name), " +
			"UNIQUE INDEX rule_subrule_map_ix_rulnm_subnm (rule_name, sub_rule_name) " +
			") ENGINE=InnoDB");
			System.out.println("Created rule_subrule_map Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertRuleLogicData() throws SQLException {
		RuleLogicDao dao = SpringUtil.getDaoAppContext().getBean(RuleLogicDao.class);
		try {
			int rows = 0;
			int seq = 100;
			for (RuleNameEnum rl : RuleNameEnum.getBuiltinRules()) {
				RuleLogicVo vo = new RuleLogicVo();
				
				vo.setRuleName(rl.name());
				vo.setRuleSeq(++seq);
				vo.setRuleType(rl.getRuleType().value());
				vo.setStatusId(StatusId.ACTIVE.value());
				vo.setStartTime(new Timestamp(System.currentTimeMillis()));
				vo.setMailType(Constants.SMTP_MAIL);
				vo.setRuleCategory(rl.getRuleCategory().value());
				vo.setSubRule(rl.isSubrule()); // sub rule?
				vo.setBuiltInRule(rl.isBuiltin()); // built-in rule?
				vo.setDescription(rl.getDescription());
				
				rows += dao.insert(vo);
			}
 			
			seq = 200;
			for (RuleNameEnum rl : RuleNameEnum.getCustomRules()) {
				RuleLogicVo vo = new RuleLogicVo();
				
				vo.setRuleName(rl.name());
				if (rl.equals(RuleNameEnum.Unattended_Mailbox)) {
					vo.setRuleSeq(0);
				}
				else {
					vo.setRuleSeq(++seq);
				}
				vo.setRuleType(rl.getRuleType().value());
				vo.setStatusId(StatusId.ACTIVE.value());
				vo.setStartTime(new Timestamp(System.currentTimeMillis()));
				vo.setMailType(Constants.SMTP_MAIL);
				vo.setRuleCategory(rl.getRuleCategory().value());
				vo.setSubRule(rl.isSubrule()); // sub rule?
				vo.setBuiltInRule(rl.isBuiltin()); // built-in rule?
				vo.setDescription(rl.getDescription());
				
				rows += dao.insert(vo);
			}
 			
			seq = 225;
			for (RuleNameEnum rl : RuleNameEnum.getSubRules()) {
				RuleLogicVo vo = new RuleLogicVo();
				
				vo.setRuleName(rl.name());
				if (rl.equals(RuleNameEnum.Unattended_Mailbox)) {
					vo.setRuleSeq(0);
				}
				else {
					vo.setRuleSeq(++seq);
				}
				vo.setRuleType(rl.getRuleType().value());
				vo.setStatusId(StatusId.ACTIVE.value());
				vo.setStartTime(new Timestamp(System.currentTimeMillis()));
				vo.setMailType(Constants.SMTP_MAIL);
				vo.setRuleCategory(rl.getRuleCategory().value());
				vo.setSubRule(rl.isSubrule()); // sub rule?
				vo.setBuiltInRule(rl.isBuiltin()); // built-in rule?
				vo.setDescription(rl.getDescription());
				
				rows += dao.insert(vo);
			}

			System.out.println("Inserted all rule_logic rows: " + rows);
		}
		catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertRuleElementData() throws SQLException {
		RuleElementDao dao = SpringUtil.getDaoAppContext().getBean(RuleElementDao.class);
		try {
			int rows = 0;
			for (RuleElementEnum re : RuleElementEnum.values()) {
				RuleElementVo vo = new RuleElementVo();
				
				vo.setRuleName(re.getRuleName().name());
				vo.setElementSeq(re.getRuleSequence());
				vo.setDataName(re.getRuleDataName().getValue());
				vo.setHeaderName(re.getXheaderNameEnum() == null ? null : re.getXheaderNameEnum().value());
				vo.setCriteria(re.getRuleCriteria().value());
				vo.setCaseSensitive(re.isCaseSensitive());
				vo.setTargetText(re.getTargetText());
				vo.setTargetProc(re.getTargetProcName());
				vo.setExclusions(re.getExclusions());
				vo.setExclListProc(re.getExclListProcName());
				vo.setDelimiter(re.getDelimiter());
				
				dao.insert(vo);
			}
			
			System.out.println("Inserted all rule_element rows: " + rows);
		}
		catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void insertRuleSubRuleMapData() throws SQLException {
		RuleSubRuleMapDao dao = SpringUtil.getDaoAppContext().getBean(RuleSubRuleMapDao.class);
		try {
			int rows = 0;
			for (RuleSubruleMapEnum sub : RuleSubruleMapEnum.values()) {
				RuleSubRuleMapVo vo = new RuleSubRuleMapVo();
				vo.setRuleName(sub.getRuleName().name());
				vo.setSubRuleName(sub.getSubruleName().name());
				vo.setSubRuleSeq(sub.getSequence());
				rows += dao.insert(vo);
			}
			System.out.println("Inserted all rule_subrule_map rows: " + rows);
		}
		catch (Exception e) {
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
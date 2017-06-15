package ltj.message.main;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;

import ltj.message.dao.abstrct.MetaDataUtil;
import ltj.message.table.ActionTables;
import ltj.message.table.ClientTable;
import ltj.message.table.CustomerTable;
import ltj.message.table.EmailAddrTable;
import ltj.message.table.IdTokensTable;
import ltj.message.table.InboxTables;
import ltj.message.table.LoadActionTables;
import ltj.message.table.LoadInboxTables;
import ltj.message.table.LoadTemplateTables;
import ltj.message.table.MailboxTable;
import ltj.message.table.SmtpTable;
import ltj.message.table.SocketServerTable;
import ltj.message.table.TemplateTables;
import ltj.message.table.TimerTable;
import ltj.message.table.UserTable;

public class CreateAllTables {

	ClientTable clientTable;
	EmailAddrTable emailAddrTable;
	CustomerTable customerTable;
	//RuleTables ruleTables;
	ActionTables actionTables;
	TemplateTables templateTables;
	InboxTables inboxTables;
	IdTokensTable idTokensTable;
	
	LoadActionTables loadActionTables;
	LoadInboxTables loadInboxTables;
	LoadTemplateTables loadTemplateTables;
	
	MailboxTable mailboxTable;
	SmtpTable smtpTable;
	TimerTable timerTable;
	SocketServerTable socketTable;
	UserTable userTable;
	
	public static void main(String[] args) {
		CreateAllTables create = new CreateAllTables();
		create.createTablesAndLoadData();
	}
	
	public void createTablesAndLoadData() {
		try {
			init();
			dropAllTables();
			createAllTables();
			wrapup();
			/*** Now run following code to complete data load ***/
			// RuleEngineTest - to create a record on MsgStream
			// MsgOutboxBoTest - to create a record on MsgRendered
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void init() throws SQLException, ClassNotFoundException {
		clientTable = new ClientTable();
		emailAddrTable = new EmailAddrTable();
		customerTable = new CustomerTable();
		//ruleTables = new RuleTables();
		actionTables = new ActionTables();
		templateTables = new TemplateTables();
		inboxTables = new InboxTables();
		idTokensTable = new IdTokensTable();
		
		loadActionTables = new LoadActionTables();
		loadInboxTables = new LoadInboxTables();
		loadTemplateTables = new LoadTemplateTables();
		
		mailboxTable = new MailboxTable();
		smtpTable = new SmtpTable();
		timerTable = new TimerTable();
		socketTable = new SocketServerTable();
		userTable = new UserTable();
	}
	
	void dropAllTables() {
		userTable.dropTables();
		socketTable.dropTables();
		timerTable.dropTables();
		smtpTable.dropTables();
		mailboxTable.dropTables();
		idTokensTable.dropTables();
		inboxTables.dropTables();
		templateTables.dropTables();
		actionTables.dropTables();
		//ruleTables.dropTables();
		customerTable.dropTables();
		emailAddrTable.dropTables();
		clientTable.dropTables();
	}
	
	void createAllTables() throws SQLException {
		createAllTables(null);
	}
	
	void createAllTables(ApplicationContext ctx) throws SQLException {
		clientTable.createTables();
		clientTable.loadTestData();
		
		emailAddrTable.createTables();
		emailAddrTable.loadTestData();
		
		customerTable.createTables();
		customerTable.loadTestData();
		
		//ruleTables.createTables();
		//ruleTables.loadTestData();
		
		actionTables.createTables();
		actionTables.loadTestData();
		loadActionTables.loadData(ctx);
		
		templateTables.createTables();
		MetaDataUtil.reloadMetaData();
		loadTemplateTables.loadData(ctx);
		
		inboxTables.createTables();
		MetaDataUtil.reloadMetaData();
		loadInboxTables.loadData(ctx);
		
		idTokensTable.createTables();
		idTokensTable.loadTestData();
		
		mailboxTable.createTables();
		mailboxTable.loadTestData();
		
		smtpTable.createTables();
		smtpTable.loadTestData();
		
		timerTable.createTables();
		timerTable.loadTestData();
		
		socketTable.createTables();
		socketTable.loadReleaseData();
		
		userTable.createTables();
		userTable.loadTestData();
		
		// build ClientVariable records
		clientTable.updateAllClients();
		// build MsgDataType records for TemplateId data type
		emailAddrTable.updateTemplates();
		// insert mailboxes to EmailAddr table
		mailboxTable.updateEmailAddrTable();
	}
	
	void wrapup() {
		clientTable.wrapup();
		emailAddrTable.wrapup();
		customerTable.wrapup();
		//ruleTables.wrapup();
		actionTables.wrapup();
		templateTables.wrapup();
		inboxTables.wrapup();
		idTokensTable.wrapup();
		
		mailboxTable.wrapup();
		smtpTable.wrapup();
		timerTable.wrapup();
		socketTable.wrapup();
		userTable.wrapup();
	}
}

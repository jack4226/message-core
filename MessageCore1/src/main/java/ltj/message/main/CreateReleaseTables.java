package ltj.message.main;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;

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
import ltj.message.table.TemplateTables;
import ltj.message.table.TimerTable;
import ltj.message.table.UserTable;

public class CreateReleaseTables {

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
	UserTable userTable;
	
	public static void main(String[] args) {
		try {
			CreateReleaseTables create = new CreateReleaseTables();
			create.init();
			create.dropAllTables();
			create.createAllTables();
			create.wrapup();
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
		userTable = new UserTable();
	}
	
	void dropAllTables() {
		userTable.dropTables();
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
		clientTable.loadReleaseData();
		
		emailAddrTable.createTables();
		emailAddrTable.loadReleaseData();
		
		customerTable.createTables();
		customerTable.loadReleaseData();
		
		//ruleTables.createTables();
		//ruleTables.loadReleaseData();
		
		actionTables.createTables();
		actionTables.loadReleaseData();
		loadActionTables.loadData(ctx);
		
		templateTables.createTables();
		loadTemplateTables.loadData(ctx);
		
		inboxTables.createTables();
		loadInboxTables.loadData(ctx);
		
		idTokensTable.createTables();
		idTokensTable.loadReleaseData();
		
		mailboxTable.createTables();
		mailboxTable.loadReleaseData();
		
		smtpTable.createTables();
		smtpTable.loadReleaseData();
		
		timerTable.createTables();
		timerTable.loadReleaseData();
		
		userTable.createTables();
		userTable.loadReleaseData();
		
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
		userTable.wrapup();
	}
}

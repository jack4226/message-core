package ltj.message.table;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.message.constant.Constants;
import ltj.message.constant.EmailIdToken;
import ltj.message.main.CreateTableBase;
public class IdTokensTable extends CreateTableBase {
	/**
	 * Creates a new instance of IdTokenTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public IdTokensTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE IdTokens");
			System.out.println("Dropped IdTokens Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException {
		try {
			stm.execute("CREATE TABLE IdTokens ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ClientId varchar(16) NOT NULL, " + 
			"Description varchar(100), " +
			"BodyBeginToken varchar(16) NOT NULL, " +
			"BodyEndToken varchar(4) NOT NULL, " +
			"XHeaderName varchar(20), " +
			"XhdrBeginToken varchar(16), " +
			"XhdrEndToken varchar(4), " +
			"MaxLength integer NOT NULL, " +
			"UpdtTime datetime(3) NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"FOREIGN KEY (ClientId) REFERENCES Clients(ClientId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (ClientId) " +
			") ENGINE=InnoDB");
			System.out.println("Created IdTokens Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO IdTokens " +
				"(ClientId," +
				"Description," +
				"BodyBeginToken," +
				"BodyEndToken," +
				"XHeaderName," +
				"XhdrBeginToken," +
				"XhdrEndToken," +
				"MaxLength," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, Constants.DEFAULT_CLIENTID);
			ps.setString(2, "Default SenderId");
			ps.setString(3, EmailIdToken.BODY_BEGIN);
			ps.setString(4, EmailIdToken.BODY_END);
			ps.setString(5, EmailIdToken.NAME);
			ps.setString(6, EmailIdToken.XHDR_BEGIN);
			ps.setString(7, EmailIdToken.XHDR_END);
			ps.setInt(8, EmailIdToken.MAXIMUM_LENGTH);
			ps.setTimestamp(9, new Timestamp(new java.util.Date().getTime()));
			ps.setString(10, "SysAdmin");
			ps.execute();
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			IdTokensTable ct = new IdTokensTable();
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
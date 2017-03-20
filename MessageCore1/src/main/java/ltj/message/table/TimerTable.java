package ltj.message.table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.main.CreateTableBase;

public class TimerTable extends CreateTableBase {
	/**
	 * Creates a new instance of MailTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public TimerTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE timer_server");
			System.out.println("Dropped timer_server Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException {
		try {
			stm.execute("CREATE TABLE timer_server ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ServerName varchar(50) NOT NULL, " + 
			"TimerInterval Integer NOT NULL, " +
			"TimerIntervalUnit varchar(6) NOT NULL, " +
			"InitialDelay Integer NOT NULL, " +
			"StartTime datetime(3), " +
			"Threads Integer NOT NULL, " +
			"StatusId char(1) NOT NULL, " +
			"ProcessorName varchar(100) NOT NULL, " +
			"UpdtTime datetime(3) NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (ServerName) " +
			") ENGINE=InnoDB");
			System.out.println("Created timer_server Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO timer_server " +
				"(ServerName," +
				"TimerInterval," +
				"TimerIntervalUnit," +
				"InitialDelay," +
				"StartTime," +
				"Threads," +
				"StatusId," +
				"ProcessorName," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"); 
			
			ps.setString(1, "PurgeTimer");
			ps.setInt(2, 60);
			ps.setString(3, "minute");
			ps.setInt(4, 5000);
			ps.setTimestamp(5, null);
			ps.setInt(6, 1);
			ps.setString(7, StatusId.ACTIVE.value());
			ps.setString(8, "timerProcessor");
			ps.setTimestamp(9, new Timestamp(new java.util.Date().getTime()));
			ps.setString(10, Constants.DEFAULT_USER_ID);
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
			TimerTable ct = new TimerTable();
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
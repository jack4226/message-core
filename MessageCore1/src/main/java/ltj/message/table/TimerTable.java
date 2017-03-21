package ltj.message.table;

import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.timer.TimerServerDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.TimerServerVo;
import ltj.spring.util.SpringUtil;

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
			"row_id int AUTO_INCREMENT not null, " +
			"server_name varchar(50) NOT NULL, " + 
			"timer_interval Integer NOT NULL, " +
			"timer_interval_unit varchar(6) NOT NULL, " +
			"initial_delay Integer NOT NULL, " +
			"start_time datetime(3), " +
			"threads Integer NOT NULL, " +
			"status_id char(1) NOT NULL, " +
			"processor_name varchar(100) NOT NULL, " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id char(10) NOT NULL, " +
			"PRIMARY KEY (row_id), " +
			"UNIQUE INDEX (server_name) " +
			") ENGINE=InnoDB");
			System.out.println("Created timer_server Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		TimerServerDao dao = SpringUtil.getDaoAppContext().getBean(TimerServerDao.class);
		
		try {
			TimerServerVo vo = new TimerServerVo();
			vo.setServerName("PurgeServer");
			vo.setTimerInterval(60);
			vo.setTimerIntervalUnit("minute");
			vo.setInitialDelay(5000);
			vo.setStartTime(null);
			vo.setThreads(1);
			vo.setStatusId(StatusId.ACTIVE.value());
			vo.setProcessorName("timerProcessor");
			vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			
			int rows = dao.insert(vo);
			System.out.println("Number of timer_server rows inserted: " + rows);
		}
		catch (Exception e) {
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
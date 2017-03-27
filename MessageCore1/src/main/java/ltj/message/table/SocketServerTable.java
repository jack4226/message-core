package ltj.message.table;

import java.sql.SQLException;
import java.sql.Timestamp;

import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.socket.SocketServerDao;
import ltj.message.main.CreateTableBase;
import ltj.message.vo.SocketServerVo;
import ltj.spring.util.SpringUtil;

public class SocketServerTable extends CreateTableBase {
	/**
	 * Creates a new instance of MailTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public SocketServerTable() throws SQLException, ClassNotFoundException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE socket_server");
			System.out.println("Dropped socket_server Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException {
		/*
	 	- ServerName: Socket Server Name
	 	- SocketPort: the port number the socket server is listening to
		- Interactive: yes/no
	 	- ServerTimeout: timeout value for server socket
	 	- SocketTimeout: timeout value for socket
	 	- TimeoutUnit: minute/second
		- Connections: number of server socket connections to create
		- Priority: high/medium/low - server thread priority
		- StatusId: A - Active, I - Inactive
		- ProcessorName: processor class name
		*/
		try {
			stm.execute("CREATE TABLE socket_server ( " +
			"row_id int AUTO_INCREMENT not null, " +
			"server_name varchar(30) NOT NULL, " + 
			"socket_port Integer NOT NULL, " +
			"interactive varchar(3) NOT NULL, " +
			"server_timeout integer NOT NULL, " +
			"socket_timeout integer NOT NULL, " +
			"timeout_unit varchar(6) NOT NULL, " +
			"connections Integer NOT NULL, " +
			"priority varchar(10), " +
			"status_id char(1) NOT NULL, " +
			"processor_name varchar(100) NOT NULL, " +
			"message_count integer NOT NULL, " +
			"updt_time datetime(3) NOT NULL, " +
			"updt_user_id char(10) NOT NULL, " +
			"CONSTRAINT socket_server_pkey PRIMARY KEY (row_id), " +
			"Constraint UNIQUE INDEX socket_server_ix_server_name (server_name), " +
			"Constraint UNIQUE INDEX socket_server_ix_server_port (socket_port) " +
			") ENGINE=InnoDB");
			System.out.println("Created socket_server Table...");
		} catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		SocketServerDao dao = SpringUtil.getDaoAppContext().getBean(SocketServerDao.class);
		
		try {
			SocketServerVo vo = new SocketServerVo();
			vo.setServerName("Socket Server 1");
			vo.setSocketPort(5444);
			vo.setInteractive(Constants.YES);
			vo.setServerTimeout(20);
			vo.setSocketTimeout(10);
			vo.setTimeoutUnit("minute");
			vo.setConnections(10);
			vo.setPriority("high");
			vo.setStatusId(StatusId.ACTIVE.value());
			vo.setProcessorName("socketProcessor");
			vo.setMessageCount(0);
			vo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			vo.setUpdtUserId(Constants.DEFAULT_USER_ID);

			int rows = dao.insert(vo);
			System.out.println("Number of rows inserted to socket_server: " + rows);
		} catch (Exception e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			SocketServerTable ct = new SocketServerTable();
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
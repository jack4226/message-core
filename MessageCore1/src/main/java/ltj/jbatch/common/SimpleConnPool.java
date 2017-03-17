package ltj.jbatch.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class provides a simple implementation for creating Oracle connection
 * pool(s).
 * <p>
 * Methods are provided to request and release connections from pool.
 */
public class SimpleConnPool {
	private final Hashtable<Connection, Boolean> connections;
	private final int increment;
	private final String dbURL, user, password;

	/**
	 * @param dbURL:
	 *            database location in URL format
	 * @param user:
	 *            user name
	 * @param password:
	 *            user password
	 * @param driverClassName:
	 *            Oracle Driver class name
	 * @param initialConnections:
	 *            number of connections to be created initially
	 * @param increment:
	 *            number of connections to be created if no free connection is
	 *            available
	 */
	public SimpleConnPool(String dbURL, String user, String password, String driverClassName, int initialConnections,
			int increment) throws SQLException, ClassNotFoundException {

		// Load the specified driver class
		Class.forName(driverClassName);

		this.dbURL = dbURL;
		this.user = user;
		this.password = password;
		this.increment = increment;

		connections = new Hashtable<Connection, Boolean>();

		// Put our pool of Connections in the Hashtable (as name/value pair)
		// The FALSE value indicates they're unused
		for (int i = 0; i < initialConnections; i++) {
			connections.put(DriverManager.getConnection(dbURL, user, password), Boolean.FALSE);
		}
	}

	/**
	 * request a connection from the connection pool
	 * 
	 * @return a JDBC connection
	 */
	public synchronized Connection getConnection() throws SQLException {
		Connection conn = null;

		Enumeration<Connection> conns = connections.keys();

		while (conns.hasMoreElements()) {
			conn = conns.nextElement();

			Boolean b = connections.get(conn);
			if (Boolean.FALSE.equals(b)) {
				// So we found an unused connection.
				// Test its integrity with a quick setAutoCommit(true) call.
				// For production use, more testing should be performed,
				// such as executing a simple query.
				try {
					boolean before = conn.getAutoCommit();
					conn.setAutoCommit(!before);
					conn.setAutoCommit(before);
				}
				catch (SQLException e) {
					connections.remove(conn); // remove the dead connection
					// Problem with the connection, replace it.
					conn = DriverManager.getConnection(dbURL, user, password);
				}
				// Update the Hashtable to show this one's taken
				connections.put(conn, Boolean.TRUE);
				// Return the connection
				return conn;
			}
		}

		// If we get here, there were no free connections.
		// We've got to make more.
		for (int i = 0; i < increment; i++) {
			connections.put(DriverManager.getConnection(dbURL, user, password), Boolean.FALSE);
		}

		// Recurse to get one of the new connections.
		return getConnection();
	}

	/**
	 * release a connection to the connection pool
	 * 
	 * @param returned:
	 *            connection to be returned to the connection pool
	 */
	public synchronized void returnConnection(Connection returned) {
//		Connection con;
//		Enumeration<?> cons = connections.keys();
//		while (cons.hasMoreElements()) {
//			con = (Connection) cons.nextElement();
//			if (con == returned) {
//				connections.put(con, Boolean.FALSE);
//				break;
//			}
//		}
		if (connections.containsKey(returned)) {
			connections.put(returned, Boolean.FALSE);
		}
	}
	
	public int getPoolSize() {
		return connections.size();
	}
}

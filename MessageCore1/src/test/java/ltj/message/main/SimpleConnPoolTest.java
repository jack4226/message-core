package ltj.message.main;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ltj.jbatch.common.SimpleConnPool;

public class SimpleConnPoolTest {

	@Test
	public void test1() {
		String dburl = "jdbc:mysql://localhost:3306/message?useSSL=false";
		String driver = "com.mysql.jdbc.Driver";
		try {
			SimpleConnPool pool = new SimpleConnPool(dburl, "email", "email", driver, 5, 2);
			
			assertNotNull(pool);
			
			assertTrue(pool.getPoolSize() >= 5);
			
			int conns = 10;
			List<Connection> connList = new ArrayList<>();
 			
			for (int i = 0; i < conns; i++) {
				Connection conn = pool.getConnection();
				assertNotNull(conn);
				connList.add(conn);
				
				Statement stmt = conn.createStatement();
				ResultSet result = stmt.executeQuery("select count(*) from EmailAddr");
				if (result.next()) {
					long rows = result.getLong(1);
					System.out.println("EmailAddr count: " + rows);
					assertTrue(rows > 0);
				}
				assertNotNull(result);
				stmt.close();
			}
			
			for (Connection conn : connList) {
				pool.returnConnection(conn);
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
}

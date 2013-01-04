package com.legacytojava.mailsender;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.legacytojava.jbatch.JbMain;
import com.legacytojava.jbatch.pool.NamedPools;
import com.legacytojava.jbatch.pool.ObjectPool;
import com.legacytojava.jbatch.smtp.SmtpConnection;
import com.legacytojava.jbatch.smtp.SmtpWrapper;

public class PoolsTest extends TestCase {
	static int init_count = 0;

	final static String LF = System.getProperty("line.separator", "\n");

	/**
	 * Constructor for TemplateTest.
	 * 
	 * @param arg0
	 */
	public PoolsTest(String arg0) throws Exception {
		super(arg0);
	}

	public void testDistribution() throws Exception {
		System.out.println(LF + "********** Starting testDistribution **********");

		JbMain.getInstance(); // to tell the system this is a JBatch run
		ArrayList<ObjectPool> poolItems = new ArrayList<ObjectPool> ();
		poolItems.add(new ObjectPool("smtpConnection", 2, "smtp1", "SMTP", 50));
		// two servers with equal distribution
		NamedPools pools = new NamedPools(poolItems);
		distribution(pools);

		// two servers with unequal distribution
		poolItems.add(new ObjectPool("smtpConnection", 2, "smtp2", "SMTP", 50));
		pools = new NamedPools(poolItems);
		List<String> names = pools.getNames();
		Iterator<String> it = names.iterator();
		int row = 70;
		while (it.hasNext()) {
			String name = it.next();
			pools.setDistribution(name, row);
			row += 90;
		}
		distribution(pools);

		// one server has zero value of distribution
		pools.setDistribution("smtp1", 0);
		distribution(pools);

		// only one server
		Object obj = pools.remove("smtp1");
		assertNotNull(obj);
		distribution(pools);
	}

	public void testSmtpConnection() throws Exception {
		System.out.println(LF + "********** Starting testSmtpConnection **********");
		{
			SmtpWrapper smtp = (SmtpWrapper) JbMain.getBatchAppContext().getBean("smtpWrapper");
			assertNotNull(smtp);
			if (smtp != null) {
				int size = smtp.getObjectPool().getSize();
				assertEquals(size, 2);
				SmtpConnection[] conn = new SmtpConnection[size];
				for (int i = 0; i < size; i++) {
					conn[i] = smtp.getConnection();
					assertNotNull(conn[i]);
					conn[i].testConnection(true);
				}
				assertEquals(smtp.getObjectPool().getNumberOfFreeItems(), 0);
				for (int i = 0; i < size; i++) {
					smtp.returnConnection(conn[i]);
				}
				assertEquals(smtp.getObjectPool().getNumberOfFreeItems(), size);
			}
		}
		
		{
			SmtpWrapper exch = (SmtpWrapper) JbMain.getBatchAppContext().getBean("exchWrapper");
			assertNotNull(exch);
			if (exch != null) {
				int size = exch.getObjectPool().getSize();
				assertEquals(size, 1);
				SmtpConnection[] conn = new SmtpConnection[size];
				for (int i = 0; i < size; i++) {
					conn[i] = exch.getConnection();
					assertNotNull(conn[i]);
					conn[i].testConnection(true);
				}
				assertEquals(exch.getObjectPool().getNumberOfFreeItems(), 0);
				for (int i = 0; i < size; i++) {
					exch.returnConnection(conn[i]);
				}
				assertEquals(exch.getObjectPool().getNumberOfFreeItems(), size);
			}
		}
	}

	public void testAllSmtpPools() throws Exception {
		System.out.println(LF + "********** Starting testAllSmtpPools **********");
		HashMap<String, Object> svrs = new HashMap<String, Object>();
		svrs.put("postexch", JbMain.getBatchAppContext().getBean("exchWrapper"));
		svrs.put("smtpsvr1", JbMain.getBatchAppContext().getBean("smtpWrapper"));
		svrs.put("exchsvr1", JbMain.getBatchAppContext().getBean("exchWrapper"));
		svrs.put("postfix1", JbMain.getBatchAppContext().getBean("smtpWrapper"));
		assertFalse(svrs.isEmpty());
		NamedPools pools = new NamedPools();
		Set<String> set = svrs.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String name = it.next();
			SmtpWrapper w = (SmtpWrapper) svrs.get(name);
			ObjectPool pool = w.getObjectPool();
			pools.add(name, pool);
		}
		smtpPools(pools, "postexch");
		smtpPools(pools, "smtpsvr1");
		smtpPools(pools, "exchsvr1");
		smtpPools(pools, "postfix1");
		System.out.println("++++++++++ Now Testing Anonymous getConnection()");
		for (int i = 0; i < 4; i++) {
			smtpPools(pools);
		}
	}

	public void testPostSmtpPools() throws Exception {
		System.out.println(LF + "********** Starting testPostSmtpPools **********");
		ArrayList<ObjectPool> poolItems = new ArrayList<ObjectPool>();
		poolItems.add(new ObjectPool("smtpConnection", 2, "postfix1", "SMTP", 50));
		poolItems.add(new ObjectPool("smtpConnection", 2, "smtpsvr1", "SMTP", 50));
		NamedPools pools = new NamedPools(poolItems);
		assertNotNull(pools);
		assertTrue(pools.size() > 0);
		smtpPools(pools, "postfix1");
		smtpPools(pools, "smtpsvr1");
		System.out.println("++++++++++ Now Testing POST Anonymous getConnection()");
		for (int i = 0; i < 4; i++) {
			smtpPools(pools);
		}
	}

	public void testExchSmtpPools() throws Exception {
		System.out.println(LF + "********** Starting testExchSmtpPools **********");
		ArrayList<ObjectPool> poolItems = new ArrayList<ObjectPool>();
		poolItems.add(new ObjectPool("exchConnection", 2, "postexch", "SMTP", 50));
		poolItems.add(new ObjectPool("exchConnection", 2, "exchsvr1", "SMTP", 50));
		NamedPools pools = new NamedPools(poolItems);
		assertNotNull(pools);
		assertTrue(pools.size() > 0);
		Object obj = pools.remove("postexch");
		assertNotNull(obj);
		smtpPools(pools, "exchsvr1");
		System.out.println("++++++++++ Now Testing EXCH Anonymous getConnection()");
		for (int i = 0; i < 4; i++) {
			smtpPools(pools);
		}
	}

	private void distribution(NamedPools pools) {
		pools.setUseDistribution(false);
		List<String> names = pools.getNames();
		for (int j = 0; j < 10; j++) {
			for (int i = 0; i < pools.size(); i++) {
				String name = pools.getNextName();
				assertEquals(name, names.get(i));
			}
		}

		pools.setUseDistribution(true);
		Hashtable<String, Integer> map = new Hashtable<String, Integer>();
		for (int i = 0; i < pools.getTotalDistributions(); i++) {
			String name = pools.getNextName();
			if (map.get(name) == null) {
				map.put(name, Integer.valueOf(1));
			}
			else {
				Integer cnt = (Integer) map.get(name);
				map.put(name, Integer.valueOf(cnt.intValue() + 1));
			}
		}
		Enumeration<?> enu = map.keys();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			Integer count = (Integer) map.get(name);
			int dist = pools.getDistribution(name);
			System.out.println("name=" + name + ", count=" + count.intValue() + ", dist=" + dist);
			if (dist == 0)
				assertTrue(count.intValue() == 0);
			else {
				assertTrue(((float) count.intValue() / dist) > 0.7);
				assertTrue(((float) count.intValue() / dist) < 1.3);
			}
		}
		System.out.println();
	}

	private void smtpPools(NamedPools pools, String name) throws Exception {
		ObjectPool pool = pools.getPool(name);
		SmtpConnection[] conn = new SmtpConnection[pool.getSize()];
		for (int i = 0; i < pool.getSize(); i++) {
			conn[i] = (SmtpConnection) pools.getConnection(name);
			assertNotNull(conn[i]);
			conn[i].testConnection(true);
		}
		for (int i = 0; i < pool.getSize(); i++)
			pools.returnConnection(name, conn[i]);
	}

	private void smtpPools(NamedPools pools) throws Exception {
		SmtpConnection conn = (SmtpConnection) pools.getConnection();
		assertNotNull(conn);
		conn.testConnection(true);
		pools.returnConnection(conn);
	}
}
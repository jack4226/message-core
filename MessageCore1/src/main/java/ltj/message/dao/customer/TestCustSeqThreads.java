package ltj.message.dao.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ltj.spring.util.SpringUtil;

public class TestCustSeqThreads implements Runnable {
	final static String LF = System.getProperty("line.separator", "\n");
	static CustSequenceDao custSequenceDao = null;
	
	final static Map<String, List<Long>> threadMap = new HashMap<>();
	
	public static void main(String[] args) {
		custSequenceDao = SpringUtil.getDaoAppContext().getBean(CustSequenceDao.class);

		try {
			TestCustSeqThreads test = new TestCustSeqThreads();
			Thread thread1 = new Thread(test);
			Thread thread2 = new Thread(test);
			Thread thread3 = new Thread(test);
			Thread thread4 = new Thread(test);
			thread1.start();
			thread2.start();
			thread3.start();
			thread4.start();
			thread1.join();
			thread2.join();
			thread3.join();
			thread4.join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		for (Iterator<String> it=threadMap.keySet().iterator(); it.hasNext();) {
			String thread = it.next();
			System.out.println(thread + " - " + threadMap.get(thread));
			assertAscending(threadMap.get(thread));
		}
		System.exit(0);
	}

	public void run() {
		for (int i = 0; i < 20; i++) {
			long nextVal = custSequenceDao.findNextValue();
			String thread = Thread.currentThread().getName();
			System.out.println(thread + " - " + nextVal);
			if (threadMap.containsKey(thread)) {
				threadMap.get(thread).add(nextVal);
			}
			else {
				List<Long> lst = new ArrayList<>();
				lst.add(nextVal);
				threadMap.put(thread, lst);
			}
		}
	}
	
	static void assertAscending(List<Long> values) {
		for (int i = 1; i < values.size(); i++) {
			assert(values.get(i) > values.get(i - 1));
		}
	}
}

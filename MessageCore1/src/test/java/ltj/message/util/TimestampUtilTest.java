package ltj.message.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class TimestampUtilTest {
	static final Logger logger = LogManager.getLogger(TimestampUtilTest.class);
	
	@Test
	public void testTimestampUtil() {
		String db2tm1 = "1582-10-23-00.48.04.702003";
		assertEquals(db2tm1, TimestampUtil.decStrToDb2(TimestampUtil.db2ToDecStr(db2tm1)));
		
		db2tm1 = "0697-10-13-22.29.59.972003";
		assertEquals(db2tm1, TimestampUtil.decStrToDb2(TimestampUtil.db2ToDecStr(db2tm1)));
		
		assertEquals(true, TimestampUtil.isValidDb2Timestamp(db2tm1));
		
		db2tm1 = TimestampUtil.getDb2Timestamp();
		assertTrue(TimestampUtil.isValidDb2Timestamp(db2tm1));
		String converted = TimestampUtil.db2ToDecStr(db2tm1);
		//converted = convert_old(db2tm);
		String restored = TimestampUtil.decStrToDb2(converted);
		logger.info("Date: " + db2tm1 + ", converted: " + converted + ", restored: " + restored);
		assertTrue("Is conversion a success? ", (db2tm1.equals(restored)));
		
		assertTrue("Is (" + restored + ") valid? ", TimestampUtil.isValidDb2Timestamp(restored));
		
		String db2tm2 = TimestampUtil.getDb2Timestamp();
		logger.info("DB2 Time stamp 2: " + db2tm2);
		
		
		String db2tm3 = "2016-12-02-15.52.43.730995";
		String oratms = TimestampUtil.db2ToOracle(db2tm3);
		logger.info("Oracle Time stamp: " + oratms);
		assertEquals("12/02/2016 15:52:43", oratms);
		
		// remove milliseconds
		java.util.Date date3 =TimestampUtil.db2ToDate(db2tm3);
		Calendar cal3 = Calendar.getInstance();
		cal3.setTime(date3);
		cal3.set(Calendar.MILLISECOND, 0);
		
		assertEquals(cal3.getTime(), TimestampUtil.oracleToDate(oratms));
	}
}

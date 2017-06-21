package ltj.message.util;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class BlobUtilTest {
	static final Logger logger = Logger.getLogger(BlobUtilTest.class);

	private Calendar cal;
	
	@Before
	public void setup() {
 		cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		logger.info("Calendar 1: " + cal.getTime());
		cal.roll(Calendar.MONTH, false);
		logger.info("Calendar 2: " + cal.getTime());
		cal.roll(Calendar.MONTH, false);
		logger.info("Calendar 3: " + cal.getTime());
 	}
	
	@Test
    public void testBytesCopy() {
    	try {
 			byte[] calBytes = BlobUtil.objectToBytes(cal);
			Object cal2 = BlobUtil.bytesToObject(calBytes);
			assertEquals(cal, cal2);
			
    	}
    	catch (Exception e) {
    		logger.error("Exception caught", e);
    		fail();
    	}
    }

	@Test
    public void testXmlCopy() {
    	try {
 			byte[] calBytes = BlobUtil.beanToXmlBytes(cal);
			Object cal2 = BlobUtil.xmlBytesToBean(calBytes);
			assertEquals(cal, cal2);
			
    	}
    	catch (Exception e) {
    		logger.error("Exception caught", e);
    		fail();
    	}
    }

}

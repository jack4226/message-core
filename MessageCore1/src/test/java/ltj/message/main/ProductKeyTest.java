package ltj.message.main;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.jbatch.common.ProductKey;
import ltj.jbatch.obsolete.ProductUtil;

public class ProductKeyTest {
	static final Logger logger = Logger.getLogger(ProductKeyTest.class);
	
	static final String LF = System.getProperty("line.separator", "\n");
	
	@Test
	public void testValidateKey() {
		String prodKey = ProductUtil.getProductKeyFromFile();
		logger.info("Product key to validate: " + prodKey);
		assertNotNull(prodKey);
		
		assertEquals(true, ProductKey.validateKey(prodKey));
		
		assertEquals(true, ProductUtil.isProductKeyValid());
	}
	
}

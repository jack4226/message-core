package ltj.message.main;

import static org.junit.Assert.*;

import org.junit.Test;

import ltj.jbatch.app.ProductUtil;
import ltj.jbatch.common.ProductKey;

public class ProductKeyTest {
	static final String LF = System.getProperty("line.separator", "\n");
	
	@Test
	public void testValidateKey() {
		String prodKey = ProductUtil.getProductKeyFromFile();
		System.out.println("Product key to validate: " + prodKey);
		assertNotNull(prodKey);
		
		assertEquals(true, ProductKey.validateKey(prodKey));
		
		assertEquals(true, ProductUtil.isProductKeyValid());
	}
	
}

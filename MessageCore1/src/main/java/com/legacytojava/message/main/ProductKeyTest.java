package com.legacytojava.message.main;

import ltj.jbatch.app.ProductUtil;
import ltj.jbatch.common.ProductKey;

public class ProductKeyTest {
	static final String LF = System.getProperty("line.separator", "\n");
	public static void main(String[] args) {
		try {
			String prodKey = ProductUtil.getProductKeyFromFile();
			System.out.println(prodKey + ", valid? " + ProductKey.validateKey(prodKey));
			System.out.println("Product Key valid? " + ProductUtil.isProductKeyValid());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

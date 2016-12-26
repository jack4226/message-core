package ltj.message.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FileUtil {

	static final Logger logger = Logger.getLogger(FileUtil.class);

	public static byte[] loadFromFile(String filePath, String fileName) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		//loader = TestUtil.class.getClassLoader(); // works too
		filePath = filePath == null ? "" : filePath;
		if (!filePath.endsWith(File.separator) && !filePath.endsWith("/")) {
			filePath += File.separator;
		}
		InputStream is = loader.getResourceAsStream(filePath + fileName);
		if (is == null) {
			throw new RuntimeException("File (" + filePath + fileName + ") not found!");
		}
		logger.info("Loading file from location: " + filePath + fileName);
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		try {
			while ((len=bis.read(buffer))>0) {
				baos.write(buffer, 0, len);
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught: " + e.getMessage());
		}
	}

	public static List<String> loadFromTextFile(String filePath) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		//loader = TestUtil.class.getClassLoader(); // works too
		filePath = filePath == null ? "" : filePath;
		if (!filePath.endsWith(File.separator) && !filePath.endsWith("/")) {
			filePath += File.separator;
		}
		InputStream is = loader.getResourceAsStream(filePath);
		if (is == null) {
			throw new RuntimeException("File (" + filePath + ") not found!");
		}
		logger.info("Loading file from location: " + filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<String> list = new ArrayList<>();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			return list;
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught: " + e.getMessage());
		}
	}

	public static void main(String args[]) {

		ClassLoader cl = FileUtil.class.getClassLoader(); //ClassLoader.getSystemClassLoader();

		java.net.URL[] urls = ((java.net.URLClassLoader) cl).getURLs();

		for (java.net.URL url : urls) {
			System.out.println(url.getFile());
		}

	}
}

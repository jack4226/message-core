package com.legacytojava.message.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TestRunnerBase {
	
	final static String PS = File.separator;
	static Class<?>[] getAllDaoTestClasses(String pkgName) {
		// looking for class name ending with "Test", for example CustomerTest.class
		List<Class<?>> clsList = new ArrayList<Class<?>>();
		String homeDir = System.getProperty("user.dir") + PS + "bin" + PS;
		System.out.println("Working directory: " + homeDir);
		List<File> files =  getClassesFromDirTree(new File(homeDir), "Test.class");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		for (File file : files) {
			String path = file.getPath();
			String pkgPath = StringUtils.removeStart(path, homeDir);
			String clsName = pkgPath.replace("\\", ".");
			try {
				clsName = StringUtils.removeEnd(clsName,".class");
				Class<?> testCls = loader.loadClass(clsName);
				if (clsName.startsWith(pkgName) && testCls.getDeclaredAnnotations().length>=4) {
					if (clsName.startsWith("com.legacytojava.message.bo.test")) {
						continue;
					}
					clsList.add(testCls);
					System.out.println("Test Class: " + testCls.getName());
				}
			}
			catch (ClassNotFoundException e) {
				System.err.println("Class not found: " + clsName);
			}
		}
		return (Class<?>[])clsList.toArray(new Class<?>[clsList.size()]);
	}

	/**
	 * Recursively walk a directory tree and return a List of all Files found;
	 * 
	 * @param dir
	 *            - a valid directory
	 */
	static List<File> getClassesFromDirTree(File dir, String endsWith) {
		List<File> result = new ArrayList<File>();
		File[] files = dir.listFiles();
		for (int i=0; i<files.length; i++) {
			File file = files[i];
			if (file.isFile()) {
				if (file.getName().endsWith(endsWith)) {
					result.add(file);
					//System.out.println(file.getPath());
				}
			}
			else if (file.isDirectory()) {
				// recursive call!
				List<File> deeperList =  getClassesFromDirTree(file, endsWith);
				result.addAll(deeperList);
			}
		}
		return result;
	}
}

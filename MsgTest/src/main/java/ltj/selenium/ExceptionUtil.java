package ltj.selenium;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ExceptionUtil {
	static final Logger logger = Logger.getLogger(ExceptionUtil.class);

	static Pattern classNameRegex = Pattern.compile("ltj\\.msgui[12]\\..{5,30}Test", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
	public static String findRootTestClass(Exception e) {
		for (StackTraceElement elm : e.getStackTrace()) {
			String className = elm.getClassName();
			Matcher m = classNameRegex.matcher(className);
			if (m.find()) {
				for (int i = 0; i <= m.groupCount(); i++) {
					//logger.info("group[" + i + "]: " + m.group(i));
				}
				String stackLine = elm.getClassName() + ":" + elm.getMethodName() + "(" + elm.getLineNumber() + ")";
				return "Root class: " + stackLine;
			}
		}
		return "Root class: unknown";
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(findRootTestClass(new Exception()));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}

package ltj.selenium;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OSUtil {
	static final Logger logger = LogManager.getLogger(OSUtil.class);
	
	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}
	
	public static void setupSeleniumBrowserDriver() {
		String chrome_path = getChromeDriverPath();
		String gecko_path = getGeckoDriverPath();

		File chrome = new File(chrome_path);
		if (!chrome.exists() || !chrome.isFile()) {
			throw new RuntimeException("Could not find " + chrome.getAbsolutePath());
		}
		System.setProperty("webdriver.chrome.driver", chrome.getAbsolutePath());

		File gecko = new File(gecko_path);
		if (!gecko.exists() || !gecko.isFile()) {
			throw new RuntimeException("Could not find " + gecko.getAbsolutePath());
		}
		System.setProperty("webdriver.gecko.driver", gecko.getAbsolutePath());
	}
	
	public static String getChromeDriverPath() {
		String selenium_dir = getSeleniumDriverRoot();

		String chrome_file = "/chromedriver.exe";
		if (OSUtil.isMac()) {
			chrome_file = "/chromedriver";
		}
		else if (OSUtil.isUnix()) {
			chrome_file = "/chromedriver.nix";
		}
		return (selenium_dir + chrome_file);
	}
	
	public static String getGeckoDriverPath() {
		String selenium_dir = getSeleniumDriverRoot();
		String gecko_file = "/geckodriver.exe";
		
		if (OSUtil.isMac()) {
			gecko_file = "/geckodriver";
		}
		else if (OSUtil.isUnix()) {
			gecko_file = "/geckodriver.nix";
		}
		return (selenium_dir + gecko_file);
	}
	
	private static String getSeleniumDriverRoot() {
		String user_dir = System.getProperty("user.dir");
		logger.info("user.dir = " + user_dir);
		String selenium_dir = StringUtils.replaceAll(user_dir, "\\\\", "/") + "/src/main/resources/selenium";
		logger.info("selenium dir = " + selenium_dir);
		return selenium_dir;
	}
	
	public static void main(String[] args) {
		logger.info("OS: " + OS);
		if (isWindows()) {
			System.out.println("This is Windows");
		} else if (isMac()) {
			System.out.println("This is Mac");
		} else if (isUnix()) {
			System.out.println("This is Unix or Linux");
		} else if (isSolaris()) {
			System.out.println("This is Solaris");
		} else {
			System.out.println("Unknown OS!!");
		}
	}

}

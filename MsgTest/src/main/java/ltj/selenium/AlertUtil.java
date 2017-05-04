package ltj.selenium;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AlertUtil {
	static final Logger logger = Logger.getLogger(AlertUtil.class);

	public static void handleAlert(WebDriver driver) {
		// accept (Click OK) JavaScript Alert pop-up
		try {
			WebDriverWait wait = new WebDriverWait(driver, 2);
			Alert alert = (org.openqa.selenium.Alert) wait.until(ExpectedConditions.alertIsPresent());
			alert.accept();
			logger.info("Accepted the alert successfully.");
		}
		catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
			logger.error(e.getMessage());
		}
	}

	public static void handleAlert(WebDriver driver, WebDriverWait wait) {
		if (wait == null) {
			 wait = new WebDriverWait(driver, 5);
		}
		// accept (Click OK) JavaScript Alert pop-up
		try {
			Alert alert = (org.openqa.selenium.Alert) wait.until(ExpectedConditions.alertIsPresent());
			alert.accept();
			logger.info("Accepted the alert successfully.");
		}
		catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
			logger.error(e.getMessage());
		}
	}

}

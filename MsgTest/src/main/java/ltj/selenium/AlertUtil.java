package ltj.selenium;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AlertUtil {
	static final Logger logger = Logger.getLogger(AlertUtil.class);

	public static void handleAlert(WebDriver driver) {
		// accept (Click OK) JavaScript Alert pop-up
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			Alert alert = (org.openqa.selenium.Alert) wait.until(ExpectedConditions.alertIsPresent());
			alert.accept();
			logger.info("Accepted the alert successfully.");
		}
		catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
			logger.error(ExceptionUtil.findRootTestClass(e));
			logger.error("TimeoutException caught: " + e.getMessage());
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
			logger.error(ExceptionUtil.findRootTestClass(e));
			logger.error("TimeoutException caught: " + e.getMessage());
		}
	}

	public static void clickCommandLink(WebDriver driver, By by) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.presenceOfElementLocated(by));
		wait.until(ExpectedConditions.elementToBeClickable(by));
		try {
			WebElement commandLink = driver.findElement(by);
			commandLink.click();
		}
		catch(org.openqa.selenium.StaleElementReferenceException ex) {
			logger.error(ExceptionUtil.findRootTestClass(ex));
			logger.error("StaleElementReferenceException caught: " + ex.getMessage());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// ignore 
			}
			driver.findElement(by).click();
		}
		catch (org.openqa.selenium.WebDriverException e) { //unknown error
			logger.error(ExceptionUtil.findRootTestClass(e));
			logger.error("WebDriverException caught: " + e.getMessage());
			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
				// ignore 
			}
			driver.findElement(by).click();
		}
	}
	
	public static void waitLongIgnoreTimeout(WebDriver driver, By by) {
		WebDriverWait waitLong = new WebDriverWait(driver, 20);
		try {
			waitLong.until(ExpectedConditions.presenceOfElementLocated(by));			
		}
		catch (org.openqa.selenium.TimeoutException e) {
			logger.error(ExceptionUtil.findRootTestClass(e));
			logger.warn("TimeoutException caught: " + e.getMessage());
		}
	}
}

package ltj.msgui1;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SmtpServerTest extends BaseLogin {
	static final Logger logger = Logger.getLogger(SmtpServerTest.class);

	@Test
	public void testSmtpServers() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText("Configure SMTP Servers"));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Configure SMTP Servers"));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement persistElm = driver.findElement(By.cssSelector("span[title=smtpServer_persistence]"));
			assertNotNull(persistElm);
			logger.info("Persistence before: " + persistElm.toString() + ", text: " + persistElm.getText());
			
			String persistBefore = persistElm.getText();
			
			link = driver.findElement(By.cssSelector("a[title='smtpServer']"));
			assertNotNull(link);
			
			link.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpedit:footer:gettingStartedFooter")));
			
			WebElement descElm = driver.findElement(By.id("smtpedit:content:desc"));
			assertNotNull(descElm);
			logger.info("Description: " + descElm.getAttribute("value") + ", test: " + descElm.getText());
			String desc = descElm.getAttribute("value");
			String suffix = "_updated";
			if (StringUtils.endsWith(desc, suffix)) {
				desc = StringUtils.removeEndIgnoreCase(desc, suffix);
			}
			else {
				desc += suffix;
			}
			descElm.clear();
			descElm.sendKeys(desc);
			
			Select selectPersist = new Select(driver.findElement(By.id("smtpedit:content:persistence")));
			assertNotNull(selectPersist);
			WebElement selectedPersist = selectPersist.getFirstSelectedOption();
			logger.info("Persistence selected before: " + selectedPersist.getText());
			if ("true".equalsIgnoreCase(persistBefore)) {
				assertEquals("Yes", selectedPersist.getText());
				selectPersist.selectByVisibleText("No");
			}
			else {
				assertEquals("No", selectedPersist.getText());
				selectPersist.selectByVisibleText("Yes");
			}
 			
			WebElement retriesElm = driver.findElement(By.id("smtpedit:content:retries"));
			String retriesStr = retriesElm.getAttribute("value");
			int retries = Integer.parseInt(retriesStr);
			if (retries > 10) {
				retries--;
			}
			else {
				retries++;
			}
			retriesElm.clear();
			retriesElm.sendKeys(retries + "");
			
			WebElement submit = driver.findElement(By.id("smtpedit:content:submit"));
			assertNotNull(submit);
			submit.click();
			
			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}
			
			// verify the results
			wait.until(ExpectedConditions.titleIs("Configure SMTP Servers"));
			
			WebElement persistAfter = driver.findElement(By.cssSelector("span[title=smtpServer_persistence]"));
			assertNotNull(persistAfter);
			if ("true".equalsIgnoreCase(persistBefore)) {
				assertEquals("false", persistAfter.getText());
			}
			else {
				assertEquals("true", persistAfter.getText());
			}

		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

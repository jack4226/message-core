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

public class SiteNameTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(SiteNameTest.class);

	@Test
	public void testSiteProfiles() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText("Configure Site Profiles"));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Configure Site Profiles"));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement siteNameElm = driver.findElement(By.cssSelector("span[title=JBatchCorp_siteName]"));
			assertNotNull(siteNameElm);
			logger.info("Site Name: " + siteNameElm.toString() + ", text: " + siteNameElm.getText());
			String siteNameBefore = siteNameElm.getText();
			String siteNameAfter = null;
			if (StringUtils.endsWith(siteNameBefore, "_updated")) {
				siteNameAfter = StringUtils.removeEnd(siteNameBefore, "_updated");
			}
			else {
				siteNameAfter = siteNameBefore + "_updated";
			}
			
			WebElement useTestAddrElm = driver.findElement(By.cssSelector("span[title=JBatchCorp_useTestAddr]"));
			assertNotNull(useTestAddrElm);
			logger.info("useTestAddr: " + useTestAddrElm.toString() + ", text: " + useTestAddrElm.getText());
			
			String useTestAddr = useTestAddrElm.getText();
			
			link = driver.findElement(By.linkText("JBatchCorp"));
			assertNotNull(link);
			
			link.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailprof:footer:gettingStartedFooter")));
			
			WebElement siteName = driver.findElement(By.id("emailprof:content:sitename"));
			siteName.clear();
			siteName.sendKeys(siteNameAfter);
			
			Select selectTest = new Select(driver.findElement(By.id("emailprof:content:usetest")));
			WebElement selectedTest = selectTest.getFirstSelectedOption();
			logger.info("Use Test Address selected before: " + selectedTest.getText());
			if ("true".equalsIgnoreCase(useTestAddr)) {
				assertEquals("Yes", selectedTest.getText());
				selectTest.selectByVisibleText("No");
			}
			else {
				assertEquals("No", selectedTest.getText());
				selectTest.selectByVisibleText("Yes");
			}
 			
			Select selectVerp = new Select(driver.findElement(By.id("emailprof:content:useverp")));
			WebElement selectedVerp = selectVerp.getFirstSelectedOption();
			logger.info("Is Verp selected before: " + selectedVerp.getText());
			String selectedVerpBefore = selectedVerp.getText();
			if ("No".equals(selectedVerp.getText())) {
				selectVerp.selectByVisibleText("Yes");
			}
			else {
				selectVerp.selectByVisibleText("No");
			}
			
			WebElement submit = driver.findElement(By.id("emailprof:content:submit"));
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
			wait.until(ExpectedConditions.titleIs("Configure Site Profiles"));
			
			WebElement siteNameAfterElm = driver.findElement(By.cssSelector("span[title=JBatchCorp_siteName]"));
			assertNotNull(siteNameAfterElm);
			assertEquals(siteNameAfter, siteNameAfterElm.getText());
			
			WebElement useTestAddrAfter = driver.findElement(By.cssSelector("span[title=JBatchCorp_useTestAddr]"));
			assertNotNull(useTestAddrAfter);
			if ("true".equalsIgnoreCase(useTestAddr)) {
				assertEquals("false", useTestAddrAfter.getText());
			}
			else {
				assertEquals("true", useTestAddrAfter.getText());
			}
			
			WebElement selectedVerpAfter = driver.findElement(By.cssSelector("span[title=JBatchCorp_isVerpEnabled]"));
			assertNotNull(selectedVerpAfter);
			if ("yes".equalsIgnoreCase(selectedVerpBefore)) {
				assertEquals("false", selectedVerpAfter.getText());
			}
			else {
				assertEquals("true", selectedVerpAfter.getText());
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

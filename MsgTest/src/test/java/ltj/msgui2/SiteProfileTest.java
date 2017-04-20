package ltj.msgui2;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SiteProfileTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(SiteProfileTest.class);

	@Test
	public void testListAndViewDetail() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Configure Site Profiles";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			
			WebElement siteNameElm = driver.findElement(By.cssSelector("span[title='JBatchCorp_senderName']"));
			String siteNameBefore = siteNameElm.getText();
			String siteNameAfter = null;
			if (StringUtils.endsWith(siteNameBefore, "_updated")) {
				siteNameAfter = StringUtils.removeEnd(siteNameBefore, "_updated");
			}
			else {
				siteNameAfter = siteNameBefore + "_updated";
			}
			
			WebElement useTestAddrElm = driver.findElement(By.cssSelector("span[title='JBatchCorp_userTestAddr']"));
			String useTestAddr = useTestAddrElm.getText();
			
			WebElement viewDetailLink = driver.findElement(By.cssSelector("a[title='JBatchCorp_viewDetail']"));
			viewDetailLink.click();
			
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
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			WebElement siteNameAfterElm = driver.findElement(By.cssSelector("span[title='JBatchCorp_senderName']"));
			assertEquals(siteNameAfter, siteNameAfterElm.getText());
			
			WebElement useTestAddrAfter = driver.findElement(By.cssSelector("span[title='JBatchCorp_userTestAddr']"));
			if ("true".equalsIgnoreCase(useTestAddr)) {
				assertEquals("false", useTestAddrAfter.getText());
			}
			else {
				assertEquals("true", useTestAddrAfter.getText());
			}
			
			WebElement selectedVerpAfter = driver.findElement(By.cssSelector("span[title='JBatchCorp_verpEnabled']"));
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

	@Test
	public void testCopyFromSelected() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Configure Site Profiles";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// locate the record to copy from
			WebElement checkBoxElm = driver.findElement(By.cssSelector("input[title='JBatchCorp_checkBox']"));
			checkBoxElm.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sitename:footer:gettingStartedFooter")));
			
			WebElement copyToNewElm = driver.findElement(By.cssSelector("input[title='Create a new row from selected']"));
			copyToNewElm.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailprof:footer:gettingStartedFooter")));
			
			// Edit Detail Page
			Random r = new Random();
			String randomStr1 = StringUtils.leftPad(r.nextInt(1000) + "", 3, '0');
			
			WebElement senderId = driver.findElement(By.id("emailprof:content:senderid"));
			senderId.clear();
			senderId.sendKeys("testsiteid_" + randomStr1);
			
			WebElement siteName = driver.findElement(By.id("emailprof:content:sitename"));
			siteName.clear();
			siteName.sendKeys("Test Sender " + randomStr1);
			
			WebElement domainName = driver.findElement(By.id("emailprof:content:domain"));
			domainName.clear();
			domainName.sendKeys("testdomain." + randomStr1 + ".local");
			
			WebElement submit = driver.findElement(By.id("emailprof:content:submit"));
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sitename:footer:gettingStartedFooter")));
			
			String title = "testsiteid_" + randomStr1 + "_checkBox";
			WebElement checkBoxLink = driver.findElement(By.cssSelector("input[title='" + title + "']"));
			assertEquals(true, checkBoxLink.isDisplayed());
			checkBoxLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sitename:footer:gettingStartedFooter")));
			
			// delete the added record
			WebElement deleteLink = driver.findElement((By.cssSelector("input[title='Delete selected rows']")));
			deleteLink.click();
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sitename:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("input[title='" + title + "']"))));
			
			// verify the record is deleted
			List<WebElement> checkBoxs = driver.findElements(By.cssSelector("input[title='" + title + "']"));
			assertTrue(checkBoxs.isEmpty());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

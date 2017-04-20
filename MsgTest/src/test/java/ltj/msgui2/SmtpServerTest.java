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

public class SmtpServerTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(SmtpServerTest.class);

	@Test
	public void testListAndViewDetail() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Configure SMTP Servers";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// save field values from the list
			WebElement checkBoxElm = driver.findElement(By.cssSelector("input[title='gmailServer_checkBox']"));
			assertEquals(true, checkBoxElm.isDisplayed());
			
			WebElement serverNameLink = driver.findElement(By.cssSelector("a[title='gmailServer_viewDetail']"));
			String serverNameBefore = serverNameLink.getText();
			
			WebElement hostNameElm = driver.findElement(By.cssSelector("span[title='gmailServer_hostName']"));
			String hostNameBefore = hostNameElm.getText();
			
			WebElement userIdElm = driver.findElement(By.cssSelector("span[title='gmailServer_userId']"));
			String userIdBefore = userIdElm.getText();
			
			WebElement useSslElm = driver.findElement(By.cssSelector("span[title='gmailServer_useSsl']"));
			String useSslBefore = useSslElm.getText();
			
			WebElement persistElm = driver.findElement(By.cssSelector("span[title='gmailServer_persistence']"));
			String persistBefore = persistElm.getText();
			
			serverNameLink.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpedit:footer:gettingStartedFooter")));
			
			// View/Edit Detail Page
			WebElement serverNameEdt = driver.findElement(By.id("smtpedit:content:servername"));
			assertEquals(serverNameBefore, serverNameEdt.getAttribute("value"));
			assertEquals(false, serverNameEdt.isEnabled());
			
			WebElement hostNameEdt = driver.findElement(By.id("smtpedit:content:smtphost"));
			assertEquals(hostNameBefore, hostNameEdt.getAttribute("value"));
			
			WebElement userIdEdt = driver.findElement(By.id("smtpedit:content:userid"));
			assertEquals(userIdBefore, userIdEdt.getAttribute("value"));
			
			Select selectUseSsl = new Select(driver.findElement(By.id("smtpedit:content:ssl")));
			WebElement selectedUseSsl = selectUseSsl.getFirstSelectedOption();
			assertEquals(useSslBefore, selectedUseSsl.getAttribute("value"));
			
			Select selectPersist = new Select(driver.findElement(By.id("smtpedit:content:persistence")));
			WebElement selectedPersist = selectPersist.getFirstSelectedOption();
			assertEquals(persistBefore, selectedPersist.getAttribute("value"));
			if ("false".equals(persistBefore)) {
				selectPersist.selectByValue("true");
			}
			else {
				selectPersist.selectByValue("false");
			}
			
			WebElement descEdt = driver.findElement(By.id("smtpedit:content:desc"));
			String desc = descEdt.getAttribute("value");
			if (StringUtils.endsWith(desc, "_updated")) {
				desc = StringUtils.removeEnd(desc, "_updated");
			}
			else {
				desc += "_updated";
			}
			descEdt.clear();
			descEdt.sendKeys(desc);
			
			// submit changes
			WebElement submit = driver.findElement(By.cssSelector("input[title='Submit changes']"));
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpsrvr:footer:gettingStartedFooter")));
			
			WebElement persistAfterElm = driver.findElement(By.cssSelector("span[title='gmailServer_persistence']"));
			String persistAfter = persistAfterElm.getText();
			if ("true".equals(persistBefore)) {
				assertEquals("false", persistAfter);
			}
			else {
				assertEquals("true", persistAfter);
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
		String listTitle = "Configure SMTP Servers";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// locate the record to copy from
			WebElement checkBoxElm = driver.findElement(By.cssSelector("input[title='smtpServer_checkBox']"));
			checkBoxElm.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpsrvr:footer:gettingStartedFooter")));
			
			WebElement copyToNewElm = driver.findElement(By.cssSelector("input[title='Create a new row from selected']"));
			copyToNewElm.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpedit:footer:gettingStartedFooter")));
			
			// Edit Detail Page
			Random r = new Random();
			String randomStr1 = StringUtils.leftPad(r.nextInt(1000) + "", 3, '0');
			
			WebElement serverNameId = driver.findElement(By.id("smtpedit:content:servername"));
			serverNameId.clear();
			serverNameId.sendKeys("testserver_" + randomStr1);
			
			WebElement userId = driver.findElement(By.id("smtpedit:content:userid"));
			userId.clear();
			userId.sendKeys("testuser_" + randomStr1);
			
			WebElement password = driver.findElement(By.id("smtpedit:content:password"));
			password.clear();
			password.sendKeys("testpswd_" + randomStr1 + ".local");
			
			WebElement submit = driver.findElement(By.cssSelector("input[title='Submit changes']"));
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpsrvr:footer:gettingStartedFooter")));
			
			String title = "testserver_" + randomStr1 + "_checkBox";
			WebElement checkBoxLink = driver.findElement(By.cssSelector("input[title='" + title + "']"));
			assertEquals(true, checkBoxLink.isDisplayed());
			checkBoxLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + title + "']"), true));
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpsrvr:footer:gettingStartedFooter")));
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

	@Test
	public void testAddNewServer() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Configure SMTP Servers";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Add a new record
			WebElement copyToNewElm = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			copyToNewElm.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpedit:footer:gettingStartedFooter")));
			
			// Edit Detail Page
			Random r = new Random();
			String randomStr1 = StringUtils.leftPad(r.nextInt(1000) + "", 3, '0');
			
			WebElement serverName = driver.findElement(By.id("smtpedit:content:servername"));
			serverName.clear();
			serverName.sendKeys("testserver_" + randomStr1);
			
			WebElement hostName = driver.findElement(By.id("smtpedit:content:smtphost"));
			hostName.clear();
			hostName.sendKeys("localhost");
			
			WebElement userId = driver.findElement(By.id("smtpedit:content:userid"));
			userId.clear();
			userId.sendKeys("testuser_" + randomStr1);
			
			WebElement password = driver.findElement(By.id("smtpedit:content:password"));
			password.clear();
			password.sendKeys("testpswd_" + randomStr1 + ".local");
			
			WebElement desc = driver.findElement(By.id("smtpedit:content:desc"));
			desc.clear();
			desc.sendKeys("Test Host - " + randomStr1);
			
			WebElement submit = driver.findElement(By.cssSelector("input[title='Submit changes']"));
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpsrvr:footer:gettingStartedFooter")));
			
			String title = "testserver_" + randomStr1 + "_checkBox";
			WebElement checkBoxLink = driver.findElement(By.cssSelector("input[title='" + title + "']"));
			assertEquals(true, checkBoxLink.isDisplayed());
			checkBoxLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + title + "']"), true));
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpsrvr:footer:gettingStartedFooter")));
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

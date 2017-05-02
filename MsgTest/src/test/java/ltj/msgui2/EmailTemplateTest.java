package ltj.msgui2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EmailTemplateTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(EmailTemplateTest.class);

	@Test
	public void testViewListAndDetails() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Templates";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			List<String> titleList = new ArrayList<>();
			for (WebElement elm : checkBoxList) {
				if (elm.isEnabled()) {
					titleList.add(elm.getAttribute("title"));
				}
			}
			
			int idx = new Random().nextInt(titleList.size() - 1);
			
			List<WebElement> listIdList = driver.findElements(By.cssSelector("span[title$='_listId']"));
			assertEquals(checkBoxList.size(), listIdList.size());
			String listId = listIdList.get(idx).getText();
			
			List<WebElement> subjectList = driver.findElements(By.cssSelector("span[title$='_subject']"));
			assertEquals(checkBoxList.size(), subjectList.size());
			String subjectTitle = subjectList.get(idx).getAttribute("title");
			String subjectBefore = subjectList.get(idx).getText();

			List<WebElement> viewDetailList = driver.findElements(By.cssSelector("a[title$='_viewDetail']"));
			assertEquals(checkBoxList.size(), viewDetailList.size());
			WebElement viewDetailLink = viewDetailList.get(idx);
			String viewDetailTitle = viewDetailLink.getAttribute("title");
			
			// View Details page
			viewDetailLink = driver.findElement(By.cssSelector("a[title='" + viewDetailTitle + "']"));
			String templateId = viewDetailLink.getText();
			viewDetailLink.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			WebElement tmpltIdElm = driver.findElement(By.id("emailtmplt:content:templateid"));
			assertEquals(templateId, tmpltIdElm.getAttribute("value"));
			
			Select listIdSelect = new Select(driver.findElement(By.id("emailtmplt:content:listid")));
			assertEquals(listId, listIdSelect.getFirstSelectedOption().getAttribute("value"));
			
			WebElement subjectElm = driver.findElement(By.id("emailtmplt:content:subject"));
			String subjectAfter = subjectElm.getAttribute("value");
			assertEquals(subjectBefore, subjectAfter);
			if (StringUtils.endsWith(subjectAfter, "_updated")) {
				subjectAfter = StringUtils.removeEnd(subjectAfter, "_updated");
			}
			else {
				subjectAfter += "_updated";
			}
			subjectElm.clear();
			subjectElm.sendKeys(subjectAfter);
			
			// Submit changes
			driver.findElement(By.cssSelector("input[title='Submit changes']")).click();
			
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

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			// Verify update results
			WebElement subjectVrf = driver.findElement(By.cssSelector("span[title='" + subjectTitle + "']"));
			assertEquals(subjectAfter, subjectVrf.getText());
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testCopyFromSelected() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Templates";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			List<String> titleList = new ArrayList<>();
			for (WebElement elm : checkBoxList) {
				if (elm.isEnabled()) {
					titleList.add(elm.getAttribute("title"));
				}
			}
			assertFalse(titleList.isEmpty());
			
			int idx = titleList.size() == 1 ? 0 : new Random().nextInt(titleList.size() - 1);
			String checkBoxTitle = titleList.get(idx);
			
			// Tick a selected record
			driver.findElement(By.cssSelector("input[title='" + checkBoxTitle + "']")).click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + checkBoxTitle + "']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			// Copy from selected
			driver.findElement(By.cssSelector("input[title='Create a new row from selected']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
						
			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			String testTmpltId = "TestTemplate_" + suffix;
			driver.findElement(By.id("emailtmplt:content:templateid")).sendKeys(testTmpltId);
			
			Select listTypeSelect = new Select(driver.findElement(By.id("emailtmplt:content:listtype")));
			if ("Traditional".equals(listTypeSelect.getFirstSelectedOption().getAttribute("value"))) {
				listTypeSelect.selectByValue("Personalized");
			}
			else {
				listTypeSelect.selectByValue("Traditional");
			}
			
			// Submit changes
			driver.findElement(By.cssSelector("input[title='Submit changes']")).click();
			
			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			// Verify update results
			WebElement tmpltIdVrf = driver.findElement(By.cssSelector("a[title='" + nextIdx + "_viewDetail']"));
			assertEquals(testTmpltId, tmpltIdVrf.getText());
			
			// Delete added record
			driver.findElement(By.cssSelector("input[title='" + nextIdx + "_checkBox']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + nextIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Delete selected rows']")).click();
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));

		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testAddNewRecord() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Templates";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			
			// Add a new record
			driver.findElement(By.cssSelector("input[title='Add a new row']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
						
			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			String testTmpltId = "TestTemplate_" + suffix;
			driver.findElement(By.id("emailtmplt:content:templateid")).sendKeys(testTmpltId);
			
			Select listIdSelect = new Select(driver.findElement(By.id("emailtmplt:content:listid")));
			listIdSelect.selectByValue("SMPLLST2");
			
			driver.findElement(By.id("emailtmplt:content:subject")).sendKeys("Test Subject " + suffix);
			
			Select listTypeSelect = new Select(driver.findElement(By.id("emailtmplt:content:listtype")));
			listTypeSelect.selectByValue("Personalized");
			
			Select emailIdSelect = new Select(driver.findElement(By.id("emailtmplt:content:emailid")));
			emailIdSelect.selectByVisibleText("Y");
			
			//Select varNameSelect = new Select(driver.findElement(By.id("emailtmplt:content:vname")));
			//varNameSelect.selectByValue("SubscriberAddressId");
			// XXX StaleElementReferenceException: stale element reference: element is not attached to the page document
			
			//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("insert_variable")));
			//wait.until(ExpectedConditions.elementToBeClickable(By.id("insert_variable")));
			//driver.findElement(By.id("insert_variable")).click();
			// XXX StaleElementReferenceException: stale element reference: element is not attached to the page document
			
			//wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:content:bodytext")));
			
			Actions builder = new Actions(driver);
			builder.moveToElement(driver.findElement(By.id("emailtmplt:content:bodytext"))).build().perform();
			
			//wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:content:bodytext")));
			//driver.findElement(By.id("emailtmplt:content:bodytext")).sendKeys("Test message body " + suffix);
			// XXX ElementNotVisibleException: element not visible
			
			// Submit changes
			driver.findElement(By.cssSelector("input[title='Submit changes']")).click();
			
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

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			// Verify update results
			WebElement tmpltIdVrf = driver.findElement(By.cssSelector("a[title='" + nextIdx + "_viewDetail']"));
			assertEquals(testTmpltId, tmpltIdVrf.getText());
			
			// Delete added record
			driver.findElement(By.cssSelector("input[title='" + nextIdx + "_checkBox']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + nextIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Delete selected rows']")).click();
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmplst:footer:gettingStartedFooter")));

		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

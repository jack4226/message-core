package ltj.msgui2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import ltj.selenium.AlertUtil;

public class EmailTemplateTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(EmailTemplateTest.class);

	@Test
	public void testViewListAndDetails() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Templates";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:templateid")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:subject")));
			
			WebElement tmpltIdElm = driver.findElement(By.id("emailtmplt:detail:templateid"));
			assertEquals(templateId, tmpltIdElm.getAttribute("value"));
			
			Select listIdSelect = new Select(driver.findElement(By.id("emailtmplt:detail:listid")));
			assertEquals(listId, listIdSelect.getFirstSelectedOption().getAttribute("value"));
			
			WebElement subjectElm = driver.findElement(By.id("emailtmplt:detail:subject"));
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
			
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Submit changes']")));
			
			// Submit changes
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Submit changes']"));
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("span[title='" + subjectTitle + "']")));
			
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
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			
			// Copy from selected
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Create a new row from selected']"));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:templateid")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:listtype")));
			
			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			String testTmpltId = "TestTemplate_" + suffix;
			driver.findElement(By.id("emailtmplt:detail:templateid")).sendKeys(testTmpltId);
						
			Select listTypeSelect = new Select(driver.findElement(By.id("emailtmplt:detail:listtype")));
			String valueBefore = listTypeSelect.getFirstSelectedOption().getAttribute("value");
			String valueAfter = null;
			if ("Traditional".equals(valueBefore)) {
				valueAfter = "Personalized";
			}
			else {
				valueAfter = "Traditional";
				
			}
			listTypeSelect.selectByValue(valueAfter);
			
			wait.until(ExpectedConditions.textToBePresentInElementValue(By.id("emailtmplt:detail:listtype"), valueAfter));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Submit changes']")));
			
			// Submit changes
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Submit changes']"));
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			// wait until the last record become available
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='" + nextIdx + "_viewDetail']")));
			
			// Verify update results
			List<WebElement> viewDetailList = driver.findElements(By.cssSelector("a[title$='_viewDetail']"));
			WebElement viewDetailElm = null;
			for (WebElement elm : viewDetailList) {
				logger.info("viewDetail item: " + elm.getAttribute("title") + ", " + elm.getText());
				if (StringUtils.equals(testTmpltId, elm.getText())) {
					viewDetailElm = elm;
					break;
				}
			}
			assertNotNull(viewDetailElm);
			String tmpltTitle = viewDetailElm.getAttribute("title");
			String tmpltIdx = StringUtils.removeEnd(tmpltTitle, "_viewDetail");
			
			// Delete added record
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']")));
			driver.findElement(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Delete selected rows']")).click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
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
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			
			Actions builder = new Actions(driver);
			
			// Add a new record
			driver.findElement(By.cssSelector("input[title='Add a new row']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:templateid")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:bodytext")));
			
			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			String testTmpltId = "TestTemplate_" + suffix;
			
			driver.findElement(By.id("emailtmplt:detail:templateid")).sendKeys(testTmpltId);
			
			Select listIdSelect = new Select(driver.findElement(By.id("emailtmplt:detail:listid")));
			listIdSelect.selectByValue("SMPLLST2");
			
			Select listTypeSelect = new Select(driver.findElement(By.id("emailtmplt:detail:listtype")));
			listTypeSelect.selectByValue("Personalized"); // this will trigger an ajax event
			
			wait.until(ExpectedConditions.textToBePresentInElementValue(By.id("emailtmplt:detail:listtype"), "Personalized"));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:subject")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("emailtmplt:detail:subject")));
			
			driver.findElement(By.id("emailtmplt:detail:subject")).sendKeys("Test Subject " + suffix);
			
			wait.until(ExpectedConditions.elementToBeClickable(By.id("emailtmplt:detail:emailid")));
			builder.moveToElement(driver.findElement(By.id("emailtmplt:detail:emailid"))).build().perform();
			
			Select emailIdSelect = new Select(driver.findElement(By.id("emailtmplt:detail:emailid")));
			emailIdSelect.selectByVisibleText("Y");
			
			Select varNameSelect = new Select(driver.findElement(By.id("emailtmplt:detail:vname")));
			varNameSelect.selectByValue("SubscriberAddressId");
			// XXX StaleElementReferenceException: stale element reference: element is not attached to the page document
			
			//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("insert_variable")));
			//wait.until(ExpectedConditions.elementToBeClickable(By.id("insert_variable")));
			//driver.findElement(By.id("insert_variable")).click();
			// XXX StaleElementReferenceException: stale element reference: element is not attached to the page document
			
			//wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:bodytext")));
			
			
			builder.moveToElement(driver.findElement(By.id("emailtmplt:detail:bodytext"))).build().perform();
			
			//wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:bodytext")));
			//driver.findElement(By.id("emailtmplt:detail:bodytext")).sendKeys("Test message body " + suffix);
			// XXX ElementNotVisibleException: element not visible
			
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Submit changes']")));
			
			// Submit changes
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Submit changes']"));
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			// wait until the last record become available
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='" + nextIdx + "_viewDetail']")));
			
			// Verify update results
			List<WebElement> viewDetailList =  driver.findElements(By.cssSelector("a[title$='_viewDetail']"));
			WebElement viewDetailElm = null;
			for (WebElement elm : viewDetailList) {
				logger.info("viewDetail item: " + elm.getAttribute("title") + ", " + elm.getText());
				if (StringUtils.equals(testTmpltId, elm.getText())) {
					viewDetailElm = elm;
					break;
				}
			}
			assertNotNull(viewDetailElm);
			String tmpltTitle = viewDetailElm.getAttribute("title");
			String tmpltIdx = StringUtils.removeEnd(tmpltTitle, "_viewDetail");
			
			// Delete added record
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']")));
			driver.findElement(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Delete selected rows']")).click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}

package ltj.msgui2;

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

public class MailingListTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(MailingListTest.class);

	@Test
	public void testViewAndAddNewRecord() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Mailing Lists";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			
			List<WebElement> viewDetailList = driver.findElements(By.cssSelector("a[title$='_viewDetail']"));
			assertEquals(checkBoxList.size(), viewDetailList.size());
			String listId0 = viewDetailList.get(0).getText();
			
			List<WebElement> dispNameList = driver.findElements(By.cssSelector("span[title$='_dispName']"));
			assertEquals(checkBoxList.size(), dispNameList.size());
			String dispName0 = dispNameList.get(0).getText();
			
			// View Detail page
			WebElement viewDetailLink = viewDetailList.get(0);
			String viewDetailTitle = viewDetailLink.getAttribute("title");
			viewDetailLink = driver.findElement(By.cssSelector("a[title='" + viewDetailTitle + "']"));
			viewDetailLink.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mlstedit:footer:gettingStartedFooter")));
			
			assertEquals(listId0, driver.findElement(By.id("mlstedit:content:listid")).getAttribute("value"));
			assertEquals(dispName0, driver.findElement(By.id("mlstedit:content:dispname")).getAttribute("value"));
			
			// Go back to the list
			driver.findElement(By.cssSelector("input[title='Go Back']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			// Add a new record
			WebElement copySelectedLink = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			copySelectedLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mlstedit:footer:gettingStartedFooter")));

			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			
			WebElement listIdElm = driver.findElement(By.id("mlstedit:content:listid"));
			String listIdNew = "TstId" + suffix;
			listIdElm.sendKeys(listIdNew);

			Actions builder = new Actions(driver);
			
			builder.moveToElement(driver.findElement(By.id("mlstedit:content:dispname"))).build().perform();

			WebElement dispNameElm = driver.findElement(By.id("mlstedit:content:dispname"));
			dispNameElm.sendKeys("Test List " + suffix);
			
			WebElement acctUserElm = driver.findElement(By.id("mlstedit:content:acctuser"));
			assertEquals("", acctUserElm.getAttribute("value"));
			acctUserElm.sendKeys("test_" + suffix);
			
			Select dataTypeSelect = new Select(driver.findElement(By.id("mlstedit:content:clientid")));
			assertEquals("System", dataTypeSelect.getFirstSelectedOption().getText()); 
			
			driver.findElement(By.id("mlstedit:content:desc")).sendKeys("Test List add new " + suffix);
			driver.findElement(By.id("mlstedit:content:mstraddr")).sendKeys("test" + suffix + "@localhost");
			
			// Submit changes
			WebElement submitChanges = driver.findElement(By.cssSelector("input[title='Submit changes']"));
			submitChanges.click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			// Delete the added record
			WebElement chkboxLink = driver.findElement(By.cssSelector("input[title='" + listIdNew + "_checkBox']"));
			chkboxLink.click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + listIdNew + "_checkBox']"), true));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			WebElement deleteLink = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			deleteLink.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("span[id^='" + nextIdx + "_']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testCopyFromSelected() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Mailing Lists";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			
			List<WebElement> dispNameList = driver.findElements(By.cssSelector("span[title$='_dispName']"));
			assertEquals(checkBoxList.size(), dispNameList.size());
			
			// Tick the selected record
			WebElement checkBoxLink = checkBoxList.get(0);
			String chkboxTitle = checkBoxLink.getAttribute("title");
			checkBoxLink = driver.findElement(By.cssSelector("input[title='" + chkboxTitle + "']"));
			checkBoxLink.click();

			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + chkboxTitle + "']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			// Copy from selected
			WebElement copySelectedLink = driver.findElement(By.cssSelector("input[title='Create a new row from selected']"));
			copySelectedLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mlstedit:footer:gettingStartedFooter")));

			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			
			WebElement listIdElm = driver.findElement(By.id("mlstedit:content:listid"));
			assertEquals("", listIdElm.getAttribute("value"));
			String listIdNew = "TstId" + suffix;
			listIdElm.sendKeys(listIdNew);

			Actions builder = new Actions(driver);
			
			builder.moveToElement(driver.findElement(By.id("mlstedit:content:dispname"))).build().perform();

			WebElement dispNameElm = driver.findElement(By.id("mlstedit:content:dispname"));
			assertTrue(StringUtils.isNotBlank(dispNameElm.getAttribute("value")));
			dispNameElm.sendKeys("_" + suffix);
			
			WebElement acctUserElm = driver.findElement(By.id("mlstedit:content:acctuser"));
			assertEquals("", acctUserElm.getAttribute("value"));
			acctUserElm.sendKeys("test_" + suffix);
			
			Select dataTypeSelect = new Select(driver.findElement(By.id("mlstedit:content:clientid")));
			assertEquals("System", dataTypeSelect.getFirstSelectedOption().getText()); 
			
			// Submit changes
			WebElement submitChanges = driver.findElement(By.cssSelector("input[title='Submit changes']"));
			submitChanges.click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			// Delete the added record
			WebElement chkboxLink = driver.findElement(By.cssSelector("input[title='" + listIdNew + "_checkBox']"));
			chkboxLink.click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + listIdNew + "_checkBox']"), true));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			WebElement deleteLink = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			deleteLink.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("span[id^='" + nextIdx + "_']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	@Test
	public void testViewSubscrbers() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Mailing Lists";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> viewSbsrsList = driver.findElements(By.cssSelector("a[title$='_viewSbsrs']"));
			assertFalse(viewSbsrsList.isEmpty());
			WebElement viewSbsrsLink = null;
			for (WebElement elm : viewSbsrsList) {
				if (StringUtils.startsWith(elm.getAttribute("title"), "SMPLLST1")) {
					viewSbsrsLink = elm;
				}
			}
			assertNotNull(viewSbsrsLink);
			
			// View Subscribers page
			String viewSbsrsTitle = viewSbsrsLink.getAttribute("title");
			viewSbsrsLink = driver.findElement(By.cssSelector("a[title='" + viewSbsrsTitle + "']"));
			viewSbsrsLink.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("subrlist:footer:gettingStartedFooter")));
			
			// View List page
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			
			int idx = new Random().nextInt(checkBoxList.size() - 1);
			WebElement checkBoxLink = checkBoxList.get(idx);
			String checkBoxTitle = checkBoxLink.getAttribute("title");
			
			List<WebElement> emailAddrList = driver.findElements(By.cssSelector("span[title$='_emailAddr']"));
			assertEquals(checkBoxList.size(), emailAddrList.size());
			
			List<WebElement> acceptHtmlList = driver.findElements(By.cssSelector("span[title$='_acceptHtml']"));
			assertEquals(checkBoxList.size(), acceptHtmlList.size());
			
			List<WebElement> subscribedList = driver.findElements(By.cssSelector("select[title$='_subscribed']"));
			assertEquals(checkBoxList.size(), subscribedList.size());
			
			WebElement subedElm = subscribedList.get(idx);
			String subedTitle = subedElm.getAttribute("title");
			
			Select subedSelect = new Select(driver.findElement(By.cssSelector("select[title='" + subedTitle + "'")));
			WebElement isSubed = subedSelect.getFirstSelectedOption();
			if ("true".equals(isSubed.getAttribute("value"))) {
				//subedSelect.selectByValue("false");
			}
			else {
				subedSelect.selectByValue("true");
			}
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("select[title='" + subedTitle + "'")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='" + subedTitle + "'")));
			
			checkBoxLink = driver.findElement(By.cssSelector("input[title='" + checkBoxTitle + "'"));
			checkBoxLink.click();
			
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Save selected rows'")));
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + checkBoxTitle + "'"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='" + checkBoxTitle + "'")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("subrlist:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Save selected rows'")).click();
			
			AlertUtil.handleAlert(driver);

			// Go back to the list
			driver.findElement(By.cssSelector("input[title='Go Back']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillistlst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

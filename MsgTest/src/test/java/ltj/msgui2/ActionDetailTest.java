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

public class ActionDetailTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(ActionDetailTest.class);

	@Test
	public void testViewListAndAddNew() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Customize Action Details";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("input[title$='_checkBox']")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			
			List<WebElement> viewDetailList = driver.findElements(By.cssSelector("a[title$='_viewDetail']"));
			assertEquals(checkBoxList.size(), viewDetailList.size());
			
			List<WebElement> descriptionList = driver.findElements(By.cssSelector("span[title$='_description']"));
			assertEquals(checkBoxList.size(), descriptionList.size());
			String desc0 = descriptionList.get(0).getText();
			
			List<WebElement> serviceNameList = driver.findElements(By.cssSelector("span[title$='_serviceName']"));
			assertEquals(checkBoxList.size(), serviceNameList.size());
			String bean0 = serviceNameList.get(0).getText();
			
			// View/Edit Details page
			WebElement viewDetailLink = viewDetailList.get(0);
			viewDetailLink = driver.findElement(By.cssSelector("a[title='0_viewDetail']"));
			viewDetailLink.click();

			logger.info("View Detail page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:detail:description")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:detail:beanid")));
			
			WebElement descElm = driver.findElement(By.id("actdtllst:detail:description"));
			assertEquals(desc0, descElm.getAttribute("value"));
			
			WebElement beanElm = driver.findElement(By.id("actdtllst:detail:beanid"));
			assertEquals(bean0, beanElm.getAttribute("value"));
			
			WebElement goBackLink = driver.findElement(By.cssSelector("input[title='Go Back']"));
			goBackLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Add a new row']")));
			
			// Add a new record
			WebElement addNewLink = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			addNewLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:detail:actionid")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:detail:description")));

			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			
			WebElement actionIdElm = driver.findElement(By.id("actdtllst:detail:actionid"));
			actionIdElm.sendKeys("TestId_" + suffix);

			Actions builder = new Actions(driver);
			
			builder.moveToElement(driver.findElement(By.id("actdtllst:detail:description"))).build().perform();

			driver.findElement(By.id("actdtllst:detail:description")).sendKeys("Test Action " + suffix);
			driver.findElement(By.id("actdtllst:detail:beanid")).sendKeys(bean0);
			
			Select dataTypeSelect = new Select(driver.findElement(By.id("actdtllst:detail:datatype")));
			dataTypeSelect.selectByValue("EMAIL_ADDRESS"); 
			
			// Submit changes
			WebElement submitChanges = driver.findElement(By.cssSelector("input[title='Submit changes']"));
			submitChanges.click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='" + nextIdx + "_checkBox']")));
			
			// Delete the added record
			WebElement chkboxLink = driver.findElement(By.cssSelector("input[title='" + nextIdx + "_checkBox']"));
			chkboxLink.click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + nextIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			
			WebElement deleteLink = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			deleteLink.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("span[id^='" + nextIdx + "_']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testCopyFromSelected() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Customize Action Details";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("input[title$='_checkBox']")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			
			List<WebElement> serviceNameList = driver.findElements(By.cssSelector("span[title$='_serviceName']"));
			assertEquals(checkBoxList.size(), serviceNameList.size());
			String bean_n = serviceNameList.get(nextIdx - 1).getText();
			
			// Tick the selected record
			WebElement checkBoxLink = checkBoxList.get(nextIdx - 1);
			checkBoxLink = driver.findElement(By.cssSelector("input[title='" + (nextIdx - 1) + "_checkBox']"));
			checkBoxLink.click();

			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + (nextIdx - 1) + "_checkBox']"), true));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			
			// Copy from selected
			WebElement copySelectedLink = driver.findElement(By.cssSelector("input[title='Create a new row from selected']"));
			copySelectedLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:detail:description")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:detail:beanid")));

			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			
			WebElement actionIdElm = driver.findElement(By.id("actdtllst:detail:actionid"));
			assertEquals("", actionIdElm.getAttribute("value"));
			actionIdElm.sendKeys("TestActionId_" + suffix);

			Actions builder = new Actions(driver);
			
			builder.moveToElement(driver.findElement(By.id("actdtllst:detail:description"))).build().perform();

			WebElement descElm = driver.findElement(By.id("actdtllst:detail:description"));
			assertTrue(StringUtils.isNotBlank(descElm.getAttribute("value")));
			descElm.sendKeys("_" + suffix);
			
			WebElement beanElm = driver.findElement(By.id("actdtllst:detail:beanid"));
			assertEquals(bean_n, beanElm.getAttribute("value"));
			
			Select dataTypeSelect = new Select(driver.findElement(By.id("actdtllst:detail:datatype")));
			dataTypeSelect.selectByValue("EMAIL_ADDRESS"); 
			
			// Submit changes
			WebElement submitChanges = driver.findElement(By.cssSelector("input[title='Submit changes']"));
			submitChanges.click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='" + nextIdx + "_checkBox']")));
			
			// Delete the added record
			WebElement chkboxLink = driver.findElement(By.cssSelector("input[title='" + nextIdx + "_checkBox']"));
			chkboxLink.click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + nextIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			
			WebElement deleteLink = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			deleteLink.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("span[id^='" + nextIdx + "_']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actdtllst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

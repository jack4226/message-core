package ltj.msgui2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import ltj.selenium.AlertUtil;

public class EmailVariableTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(EmailVariableTest.class);

	@Test
	public void testViewListAndDetails() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Variables";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
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
			
			List<WebElement> defaultValueList = driver.findElements(By.cssSelector("span[title$='_defaultValue']"));
			assertEquals(checkBoxList.size(), defaultValueList.size());
			String defaultValueTitle = defaultValueList.get(idx).getAttribute("title");
			String defaulValueBefore = defaultValueList.get(idx).getText();

			List<WebElement> viewDetailList = driver.findElements(By.cssSelector("a[title$='_viewDetail']"));
			assertEquals(checkBoxList.size(), viewDetailList.size());
			WebElement viewDetailLink = viewDetailList.get(idx);
			String viewDetailTitle = viewDetailLink.getAttribute("title");
			
			// View Details page
			viewDetailLink = driver.findElement(By.cssSelector("a[title='" + viewDetailTitle + "']"));
			String variableName = viewDetailLink.getText();
			viewDetailLink.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			WebElement varNameElm = driver.findElement(By.id("emailvarbl:content:variablename"));
			assertEquals(variableName, varNameElm.getAttribute("value"));
			assertEquals(false, varNameElm.isEnabled());
			
			WebElement defaulElm = driver.findElement(By.id("emailvarbl:content:defaultvalue"));
			String defaultValueAfter = defaulElm.getAttribute("value");
			assertEquals(defaulValueBefore, defaultValueAfter);
			if (StringUtils.endsWith(defaultValueAfter, "_updated")) {
				defaultValueAfter = StringUtils.removeEnd(defaultValueAfter, "_updated");
			}
			else {
				defaultValueAfter += "_updated";
			}
			defaulElm.clear();
			defaulElm.sendKeys(defaultValueAfter);
			
			// Submit changes
			driver.findElement(By.cssSelector("input[title='Submit changes']")).click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Verify update results
			WebElement defaultVrf = driver.findElement(By.cssSelector("span[title='" + defaultValueTitle + "']"));
			assertEquals(defaultValueAfter, defaultVrf.getText());
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testCopyFromSelected() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Variables";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
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
			
			int idx = new Random().nextInt(titleList.size() - 1);
			String checkBoxTitle = titleList.get(idx);
			
			// Tick a selected record
			driver.findElement(By.cssSelector("input[title='" + checkBoxTitle + "']")).click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + checkBoxTitle + "']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Copy from selected
			driver.findElement(By.cssSelector("input[title='Create a new row from selected']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			String testVarName = "TestVariable_" + suffix;
			driver.findElement(By.id("emailvarbl:content:variablename")).sendKeys(testVarName);
			
			// Submit changes
			driver.findElement(By.cssSelector("input[title='Submit changes']")).click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Delete added record
			driver.findElement(By.cssSelector("input[title='" + nextIdx + "_checkBox']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + nextIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Delete selected rows']")).click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testAddNewRecord() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Variables";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			
			// Add a new record
			driver.findElement(By.cssSelector("input[title='Add a new row']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			String testVarName = "TestVariable_" + suffix;
			driver.findElement(By.id("emailvarbl:content:variablename")).sendKeys(testVarName);
			
			Select varTypeSelect = new Select(driver.findElement(By.id("emailvarbl:content:varbltype")));
			varTypeSelect.selectByVisibleText("Customer");
			
			driver.findElement(By.id("emailvarbl:content:tablename")).sendKeys("subscriber_data");
			
			driver.findElement(By.id("emailvarbl:content:columnname")).sendKeys("LastName");
			
			driver.findElement(By.id("emailvarbl:content:defaultvalue")).sendKeys("Valued Customer " + suffix);
			
			String query ="SELECT c.LastName as ResultStr FROM subscriber_data c, email_address e where e.row_id=c.EmailAddrRowId and e.row_id=?1";
			driver.findElement(By.id("emailvarbl:content:variablequery")).sendKeys(query);
			
			driver.findElement(By.id("emailvarbl:content:variableproc")).sendKeys("jpa.service.external.SubscriberNameResolver");
			
			// Submit changes
			driver.findElement(By.cssSelector("input[title='Submit changes']")).click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Verify results
			WebElement viewDetailLink = driver.findElement(By.cssSelector("a[title='" + nextIdx + "_viewDetail']"));
			assertEquals(testVarName, viewDetailLink.getText());
			
			WebElement defaultValue = driver.findElement(By.cssSelector("span[title='" + nextIdx + "_defaultValue']"));
			assertEquals("Valued Customer " + suffix, defaultValue.getText());
			
			WebElement beanClass = driver.findElement(By.cssSelector("span[title='" + nextIdx + "_className']"));
			assertTrue(StringUtils.contains("jpa.service.external.SubscriberNameResolver", beanClass.getText()));
			
			// Delete added record
			driver.findElement(By.cssSelector("input[title='" + nextIdx + "_checkBox']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + nextIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Delete selected rows']")).click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Go back to the list
//			driver.findElement(By.cssSelector("input[title='Go Back']")).click();
//			
//			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

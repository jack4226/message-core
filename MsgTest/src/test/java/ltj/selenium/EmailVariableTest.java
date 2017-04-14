package ltj.selenium;

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

public class EmailVariableTest extends BaseLogin {
	static final Logger logger = Logger.getLogger(EmailVariableTest.class);

	@Test
	public void testEmailVariables() {
		String listTitle = "Setup Email Variables";
		String testVarName = "CustomerLastName";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			// Email Variable List page
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement defaultValue = driver.findElement(By.cssSelector("span[title='CustomerLastName_defaultValue']"));
			assertNotNull(defaultValue);
			String defaultValueBefore = defaultValue.getText();
			assertTrue(defaultValueBefore.startsWith("Valued Customer"));
			
			WebElement className = driver.findElement(By.cssSelector("span[title='CustomerLastName_className']"));
			assertNotNull(className);
			String classNameShort = className.getText();
			assertEquals(".CustomerNameResolver", className.getText());
			
			WebElement listStatId = driver.findElement(By.cssSelector("span[title='CustomerLastName_statusId']"));
			assertNotNull(listStatId);
			assertEquals("Active", listStatId.getText());
			String statusId = listStatId.getText();
			
			WebElement listEditLink = driver.findElement(By.cssSelector("a[title=CustomerLastName]"));
			assertNotNull(listEditLink);
			assertEquals(testVarName, listEditLink.getText());
			
			listEditLink.click();
			
			// Email Variable Edit Page
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			WebElement varNameElm = driver.findElement(By.id("emailvarbl:content:variablename"));
			assertNotNull(varNameElm);
			assertEquals(testVarName, varNameElm.getAttribute("value"));
			assertFalse(varNameElm.isEnabled());
			
			WebElement colNameElm = driver.findElement(By.id("emailvarbl:content:columnname"));
			assertNotNull(colNameElm);
			assertEquals("LastName", colNameElm.getAttribute("value"));
			
			WebElement procNameElm = driver.findElement(By.id("emailvarbl:content:variableproc"));
			assertNotNull(procNameElm);
			assertTrue(procNameElm.getAttribute("value").endsWith(classNameShort));
			
			WebElement defaultElm = driver.findElement(By.id("emailvarbl:content:defaultvalue"));
			// TextArea - use getAttribute("value") instead of getText()
			String defaultValueAfter = defaultElm.getAttribute("value");
			assertEquals(defaultValueBefore, defaultValueAfter);
			if (StringUtils.endsWith(defaultValueAfter, "_updated")) {
				defaultValueAfter = StringUtils.removeEnd(defaultValueAfter, "_updated");
			}
			else {
				defaultValueAfter += "_updated";
			}
			assertFalse(defaultValueAfter.equals(defaultValueBefore));
			defaultElm.clear();
			defaultElm.sendKeys(defaultValueAfter);
 			
			Select selectStatId = new Select(driver.findElement(By.id("emailvarbl:content:statusid")));
			WebElement selectedStatId = selectStatId.getFirstSelectedOption();
			logger.info("Status Id selected: " + selectedStatId.getText());
			if ("Active".equals(statusId)) {
				assertEquals("A", selectedStatId.getAttribute("value"));
			}
			else {
				assertEquals("I", selectedStatId.getAttribute("value"));
			}
			
			// Click on Test button to test SQL query
			WebElement testQuery = driver.findElement(By.id("emailvarbl:content:testquery"));
			assertNotNull(testQuery);
			testQuery.click();
			// Verify query test result
			WebElement testResult = driver.findElement(By.id("emailvarbl:content:testResult"));
			assertEquals("Test was successful, query is valid.", testResult.getText());
			
			// submit the changes and go back to list page
			WebElement submit = driver.findElement(By.id("emailvarbl:content:submit"));
			assertNotNull(submit);
			submit.click();
			
			// Accept (Click OK on JavaScript Alert pop-up)
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}
			
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			WebElement refreshLink = driver.findElement(By.cssSelector("input[title='Refresh from database']"));
			refreshLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("advanced:footer:gettingStartedFooter")));
			
			// verify results
			WebElement defaultValueAfterElm = driver.findElement(By.cssSelector("span[title='CustomerFirstName_defaultValue']"));
			assertNotNull(defaultValueAfterElm);
			// TODO fix this when running from Maven
			//assertEquals(defaultValueAfter, defaultValueAfterElm.getText());
			
			
			// View SubscriberUrl
			WebElement subrUrlLink = driver.findElement(By.cssSelector("a[title=SubscribeURL]"));
			assertEquals("SubscribeURL", subrUrlLink.getText());
			
			subrUrlLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			// SubscriberURL edit page
			varNameElm = driver.findElement(By.id("emailvarbl:content:variablename"));
			assertEquals("SubscribeURL", varNameElm.getAttribute("value"));
			assertFalse(varNameElm.isEnabled());
			
			defaultElm = driver.findElement(By.id("emailvarbl:content:defaultvalue"));
			assertTrue(defaultElm.getText().contains("sbsrid=${SubscriberAddressId}"));
			
			// Go back to the list page
			WebElement cancel = driver.findElement(By.cssSelector("input[title='Cancel changes']"));
			cancel.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("advanced:footer:gettingStartedFooter")));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}

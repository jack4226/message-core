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

public class ActionDetailsTest extends BaseLogin {
	static final Logger logger = Logger.getLogger(ActionDetailsTest.class);

	@Test
	public void testActionDetails() {
		String listTitle = "Customize Action Details";
		String testActionName = "ASSIGN_RULENAME";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			// Action Details List page
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement desc = driver.findElement(By.cssSelector("span[title='ASSIGN_RULENAME_desc']"));
			assertNotNull(desc);
			String descBefore = desc.getText();
			assertTrue(descBefore.startsWith("set a rule mame and re-process"));
			
			WebElement prodBeanId = driver.findElement(By.cssSelector("span[title='ASSIGN_RULENAME_beanId']"));
			assertNotNull(prodBeanId);
			assertEquals("assignRuleNameBo", prodBeanId.getText());
			
			WebElement dataType = driver.findElement(By.cssSelector("span[title='ASSIGN_RULENAME_dataType']"));
			assertNotNull(dataType);
			assertEquals("RULE_NAME", dataType.getText());
			
			WebElement actionEditLink = driver.findElement(By.cssSelector("a[title=ASSIGN_RULENAME]"));
			assertNotNull(actionEditLink);
			logger.info("Assign Rule Name: " + actionEditLink.getText());
			assertEquals(testActionName, actionEditLink.getText());
			
			actionEditLink.click();
			
			// Action Details Edit Page
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("advanced:footer:gettingStartedFooter")));
			
			WebElement actionNameElm = driver.findElement(By.id("advanced:content:actionid"));
			assertNotNull(actionNameElm);
			assertEquals(false, actionNameElm.isEnabled());
			assertEquals(testActionName, actionNameElm.getAttribute("value"));
			
			WebElement actionDescElm = driver.findElement(By.id("advanced:content:description"));
			assertNotNull(actionDescElm);
			String actionDesc = actionDescElm.getAttribute("value");
			assertTrue(actionDesc.startsWith("set a rule mame and re-process"));
			if (StringUtils.endsWith(actionDesc, "_updated")) {
				actionDesc = StringUtils.removeEnd(actionDesc, "_updated");
			}
			else {
				actionDesc += "_updated";
			}
			assertFalse(descBefore.equals(actionDesc));
			actionDescElm.clear();
			actionDescElm.sendKeys(actionDesc);
 			
			WebElement beanIdElm = driver.findElement(By.id("advanced:content:beanid"));
			assertNotNull(beanIdElm);
			assertEquals("assignRuleNameBo", beanIdElm.getAttribute("value"));
 			
			Select selectDataType = new Select(driver.findElement(By.id("advanced:content:datatype")));
			WebElement selectedDataType = selectDataType.getFirstSelectedOption();
			logger.info("Data Type selected: " + selectedDataType.getText());
			assertEquals("RULE_NAME", selectedDataType.getAttribute("value"));
			
			// submit the changes and go back to list page
			WebElement submit = driver.findElement(By.id("advanced:content:submit"));
			assertNotNull(submit);
			submit.click();
			
			// Accept (Click OK on JavaScript Alert pop-up)
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Cancelled the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}
			
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			// verify results
			WebElement descElm = driver.findElement(By.cssSelector("span[title='ASSIGN_RULENAME_desc']"));
			assertNotNull(descElm);
			assertEquals(actionDesc, descElm.getText());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}

package ltj.msgui1;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BuiltinRuleTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(BuiltinRuleTest.class);

	@Test
	public void testBuiltinRules() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText("Customize Actions for Built-in Rules"));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Customize Actions for Built-in Rules"));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement bounceElm = driver.findElement(By.cssSelector("span[title=HARD_BOUNCE_ruleName]"));
			assertNotNull(bounceElm);
			logger.info("Hard Bounce: " + bounceElm.toString() + ", text: " + bounceElm.getText());
			assertEquals("HARD_BOUNCE", bounceElm.getText());
			
			WebElement typeElm = driver.findElement(By.cssSelector("span[title=HARD_BOUNCE_ruleType]"));
			assertNotNull(typeElm);
			logger.info("Rule Type: " + typeElm.toString() + ", text: " + typeElm.getText());
			assertEquals("Any", typeElm.getText());
			
			link = driver.findElement(By.cssSelector("a[title='HARD_BOUNCE']"));
			assertNotNull(link);
			
			link.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionbiedit:footer:gettingStartedFooter")));
			
			WebElement ruleNameElm = driver.findElement(By.id("actionbiedit:content:rulename"));
			assertNotNull(ruleNameElm);
			logger.info("Rule Name: " + ruleNameElm.getText());
			assertEquals("HARD_BOUNCE", ruleNameElm.getText());
			
			WebElement ruleTypeElm = driver.findElement(By.id("actionbiedit:content:ruletype"));
			assertNotNull(ruleTypeElm);
			assertEquals("Any", ruleTypeElm.getText());
 			
			WebElement actSeq0Elm = driver.findElement(By.id("actionbiedit:content:builtin:0:actionseq"));
			assertNotNull(actSeq0Elm);
			assertEquals("1", actSeq0Elm.getAttribute("value"));
			
			Select selectActId1 = new Select(driver.findElement(By.id("actionbiedit:content:builtin:0:actionid")));
			WebElement selectedActId1 = selectActId1.getFirstSelectedOption();
			logger.info("Action Id 1 selected: " + selectedActId1.getText());
			assertEquals("SAVE", selectedActId1.getAttribute("value"));
			
			Select selectStatId = new Select(driver.findElement(By.id("actionbiedit:content:builtin:0:statusid")));
			WebElement selectedStatId = selectStatId.getFirstSelectedOption();
			logger.info("Status Id selected: " + selectedStatId.getText());
			assertEquals("A", selectedStatId.getAttribute("value"));
			
			Select selectActId2 = new Select(driver.findElement(By.id("actionbiedit:content:builtin:1:actionid")));
			WebElement selectedActId2 = selectActId2.getFirstSelectedOption();
			logger.info("Action Id 2 selected: " + selectedActId2.getText());
			assertEquals("SUSPEND", selectedActId2.getAttribute("value"));
						
			Select selectDataType = new Select(driver.findElement(By.id("actionbiedit:content:builtin:1:datatypevalues1")));
			WebElement selectedDataType = selectDataType.getFirstSelectedOption();
			logger.info("Data Type selected: " + selectedDataType.getText());
			assertEquals("$FinalRcpt", selectedDataType.getAttribute("value"));
			

			Select selectActId3 = new Select(driver.findElement(By.id("actionbiedit:content:builtin:2:actionid")));
			WebElement selectedActId3 = selectActId3.getFirstSelectedOption();
			logger.info("Action Id 3 selected: " + selectedActId3.getText());
			assertEquals("MARK_DLVR_ERR", selectedActId3.getAttribute("value"));

			Select selectActId4 = new Select(driver.findElement(By.id("actionbiedit:content:builtin:3:actionid")));
			WebElement selectedActId4 = selectActId4.getFirstSelectedOption();
			logger.info("Action Id 4 selected: " + selectedActId4.getText());
			assertEquals("CLOSE", selectedActId4.getAttribute("value"));

			
			WebElement submit = driver.findElement(By.id("actionbiedit:content:submit"));
			assertNotNull(submit);
			submit.click();
			
			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.dismiss(); // cancel the alert to prevent changes to the database
				logger.info("Cancelled the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

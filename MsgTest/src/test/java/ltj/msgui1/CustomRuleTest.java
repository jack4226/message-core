package ltj.msgui1;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CustomRuleTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(CustomRuleTest.class);

	@Test
	public void testCustomRules() {
		String ruleListTitle = "Setup Custom Bounce Rules and Actions";
		String testRuleName = "OutOfOffice_AutoReply";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(ruleListTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(ruleListTitle));
			
			// Rule List page
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement ruleType = driver.findElement(By.cssSelector("span[title=OutOfOffice_AutoReply_ruleType]"));
			assertNotNull(ruleType);
			assertEquals("All", ruleType.getText());
			
			WebElement statusId = driver.findElement(By.cssSelector("span[title=OutOfOffice_AutoReply_statusId]"));
			assertNotNull(statusId);
			assertEquals("Active", statusId.getText());
			
			WebElement ruleEditLink = driver.findElement(By.cssSelector("a[title=OutOfOffice_AutoReply]"));
			assertNotNull(ruleEditLink);
			logger.info("OutOfOffice_AutoReply: " + ruleEditLink.getText());
			assertEquals(testRuleName, ruleEditLink.getText());
			
			ruleEditLink.click();
			
			// Rule Edit Page
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			WebElement ruleNameElm = driver.findElement(By.id("ruleedit:content:rulename"));
			assertNotNull(ruleNameElm);
			logger.info("Rule Name: " + ruleNameElm.getText());
			assertEquals(testRuleName, ruleNameElm.getAttribute("value"));
			
			WebElement ruleDescElm = driver.findElement(By.id("ruleedit:content:desc"));
			assertNotNull(ruleDescElm);
			assertEquals("Ouf of the office auto reply", ruleDescElm.getAttribute("value"));
 			
			Select selectStatId = new Select(driver.findElement(By.id("ruleedit:content:statusid")));
			WebElement selectedStatId = selectStatId.getFirstSelectedOption();
			logger.info("Status Id selected: " + selectedStatId.getText());
			assertEquals("A", selectedStatId.getAttribute("value"));
			
			Select selectMailType = new Select(driver.findElement(By.id("ruleedit:content:mailtype")));
			assertNotNull(selectMailType);
			WebElement selectedMailType = selectMailType.getFirstSelectedOption();
			assertEquals("SMTP Mail", selectedMailType.getText());
			
			Select selectCategory = new Select(driver.findElement(By.id("ruleedit:content:rulecategory")));
			WebElement selectedCategory = selectCategory.getFirstSelectedOption();
			logger.info("Rule Category selected: " + selectedCategory.getText());
			assertEquals("M", selectedCategory.getAttribute("value"));
			
			Select selectSubrule = new Select(driver.findElement(By.id("ruleedit:content:subrule")));
			WebElement selectedSubrule = selectSubrule.getFirstSelectedOption();
			logger.info("Is Subrule selected: " + selectedSubrule.getText());
			assertEquals("false", selectedSubrule.getAttribute("value"));
			
			
			List<WebElement> ruleTypes = driver.findElements(By.cssSelector("input[type='radio'][name='ruleedit:content:ruletype']"));
			boolean found = false;
			for (WebElement type : ruleTypes) {
				String checked = type.getAttribute("checked");
				if (StringUtils.isNotBlank(checked)) {
					assertEquals("All", type.getAttribute("value"));
					found = true;
					break;
				}
			}
			assertEquals(true, found);

			Select selectVarName = new Select(driver.findElement(By.cssSelector("select[title=Subject_change]")));
			WebElement selectedVarName = selectVarName.getFirstSelectedOption();
			logger.info("Variable name selected: " + selectedVarName.getText());
			assertEquals("Subject", selectedVarName.getAttribute("value"));

			Select selectCriteria = new Select(driver.findElement(By.cssSelector("select[title=Subject_criteria]")));
			WebElement selectedCriteria = selectCriteria.getFirstSelectedOption();
			logger.info("Criteria selected: " + selectedCriteria.getText());
			assertEquals("reg_ex", selectedCriteria.getAttribute("value"));

			
			WebElement viewElement = driver.findElement(By.cssSelector("a[title='Subject_viewElement']"));
			assertNotNull(viewElement);
			viewElement.click();
			
			// View Rule Element page
			logger.info("View Element URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			WebElement elmRuleName = driver.findElement(By.id("ruleedit:content:rulename"));
			logger.info("Element Rule Name selected: " + elmRuleName.getText());
			assertEquals(testRuleName, elmRuleName.getAttribute("value"));
			assertFalse(elmRuleName.isEnabled());
			
			Select selectDataName = new Select(driver.findElement(By.id("ruleedit:content:dataname")));
			WebElement selectedDataName = selectDataName.getFirstSelectedOption();
			logger.info("Data Name selected: " + selectedDataName.getText());
			assertEquals("Subject", selectedDataName.getAttribute("value"));
			
			// Cancel changes and go back to Rule Edit page
			WebElement cancelViewElement = driver.findElement(By.cssSelector("input[title='Cancel Changes']"));
			assertNotNull(cancelViewElement);
			cancelViewElement.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			// Click on submit change button on Rule Edit page
			WebElement submitRuleEdit = driver.findElement(By.id("ruleedit:content:submit"));
			assertNotNull(submitRuleEdit);
			submitRuleEdit.click();
			
			// Cancel change (Click Cancel on JavaScript Alert pop-up)
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.dismiss(); // cancel the alert to prevent changes to the database
				logger.info("Cancelled the alert successfully.");
				
				/* */
				// Go back to Rule List page
				WebElement cancelRuleEdit = driver.findElement(By.cssSelector("input[title='Cancel Changes']"));
				assertNotNull(cancelRuleEdit);
				cancelRuleEdit.click();
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running with HtmlUnitDriver
				logger.error("TimeoutException", e);
			}
			
			wait.until(ExpectedConditions.titleIs(ruleListTitle));
			
			// Locate View Action page
			WebElement viewActionLink = driver.findElement(By.cssSelector("a[title=OutOfOffice_AutoReply_viewAction]"));
			assertNotNull(viewActionLink);
			logger.info("View Action Link Test: " +viewActionLink.getText());
			assertEquals("Edit", viewActionLink.getText());
			
			// View/Edit Action page
			viewActionLink.click();
			
			wait.until(ExpectedConditions.titleIs("Edit Action Information"));
			
			assertEquals(testRuleName, driver.findElement(By.id("actionedit:content:rulename")).getText());
			
			assertEquals("Main", driver.findElement(By.id("actionedit:content:rulecategory")).getText());
			
			assertEquals("All", driver.findElement(By.id("actionedit:content:ruletype")).getText());
			
			Select selectAction1 = new Select(driver.findElement(By.id("actionedit:content:jsftable:0:actionid")));
			WebElement selectedAction1 = selectAction1.getFirstSelectedOption();
			assertEquals("SAVE", selectedAction1.getText());
			
			Select selectStat1 = new Select(driver.findElement(By.id("actionedit:content:jsftable:0:statusid")));
			assertEquals("Active", selectStat1.getFirstSelectedOption().getText());
			
			Select selectClient1 = new Select(driver.findElement(By.id("actionedit:content:jsftable:0:clientid")));
			assertEquals("", selectClient1.getFirstSelectedOption().getText());
			
			Select selectAction2 = new Select(driver.findElement(By.id("actionedit:content:jsftable:1:actionid")));
			WebElement selectedAction2 = selectAction2.getFirstSelectedOption();
			assertEquals("CLOSE", selectedAction2.getText());
			
			Select selectStat2 = new Select(driver.findElement(By.id("actionedit:content:jsftable:1:statusid")));
			assertEquals("Active", selectStat2.getFirstSelectedOption().getText());
			
			// Go back to Rule List page
			WebElement cancelAction = driver.findElement(By.cssSelector("input[title='Cancel changes']"));
			assertNotNull(cancelAction);
			cancelAction.click();
			
			wait.until(ExpectedConditions.titleIs(ruleListTitle));
			
			// Locate view SubRule page
			WebElement viewSubRuleLink = driver.findElement(By.cssSelector("a[title=Unattended_Mailbox_viewSubRule]"));
			assertNotNull(viewSubRuleLink);
			logger.info("View SubRule Link Text: " +viewSubRuleLink.getText());
			assertEquals("Add", viewSubRuleLink.getText());
			
			// View/Edit SubRule page
			viewSubRuleLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("subruleedit:footer:gettingStartedFooter")));
			
			assertEquals("smtpmail", driver.findElement(By.id("subruleedit:content:mailtype")).getText());
			
			assertEquals("Active", driver.findElement(By.id("subruleedit:content:statusid")).getText());
			
			// Go back to Rule List page
			WebElement cancelSubRule = driver.findElement(By.cssSelector("input[title='Cancel changes']"));
			assertNotNull(cancelSubRule);
			cancelSubRule.click();
			
			wait.until(ExpectedConditions.titleIs(ruleListTitle));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}

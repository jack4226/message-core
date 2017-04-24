package ltj.msgui2;

import java.util.List;

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
	public void testListAndViewDetail() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Customize Actions for Built-in Rules";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Retrieve field values from the list
			WebElement ruleNameElm = driver.findElement(By.cssSelector("span[title='Hard Bounce_ruleName']"));
			String ruleNameBefore = ruleNameElm.getText();
			
			WebElement ruleTypeElm = driver.findElement(By.cssSelector("span[title='Hard Bounce_ruleType']"));
			String ruleTypeBefore = ruleTypeElm.getText();
			
			WebElement categoryElm = driver.findElement(By.cssSelector("span[title='Hard Bounce_category']"));
			String categoryBefore = categoryElm.getText();
			
			WebElement viewActionsLink = driver.findElement(By.cssSelector("a[title='Hard Bounce_viewActions']"));
			viewActionsLink.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionbiedit:footer:gettingStartedFooter")));
			
			// View/Edit Detail Page
			WebElement ruleNameEdt = driver.findElement(By.id("actionbiedit:content:rulename"));
			assertEquals(ruleNameBefore, ruleNameEdt.getText());
			
			WebElement ruleTypeEdt = driver.findElement(By.id("actionbiedit:content:ruletype"));
			assertEquals(ruleTypeBefore, ruleTypeEdt.getText());
			
			WebElement categoryEdt = driver.findElement(By.id("actionbiedit:content:rulecategory"));
			assertEquals(categoryBefore, categoryEdt.getText());
			
			WebElement seqEdt = driver.findElement(By.id("actionbiedit:content:builtin:0:actionseq"));
			assertEquals("1", seqEdt.getAttribute("value"));
			
			List<WebElement> actSeqList = driver.findElements(By.cssSelector("input[id$=':actionseq']"));
			assertFalse(actSeqList.isEmpty());
			
			List<WebElement> actIdList = driver.findElements(By.cssSelector("select[id$=':actionid']"));
			assertEquals(actSeqList.size(), actIdList.size());
			for (int i = 0; i < actIdList.size(); i++) {
				Select selectActId = new Select(actIdList.get(i));
				WebElement selectedActId = selectActId.getFirstSelectedOption();
				if (i == 0) {
					assertEquals("SAVE", selectedActId.getAttribute("value"));
				}
				else if (i == 1) {
					assertEquals("SUSPEND", selectedActId.getAttribute("value"));
				}
				else if (i == 3) {
					assertEquals("CLOSE", selectedActId.getAttribute("value"));
				}
			}
			
			List<WebElement> statIdList = driver.findElements(By.cssSelector("select[id$=':statusid']"));
			assertEquals(actSeqList.size(), statIdList.size());
			for (WebElement elm : statIdList) {
				Select selectStatId = new Select(elm);
				WebElement selectedStatId = selectStatId.getFirstSelectedOption();
				assertEquals("A", selectedStatId.getAttribute("value"));
			}
			
			Select selectValues = new Select(driver.findElement(By.id("actionbiedit:content:builtin:1:datatypevalues1")));
			List<WebElement> selectedValues = selectValues.getAllSelectedOptions();
			assertFalse(selectedValues.isEmpty());
			assertEquals("$FinalRcpt", selectedValues.get(0).getAttribute("value"));
			
			// Go back to list
			WebElement submit = driver.findElement(By.cssSelector("input[title='Go Back']"));
			submit.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testAddNewAction() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Customize Actions for Built-in Rules";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Locate view/edit detail link
			WebElement viewActionsLink = driver.findElement(By.cssSelector("a[title='Mail Block_viewActions']"));
			viewActionsLink.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionbiedit:footer:gettingStartedFooter")));
			
			// View/Edit Detail Page
			List<WebElement> actSeqList = driver.findElements(By.cssSelector("input[id$=':actionseq']"));
			assertFalse(actSeqList.isEmpty());
			int listSize = actSeqList.size();
			
			List<WebElement> actIdList = driver.findElements(By.cssSelector("select[id$=':actionid']"));
			assertEquals(actSeqList.size(), actIdList.size());

			// Click on Add New button
			WebElement addNewLink = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			addNewLink.click();
			
			String prefix = "actionbiedit:content:builtin:" + listSize + ":";
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id(prefix + "statusid")));
			
			// make changes to the new record
			Select selectActId = new Select(driver.findElement(By.id(prefix + "actionid")));
			selectActId.selectByValue("BOUNCE_UP");
			
			Select selectValues = new Select(driver.findElement(By.id(prefix + "datatypevalues1")));
			selectValues.selectByValue("$OrigRcpt");
			selectValues.selectByValue("$FinalRcpt");
			assertEquals(2, selectValues.getAllSelectedOptions().size());
			
			// Submit the changes
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

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionbiedit:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id(prefix + "datatypevalues1")));
			
			// Delete added record
			// Tick check box of the record
			WebElement checkBoxLink = driver.findElement(By.id(prefix + "checkbox"));
			checkBoxLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionbiedit:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id(prefix + "checkbox"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			
			// Delete the record
			WebElement delete = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			delete.click();

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

			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("select[id='" + prefix + "']"))));
			
			// Go back to list
			WebElement goback = driver.findElement(By.cssSelector("input[title='Go Back']"));
			goback.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("footer:gettingStartedFooter")));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

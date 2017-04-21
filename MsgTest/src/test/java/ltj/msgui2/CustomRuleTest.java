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

public class CustomRuleTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(CustomRuleTest.class);


	@Test
	public void testAddNewAction() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Custom Bounce Rules and Actions";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Locate view/edit detail link
			WebElement viewActionsLink = driver.findElement(By.cssSelector("a[title='XHeader_SpamScore_viewActions']"));
			viewActionsLink.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionedit:footer:gettingStartedFooter")));
			
			// View/Edit Detail Page
			List<WebElement> actSeqList = driver.findElements(By.cssSelector("input[id$=':actionseq']"));
			assertFalse(actSeqList.isEmpty());
			int listSize = actSeqList.size();
			
			List<WebElement> actIdList = driver.findElements(By.cssSelector("select[id$=':actionid']"));
			assertEquals(actSeqList.size(), actIdList.size());

			// Click on Add New button
			WebElement addNewLink = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			addNewLink.click();
			
			String prefix = "actionedit:content:jsftable:" + listSize + ":";
			
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

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionedit:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id(prefix + "datatypevalues1")));
			
			// Delete added record
			// Tick check box of the record
			WebElement checkBoxLink = driver.findElement(By.id(prefix + "checkbox"));
			checkBoxLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionedit:footer:gettingStartedFooter")));
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

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

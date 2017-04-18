package ltj.msgui1;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EmailVariableTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(EmailVariableTest.class);

	@Test
	public void testEmailListUpload() {
		String listTitle = "Upload Email Addresses to List";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			Select selectImpTo = new Select(driver.findElement(By.id("import_to")));
			selectImpTo.selectByValue("ORDERLST");
			
			Select selectImpFrom = new Select(driver.findElement(By.id("import_from")));
			selectImpFrom.selectByValue("SMPLLST2");
			
			WebElement submitImpLink = driver.findElement(By.id("submit_import_from_list"));
			submitImpLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("done_import_from_list")));
			
			// Go back to menu
			WebElement doneImpLink = driver.findElement(By.id("done_import_from_list"));
			doneImpLink.click();
			
			wait.until(ExpectedConditions.titleIs("Main Page"));
			
			// Verify results
			listTitle = "Setup Email Mailing Lists";
			
			WebElement mlistLink = driver.findElement(By.linkText(listTitle));
			mlistLink.click();
			
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			// View Subscriber List page
			WebElement viewListLink = driver.findElement(By.cssSelector("a[title='ORDERLST_viewList']"));
			viewListLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("subrlist:footer:gettingStartedFooter")));
			
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkbox']"));
			assertFalse(checkBoxList.isEmpty());
			// save check box titles
			List<String> checkBoxTitleList = new ArrayList<>();
			for (int i = 0; i < checkBoxList.size(); i++) {
				checkBoxTitleList.add(checkBoxList.get(i).getAttribute("title"));
			}
			
			List<WebElement> addrElmList = driver.findElements(By.cssSelector("span[title$='_emailAddr']"));
			assertEquals(checkBoxList.size(), addrElmList.size());
			
			for (int i = 0; i < checkBoxList.size(); i++) {
				logger.info("Subscriber email address: " + addrElmList.get(i).getText());
			}

			// delete all subscribers but the first
			for (int i = 1; i < checkBoxList.size(); i++) {
				WebElement checkBox = driver.findElement(By.cssSelector("input[title='" + checkBoxTitleList.get(i) + "']"));
				// tick the check box
				if (checkBox.isEnabled() && !checkBox.isSelected()) {
					checkBox.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("subrlist:footer:gettingStartedFooter")));
				}
				else {
					fail("The check box is not enabled or is already selected!");
				}
			}

			// submit the delete
			WebElement deleteLink = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			if (deleteLink.isEnabled()) {
				deleteLink.click();
			}
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("subrlist:footer:gettingStartedFooter")));
			
			// Go back to list page
			WebElement gobackLink = driver.findElement(By.cssSelector("input[title='Go Back']"));
			gobackLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("maillist:footer:gettingStartedFooter")));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}

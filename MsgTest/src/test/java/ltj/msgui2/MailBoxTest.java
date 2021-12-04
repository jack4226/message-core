package ltj.msgui2;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import ltj.selenium.AlertUtil;

public class MailBoxTest extends AbstractLogin {
	static final Logger logger = LogManager.getLogger(MailBoxTest.class);

	@Test
	public void testListAndViewDetail() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Configure POP/IMAP Accounts";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement descriptionElm = driver.findElement(By.cssSelector("span[title='localhost_jwang_description']"));
			String descriptionBefore = descriptionElm.getText();
			String descriptionAfter = null;
			if (StringUtils.endsWith(descriptionBefore, "_updated")) {
				descriptionAfter = StringUtils.removeEnd(descriptionBefore, "_updated");
			}
			else {
				descriptionAfter = descriptionBefore + "_updated";
			}
			
			WebElement useSslElm = driver.findElement(By.cssSelector("span[title='localhost_jwang_useSsl']"));
			String useSslBefore = useSslElm.getText();
			
			WebElement viewDetailLink = driver.findElement(By.cssSelector("a[title='localhost jwang']"));
			viewDetailLink.click();

			// View/Edit Detail Page
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:detail:desc")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:detail:ssl")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:detail:submit")));
			
			WebElement descEditElm = driver.findElement(By.id("mailbox:detail:desc"));
			descEditElm.clear();
			descEditElm.sendKeys(descriptionAfter);
			
			Select selectSsl = new Select(driver.findElement(By.id("mailbox:detail:ssl")));
			WebElement selectedSsl = selectSsl.getFirstSelectedOption();
			logger.info("Use SSL selected before: " + selectedSsl.getText());
			if ("true".equalsIgnoreCase(useSslBefore)) {
				assertEquals("Yes", selectedSsl.getText());
				selectSsl.selectByVisibleText("No");
			}
			else {
				assertEquals("No", selectedSsl.getText());
				//selectSsl.selectByVisibleText("Yes");
			}
 			
			Select selectStatus = new Select(driver.findElement(By.id("mailbox:detail:statusid")));
			WebElement selectedStatus = selectStatus.getFirstSelectedOption();
			String selectedStatusIdBefore = selectedStatus.getAttribute("value");
			if ("A".equals(selectedStatusIdBefore)) {
				selectStatus.selectByValue("I");
			}
			else {
				selectStatus.selectByValue("A");
			}
			
			WebElement submit = driver.findElement(By.id("mailbox:detail:submit"));
			submit.click();
			
			AlertUtil.handleAlert(driver);
			
			// verify the results
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("span[title='localhost_jwang_description']")));
			
			WebElement descAfterElm = driver.findElement(By.cssSelector("span[title='localhost_jwang_description']"));
			assertEquals(descriptionAfter, descAfterElm.getText());
			
			WebElement useSslAfter = driver.findElement(By.cssSelector("span[title='localhost_jwang_useSsl']"));
//			if ("true".equalsIgnoreCase(useSslBefore)) {
				assertEquals("false", useSslAfter.getText());
//			}
//			else {
//				assertEquals("true", useSslAfter.getText());
//			}
			
			WebElement selectedStatusAfter = driver.findElement(By.cssSelector("span[title='localhost_jwang_statusId']"));
			logger.info("StatusId after: " + selectedStatusAfter.getText());
			if ("A".equals(selectedStatusIdBefore)) {
				assertEquals("Inactive", selectedStatusAfter.getText());
			}
			else {
				assertEquals("Active", selectedStatusAfter.getText());
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	@Test
	public void testCopyFromSelected() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Configure POP/IMAP Accounts";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// select a record to copy
			WebElement checkBoxElm = driver.findElement(By.cssSelector("input[title='localhost_jwang_checkBox']"));
			checkBoxElm.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			
			// copy to a new record
			WebElement copyToNewElm = driver.findElement(By.cssSelector("input[title='Create a new row from selected']"));
			copyToNewElm.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:detail:hostname")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:detail:submit")));
			
			// Edit Detail Page
			Random r = new Random();
			String randomStr1 = StringUtils.leftPad(r.nextInt(1000) + "", 3, '0');
			
			WebElement hostNameElm = driver.findElement(By.id("mailbox:detail:hostname"));
			hostNameElm.sendKeys("testhost_" + randomStr1 + ".local");
			
			WebElement userIdElm = driver.findElement(By.id("mailbox:detail:userid"));
			userIdElm.sendKeys("testuser_" + randomStr1);
			
			WebElement submitElm = driver.findElement(By.id("mailbox:detail:submit"));
			submitElm.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:footer:gettingStartedFooter")));
			
			// Now delete the added record
			// locate the record by title
			String title = "testhost_" + randomStr1 + ".local_" + "testuser_" + randomStr1 + "_checkBox";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='" + title + "']")));
			
			checkBoxElm = driver.findElement(By.cssSelector("input[title='" + title + "']"));
			checkBoxElm.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			
			// delete the record
			WebElement deleteRowElm = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			deleteRowElm.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("input[title='" + title + "']"))));
			
			// verify the record is deleted
			List<WebElement> checkBoxs = driver.findElements(By.cssSelector("input[title='" + title + "']"));
			assertTrue(checkBoxs.isEmpty());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testAddNewRecord() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Configure POP/IMAP Accounts";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Add a new row']")));
			
			// add a new record
			WebElement copyToNewElm = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			copyToNewElm.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:detail:hostname")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:detail:submit")));
			
			// Edit Detail Page
			Random r = new Random();
			String randomStr1 = StringUtils.leftPad(r.nextInt(1000) + "", 3, '0');
			
			WebElement hostNameElm = driver.findElement(By.id("mailbox:detail:hostname"));
			hostNameElm.sendKeys("testhost_" + randomStr1 + ".local");
			
			WebElement userIdElm = driver.findElement(By.id("mailbox:detail:userid"));
			userIdElm.sendKeys("testuser_" + randomStr1);
			
			WebElement passwordElm = driver.findElement(By.id("mailbox:detail:password"));
			passwordElm.sendKeys("testpswd_" + randomStr1);
			
			WebElement descElm = driver.findElement(By.id("mailbox:detail:desc"));
			descElm.sendKeys("test description " + randomStr1);

			WebElement retrymaxElm = driver.findElement(By.id("mailbox:detail:retrymax"));
			retrymaxElm.sendKeys("10");
			WebElement minWaitElm = driver.findElement(By.id("mailbox:detail:minimumwait"));
			minWaitElm.sendKeys("10");

			WebElement submitElm = driver.findElement(By.id("mailbox:detail:submit"));
			submitElm.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:footer:gettingStartedFooter")));
			
			// Now delete the added record
			// locate the record by title
			String title = "testhost_" + randomStr1 + ".local_" + "testuser_" + randomStr1 + "_checkBox";
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='" + title + "']")));
			
			WebElement checkBoxElm = driver.findElement(By.cssSelector("input[title='" + title + "']"));
			checkBoxElm.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			
			// delete the record
			WebElement deleteRowElm = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			deleteRowElm.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailbox:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("input[title='" + title + "']"))));
			
			// verify the record is deleted
			List<WebElement> checkBoxs = driver.findElements(By.cssSelector("input[title='" + title + "']"));
			assertTrue(checkBoxs.isEmpty());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

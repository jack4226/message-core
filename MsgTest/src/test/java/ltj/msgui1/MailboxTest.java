package ltj.msgui1;

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

public class MailboxTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(MailboxTest.class);

	@Test
	public void testMailboxes() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText("Configure POP/IMAP Accounts"));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Configure POP/IMAP Accounts"));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement descElm = driver.findElement(By.cssSelector("span[title=localhost_jwang_desc]"));
			assertNotNull(descElm);
			logger.info("Description before: " + descElm.toString() + ", text: " + descElm.getText());
			String descBefore = descElm.getText();
			String descAfter = null;
			if (StringUtils.endsWith(descBefore, "_updated")) {
				descAfter = StringUtils.removeEnd(descBefore, "_updated");
			}
			else {
				descAfter = descBefore + "_updated";
			}
			
			WebElement useSslElm = driver.findElement(By.cssSelector("span[title=localhost_jwang_useSsl]"));
			assertNotNull(useSslElm);
			logger.info("useSsl before: " + useSslElm.toString() + ", text: " + useSslElm.getText());
			
			String useSslBefore = useSslElm.getText();
			
			link = driver.findElement(By.cssSelector("a[title='localhost jwang']"));
			assertNotNull(link);
			
			link.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mboxedit:footer:gettingStartedFooter")));
			
			WebElement desc = driver.findElement(By.id("mboxedit:content:desc"));
			assertNotNull(desc);
			desc.clear();
			desc.sendKeys(descAfter);
			
			Select selectSsl = new Select(driver.findElement(By.id("mboxedit:content:ssl")));
			assertNotNull(selectSsl);
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
 			
			Select selectStatus = new Select(driver.findElement(By.id("mboxedit:content:statusid")));
			assertNotNull(selectStatus);
			WebElement selectedStatus = selectStatus.getFirstSelectedOption();
			logger.info("StatusId before: " + selectedStatus.getAttribute("value") + ", text: " + selectedStatus.getText());
			String selectedStatusIdBefore = selectedStatus.getAttribute("value");
			if ("A".equals(selectedStatusIdBefore)) {
				selectStatus.selectByValue("I");
			}
			else {
				selectStatus.selectByValue("A");
			}
			
			WebElement submit = driver.findElement(By.id("mboxedit:content:submit"));
			assertNotNull(submit);
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
			
			// verify the results
			wait.until(ExpectedConditions.titleIs("Configure POP/IMAP Accounts"));
			
			WebElement descAfterElm = driver.findElement(By.cssSelector("span[title=localhost_jwang_desc]"));
			assertNotNull(descAfterElm);
			assertEquals(descAfter, descAfterElm.getText());
			
			WebElement useSslAfter = driver.findElement(By.cssSelector("span[title=localhost_jwang_useSsl]"));
			assertNotNull(useSslAfter);
			assertEquals("false", useSslAfter.getText());
//			if ("true".equalsIgnoreCase(useSslBefore)) {
//				assertEquals("false", useSslAfter.getText());
//			}
//			else {
//				assertEquals("true", useSslAfter.getText());
//			}
			
			WebElement selectedStatusAfter = driver.findElement(By.cssSelector("span[title=localhost_jwang_statusId]"));
			assertNotNull(selectedStatusAfter);
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

}

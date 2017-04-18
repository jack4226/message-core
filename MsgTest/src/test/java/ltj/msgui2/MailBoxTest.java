package ltj.msgui2;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MailBoxTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(MailBoxTest.class);

	@Test
	public void testMailBoxes() {
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mboxedit:footer:gettingStartedFooter")));
			
			WebElement descEditElm = driver.findElement(By.id("mboxedit:content:desc"));
			descEditElm.clear();
			descEditElm.sendKeys(descriptionAfter);
			
			Select selectSsl = new Select(driver.findElement(By.id("mboxedit:content:ssl")));
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
			WebElement selectedStatus = selectStatus.getFirstSelectedOption();
			String selectedStatusIdBefore = selectedStatus.getAttribute("value");
			if ("A".equals(selectedStatusIdBefore)) {
				selectStatus.selectByValue("I");
			}
			else {
				selectStatus.selectByValue("A");
			}
			
			WebElement submit = driver.findElement(By.id("mboxedit:content:submit"));
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
			wait.until(ExpectedConditions.titleIs(listTitle));
			
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
}

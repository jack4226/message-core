package ltj.selenium;

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

public class EmailTemplateTest extends BaseLogin {
	static final Logger logger = Logger.getLogger(EmailTemplateTest.class);

	@Test
	public void testEmailTemplates() {
		String listTitle = "Setup Email Templates";
		String testTmpltName = "SampleNewsletter2";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			// Email Template List page
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement subject = driver.findElement(By.cssSelector("span[title='SampleNewsletter2_subject']"));
			assertNotNull(subject);
			String subjectBefore = subject.getText();
			assertTrue(subjectBefore.startsWith("Sample HTML newsletter to"));
			
			WebElement listId = driver.findElement(By.cssSelector("span[title='SampleNewsletter2_listId']"));
			assertNotNull(listId);
			String listIdBefore = listId.getText();
			assertEquals("SMPLLST2", listIdBefore);
			
			WebElement listType = driver.findElement(By.cssSelector("span[title='SampleNewsletter2_listType']"));
			assertNotNull(listType);
			String listTypeBefore= listType.getText();
			assertEquals("Traditional", listTypeBefore);
			
			WebElement dlvrOpt = driver.findElement(By.cssSelector("span[title='SampleNewsletter2_deliveryOption']"));
			String dlvrOptBefore = dlvrOpt.getText();
			assertEquals("All on list", dlvrOptBefore);
			
			WebElement listEditLink = driver.findElement(By.cssSelector("a[title='SampleNewsletter2']"));
			assertNotNull(listEditLink);
			assertEquals(testTmpltName, listEditLink.getText());
			
			listEditLink.click();
			
			// Email Template Edit Page
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			WebElement tmpltNameElm = driver.findElement(By.id("emailtmplt:content:templateid"));
			assertNotNull(tmpltNameElm);
			assertEquals(testTmpltName, tmpltNameElm.getAttribute("value"));
			
			Select selectListType = new Select(driver.findElement(By.id("emailtmplt:content:listtype")));
			WebElement selectedListType = selectListType.getFirstSelectedOption();
			if ("Traditional".equals(listTypeBefore)) {
				assertEquals("Traditional", selectedListType.getText());
			}
			else {
				assertEquals("Personalized", selectedListType.getText());
			}
			
			Select selectDlvrOpt = new Select(driver.findElement(By.id("emailtmplt:content:dlvropt")));
			WebElement selectedDlvrOpt = selectDlvrOpt.getFirstSelectedOption();
			logger.info("Delivery Option selected: " + selectedDlvrOpt.getText());
			if ("All on list".equals(dlvrOptBefore)) {
				assertEquals("ALL", selectedDlvrOpt.getAttribute("value"));
			}
			else if ("Customers only".equals(dlvrOptBefore)) {
				assertEquals("CUST", selectedDlvrOpt.getAttribute("value"));
			}
			else {
				assertEquals("PROS", selectedDlvrOpt.getAttribute("value"));
			}
			
			Select selectEmbed = new Select(driver.findElement(By.id("emailtmplt:content:emailid")));
			List<WebElement> selectedElms = selectEmbed.getAllSelectedOptions();
			assertEquals(1, selectedElms.size());
			
			WebElement subjectElm = driver.findElement(By.id("emailtmplt:content:subject"));
			assertNotNull(subjectElm);
			String subjectAfter = subjectElm.getAttribute("value");
			assertEquals(subjectBefore, subjectAfter);
			if (StringUtils.endsWith(subjectAfter, "_updated")) {
				subjectAfter = StringUtils.removeEnd(subjectAfter, "_updated");
			}
			else {
				subjectAfter += "_updated";
			}
			assertFalse(subjectAfter.equals(subjectBefore));
			subjectElm.clear();
			subjectElm.sendKeys(subjectAfter);
 			
			WebElement bodyElm = driver.findElement(By.id("emailtmplt:content:bodytext"));
			// TextArea, use getAttribute("value") to retrieve text content
			assertTrue(bodyElm.getAttribute("value").length() > 0);
			
			// submit the changes and go back to list page
			WebElement submit = driver.findElement(By.id("emailtmplt:content:submit"));
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmpt:footer:gettingStartedFooter")));
			
			// verify results
			WebElement subjectAfterElm = driver.findElement(By.cssSelector("span[title='SampleNewsletter2_subject']"));
			assertEquals(subjectAfter, subjectAfterElm.getText());
			
			
			// View/Edit Scheduler page
			WebElement schedulerLink = driver.findElement(By.cssSelector("a[title='SampleNewsletter2_editSchedule']"));
			schedulerLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailsched:footer:gettingStartedFooter")));
			
			//  Scheduler edit page
			tmpltNameElm = driver.findElement(By.id("emailsched:content:tmpltid"));
			assertEquals(testTmpltName, tmpltNameElm.getAttribute("value"));
			assertFalse(tmpltNameElm.isEnabled());
			
			Select selectHour = new Select(driver.findElement(By.id("emailsched:content:starttime")));
			WebElement selectedHour = selectHour.getFirstSelectedOption();
			assertEquals("2", selectedHour.getAttribute("value"));
			
			Select selectMinute = new Select(driver.findElement(By.id("emailsched:content:startminute")));
			WebElement selectedMinute = selectMinute.getFirstSelectedOption();
			String minuteStr = selectedMinute.getAttribute("value");
			int minuteAfter = (Integer.parseInt(minuteStr) + 5) % 60;
			selectMinute.selectByValue("" + minuteAfter);
			
			WebElement sundayElm = driver.findElement(By.id("emailsched:content:weekly:0")); // Sunday
			boolean sundayBefore = sundayElm.isSelected();
			sundayElm.click();
			assertFalse(sundayElm.isSelected() == sundayBefore);
			
			// Save change and go back to the list page
			WebElement saveSchedule = driver.findElement(By.id("emailsched:content:submit"));
			saveSchedule.click();
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailtmpt:footer:gettingStartedFooter")));
			
			// verify results
			schedulerLink = driver.findElement(By.cssSelector("a[title='SampleNewsletter2_editSchedule']"));
			schedulerLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailsched:footer:gettingStartedFooter")));
			
			selectMinute = new Select(driver.findElement(By.id("emailsched:content:startminute")));
			assertEquals("" + minuteAfter, selectMinute.getFirstSelectedOption().getAttribute("value"));
			
			sundayElm = driver.findElement(By.id("emailsched:content:weekly:0"));
			assertFalse(sundayBefore == sundayElm.isSelected());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}

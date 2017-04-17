package ltj.selenium;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import ltj.selenium.MsgInbox1Test.MsgListDetail;

public class MsgInbox2Test extends BaseLogin {
	static final Logger logger = Logger.getLogger(MsgInbox2Test.class);

	@Test
	public void testEmailBrowser() {
		String listTitle = "Manage Email Correspondence";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Display ALL messages 
			WebElement allMsgLink = driver.findElement(By.cssSelector("a[title='All Messages']"));
			allMsgLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// Email List page
			Select selectRuleName = new Select(driver.findElement(By.cssSelector("select[title='Select Rule Name']")));
			WebElement selectedRuleName = selectRuleName.getFirstSelectedOption();
			assertEquals("All", selectedRuleName.getAttribute("value"));
			
			WebElement checkAll = driver.findElement(By.id("msgform:msgrow:checkAll"));
			checkAll.click();

			WebElement markUnreadLink = driver.findElement(By.cssSelector("a[title='Mark as unread']"));
			markUnreadLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// get the list
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			
			MsgListDetail tuple1 = MsgInbox1Test.getListDetails();
			
			// select rows to mark as Read
			List<Integer> elements = StringUtil.getRandomElements(checkBoxList.size());
			for (Integer idx : elements) {
				WebElement checkbox = checkBoxList.get(idx);
				checkbox.click();
			}
			
			// Mark messages as Read
			WebElement markAsRead = driver.findElement(By.cssSelector("a[title='Mark as read']"));
			markAsRead.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// Only display messages marks as Read
			WebElement viewRead = driver.findElement(By.cssSelector("a[title='View Read']"));
			viewRead.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// verify results
			List<WebElement> readCheckBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertTrue(readCheckBoxList.size() >= elements.size());
			
			// Reset messages to as Unread
			for (WebElement elm : readCheckBoxList) {
				elm.click();
			}
			
			WebElement markAsUnread = driver.findElement(By.cssSelector("a[title='Mark as unread']"));
			markAsUnread.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// Reset to view all messages
			WebElement viewAll = driver.findElement(By.cssSelector("a[title='View All']"));
			viewAll.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// Verify the list hasn't changed
			MsgListDetail tuple2 = MsgInbox1Test.getListDetails();
			assertTrue(tuple1.equalsTo(tuple2));
			
			// Flagged
			checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			
			// select rows to mark as Flagged
			elements = StringUtil.getRandomElements(checkBoxList.size());
			for (Integer idx : elements) {
				WebElement checkbox = checkBoxList.get(idx);
				checkbox.click();
			}
			
			// Mark selected rows ad Flagged
			WebElement markAsFlagged = driver.findElement(By.cssSelector("a[title='Mark as flagged']"));
			markAsFlagged.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// Only display rows as Flagged
			WebElement viewFlagged = driver.findElement(By.cssSelector("a[title='View Flagged']"));
			viewFlagged.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// verify results
			List<WebElement> flaggedCheckBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertTrue(flaggedCheckBoxList.size() >= elements.size());
			
			// Reset messages to as Unflagged
			for (WebElement elm : flaggedCheckBoxList) {
				elm.click();
			}
			
			WebElement markAsUnflagged = driver.findElement(By.cssSelector("a[title='Mark as unflagged']"));
			markAsUnflagged.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// Reset to view all messages
			viewAll = driver.findElement(By.cssSelector("a[title='View All']"));
			viewAll.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));

			// Verify the list hasn't changed
			MsgListDetail tuple3 = MsgInbox1Test.getListDetails();
			assertTrue(tuple1.equalsTo(tuple3));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}

package ltj.msgui1;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BroadcastMsgTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(BroadcastMsgTest.class);

	@Test
	public void testBroadcastMsgBrowser() {
		String listTitle = "View Broadcast Messages";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Customer List page
			BrstMsgListDetail dtl1 = getListDetails();
			
			// View Customer Details
			assertNotNull(dtl1.viewMsgLink);
			dtl1.viewMsgLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("viewbcst:footer:gettingStartedFooter")));
			
			// verify that fields from details and list match
			WebElement toAddrElm = driver.findElement(By.cssSelector("span[title='To Address']"));
			String toAddr = toAddrElm.getText();
			assertTrue(StringUtils.isNotBlank(toAddr));
			
			WebElement msgIdElm = driver.findElement(By.id("viewbcst:content:msgId"));
			assertEquals(dtl1.brstMsgIdList.get(dtl1.idx), msgIdElm.getAttribute("value"));
			
			// go back to list
			WebElement viewSubmit = driver.findElement(By.cssSelector("input[title='Go back to List']"));
			viewSubmit.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brdslist:footer:gettingStartedFooter")));
			
			// Get total number of rows
			WebElement totalRowsElm = driver.findElement(By.cssSelector("span[title='Total Row Count']"));
			String totalRowsStr = totalRowsElm.getText();
			int totalRows = Integer.parseInt(totalRowsStr);
			if (totalRows > 20) { // test paging
				try {
					// page next
					WebElement pageNextElm = driver.findElement(By.cssSelector("a[title='Page Next']"));
					pageNextElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brdslist:footer:gettingStartedFooter")));
					BrstMsgListDetail dtl2 = getListDetails();
					assertFalse(dtl1.brstMsgIdList.equals(dtl2.brstMsgIdList));
					// page previous
					WebElement pagePrevElm = driver.findElement(By.cssSelector("a[title='Page Previous']"));
					pagePrevElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brdslist:footer:gettingStartedFooter")));
					BrstMsgListDetail dtl3 = getListDetails();
					assertTrue(dtl1.brstMsgIdList.equals(dtl3.brstMsgIdList) && dtl1.listIdList.equals(dtl3.listIdList));
				}
				catch (Exception e) {
					logger.info(e.getMessage());
					fail();
				}
				
				try {
					// page last
					WebElement pageLastElm = driver.findElement(By.cssSelector("a[title='Page Last']"));
					pageLastElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brdslist:footer:gettingStartedFooter")));
					BrstMsgListDetail dtl4 = getListDetails();
					assertFalse(dtl1.brstMsgIdList.equals(dtl4.brstMsgIdList));
					// page first
					WebElement pageFirstElm = driver.findElement(By.cssSelector("a[title='Page First']"));
					pageFirstElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("brdslist:footer:gettingStartedFooter")));
					BrstMsgListDetail dtl5 = getListDetails();
					assertTrue(dtl1.brstMsgIdList.equals(dtl5.brstMsgIdList) && dtl1.listIdList.equals(dtl5.listIdList));
				}
				catch (Exception e) {
					logger.info(e.getMessage());
					fail();
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	static class BrstMsgListDetail {
		List<String> brstMsgIdList;
		List<String> listIdList;
		List<String> unsubCountList;
		WebElement viewMsgLink;
		int idx;
		
		boolean equalsTo(BrstMsgListDetail other) {
			if (brstMsgIdList != null) {
				if (other.brstMsgIdList == null) {
					return false;
				}
			}
			if (listIdList != null) {
				if (other.listIdList == null) {
					return false;
				}
			}
			if (unsubCountList != null) {
				if (other.unsubCountList == null) {
					return false;
				}
			}
			return (brstMsgIdList.equals(other.brstMsgIdList) && listIdList.equals(other.listIdList) && unsubCountList.equals(other.unsubCountList));
		}
	}
	
	BrstMsgListDetail getListDetails() {
		List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
		assertFalse(checkBoxList.isEmpty());
		
		BrstMsgListDetail dtl = new BrstMsgListDetail();
		
		dtl.brstMsgIdList = new ArrayList<>();
		dtl.listIdList = new ArrayList<>();
		dtl.unsubCountList = new ArrayList<>();
		
		dtl.idx = new Random().nextInt(checkBoxList.size());
		
		List<WebElement> listIdList = driver.findElements(By.cssSelector("span[title$='_listId']"));
		assertEquals(checkBoxList.size(), listIdList.size());

		List<WebElement> unsubCountList = driver.findElements(By.cssSelector("span[title$='_unsubCount']"));
		assertEquals(checkBoxList.size(), unsubCountList.size());
		
		for (int i = 0; i < checkBoxList.size(); i++) {
			dtl.listIdList.add(listIdList.get(i).getText());
			dtl.unsubCountList.add(unsubCountList.get(i).getText());
			
			String checkBoxTitle = checkBoxList.get(i).getAttribute("title");
			String prefix = StringUtils.removeEnd(checkBoxTitle, "_checkBox");
			
			WebElement viewMsgLink = driver.findElement(By.cssSelector("a[title='" + prefix + "']"));
			String brstMsgId = viewMsgLink.getAttribute("title");
			dtl.brstMsgIdList.add(brstMsgId);
			
			if (i == dtl.idx) {
				dtl.viewMsgLink = viewMsgLink;
			}
		}
		
		assertNotNull(dtl.viewMsgLink);
		
		return dtl;
	}
}

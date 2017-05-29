package ltj.msgui1;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import ltj.selenium.StringUtil;

public class EmailAddressTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(EmailAddressTest.class);

	@Test
	public void testEmailAddrBrowser() {
		String listTitle = "Manage Email Addresses";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Address List page
			EmailAddrListDetail dtl1 = getListDetails();
			
			// View Email Details
			assertNotNull(dtl1.viewMsgLink);
			dtl1.viewMsgLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addredit:footer:gettingStartedFooter")));
			
			// verify that fields from details and list match
			WebElement emailAddrElm = driver.findElement(By.id("addredit:content:emailaddr"));
			String emailAddr = emailAddrElm.getAttribute("value");
			assertEquals(dtl1.emailAddrList.get(dtl1.idx), emailAddr);
			
			Select selectHtml = new Select(driver.findElement(By.id("addredit:content:html")));
			WebElement selectedHtml = selectHtml.getFirstSelectedOption();
			assertEquals(dtl1.accetpHtmlList.get(dtl1.idx), selectedHtml.getText());
			
			WebElement bounceCountElm = driver.findElement(By.id("addredit:content:bounce"));
			String bounceCountStr = bounceCountElm.getAttribute("value");
			assertEquals(dtl1.bounceCountList.get(dtl1.idx), bounceCountStr);
			
			int bounceCount = Integer.parseInt(bounceCountStr);
			bounceCountElm.clear();
			bounceCountElm.sendKeys((++bounceCount) + "");
			
			WebElement viewSubmit = driver.findElement(By.id("addredit:content:submit"));
			viewSubmit.click();
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addrlist:footer:gettingStartedFooter")));
			
			// verify edit results
			WebElement bounceCountAfterElm = driver.findElement(By.cssSelector("span[title='" + emailAddr + "_bounceCount']"));
			assertEquals(bounceCount + "", bounceCountAfterElm.getText());
			
			// test search by address
			WebElement addrToSrch = driver.findElement(By.cssSelector("input[title='Address to Search']"));
			addrToSrch.clear();
			String emailDomain = StringUtil.getEmailDomainName(emailAddr);
			addrToSrch.sendKeys(emailDomain);
			
			WebElement submitSearch = driver.findElement(By.cssSelector("input[title='Submit Search']"));
			submitSearch.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addrlist:footer:gettingStartedFooter")));
			
			// verify search results
			EmailAddrListDetail srchdtl = getListDetails();
			for (String addr : srchdtl.emailAddrList) {
				assertTrue(StringUtils.contains(addr, emailDomain));
			}
			
			// reset search
			WebElement resetSearch = driver.findElement(By.cssSelector("input[title='Reset Search']"));
			resetSearch.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addrlist:footer:gettingStartedFooter")));
			
			// Get total number of rows
			WebElement totalRowsElm = driver.findElement(By.cssSelector("span[title='Total Address Count']"));
			String totalRowsStr = totalRowsElm.getText();
			int totalRows = Integer.parseInt(totalRowsStr);
			if (totalRows > 20) { // test paging
				try {
					// page next
					WebElement pageNextElm = driver.findElement(By.cssSelector("a[title='Page Next']"));
					pageNextElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addrlist:footer:gettingStartedFooter")));
					EmailAddrListDetail dtl2 = getListDetails();
					assertFalse(dtl1.emailAddrList.equals(dtl2.emailAddrList));
					// page previous
					WebElement pagePrevElm = driver.findElement(By.cssSelector("a[title='Page Previous']"));
					pagePrevElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addrlist:footer:gettingStartedFooter")));
					EmailAddrListDetail dtl3 = getListDetails();
					assertTrue(dtl1.emailAddrList.equals(dtl3.emailAddrList) && dtl1.accetpHtmlList.equals(dtl3.accetpHtmlList));
				}
				catch (Exception e) {
					logger.info(e.getMessage());
					fail();
				}
				
				try {
					// page last
					WebElement pageLastElm = driver.findElement(By.cssSelector("a[title='Page Last']"));
					pageLastElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addrlist:footer:gettingStartedFooter")));
					EmailAddrListDetail dtl4 = getListDetails();
					assertFalse(dtl1.emailAddrList.equals(dtl4.emailAddrList));
					// page first
					WebElement pageFirstElm = driver.findElement(By.cssSelector("a[title='Page First']"));
					pageFirstElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addrlist:footer:gettingStartedFooter")));
					EmailAddrListDetail dtl5 = getListDetails();
					assertTrue(dtl1.emailAddrList.equals(dtl5.emailAddrList) && dtl1.accetpHtmlList.equals(dtl5.accetpHtmlList));
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

	static class EmailAddrListDetail {
		List<String> emailAddrList;
		List<String> accetpHtmlList;
		List<String> bounceCountList;
		WebElement viewMsgLink;
		int idx;
		
		boolean equalsTo(EmailAddrListDetail other) {
			if (emailAddrList != null) {
				if (other.emailAddrList == null) {
					return false;
				}
			}
			if (accetpHtmlList != null) {
				if (other.accetpHtmlList == null) {
					return false;
				}
			}
			if (bounceCountList != null) {
				if (other.bounceCountList == null) {
					return false;
				}
			}
			return (emailAddrList.equals(other.emailAddrList) && accetpHtmlList.equals(other.accetpHtmlList) && bounceCountList.equals(other.bounceCountList));
		}
	}
	
	EmailAddrListDetail getListDetails() {
		List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
		assertFalse(checkBoxList.isEmpty());
		
		EmailAddrListDetail dtl = new EmailAddrListDetail();
		
		dtl.emailAddrList = new ArrayList<>();
		dtl.accetpHtmlList = new ArrayList<>();
		dtl.bounceCountList = new ArrayList<>();
		
		dtl.idx = new Random().nextInt(checkBoxList.size());
		
		List<WebElement> acceptHtmlList = driver.findElements(By.cssSelector("span[title$='_acceptHtml']"));
		assertEquals(checkBoxList.size(), acceptHtmlList.size());
		
		List<WebElement> bounceCountList = driver.findElements(By.cssSelector("span[title$='_bounceCount']"));
		assertEquals(checkBoxList.size(), bounceCountList.size());
		
		for (int i = 0; i < checkBoxList.size(); i++) {
			dtl.accetpHtmlList.add(acceptHtmlList.get(i).getText());
			dtl.bounceCountList.add(bounceCountList.get(i).getText());
			
			String checkBoxTitle = checkBoxList.get(i).getAttribute("title");
			String prefix = StringUtils.removeEnd(checkBoxTitle, "_checkBox");

			WebElement viewMsgLink = driver.findElement(By.cssSelector("a[title='" + prefix + "']"));
			String emailAddr = viewMsgLink.getText();
			dtl.emailAddrList.add(emailAddr);
			
			if (i == dtl.idx) {
				dtl.viewMsgLink = viewMsgLink;
			}
		}
		
		assertNotNull(dtl.viewMsgLink);
		
		return dtl;
	}
}

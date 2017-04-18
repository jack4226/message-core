package ltj.msgui1;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import ltj.selenium.StringUtil;

public class CustomerTest extends BaseLogin {
	static final Logger logger = Logger.getLogger(CustomerTest.class);

	@Test
	public void testCustomerBrowser() {
		String listTitle = "Manage Customer Information";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Customer List page
			CustomerListDetail dtl1 = getListDetails();
			
			// View Customer Details
			assertNotNull(dtl1.viewMsgLink);
			dtl1.viewMsgLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custedit:footer:gettingStartedFooter")));
			
			// verify that fields from details and list match
			WebElement emailAddrElm = driver.findElement(By.id("custedit:content:emailaddr"));
			String emailAddr = emailAddrElm.getAttribute("value");
			assertEquals(dtl1.emailAddrList.get(dtl1.idx), emailAddr);
			
			WebElement lastNameElm = driver.findElement(By.id("custedit:content:lastnm"));
			assertEquals(dtl1.lastNameList.get(dtl1.idx), lastNameElm.getAttribute("value"));
			
			WebElement birthDateElm = driver.findElement(By.id("custedit:content:birthdt"));
			String birthDateStr = birthDateElm.getAttribute("value");
			assertEquals(dtl1.birthDateList.get(dtl1.idx), birthDateStr);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date birthDate =sdf.parse(birthDateStr);
			// update birth date
			Calendar cal = Calendar.getInstance();
			cal.setTime(birthDate);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			birthDateElm.clear();
			String birthDateAfter = sdf.format(cal.getTime());
			birthDateElm.sendKeys(birthDateAfter);
			
			// submit the change
			WebElement viewSubmit = driver.findElement(By.id("custedit:content:submit"));
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custlist:footer:gettingStartedFooter")));
			
			// verify edit results
			WebElement birthDateAfterElm = driver.findElement(By.cssSelector("span[title='" + dtl1.custIdList.get(dtl1.idx) + "_birthDate']"));
			assertEquals(birthDateAfter, birthDateAfterElm.getText());
			
			// test search by address
			WebElement addrToSrch = driver.findElement(By.cssSelector("input[title='Email Address to Search']"));
			addrToSrch.clear();
			String emailDomain = StringUtil.getEmailDomainName(emailAddr);
			addrToSrch.sendKeys(emailDomain);
			
			WebElement submitSearch = driver.findElement(By.cssSelector("input[title='Search By Email Address']"));
			submitSearch.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custlist:footer:gettingStartedFooter")));
			
			// verify search results
			CustomerListDetail srchdtl = getListDetails();
			for (String addr : srchdtl.emailAddrList) {
				assertTrue(StringUtils.contains(addr, emailDomain));
			}
			
			// reset search
			WebElement resetSearch = driver.findElement(By.cssSelector("input[title='Reset Search']"));
			resetSearch.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custlist:footer:gettingStartedFooter")));
			
			// Get total number of rows
			WebElement totalRowsElm = driver.findElement(By.cssSelector("span[title='Total Number of Customers']"));
			String totalRowsStr = totalRowsElm.getText();
			int totalRows = Integer.parseInt(totalRowsStr);
			if (totalRows > 20) { // test paging
				try {
					// page next
					WebElement pageNextElm = driver.findElement(By.cssSelector("a[title='Page Next']"));
					pageNextElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custlist:footer:gettingStartedFooter")));
					CustomerListDetail dtl2 = getListDetails();
					assertFalse(dtl1.custIdList.equals(dtl2.custIdList));
					// page previous
					WebElement pagePrevElm = driver.findElement(By.cssSelector("a[title='Page Previous']"));
					pagePrevElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custlist:footer:gettingStartedFooter")));
					CustomerListDetail dtl3 = getListDetails();
					assertTrue(dtl1.custIdList.equals(dtl3.custIdList) && dtl1.emailAddrList.equals(dtl3.emailAddrList));
				}
				catch (Exception e) {
					logger.info(e.getMessage());
					fail();
				}
				
				try {
					// page last
					WebElement pageLastElm = driver.findElement(By.cssSelector("a[title='Page Last']"));
					pageLastElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custlist:footer:gettingStartedFooter")));
					CustomerListDetail dtl4 = getListDetails();
					assertFalse(dtl1.custIdList.equals(dtl4.custIdList));
					// page first
					WebElement pageFirstElm = driver.findElement(By.cssSelector("a[title='Page First']"));
					pageFirstElm.click();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custlist:footer:gettingStartedFooter")));
					CustomerListDetail dtl5 = getListDetails();
					assertTrue(dtl1.custIdList.equals(dtl5.custIdList) && dtl1.emailAddrList.equals(dtl5.emailAddrList));
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

	static class CustomerListDetail {
		List<String> custIdList;
		List<String> emailAddrList;
		List<String> lastNameList;
		List<String> birthDateList;
		WebElement viewMsgLink;
		int idx;
		
		boolean equalsTo(CustomerListDetail other) {
			if (custIdList != null) {
				if (other.custIdList == null) {
					return false;
				}
			}
			if (emailAddrList != null) {
				if (other.emailAddrList == null) {
					return false;
				}
			}
			if (lastNameList != null) {
				if (other.lastNameList == null) {
					return false;
				}
			}
			if (birthDateList != null) {
				if (other.birthDateList == null) {
					return false;
				}
			}
			return (custIdList.equals(other.custIdList) && emailAddrList.equals(other.emailAddrList) && lastNameList.equals(other.lastNameList));
		}
	}
	
	CustomerListDetail getListDetails() {
		List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
		assertFalse(checkBoxList.isEmpty());
		
		CustomerListDetail dtl = new CustomerListDetail();
		
		dtl.custIdList = new ArrayList<>();
		dtl.emailAddrList = new ArrayList<>();
		dtl.lastNameList = new ArrayList<>();
		dtl.birthDateList = new ArrayList<>();
		
		dtl.idx = new Random().nextInt(checkBoxList.size());
		
		List<WebElement> emailAddrList = driver.findElements(By.cssSelector("span[title$='_emailAddr']"));
		assertEquals(checkBoxList.size(), emailAddrList.size());
		
		List<WebElement> lastNameList = driver.findElements(By.cssSelector("span[title$='_lastName']"));
		assertEquals(checkBoxList.size(), lastNameList.size());
		
		List<WebElement> birthDateList = driver.findElements(By.cssSelector("span[title$='_birthDate']"));
		assertEquals(checkBoxList.size(), birthDateList.size());

		for (int i = 0; i < checkBoxList.size(); i++) {
			dtl.emailAddrList.add(emailAddrList.get(i).getText());
			dtl.lastNameList.add(lastNameList.get(i).getText());
			dtl.birthDateList.add(birthDateList.get(i).getText());
			
			String checkBoxTitle = checkBoxList.get(i).getAttribute("title");
			String prefix = StringUtils.removeEnd(checkBoxTitle, "_checkBox");
			
			WebElement viewMsgLink = driver.findElement(By.cssSelector("a[title='" + prefix + "']"));
			String custId = viewMsgLink.getText();
			dtl.custIdList.add(custId);
			
			if (i == dtl.idx) {
				dtl.viewMsgLink = viewMsgLink;
			}
		}
		
		assertNotNull(dtl.viewMsgLink);
		
		return dtl;
	}
}

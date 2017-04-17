package ltj.selenium;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import junit.framework.TestCase;

@RunWith(BlockJUnit4ClassRunner.class)
public class LoginFlowTest extends TestCase {
	static final Logger logger = Logger.getLogger(LoginFlowTest.class);

	private static ChromeDriverService service;
	private WebDriver driver;

	@BeforeClass
	public static void createAndStartService() throws IOException {
		service = new ChromeDriverService.Builder().usingDriverExecutable(new File(OSUtil.getChromeDriverPath()))
				.usingAnyFreePort().build();
		service.start();
	}

	@AfterClass
	public static void createAndStopService() {
		service.stop();
	}

	@Before
	public void createDriver() {
		driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
		login();
	}

	@After
	public void quitDriver() {
		driver.quit();
	}

	void login() {
		try {
			driver.get("http://localhost:8080/MsgUI1/login.faces");
	
			// Find the text input element by its name
			WebElement element_user = driver.findElement(By.id("login:userid"));
			element_user.sendKeys("admin");
			
			WebElement element_pswd = driver.findElement(By.name("login:password"));
			element_pswd.sendKeys("admin");
			
			List<WebElement> inputs = driver.findElements(By.xpath("//input"));
			for (WebElement we : inputs) {
				logger.info("Input: " + we.toString());
			}
			
			//element_pswd.submit();
			driver.findElement(By.id("login:submit")).click();
	
			logger.info("Page title is: " + driver.getTitle());
	
			logger.info("Page URL is: " + driver.getCurrentUrl());
			logger.info("Page source is: " + driver.getPageSource());
	        
			// wait until page is loaded
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Main Page"));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
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

		for (int i = 0; i < checkBoxList.size(); i++) {
			WebElement elm = checkBoxList.get(i);
			String checkBoxTitle = elm.getAttribute("title");
			String prefix = StringUtils.removeEnd(checkBoxTitle, "_checkBox");
			
			WebElement listIdElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_listId']"));
			String listId = listIdElm.getText();
			assertTrue(StringUtils.isNotBlank(listId));
			dtl.listIdList.add(listId);
			
			WebElement unsubCountElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_unsubCount']"));
			String unsubCount = unsubCountElm.getText();
			assertTrue(StringUtils.isNotBlank(unsubCount));
			dtl.unsubCountList.add(unsubCount);
			
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
	
	/*
	 Configure Site Profiles
	 Configure POP/IMAP Accounts
	 Configure SMTP Servers
	 
	 Customize Actions for Built-in Rules
	 Setup Custom Bounce Rules and Actions
	 Customize Action Details
	 
	 Setup Email Mailing Lists
	 Upload Email Addresses to List
	 Setup Email Variables
	 Setup Email Templates
	 
	 Manage Email Correspondence
	 Broadcast to a Mailing List
	 Manage Email Addresses
	 Manage Customer Information
	 View Broadcast Messages
	 
	 Manage Site Users
	 Change Password
	 */
	
	
	public static void main(String[] args) {
		OSUtil.setupSeleniumBrowserDriver();
		
		WebDriver driver = new ChromeDriver();
		//WebDriver driver = new FirefoxDriver();
		
		try {
			driver.get("http://localhost:8080/MsgUI1/login.faces");
	
			// Find the text input element by its name
			WebElement element_user = driver.findElement(By.id("login:userid"));
			element_user.sendKeys("admin");
			
			WebElement element_pswd = driver.findElement(By.name("login:password"));
			element_pswd.sendKeys("admin");
			
			List<WebElement> inputs = driver.findElements(By.xpath("//input"));
			for (WebElement we : inputs) {
				logger.info("Input: " + we.toString());
			}
			
			// Now submit the form. WebDriver will find the form for us from the element
			//element_pswd.submit();
			driver.findElement(By.id("login:submit")).click();
	
			// Check the title of the page
			logger.info("Page title is: " + driver.getTitle());
			logger.info("Page URL is: " + driver.getCurrentUrl());
			logger.info("Page source is: " + driver.getPageSource());

			// wait until page is loaded
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Main Page"));
			
			logger.info("Current URL: " + driver.getCurrentUrl());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			// Close the browser
			driver.quit();
		}
	}
	
	/*
	 * Start the ChromeDriver server separately before running tests: 
	 * Terminal:
		$ ./chromedriver
		Started ChromeDriver
		port=9515
		version=14.0.836.0
	 * 
	 * and connect to it using the Remote WebDriver:
	 * WebDriver driver = new RemoteWebDriver("http://127.0.0.1:9515", DesiredCapabilities.chrome());
	 * driver.get("http://www.google.com");
	 */
}

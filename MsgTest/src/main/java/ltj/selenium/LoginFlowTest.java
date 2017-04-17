package ltj.selenium;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.openqa.selenium.support.ui.Select;
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

		for (int i = 0; i < checkBoxList.size(); i++) {
			WebElement elm = checkBoxList.get(i);
			String checkBoxTitle = elm.getAttribute("title");
			String prefix = StringUtils.removeEnd(checkBoxTitle, "_checkBox");
			
			WebElement acceptHtmlElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_acceptHtml']"));
			String acceptHtml = acceptHtmlElm.getText();
			assertTrue(StringUtils.isNotBlank(acceptHtml));
			dtl.accetpHtmlList.add(acceptHtml);
			
			WebElement bounceCountElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_bounceCount']"));
			String bounceCount = bounceCountElm.getText();
			assertTrue(StringUtils.isNotBlank(bounceCount));
			dtl.bounceCountList.add(bounceCount);
			
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

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
	
	static final List<String> FromAddrList = new ArrayList<>();
	static final List<String> RuleNameList = new ArrayList<>();
	static final List<String> SubjectList = new ArrayList<>();
	
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
			
			ListTuple tuple1 = getListDetails();
			
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
			ListTuple tuple2 = getListDetails();
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
			ListTuple tuple3 = getListDetails();
			assertTrue(tuple1.equalsTo(tuple3));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	private static class ListTuple {
		List<String> fromAddrList;
		List<String> subjectList;
		List<String> ruleNameList;
		WebElement viewMsgLink;
		
		boolean equalsTo(ListTuple other) {
			if (fromAddrList != null) {
				if (other.fromAddrList == null) {
					return false;
				}
			}
			if (subjectList != null) {
				if (other.subjectList == null) {
					return false;
				}
			}
			if (ruleNameList != null) {
				if (other.ruleNameList == null) {
					return false;
				}
			}
			return (fromAddrList.equals(other.fromAddrList) && subjectList.equals(other.subjectList) && ruleNameList.equals(other.ruleNameList));
		}
	}
	
	ListTuple getListDetails() {
		List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
		assertFalse(checkBoxList.isEmpty());
		
		ListTuple tuple = new ListTuple();
		
		tuple.fromAddrList = new ArrayList<>();
		tuple.subjectList = new ArrayList<>();
		tuple.ruleNameList = new ArrayList<>();
		
		int idx = new Random().nextInt(checkBoxList.size());

		for (int i = 0; i < checkBoxList.size(); i++) {
			WebElement elm = checkBoxList.get(i);
			String checkBoxTitle = elm.getAttribute("title");
			String prefix = StringUtils.removeEnd(checkBoxTitle, "_checkBox");
			
			WebElement dispNameElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_dispName']"));
			String fromAddr = dispNameElm.getText();
			assertTrue(StringUtils.isNotBlank(fromAddr));
			tuple.fromAddrList.add(fromAddr);
			
			WebElement ruleNameElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_ruleName']"));
			String ruleName = ruleNameElm.getText();
			assertTrue(StringUtils.isNotBlank(ruleName));
			tuple.ruleNameList.add(ruleName);
			
			WebElement viewMsgLink = driver.findElement(By.cssSelector("a[title='" + prefix + "_viewMessage']"));
			String subject = viewMsgLink.getText();
			tuple.subjectList.add(subject);
			
			if (i == idx) {
				tuple.viewMsgLink = viewMsgLink;
			}
		}
		
		if (FromAddrList.isEmpty()) {
			FromAddrList.addAll(tuple.fromAddrList);
		}
		if (RuleNameList.isEmpty()) {
			RuleNameList.addAll(tuple.ruleNameList);
		}
		if (SubjectList.isEmpty()) {
			SubjectList.addAll(tuple.subjectList);
		}
		assertNotNull(tuple.viewMsgLink);
		
		return tuple;
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

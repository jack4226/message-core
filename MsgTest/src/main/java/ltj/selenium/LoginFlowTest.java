package ltj.selenium;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			
			// Email List page
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement allMsgLink = driver.findElement(By.cssSelector("a[title='All Messages']"));
			allMsgLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			Select selectRuleName = new Select(driver.findElement(By.cssSelector("select[title='Select Rule Name']")));
			WebElement selectedRuleName = selectRuleName.getFirstSelectedOption();
			assertEquals("All", selectedRuleName.getAttribute("value"));
			
			WebElement checkAll = driver.findElement(By.id("msgform:msgrow:checkAll"));
			checkAll.click();

			WebElement markUnreadLink = driver.findElement(By.cssSelector("a[title='Mark as unread']"));
			markUnreadLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			
			List<String> fromAddrList = new ArrayList<>();
			List<String> subjectList = new ArrayList<>();
			List<String> ruleNameList = new ArrayList<>();

			WebElement viewMsgLink = null;

			for (int i = 0; i < checkBoxList.size() && i < 2; i++) {
				WebElement elm = checkBoxList.get(i);
				String checkBoxTitle = elm.getAttribute("title");
				String prefix = StringUtils.removeEnd(checkBoxTitle, "_checkBox");
				
				WebElement dispNameElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_dispName']"));
				String fromAddr = dispNameElm.getText();
				assertTrue(StringUtils.isNotBlank(fromAddr));
				fromAddrList.add(fromAddr);
				
				WebElement ruleNameElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_ruleName']"));
				String ruleName = ruleNameElm.getText();
				assertTrue(StringUtils.isNotBlank(ruleName));
				ruleNameList.add(ruleName);
				
				// View Message page
				viewMsgLink = driver.findElement(By.cssSelector("a[title='" + prefix + "_viewMessage']"));
				String subject = viewMsgLink.getText();
				subjectList.add(subject);
			}
			
			// View message details
			assertNotNull(viewMsgLink);
			viewMsgLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inboxview:gettingStartedFooter")));

			// verify that fields from details and list match
			WebElement fromAddrElm = driver.findElement(By.id("inboxview:from_address"));
			assertTrue(StringUtils.contains(fromAddrElm.getText(), fromAddrList.get(fromAddrList.size() - 1)));
			
			WebElement ruleNameElm = driver.findElement(By.id("inboxview:rule_name"));
			assertEquals(ruleNameElm.getText(), ruleNameList.get(ruleNameList.size() - 1));
			
			WebElement subjectElm = driver.findElement(By.id("inboxview:msg_subject"));
			assertTrue(StringUtils.contains(subjectElm.getText(), subjectList.get(subjectList.size() - 1)));
			
			WebElement bodyElm = driver.findElement(By.id("inboxview:body_content_msg"));
			String body = bodyElm.getText();
			assertTrue(StringUtils.isNotBlank(body));
			if (StringUtils.contains(body, "System Email Id:")) {
				logger.info("Embeded Email Id Found.");
			}
			
			Select selectNewRM = new Select(driver.findElement(By.id("inboxview:newrulename")));
			WebElement selectednewRM = selectNewRM.getFirstSelectedOption();
			assertEquals(ruleNameElm.getText(), selectednewRM.getAttribute("value"));
			
			// Go back to the list
			WebElement goback = driver.findElement(By.cssSelector("input[title='Go back to List']"));
			goback.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// click page next/previous
			try {
				WebElement pageNext = driver.findElement(By.id("msgform:msgrow:pagenext"));
				pageNext.click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
				WebElement pagePrev = driver.findElement(By.id("msgform:msgrow:pageprev"));
				pagePrev.click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}

			// click page last/first
			try {
				WebElement pageLast = driver.findElement(By.id("msgform:msgrow:pagelast"));
				pageLast.click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
				WebElement pageFirst = driver.findElement(By.id("msgform:msgrow:pagefirst"));
				pageFirst.click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}

		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
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

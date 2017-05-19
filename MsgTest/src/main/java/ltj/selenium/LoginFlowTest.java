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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.interactions.Actions;
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
	protected WebDriverWait waitLong;

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
		waitLong = new WebDriverWait(driver, 30);
		login();
	}

	@After
	public void quitDriver() {
		driver.quit();
	}

	void login() {
		try {
			driver.get("http://localhost:8080/MsgUI2/login.faces");
	
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
	public void testAddNewRecord() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Templates";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			
			Actions builder = new Actions(driver);
			
			// Add a new record
			driver.findElement(By.cssSelector("input[title='Add a new row']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:templateid")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:bodytext")));
			
			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			String testTmpltId = "TestTemplate_" + suffix;
			
			driver.findElement(By.id("emailtmplt:detail:templateid")).sendKeys(testTmpltId);
			
			Select listIdSelect = new Select(driver.findElement(By.id("emailtmplt:detail:listid")));
			listIdSelect.selectByValue("SMPLLST2");
			
			Select listTypeSelect = new Select(driver.findElement(By.id("emailtmplt:detail:listtype")));
			listTypeSelect.selectByValue("Personalized");
			// above statement will fire an ajax event
			
			wait.until(ExpectedConditions.textToBePresentInElementValue(By.id("emailtmplt:detail:listtype"), "Personalized"));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:subject")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("emailtmplt:detail:subject")));
			
			driver.findElement(By.id("emailtmplt:detail:subject")).sendKeys("Test Subject " + suffix);
			
			wait.until(ExpectedConditions.elementToBeClickable(By.id("emailtmplt:detail:emailid")));
			builder.moveToElement(driver.findElement(By.id("emailtmplt:detail:emailid"))).build().perform();
			
			Select emailIdSelect = new Select(driver.findElement(By.id("emailtmplt:detail:emailid")));
			emailIdSelect.selectByVisibleText("Y");
			
			Select varNameSelect = new Select(driver.findElement(By.id("emailtmplt:detail:vname")));
			varNameSelect.selectByValue("SubscriberAddressId");
			// XXX StaleElementReferenceException: stale element reference: element is not attached to the page document
			
			//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("insert_variable")));
			//wait.until(ExpectedConditions.elementToBeClickable(By.id("insert_variable")));
			//driver.findElement(By.id("insert_variable")).click();
			// XXX StaleElementReferenceException: stale element reference: element is not attached to the page document
			
			//wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:bodytext")));
			
			
			builder.moveToElement(driver.findElement(By.id("emailtmplt:detail:bodytext"))).build().perform();
			
			//wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:detail:bodytext")));
			//driver.findElement(By.id("emailtmplt:detail:bodytext")).sendKeys("Test message body " + suffix);
			// XXX ElementNotVisibleException: element not visible
			
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Submit changes']")));
			
			// Submit changes
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Submit changes']"));
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			// wait until the last record become available
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='" + nextIdx + "_viewDetail']")));
			
			// Verify update results
			List<WebElement> viewDetailList =  driver.findElements(By.cssSelector("a[title$='_viewDetail']"));
			WebElement viewDetailElm = null;
			for (WebElement elm : viewDetailList) {
				logger.info("viewDetail item: " + elm.getAttribute("title") + ", " + elm.getText());
				if (StringUtils.equals(testTmpltId, elm.getText())) {
					viewDetailElm = elm;
					break;
				}
			}
			assertNotNull(viewDetailElm);
			String tmpltTitle = viewDetailElm.getAttribute("title");
			String tmpltIdx = StringUtils.removeEnd(tmpltTitle, "_viewDetail");
			
			// Delete added record
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']")));
			driver.findElement(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + tmpltIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Delete selected rows']")).click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailtmplt:footer:gettingStartedFooter")));
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

package ltj.selenium;

import java.io.File;
import java.io.IOException;
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
	public void testSiteProfiles() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Configure Site Profiles";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			
			WebElement siteNameElm = driver.findElement(By.cssSelector("span[title='JBatchCorp_senderName']"));
			String siteNameBefore = siteNameElm.getText();
			String siteNameAfter = null;
			if (StringUtils.endsWith(siteNameBefore, "_updated")) {
				siteNameAfter = StringUtils.removeEnd(siteNameBefore, "_updated");
			}
			else {
				siteNameAfter = siteNameBefore + "_updated";
			}
			
			WebElement useTestAddrElm = driver.findElement(By.cssSelector("span[title='JBatchCorp_userTestAddr']"));
			String useTestAddr = useTestAddrElm.getText();
			
			WebElement viewDetailLink = driver.findElement(By.cssSelector("a[title='JBatchCorp_viewDetail']"));
			viewDetailLink.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailprof:footer:gettingStartedFooter")));
			
			WebElement siteName = driver.findElement(By.id("emailprof:content:sitename"));
			siteName.clear();
			siteName.sendKeys(siteNameAfter);
			
			Select selectTest = new Select(driver.findElement(By.id("emailprof:content:usetest")));
			WebElement selectedTest = selectTest.getFirstSelectedOption();
			logger.info("Use Test Address selected before: " + selectedTest.getText());
			if ("true".equalsIgnoreCase(useTestAddr)) {
				assertEquals("Yes", selectedTest.getText());
				selectTest.selectByVisibleText("No");
			}
			else {
				assertEquals("No", selectedTest.getText());
				selectTest.selectByVisibleText("Yes");
			}
 			
			Select selectVerp = new Select(driver.findElement(By.id("emailprof:content:useverp")));
			WebElement selectedVerp = selectVerp.getFirstSelectedOption();
			logger.info("Is Verp selected before: " + selectedVerp.getText());
			String selectedVerpBefore = selectedVerp.getText();
			if ("No".equals(selectedVerp.getText())) {
				selectVerp.selectByVisibleText("Yes");
			}
			else {
				selectVerp.selectByVisibleText("No");
			}
			
			WebElement submit = driver.findElement(By.id("emailprof:content:submit"));
			submit.click();
			
			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}
			
			// verify the results
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			WebElement siteNameAfterElm = driver.findElement(By.cssSelector("span[title='JBatchCorp_senderName']"));
			assertEquals(siteNameAfter, siteNameAfterElm.getText());
			
			WebElement useTestAddrAfter = driver.findElement(By.cssSelector("span[title='JBatchCorp_userTestAddr']"));
			if ("true".equalsIgnoreCase(useTestAddr)) {
				assertEquals("false", useTestAddrAfter.getText());
			}
			else {
				assertEquals("true", useTestAddrAfter.getText());
			}
			
			WebElement selectedVerpAfter = driver.findElement(By.cssSelector("span[title='JBatchCorp_verpEnabled']"));
			if ("yes".equalsIgnoreCase(selectedVerpBefore)) {
				assertEquals("false", selectedVerpAfter.getText());
			}
			else {
				assertEquals("true", selectedVerpAfter.getText());
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

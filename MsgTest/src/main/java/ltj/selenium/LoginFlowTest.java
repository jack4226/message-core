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
import org.junit.Ignore;
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
import org.openqa.selenium.support.ui.ExpectedCondition;
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
	
	@Ignore
	public void testMainPage() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			List<WebElement> links = driver.findElements(By.partialLinkText("Configure"));
			for (WebElement we : links) {
				logger.info("HREF Link: " + we.toString());
			}
			assertEquals(3, links.size());
			
			String winHandleBefore = driver.getWindowHandle();
			logger.info("Current Window Handle: " + winHandleBefore);
	        for(String winHandle : driver.getWindowHandles()) {
	        	if (!StringUtils.equals(winHandleBefore, winHandle)) {
	        		logger.info("Switch to Window Handle: " + winHandle);
	        		driver.switchTo().window(winHandle);
	        	}
	        }
	        
	        links.get(0).click();
	        logger.info("Next URL: " + driver.getCurrentUrl());
			
			(new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return d.getTitle().equals("Configure Site Profiles");
				}
			});
			
			Thread.sleep(1000);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	@Test
	public void testSmtpServers() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText("Configure SMTP Servers"));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Configure SMTP Servers"));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement useSslElm = driver.findElement(By.cssSelector("span[title=smtpServer_useSsl]"));
			assertNotNull(useSslElm);
			logger.info("useSsl before: " + useSslElm.toString() + ", text: " + useSslElm.getText());
			
			String useSslBefore = useSslElm.getText();
			
			WebElement persistenceElm = driver.findElement(By.cssSelector("span[title=smtpServer_persistence]"));
			assertNotNull(persistenceElm);
			logger.info("Persistence before: " + persistenceElm.toString() + ", text: " + persistenceElm.getText());
			
			String persistenceBefore = persistenceElm.getText();
			
			link = driver.findElement(By.cssSelector("a[title='smtpServer']"));
			assertNotNull(link);
			
			link.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("smtpedit:footer:gettingStartedFooter")));
			
			WebElement desc = driver.findElement(By.id("smtpedit:content:desc"));
			assertNotNull(desc);
			logger.info("Description: " + desc.getAttribute("value") + ", test: " + desc.getText());
			//desc.clear();
			//desc.sendKeys("");
			
			Select selectSsl = new Select(driver.findElement(By.id("smtpedit:content:ssl")));
			assertNotNull(selectSsl);
			WebElement selectedSsl = selectSsl.getFirstSelectedOption();
			logger.info("Use SSL selected before: " + selectedSsl.getText());
			if ("true".equalsIgnoreCase(useSslBefore)) {
				assertEquals("Yes", selectedSsl.getText());
				selectSsl.selectByVisibleText("No");
			}
			else {
				assertEquals("No", selectedSsl.getText());
				selectSsl.selectByVisibleText("Yes");
			}
 			
			Select selectStatus = new Select(driver.findElement(By.id("smtpedit:content:statusid")));
			assertNotNull(selectStatus);
			WebElement selectedStatus = selectStatus.getFirstSelectedOption();
			logger.info("StatusId before: " + selectedStatus.getAttribute("value") + ", text: " + selectedStatus.getText());
			String selectedStatusIdBefore = selectedStatus.getAttribute("value");
			//selectStatus.deselectAll();
			if ("A".equals(selectedStatusIdBefore)) {
				selectStatus.selectByValue("I");
			}
			else {
				selectStatus.selectByValue("A");
			}
			
			WebElement submit = driver.findElement(By.id("smtpedit:content:submit"));
			assertNotNull(submit);
			submit.click();
			
			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running from Maven test
				logger.error(e.getMessage());
			}
			
			// verify the results
			wait.until(ExpectedConditions.titleIs("Configure SMTP Servers"));
			
			WebElement useSslAfter = driver.findElement(By.cssSelector("span[title=smtpServer_useSsl]"));
			assertNotNull(useSslAfter);
			if ("true".equalsIgnoreCase(useSslBefore)) {
				assertEquals("false", useSslAfter.getText());
			}
			else {
				assertEquals("true", useSslAfter.getText());
			}
			
			WebElement selectedStatusAfter = driver.findElement(By.cssSelector("span[title=smtpServer_statusId]"));
			assertNotNull(selectedStatusAfter);
			logger.info("StatusId after: " + selectedStatusAfter.getText());
			if ("A".equals(selectedStatusIdBefore)) {
				assertEquals("Inactive", selectedStatusAfter.getText());
			}
			else {
				assertEquals("Active", selectedStatusAfter.getText());
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

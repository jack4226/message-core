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
	public void testEmailVariables() {
		String listTitle = "Setup Email Variables";
		String testVarName = "CustomerLastName";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			// Email Variable List page
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement defaultValue = driver.findElement(By.cssSelector("span[title='CustomerLastName_defaultValue']"));
			assertNotNull(defaultValue);
			String defaultValueBefore = defaultValue.getText();
			assertTrue(defaultValueBefore.startsWith("Valued Customer"));
			
			WebElement className = driver.findElement(By.cssSelector("span[title='CustomerLastName_className']"));
			assertNotNull(className);
			String classNameShort = className.getText();
			assertEquals(".CustomerNameResolver", className.getText());
			
			WebElement listStatId = driver.findElement(By.cssSelector("span[title='CustomerLastName_statusId']"));
			assertNotNull(listStatId);
			assertEquals("Active", listStatId.getText());
			String statusId = listStatId.getText();
			
			WebElement listEditLink = driver.findElement(By.cssSelector("a[title=CustomerLastName]"));
			assertNotNull(listEditLink);
			assertEquals(testVarName, listEditLink.getText());
			
			listEditLink.click();
			
			// Email Variable Edit Page
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			WebElement varNameElm = driver.findElement(By.id("emailvarbl:content:variablename"));
			assertNotNull(varNameElm);
			assertEquals(testVarName, varNameElm.getAttribute("value"));
			assertFalse(varNameElm.isEnabled());
			
			WebElement colNameElm = driver.findElement(By.id("emailvarbl:content:columnname"));
			assertNotNull(colNameElm);
			assertEquals("LastName", colNameElm.getAttribute("value"));
			
			WebElement procNameElm = driver.findElement(By.id("emailvarbl:content:variableproc"));
			assertNotNull(procNameElm);
			assertTrue(procNameElm.getAttribute("value").endsWith(classNameShort));
			
			WebElement defaultElm = driver.findElement(By.id("emailvarbl:content:defaultvalue"));
			assertNotNull(defaultElm);
			String defaultValueAfter = defaultElm.getText();
			assertTrue(defaultValueAfter.startsWith("Valued Customer"));
			if (StringUtils.endsWith(defaultValueAfter, "_updated")) {
				defaultValueAfter = StringUtils.removeEnd(defaultValueAfter, "_updated");
			}
			else {
				defaultValueAfter += "_updated";
			}
			assertFalse(defaultValueAfter.equals(defaultValueBefore));
			defaultElm.clear();
			defaultElm.sendKeys(defaultValueAfter);
 			
			Select selectStatId = new Select(driver.findElement(By.id("emailvarbl:content:statusid")));
			WebElement selectedStatId = selectStatId.getFirstSelectedOption();
			logger.info("Status Id selected: " + selectedStatId.getText());
			if ("Active".equals(statusId)) {
				assertEquals("A", selectedStatId.getAttribute("value"));
			}
			else {
				assertEquals("I", selectedStatId.getAttribute("value"));
			}
			
			// Click on Test button to test SQL query
			WebElement testQuery = driver.findElement(By.id("emailvarbl:content:testquery"));
			assertNotNull(testQuery);
			testQuery.click();
			// Verify query test result
			WebElement testResult = driver.findElement(By.id("emailvarbl:content:testResult"));
			assertEquals("Test was successful, query is valid.", testResult.getText());
			
			// submit the changes and go back to list page
			WebElement submit = driver.findElement(By.id("emailvarbl:content:submit"));
			assertNotNull(submit);
			submit.click();
			
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
			
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			WebElement refreshLink = driver.findElement(By.cssSelector("input[title='Refresh from database']"));
			refreshLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("advanced:footer:gettingStartedFooter")));
			
			// verify results
			WebElement defaultValueAfterElm = driver.findElement(By.cssSelector("span[title='CustomerFirstName_defaultValue']"));
			assertNotNull(defaultValueAfterElm);
			// TODO fix this
			//assertEquals(defaultValueAfter, defaultValueAfterElm.getText());
			
			
			// View SubscriberUrl
			WebElement subrUrlLink = driver.findElement(By.cssSelector("a[title=SubscribeURL]"));
			assertEquals("SubscribeURL", subrUrlLink.getText());
			
			subrUrlLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			// SubscriberURL edit page
			varNameElm = driver.findElement(By.id("emailvarbl:content:variablename"));
			assertEquals("SubscribeURL", varNameElm.getAttribute("value"));
			assertFalse(varNameElm.isEnabled());
			
			defaultElm = driver.findElement(By.id("emailvarbl:content:defaultvalue"));
			assertTrue(defaultElm.getText().contains("sbsrid=${SubscriberAddressId}"));
			
			// Go back to the list page
			WebElement cancel = driver.findElement(By.cssSelector("input[title='Cancel changes']"));
			cancel.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("advanced:footer:gettingStartedFooter")));
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

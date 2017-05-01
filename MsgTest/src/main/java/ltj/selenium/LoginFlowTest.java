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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.javascript.host.css.CSS;

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
	public void testViewListAndCopy() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Email Variables";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// List
			List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
			assertFalse(checkBoxList.isEmpty());
			int nextIdx = checkBoxList.size();
			List<String> titleList = new ArrayList<>();
			for (WebElement elm : checkBoxList) {
				if (elm.isEnabled()) {
					titleList.add(elm.getAttribute("title"));
				}
			}
			
			int idx = new Random().nextInt(titleList.size() - 1);
			String checkBoxTitle = titleList.get(idx);
			
			List<WebElement> defaultValueList = driver.findElements(By.cssSelector("span[title$='_defaultValue']"));
			assertEquals(checkBoxList.size(), defaultValueList.size());
			String defaultValueTitle = defaultValueList.get(idx).getAttribute("title");
			String defaulValueBefore = defaultValueList.get(idx).getText();

			List<WebElement> viewDetailList = driver.findElements(By.cssSelector("a[title$='_viewDetail']"));
			assertEquals(checkBoxList.size(), viewDetailList.size());
			WebElement viewDetailLink = viewDetailList.get(idx);
			String viewDetailTitle = viewDetailLink.getAttribute("title");
			
			// View Details page
			viewDetailLink = driver.findElement(By.cssSelector("a[title='" + viewDetailTitle + "']"));
			String variableName = viewDetailLink.getText();
			viewDetailLink.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			WebElement varNameElm = driver.findElement(By.id("emailvarbl:content:variablename"));
			assertEquals(variableName, varNameElm.getAttribute("value"));
			assertEquals(false, varNameElm.isEnabled());
			
			WebElement defaulElm = driver.findElement(By.id("emailvarbl:content:defaultvalue"));
			String defaultValueAfter = defaulElm.getAttribute("value");
			assertEquals(defaulValueBefore, defaultValueAfter);
			if (StringUtils.endsWith(defaultValueAfter, "_updated")) {
				defaultValueAfter = StringUtils.removeEnd(defaultValueAfter, "_updated");
			}
			else {
				defaultValueAfter += "_updated";
			}
			defaulElm.clear();
			defaulElm.sendKeys(defaultValueAfter);
			
			// Submit changes
			driver.findElement(By.cssSelector("input[title='Submit changes']")).click();
			
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

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Verify update results
			WebElement defaultVrf = driver.findElement(By.cssSelector("span[title='" + defaultValueTitle + "']"));
			assertEquals(defaultValueAfter, defaultVrf.getText());
			
			
			// Tick a selected record
			driver.findElement(By.cssSelector("input[title='" + checkBoxTitle + "']")).click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + checkBoxTitle + "']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Copy from selected
			driver.findElement(By.cssSelector("input[title='Create a new row from selected']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("emailvarbl:footer:gettingStartedFooter")));
			
			String suffix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
			String testVarName = "TestVariable_" + suffix;
			driver.findElement(By.id("emailvarbl:content:variablename")).sendKeys(testVarName);
			
			// Submit changes
			driver.findElement(By.cssSelector("input[title='Submit changes']")).click();
			
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

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Delete added record
			driver.findElement(By.cssSelector("input[title='" + nextIdx + "_checkBox']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector("input[title='" + nextIdx + "_checkBox']"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			driver.findElement(By.cssSelector("input[title='Delete selected rows']")).click();
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
			// Go back to the list
//			driver.findElement(By.cssSelector("input[title='Go Back']")).click();
//			
//			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mailvarlst:footer:gettingStartedFooter")));
			
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

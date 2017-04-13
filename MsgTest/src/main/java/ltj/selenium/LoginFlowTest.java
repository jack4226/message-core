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
	public void testMailingList() {
		String listTitle = "Setup Email Mailing Lists";
		String testListName = "SMPLLST1";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			// Action Details List page
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement listDispName = driver.findElement(By.cssSelector("span[title='SMPLLST1_displayName']"));
			assertNotNull(listDispName);
			String dispNameBefore = listDispName.getText();
			assertTrue(listDispName.getText().startsWith("Sample List 1"));
			
			WebElement listAddr = driver.findElement(By.cssSelector("span[title='SMPLLST1_emailAddr']"));
			assertNotNull(listAddr);
			assertEquals("demolist1@localhost", listAddr.getText());
			
			WebElement listStatId = driver.findElement(By.cssSelector("span[title='SMPLLST1_statusId']"));
			assertNotNull(listStatId);
			assertEquals("Active", listStatId.getText());
			String statusId = listStatId.getText();
			
			WebElement listEditLink = driver.findElement(By.cssSelector("a[title=SMPLLST1]"));
			assertNotNull(listEditLink);
			assertEquals(testListName, listEditLink.getText());
			
			listEditLink.click();
			
			// Mailing List Edit Page
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mlstedit:footer:gettingStartedFooter")));
			
			WebElement listIdElm = driver.findElement(By.id("mlstedit:content:listid"));
			assertNotNull(listIdElm);
			assertEquals(testListName, listIdElm.getAttribute("value"));
			
			WebElement acctUserElm = driver.findElement(By.id("mlstedit:content:acctuser"));
			assertNotNull(acctUserElm);
			assertEquals("demolist1", acctUserElm.getAttribute("value"));
			
			WebElement listDescElm = driver.findElement(By.id("mlstedit:content:desc"));
			assertNotNull(listDescElm);
			String actionDesc = listDescElm.getAttribute("value");
			assertTrue(actionDesc.startsWith("Sample mailing list 1"));
			if (StringUtils.endsWith(actionDesc, "_updated")) {
				actionDesc = StringUtils.removeEnd(actionDesc, "_updated");
			}
			else {
				actionDesc += "_updated";
			}
			listDescElm.clear();
			listDescElm.sendKeys(actionDesc);
 			
			WebElement dispNameElm = driver.findElement(By.id("mlstedit:content:dispname"));
			assertNotNull(dispNameElm);
			String  dispName = dispNameElm.getAttribute("value");
			assertTrue(StringUtils.startsWith(dispName, "Sample List 1"));
			if (StringUtils.endsWith(dispName, " - v2")) {
				dispName = StringUtils.removeEnd(dispName, " - v2");
			}
			else {
				dispName += " - v2";
			}
			assertFalse(dispName.equals(dispNameBefore));
			dispNameElm.clear();
			dispNameElm.sendKeys(dispName);
 			
			Select selectStatId = new Select(driver.findElement(By.id("mlstedit:content:statusid")));
			WebElement selectedStatId = selectStatId.getFirstSelectedOption();
			logger.info("Status Id selected: " + selectedStatId.getText());
			if ("Active".equals(statusId)) {
				assertEquals("A", selectedStatId.getAttribute("value"));
			}
			else {
				assertEquals("I", selectedStatId.getAttribute("value"));
			}
			
			// submit the changes and go back to list page
			WebElement submit = driver.findElement(By.id("mlstedit:content:submit"));
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
			
			// verify results
			WebElement dispNameAfterElm = driver.findElement(By.cssSelector("span[title='SMPLLST1_displayName']"));
			assertNotNull(dispNameAfterElm);
			assertEquals(dispName, dispNameAfterElm.getText());
			
			// View Subscriber List page
			WebElement viewListLink = driver.findElement(By.cssSelector("a[title='SMPLLST1_viewList']"));
			assertNotNull(viewListLink);
			viewListLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("subrlist:footer:gettingStartedFooter")));
			
			List<WebElement> addrElmList = driver.findElements(By.cssSelector("span[title$='_emailAddr']"));
			assertFalse(addrElmList.isEmpty());
			WebElement addr1Elm = addrElmList.get(0);
			String addr1Title = addr1Elm.getAttribute("title");
			assertTrue(addr1Title.endsWith("_emailAddr"));
			String prefix = StringUtils.removeEnd(addr1Title, "_emailAddr");
			assertTrue(prefix.length() > 0);
			logger.info("Email address 1: " + addr1Elm.getText());
			String emailAddr1 = addr1Elm.getText();
			
			// tick the check box
			WebElement checkBox = driver.findElement(By.cssSelector("input[title='" + prefix + "_checkbox']"));
			if (checkBox.isEnabled() && !checkBox.isSelected()) {
				checkBox.click();
			}
			else {
				fail("The check box is not enabled or is already selected!");
			}
			
			driver.findElement(By.cssSelector("span[title='" + prefix + "_sentCount']"));
			driver.findElement(By.cssSelector("span[title='" + prefix + "_openCount']"));
			driver.findElement(By.cssSelector("span[title='" + prefix + "_clickCount']"));
			
			Select selectSubed = new Select(driver.findElement(By.cssSelector("select[title='" + prefix + "_subscribed']")));
			WebElement selectedSubed = selectSubed.getFirstSelectedOption();
			logger.info("Is " + emailAddr1 + " subscribed? " + selectedSubed.getText());
			
			Select selectHtml = new Select(driver.findElement(By.cssSelector("select[title='" + prefix + "_acceptHtml']")));
			WebElement selectedHtml = selectHtml.getFirstSelectedOption();
			logger.info("Is " + emailAddr1 + " accept HTML? " + selectedHtml.getText());
			// flip the accept HTML flag
			if ("true".equals(selectedHtml.getAttribute("value"))) {
				selectHtml.selectByVisibleText("No");
			}
			else {
				selectHtml.selectByVisibleText("Yes");
			}
			logger.info("Accept HTML is changed to: " + selectedHtml.getAttribute("value"));
			
			// submit the change
			WebElement saveChange = driver.findElement(By.cssSelector("input[title='Save selected rows']"));
			assertNotNull(saveChange);
			if (saveChange.isEnabled()) {
				saveChange.click();
				//saveChange.submit();
			}
			else {
				fail("The Save selected rows button is not enabled!");
			}
			
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("subrlist:footer:gettingStartedFooter")));
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

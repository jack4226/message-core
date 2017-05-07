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
	public void testListAndViewDetails() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Custom Bounce Rules and Actions";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// View/Edit Details page
			WebElement viewDetailLink = driver.findElement(By.cssSelector("a[title='HardBouce_WatchedMailbox_viewDetail']"));
			viewDetailLink.click();

			logger.info("View Detail page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			WebElement ruleDescEdt = driver.findElement(By.id("ruleedit:content:desc"));
			String desc = ruleDescEdt.getAttribute("value");
			if (StringUtils.endsWith(desc, "_updated")) {
				desc = StringUtils.removeEnd(desc, "_updated");
			}
			else {
				desc += "_updated";
			}
			ruleDescEdt.sendKeys(desc);
			
			Select selectCategory = new Select(driver.findElement(By.id("ruleedit:content:rulecategory")));
			WebElement selectedCtgy = selectCategory.getFirstSelectedOption();
			assertEquals("P", selectedCtgy.getAttribute("value"));
			
			Select selectSubrule = new Select(driver.findElement(By.id("ruleedit:content:subrule")));
			WebElement selectedSubrule = selectSubrule.getFirstSelectedOption();
			assertEquals("false", selectedSubrule.getAttribute("value"));
			
			List<WebElement> ruleTypeList = driver.findElements(By.cssSelector("input[id^='ruleedit:content:ruletype:']"));
			assertEquals(4, ruleTypeList.size());
			WebElement ruleTypeElm2 = ruleTypeList.get(1);
			assertEquals(true, ruleTypeElm2.isSelected());
			
			List<WebElement> chkboxList = driver.findElements(By.cssSelector("input[id$=':checkbox']"));
			assertTrue(chkboxList.size() >= 2);
			int nextIdx = chkboxList.size();
			
			// Select the last record to copy from
			WebElement checkBox2Link = chkboxList.get(nextIdx - 1);
			checkBox2Link.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			
			// Create a new record from selected record
			WebElement copySelecedLink = driver.findElement(By.cssSelector("input[title='Create a new row from selected']"));
			copySelecedLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":checkbox")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			Select selectDataName = new Select(driver.findElement(By.id("ruleedit:content:data_table:" + nextIdx + ":dataname")));
			selectDataName.selectByValue("From");
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":dataname")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":criteria")));
			wait.until(ExpectedConditions.elementToBeClickable(By.id("ruleedit:content:data_table:" + nextIdx + ":criteria")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":editelement")));
			
			Select selectCriteria = new Select(driver.findElement(By.id("ruleedit:content:data_table:" + nextIdx + ":criteria")));
			selectCriteria.selectByValue("contains");
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":casesensitive")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":editelement")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[title='Submit Changes']")));
			
			// Submit changes
			WebElement submitChanges = driver.findElement(By.cssSelector("input[title='Submit Changes']"));
			submitChanges.click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[id='ruleedit:content:data_table:" + nextIdx + ":checkbox']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='ruleedit:content:data_table:" + nextIdx + ":checkbox']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			// Refresh from database
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Refresh from database']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Refresh from database']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			driver.findElement(By.cssSelector("input[title='Refresh from database']")).click();
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id("ruleedit:content:data_table:" + nextIdx + ":checkbox"), false));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			// Delete the added record
			AlertUtil.clickCommandLink(driver, By.id("ruleedit:content:data_table:" + nextIdx + ":checkbox"));
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id("ruleedit:content:data_table:" + nextIdx + ":checkbox"), true));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":checkbox")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Delete selected rows']"));
			
			AlertUtil.handleAlert(driver);
			
			// Refresh from database
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Refresh from database']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Refresh from database']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			driver.findElement(By.cssSelector("input[title='Refresh from database']")).click();
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("input[id^='ruleedit:content:data_table:" + nextIdx + ":']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
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

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
	public void testCustomRules() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText("Setup Custom Bounce Rules and Actions"));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Setup Custom Bounce Rules and Actions"));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			
			WebElement ruleType = driver.findElement(By.cssSelector("span[title=OutOfOffice_AutoReply_ruleType]"));
			assertNotNull(ruleType);
			assertEquals("All", ruleType.getText());
			
			WebElement statusId = driver.findElement(By.cssSelector("span[title=OutOfOffice_AutoReply_statusId]"));
			assertNotNull(statusId);
			assertEquals("Active", statusId.getText());
			
			WebElement ruleEditLink = driver.findElement(By.cssSelector("a[title=OutOfOffice_AutoReply]"));
			assertNotNull(ruleEditLink);
			logger.info("OutOfOffice_AutoReply: " + ruleEditLink.getText());
			assertEquals("OutOfOffice_AutoReply", ruleEditLink.getText());
			
			WebElement viewActionLink = driver.findElement(By.cssSelector("a[title=OutOfOffice_AutoReply_viewAction]"));
			assertNotNull(viewActionLink);
			logger.info("View Action Link Test: " +viewActionLink.getText());
			assertEquals("Edit", viewActionLink.getText());
			
			WebElement viewSubRuleLink = driver.findElement(By.cssSelector("a[title=Unattended_Mailbox_viewSubRule]"));
			assertNotNull(viewSubRuleLink);
			logger.info("View SubRule Link Text: " +viewSubRuleLink.getText());
			assertEquals("Add", viewSubRuleLink.getText());
			
			ruleEditLink.click();
			
			// Rule Edit Page
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			WebElement ruleNameElm = driver.findElement(By.id("ruleedit:content:rulename"));
			assertNotNull(ruleNameElm);
			logger.info("Rule Name: " + ruleNameElm.getText());
			assertEquals("OutOfOffice_AutoReply", ruleNameElm.getAttribute("value"));
			
			WebElement ruleDescElm = driver.findElement(By.id("ruleedit:content:desc"));
			assertNotNull(ruleDescElm);
			assertEquals("Ouf of the office auto reply", ruleDescElm.getAttribute("value"));
 			
			Select selectStatId = new Select(driver.findElement(By.id("ruleedit:content:statusid")));
			WebElement selectedStatId = selectStatId.getFirstSelectedOption();
			logger.info("Status Id selected: " + selectedStatId.getText());
			assertEquals("A", selectedStatId.getAttribute("value"));
			
			Select selectMailType = new Select(driver.findElement(By.id("ruleedit:content:mailtype")));
			assertNotNull(selectMailType);
			WebElement selectedMailType = selectMailType.getFirstSelectedOption();
			assertEquals("SMTP Mail", selectedMailType.getText());
			
			Select selectCategory = new Select(driver.findElement(By.id("ruleedit:content:rulecategory")));
			WebElement selectedCategory = selectCategory.getFirstSelectedOption();
			logger.info("Rule Category selected: " + selectedCategory.getText());
			assertEquals("M", selectedCategory.getAttribute("value"));
			
			Select selectSubrule = new Select(driver.findElement(By.id("ruleedit:content:subrule")));
			WebElement selectedSubrule = selectSubrule.getFirstSelectedOption();
			logger.info("Is Subrule selected: " + selectedSubrule.getText());
			assertEquals("false", selectedSubrule.getAttribute("value"));
			
			
			List<WebElement> ruleTypes = driver.findElements(By.cssSelector("input[type='radio'][name='ruleedit:content:ruletype']"));
			boolean found = false;
			for (WebElement type : ruleTypes) {
				String checked = type.getAttribute("checked");
				if (StringUtils.isNotBlank(checked)) {
					assertEquals("All", type.getAttribute("value"));
					found = true;
					break;
				}
			}
			assertEquals(true, found);

			Select selectVarName = new Select(driver.findElement(By.cssSelector("select[title=Subject_change]")));
			WebElement selectedVarName = selectVarName.getFirstSelectedOption();
			logger.info("Variable name selected: " + selectedVarName.getText());
			assertEquals("Subject", selectedVarName.getAttribute("value"));

			Select selectCriteria = new Select(driver.findElement(By.cssSelector("select[title=Subject_criteria]")));
			WebElement selectedCriteria = selectCriteria.getFirstSelectedOption();
			logger.info("Criteria selected: " + selectedCriteria.getText());
			assertEquals("reg_ex", selectedCriteria.getAttribute("value"));

			
			WebElement viewElement = driver.findElement(By.cssSelector("a[title='Subject_viewElement']"));
			assertNotNull(viewElement);
			viewElement.click();
			
			// View Rule Element page
			logger.info("View Element URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			WebElement elmRuleName = driver.findElement(By.id("ruleedit:content:rulename"));
			logger.info("Element Rule Name selected: " + elmRuleName.getText());
			assertEquals("OutOfOffice_AutoReply", elmRuleName.getAttribute("value"));
			assertFalse(elmRuleName.isEnabled());
			
			Select selectDataName = new Select(driver.findElement(By.id("ruleedit:content:dataname")));
			WebElement selectedDataName = selectDataName.getFirstSelectedOption();
			logger.info("Data Name selected: " + selectedDataName.getText());
			assertEquals("Subject", selectedDataName.getAttribute("value"));
			
			// Cancel changes and go back to previous page
			WebElement cancel = driver.findElement(By.cssSelector("input[title='Cancel Changes']"));
			assertNotNull(cancel);
			cancel.click();
			
			// submit change
			WebElement submit = driver.findElement(By.id("ruleedit:content:submit"));
			assertNotNull(submit);
			submit.click();
			
			// dismiss (Click Cancel) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.dismiss(); // cancel the alert to prevent changes to the database
				logger.info("Cancelled the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running from Maven test
				logger.error(e.getMessage());
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

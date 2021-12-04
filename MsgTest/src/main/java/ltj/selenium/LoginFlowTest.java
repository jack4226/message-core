package ltj.selenium;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	static final Logger logger = LogManager.getLogger(LoginFlowTest.class);

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

			// element_pswd.submit();
			driver.findElement(By.id("login:submit")).click();

			logger.info("Page title is: " + driver.getTitle());

			logger.info("Page URL is: " + driver.getCurrentUrl());
			logger.info("Page source is: " + driver.getPageSource());

			// wait until page is loaded
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Main Page"));
		} catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testListAndViewAction() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Custom Bounce Rules and Actions";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();

			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions
					.presenceOfElementLocated(By.cssSelector("a[title='XHeader_SpamScore_viewActions']")));

			logger.info("Switched to URL: " + driver.getCurrentUrl());

			// Retrieve field values from the list
			WebElement ruleNameElm = driver.findElement(By.cssSelector("a[title='XHeader_SpamScore_viewDetail']"));
			String ruleNameBefore = ruleNameElm.getText();

			WebElement ruleTypeElm = driver.findElement(By.cssSelector("span[title='XHeader_SpamScore_ruleType']"));
			String ruleTypeBefore = ruleTypeElm.getText();

			WebElement categoryElm = driver.findElement(By.cssSelector("span[title='XHeader_SpamScore_category']"));
			String categoryBefore = categoryElm.getText();

			WebElement viewActionsLink = driver.findElement(By.cssSelector("a[title='XHeader_SpamScore_viewActions']"));
			viewActionsLink.click();

			logger.info("View Actions page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:action:rulecategory")));

			// View/Edit Actions Page
			WebElement ruleNameEdt = driver.findElement(By.id("custrulelst:action:rulename"));
			assertEquals(ruleNameBefore, ruleNameEdt.getText());

			WebElement ruleTypeEdt = driver.findElement(By.id("custrulelst:action:ruletype"));
			assertEquals(ruleTypeBefore, ruleTypeEdt.getText());

			WebElement categoryEdt = driver.findElement(By.id("custrulelst:action:rulecategory"));
			assertEquals(categoryBefore, categoryEdt.getText());

			WebElement seqEdt = driver.findElement(By.id("custrulelst:action:jsftable:0:actionseq"));
			assertEquals("1", seqEdt.getAttribute("value"));

			List<WebElement> actSeqList = driver.findElements(By.cssSelector("input[id$=':actionseq']"));
			assertFalse(actSeqList.isEmpty());

			List<WebElement> actIdList = driver.findElements(By.cssSelector("select[id$=':actionid']"));
			assertEquals(actSeqList.size(), actIdList.size());
			for (int i = 0; i < actIdList.size(); i++) {
				Select selectActId = new Select(actIdList.get(i));
				WebElement selectedActId = selectActId.getFirstSelectedOption();
				if (i == 0) {
					assertEquals("SAVE", selectedActId.getAttribute("value"));
				} else if (i == 1) {
					assertEquals("CLOSE", selectedActId.getAttribute("value"));
				}
			}

			List<WebElement> statIdList = driver.findElements(By.cssSelector("select[id$=':statusid']"));
			assertEquals(actSeqList.size(), statIdList.size());
			for (WebElement elm : statIdList) {
				Select selectStatId = new Select(elm);
				WebElement selectedStatId = selectStatId.getFirstSelectedOption();
				assertEquals("A", selectedStatId.getAttribute("value"));
			}

			// Go back to list
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Go Back']"));

			AlertUtil.waitLongIgnoreTimeout(driver, By.id("custrulelst:footer:gettingStartedFooter"));
		} catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	/*
	 * Configure Site Profiles Configure POP/IMAP Accounts Configure SMTP Servers
	 * 
	 * Customize Actions for Built-in Rules Setup Custom Bounce Rules and Actions
	 * Customize Action Details
	 * 
	 * Setup Email Mailing Lists Upload Email Addresses to List Setup Email
	 * Variables Setup Email Templates
	 * 
	 * Manage Email Correspondence Broadcast to a Mailing List Manage Email
	 * Addresses Manage Customer Information View Broadcast Messages
	 * 
	 * Manage Site Users Change Password
	 */

	public static void main(String[] args) {
		OSUtil.setupSeleniumBrowserDriver();

		WebDriver driver = new ChromeDriver();
		// WebDriver driver = new FirefoxDriver();

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
			// element_pswd.submit();
			driver.findElement(By.id("login:submit")).click();

			// Check the title of the page
			logger.info("Page title is: " + driver.getTitle());
			logger.info("Page URL is: " + driver.getCurrentUrl());
			logger.info("Page source is: " + driver.getPageSource());

			// wait until page is loaded
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs("Main Page"));

			logger.info("Current URL: " + driver.getCurrentUrl());
		} catch (Exception e) {
			logger.error("Exception caught", e);
		} finally {
			// Close the browser
			driver.quit();
		}
	}

	/*
	 * Start the ChromeDriver server separately before running tests: Terminal: $
	 * ./chromedriver Started ChromeDriver port=9515 version=14.0.836.0
	 * 
	 * and connect to it using the Remote WebDriver: WebDriver driver = new
	 * RemoteWebDriver("http://127.0.0.1:9515", DesiredCapabilities.chrome());
	 * driver.get("http://www.google.com");
	 */
}

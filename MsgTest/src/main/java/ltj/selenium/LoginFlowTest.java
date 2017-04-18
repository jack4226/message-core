package ltj.selenium;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
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
	public void testBroadcastToList() {
		String listTitle = "Broadcast to a Mailing List";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mlstcomp:gettingStartedFooter")));
			
			Select selectTmpltId = new Select(driver.findElement(By.id("mlstcomp:template")));
			selectTmpltId.selectByValue("SampleNewsletter2");
			
			WebElement loadTmpltLink = driver.findElement(By.cssSelector("input[title='Copy from Template']"));
			loadTmpltLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mlstcomp:gettingStartedFooter")));
			
			// verify fields are populated
			// Mailing List Id
			Select selectListId = new Select(driver.findElement(By.id("mlstcomp:listid")));
			WebElement selectedListId = selectListId.getFirstSelectedOption();
			assertEquals("SMPLLST2", selectedListId.getAttribute("value"));
			
			// Message Subject
			WebElement subjectElm = driver.findElement(By.id("mlstcomp:subject"));
			String subject = subjectElm.getAttribute("value");
			assertTrue(StringUtils.contains(subject, "Sample HTML newsletter"));
			
			// Message body text
			WebElement bodyTextElm = driver.findElement(By.id("mlstcomp:bodytext"));
			String bodyText = bodyTextElm.getAttribute("value");
			assertTrue(StringUtils.contains(bodyText, "This is a sample HTML newsletter message for a traditional mailing list."));
			// TODO  fix this - "element not visible" exception
			try {
				bodyTextElm.sendKeys(Keys.CONTROL, Keys.END, Keys.ENTER);
				bodyTextElm.sendKeys("Current Date Time: ");
				fail(); // TODO remove after it's fixed
			}
			catch (ElementNotVisibleException e) {
				logger.error("ElementNotVisibleException caught: " + e.getMessage());
			}
			
			// Variable Name
			Select selectVarNm = new Select(driver.findElement(By.id("mlstcomp:vname")));
			assertEquals(1, selectVarNm.getAllSelectedOptions().size());
			selectVarNm.selectByValue("CurrentDateTime");
			
			// Insert a variable
			WebElement insertVarEm = driver.findElement(By.id("insert_variable"));
			insertVarEm.click();
			
			bodyTextElm = driver.findElement(By.id("mlstcomp:bodytext"));
			bodyText = bodyTextElm.getAttribute("value");
			// TODO does not work, fix "insert variable"
			//assertTrue(StringUtils.contains(bodyText, "${CurrentDateTime}"));
			
			// Embed EmailId
			Select selectEmbed = new Select(driver.findElement(By.id("mlstcomp:embed_email_id")));
			WebElement selectedEmbed = selectEmbed.getFirstSelectedOption();
			assertEquals("System default", selectedEmbed.getText());
			
			// Delivery Option
			Select selectDlvrOpt = new Select(driver.findElement(By.id("mlstcomp:dlvropt")));
			WebElement selectedDlvrOpt = selectDlvrOpt.getFirstSelectedOption();
			assertEquals("ALL", selectedDlvrOpt.getAttribute("value"));
			
			WebElement isHtmlElm = driver.findElement(By.id("mlstcomp:is_html"));
			assertEquals(true, isHtmlElm.isSelected());
			
			// preview the message
			WebElement previewLink = driver.findElement(By.cssSelector("input[title='Preview Rendered Message']"));
			previewLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("preview:gettingStartedFooter")));
			
			WebElement renderedSubjElm = driver.findElement(By.cssSelector("span[title='Rendered Subject']"));
			String renderedSubj = renderedSubjElm.getText();
			assertTrue(StringUtils.contains(renderedSubj, "Sample HTML newsletter"));
			
			WebElement renderedBodyElm = driver.findElement(By.cssSelector("span[title='Rendered Body']"));
			String renderedBody = renderedBodyElm.getText();
			logger.info("Rendered body text: " + renderedBody);
			assertTrue(StringUtils.contains(renderedBody, "This is a sample HTML newsletter message for a traditional mailing list."));
			if (StringUtils.contains(bodyText, "${CurrentDate}")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				assertTrue(StringUtils.contains(renderedBody, sdf.format(new java.util.Date())));
			}
			if (StringUtils.contains(bodyText, "${MailingListId}")) {
				assertTrue(StringUtils.contains(renderedBody, "SMPLLST2"));
			}
			if (StringUtils.contains(bodyText, "${FooterWithUnsubAddr}")) {
				assertTrue(StringUtils.contains(renderedBody, "To unsubscribe from this mailing list, send an e-mail to: demolist2@localhost"));
			}
			
			// Go back to compose page
			WebElement goBackToCompose = driver.findElement(By.cssSelector("input[title='Go Back']"));
			goBackToCompose.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mlstcomp:gettingStartedFooter")));
			
			// Send message link
//			WebElement sendMsgLink = driver.findElement(By.cssSelector("input[title='Send message']"));
//			sendMsgLink.click();
//			
//			wait.until(ExpectedConditions.titleIs("Main Page"));
			
			// Go back link
			WebElement gobackLink = driver.findElement(By.cssSelector("input[title='Cancel']"));
			gobackLink.click();
			
			wait.until(ExpectedConditions.titleIs("Main Page"));
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

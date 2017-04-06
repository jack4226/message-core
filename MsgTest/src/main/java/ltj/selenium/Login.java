package ltj.selenium;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Login {
	static final Logger logger = Logger.getLogger(Login.class);

	public static void main(String[] args) {
		OSUtil.setupSeleniumBrowserDriver();
		
		// Create a new instance of the Chrome driver
		// Notice that the remainder of the code relies on the interface,
		// not the implementation.
		WebDriver driver = new ChromeDriver();
		//WebDriver driver = new FirefoxDriver();
		
		try {
		// And now use this to visit login page
		driver.get("http://www.google.com"); //"http://128.7.0.1:8080/MsgUI1/login.faces");
		// Alternatively the same thing can be done like this
		// driver.navigate().to("http://www.google.com");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}

//		// Find the text input element by its name
//		WebElement element_user = driver.findElement(By.name("login:userid"));
//
//		// Enter something to search for
//		element_user.sendKeys("admin");
//
//		// Find the text input element by its name
//		WebElement element_pswd = driver.findElement(By.name("login:password"));
//
//		// Enter something to search for
//		element_pswd.sendKeys("admin");
//
//
//		// Now submit the form. WebDriver will find the form for us from the
//		// element
//		element_user.submit();
//
//		// Check the title of the page
//		System.out.println("Page title is: " + driver.getTitle());
//
//		// Google's search is rendered dynamically with JavaScript.
//		// Wait for the page to load, timeout after 10 seconds
//		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
//			public Boolean apply(WebDriver d) {
//				return d.getTitle().toLowerCase().startsWith("Main Page");
//			}
//		});
//
//		// Should see: "cheese! - Google Search"
//		System.out.println("Page title is: " + driver.getTitle());

		try {
		// Close the browser
		driver.quit();
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}

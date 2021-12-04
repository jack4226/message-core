package ltj.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Google {
	static final Logger logger = LogManager.getLogger(Google.class);

	public static void main(String[] args) {
		OSUtil.setupSeleniumBrowserDriver();
		
		// Create a new instance of the Chrome driver
		// Notice that the remainder of the code relies on the interface,
		// not the implementation.
		WebDriver driver = new ChromeDriver();
		//WebDriver driver = new FirefoxDriver();
		
		try {
			// And now use this to visit login page
			driver.get("http://www.google.com/xhtml");
			// Alternatively the same thing can be done like this
			// driver.navigate().to("http://www.google.com");
			Thread.sleep(5000);  // Let the user actually see something!
			
			WebElement searchBox = driver.findElement(By.name("q"));
			searchBox.sendKeys("ChromeDriver");
			searchBox.submit();
			Thread.sleep(5000);  // Let the user actually see something!
			// Close the browser
			driver.quit();
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}

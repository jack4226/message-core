package ltj.selenium;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MainPageTest extends BaseLogin {
	static final Logger logger = Logger.getLogger(MainPageTest.class);

	@Test
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
			(new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return d.getTitle().equals("Configure Site Profiles");
				}
			});
	        logger.info("Next URL: " + driver.getCurrentUrl());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
}

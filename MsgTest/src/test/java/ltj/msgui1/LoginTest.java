package ltj.msgui1;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class LoginTest {
	static final Logger logger = Logger.getLogger(LoginTest.class);

	private HtmlUnitDriver driver;

	@Before
	public void setup() {
	    driver = new HtmlUnitDriver(true);
	    driver.get("http://localhost:8080/MsgUI1/login.faces");
	}

	@Test
	public void testFailure(){
	    driver.findElement(By.id("login:userid")).sendKeys("fakeuser");
	    driver.findElement(By.id("login:password")).sendKeys("fakepassword");     
	    driver.findElement(By.id("login:submit")).click();

	    logger.info("URL failed: " + driver.getCurrentUrl());
	    assertTrue(driver.getCurrentUrl().contains("login.faces"));
	}

	@Test
	public void testSuccess(){
	    driver.findElement(By.id("login:userid")).sendKeys("admin");
	    driver.findElement(By.id("login:password")).sendKeys("admin");
	    driver.findElement(By.id("login:submit")).click();

	    logger.info("URL success: " + driver.getCurrentUrl());
	    assertTrue(driver.getCurrentUrl().endsWith("main.faces"));
	}

}

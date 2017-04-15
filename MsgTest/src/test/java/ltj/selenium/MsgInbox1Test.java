package ltj.selenium;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MsgInbox1Test extends BaseLogin {
	static final Logger logger = Logger.getLogger(MsgInbox1Test.class);

	static final List<String> FromAddrList = new ArrayList<>();
	static final List<String> RuleNameList = new ArrayList<>();
	static final List<String> SubjectList = new ArrayList<>();
	
	@Test
	public void testEmailBrowser() {
		String listTitle = "Manage Email Correspondence";
		
		logger.info("Current URL: " + driver.getCurrentUrl());
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			assertNotNull(link);
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 5);
			wait.until(ExpectedConditions.titleIs(listTitle));
			
			// Email List page
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			WebElement allMsgLink = driver.findElement(By.cssSelector("a[title='All Messages']"));
			allMsgLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			Select selectRuleName = new Select(driver.findElement(By.cssSelector("select[title='Select Rule Name']")));
			WebElement selectedRuleName = selectRuleName.getFirstSelectedOption();
			assertEquals("All", selectedRuleName.getAttribute("value"));
			
			WebElement checkAll = driver.findElement(By.id("msgform:msgrow:checkAll"));
			checkAll.click();

			WebElement markUnreadLink = driver.findElement(By.cssSelector("a[title='Mark as unread']"));
			markUnreadLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			ListTuple tuple1 = getListDetails();
			
			// View message details
			assertNotNull(tuple1.viewMsgLink);
			tuple1.viewMsgLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inboxview:gettingStartedFooter")));

			// verify that fields from details and list match
			WebElement fromAddrElm = driver.findElement(By.id("inboxview:from_address"));
			assertTrue(StringUtils.contains(fromAddrElm.getText(), tuple1.fromAddrList.get(tuple1.idx)));
			
			WebElement ruleNameElm = driver.findElement(By.id("inboxview:rule_name"));
			assertEquals(ruleNameElm.getText(), tuple1.ruleNameList.get(tuple1.idx));
			
			WebElement subjectElm = driver.findElement(By.id("inboxview:msg_subject"));
			assertTrue(StringUtils.contains(subjectElm.getText(), tuple1.subjectList.get(tuple1.idx)));
			
			WebElement bodyElm = driver.findElement(By.id("inboxview:body_content_msg"));
			String bodyText = bodyElm.getText();
			assertTrue(StringUtils.isNotBlank(bodyText));
			if (StringUtils.contains(bodyText, "System Email Id:")) {
				logger.info("Embeded Email Id Found.");
			}
			
			Select selectNewRM = new Select(driver.findElement(By.id("inboxview:newrulename")));
			WebElement selectednewRM = selectNewRM.getFirstSelectedOption();
			assertEquals(ruleNameElm.getText(), selectednewRM.getAttribute("value"));
			
			// Go back to the list
			WebElement goback = driver.findElement(By.cssSelector("input[title='Go back to List']"));
			goback.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// click page next/previous
			try {
				WebElement pageNext = driver.findElement(By.id("msgform:msgrow:pagenext"));
				pageNext.click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
				ListTuple next = getListDetails();
				assertFalse(tuple1.equalsTo(next));
				WebElement pagePrev = driver.findElement(By.id("msgform:msgrow:pageprev"));
				pagePrev.click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				fail();
			}

			// click page last/first
			try {
				WebElement pageLast = driver.findElement(By.id("msgform:msgrow:pagelast"));
				pageLast.click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
				// TODO fix this
				//ListTuple last = getListDetails();
				//assertFalse(tuple1.equalsTo(last));
				WebElement pageFirst = driver.findElement(By.id("msgform:msgrow:pagefrst"));
				pageFirst.click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				fail();
			}

			// Search by Rule Name
			Select srchRuleName = new Select(driver.findElement(By.id("msgform:by_rulename")));
			WebElement selectedArchRM = srchRuleName.getFirstSelectedOption();
			assertEquals("All", selectedArchRM.getAttribute("value"));
			srchRuleName.selectByValue("GENERIC");
			
			// click Search button
			WebElement searchBtn = driver.findElement(By.cssSelector("input[title='Submit search request']"));
			searchBtn.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// verify results
			List<WebElement> ruleNames = driver.findElements(By.cssSelector("input[title$='_ruleName']"));
			for (WebElement elm : ruleNames) {
				assertEquals("GENERIC", elm.getText());
			}
			
			// reset search
			WebElement resetBtn = driver.findElement(By.cssSelector("input[title='Reset search fields']"));
			resetBtn.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// verify the list page hasn't changed
			ListTuple tuple2 = getListDetails();
			assertTrue(tuple1.equalsTo(tuple2));
			
			// search by from address and subject
			int idx = new Random().nextInt(SubjectList.size());
			WebElement srchSubject = driver.findElement(By.id("msgform:subject"));
			srchSubject.clear();
			String subjSrchWord = StringUtil.getRandomWord(SubjectList.get(idx));
			srchSubject.sendKeys(subjSrchWord);
			
			WebElement srchFromAddr = driver.findElement(By.id("msgform:fromaddr"));
			srchFromAddr.clear();
			String fromAddrWord = FromAddrList.get(idx);
			if (fromAddrWord.indexOf("@") > 0) {
				fromAddrWord = StringUtil.getDisplayName(fromAddrWord);
			}
			else {
				fromAddrWord = StringUtil.getRandomWord(fromAddrWord);
			}
			if (StringUtils.isBlank(fromAddrWord)) {
				fromAddrWord = FromAddrList.get(idx);
			}
			srchFromAddr.sendKeys(fromAddrWord);
			
			// click search button
			searchBtn = driver.findElement(By.cssSelector("input[title='Submit search request']"));
			searchBtn.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
			
			// verify search results
			List<WebElement> fromAddrs = driver.findElements(By.cssSelector("input[title$='_dispName']"));
			for (WebElement elm : fromAddrs) {
				logger.info("From Address - does \"" + elm.getText() + "\" contains \"" + fromAddrWord + "\"?");
				assertTrue(StringUtils.containsIgnoreCase(elm.getText(), fromAddrWord));
			}
			List<WebElement> subjects = driver.findElements(By.cssSelector("a[title$='_viewMessage']"));
			for (WebElement elm : subjects) {
				logger.info("Subject - does \"" + elm.getText() + "\" contains \"" + subjSrchWord + "\"?");
				assertTrue(StringUtils.containsIgnoreCase(elm.getText(), subjSrchWord));
			}
			
			// reset search
			resetBtn = driver.findElement(By.cssSelector("input[title='Reset search fields']"));
			resetBtn.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("msgform:gettingStartedFooter")));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	private static class ListTuple {
		List<String> fromAddrList;
		List<String> subjectList;
		List<String> ruleNameList;
		WebElement viewMsgLink;
		int idx;
		
		boolean equalsTo(ListTuple other) {
			if (fromAddrList != null) {
				if (other.fromAddrList == null) {
					return false;
				}
			}
			if (subjectList != null) {
				if (other.subjectList == null) {
					return false;
				}
			}
			if (ruleNameList != null) {
				if (other.ruleNameList == null) {
					return false;
				}
			}
			return (fromAddrList.equals(other.fromAddrList) && subjectList.equals(other.subjectList) && ruleNameList.equals(other.ruleNameList));
		}
	}
	
	ListTuple getListDetails() {
		List<WebElement> checkBoxList = driver.findElements(By.cssSelector("input[title$='_checkBox']"));
		assertFalse(checkBoxList.isEmpty());
		
		ListTuple tuple = new ListTuple();
		
		tuple.fromAddrList = new ArrayList<>();
		tuple.subjectList = new ArrayList<>();
		tuple.ruleNameList = new ArrayList<>();
		
		tuple.idx = new Random().nextInt(checkBoxList.size());

		for (int i = 0; i < checkBoxList.size(); i++) {
			WebElement elm = checkBoxList.get(i);
			String checkBoxTitle = elm.getAttribute("title");
			String prefix = StringUtils.removeEnd(checkBoxTitle, "_checkBox");
			
			WebElement dispNameElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_dispName']"));
			String fromAddr = dispNameElm.getText();
			assertTrue(StringUtils.isNotBlank(fromAddr));
			tuple.fromAddrList.add(fromAddr);
			
			WebElement ruleNameElm = driver.findElement(By.cssSelector("span[title='" + prefix + "_ruleName']"));
			String ruleName = ruleNameElm.getText();
			assertTrue(StringUtils.isNotBlank(ruleName));
			tuple.ruleNameList.add(ruleName);
			
			WebElement viewMsgLink = driver.findElement(By.cssSelector("a[title='" + prefix + "_viewMessage']"));
			String subject = viewMsgLink.getText();
			tuple.subjectList.add(subject);
			
			if (i == tuple.idx) {
				tuple.viewMsgLink = viewMsgLink;
			}
		}
		
		if (FromAddrList.isEmpty()) {
			FromAddrList.addAll(tuple.fromAddrList);
		}
		if (RuleNameList.isEmpty()) {
			RuleNameList.addAll(tuple.ruleNameList);
		}
		if (SubjectList.isEmpty()) {
			SubjectList.addAll(tuple.subjectList);
		}
		assertNotNull(tuple.viewMsgLink);
		
		return tuple;
	}
	
}

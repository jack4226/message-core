package ltj.msgui2;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CustomRuleTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(CustomRuleTest.class);


	@Test
	public void testAddNewAction() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Custom Bounce Rules and Actions";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Locate view/edit detail link
			WebElement viewActionsLink = driver.findElement(By.cssSelector("a[title='XHeader_SpamScore_viewActions']"));
			viewActionsLink.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionedit:footer:gettingStartedFooter")));
			
			// View/Edit Detail Page
			List<WebElement> actSeqList = driver.findElements(By.cssSelector("input[id$=':actionseq']"));
			assertFalse(actSeqList.isEmpty());
			int listSize = actSeqList.size();
			
			List<WebElement> actIdList = driver.findElements(By.cssSelector("select[id$=':actionid']"));
			assertEquals(actSeqList.size(), actIdList.size());

			// Click on Add New button
			WebElement addNewLink = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			addNewLink.click();
			
			String prefix = "actionedit:content:jsftable:" + listSize + ":";
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id(prefix + "statusid")));
			
			// make changes to the new record
			Select selectActId = new Select(driver.findElement(By.id(prefix + "actionid")));
			selectActId.selectByValue("BOUNCE_UP");
			
			Select selectValues = new Select(driver.findElement(By.id(prefix + "datatypevalues1")));
			selectValues.selectByValue("$OrigRcpt");
			selectValues.selectByValue("$FinalRcpt");
			assertEquals(2, selectValues.getAllSelectedOptions().size());
			
			// Submit the changes
			WebElement submit = driver.findElement(By.cssSelector("input[title='Submit changes']"));
			submit.click();
			
			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionedit:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id(prefix + "datatypevalues1")));
			
			// Delete added record
			// Tick check box of the record
			WebElement checkBoxLink = driver.findElement(By.id(prefix + "checkbox"));
			checkBoxLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionedit:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id(prefix + "checkbox"), true));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			
			// Delete the record
			WebElement delete = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			delete.click();

			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}

			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("select[id='" + prefix + "']"))));
			
			// Go back to list
			WebElement goback = driver.findElement(By.cssSelector("input[title='Go Back']"));
			goback.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionedit:footer:gettingStartedFooter")));
			
			// View/Edit Actions Page
			WebElement ruleNameEdt = driver.findElement(By.id("actionedit:content:rulename"));
			assertEquals(ruleNameBefore, ruleNameEdt.getText());
			
			WebElement ruleTypeEdt = driver.findElement(By.id("actionedit:content:ruletype"));
			assertEquals(ruleTypeBefore, ruleTypeEdt.getText());
			
			WebElement categoryEdt = driver.findElement(By.id("actionedit:content:rulecategory"));
			assertEquals(categoryBefore, categoryEdt.getText());
			
			WebElement seqEdt = driver.findElement(By.id("actionedit:content:jsftable:0:actionseq"));
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
				}
				else if (i == 1) {
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
			WebElement submit = driver.findElement(By.cssSelector("input[title='Go Back']"));
			submit.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":editelement")));
			
			Select selectCriteria = new Select(driver.findElement(By.id("ruleedit:content:data_table:" + nextIdx + ":criteria")));
			selectCriteria.selectByValue("contains");
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":casesensitive")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:content:data_table:" + nextIdx + ":editelement")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[title='Submit Changes']")));
			
			// Submit changes
			WebElement submitChanges = driver.findElement(By.cssSelector("input[title='Submit Changes']"));
			submitChanges.click();
			
			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}

			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[id='ruleedit:content:data_table:" + nextIdx + ":checkbox']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='ruleedit:content:data_table:" + nextIdx + ":checkbox']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			// Delete the added record
			WebElement chkboxLink = driver.findElement(By.id("ruleedit:content:data_table:" + nextIdx + ":checkbox"));
			chkboxLink.click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id("ruleedit:content:data_table:" + nextIdx + ":checkbox"), true));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
			WebElement deleteLink = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			deleteLink.click();
			
			// accept (Click OK) JavaScript Alert pop-up
			try {
				WebDriverWait waitShort = new WebDriverWait(driver, 1);
				Alert alert = (org.openqa.selenium.Alert) waitShort.until(ExpectedConditions.alertIsPresent());
				alert.accept();
				logger.info("Accepted the alert successfully.");
			}
			catch (org.openqa.selenium.TimeoutException e) { // when running HtmlUnitDriver
				logger.error(e.getMessage());
			}
			
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("input[id^='ruleedit:content:data_table:" + nextIdx + ":']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ruleedit:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}

package ltj.msgui2;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import ltj.selenium.AlertUtil;

public class CustomRuleTest extends AbstractLogin {
	static final Logger logger = Logger.getLogger(CustomRuleTest.class);

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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='XHeader_SpamScore_viewActions']")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Retrieve field values from the list
			WebElement ruleNameElm = driver.findElement(By.cssSelector("a[title='XHeader_SpamScore_viewDetail']"));
			String ruleNameBefore = ruleNameElm.getText();
			
			WebElement ruleTypeElm = driver.findElement(By.cssSelector("span[title='XHeader_SpamScore_ruleType']"));
			String ruleTypeBefore = ruleTypeElm.getText();
			
			WebElement categoryElm = driver.findElement(By.cssSelector("span[title='XHeader_SpamScore_category']"));
			String categoryBefore = categoryElm.getText();
			
			// View/Edit Actions Page
			WebElement viewActionsLink = driver.findElement(By.cssSelector("a[title='XHeader_SpamScore_viewActions']"));
			viewActionsLink.click();
			
			logger.info("View Actions page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:action:rulecategory")));
			
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
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Go Back']"));

			AlertUtil.waitLongIgnoreTimeout(driver, By.id("custrulelst:footer:gettingStartedFooter"));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

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
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='XHeader_SpamScore_viewActions']")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// Locate view/edit detail link
			WebElement viewActionsLink = driver.findElement(By.cssSelector("a[title='XHeader_SpamScore_viewActions']"));
			viewActionsLink.click();
			
			logger.info("Edit page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("select[id$=':actionid']")));
			
			// View/Edit Detail Page
			List<WebElement> actSeqList = driver.findElements(By.cssSelector("input[id$=':actionseq']"));
			assertFalse(actSeqList.isEmpty());
			int listSize = actSeqList.size();
			
			List<WebElement> actIdList = driver.findElements(By.cssSelector("select[id$=':actionid']"));
			assertEquals(actSeqList.size(), actIdList.size());

			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Add a new row']")));
			// Click on Add New button
			WebElement addNewLink = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			addNewLink.click();
			
			String prefix = "custrulelst:action:jsftable:" + listSize + ":";
			
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
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id(prefix + "datatypevalues1")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id(prefix + "checkbox")));
			
			// Delete added record
			// 1) Tick check box of the record
			AlertUtil.clickCommandLink(driver, By.id(prefix + "checkbox"));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(prefix + "checkbox")));
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id(prefix + "checkbox"), true));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));

			// 2) Delete the record
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Delete selected rows']"));

			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("select[id='" + prefix + "']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Go Back']")));
			// Go back to list
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Go Back']"));
			AlertUtil.waitLongIgnoreTimeout(driver, By.id("custrulelst:footer:gettingStartedFooter"));
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
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='HardBouce_WatchedMailbox_viewDetail']")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// View/Edit Details page
			WebElement viewDetailLink = driver.findElement(By.cssSelector("a[title='HardBouce_WatchedMailbox_viewDetail']"));
			viewDetailLink.click();

			logger.info("View Detail page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:rulecategory")));
			
			WebElement ruleDescEdt = driver.findElement(By.id("custrulelst:detail:desc"));
			String desc = ruleDescEdt.getAttribute("value");
			if (StringUtils.endsWith(desc, "_updated")) {
				desc = StringUtils.removeEnd(desc, "_updated");
			}
			else {
				desc += "_updated";
			}
			ruleDescEdt.sendKeys(desc);
			
			Select selectCategory = new Select(driver.findElement(By.id("custrulelst:detail:rulecategory")));
			WebElement selectedCtgy = selectCategory.getFirstSelectedOption();
			assertEquals("P", selectedCtgy.getAttribute("value"));
			
			Select selectSubrule = new Select(driver.findElement(By.id("custrulelst:detail:subrule")));
			WebElement selectedSubrule = selectSubrule.getFirstSelectedOption();
			assertEquals("false", selectedSubrule.getAttribute("value"));
			
			List<WebElement> ruleTypeList = driver.findElements(By.cssSelector("input[id^='custrulelst:detail:ruletype:']"));
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
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":dataname")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			Select selectDataName = new Select(driver.findElement(By.id("custrulelst:detail:data_table:" + nextIdx + ":dataname")));
			selectDataName.selectByValue("From");
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":dataname")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":criteria")));
			wait.until(ExpectedConditions.elementToBeClickable(By.id("custrulelst:detail:data_table:" + nextIdx + ":criteria")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":editelement")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Submit Changes']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			Select selectCriteria = new Select(driver.findElement(By.id("custrulelst:detail:data_table:" + nextIdx + ":criteria")));
			selectCriteria.selectByValue("contains");
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":casesensitive")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":editelement")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[title='Submit Changes']")));
			
			// Submit changes
			WebElement submitChanges = driver.findElement(By.cssSelector("input[title='Submit Changes']"));
			submitChanges.click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Refresh from database']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Refresh from database']")));
			
			// Refresh from database
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Refresh from database']"));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox")));
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox"), false));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			// Delete the added record
			AlertUtil.clickCommandLink(driver, By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox"));
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox"), true));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Create a new row from selected']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Delete selected rows']"));
			
			AlertUtil.handleAlert(driver);
			
			// Refresh from database
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Refresh from database']")));
			
			AlertUtil.clickCommandLink(driver, By.cssSelector("input[title='Refresh from database']"));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("input[id^='custrulelst:detail:data_table:" + nextIdx + ":']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	@Test
	public void testViewDetailsAndElement() {
		logger.info("Current URL: " + driver.getCurrentUrl());
		String listTitle = "Setup Custom Bounce Rules and Actions";
		try {
			WebElement link = driver.findElement(By.linkText(listTitle));
			link.click();
			
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.titleIs(listTitle));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='HardBouce_WatchedMailbox_viewDetail']")));
			
			logger.info("Switched to URL: " + driver.getCurrentUrl());
			
			// View/Edit Details page
			WebElement viewDetailLink = driver.findElement(By.cssSelector("a[title='HardBouce_WatchedMailbox_viewDetail']"));
			viewDetailLink.click();

			logger.info("View Detail page URL: " + driver.getCurrentUrl());
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("input[id$=':checkbox']")));
			
			List<WebElement> chkboxList = driver.findElements(By.cssSelector("input[id$=':checkbox']"));
			assertTrue(chkboxList.size() >= 2);
			int nextIdx = chkboxList.size();
			
			// Add a new record
			WebElement addNewLink = driver.findElement(By.cssSelector("input[title='Add a new row']"));
			addNewLink.click();

			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			Select selectDataName = new Select(driver.findElement(By.id("custrulelst:detail:data_table:" + nextIdx + ":dataname")));
			selectDataName.selectByValue("X-Header"); // triggers ajax event
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":headername")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":criteria")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":editelement")));
			
			WebElement hdrNameElm = driver.findElement(By.id("custrulelst:detail:data_table:" + nextIdx + ":headername"));
			
			Actions builder = new Actions(driver);
			builder.moveToElement(hdrNameElm).build().perform();
			
			hdrNameElm.sendKeys("X_Rule_Test");
			
			// to "blur" the header name field and trigger the ajax event
			driver.findElement(By.cssSelector("input[title='" + nextIdx + "_input_targetText']")).click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":criteria")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='" + nextIdx + "_criteria']")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[title='" + nextIdx + "_input_targetText']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='" + nextIdx + "_editElement']")));
			
			WebElement criteriaElm = driver.findElement(By.cssSelector("select[title='" + nextIdx + "_criteria']"));
			builder.moveToElement(criteriaElm).build().perform(); // move cursor to "select criteria"
			
			Select selectCriteria = new Select(criteriaElm);
			List<WebElement> optionList = selectCriteria.getOptions();
			List<String> optionValues = new ArrayList<>();
			for (WebElement option : optionList) {
				optionValues.add(option.getAttribute("value"));
			}
			assertTrue(optionValues.contains("contains"));
			selectCriteria.selectByValue("contains"); // triggers ajax event
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":headername")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("select[title='" + nextIdx + "_criteria']")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[title='" + nextIdx + "_input_targetText']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[title='" + nextIdx + "_editElement']")));
		
			List<WebElement> targetList = driver.findElements(By.cssSelector("input[title$='_input_targetText']"));
			assertTrue(targetList.size() > 0);
			WebElement targetElm = targetList.get(targetList.size() - 1);
			targetElm = driver.findElement(By.cssSelector("input[title='" + nextIdx + "_input_targetText']"));
			
			try {
				builder.moveToElement(targetElm).build().perform(); // move cursor to "target text"
				targetElm.sendKeys("Test add new element");
			}
			catch (Exception e) {
				logger.error("Failed to send target text: " + e.getMessage());
				Thread.sleep(1000);
				targetElm = driver.findElement(By.cssSelector("input[title='" + nextIdx + "_input_targetText']"));
				targetElm.sendKeys("Test add new element");
			}
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[title='Submit Changes']")));
			
			// Submit changes
			WebElement submitChanges = driver.findElement(By.cssSelector("input[title='Submit Changes']"));
			submitChanges.click();
			
			AlertUtil.handleAlert(driver);

			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[id='custrulelst:detail:data_table:" + nextIdx + ":checkbox']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[id='custrulelst:detail:data_table:" + nextIdx + ":checkbox']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			// Enter edit rule element
			AlertUtil.clickCommandLink(driver, By.id("custrulelst:detail:data_table:" + nextIdx + ":editelement"));
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:element:rulename")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:element:delimiter")));
			
			WebElement ruleNameElm = driver.findElement(By.id("custrulelst:element:rulename"));
			assertEquals("HardBouce_WatchedMailbox", ruleNameElm.getAttribute("value"));
			
			Select selectDataNameElm = new Select(driver.findElement(By.id("custrulelst:element:dataname")));
			assertEquals("X-Header", selectDataNameElm.getFirstSelectedOption().getAttribute("value"));
			
			Select selectCriteriaElm = new Select(driver.findElement(By.id("custrulelst:element:criteria")));
			assertNotNull(selectCriteriaElm.getFirstSelectedOption().getText());
			assertEquals("contains", selectCriteriaElm.getFirstSelectedOption().getAttribute("value"));
			
			WebElement targetTextElm = driver.findElement(By.id("custrulelst:element:targettext1"));
			assertEquals("Test add new element", targetTextElm.getAttribute("value"));
			
			WebElement exclusionsElm = driver.findElement(By.id("custrulelst:element:exclusions"));
			exclusionsElm.click();
			exclusionsElm.sendKeys("test exclusions");
			
			driver.findElement(By.id("custrulelst:element:delimiter")).sendKeys(",");
			
			// Submit rule element changes
			WebElement doneEditLink = driver.findElement(By.cssSelector("input[title='Done Edit']"));
			doneEditLink.click();
			
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox")));
			wait.until(ExpectedConditions.elementToBeClickable(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox")));
			
			// Delete the added record
			WebElement chkboxLink = driver.findElement(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox"));
			chkboxLink.click();
			
			wait.until(ExpectedConditions.elementSelectionStateToBe(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox"), true));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("custrulelst:detail:data_table:" + nextIdx + ":checkbox")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[title='Delete selected rows']")));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
			WebElement deleteLink = driver.findElement(By.cssSelector("input[title='Delete selected rows']"));
			deleteLink.click();
			
			AlertUtil.handleAlert(driver);
			
			wait.until(ExpectedConditions.invisibilityOfAllElements(driver.findElements(By.cssSelector("input[id^='custrulelst:detail:data_table:" + nextIdx + ":']"))));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("custrulelst:footer:gettingStartedFooter")));
			
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}

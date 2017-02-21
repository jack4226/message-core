package ltj.message.bo;

import static ltj.message.constant.Constants.DEFAULT_CLIENTID;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bo.template.RenderUtil;
import ltj.message.bo.template.RenderVariable;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.VariableType;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.dao.template.BodyTemplateDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.message.vo.emailaddr.EmailVariableVo;
import ltj.message.vo.emailaddr.TemplateRenderVo;
import ltj.vo.template.BodyTemplateVo;

public class RenderUtilTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(RenderUtilTest.class);
	
	public static final String LF = System.getProperty("line.separator", "\n");
	
	@Resource
	private BodyTemplateDao bodyTemplateDao;
	
	@Resource
	private EmailVariableDao emailVariableDao;
	
	@Resource
	private EmailTemplateDao emailTemplateDao;
	
	@Test
	public void testCheckVariableLoop() {
		String checkText = "Dear ${SubscriberAddress}," + LF + LF + 
		"This is a sample text newsletter message for a traditional mailing list." + LF +
		"With a traditional mailing list, people who want to subscribe to the list " + LF +
		"must send an email from their account to the mailing list address with " + LF +
		"\"subscribe\" in the email subject." + LF + LF + 
		"Unsubscribing from a traditional mailing list is just as easy; simply send " + LF +
		"an email to the mailing list address with \"unsubscribe\" in subject." + LF + LF +
		"Date sent: ${CurrentDate}" + LF + LF +
		"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}" + LF + LF +
		"Contact Email: ${ContactEmailAddress}" + LF + LF +
		"To see our promotions, copy and paste the following link in your browser:" + LF +
		"${WebSiteUrl}/SamplePromoPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}" + LF +
		"${FooterWithUnsubAddr}";
		try {
			RenderUtil.checkVariableLoop(checkText);
		}
		catch (DataValidationException e) {
			fail("Should not have found variable loop!");
		}
		
		String varName = "SubscribeURL";
		EmailVariableVo varVo = emailVariableDao.getByName(varName);
		assertNotNull(varVo);
		logger.info("${" + varName + "} variable: " + varVo.getDefaultValue());
		
		String varValue = "Test variable loop ${" + varName + "} here it comes...";
		RenderVariable var = new RenderVariable(
				"WebSiteUrl",
				varValue,
				null,
				VariableType.TEXT,
				null,
				null,
				null);
		Map<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
		vars.put(var.getVariableName(), var);
		try {
			RenderUtil.checkVariableLoop("loop variable ${" + varName + "}", vars);
			fail("Should have found variable loop!");
		}
		catch (DataValidationException e) {
			// expected
		}
	}
	
	@Test
	public void testRenderTemplate() {
		String tmpltId = "testTemplate";
		
		try {
			BodyTemplateVo bodyVo = bodyTemplateDao.getByBestMatch(tmpltId, DEFAULT_CLIENTID, null);
			if (bodyVo == null) {
				fail("BodyTemplate not found for " + tmpltId);
			}
			else {
				logger.debug("Template Body:" + LF + bodyVo.getTemplateValue());
			}
			
			List<String> variables =  RenderUtil.retrieveVariableNames(bodyVo.getTemplateValue());
			logger.info("Variables: " + variables);
			assertFalse(variables.isEmpty());
			for (String var : variables) {
				assertTrue(StringUtils.contains(bodyVo.getTemplateValue(), "${" + var + "}"));
			}
			
			assertTrue(variables.contains("name1"));
			String name1Value = "this-is-name1-value";
			RenderVariable var = new RenderVariable(
					"name1",
					name1Value,
					null,
					VariableType.TEXT,
					null,
					null,
					null);
			Map<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
			vars.put(var.getVariableName(), var);
			
			String text = RenderUtil.renderTemplateId(tmpltId, null, vars);
			logger.info("testTemplate:" + LF + text);
			assertTrue(StringUtils.contains(text, "BeginTemplate"));
			assertTrue(StringUtils.contains(text, "EndTemplate"));
			assertTrue(StringUtils.contains(text, name1Value));
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testRenderEmailTemplate() {
		String tmpltId = "SampleNewsletter1";
		EmailTemplateVo tmpltVo = emailTemplateDao.getByTemplateId(tmpltId);
		assertNotNull(tmpltVo);
		logger.info("EmailTemplateVo: " + tmpltVo);
		assertTrue(StringUtils.contains(tmpltVo.getBodyText(), "BroadcastMsgId: ${BroadcastMsgId}"));
		Map<String, String> vars = new HashMap<String, String>();
		String bcstMsgId = StringUtils.leftPad("" + new Random().nextInt(1000), 4, '0');
		vars.put("BroadcastMsgId", bcstMsgId);
		try {
			TemplateRenderVo renderVo = RenderUtil.renderEmailTemplate("jsmith@test.com", vars, tmpltId);
			logger.info("TemplateRenderVo:" + renderVo);
			assertTrue(StringUtils.contains(renderVo.getBody(), "BroadcastMsgId: " + bcstMsgId));
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testRenderEmailVariable() {
		String emailVar = "UserProfileURL";
		EmailVariableVo emailVarVo = emailVariableDao.getByName(emailVar);
		logger.info("Email VariableType Vo: " + emailVarVo);
		assertNotNull(emailVarVo);
		assertTrue(StringUtils.contains(emailVarVo.getDefaultValue(), "${SubscriberAddressId}"));
		try {
			String renderedVar = RenderUtil.renderEmailVariable("UserProfileURL", Long.valueOf(101));
			logger.info("Render VariableType: " + renderedVar);
			assertTrue(StringUtils.contains(renderedVar, "sbsrid=101"));
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}
}

package ltj.message.main;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import ltj.message.bo.rule.RuleBase;
import ltj.message.constant.Constants;
import ltj.message.constant.MailingListDeliveryOption;
import ltj.message.constant.MailingListType;
import ltj.message.constant.StatusIdCode;
import ltj.message.constant.VariableName;
import ltj.message.dao.action.MsgActionDao;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.rule.RuleElementDao;
import ltj.message.dao.rule.RuleLogicDao;
import ltj.message.vo.action.MsgActionVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.message.vo.rule.RuleElementVo;
import ltj.message.vo.rule.RuleLogicVo;
import ltj.spring.util.SpringUtil;

public class PremiumUpgradeSetup {
	static final Logger logger = Logger.getLogger(PremiumUpgradeSetup.class);
	static final String LF = System.getProperty("line.separator", "\n");
	final AbstractApplicationContext context;
	private String templateId = Constants.FreePremiumUpgradeTemplateId;
	private String ruleName = Constants.FreePremiumUpgradeRuleName;
	EmailTemplateDao emailTemplateDao;
	String bodyText = "<p>Dear ${_UserName},</p>"
			+ LF + "<p>Thank you for requesting your free premium upgrade of EmailSphere server. We hope you find the software useful. Here is the product key:</p>"
			+ LF + "<p>${_ProductKey}</p>"
			+ LF + "<p>To activate the product key, please login to your Emailsphere system management console, click \"Enter Product Key\", and copy this key to the input field and click \"Submit\".</p>"
			+ LF + "<p>Please don't hesitate to contact us or visiting ${WebSiteUrl} with any questions and suggestions.</p>"
			+ LF + "<p>Sincerely,</p>"
			+ LF + "<p>Emailsphere Team</p>"
			+ LF + "<p>Legacy System Solutions, LLC</p>";
	// 1) insert mailing Template
	// 2) insert bounce rules and actions
	public PremiumUpgradeSetup() {
		context = SpringUtil.getDaoAppContext();
		emailTemplateDao = context.getBean(EmailTemplateDao.class);
	}
	
	void insertEmailTemplate() {
		EmailTemplateVo vo = emailTemplateDao.getByTemplateId(templateId);
		if (vo == null) {
			vo = new EmailTemplateVo();
			vo.setTemplateId(templateId);
			setTemplateData(vo);
			int rows = emailTemplateDao.insert(vo);
			logger.info("Template - Record inserted: " + rows);
		}
		else {
			setTemplateData(vo);
			int rows = emailTemplateDao.update(vo);
			logger.info("Template - Record updated: " + rows);
		}
	}
	
	private void setTemplateData(EmailTemplateVo vo) {
		vo.setIsHtml(true);
		vo.setListId("ORDERLST");
		vo.setEmbedEmailId(Constants.Y);
		vo.setDeliveryOption(MailingListDeliveryOption.ALL_ON_LIST);
		vo.setListType(MailingListType.TRADITIONAL);
		vo.setSubject("Product Key for EmailSphere premium version included");
		vo.setBodyText(bodyText);
	}
	
	void insertRuleLogic() {
		RuleLogicDao dao = (RuleLogicDao) context.getBean("ruleLogicDao");
		List<RuleLogicVo> contact_us = dao.getByRuleName("Contact_Us");
		int ruleSeq = 201;
		if (!contact_us.isEmpty()) {
			ruleSeq = contact_us.get(0).getRuleSeq() - 1;
		}
		List<RuleLogicVo> list = dao.getByRuleName(ruleName);
		RuleLogicVo vo = null;
		if (list.isEmpty()) {
			vo = new RuleLogicVo();
			vo.setRuleName(ruleName);
			vo.setRuleSeq(ruleSeq);
			setRuleData(vo);
			int rows = dao.insert(vo);
			logger.info("RuleLogic - Record inserted: " + rows);
		}
		else if (list.size()>1) {
			throw new RuntimeException("Unexpected results returned from RuleName lookup.");
		}
		else {
			vo = list.get(0);
			setRuleData(vo);
			int rows = dao.update(vo);
			logger.info("RuleLogic - Record updated: " + rows);
		}
	}

	void setRuleData(RuleLogicVo vo) {
		vo.setRuleType(RuleBase.ALL_RULE);
		vo.setStatusId(StatusIdCode.ACTIVE);
		vo.setMailType(Constants.SMTP_MAIL);
		vo.setRuleCategory(RuleBase.MAIN_RULE);
		vo.setIsSubRule(Constants.N);
		vo.setBuiltInRule(Constants.N);
		vo.setDescription("A limited time free Premium upgrade");
	}

	void insertRuleElements() {
		RuleElementDao dao = (RuleElementDao)context.getBean("ruleElementDao");
		int rowsDeleted = dao.deleteByRuleName(ruleName);
		if (rowsDeleted > 0) {
			logger.warn("RuleElement - Record deleted: " + rowsDeleted);
		}
		RuleElementVo vo = new RuleElementVo();
		vo.setRuleName(ruleName);
		vo.setElementSeq(0);
		vo.setDataName(VariableName.SUBJECT);
		vo.setCriteria(RuleBase.EQUALS);
		vo.setCaseSensitive(Constants.N);
		vo.setTargetText("Inquiry About: Free Premium Upgrade");
		int rows = dao.insert(vo);
		vo.setElementSeq(1);
		vo.setDataName(VariableName.MAILBOX_USER);
		vo.setTargetText("support");
		rows += dao.insert(vo);
		logger.info("RuleElement - Record inserted: " + rows);
	}

	void insertMsgActions() {
		MsgActionDao dao = (MsgActionDao) context.getBean("msgActionDao");
		int rowsDeleted = dao.deleteByRuleName(ruleName);
		if (rowsDeleted > 0) {
			logger.warn("MsgAction - Record deleted: " + rowsDeleted);
		}
		MsgActionVo vo = new MsgActionVo();
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		vo.setRuleName(ruleName);
		vo.setActionSeq(1);
		vo.setStatusId(StatusIdCode.ACTIVE);
		vo.setStartTime(startTime);
		vo.setActionId("SAVE");
		int rows = dao.insert(vo);
		vo.setActionSeq(2);
		vo.setActionId("AUTO_REPLY");
		vo.setDataTypeValues(templateId);
		rows += dao.insert(vo);
		logger.info("MsgAction - Record inserted: " + rows);
	}

	public static void main(String[] args) {
		PremiumUpgradeSetup setup = new PremiumUpgradeSetup();
		setup.insertEmailTemplate();
		setup.insertRuleLogic();
		setup.insertRuleElements();
		setup.insertMsgActions();
	}
}

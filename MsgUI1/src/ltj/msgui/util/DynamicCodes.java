package ltj.msgui.util;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

import org.springframework.web.context.WebApplicationContext;

import ltj.message.constant.Constants;
import ltj.message.constant.VariableName;
import ltj.message.dao.action.MsgActionDetailDao;
import ltj.message.dao.action.MsgDataTypeDao;
import ltj.message.dao.client.ClientDao;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.rule.RuleLogicDao;
import ltj.message.dao.template.ClientVariableDao;
import ltj.message.vo.ClientVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.message.vo.emailaddr.EmailVariableVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.rule.RuleLogicVo;
import ltj.vo.template.ClientVariableVo;

@ManagedBean(name="dynacodes")
@ApplicationScoped
public class DynamicCodes {
	static WebApplicationContext webContext = null;
	
	private static RuleLogicDao ruleLogicDao = null;
	public static RuleLogicDao getRuleLogicDao() {
		if (ruleLogicDao == null) {
			ruleLogicDao = (RuleLogicDao) SpringUtil.getWebAppContext().getBean("ruleLogicDao");
		}
		return ruleLogicDao;
	}
	
	private static MsgActionDetailDao msgActionDetailDao = null;
	public static MsgActionDetailDao getMsgActionDetailDao() {
		if (msgActionDetailDao == null) {
			msgActionDetailDao = (MsgActionDetailDao) SpringUtil.getWebAppContext().getBean("msgActionDetailDao");
		}
		return msgActionDetailDao;
	}
	
	private static MsgDataTypeDao msgDataTypeDao = null;
	public static MsgDataTypeDao getMsgDataTypeDao() {
		if (msgDataTypeDao == null) {
			msgDataTypeDao = (MsgDataTypeDao) SpringUtil.getWebAppContext().getBean("msgDataTypeDao");
		}
		return msgDataTypeDao;
	}
	
	private static ClientDao clientDao = null;
	public static ClientDao getClientDao() {
		if (clientDao == null) {
			clientDao = (ClientDao) SpringUtil.getWebAppContext().getBean("clientDao");
		}
		return clientDao;
	}
	
	private static MailingListDao mailingListDao = null;
	public static MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListDao) SpringUtil.getWebAppContext().getBean("mailingListDao");
		}
		return mailingListDao;
	}
	
	private static EmailVariableDao emailVariableDao = null;
	public static EmailVariableDao getEmailVariableDao() {
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableDao) SpringUtil.getWebAppContext().getBean("emailVariableDao");
		}
		return emailVariableDao;
	}
	
	private static EmailTemplateDao emailTemplateDao = null;
	public static EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = (EmailTemplateDao) SpringUtil.getWebAppContext().getBean("emailTemplateDao");
		}
		return emailTemplateDao;
	}
	
	private static ClientVariableDao clientVariableDao = null;
	public static ClientVariableDao getClientVariableDao() {
		if (clientVariableDao == null) {
			clientVariableDao = (ClientVariableDao) SpringUtil.getWebAppContext().getBean("clientVariableDao");
		}
		return clientVariableDao;
	}
	
	// PROPERTY: SubRule Items 
	public SelectItem[] getSubRuleItems() {
		List<RuleLogicVo> list = getRuleLogicDao().getAllSubRules(true);
		SelectItem[] subRules = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			subRules[i] = new SelectItem(list.get(i).getRuleName());
		}
		return subRules;
	}
	
	// PROPERTY: Rule Name Items 
	public SelectItem[] getBuiltinRuleNameItems() {
		List<String> list = getRuleLogicDao().getBuiltinRuleNames4Web();
		SelectItem[] ruleNames = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			ruleNames[i] = new SelectItem(list.get(i));
		}
		return ruleNames;
	}
	
	// PROPERTY: Custom Rule Name Items 
	public SelectItem[] getCustomRuleNameItems() {
		List<String> list = getRuleLogicDao().getCustomRuleNames4Web();
		SelectItem[] ruleNames = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			ruleNames[i] = new SelectItem(list.get(i));
		}
		return ruleNames;
	}
	
	// PROPERTY: ActionId Items 
	public SelectItem[] getActionIdItems() {
		List<String> list = getMsgActionDetailDao().getActionIds();
		//list.add("-- New Action");
		SelectItem[] actionIds = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			actionIds[i] = new SelectItem(list.get(i));
		}
		return actionIds;
	}
	
	// PROPERTY: DataType Items 
	public SelectItem[] getMsgDataTypeItems() {
		List<String> list = getMsgDataTypeDao().getDataTypes();
		SelectItem[] dataTypes = new SelectItem[list.size() + 1];
		dataTypes[0] = new SelectItem(null, "");
		for (int i=0; i<list.size(); i++) {
			dataTypes[i + 1] = new SelectItem(list.get(i), list.get(i));
		}
		return dataTypes;
	}

	// PROPERTY: ClientId Items 
	public SelectItem[] getClientIdItems() {
		List<ClientVo> list = getClientDao().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String clientId = list.get(i).getClientId();
			dataTypes[i] = new SelectItem(clientId, clientId);
		}
		return dataTypes;
	}
	
	// PROPERTY: Mailing List listId Items 
	public SelectItem[] getMailingListIdItems() {
		List<MailingListVo> list = getMailingListDao().getAll(false);
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String listId = list.get(i).getListId();
			String display = listId + " - " + list.get(i).getEmailAddr();
			dataTypes[i] = new SelectItem(listId, display);
		}
		return dataTypes;
	}
	
	// PROPERTY: Email VariableType Name Items 
	public SelectItem[] getEmailVariableNameItems() {
		// 1) custom variables
		List<EmailVariableVo> list = getEmailVariableDao().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		// custom variable names
		for (int i=0; i<list.size(); i++) {
			String variableName = list.get(i).getVariableName();
			dataTypes[i] = new SelectItem(variableName);
		}
		return dataTypes;
	}
	
	// PROPERTY: Global Email VariableType Name Items 
	public SelectItem[] getGlobalVariableNameItems() {
		// 2) Mailing List built-in variables
		VariableName.MailingListVariableName[] listVarNames = VariableName.MailingListVariableName.values();
		// 3) Client built-in variables
		List<ClientVariableVo> clientVars = getClientVariableDao().getCurrentByClientId(Constants.DEFAULT_CLIENTID);
		List<String> clientVarNames = new ArrayList<String>();
		for (ClientVariableVo clientVar : clientVars) {
			if (!"ClientId".equalsIgnoreCase(clientVar.getVariableName())) {
				clientVarNames.add(clientVar.getVariableName());
			}
		}
		SelectItem[] dataTypes = new SelectItem[listVarNames.length + clientVarNames.size()];
		// include mailing list variable names
		for (int i = 0; i < listVarNames.length; i++) {
			String variableName = listVarNames[i].name();
			dataTypes[i] = new SelectItem(variableName);
		}
		// include client variable names
		for (int i = 0; i < clientVarNames.size(); i++) {
			String variableName = clientVarNames.get(i);
			dataTypes[i + listVarNames.length] = new SelectItem(variableName);
		}
		return dataTypes;
	}
	
	// PROPERTY: Email Template Id Items 
	public SelectItem[] getEmailTemplateIdItems() {
		List<EmailTemplateVo> list = getEmailTemplateDao().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String templateId = list.get(i).getTemplateId();
			dataTypes[i] = new SelectItem(templateId);
		}
		return dataTypes;
	}
}

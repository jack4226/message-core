package com.legacytojava.msgui.bean;

import java.util.LinkedList;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

public class GettingStarted {
	static final Logger logger = Logger.getLogger(GettingStarted.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private String titleKey;
	private DataModel functionKeys = null;
	private String functionKey = null;
	private String jspPageLink = null;
	
	/* values must be defined in resource bundle - messages.properties */
	private String[] menuTooltips = { "configureMailboxes", "configureSmtpServers",
			"configureSiteProfiles", "customizeBuiltInRules", "configureCustomRules",
			"maintainActionDetails", "configureMailingLists", "configureEmailVariables",
			"configureEmailTemplates", "manageUserAccounts" };

	private String[] navigationKeys = { "mailbox.list", "smtpserver.list", "siteprofile.list",
			"builtinrule.list", "msgrule.list", "actiondetail.list", "mailinglist.list",
			"emailvariable.list", "emailtemplate.list", "useraccount.list" };

	private String[] functionRequired = { "yes", "yes", "yes", "no", "no", "no", "no", "no", "no",
			"no" };

	/* JSTL import attribute url does not accept any expressions. So this is not used.  */
	private String[] jspPageLinks = { "configureMailboxes.jsp", "configureSmtpServers.jsp",
			"configureSiteProfiles.jsp", "customizeBuiltInRules.jsp", "configureMsgRules.jsp",
			"msgActionDetailList.jsp", "configureMailingLists.jsp", "configureEmailVariables.jsp",
			"configureEmailTemplates.jsp", "manageUserAccounts.jsp" };

	// PROPERTY: titleKey
	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

	public String getTitleKey() {
		return titleKey;
	}

	// PROPERTY: functionKeys
	public DataModel getFunctionKeys() {
		if (functionKeys == null) {
			List<String> functionList = new LinkedList<String>();
			for (int i = 0; i < menuTooltips.length; i++) {
				functionList.add(menuTooltips[i]);
			}
			functionKeys = new ListDataModel();
			functionKeys.setWrappedData(functionList);
		}
		return functionKeys;
	}

	public String selectFunction() {
		this.functionKey = (String) getFunctionKeys().getRowData();
		int i;
		for (i = 0; i < menuTooltips.length; i++) {
			if (menuTooltips[i].equals(this.functionKey)) {
				jspPageLink = jspPageLinks[i];
				break;
			}
		}
		logger.info("selectFunction() - functionKey selected: " + functionKey + ", value: "
				+ navigationKeys[i]);
		return null; // navigationKeys[i];
	}

	public String getFunctionKey() {
		if (functionKey == null)
			functionKey = menuTooltips[0];
		return functionKey;
	}

	public void setFunctionKey(String function) {
		this.functionKey = function;
	}

	public String[] getFunctionRequired() {
		return functionRequired;
	}

	public void setFunctionRequired(String[] functionRequired) {
		this.functionRequired = functionRequired;
	}

	public String getJspPageLink() {
		if (jspPageLink == null)
			jspPageLink = jspPageLinks[0];
		return jspPageLink;
	}

	public void setJspPageLink(String jspPageLink) {
		this.jspPageLink = jspPageLink;
	}
}

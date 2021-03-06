package ltj.message.vo;

import java.io.Serializable;

import ltj.message.constant.Constants;

public class ClientVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -16795349179937720L;
	private String clientId = "";
	private String clientName = "";
	private String clientType = null;
	private String domainName = "";
	private String irsTaxId = null;
	private String webSiteUrl = null;
	private boolean saveRawMsg = true;
	private String contactName = null;
	private String contactPhone = null;
	private String contactEmail = "";
	private String securityEmail = "";
	private String custcareEmail = "";
	private String rmaDeptEmail = "";
	private String spamCntrlEmail = "";
	private String chaRspHndlrEmail = "";
	private boolean embedEmailId = true;
	private String returnPathLeft = "";
	private boolean useTestAddr = false;
	private String testFromAddr = null; 
	private String testToAddr = null;
	private String testReplytoAddr = null;
	private boolean isVerpEnabled = false;
	private String verpSubDomain = null;
	private String verpInboxName = null;
	private String verpRemoveInbox = null;
	private String systemId = "";
	private String systemKey = "";
	private String origClientId = null;
	
	/** define components for UI */
	public boolean getIsSystemClient() {
		return Constants.DEFAULT_CLIENTID.equalsIgnoreCase(clientId);
	}
	/** end of UI components */
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getClientType() {
		return clientType;
	}
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getIrsTaxId() {
		return irsTaxId;
	}
	public void setIrsTaxId(String irsTaxId) {
		this.irsTaxId = irsTaxId;
	}
	public boolean isSaveRawMsg() {
		return saveRawMsg;
	}
	public void setSaveRawMsg(boolean saveRawMsg) {
		this.saveRawMsg = saveRawMsg;
	}
	public String getWebSiteUrl() {
		return webSiteUrl;
	}
	public void setWebSiteUrl(String webSiteUrl) {
		this.webSiteUrl = webSiteUrl;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactPhone() {
		return contactPhone;
	}
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public String getContactEmail() {
		return contactEmail;
	}
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
	public String getSecurityEmail() {
		return securityEmail;
	}
	public void setSecurityEmail(String securityEmail) {
		this.securityEmail = securityEmail;
	}
	public String getCustcareEmail() {
		return custcareEmail;
	}
	public void setCustcareEmail(String custcareEmail) {
		this.custcareEmail = custcareEmail;
	}
	public String getRmaDeptEmail() {
		return rmaDeptEmail;
	}
	public void setRmaDeptEmail(String rmaDeptEmail) {
		this.rmaDeptEmail = rmaDeptEmail;
	}
	public String getSpamCntrlEmail() {
		return spamCntrlEmail;
	}
	public void setSpamCntrlEmail(String spamCntrlEmail) {
		this.spamCntrlEmail = spamCntrlEmail;
	}
	public String getChaRspHndlrEmail() {
		return chaRspHndlrEmail;
	}
	public void setChaRspHndlrEmail(String chaRspHndlrEmail) {
		this.chaRspHndlrEmail = chaRspHndlrEmail;
	}
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	String getSystemKey() {
		return systemKey;
	}
	public void setSystemKey(String systemKey) {
		this.systemKey = systemKey;
	}
	public boolean isEmbedEmailId() {
		return embedEmailId;
	}
	public void setEmbedEmailId(boolean embedEmailId) {
		this.embedEmailId = embedEmailId;
	}
	public boolean isUseTestAddr() {
		return useTestAddr;
	}
	public void setUseTestAddr(boolean useTestAddr) {
		this.useTestAddr = useTestAddr;
	}
	public String getTestFromAddr() {
		return testFromAddr;
	}
	public void setTestFromAddr(String testFromAddr) {
		this.testFromAddr = testFromAddr;
	}
	public String getTestToAddr() {
		return testToAddr;
	}
	public void setTestToAddr(String testToAddr) {
		this.testToAddr = testToAddr;
	}
	public String getTestReplytoAddr() {
		return testReplytoAddr;
	}
	public void setTestReplytoAddr(String testReplytoAddr) {
		this.testReplytoAddr = testReplytoAddr;
	}
	public boolean isIsVerpEnabled() {
		return isVerpEnabled;
	}
	public void setIsVerpEnabled(boolean isVerpEnabled) {
		this.isVerpEnabled = isVerpEnabled;
	}
	public String getVerpSubDomain() {
		return verpSubDomain;
	}
	public void setVerpSubDomain(String verpSubDomain) {
		this.verpSubDomain = verpSubDomain;
	}
	public String getVerpInboxName() {
		return verpInboxName;
	}
	public void setVerpInboxName(String verpInboxName) {
		this.verpInboxName = verpInboxName;
	}
	public String getVerpRemoveInbox() {
		return verpRemoveInbox;
	}
	public void setVerpRemoveInbox(String verpRemoveInbox) {
		this.verpRemoveInbox = verpRemoveInbox;
	}
	public String getOrigClientId() {
		return origClientId;
	}
	public void setOrigClientId(String origClientId) {
		this.origClientId = origClientId;
	}
	public String getReturnPathLeft() {
		return returnPathLeft;
	}
	public void setReturnPathLeft(String returnPathLeft) {
		this.returnPathLeft = returnPathLeft;
	}
}
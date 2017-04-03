package ltj.message.vo;

import java.io.Serializable;

public class SmtpConnVo extends ServerBaseVo implements Serializable {
	private static final long serialVersionUID = -1343713327747984527L;
	private String smtpHost = "";
	private int smtpPort = -1;
	private boolean useSsl = false;
	private Boolean useAuth = null;
	private String userId = ""; 
	private String userPswd = "";
	private boolean persistence = false;
	private String serverType = null;
	private int retries = -1;
	private int retryFreq = -1;
	private Integer alertAfter = null;
	private String alertLevel = null;
	
	public boolean isSsl() {
		return useSsl;
	}
	
	public Integer getAlertAfter() {
		return alertAfter;
	}
	public void setAlertAfter(Integer alertAfter) {
		this.alertAfter = alertAfter;
	}
	public String getAlertLevel() {
		return alertLevel;
	}
	public void setAlertLevel(String alertLevel) {
		this.alertLevel = alertLevel;
	}
	public boolean getPersistence() {
		return persistence;
	}
	public void setPersistence(boolean persistence) {
		this.persistence = persistence;
	}
	public int getRetries() {
		return retries;
	}
	public void setRetries(int retries) {
		this.retries = retries;
	}
	public int getRetryFreq() {
		return retryFreq;
	}
	public void setRetryFreq(int retryFreq) {
		this.retryFreq = retryFreq;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public String getSmtpHost() {
		return smtpHost;
	}
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	public int getSmtpPort() {
		return smtpPort;
	}
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserPswd() {
		return userPswd;
	}
	public void setUserPswd(String userPswd) {
		this.userPswd = userPswd;
	}
	public boolean getUseSsl() {
		return useSsl;
	}
	public void setUseSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}
	public Boolean getUseAuth() {
		return useAuth;
	}
	public void setUseAuth(Boolean useAuth) {
		this.useAuth = useAuth;
	}
}
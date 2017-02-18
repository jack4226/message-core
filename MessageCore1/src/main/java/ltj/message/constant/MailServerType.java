package ltj.message.constant;

public enum MailServerType {
	// define mail server type
	SMTP("smtp"),
	SMTPS("smtps"),
	EXCH("exch");
	
	private String value;
	private MailServerType(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
}

package ltj.message.constant;

public enum MailServerType {
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
	
	// define mail server type
//	public static final String SMTP = "smtp";
//	public static final String SMTPS = "smtps";
//	public static final String EXCH = "exch";

}

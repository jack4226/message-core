package ltj.message.constant;

public enum MailCodeType {
	SMTPMAIL("S"),
	WEBMAIL("W"),
	READONLY("R");

	private String value;
	MailCodeType(String value) {
		this.value=value;
	}
	public String value() {
		return value;
	}
	@Override public String toString() {
		return value();
	}
	public MailCodeType fromValue(String value) {
		for (MailCodeType v : MailCodeType.values()) {
			if (v.value().equalsIgnoreCase(value)) {
				return v;
			}
		}
		return null;
	}
}

package ltj.message.constant;

public enum MailProtocol {

	// define mail protocol
	POP3("pop3"),
	IMAP("imap");

	private final String value;
	private MailProtocol(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
}

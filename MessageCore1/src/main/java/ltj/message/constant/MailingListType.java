package ltj.message.constant;

public enum MailingListType {

	TRADITIONAL("Traditional"),
	PERSONALIZED("Personalized");
	
	private String value;
	private MailingListType(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}

}

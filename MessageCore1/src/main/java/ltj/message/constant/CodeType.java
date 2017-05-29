package ltj.message.constant;

public enum CodeType {
	Y("Y"),
	N("N"),
	MANDATORY("M"),
	Yes("Yes"),
	No("No");
	
	private final String value;
	private CodeType(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
}
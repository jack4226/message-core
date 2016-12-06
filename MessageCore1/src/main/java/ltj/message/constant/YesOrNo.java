package ltj.message.constant;

public enum YesOrNo {
	Yes("Yes"),No("No");

	private final String value;
	private YesOrNo(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
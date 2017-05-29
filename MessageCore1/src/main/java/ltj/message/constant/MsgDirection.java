package ltj.message.constant;

public enum MsgDirection {

	//
	// define message direction id
	//
	RECEIVED("R"),
	SENT("S");

	private final String value;
	private MsgDirection(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
}

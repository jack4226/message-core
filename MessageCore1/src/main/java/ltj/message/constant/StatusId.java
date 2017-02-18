package ltj.message.constant;

public enum StatusId {

	//
	// define general statusId
	//
	ACTIVE("A"),
	INACTIVE("I"),
	SUSPENDED("S"); // for Email Address

	private final String value;
	private StatusId(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
}

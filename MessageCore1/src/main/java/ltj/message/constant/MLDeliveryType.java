package ltj.message.constant;

public enum MLDeliveryType {
	ALL_ON_LIST("ALL"),
	CUSTOMERS_ONLY("CUST"),
	PROSPECTS_ONLY("PROS");

	private String value;
	private MLDeliveryType(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
	// define mailing list delivery options
//	public static final String ALL_ON_LIST = "ALL";
//	public static final String CUSTOMERS_ONLY = "CUST";
//	public static final String PROSPECTS_ONLY = "PROS";

}

package ltj.message.constant;

public enum MLDeliveryType {
	// define mailing list delivery options
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

}

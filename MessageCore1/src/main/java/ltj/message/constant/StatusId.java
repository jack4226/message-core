package ltj.message.constant;

public enum StatusId {

	//
	// define general statusId
	//
	ACTIVE("A", IdType.Record),
	INACTIVE("I", IdType.Record),
	SUSPENDED("S", IdType.Record), // for Email Address

	//
	// define out-bound message statusId
	//
	PENDING("P", IdType.Outbound),
	DELIVERED("D", IdType.Outbound),
	DELIVERY_FAILED("F", IdType.Outbound),
	//
	// define in-bound message status
	//
	CLOSED("C", IdType.Inbound),
	OPENED("O", IdType.Inbound);
	
	private final String value;
	private final IdType type;
	private StatusId(String value, IdType type) {
		this.value = value;
		this.type = type;
	}
	
	public String value() {
		return value;
	}
	
	public IdType getType() {
		return type;
	}
	
	public static enum IdType {
		Record, Inbound, Outbound;
	}
}

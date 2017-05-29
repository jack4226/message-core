package ltj.message.constant;

public enum VariableName {
	//
	// define variable names
	//
	// content names
	SUBJECT("Subject"),
	BODY("Body"),
	// X-Header data type
	DATA_NAME("X-Header"),
	// template names
	SUBJECT_TEMPLATE("SubjTemplate"),
	BODY_TEMPLATE("BodyTemplate"),
	// field names for String fields
	RULE_NAME("RuleName"),
	CARRIER_CODE("CarrierCode"),
	MAILBOX_HOST("MailboxHost"),
	MAILBOX_USER("MailboxUser"),
	MAILBOX_NAME("MailboxName"),
	FOLDER_NAME("FolderName"),
	CLIENT_ID("ClientId"),
	CUSTOMER_ID("CustId"),
	TO_PLAIN_TEXT("ToPlainText"), // "yes" or "no"
	// field names for numeric fields (Long)
	MSG_ID("MsgId"),
	MSG_REF_ID("MsgRefId"),
	// email property names
	PRIORITY("Priority"),
	SEND_DATE("SendDate"),
	RFC822("Rfc822"),
	DELIVERY_REPORT("Report"),
	DELIVERY_STATUS("DeliveryStatus");

	private final String value;
	private VariableName(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
	public static enum MailingListVariableName {
		MailingListAddress, MailingListId, MailingListName, SubscriberAddress, SubscriberAddressId, BroadcastMsgId;
	}
}

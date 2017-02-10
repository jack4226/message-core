package ltj.message.constant;

import ltj.data.preload.RuleDataTypeEnum;

/** define rule search field name for Internet mail */
public enum RuleDataName {
	FROM_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, AddressType.FROM_ADDR.value()),
	TO_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, AddressType.TO_ADDR.value()),
	REPLYTO_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, AddressType.REPLYTO_ADDR.value()),
	CC_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, AddressType.CC_ADDR.value()),
	BCC_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, AddressType.BCC_ADDR.value()),
	
	SUBJECT(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.SUBJECT),
	BODY(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.BODY),
	MSG_ID(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.MSG_ID),
	MSG_REF_ID(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.MSG_REF_ID),
	RULE_NAME(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.RULE_NAME),
	X_HEADER(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.DATA_NAME),
	RETURN_PATH(RuleDataTypeEnum.EMAIL_PROPERTY, "ReturnPath"),
	// mailbox properties
	MAILBOX_USER(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.MAILBOX_USER),
	MAILBOX_HOST(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.MAILBOX_HOST),
	// the next two items are not implemented yet
	RFC822(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.RFC822),
	DELIVERY_STATUS(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.DELIVERY_STATUS),
	// define data type constants for Internet email attachments
	MIME_TYPE(RuleDataTypeEnum.EMAIL_PROPERTY, "MimeType"),
	FILE_NAME(RuleDataTypeEnum.EMAIL_PROPERTY, "FileName"),
	// define other email address properties
	FINAL_RCPT_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, AddressType.FINAL_RCPT_ADDR.value()),
	ORIG_RCPT_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, AddressType.ORIG_RCPT_ADDR.value());

	private RuleDataTypeEnum dataType;
	private String value;
	private RuleDataName(RuleDataTypeEnum dataType, String value) {
		this.dataType = dataType;
		this.value = value;
	}
	
	public RuleDataTypeEnum getRuleDataType() {
		return dataType;
	}

	public String getValue() {
		return value;
	}
}
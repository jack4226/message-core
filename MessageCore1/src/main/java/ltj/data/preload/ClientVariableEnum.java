package ltj.data.preload;

import ltj.message.constant.CodeType;
import ltj.message.constant.VariableType;

/*
 * define sample sender variables
 */
public enum ClientVariableEnum {
	CurrentDateTime(null, null, VariableType.DATETIME, CodeType.Y),
	CurrentDate(null, "yyyy-MM-dd", VariableType.DATETIME, CodeType.Y),
	CurrentTime(null, "hh:mm:ss a", VariableType.DATETIME, CodeType.Y),
	//SenderId(Constants.DEFAULT_SENDER_ID,null,VariableType.TEXT,CodeType.YES_CODE)
	;
	
	private String value;
	private String format;
	private VariableType type;
	private CodeType allowOverride;
	private ClientVariableEnum(String value, String format, VariableType type, CodeType allowOverride) {
		this.value=value;
		this.format=format;
		this.type=type;
		this.allowOverride=allowOverride;
	}

	public String getDefaultValue() {
		return value;
	}
	public String getVariableFormat() {
		return format;
	}
	public VariableType getVariableType() {
		return type;
	}
	public CodeType getAllowOverride() {
		return allowOverride;
	}
}

package ltj.data.preload;

import ltj.message.constant.CodeType;
import ltj.message.constant.VariableType;

/*
 * define sample sender variables
 */
public enum SenderVariableEnum {
	CurrentDateTime(null,null,VariableType.DATETIME,CodeType.Y),
	CurrentDate(null,"yyyy-MM-dd",VariableType.DATETIME,CodeType.Y),
	CurrentTime(null,"hh:mm:ss a",VariableType.DATETIME,CodeType.Y),
	//SenderId(Constants.DEFAULT_SENDER_ID,null,VariableType.TEXT,CodeType.YES_CODE)
	;
	
	private String value;
	private String format;
	private String type;
	private CodeType allowOverride;
	private SenderVariableEnum(String value, String format, String type, CodeType allowOverride) {
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
	public String getVariableType() {
		return type;
	}
	public CodeType getAllowOverride() {
		return allowOverride;
	}
}

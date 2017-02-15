package ltj.data.preload;

import ltj.message.constant.CodeType;
import ltj.message.constant.Constants;
import ltj.message.constant.VariableType;
import ltj.message.constant.XHeaderName;

public enum GlobalVariableEnum {
	CurrentDateTime(null, "yyyy-MM-dd HH:mm:ss", VariableType.DATETIME, CodeType.Y, null),
	CurrentDate(null, "yyyy-MM-dd", VariableType.DATETIME, CodeType.Y, null),
	CurrentTime(null, "hh:mm:ss a", VariableType.DATETIME, CodeType.Y, null),
	XClientId(Constants.DEFAULT_CLIENTID, null, VariableType.X_HEADER, CodeType.Y, XHeaderName.CLIENT_ID.value()),
	//To(null, null, VariableType.ADDRESS, CodeType.MANDATORY_CODE, null),
	PoweredBySignature(Constants.POWERED_BY_HTML_TAG, null, VariableType.TEXT, CodeType.N, null);
	
	private String value;
	private String format;
	private String type;
	private CodeType allowOverride;
	private String name;
	private GlobalVariableEnum(String value, String format, String type, CodeType allowOverride, String name) {
		this.value=value;
		this.format=format;
		this.type=type;
		this.allowOverride=allowOverride;
		this.name = name;
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
	public String getVariableName() {
		return name;
	}
}

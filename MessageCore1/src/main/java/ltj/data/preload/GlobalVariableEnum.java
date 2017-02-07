package ltj.data.preload;

import ltj.message.constant.CodeType;
import ltj.message.constant.Constants;
import ltj.message.constant.VariableType;

public enum GlobalVariableEnum {
	CurrentDateTime(null,"yyyy-MM-dd HH:mm:ss",VariableType.DATETIME,CodeType.Y),
	CurrentDate(null,"yyyy-MM-dd",VariableType.DATETIME,CodeType.Y),
	CurrentTime(null,"hh:mm:ss a",VariableType.DATETIME,CodeType.Y),
	PoweredBySignature(Constants.POWERED_BY_HTML_TAG,null,VariableType.TEXT,CodeType.N),
	To(null,null,VariableType.ADDRESS,CodeType.MANDATORY_CODE);
	
	private String value;
	private String format;
	private String type;
	private CodeType allowOverride;
	private GlobalVariableEnum(String value, String format, String type, CodeType allowOverride) {
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

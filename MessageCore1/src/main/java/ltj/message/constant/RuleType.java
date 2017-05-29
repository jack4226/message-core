package ltj.message.constant;

/** define rule type constants */
public enum RuleType {
	SIMPLE("Simple"),
	ALL("All"),
	ANY("Any"),
	NONE("None");
	
	private String value;
	private RuleType(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
	public static RuleType getByValue(String value) {
		for (RuleType type : RuleType.values()) {
			if (type.value().equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("No enum const value jpa.constant.RuleType." + value);
	}
} 


package ltj.message.constant;

public enum VariableType {
	//
	// define variable types
	//
	ADDRESS("A"), TEXT("T"), NUMERIC("N"), DATETIME("D"), X_HEADER("X"), LOB("L"), COLLECTION("C");
	private final String value;
	private VariableType(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	public static VariableType getByValue(String value) {
		for (VariableType type : VariableType.values()) {
			if (type.value().equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("VariableType does not exist with value \"" + value + "\".");
	}
}

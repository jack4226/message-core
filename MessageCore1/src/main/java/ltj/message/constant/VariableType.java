package ltj.message.constant;

public class VariableType {
	//
	// define variable types
	//
	public final static String TEXT = Variable.TEXT.getValue();
	public final static String NUMERIC = Variable.NUMERIC.getValue();
	public final static String ADDRESS = Variable.ADDRESS.getValue();
	public final static String DATETIME = Variable.DATETIME.getValue();
	public final static String X_HEADER = Variable.X_HEADER.getValue();
	public final static String LOB = Variable.LOB.getValue();
	// body template only
	public final static String COLLECTION = Variable.COLLECTION.getValue();
	// a collection of <HashMap>s (for Table section)

	// enum class = list of variables
	public static enum Variable {
		ADDRESS("A"), TEXT("T"), NUMERIC("N"), DATETIME("D"), X_HEADER("X"), LOB("L"), COLLECTION("C");
		private final String value;
		private Variable(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}
}

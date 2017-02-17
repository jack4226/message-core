package ltj.message.constant;

public class VariableType {
	//
	// define variable types
	//
	public final static String TEXT = Variable.TEXT.value();
	public final static String NUMERIC = Variable.NUMERIC.value();
	public final static String ADDRESS = Variable.ADDRESS.value();
	public final static String DATETIME = Variable.DATETIME.value();
	public final static String X_HEADER = Variable.X_HEADER.value();
	public final static String LOB = Variable.LOB.value();
	// body template only
	public final static String COLLECTION = Variable.COLLECTION.value();
	// a collection of <HashMap>s (for Table section)

	// enum class = list of variables
	public static enum Variable {
		ADDRESS("A"), TEXT("T"), NUMERIC("N"), DATETIME("D"), X_HEADER("X"), LOB("L"), COLLECTION("C");
		private final String value;
		private Variable(String value) {
			this.value = value;
		}
		public String value() {
			return value;
		}
	}
}

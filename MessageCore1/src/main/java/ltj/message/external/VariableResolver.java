package ltj.message.external;

import ltj.message.exception.DataValidationException;

public interface VariableResolver {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process(long addrId) throws DataValidationException;
}

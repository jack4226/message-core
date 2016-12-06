package ltj.message.external;

import ltj.message.exception.DataValidationException;

public interface RuleTargetProc {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process() throws DataValidationException;
}

package ltj.message.external;

import org.apache.log4j.Logger;

import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.exception.DataValidationException;
import ltj.spring.util.SpringUtil;

public class CustomerNameResolver implements VariableResolver {
	static final Logger logger = Logger.getLogger(CustomerNameResolver.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public String process(long addrId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		String query = "SELECT CONCAT(c.firstName,' ',c.lastName) as ResultStr " +
				" FROM customers c, emailaddr e " +
				" where e.emailaddrId=c.emailAddrId and e.emailAddrId=?";
		
		EmailVariableDao dao = SpringUtil.getDaoAppContext().getBean(EmailVariableDao.class);
		String result = dao.getByQuery(query, addrId);
		
		return result;
	}
}

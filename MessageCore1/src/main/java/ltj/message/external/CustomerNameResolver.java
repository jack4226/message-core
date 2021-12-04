package ltj.message.external;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.exception.DataValidationException;
import ltj.spring.util.SpringUtil;

public class CustomerNameResolver implements VariableResolver {
	static final Logger logger = LogManager.getLogger(CustomerNameResolver.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public String process(long addrId) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		String query = "SELECT CONCAT(c.first_name,' ',c.last_name) as ResultStr " +
				" FROM customer_tbl c, email_address e " +
				" where e.email_addr_id=c.email_addr_id and e.email_addr_id=?";
		
		EmailVariableDao dao = SpringUtil.getDaoAppContext().getBean(EmailVariableDao.class);
		String result = dao.getByQuery(query, addrId);
		
		return result;
	}
}

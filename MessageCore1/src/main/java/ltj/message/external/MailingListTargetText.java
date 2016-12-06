package ltj.message.external;

import java.util.List;

import org.apache.log4j.Logger;

import ltj.jbatch.app.SpringUtil;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.emailaddr.MailingListVo;

public class MailingListTargetText implements RuleTargetProc {
	static final Logger logger = Logger.getLogger(MailingListTargetText.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * retrieve all mailing list addresses, and construct a regular expression
	 * that matches any address on the list.
	 * 
	 * @return a regular expression
	 */
	public String process() throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		
		StringBuffer sb = new StringBuffer();
		MailingListDao dao = (MailingListDao) SpringUtil.getDaoAppContext().getBean("mailingListDao");
		List<MailingListVo> list = dao.getAll(false);
		for (int i = 0; i < list.size(); i++) {
			MailingListVo item = list.get(i);
			// no display name allowed for list address, just for safety
			String emailAddr = EmailAddrUtil.removeDisplayName(item.getEmailAddr(), true);
			// make it a regular expression
			emailAddr = StringUtil.replaceAll(emailAddr, ".", "\\.");
			if (i > 0) {
				sb.append("|");
			}
			sb.append("^" + emailAddr + "$");
		}
		return sb.toString();
	}
}

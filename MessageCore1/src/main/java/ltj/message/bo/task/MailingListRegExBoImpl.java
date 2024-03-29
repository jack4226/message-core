package ltj.message.bo.task;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.StringUtil;
import ltj.message.vo.emailaddr.MailingListVo;

@Component("mailingListRegExBo")
@Scope(value="prototype")
@Lazy(value=true)
public class MailingListRegExBoImpl extends TaskBaseAdaptor {
	static final Logger logger = LogManager.getLogger(MailingListRegExBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailingListDao mailingListDao;

	/**
	 * retrieve all mailing list addresses, and construct a regular expression
	 * that matches any address on the list.
	 * 
	 * @return a regular expression
	 */
	public String process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		StringBuffer sb = new StringBuffer();
		List<MailingListVo> list = mailingListDao.getAll(false);
		for (int i = 0; i < list.size(); i++) {
			MailingListVo item = list.get(i);
			// no display name allowed for list address, just for safety
			String emailAddr = EmailAddrUtil.removeDisplayName(item.getEmailAddr(), true);
			emailAddr = StringUtil.replaceAll(emailAddr, ".", "\\.");
			emailAddr = StringUtil.replaceAll(emailAddr, "-", "\\-");
			if (i > 0) {
				sb.append("|");
			}
			sb.append("^" + emailAddr + "$");
		}
		return sb.toString();
	}
}

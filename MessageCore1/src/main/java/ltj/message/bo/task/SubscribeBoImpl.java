package ltj.message.bo.task;

import java.util.List;

import javax.mail.Address;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.StringUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.MailingListVo;

@Component("subscribeBo")
@Scope(value="prototype")
@Lazy(value=true)
public class SubscribeBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(SubscribeBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressDao emailAddressDao;
	@Autowired
	private MailingListDao mailingListDao;
	@Autowired
	private EmailSubscrptDao emailSubscrptDao;

	/**
	 * Subscribe the FROM address to the mailing list (TO).
	 * 
	 * @return a Long value representing number of addresses that have been
	 *         updated.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		long addrsUpdated = 0;
		Address[] toAddrs = messageBean.getTo();
		if (toAddrs == null || toAddrs.length == 0) { // just for safety
			logger.error("subscription address (TO) is null.");
			throw new DataValidationException("TO address is null");
		}
		for (int k = 0; k < toAddrs.length; k++) {
			Address toAddr = toAddrs[k];
			if (toAddr == null || StringUtil.isEmpty(toAddr.toString())) {
				continue; // just for safety
			}
			List<MailingListVo> mlist = mailingListDao.getByAddress(toAddr.toString());
			if (mlist.size() == 0) {
				logger.warn("Mailing List not found for address: " + toAddr);
				continue;
			}
			Address[] addrs = messageBean.getFrom();
			for (int i = 0; addrs != null && i < addrs.length; i++) {
				Address addr = addrs[i];
				if (addr == null || StringUtil.isEmpty(addr.toString())) {
					continue; // just for safety
				}
				EmailAddressVo emailAddressVo = emailAddressDao.findByAddress(addr.toString());
				int rowsAffected = 0;
				for (MailingListVo item : mlist) {
					messageBean.setMailingListId(item.getListId());
					rowsAffected += emailSubscrptDao.subscribe(emailAddressVo.getEmailAddrId(), item.getListId());
					logger.info(addr + " subscribed to: " + item.getListId());
				}
				if (rowsAffected > 0) {
					addrsUpdated++;
				}
			}
		}
		return Long.valueOf(addrsUpdated);
	}
}

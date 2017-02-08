package ltj.message.bo;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.constant.AddressType;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.emailaddr.EmailAddrVo;

@Component("bounceBo")
@Scope(value="prototype")
@Lazy(value=true)
public class BounceBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(BounceBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddrDao emailAddrDao;

	/**
	 * Increase the bounce count to the email addresses involved. The column
	 * "DataTypeValues" from MsgAction table should contain address types (FROM,
	 * TO, etc) that need to be updated (bounce count increment).
	 * 
	 * @return a Long representing the number of addresses updated.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		if (getArgumentList(taskArguments).isEmpty()) {
			throw new DataValidationException("Arguments is not valued, nothing to suspend");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		// example: $FinalRcpt,$OriginalRcpt,badaddress@baddomain.com
		long addrsUpdated = 0;
		for (String token : taskArguments) {
			String addrs = null;
			if (token != null && token.startsWith("$")) { // address type
				token = token.substring(1);
				if (AddressType.FROM_ADDR.value().equals(token)) {
					addrs = messageBean.getFromAsString();
				}
				else if (AddressType.FINAL_RCPT_ADDR.value().equals(token)) {
					addrs = messageBean.getFinalRcpt();
				}
				else if (AddressType.ORIG_RCPT_ADDR.value().equals(token)) {
					addrs = messageBean.getOrigRcpt();
				}
				else if (AddressType.FORWARD_ADDR.value().equals(token)) {
					addrs = messageBean.getForwardAsString();
				}
				else if (AddressType.TO_ADDR.value().equals(token)) {
					addrs = messageBean.getToAsString();
				}
				else if (AddressType.REPLYTO_ADDR.value().equals(token)) {
					addrs = messageBean.getReplytoAsString();
				}
			}
			else { // address
				addrs = token;
			}
			
			Address[] iAddrs = null;
			if (StringUtils.isNotBlank(addrs)) {
				try {
					iAddrs = InternetAddress.parse(addrs);
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
				}
			}
			if (isDebugEnabled) {
				logger.debug("Address(es) to increase bounce count: " + addrs);
			}
			for (int i=0; iAddrs!=null && i<iAddrs.length; i++) {
				Address iAddr = iAddrs[i];
				String addr = iAddr.toString();
				EmailAddrVo emailAddrVo = emailAddrDao.getByAddress(addr);
				if (emailAddrVo != null) {
					if (isDebugEnabled) {
						logger.debug("Increasing bounce count to EmailAddr: " + addr);
					}
					emailAddrDao.updateBounceCount(emailAddrVo);
					addrsUpdated++;
				}
				else {
					if (isDebugEnabled) {
						logger.debug("Address (" + addr + ") does not exist, failed to increase bounce count!");
					}
				}
			}
		}
		return Long.valueOf(addrsUpdated);
	}
}

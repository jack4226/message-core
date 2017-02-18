package ltj.message.bo;

import java.sql.Timestamp;

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
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.emailaddr.EmailAddrVo;

@Component("activateBo")
@Scope(value="prototype")
@Lazy(value=true)
public class ActivateBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ActivateBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddrDao emailAddrDao;
	
	/**
	 * Activate email addresses. The column "DataTypeValues" from MsgAction
	 * table should contain address types (FROM, TO, etc) that need to be
	 * activated.
	 * 
	 * @return a Long value representing the number of addresses that have been
	 *         activated.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		if (getArgumentList(taskArguments).isEmpty()) {
			throw new DataValidationException("Arguments is blank, nothing to activate");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		// example: $From,$To,myaddress@mydomain.com
		long addrsActiveted = 0;
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		for (String token : taskArguments) {
			String addrs = null;
			if (token != null && token.startsWith("$")) { // address variable
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
			else { // real email address
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
			for (int i = 0; iAddrs != null && i < iAddrs.length; i++) {
				Address iAddr = iAddrs[i];
				String addr = iAddr.toString();
				if (isDebugEnabled) {
					logger.debug("Address to actiavte: " + addr);
				}
				EmailAddrVo emailAddrVo = emailAddrDao.findByAddress(addr);
				if (!StatusId.ACTIVE.value().equals(emailAddrVo.getStatusId())) {
					if (isDebugEnabled) {
						logger.debug("Activating EmailAddr: " + addr);
					}
					emailAddrVo.setStatusId(StatusId.ACTIVE.value());
					emailAddrVo.setBounceCount(0); // reset bounce count
					emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
					emailAddrVo.setStatusChangeTime(updtTime);
					emailAddrDao.update(emailAddrVo);
				}
				else if (emailAddrVo.getBounceCount() > 0) {
					// email address already active, reset bounce count
					emailAddrDao.updateBounceCount(emailAddrVo.getEmailAddrId(), 0);
				}
				addrsActiveted++;
			}
		}
		return Long.valueOf(addrsActiveted);
	}
}

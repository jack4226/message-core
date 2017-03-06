package ltj.message.bo.task;

import java.sql.Timestamp;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.constant.AddressType;
import ltj.message.constant.Constants;
import ltj.message.constant.StatusId;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.StringUtil;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.MsgInboxVo;

@Component("suspendBo")
@Scope(value="prototype")
@Lazy(value=true)
public class SuspendBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(SuspendBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddrDao emailAddrDao;
	@Autowired
	private MsgInboxDao msgInboxDao;

	/**
	 * Suspend email addresses. The column "DataTypeValues" from MsgAction table
	 * contains address types (FROM, TO, etc) that to be suspended.
	 * 
	 * @return a Long value representing the number of addresses that have been
	 *         suspended.
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
		
		// example: $FinalRcpt,$OriginalRcpt,badaddress@badcompany.com
		long addrsSuspended = 0;
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
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
			else { // real email address
				addrs = token;
			}
			
			if (!StringUtil.isEmpty(addrs)) {
				try {
					InternetAddress.parse(addrs);
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
					addrs = null;
				}
			}
			if (StringUtil.isEmpty(addrs)) {
				// just for safety
				continue;
			}
			if (isDebugEnabled) {
				logger.debug("Address(es) to suspend: " + addrs);
			}
			StringTokenizer st2 = new StringTokenizer(addrs, ",");
			while (st2.hasMoreTokens()) {
				String addr = st2.nextToken();
				EmailAddrVo emailAddrVo = emailAddrDao.getByAddress(addr);
				if (emailAddrVo != null && !StatusId.SUSPENDED.value().equals(emailAddrVo.getStatusId())) {
					if (isDebugEnabled) {
						logger.debug("Suspending EmailAddr: " + addr);
					}
					emailAddrVo.setStatusId(StatusId.SUSPENDED.value());
					emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
					emailAddrVo.setStatusChangeTime(updtTime);
					emailAddrDao.update(emailAddrVo);
					addrsSuspended++;
				}
			}
		} // end of while loop
		// if failed to suspend any address, check if MsgRefId is valued
		if (addrsSuspended == 0 && messageBean.getMsgRefId() != null) {
			// -- this has been taken care of by MessageParser
			//addrsSuspended = suspendByMsgRefId(messageBean, updtTime);
		}
		return Long.valueOf(addrsSuspended);
	}

	int suspendByMsgRefId(MessageBean messageBean, Timestamp updtTime) {
		int addrsSuspended = 0;
		if (messageBean.getMsgRefId() == null) {
			return addrsSuspended;
		}
		// find the "to address" by MsgRefId and suspend it
		long msgId = messageBean.getMsgRefId().longValue();
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			logger.warn("Failed to find MsgInbox record by MsgId: " + msgId);
		}
		else if (!RuleNameEnum.SEND_MAIL.name().equals(msgInboxVo.getRuleName())) {
			logger.error("Message from MsgRefId is not a 'SEND_MAIL', ignored." + LF + messageBean);
		}
		else if (msgInboxVo.getToAddrId() != null) { // should always valued
			long toAddr = msgInboxVo.getToAddrId().longValue();
			EmailAddrVo emailAddrVo = emailAddrDao.getByAddrId(toAddr);
			if (!StatusId.SUSPENDED.value().equals(emailAddrVo.getStatusId())) {
				if (isDebugEnabled) {
					logger.debug("Suspending EmailAddr: " + emailAddrVo.getEmailAddr());
				}
				emailAddrVo.setStatusId(StatusId.SUSPENDED.value());
				emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
				emailAddrVo.setStatusChangeTime(updtTime);
				emailAddrDao.update(emailAddrVo);
				addrsSuspended++;
			}
		}
		return addrsSuspended;
	}
}

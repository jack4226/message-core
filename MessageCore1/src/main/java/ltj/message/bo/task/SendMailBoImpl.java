package ltj.message.bo.task;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.constant.StatusId;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.emailaddr.EmailAddressVo;

@Component("sendMailBo")
@Scope(value="prototype")
@Lazy(value=true)
public class SendMailBoImpl extends TaskBaseAdaptor {
	static final Logger logger = LogManager.getLogger(SendMailBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressDao emailAddressDao;

	/**
	 * Send the email off by writing the MessageBean object to MailSender input
	 * queue.
	 * 
	 * @param messageBean -
	 *            MsgRefId that links to a received message must be populated.
	 * @return a Long value representing number of addresses the message has
	 *         been sent to.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException, AddressException, JMSException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (messageBean.getMsgRefId() == null) {
			logger.warn("messageBean.getMsgRefId() returned null");
			//throw new DataValidationException("messageBean.getMsgRefId() returned null");
		}
		
		if (isDebugEnabled) {
			logger.debug("Sending email to: " + messageBean.getToAsString());
		}
		setTargetToMailSender();
		// validate the TO address, just for safety
		Address[] addrs = InternetAddress.parse(messageBean.getToAsString());
		int mailsSent = 0;
		for (Address addr : addrs) {
			if (addr == null) {
				continue;
			}
			EmailAddressVo vo = emailAddressDao.findByAddress(addr.toString());
			if (StatusId.ACTIVE.value().equalsIgnoreCase(vo.getStatusId())) {
				String jmsMsgId = jmsProcessor.writeMsg(messageBean);
				mailsSent++;
				if (isDebugEnabled) {
					logger.debug("Jms Message Id returned: " + jmsMsgId);
				}
			}
		}
		return Long.valueOf(mailsSent);
	}
}

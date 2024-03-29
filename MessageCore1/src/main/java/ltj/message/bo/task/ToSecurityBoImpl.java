package ltj.message.bo.task;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.constant.AddressType;
import ltj.message.constant.TableColumnName;
import ltj.message.dao.client.ClientUtil;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.ClientVo;

@Component("toSecurityBo")
@Scope(value="prototype")
@Lazy(value=true)
public class ToSecurityBoImpl extends TaskBaseAdaptor {
	static final Logger logger = LogManager.getLogger(ToSecurityBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * Forward the message to security department by sending the message to Mail
	 * Sender queue. The forwarding addresses or address types are obtained from
	 * the "DataTypeValues" field of MsgAction table.
	 * 
	 * @return a Long value representing number of addresses the message has
	 *         been forwarded to.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException, MessagingException, JMSException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		String clientId = messageBean.getClientId();
		ClientVo clientVo = ClientUtil.getClientVo(clientId);
		if (clientVo == null) {
			throw new DataValidationException("Client record not found by clientId: " + clientId);
		}

		String forwardAddrs = "";
		String ccAddrs = "";
		/*
		 * If a security email variable is supplied from task arguments, use it,
		 * otherwise, use security address from Client table. If Forward or To
		 * variables are supplied from task arguments, send carbon copies to
		 * those addresses as well.
		 */
		if (getArgumentList(taskArguments).size() > 0) {
			// example: $Forward,security@mycompany.com or $To or security@mycompany.com
			for (String token : taskArguments) {
				String addrs = null;
				String cc_addrs = null;
				if (token != null && token.startsWith("$")) { // address type
					token = token.substring(1);
					// only $Forward and $To can be cc'ed.
					if (AddressType.FORWARD_ADDR.value().equals(token)) {
						cc_addrs = messageBean.getForwardAsString();
					}
					else if (AddressType.TO_ADDR.value().equals(token)) {
						cc_addrs = messageBean.getToAsString();
					}
					// E-mail addresses from Client table
					else if (TableColumnName.CUSTOMER_CARE_ADDR.equals(token)) {
						addrs = clientVo.getCustcareEmail();
					}
					else if (TableColumnName.SECURITY_DEPT_ADDR.equals(token)) {
						addrs = clientVo.getSecurityEmail();
					}
					else if (TableColumnName.RMA_DEPT_ADDR.equals(token)) {
						addrs = clientVo.getRmaDeptEmail();
					}
					else if (TableColumnName.SPAM_CONTROL_ADDR.equals(token)) {
						addrs = clientVo.getSpamCntrlEmail();
					}
					else if (TableColumnName.VIRUS_CONTROL_ADDR.equals(token)) {
						addrs = clientVo.getSpamCntrlEmail();
					}
					else if (TableColumnName.CHALLENGE_HANDLER_ADDR.equals(token)) {
						addrs = clientVo.getChaRspHndlrEmail();
					}
					// end of Client table
				}
				else { // real security email address from input
					addrs = token;
				}
				// accumulate addresses
				if (addrs != null && addrs.trim().length() > 0) {
					if (forwardAddrs == null || forwardAddrs.trim().length() == 0) {
						forwardAddrs += addrs;
					}
					else {
						forwardAddrs += "," + addrs;
					}
				}
				else if (cc_addrs != null && cc_addrs.trim().length() > 0) {
					if (ccAddrs == null || ccAddrs.trim().length() == 0) {
						ccAddrs += cc_addrs;
					}
					else {
						ccAddrs += "," + cc_addrs;
					}
				}
			}
		}
		// if security email address not passed from input, retrieve if from Client table
		if (forwardAddrs == null || forwardAddrs.trim().length() == 0) {
			forwardAddrs = clientVo.getSecurityEmail();
		}
		if (isDebugEnabled) {
			logger.debug("Address(es) to forward to: " + forwardAddrs);
		}
		if (forwardAddrs == null || forwardAddrs.trim().length() == 0) {
			throw new DataValidationException("forward address is not provided");
		}
		
		Address[] addresses = InternetAddress.parse(forwardAddrs);
		MessageBean mBean = new MessageBean();
		mBean.setFrom(messageBean.getTo());
		mBean.setTo(addresses);
		if (ccAddrs != null && ccAddrs.trim().length() > 0) {
			Address[] ccAddresses = InternetAddress.parse(ccAddrs);
			mBean.setCc(ccAddresses);
		}
		mBean.setSubject("Fwd: " + messageBean.getSubject());
		mBean.setValue("Forward to Security."); 
		mBean.setMailboxUser(messageBean.getMailboxUser());
		mBean.setOriginalMail(messageBean);

		String jmsMsgId = jmsProcessor.writeMsg(mBean);
		if (isDebugEnabled) {
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		}
		return Long.valueOf(addresses.length);
	}
}

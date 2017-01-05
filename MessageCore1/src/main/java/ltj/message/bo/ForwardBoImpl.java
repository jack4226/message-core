package ltj.message.bo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanBuilder;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.constant.EmailAddressType;
import ltj.message.constant.TableColumnName;
import ltj.message.dao.client.ClientUtil;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.ClientVo;

@Component("forwardBo")
@Scope(value="prototype")
@Lazy(value=true)
public class ForwardBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ForwardBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * Forward the message to the specified addresses. The forwarding addresses
	 * or address types are obtained from "DataTypeValues" column of MsgAction
	 * table. The original email raw stream should be included in the input
	 * MessageBean by the calling program.
	 * 
	 * @return a Long value representing number of addresses the message is
	 *         forwarded to.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException, MessagingException, JMSException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (!messageBean.getHashMap().containsKey(MessageBeanBuilder.MSG_RAW_STREAM)) {
			logger.warn("Email Raw Stream not found in MessageBean.hashMap");
		}
		if (taskArguments == null || taskArguments.trim().length() == 0) {
			throw new DataValidationException("Arguments is not valued, can't forward");
		}
		
		String clientId = messageBean.getClientId();
		ClientVo clientVo = ClientUtil.getClientVo(clientId);
		if (clientVo == null) {
			throw new DataValidationException("Client record not found by clientId: " + clientId);
		}

		// example: $Forward,securityDept@mycompany.com
		String forwardAddrs = "";
		StringTokenizer st = new StringTokenizer(taskArguments, ",");
		while (st.hasMoreTokens()) {
			String addr = null;
			String token = st.nextToken();
			if (token != null && token.startsWith("$")) { // address type
				token = token.substring(1);
				if (EmailAddressType.FROM_ADDR.equals(token)) {
					addr = messageBean.getFromAsString();
				}
				else if (EmailAddressType.FINAL_RCPT_ADDR.equals(token)) {
					addr = messageBean.getFinalRcpt();
				}
				else if (EmailAddressType.ORIG_RCPT_ADDR.equals(token)) {
					addr = messageBean.getOrigRcpt();
				}
				else if (EmailAddressType.FORWARD_ADDR.equals(token)) {
					addr = messageBean.getForwardAsString();
				}
				else if (EmailAddressType.TO_ADDR.equals(token)) {
					addr = messageBean.getToAsString();
				}
				else if (EmailAddressType.REPLYTO_ADDR.equals(token)) {
					addr = messageBean.getReplytoAsString();
				}
				// E-mail addresses from Client table
				else if (TableColumnName.CUSTOMER_CARE_ADDR.equals(token)) {
					addr = clientVo.getCustcareEmail();
				}
				else if (TableColumnName.SECURITY_DEPT_ADDR.equals(token)) {
					addr = clientVo.getSecurityEmail();
				}
				else if (TableColumnName.RMA_DEPT_ADDR.equals(token)) {
					addr = clientVo.getRmaDeptEmail();
				}
				else if (TableColumnName.SPAM_CONTROL_ADDR.equals(token)) {
					addr = clientVo.getSpamCntrlEmail();
				}
				else if (TableColumnName.VIRUS_CONTROL_ADDR.equals(token)) {
					addr = clientVo.getSpamCntrlEmail();
				}
				else if (TableColumnName.CHALLENGE_HANDLER_ADDR.equals(token)) {
					addr = clientVo.getChaRspHndlrEmail();
				}
				// end of Client table
			}
			else { // real email address
				addr = token;
			}
			
			if (StringUtils.isNotBlank(addr)) {
				try {
					InternetAddress.parse(addr);
					if (StringUtils.isBlank(forwardAddrs)) {
						forwardAddrs += addr;
					}
					else {
						forwardAddrs += "," + addr;
					}
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addr + ", skip...");
				}
			}
		} // end of while
		if (isDebugEnabled) {
			logger.debug("Address(es) to forward to: " + forwardAddrs);
		}
		if (StringUtils.isBlank(forwardAddrs)) {
			throw new DataValidationException("forward address is empty!");
		}
		
		if (messageBean.getMsgId() != null) {
			messageBean.setMsgRefId(messageBean.getMsgId());
		}
		Address[] addresses = InternetAddress.parse(forwardAddrs);
		Message msg = null;
		byte[] stream = (byte[]) messageBean.getHashMap().get(MessageBeanBuilder.MSG_RAW_STREAM);
		if (stream == null) { // just for safety
			msg = MessageBeanUtil.createMimeMessage(messageBean);
		}
		else {
			msg = MessageBeanUtil.createMimeMessage(stream);
			MessageBeanUtil.addBeanFieldsToHeader(messageBean, msg);
		}
		//msg.removeHeader("Received"); // remove "Received" history
		//msg.removeHeader("Delivered-To"); // remove delivery history
		
		msg.setSubject("Fwd: " + messageBean.getSubject());
		msg.setRecipients(Message.RecipientType.CC, null);
		msg.setRecipients(Message.RecipientType.BCC, null);
		
		msg.setRecipients(Message.RecipientType.TO, addresses);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			msg.writeTo(baos);
			setTargetToMailSender();
			String jmsMsgId = jmsProcessor.writeMsg(baos.toByteArray());
			baos.close();
			if (isDebugEnabled) {
				logger.debug("Jms Message Id returned: " + jmsMsgId);
			}
		} catch (IOException e) {
			logger.error("IOException caught", e);
		}
		return Long.valueOf(addresses.length);
	}
}

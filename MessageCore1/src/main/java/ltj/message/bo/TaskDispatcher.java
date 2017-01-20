package ltj.message.bo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ltj.message.bean.MessageBean;
import ltj.message.constant.RuleNameType;
import ltj.message.dao.action.MsgActionDao;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.action.MsgActionVo;
import ltj.spring.util.SpringUtil;

/**
 * An Email Message Processor. It retrieve process class names or process bean
 * id's from MsgAction table and MsgActionDetail table by the rule name, and
 * process the message by invoking the classes or beans.
 */
@Component("taskDispatcher")
@Lazy(value=true)
public class TaskDispatcher {
	static final Logger logger = Logger.getLogger(TaskDispatcher.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static final String LF = System.getProperty("line.separator", "\n");
	
	@Autowired
	private MsgActionDao msgActionDao;
	@Autowired
	private EmailAddrDao emailAddrDao;
	
	private static Hashtable<String, String> mailSenderJndi = null;
	
	public TaskDispatcher() {
	}
	
	//@org.springframework.transaction.annotation.Transactional
	public void dispatchTasks(MessageBean msgBean) throws DataValidationException, MessagingException, JMSException {
		if (isDebugEnabled) {
			logger.debug("Entering dispatchTasks() method. MessageBean:" + LF + msgBean);
		}
		if (StringUtils.isBlank(msgBean.getRuleName())) {
			throw new DataValidationException("RuleName is required.");
		}
		
		if (msgBean.getFrom() == null) {
			if (RuleNameType.BROADCAST.name().equals(msgBean.getRuleName())) {
				msgBean.setFrom(InternetAddress.parse("bcst_tmplt@localhost"));
			}
			else {
				throw new DataValidationException("From email address is required.");
			}
		}
		if (msgBean.getTo() == null) {
			if (RuleNameType.BROADCAST.name().equals(msgBean.getRuleName())) {
				msgBean.setTo(InternetAddress.parse("bcst_tmplt@localhost"));
			}
			else {
				throw new DataValidationException("To email address is required.");
			}
		}
		
		/* 
		 * Save addresses first to resolve MySQL error - Lock wait timeout exceeded;
		 * Only do this if it's not already in a transaction. 
		 */
		if (SpringUtil.isInTransaction() == false) {
			SpringUtil.beginTransaction();
			try {
				/* insert email addresses */
				saveEmailAddr(msgBean.getFrom());
				saveEmailAddr(msgBean.getTo());
				saveEmailAddr(msgBean.getReplyto());
				/* end of email addresses */
				SpringUtil.commitTransaction();
			}
			catch (Exception e) {
				logger.error("Exception during saving email addrs: " + e.getMessage());
				SpringUtil.rollbackTransaction();
			}
		}
		
		SpringUtil.beginTransaction();
		try{
			List<MsgActionVo> actions = msgActionDao.getByBestMatch(msgBean.getRuleName(), null, msgBean.getClientId());
			if (actions == null || actions.isEmpty()) {
				// actions not defined, save the message.
				String processBeanId = "saveBo";
				logger.warn("dispatchTasks() - No Actions found for ruleName: " + msgBean.getRuleName()
						+ ", ProcessBeanId [0]: " + processBeanId);
				TaskBaseBo bo = (TaskBaseBo) SpringUtil.getAppContext().getBean(processBeanId);
				bo.process(msgBean);
				return;
			}
			for (int i = 0; i < actions.size(); i++) {
				MsgActionVo msgActionVo = actions.get(i);
				TaskBaseBo bo = null;
				String processClassName = msgActionVo.getProcessClassName();
				if (StringUtils.isNotBlank(processClassName)) {
					// use process class
					logger.info("dispatchTasks() - ProcessClassName [" + i + "]: " + processClassName);
					try {
						bo = (TaskBaseBo) Class.forName(processClassName).newInstance();
					}
					catch (ClassNotFoundException e) {
						logger.error("ClassNotFoundException caught", e);
						throw new DataValidationException(e.getMessage());
					}
					catch (InstantiationException e) {
						logger.error("InstantiationException caught", e);
						throw new DataValidationException(e.getMessage());
					}
					catch (IllegalAccessException e) {
						logger.error("IllegalAccessException caught", e);
						throw new DataValidationException(e.getMessage());
					}
				}
				else { // use process bean
					String processBeanId = msgActionVo.getProcessBeanId();
					logger.info("dispatchTasks() - ProcessBeanId [" + i + "]: " + processBeanId);
					bo = (TaskBaseBo) SpringUtil.getAppContext().getBean(processBeanId);
				}
				/*
				 * retrieve arguments
				 */
				if (msgActionVo.getDataTypeValues() != null) {
					bo.setTaskArguments(msgActionVo.getDataTypeValues());
				}
				else {
					bo.setTaskArguments(null);
				}
				// TODO set queue name for jmsProcessor
				//bo.getJmsProcessor().setQueueName("");
				// invoke the processor
				bo.process(msgBean);
			}
			
			SpringUtil.commitTransaction();
			
		} catch (DataValidationException | MessagingException | JMSException e) {
			SpringUtil.rollbackTransaction();
			throw e;
		}
	}

	private void saveEmailAddr(Address[] addrs) {
		for (int i = 0; addrs != null && i < addrs.length; i++) {
			Address addr = addrs[i];
			if (addr != null && StringUtils.isNotBlank(addr.toString())) {
				emailAddrDao.findByAddress(addr.toString().trim());
			}
		}
	}
	
	private synchronized static Map<String, String> getMailSenderJndi() {
		if (mailSenderJndi != null) {
			return Collections.unmodifiableMap(mailSenderJndi);
		}
		mailSenderJndi = new Hashtable<String, String>();
		Properties props = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream("jndi.properties");
		try {
			props.load(is);
			Set<?> set = props.keySet();
			for (Iterator<?> it=set.iterator(); it.hasNext();) {
				String key = (String) it.next();
				mailSenderJndi.put(key, props.getProperty(key));
			}
			//mailSenderJndi.putAll(props);
			return Collections.unmodifiableMap(mailSenderJndi);
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void main(String[] args) {
		System.out.println(getMailSenderJndi());

		System.exit(0);
	}
}
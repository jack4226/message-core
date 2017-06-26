package ltj.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.constant.CarrierCode;
import ltj.message.constant.Constants;
import ltj.message.dao.client.ClientUtil;
import ltj.message.dao.servers.MailBoxDao;
import ltj.message.vo.MailBoxVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="mailInbox")
@javax.faces.bean.ViewScoped
public class MailInboxBean implements java.io.Serializable {
	private static final long serialVersionUID = 2069189605831996367L;
	static final Logger logger = Logger.getLogger(MailInboxBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();

	private transient MailBoxDao mailBoxDao = null;
	
	private transient DataModel<MailBoxVo> mailBoxes = null;
	private MailBoxVo mailbox = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput userIdInput = null;
	private transient UIInput hostNameInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "mailboxEdit.xhtml";
	private static String TO_FAILED = null;
	private static String TO_SAVED = "configureMailboxes.xhtml";
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public String refresh() {
		mailBoxes = null;
		return "";
	}
	
	public MailBoxDao getMailBoxDao() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (mailBoxDao == null) {
			mailBoxDao = SpringUtil.getWebAppContext().getBean(MailBoxDao.class);
		}
		return mailBoxDao;
	}

	public void setMailBoxDao(MailBoxDao mailBoxDao) {
		this.mailBoxDao = mailBoxDao;
	}
	
	public DataModel<MailBoxVo> getAll() {
		if (mailBoxes == null) {
			List<MailBoxVo> mailBoxList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				mailBoxList = getMailBoxDao().getAll(false);
			}
			else {
				mailBoxList = getMailBoxDao().getAll(false);
			}
			mailBoxes = new ListDataModel<MailBoxVo>(mailBoxList);
		}
		return mailBoxes;
	}

	public void viewMailBoxListener(AjaxBehaviorEvent event) {
		viewMailBox();
	}
	
	public String viewMailBox() {
		if (isDebugEnabled)
			logger.debug("viewMailBox() - Entering...");
		if (mailBoxes == null) {
			logger.warn("viewMailBox() - MailBox List is null.");
			return TO_FAILED;
		}
		if (!mailBoxes.isRowAvailable()) {
			logger.warn("viewMailBox() - MailBox Row not available.");
			return TO_FAILED;
		}
		reset();
		this.mailbox = mailBoxes.getRowData();
		if (isInfoEnabled) {
			logger.info("viewMailBox() - Mailbox to be edited: " + mailbox.getHostName() + ":" + mailbox.getUserId());
		}
		mailbox.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled)
			logger.debug("viewMailBox() - MailBoxVo to be passed to jsp: " + mailbox);
		
		return TO_EDIT;
	}
	
	public void saveMailboxListener(AjaxBehaviorEvent event) {
		saveMailbox();
//		try {
//			FacesContext.getCurrentInstance().getExternalContext().redirect(TO_SAVED);
//		}
//		catch (IOException e) {
//			logger.error("IOException caught during Faces redirect", e);
//		}
	}

	
	public String saveMailbox() {
		if (isDebugEnabled)
			logger.debug("saveMailbox() - Entering...");
		if (mailbox == null) {
			logger.warn("saveMailbox() - MailBoxVo is null.");
			return TO_FAILED;
		}
		reset();
		if (validatePrimaryKey(mailbox.getUserId(), mailbox.getHostName()) != null) {
			return TO_FAILED;
		}
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			mailbox.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getMailBoxDao().update(mailbox);
			logger.info("saveMailBox() - Rows Updated: " + 1);
		}
		else {
			getMailBoxDao().insert(mailbox);
			getMailBoxList().add(mailbox);
			logger.info("saveMailBox() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	public void deleteMailBoxesListener(AjaxBehaviorEvent event) {
		deleteMailBoxes();
	}

	public String deleteMailBoxes() {
		if (isDebugEnabled)
			logger.debug("deleteMailBoxes() - Entering...");
		if (mailBoxes == null) {
			logger.warn("deleteMailBoxes() - MailBox List is null.");
			return TO_FAILED;
		}
		reset();
		List<MailBoxVo> mboxList = getMailBoxList();
		for (int i=0; i<mboxList.size(); i++) {
			MailBoxVo vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMailBoxDao().deleteByPrimaryKey(vo.getUserId(), vo.getHostName());
				if (rowsDeleted > 0) {
					logger.info("deleteMailBoxes() - Mailbox deleted: " + vo.getUserId() + ":" + vo.getHostName());
				}
				vo.setMarkedForDeletion(false);
				mboxList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void testMailboxListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("testMailbox() - Entering...");
		if (mailbox == null) {
			logger.warn("testMailbox() - MailBoxVo is null.");
			testResult = "internalServerError";
			return;
		}
		Properties m_props = (Properties) System.getProperties().clone();
		Session session = null;
		// Get a Session object
		if (mailbox.getUseSsl()) {
			m_props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			m_props.setProperty("mail.pop3.socketFactory.fallback", "false");
			m_props.setProperty("mail.pop3.port", mailbox.getPortNumber()+"");
			m_props.setProperty("mail.pop3.socketFactory.port", mailbox.getPortNumber()+"");
			session = Session.getInstance(m_props);
		}
		else {
			session = Session.getInstance(m_props, null);
		}
		session.setDebug(true);
		Store store = null;
		try {
			// Get a Store object
			store = session.getStore(mailbox.getProtocol());
			// connect to the store
			store.connect(mailbox.getHostName(), mailbox.getPortNumber(), mailbox.getUserId(), mailbox.getUserPswd());
			
			testResult = "mailboxTestSuccess";
		}
		catch (MessagingException me) {
			logger.error("MessagingException caught: " + me.getMessage());
			testResult = "mailboxTestFailure";
		}
		finally {
			if (store != null) {
				try {
					store.close();
				}
				catch (Exception e) {
					logger.error("Exception caught: " + e.getMessage());
				}
			}
		}
		/* 
		 * Add to Face message queue. Works with h:message tag, for example:
		 * 
		 * <h:messages id="myMessage" globalOnly="true" showDetail="false"/>
		 * 
		 */
		FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
				"ltj.msgui.messages", testResult, null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
	
	public void copyMailboxListener(AjaxBehaviorEvent event) {
		copyMailbox();
	}
	
	public String copyMailbox() {
		if (isDebugEnabled)
			logger.debug("copyMailbox() - Entering...");
		if (mailBoxes == null) {
			logger.warn("copyMailbox() - MailBox List is null.");
			return TO_FAILED;
		}
		reset();
		List<MailBoxVo> mboxList = getMailBoxList();
		for (int i=0; i<mboxList.size(); i++) {
			MailBoxVo vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				mailbox = new MailBoxVo();
				try {
					vo.copyPropertiesTo(mailbox);
					mailbox.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
					setDefaultValues(mailbox);
				}
				mailbox.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public void addMailboxListener(AjaxBehaviorEvent event) {
		addMailbox();
	}
	
	public String addMailbox() {
		if (isDebugEnabled)
			logger.debug("addMailbox() - Entering...");
		reset();
		this.mailbox = new MailBoxVo();
		mailbox.setMarkedForEdition(true);
		mailbox.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mailbox.setUseSsl(false);
		mailbox.setToPlainText(false);
		mailbox.setReadPerPass(5);
		mailbox.setThreads(4);
		setDefaultValues(mailbox);
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT;
	}
	
	private void setDefaultValues(MailBoxVo mailbox) {
		// default values, not present on screen
		mailbox.setCheckDuplicate(true);
		mailbox.setAlertDuplicate(true);
		mailbox.setLogDuplicate(true);
		mailbox.setPurgeDupsAfter(24); // in hours
		mailbox.setCarrierCode(CarrierCode.SMTPMAIL.value());
		mailbox.setInternalOnly(false);
		mailbox.setMessageCount(-1);
	}
	
	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}
	
	public boolean getAnyMailBoxsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMailBoxsMarkedForDeletion() - Entering...");
		if (mailBoxes == null) {
			logger.warn("getAnyMailBoxsMarkedForDeletion() - MailBox List is null.");
			return false;
		}
		List<MailBoxVo> mboxList = getMailBoxList();
		for (Iterator<MailBoxVo> it=mboxList.iterator(); it.hasNext();) {
			MailBoxVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String userId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - UserId: " + userId);

		MailBoxVo vo = getMailBoxDao().getByPrimaryKey(mailbox.getUserId(), mailbox.getHostName());
		if (vo != null) {
			if (editMode == true && mailbox != null
					&& vo.getRowId() != mailbox.getRowId()) {
		        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
						"ltj.msgui.messages", "mailboxAlreadyExist", new String[] {userId});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
			else if (editMode == false) {
		        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
						"ltj.msgui.messages", "mailboxAlreadyExist", new String[] {userId});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	private String validatePrimaryKey(String userId, String hostName) {
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - hostName/userId: " + hostName+ "/" + userId);
		testResult = null;

		MailBoxVo vo = (MailBoxVo) getMailBoxDao().getByPrimaryKey(userId, hostName);
		if (vo != null) {
			if (editMode == true && vo.getRowId() != mailbox.getRowId()) {
				// mailbox does not exist
				testResult = "mailboxAlreadyExist";
			}
			else if (editMode == false) {
				// mailbox already exist
				testResult = "mailboxAlreadyExist";
			}
		}
		return testResult;
	}
	
	/**
	 * actionListener
	 * @param e
	 */
	public void actionFired(ActionEvent e) {
		logger.info("actionFired(ActionEvent) - " + e.getComponent().getId());
	}
	
	/**
	 * valueChangeEventListener
	 * @param e
	 */
	public void hostNameOrUserIdChanged(ValueChangeEvent e) {
		logger.info("hostNameOrUserIdChanged(ValueChangeEvent) - " + e.getComponent().getId()
				+ ": " + e.getOldValue() + " -> " + e.getNewValue());
		//FacesContext.getCurrentInstance().renderResponse();
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		userIdInput = null;
		hostNameInput = null;
	}
	
	@SuppressWarnings("unchecked")
	private List<MailBoxVo> getMailBoxList() {
		if (mailBoxes == null) {
			return new ArrayList<MailBoxVo>();
		}
		else {
			return (List<MailBoxVo>) mailBoxes.getWrappedData();
		}
	}
	
	public MailBoxVo getMailbox() {
		return mailbox;
	}

	public void setMailbox(MailBoxVo mailbox) {
		this.mailbox = mailbox;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public String getBeanMode() {
		return beanMode == null ? "" : beanMode.name();
	}

	public void setBeanMode(String beanMode) {
		try {
			this.beanMode = BeanMode.valueOf(beanMode);
		}
		catch (Exception e) {}
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}

	public UIInput getUserIdInput() {
		return userIdInput;
	}

	public void setUserIdInput(UIInput userIdInput) {
		this.userIdInput = userIdInput;
	}

	public UIInput getHostNameInput() {
		return hostNameInput;
	}

	public void setHostNameInput(UIInput hostNameInput) {
		this.hostNameInput = hostNameInput;
	}
}

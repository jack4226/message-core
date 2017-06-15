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
import javax.mail.Transport;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.constant.CodeType;
import ltj.message.constant.Constants;
import ltj.message.constant.MailServerType;
import ltj.message.dao.client.ClientUtil;
import ltj.message.dao.smtp.SmtpServerDao;
import ltj.message.vo.SmtpConnVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="smtpServer")
@javax.faces.bean.ViewScoped
public class SmtpServerBean implements java.io.Serializable {
	private static final long serialVersionUID = -2610108607170535587L;
	static final Logger logger = Logger.getLogger(SmtpServerBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient SmtpServerDao smtpServerDao = null;
	
	private transient DataModel<SmtpConnVo> smtpServers = null;
	private SmtpConnVo smtpServer = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput serverNameInput = null;
	private transient UIInput useSslInput = null;
	private transient UIInput useAuthInput = null;

	private String testResult = null;
	private String actionFailure = null;

	private static String TO_EDIT = "smtpServerEdit.xhtml";
	private static String TO_FAILED = null;
	private static String TO_SAVED = "configureSmtpServers.xhtml";
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public DataModel<SmtpConnVo> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (smtpServers == null) {
			List<SmtpConnVo> smtpServerList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				smtpServerList = getSmtpServerDao().getAll(false);
			}
			else {
				smtpServerList = getSmtpServerDao().getAll(false);
			}
			smtpServers = new ListDataModel<SmtpConnVo>(smtpServerList);
		}
		return smtpServers;
	}

	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public String refresh() {
		smtpServers = null;
		return "";
	}
	
	public SmtpServerDao getSmtpServerDao() {
		if (smtpServerDao == null) {
			smtpServerDao = SpringUtil.getWebAppContext().getBean(SmtpServerDao.class);
		}
		return smtpServerDao;
	}

	public void setSmtpServerDao(SmtpServerDao smtpServerDao) {
		this.smtpServerDao = smtpServerDao;
	}
	
	public void viewSmtpServerListener(AjaxBehaviorEvent event) {
		viewSmtpServer();
	}
	
	public String viewSmtpServer() {
		if (isDebugEnabled)
			logger.debug("viewSmtpServer() - Entering...");
		if (smtpServers == null) {
			logger.warn("viewSmtpServer() - SmtpServer List is null.");
			return TO_SAVED;
		}
		if (!smtpServers.isRowAvailable()) {
			logger.warn("viewSmtpServer() - SmtpServer Row not available.");
			return TO_FAILED;
		}
		reset();
		this.smtpServer = smtpServers.getRowData();
		logger.info("viewSmtpServer() - SmtpServer to be edited: " + smtpServer.getSmtpHost());
		smtpServer.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled) {
			logger.debug("viewSmtpServer() - SmtpServer to be passed to jsp: " + smtpServer);
		}
		return TO_EDIT;
	}
	
	public void saveSmtpServerListener(AjaxBehaviorEvent event) {
		saveSmtpServer();
	}
	
	public String saveSmtpServer() {
		if (isDebugEnabled)
			logger.debug("saveSmtpServer() - Entering...");
		if (smtpServer == null) {
			logger.warn("saveSmtpServer() - SmtpServer is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			smtpServer.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getSmtpServerDao().update(smtpServer);
			logger.info("saveSmtpServer() - Rows Updated: " + 1);
		}
		else {
			getSmtpServerDao().insert(smtpServer);
			getSmtpServerList().add(smtpServer);
			logger.info("saveSmtpServer() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	public void deleteSmtpServersListener(AjaxBehaviorEvent event) {
		deleteSmtpServers();
	}
	
	public String deleteSmtpServers() {
		if (isDebugEnabled)
			logger.debug("deleteSmtpServers() - Entering...");
		if (smtpServers == null) {
			logger.warn("deleteSmtpServers() - SmtpServer List is null.");
			return TO_FAILED;
		}
		reset();
		List<SmtpConnVo> smtpList = getSmtpServerList();
		for (int i=0; i<smtpList.size(); i++) {
			SmtpConnVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSmtpServerDao().deleteByServerName(vo.getServerName());
				if (rowsDeleted > 0) {
					logger.info("SmtpServer deleted: " + vo.getServerName());
				}
				vo.setMarkedForDeletion(false);
				smtpList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void testSmtpServerListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("testSmtpServer() - Entering...");
		if (smtpServer == null) {
			logger.warn("testSmtpServer() - SmtpServer is null.");
			testResult = "internalServerError";
			return;
		}
		String smtpHost = smtpServer.getSmtpHost();
		int smtpPort = smtpServer.getSmtpPort();
		String userId = smtpServer.getUserId();
		String password = smtpServer.getUserPswd();
		
		Session session = null;
		Properties sys_props = (Properties) System.getProperties().clone();
		sys_props.put("mail.smtp.host", smtpHost);
		String protocol = null;
		if (smtpServer.getUseSsl()) {
			sys_props.put("mail.smtps.auth", "true");
			sys_props.put("mail.user", userId);
			protocol = MailServerType.SMTPS.value();
		}
		else {
			protocol = MailServerType.SMTP.value();
		}
		sys_props.put("mail.host", smtpHost);
		sys_props.put("mail.smtp.connectiontimeout", "2000");
			// socket connection timeout value in milliseconds
		sys_props.put("mail.smtp.timeout", "2000");
			// socket I/O timeout value in milliseconds

		// Get a Session object
		session = Session.getInstance(sys_props);
		session.setDebug(true);
		Transport transport = null;
		try {
			transport = session.getTransport(protocol);
			if (smtpPort > 0) {
				transport.connect(smtpHost, smtpPort, userId, password);
			}
			else {
				transport.connect(smtpHost, userId, password);
			}
			testResult = "smtpServerTestSuccess";
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught: " + e.getMessage());
			testResult = "smtpServerTestFailure";
		}
		finally {
			if (transport !=null) {
				try {
					transport.close();
				}
				catch (MessagingException e) {
					logger.error("MessagingException caught: " + e.getMessage());
				}
			}
		}
	}
	
	public void copySmtpServerListener(AjaxBehaviorEvent event) {
		copySmtpServer();
	}
	
	public String copySmtpServer() {
		if (isDebugEnabled)
			logger.debug("copySmtpServer() - Entering...");
		if (smtpServers == null) {
			logger.warn("copySmtpServer() - SmtpServer List is null.");
			return TO_FAILED;
		}
		reset();
		List<SmtpConnVo> smtpList = getSmtpServerList();
		for (int i = 0; i < smtpList.size(); i++) {
			SmtpConnVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.smtpServer = new SmtpConnVo();
				try {
					vo.copyPropertiesTo(this.smtpServer);
					smtpServer.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				smtpServer.setServerName(null);
				smtpServer.setMarkedForEdition(true);
				setDefaultValues(smtpServer);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public void addSmtpServerListener(AjaxBehaviorEvent event) {
		addSmtpServer();
	}
	
	public String addSmtpServer() {
		if (isDebugEnabled)
			logger.debug("addSmtpServer() - Entering...");
		reset();
		this.smtpServer = new SmtpConnVo();
		smtpServer.setMarkedForEdition(true);
		smtpServer.setUpdtUserId(Constants.DEFAULT_USER_ID);
		smtpServer.setUseSsl(false);
		setDefaultValues(smtpServer);
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT;
	}
	
	void setDefaultValues(SmtpConnVo smtpServer) {
		smtpServer.setRetryFreq(10); // in seconds
		smtpServer.setMessageCount(0);
		smtpServer.setAlertAfter(5);
		smtpServer.setAlertLevel("error");
	}
	
	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		refresh();
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}
	
	public boolean getAnyServersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyServersMarkedForDeletion() - Entering...");
		if (smtpServers == null) {
			logger.warn("getAnyServersMarkedForDeletion() - SmtpServer List is null.");
			return false;
		}
		List<SmtpConnVo> smtpList = getSmtpServerList();
		for (Iterator<SmtpConnVo> it=smtpList.iterator(); it.hasNext();) {
			SmtpConnVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * validate primary key
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String serverName = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - serverName: " + serverName);
		SmtpConnVo vo = getSmtpServerDao().getByServerName(serverName);
		if (editMode == false && vo != null) {
			// smtpServer already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "smtpServerAlreadyExist", new String[] {serverName});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}

		if (editMode == true && vo == null) {
			// smtpServer does not exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "smtpServerDoesNotExist", new String[] {serverName});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public boolean getIsUseSslInput() {
		if (useSslInput != null) {
			if (useSslInput.getLocalValue() != null) {
				return CodeType.Yes.value().equals(useSslInput.getLocalValue());
			}
			else if (useSslInput.getValue() != null) {
				return CodeType.Yes.value().equals(useSslInput.getValue());
			}
		}
		return true; // for safety
	}
	
	public boolean getIsUseAuthInput() {
		if (useAuthInput != null) {
			if (useAuthInput.getLocalValue() != null) {
				return CodeType.Yes.value().equals(useAuthInput.getLocalValue());
			}
			else if (useAuthInput.getValue() != null) {
				return CodeType.Yes.value().equals(useAuthInput.getValue());
			}
		}
		return true; // for safety
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
	public void fieldValueChanged(ValueChangeEvent e) {
		if (isDebugEnabled) {
			logger.debug("fieldValueChanged(ValueChangeEvent) - " + e.getComponent().getId()
					+ ": " + e.getOldValue() + " -> " + e.getNewValue());
		}
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		serverNameInput = null;
		useSslInput = null;
		useAuthInput = null;
	}
	
	@SuppressWarnings("unchecked")
	private List<SmtpConnVo> getSmtpServerList() {
		if (smtpServers == null) {
			return new ArrayList<SmtpConnVo>();
		}
		else {
			return (List<SmtpConnVo>) smtpServers.getWrappedData();
		}
	}
	
	public SmtpConnVo getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(SmtpConnVo smtpServer) {
		this.smtpServer = smtpServer;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public UIInput getServerNameInput() {
		return serverNameInput;
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

	public void setServerNameInput(UIInput smtpHostInput) {
		this.serverNameInput = smtpHostInput;
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

	public UIInput getUseSslInput() {
		return useSslInput;
	}

	public void setUseSslInput(UIInput useSslInput) {
		this.useSslInput = useSslInput;
	}

	public UIInput getUseAuthInput() {
		return useAuthInput;
	}

	public void setUseAuthInput(UIInput useAuthInput) {
		this.useAuthInput = useAuthInput;
	}
}

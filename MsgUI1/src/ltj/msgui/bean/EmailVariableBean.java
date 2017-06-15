package ltj.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.bo.template.RenderUtil;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.exception.DataValidationException;
import ltj.message.external.VariableResolver;
import ltj.message.util.TestUtil;
import ltj.message.vo.emailaddr.EmailVariableVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="emailVariable")
@javax.faces.bean.ViewScoped
public class EmailVariableBean implements java.io.Serializable {
	private static final long serialVersionUID = 8620743959575480890L;
	static final Logger logger = Logger.getLogger(EmailVariableBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient EmailVariableDao emailVariableDao = null;
	private transient DataModel<EmailVariableVo> emailVariables = null;
	
	private EmailVariableVo emailVariable = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput variableNameInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	final static String TO_EDIT = "emailVariableEdit.xhtml";
	final static String TO_SELF = null;
	final static String TO_SAVED = "configureEmailVariables.xhtml";
	final static String TO_FAILED = null;
	final static String TO_DELETED = TO_SAVED;
	final static String TO_CANCELED = TO_SAVED;
	
	public DataModel<EmailVariableVo> getAll() {
		if (emailVariables == null) {
			List<EmailVariableVo> emailVariableList = null;
//			if (!jpa.util.SenderUtil.isProductKeyValid() && jpa.util.SenderUtil.isTrialPeriodEnded()) {
//				emailVariableList = getEmailVariableDao().getAll();
//			}
//			else {
				emailVariableList = getEmailVariableDao().getAll();
//			}
			emailVariables = new ListDataModel<EmailVariableVo>(emailVariableList);
		}
		return emailVariables;
	}

	public String refresh() {
		emailVariables = null;
		return TO_SELF;
	}
	
	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public EmailVariableDao getEmailVariableDao() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (emailVariableDao == null) {
			emailVariableDao = SpringUtil.getWebAppContext().getBean(EmailVariableDao.class);
		}
		return emailVariableDao;
	}

	public void setEmailVariableDao(EmailVariableDao emailVariableDao) {
		this.emailVariableDao = emailVariableDao;
	}
	
	public void viewEmailVariableListener(AjaxBehaviorEvent event) {
		viewEmailVariable();
	}
	
	public String viewEmailVariable() {
		if (isDebugEnabled)
			logger.debug("viewEmailVariable() - Entering...");
		if (emailVariables == null) {
			logger.warn("viewEmailVariable() - EmailVariable List is null.");
			return TO_FAILED;
		}
		if (!emailVariables.isRowAvailable()) {
			logger.warn("viewEmailVariable() - EmailVariable Row not available.");
			return TO_FAILED;
		}
		reset();
		this.emailVariable = emailVariables.getRowData();
		logger.info("viewEmailVariable() - EmailVariable to be edited: " + emailVariable.getVariableName());
		emailVariable.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled) {
			logger.debug("viewEmailVariable() - EmailVariable to be passed to jsp: " + emailVariable);
		}
		return TO_EDIT;
	}
	
	public void testEmailVariableListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("testEmailVariable() - Entering...");
		if (emailVariable == null) {
			logger.warn("testEmailVariable() - EmailVariable is null.");
			return; // TO_FAILED;
		}
		isQueryValid();
		//return TO_SELF;
	}
	
	private boolean isQueryValid() {
		String query = emailVariable.getVariableQuery();
		if (query == null || query.trim().length() == 0) {
			testResult = "variableQueryIsBlank";
			return true;
		}
		else {
			try {
				getEmailVariableDao().getByQuery(query, 1);
				testResult = "variableQueryTestSuccess";
				return true;
			}
			catch (Exception e) {
				logger.error("Exception caught: " + e.getMessage());
				testResult = "variableQueryTestFailure";
				return false;
			}
			finally {
			}
		}
	}
	
	public void saveEmailVariableListener(AjaxBehaviorEvent event) {
		saveEmailVariable();
	}
	
	public String saveEmailVariable() {
		if (isDebugEnabled)
			logger.debug("saveEmailVariable() - Entering...");
		if (emailVariable == null) {
			logger.warn("saveEmailVariable() - EmailVariable is null.");
			return TO_FAILED;
		}
		reset();
		// validate user input
		if (isQueryValid() == false) {
			return TO_SELF;
		}
		String className = emailVariable.getVariableProc();
		if (className != null && className.trim().length() > 0) {
			Class<?> proc = null;
			try {
				proc = Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException caught: " + e.getMessage());
				testResult = "variableClassNotFound";
				return TO_SELF;
			}
			try {
				Object obj = proc.newInstance();
				if (!(obj instanceof VariableResolver)) {
					throw new Exception("Variable class is not a VariableResolver");
				}
			}
			catch (Exception e) {
				logger.error("Exception caught: " + e.getMessage());
				testResult = "variableClassNotValid";
				return TO_SELF;
			}
		}
		// validate variable loops
		try {
			RenderUtil.checkVariableLoop(emailVariable.getDefaultValue(), emailVariable.getVariableName());
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught: " + e.getMessage());
			actionFailure = e.getMessage();
			return TO_SELF;
		}
		// end of validate
		// update database
		if (TestUtil.isRunningInJunitTest() == false) {
			if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
				emailVariable.setUpdtUserId(FacesUtil.getLoginUserId());
			}
		}
		if (editMode == true) {
			getEmailVariableDao().update(emailVariable);
			logger.info("saveEmailVariable() - Rows Updated: " + 1);
		}
		else {
			getEmailVariableDao().insert(emailVariable);
			addToList(emailVariable);
			logger.info("saveEmailVariable() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	private void addToList(EmailVariableVo vo) {
		getEmailVariableList().add(vo);
	}
	
	public void deleteEmailVariablesListener(AjaxBehaviorEvent event) {
		deleteEmailVariables();
	}

	public String deleteEmailVariables() {
		if (isDebugEnabled)
			logger.debug("deleteEmailVariables() - Entering...");
		if (emailVariables == null) {
			logger.warn("deleteEmailVariables() - EmailVariable List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailVariableVo> evList = getEmailVariableList();
		for (int i = 0; i < evList.size(); i++) {
			EmailVariableVo vo = evList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailVariableDao().deleteByName(vo.getVariableName());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailVariables() - EmailVariable deleted: " + vo.getVariableName());
				}
				evList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void copyEmailVariableListener(AjaxBehaviorEvent event) {
		copyEmailVariable();
	}
	
	public String copyEmailVariable() {
		if (isDebugEnabled)
			logger.debug("copyEmailVariable() - Entering...");
		if (emailVariables == null) {
			logger.warn("copyEmailVariable() - EmailVariable List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailVariableVo> smtpList = getEmailVariableList();
		for (int i = 0; i < smtpList.size(); i++) {
			EmailVariableVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.emailVariable = new EmailVariableVo();
				try {
					vo.copyPropertiesTo(this.emailVariable);
					emailVariable.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				emailVariable.setVariableName(null);
				emailVariable.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public void addEmailVariableListener(AjaxBehaviorEvent event) {
		addEmailVariable();
	}
	
	public String addEmailVariable() {
		if (isDebugEnabled)
			logger.debug("addEmailVariable() - Entering...");
		reset();
		this.emailVariable = new EmailVariableVo();
		emailVariable.setMarkedForEdition(true);
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT;
	}
	
	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		refresh();
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}
	
	public boolean getAnyListsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyListsMarkedForDeletion() - Entering...");
		if (emailVariables == null) {
			logger.warn("getAnyListsMarkedForDeletion() - EmailVariable List is null.");
			return false;
		}
		List<EmailVariableVo> smtpList = getEmailVariableList();
		for (Iterator<EmailVariableVo> it = smtpList.iterator(); it.hasNext();) {
			EmailVariableVo vo = it.next();
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
		String variableName = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - variableName: " + variableName);
		EmailVariableVo vo = getEmailVariableDao().getByName(variableName);
		if (editMode == false && vo != null) {
			// emailVariable already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "emailVariableAlreadyExist", new String[] {variableName});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (vo == null && editMode == true) {
			// emailVariable does not exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "emailVariableDoesNotExist", new String[] {variableName});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
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
		variableNameInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<EmailVariableVo> getEmailVariableList() {
		if (emailVariables == null) {
			return new ArrayList<EmailVariableVo>();
		}
		else {
			return (List<EmailVariableVo>)emailVariables.getWrappedData();
		}
	}
	
	public EmailVariableVo getEmailVariable() {
		if (emailVariable == null && emailVariables != null) {
			if (emailVariables.isRowAvailable()) {
				emailVariable = emailVariables.getRowData();
			}
		}
		return emailVariable;
	}

	public void setEmailVariable(EmailVariableVo emailVariable) {
		this.emailVariable = emailVariable;
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

	public UIInput getVariableNameInput() {
		return variableNameInput;
	}

	public void setVariableNameInput(UIInput variableNameInput) {
		this.variableNameInput = variableNameInput;
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
}

package ltj.msgui.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.bo.template.RenderUtil;
import ltj.message.constant.Constants;
import ltj.message.dao.client.ClientDao;
import ltj.message.dao.client.ClientUtil;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.SchedulesBlob;
import ltj.message.exception.DataValidationException;
import ltj.message.util.BlobUtil;
import ltj.message.util.HtmlTags;
import ltj.message.vo.ClientVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.message.vo.emailaddr.EmailVariableVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.msgui.util.DynamicCodes;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="emailTemplate")
@javax.faces.bean.ViewScoped
public class EmailTemplateBean implements java.io.Serializable {
	private static final long serialVersionUID = -4812680785383460662L;
	static final Logger logger = Logger.getLogger(EmailTemplateBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient EmailTemplateDao emailTemplateDao = null;
	private transient EmailVariableDao emailVariableDao = null;
	private transient MailingListDao mailingListDao = null;
	private transient ClientDao senderDataDao = null;
	
	private transient DataModel<EmailTemplateVo> emailTemplates = null;
	
	private EmailTemplateVo emailTemplate = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient DataModel<Object> dateList = null;
	
	private transient UIInput templateIdInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	final static String TO_EDIT = "emailTemplateEdit.xhtml";
	final static String TO_SAVED = "configureEmailTemplates.xhtml";
	final static String TO_DELETED = TO_SAVED;
	final static String TO_CANCELED = TO_SAVED;
	final static String TO_SELF = null;
	final static String TO_FAILED = null;
	final static String TO_SCHEDULE_EDIT = "emailSchedulesEdit.xhtml";
	final static String TO_SCHEDULE_SAVED = TO_SAVED;
	
	public DataModel<EmailTemplateVo> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (emailTemplates == null) {
			List<EmailTemplateVo> emailTemplateList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				emailTemplateList = getEmailTemplateDao().getAll();
			}
			else {
				emailTemplateList = getEmailTemplateDao().getAll();
			}
			emailTemplates = new ListDataModel<EmailTemplateVo>(emailTemplateList);
		}
		return emailTemplates;
	}

	public void refreshListener(AjaxBehaviorEvent e) {
		refresh();
	}
	
	public String refresh() {
		emailTemplates = null;
		return TO_SELF;
	}
	
	public EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = SpringUtil.getWebAppContext().getBean(EmailTemplateDao.class);
		}
		return emailTemplateDao;
	}

	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}
	
	public EmailVariableDao getEmailVariableDao() {
		if (emailVariableDao == null) {
			emailVariableDao = SpringUtil.getWebAppContext().getBean(EmailVariableDao.class);
		}
		return emailVariableDao;
	}

	public MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = SpringUtil.getWebAppContext().getBean(MailingListDao.class);
		}
		return mailingListDao;
	}
	
	public ClientDao getClientDao() {
		if (senderDataDao == null) {
			senderDataDao = SpringUtil.getWebAppContext().getBean(ClientDao.class);
		}
		return senderDataDao;
	}
	
	public void viewEmailTemplateListener(AjaxBehaviorEvent e) {
		viewEmailTemplate();
	}
	
	public String viewEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("viewEmailTemplate() - Entering...");
		if (emailTemplates == null) {
			logger.warn("viewEmailTemplate() - EmailTemplate List is null.");
			return TO_FAILED;
		}
		if (!emailTemplates.isRowAvailable()) {
			logger.warn("viewEmailTemplate() - EmailTemplate Row not available.");
			return TO_FAILED;
		}
		reset();
		this.emailTemplate = emailTemplates.getRowData();
		logger.info("viewEmailTemplate() - EmailTemplate to be edited: " + emailTemplate.getTemplateId());
		emailTemplate.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled) {
			logger.debug("viewEmailTemplate() - EmailTemplate to be passed to jsp: " + emailTemplate);
		}
		return TO_EDIT;
	}

	private void checkVariableLoop(String text) throws DataValidationException {
		List<String> varNames = RenderUtil.retrieveVariableNames(text);
		for (String loopName : varNames) {
			EmailVariableVo vo = getEmailVariableDao().getByName(loopName);
			if (vo != null) {
				RenderUtil.checkVariableLoop(vo.getDefaultValue(), loopName);
			}
		}
	}

	public void saveEmailTemplateListener(AjaxBehaviorEvent e) {
		saveEmailTemplate();
	}
	
	public String saveEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("saveEmailTemplate() - Entering...");
		if (emailTemplate == null) {
			logger.warn("saveEmailTemplate() - EmailTemplate is null.");
			return TO_FAILED;
		}
		reset();
		// validate templates
		try {
			checkVariableLoop(emailTemplate.getBodyText());
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught: " + e.getMessage());
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		try {
			checkVariableLoop(emailTemplate.getSubject());
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught: " + e.getMessage());
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			emailTemplate.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (!emailTemplate.getIsHtml()) { // make sure HTML is set correctly
			emailTemplate.setIsHtml(HtmlTags.isHTML(emailTemplate.getBodyText()));
		}
		if (editMode == true) {
			getEmailTemplateDao().update(emailTemplate);
			logger.info("saveEmailTemplate() - Rows Updated: " + 1);
		}
		else { // insert
			MailingListVo mlist = getMailingListDao().getByListId(emailTemplate.getListId());
			emailTemplate.setListId(mlist.getListId());
			getEmailTemplateDao().insert(emailTemplate);
			getEmailTemplateList().add(emailTemplate);
			refresh();
			logger.info("saveEmailTemplate() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	public void editSchedulesListener(AjaxBehaviorEvent event) {
		editSchedules();
	}

	public String editSchedules() {
		if (isDebugEnabled)
			logger.debug("editSchedules() - Entering...");
		this.emailTemplate = emailTemplates.getRowData();
		dateList = new ArrayDataModel<Object>(emailTemplate.getSchedulesBlob().getDateList());
		beanMode = BeanMode.schedule;
		return TO_SCHEDULE_EDIT;
	}
	
	public void saveSchedulesListener(AjaxBehaviorEvent event) {
		saveSchedules();
	}
	
	public String saveSchedules() {
		if (isDebugEnabled)
			logger.debug("saveSchedules() - Entering...");
		getEmailTemplateDao().update(emailTemplate);
		if (isDebugEnabled)
			logger.debug("saveSchedules() - rows updated: " + 1);
		beanMode = BeanMode.list;
		return TO_SCHEDULE_SAVED;
	}
	
	public void deleteEmailTemplatesListener(AjaxBehaviorEvent event) {
		deleteEmailTemplates();
	}
	
	public String deleteEmailTemplates() {
		if (isDebugEnabled)
			logger.debug("deleteEmailTemplates() - Entering...");
		if (emailTemplates == null) {
			logger.warn("deleteEmailTemplates() - EmailTemplate List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailTemplateVo> smtpList = getEmailTemplateList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailTemplateVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailTemplateDao().deleteByTemplateId(vo.getTemplateId());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailTemplates() - EmailTemplate deleted: " + vo.getTemplateId());
				}
				smtpList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void copyEmailTemplateListener(AjaxBehaviorEvent e) {
		copyEmailTemplate();
	}
	
	public String copyEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("copyEmailTemplate() - Entering...");
		if (emailTemplates == null) {
			logger.warn("copyEmailTemplate() - EmailTemplate List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailTemplateVo> smtpList = getEmailTemplateList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailTemplateVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) { // template to copy from
				emailTemplate = new EmailTemplateVo();
				try {
					vo.copyPropertiesTo(this.emailTemplate);
					emailTemplate.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
					if (emailTemplate.getClientId() == null) {
						emailTemplate.setClientId(vo.getClientId());
					}
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				emailTemplate.setTemplateId(null);
				emailTemplate.setSchedulesBlob((SchedulesBlob)BlobUtil.deepCopy(vo.getSchedulesBlob()));
				emailTemplate.getSchedulesBlob().reset();
				emailTemplate.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public void addEmailTemplateListener(AjaxBehaviorEvent e) {
		addEmailTemplate();
	}
	
	public String addEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("addEmailTemplate() - Entering...");
		reset();
		this.emailTemplate = new EmailTemplateVo();
		MailingListVo mlist = new MailingListVo();
		emailTemplate.setListId(mlist.getListId());
		ClientVo sender = getClientDao().getByClientId(Constants.DEFAULT_CLIENTID);
		emailTemplate.setClientId(sender.getClientId());
		emailTemplate.setSchedulesBlob(new SchedulesBlob());
		emailTemplate.setMarkedForEdition(true);
		emailTemplate.setSubject("");
		emailTemplate.setBodyText("");
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT;
	}
	
	public void cancelEditListener(AjaxBehaviorEvent e) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		refresh();
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}
	
	public boolean getAnyTemplatesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyTemplatesMarkedForDeletion() - Entering...");
		if (emailTemplates == null) {
			logger.warn("getAnyTemplatesMarkedForDeletion() - EmailTemplate List is null.");
			return false;
		}
		List<EmailTemplateVo> smtpList = getEmailTemplateList();
		for (Iterator<EmailTemplateVo> it=smtpList.iterator(); it.hasNext();) {
			EmailTemplateVo vo = it.next();
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
		String templateId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - templateId: " + templateId);
		EmailTemplateVo vo = getEmailTemplateDao().getByTemplateId(templateId);
		if (editMode == true && vo != null && emailTemplate != null && vo.getRowId() != emailTemplate.getRowId()) {
			// emailTemplate already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
	        		"ltj.msgui.messages", "emailTemplateAlreadyExist", new String[] {templateId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// emailTemplate already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "emailTemplateAlreadyExist", new String[] {templateId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		if (editMode == true && vo == null) {
			// emailTemplate does not exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "emailTemplateDoesNotExist", new String[] {templateId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void checkDate(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("checkDate() - date = " + value);
		if (value == null) return;
		if (value instanceof Date) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime((Date)value);
			return;
		}
		((UIInput)component).setValid(false);
		FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
				"ltj.msgui.messages", "invalidDate", new Object[] {value});
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		context.addMessage(component.getClientId(context), message);
	}
	
	/**
	 * actionListener
	 * @param e
	 */
	public void actionFired(ActionEvent e) {
		logger.info("actionFired(ActionEvent) - " + e.getComponent().getId());
	}
	
	/**
	 * @deprecated - replaced by ajax event
	 * valueChangeEventListener
	 * Example JSF tag: valueChangeListener="#{emailTemplate.fieldValueChanged}"
	 * @param e
	 */
	public void fieldValueChanged(ValueChangeEvent e) {
		logger.info("fieldValueChanged(ValueChangeEvent) - " + e.getComponent().getId() 
				+ ": " + e.getOldValue() + " -> " + e.getNewValue());
		if (emailTemplate != null) {
			emailTemplate.setListType((String)e.getNewValue());
		}
		FacesContext.getCurrentInstance().renderResponse();
	}
	
	public SelectItem[] getEmailVariables() {
		DynamicCodes codes = (DynamicCodes) FacesUtil.getApplicationMapValue("dynacodes");
		SelectItem[] codes1 = codes.getEmailVariableNameItems();
		SelectItem[] codes2 = codes.getGlobalVariableNameItems();
		if (emailTemplate == null) {
			SelectItem[] codesAll = new SelectItem[codes1.length + codes2.length];
			System.arraycopy(codes1, 0, codesAll, 0, codes1.length);
			System.arraycopy(codes2, 0, codesAll, codes1.length, codes2.length);
			return codesAll;
		}
		else {
			return codes2;
		}
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		templateIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<EmailTemplateVo> getEmailTemplateList() {
		if (emailTemplates == null) {
			return new ArrayList<EmailTemplateVo>();
		}
		else {
			return (List<EmailTemplateVo>)emailTemplates.getWrappedData();
		}
	}
	
	public EmailTemplateVo getEmailTemplate() {
		return emailTemplate;
	}

	public void setEmailTemplate(EmailTemplateVo emailTemplate) {
		this.emailTemplate = emailTemplate;
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

	public UIInput getTemplateIdInput() {
		return templateIdInput;
	}

	public void setTemplateIdInput(UIInput templateIdInput) {
		this.templateIdInput = templateIdInput;
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

	public DataModel<Object> getDateList() {
		return dateList;
	}

	public void setDateList(DataModel<Object> schedulesBlob) {
		this.dateList = schedulesBlob;
	}
}

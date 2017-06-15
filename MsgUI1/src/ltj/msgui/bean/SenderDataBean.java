package ltj.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.constant.CodeType;
import ltj.message.constant.Constants;
import ltj.message.dao.client.ClientDao;
import ltj.message.dao.client.ClientUtil;
import ltj.message.util.EmailAddrUtil;
import ltj.message.vo.ClientVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="senderData")
@javax.faces.bean.ViewScoped
public class SenderDataBean implements java.io.Serializable {
	private static final long serialVersionUID = 1121882547043576165L;
	static final Logger logger = Logger.getLogger(SenderDataBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient ClientDao senderDao = null;
	
	private transient DataModel<ClientVo> siteProfiles = null;
	private ClientVo sender = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput senderIdInput = null;
	private transient UIInput returnPathLeftInput = null;
	private transient UIInput verpEnabledInput = null;
	private transient UIInput useTestAddrInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "emailProfileEdit.xhtml";
	private static String TO_SAVED = "configureSiteProfiles.xhtml";
	private static String TO_FAILED = null;
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;
	
	private final ClientVo siteMeta = new ClientVo();
	
	//public SenderDataBean() {
	//	getData();
	//}
	
	public DataModel<ClientVo> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (siteProfiles == null) {
			List<ClientVo> senderList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				senderList = getClientDao().getAll();
			}
			else {
				senderList = getClientDao().getAll();
			}
			List<ClientVo> systemAtTop = new ArrayList<ClientVo>();
			for (int i = 0; i < senderList.size(); i++) {
				ClientVo vo = senderList.get(i);
				if (Constants.DEFAULT_CLIENTID.equals(vo.getClientId())) {
					systemAtTop.add(vo);
					senderList.remove(i);
				}
			}
			systemAtTop.addAll(senderList);
			siteProfiles = new ListDataModel<ClientVo>(systemAtTop);
		}
		return siteProfiles;
	}
	
	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public String refresh() {
		siteProfiles = null;
		return "";
	}
	
	public void refreshSenderListener(AjaxBehaviorEvent event) {
		refreshSender();
	}
	
	public String refreshSender() {
		if (siteProfiles != null && siteProfiles.isRowAvailable()) {
			ClientVo sd = siteProfiles.getRowData();
			if (sd != null) {
				sender = getClientDao().getByClientId(sd.getClientId());
			}
		}
		reset();
		//FacesUtil.refreshCurrentJSFPage();
		return null;
	}
	
	public ClientDao getClientDao() {
		if (senderDao == null) {
			senderDao = SpringUtil.getWebAppContext().getBean(ClientDao.class);
		}
		return senderDao;
	}

	public void setClientDao(ClientDao senderDao) {
		this.senderDao = senderDao;
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		senderIdInput = null;
		returnPathLeftInput = null;
		verpEnabledInput = null;
		useTestAddrInput = null;
	}
	
	public void viewSiteProfileListener(AjaxBehaviorEvent event) {
		viewSiteProfile();
	}
	
	public String viewSiteProfile() {
		if (isDebugEnabled)
			logger.debug("viewSiteProfile() - Entering...");
		if (siteProfiles == null) {
			logger.warn("viewSiteProfile() - SiteProfile List is null.");
			return TO_FAILED;
		}
		if (!siteProfiles.isRowAvailable()) {
			logger.warn("viewMailingList() - SiteProfile Row not available.");
			return TO_FAILED;
		}
		reset();
		this.sender = (ClientVo) siteProfiles.getRowData();
		logger.info("viewSiteProfile() - Site to be edited: " + sender.getClientId());
		sender.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled)
			logger.debug("viewSiteProfile() - ClientVo to be passed to jsp: " + sender);
		
		return TO_EDIT;
	}
	
	public void saveSenderListener(AjaxBehaviorEvent event) {
		saveSender();
//		try {
//			FacesContext.getCurrentInstance().getExternalContext().redirect(TO_SAVED);
//		}
//		catch (IOException e) {
//			logger.error("IOException caught during Faces redirect", e);
//		}
	}

	public String saveSender() {
		if (isDebugEnabled)
			logger.debug("saveSender() - Entering...");
		if (sender == null) {
			logger.warn("saveSender() - ClientVo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			sender.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getClientDao().update(sender);
			logger.info("saveSender() - Rows Updated: " + 1);
		}
		else {
			getClientDao().insert(sender);
			getSiteProfilesList().add(sender);
			logger.info("saveSender() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	public void deleteSiteProfilesListener(AjaxBehaviorEvent event) {
		logger.info("deleteSiteProfilesListener() - event source: " + event.getSource());
		deleteSiteProfiles();
	}
	
	public String deleteSiteProfiles() {
		if (isDebugEnabled)
			logger.debug("deleteSiteProfiles() - Entering...");
		if (siteProfiles == null) {
			logger.warn("deleteSiteProfiles - SiteProfile List is null.");
			return TO_FAILED;
		}
		reset();
		List<ClientVo> list = getSiteProfilesList();
		for (int i=0; i<list.size(); i++) {
			ClientVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getClientDao().delete(vo.getClientId());
				if (rowsDeleted > 0) {
					logger.info("deleteSiteProfiles() - Sender deleted: " + vo.getClientId());
				}
				list.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void copySiteProfileListener(AjaxBehaviorEvent event) {
		copySiteProfile();
	}

	public String copySiteProfile() {
		if (isDebugEnabled)
			logger.debug("copySiteProfile() - Entering...");
		if (siteProfiles == null) {
			logger.warn("copySiteProfile() - Sender List is null.");
			return TO_FAILED;
		}
		reset();
		List<ClientVo> mailList = getSiteProfilesList();
		for (int i=0; i<mailList.size(); i++) {
			ClientVo vo = mailList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.sender = new ClientVo();
				try {
					vo.copyPropertiesTo(this.sender);
					sender.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				sender.setClientId(null);
				sender.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public void addSiteProfileListener(AjaxBehaviorEvent event) {
		addSiteProfile();
	}
	
	public String addSiteProfile() {
		if (isDebugEnabled)
			logger.debug("addSiteProfile() - Entering...");
		reset();
		this.sender = new ClientVo();
		sender.setMarkedForEdition(true);
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
	
	public boolean getAnySitesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnySitesMarkedForDeletion() - Entering...");
		if (siteProfiles == null) {
			logger.warn("getAnySitesMarkedForDeletion() - Sender List is null.");
			return false;
		}
		List<ClientVo> mailList = getSiteProfilesList();
		for (Iterator<ClientVo> it=mailList.iterator(); it.hasNext();) {
			ClientVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}
	
	public void validateEmailAddress(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailAddress() - addr: " + emailAddr);
		if (StringUtils.isNotBlank(emailAddr) && !EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
			// invalid email address
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "invalidEmailAddress", new String[] {emailAddr});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validateEmailLocalPart(FacesContext context, UIComponent component, Object value) {
		String localPart = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailLocalPart() - local part: " + localPart);
		if (!EmailAddrUtil.isValidEmailLocalPart(localPart)) {
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "invalidEmailLocalPart", new String[] {localPart});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String senderId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - senderId: " + senderId);
		ClientVo vo = getClientDao().getByClientId(senderId);
		if (editMode == true && vo != null && sender != null && vo.getRowId() != sender.getRowId()) {
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
	        		"ltj.msgui.messages", "siteProfileAlreadyExist", new String[] {senderId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// mailingList already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "siteProfileAlreadyExist", new String[] {senderId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public boolean getIsVerpEnabledInput() {
		if (verpEnabledInput != null) {
			if (verpEnabledInput.getLocalValue() != null) {
				return CodeType.Yes.value().equals(verpEnabledInput.getLocalValue());
			}
			else if (verpEnabledInput.getValue() != null) {
				return CodeType.Yes.value().equals(verpEnabledInput.getValue());
			}
		}
		return true; // for safety
	}
	
	public boolean getIsUseTestAddrInput() {
		if (useTestAddrInput != null) {
			if (useTestAddrInput.getLocalValue() != null) {
				return CodeType.Yes.value().equals(useTestAddrInput.getLocalValue());
			}
			else if (useTestAddrInput.getValue() != null) {
				return CodeType.Yes.value().equals(useTestAddrInput.getValue());
			}
		}
		return true; // for safety
	}
	
	@SuppressWarnings("unchecked")
	private List<ClientVo> getSiteProfilesList() {
		if (siteProfiles == null) {
			return new ArrayList<ClientVo>();
		}
		else {
			return (List<ClientVo>)siteProfiles.getWrappedData();
		}
	}
	
	public ClientVo getSender() {
		return sender;
	}

	public void setSender(ClientVo sender) {
		this.sender = sender;
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

	public DataModel<ClientVo> getSiteProfiles() {
		return siteProfiles;
	}

	public void setSiteProfiles(DataModel<ClientVo> siteProfiles) {
		this.siteProfiles = siteProfiles;
	}

	public UIInput getSenderIdInput() {
		return senderIdInput;
	}

	public void setSenderIdInput(UIInput senderIdInput) {
		this.senderIdInput = senderIdInput;
	}

	public UIInput getVerpEnabledInput() {
		return verpEnabledInput;
	}

	public void setVerpEnabledInput(UIInput verpEnabledInput) {
		this.verpEnabledInput = verpEnabledInput;
	}

	public UIInput getUseTestAddrInput() {
		return useTestAddrInput;
	}

	public void setUseTestAddrInput(UIInput useTestAddrInput) {
		this.useTestAddrInput = useTestAddrInput;
	}

	public UIInput getReturnPathLeftInput() {
		return returnPathLeftInput;
	}

	public void setReturnPathLeftInput(UIInput returnPathLeftInput) {
		this.returnPathLeftInput = returnPathLeftInput;
	}

	public ClientVo getSiteMeta() {
		return siteMeta;
	}
}

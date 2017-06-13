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
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ltj.message.bo.task.TaskBaseBo;
import ltj.message.constant.Constants;
import ltj.message.dao.action.MsgActionDetailDao;
import ltj.message.dao.action.MsgDataTypeDao;
import ltj.message.vo.action.MsgActionDetailVo;
import ltj.message.vo.action.MsgDataTypeVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="ruleAction")
@javax.faces.bean.ViewScoped
public class RuleActionDetailBean implements java.io.Serializable {
	private static final long serialVersionUID = -1479694457663800603L;
	static final Logger logger = Logger.getLogger(RuleActionDetailBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();

	private transient MsgActionDetailDao msgActionDetailDao = null;
	private transient MsgDataTypeDao ruleDataTypeDao = null;
	
	private transient DataModel<MsgActionDetailVo> actionDetails = null;
	private MsgActionDetailVo actionDetail = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput actionIdInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "ruleActionDetailEdit.xhtml";
	private static String TO_SELF = null;
	private static String TO_FAILED = null;
	private static String TO_SAVED = "maintainActionDetails.xhtml";
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public DataModel<MsgActionDetailVo> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (actionDetails == null) {
			List<MsgActionDetailVo> MsgActionDetailList = getMsgActionDetailDao().getAll();
			actionDetails = new ListDataModel<MsgActionDetailVo>(MsgActionDetailList);
		}
		return actionDetails;
	}

	public void refreshListener(AjaxBehaviorEvent event) {
		logger.info("refreshListener() - Event Source: " + event.getSource());
		refresh();
	}
	
	public String refresh() {
		actionDetails = null;
		return TO_SELF;
	}
	
	public MsgActionDetailDao getMsgActionDetailDao() {
		if (msgActionDetailDao == null) {
			msgActionDetailDao = SpringUtil.getWebAppContext().getBean(MsgActionDetailDao.class);
		}
		return msgActionDetailDao;
	}

	public void setMsgActionDetailDao(MsgActionDetailDao msgActionDetailDao) {
		this.msgActionDetailDao = msgActionDetailDao;
	}
	
	public MsgDataTypeDao getMsgDataTypeDao() {
		if (ruleDataTypeDao == null) {
			ruleDataTypeDao = SpringUtil.getWebAppContext().getBean(MsgDataTypeDao.class);
		}
		return ruleDataTypeDao;
	}
	
	public void viewMsgActionDetailListener(AjaxBehaviorEvent event) {
		viewMsgActionDetail();
	}

	public String viewMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("viewMsgActionDetail() - Entering...");
		if (actionDetails == null) {
			logger.warn("viewMsgActionDetail() - MsgActionDetail List is null.");
			return TO_FAILED;
		}
		if (!actionDetails.isRowAvailable()) {
			logger.warn("viewMsgActionDetail() - MsgActionDetail Row not available.");
			return TO_FAILED;
		}
		reset();
		this.actionDetail = (MsgActionDetailVo) actionDetails.getRowData();
		if (isInfoEnabled) {
			logger.info("viewMsgActionDetail() - MsgActionDetail to be edited: "
					+ actionDetail.getActionId());
		}
		actionDetail.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled) {
			logger.debug("viewMsgActionDetail() - MsgActionDetailVo to be passed to jsp: "
					+ actionDetail);
		}
		return TO_EDIT;
	}
	
	public void saveMsgActionDetailListener(AjaxBehaviorEvent event) {
		saveMsgActionDetail();
	}
	
	public String saveMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("saveMsgActionDetail() - Entering...");
		if (actionDetail == null) {
			logger.warn("saveMsgActionDetail() - MsgActionDetailVo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		MsgDataTypeVo ruleDataType = getMsgDataTypeDao().getByTypeValuePair(actionDetail.getActionId(), actionDetail.getDataType());
		actionDetail.setDataType(ruleDataType.getDataType());
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			actionDetail.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getMsgActionDetailDao().update(actionDetail);
			logger.info("saveMsgActionDetail() - Rows Updated: " + 1);
		}
		else {
			getMsgActionDetailDao().insert(actionDetail);
			getMsgActionDetailList().add(actionDetail);
			logger.info("saveMsgActionDetail() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	public void deleteMsgActionDetailsListener(AjaxBehaviorEvent event) {
		deleteMsgActionDetails();
	}
	
	public String deleteMsgActionDetails() {
		if (isDebugEnabled)
			logger.debug("deleteMsgActionDetails() - Entering...");
		if (actionDetails == null) {
			logger.warn("deleteMsgActionDetails() - MsgActionDetail List is null.");
			return TO_FAILED;
		}
		reset();
		List<MsgActionDetailVo> list = getMsgActionDetailList();
		for (int i=0; i<list.size(); i++) {
			MsgActionDetailVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMsgActionDetailDao().deleteByActionId(vo.getActionId());
				if (rowsDeleted > 0) {
					logger.info("deleteMsgActionDetails() - MsgActionDetail deleted: "
							+ vo.getActionId());
				}
				list.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void copyMsgActionDetailListener(AjaxBehaviorEvent event) {
		copyMsgActionDetail();
	}
	
	public String copyMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("copyMsgActionDetail() - Entering...");
		if (actionDetails == null) {
			logger.warn("copyMsgActionDetail() - MsgActionDetail List is null.");
			return TO_FAILED;
		}
		reset();
		List<MsgActionDetailVo> mboxList = getMsgActionDetailList();
		for (int i=0; i<mboxList.size(); i++) {
			MsgActionDetailVo vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.actionDetail = new MsgActionDetailVo();
				try {
					vo.copyPropertiesTo(this.actionDetail);
					actionDetail.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				actionDetail.setActionId(null);
				actionDetail.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public void addMsgActionDetailListener(AjaxBehaviorEvent event) {
		addMsgActionDetail();
	}
	
	public String addMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("addMsgActionDetail() - Entering...");
		reset();
		this.actionDetail = new MsgActionDetailVo();
		MsgDataTypeVo ruleDataType = new MsgDataTypeVo();
		ruleDataType.setMarkedForEdition(true);
		actionDetail.setDataType(ruleDataType.getDataType());
		actionDetail.setMarkedForEdition(true);
		actionDetail.setUpdtUserId(Constants.DEFAULT_USER_ID);
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
	
	public boolean getAnyActionDetailsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyActionDetailsMarkedForDeletion() - Entering...");
		if (actionDetails == null) {
			logger.warn("getAnyActionDetailsMarkedForDeletion() - MsgActionDetail List is null.");
			return false;
		}
		List<MsgActionDetailVo> list = getMsgActionDetailList();
		for (Iterator<MsgActionDetailVo> it=list.iterator(); it.hasNext();) {
			MsgActionDetailVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate primary key
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String actionDetailId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - MsgActionDetailKey: " + actionDetailId);
		MsgActionDetailVo vo = getMsgActionDetailDao().getByActionId(actionDetailId);
		if (!editMode && vo != null) {
			// MsgActionDetail already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "RuleActionIdAlreadyExist", new String[] {actionDetailId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		if (editMode && vo == null) {
			// MsgActionDetail does not exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "RuleActionIdDoesNotExist", new String[] {actionDetailId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void testActionDetailListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("testActionDetail() - Entering...");
		if (actionDetail == null) {
			logger.warn("testActionDetail() - ActionDetailVo is null.");
			return; // TO_FAILED;
		}
		testResult = null;
		String className = actionDetail.getProcessClassName();
		if (className != null && className.trim().length() > 0) {
			try {
				Object bo = Class.forName(className).newInstance();
				if (bo instanceof TaskBaseBo) {
					testResult = "actionDetailClassNameTestSuccess";
				}
				else {
					testResult = "actionDetailClassNameTestFailure";
				}
			}
			catch (Exception e) {
				logger.error("Exception caught: " + e.toString());
				testResult = "actionDetailClassNameTestFailure";
			}
		}
		String beanId = actionDetail.getProcessBeanId();
		if (beanId != null && testResult == null) {
			FacesContext facesCtx = FacesContext.getCurrentInstance();
			ServletContext sctx = (ServletContext) facesCtx.getExternalContext().getContext();
			WebApplicationContext ctx = WebApplicationContextUtils
					.getRequiredWebApplicationContext(sctx);
			try {
				Object bo = ctx.getBean(beanId);
				if (bo instanceof TaskBaseBo) {
					testResult = "actionDetailBeanIdTestSuccess";
				}
				else {
					testResult = "actionDetailBeanIdTestFailure";
				}
			}
			catch (Exception e) {
				logger.error("Exception caught: " + e.toString());
				testResult = "actionDetailBeanIdTestFailure";
			}
		}
		//return TO_SELF;
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		actionIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<MsgActionDetailVo> getMsgActionDetailList() {
		if (actionDetails == null) {
			return new ArrayList<MsgActionDetailVo>();
		}
		else {
			return (List<MsgActionDetailVo>)actionDetails.getWrappedData();
		}
	}
	
	public MsgActionDetailVo getActionDetail() {
		return actionDetail;
	}

	public void setActionDetail(MsgActionDetailVo actionDetail) {
		this.actionDetail = actionDetail;
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

	public UIInput getActionIdInput() {
		return actionIdInput;
	}

	public void setActionIdInput(UIInput actionDetailIdInput) {
		this.actionIdInput = actionDetailIdInput;
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

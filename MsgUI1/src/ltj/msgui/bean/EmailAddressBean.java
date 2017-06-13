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
import org.opensaml.saml2.metadata.EmailAddress;

import ltj.message.constant.Constants;
import ltj.message.constant.RuleCriteria;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.util.TestUtil;
import ltj.message.vo.PagingVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="emailAddress")
@javax.faces.bean.ViewScoped
public class EmailAddressBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = -1230662734764912082L;
	static final Logger logger = Logger.getLogger(EmailAddressBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient EmailAddressDao emailAddrDao = null;
	private transient MailingListDao mailingListDao = null;
	
	private EmailAddressVo emailAddr = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;

	private transient DataModel<EmailAddressVo> emailAddrs = null;
	private transient UIInput emailAddrInput = null;
	
	private String searchString = null;
	
	private List<MailingListVo> mailingLists = null;
	private String testResult = null;
	private String actionFailure = null;

	private static String TO_SELF = null;
	private static String TO_FAILED = TO_SELF;
	private static String TO_LIST = "emailAddressList.xhtml";
	private static String TO_EDIT = "emailAddressEdit.xhtml";
	private static String TO_SAVED = TO_LIST;
	private static String TO_CANCELED = "main.xhtml";
	
	public DataModel<EmailAddressVo> getEmailAddrs() {
		String fromPage = sessionBean.getRequestParam("frompage");
		logger.info("getEmailAddrs() - fromPage = " + fromPage);
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		if (!getPagingVo().getPageAction().equals(PagingVo.PageAction.CURRENT) || emailAddrs == null) {
			getPagingVo().setStatusId(null);
			getPagingVo().setOrderBy(PagingVo.Column.emailAddr, true);
			logger.info("PagingVo Before: " + getPagingVo());
			List<EmailAddressVo> emailAddrList = getEmailAddressDao().getEmailAddrsWithPaging(getPagingVo());
			getPagingVo().setPageAction(PagingVo.PageAction.CURRENT);
			logger.info("PagingVo After: " + getPagingVo());
			emailAddrs = new ListDataModel<EmailAddressVo>(emailAddrList);
		}
		return emailAddrs;
	}

	@Override
	public long getRowCount() {
		getPagingVo().setStatusId(null);
		long rowCount = getEmailAddressDao().getEmailAddressCount(getPagingVo());
		getPagingVo().setRowCount(rowCount);
		return rowCount;
	}

	/*
	 * Use String signature for rowId to support JSF script.
	 */
	public String findAddressByRowId(String rowId) {
		EmailAddressVo addr = getEmailAddressDao().getByAddrId(Integer.parseInt(rowId));
		if (addr != null) {
			return addr.getEmailAddr();
		}
		else {
			return "";
		}
	}

	public void searchByAddress(AjaxBehaviorEvent event) {
		boolean changed = false;
		
		PagingVo.Criteria criteria = getPagingVo().getSearchBy().getCriteria(PagingVo.Column.origEmailAddr);
		if (this.searchString == null) {
			if (criteria != null && criteria.getValue() != null) {
				changed = true;
			}
		}
		else {
			if (criteria != null && !this.searchString.equals(criteria.getValue())) {
				changed = true;
			}
		}
		if (changed) {
			resetPagingVo();
			getPagingVo().setSearchCriteria(PagingVo.Column.origEmailAddr, new PagingVo.Criteria(RuleCriteria.CONTAINS, searchString));
		}
		return; // TO_SELF;
	}
	
	public String resetSearch() {
		searchString = null;
		getPagingVo().setSearchCriteria(PagingVo.Column.origEmailAddr, new PagingVo.Criteria(RuleCriteria.CONTAINS, null));
		resetPagingVo();
		return TO_SELF;
	}
	
	public void resetSearchListener(AjaxBehaviorEvent event) {
		resetSearch();
	}

	@Override
	protected void refresh() {
		emailAddrs = null;
	}

	public void refreshPage(AjaxBehaviorEvent event) {
		refresh(); // stay on the same page
		//resetPagingVo(); // jump to first page
	}
	
	public EmailAddressDao getEmailAddressDao() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddrDao;
	}

	public void setEmailAddressDao(EmailAddressDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = SpringUtil.getWebAppContext().getBean(MailingListDao.class);
		}
		return mailingListDao;
	}

	public void setMailingListDao(MailingListDao mailingListDao) {
		this.mailingListDao = mailingListDao;
	}

	public void viewEmailAddrListener(AjaxBehaviorEvent event) {
		viewEmailAddr();
	}
	
	public String viewEmailAddr() {
		if (isDebugEnabled)
			logger.debug("viewEmailAddr() - Entering...");
		if (emailAddrs == null) {
			logger.warn("viewEmailAddr() - EmailAddr List is null.");
			actionFailure = "EmailAddr List is null";
			return TO_FAILED;
		}
		if (!emailAddrs.isRowAvailable()) {
			logger.warn("viewEmailAddr() - EmailAddr Row not available.");
			actionFailure = "EmailAddr Row not available.";
			return TO_FAILED;
		}
		reset();
		this.emailAddr = (EmailAddressVo) emailAddrs.getRowData();
		logger.info("viewEmailAddr() - EmailAddr to be edited: " + emailAddr.getEmailAddr());
		emailAddr.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		mailingLists = getMailingListDao().getByAddressWithCounts(emailAddr.getEmailAddr());
		if (isDebugEnabled) {
			logger.debug("viewEmailAddr() - EmailAddress to be passed to jsp: " + emailAddr);
		}
		return TO_EDIT;
	}

	public String saveEmailAddr() {
		if (isDebugEnabled)
			logger.debug("saveEmailAddr() - Entering...");
		if (emailAddr == null) {
			logger.warn("saveEmailAddr() - EmailAddress is null.");
			actionFailure = "EmailAddress is null.";
			return TO_FAILED;
		}
		reset();
		// update database
		if (TestUtil.isRunningInJunitTest() == false) {
			if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
				emailAddr.setUpdtUserId(FacesUtil.getLoginUserId());
			}
		}
		if (editMode == true) {
			getEmailAddressDao().update(emailAddr);
			refresh();
			logger.info("saveEmailAddr() - Rows Updated: " + 1);
		}
		else {
			getEmailAddressDao().insert(emailAddr);
			addToList(emailAddr);
			getPagingVo().setRowCount(getPagingVo().getRowCount() + 1);
			resetPagingVo();
			logger.info("saveEmailAddr() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	public void saveEmailAddrListener(AjaxBehaviorEvent event) {
		saveEmailAddr();
		testResult = "changesAreSaved";
	}

	private void addToList(EmailAddressVo vo) {
		@SuppressWarnings("unchecked")
		List<EmailAddressVo> list = (List<EmailAddressVo>) emailAddrs.getWrappedData();
		list.add(vo);
	}

	public void deleteEmailAddrs(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("deleteEmailAddrs() - Entering...");
		if (emailAddrs == null) {
			logger.warn("deleteEmailAddrs() - EmailAddr List is null.");
			actionFailure = "EmailAddr List is null";
			return; // TO_FAILED;
		}
		reset();
		List<EmailAddressVo> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddressVo vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailAddressDao().deleteByAddrId(vo.getEmailAddrId());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailAddrs() - EmailAddr deleted: " + vo.getEmailAddr());
					getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsDeleted);
				}
				addrList.remove(i);
			}
		}
		//refresh();
		return; // TO_DELETED;
	}

	public String saveEmailAddrs() {
		if (isDebugEnabled)
			logger.debug("saveEmailAddrs() - Entering...");
		if (emailAddrs == null) {
			logger.warn("saveEmailAddrs() - EmailAddr List is null.");
			actionFailure = "EmailAddr List is null";
			return TO_FAILED;
		}
		reset();
		List<EmailAddressVo> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddressVo vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				if (!vo.getEmailAddr().equals(vo.getCurrEmailAddr())) {
					EmailAddressVo vo2 = getEmailAddressDao().getByAddress(vo.getEmailAddr());
					if (vo2 != null && vo2.getEmailAddrId() != vo.getEmailAddrId()) {
						actionFailure = "Email address " + vo.getEmailAddr() + " already exists.";
						return TO_SELF;
					}
				}
				getEmailAddressDao().update(vo);
				logger.info("saveEmailAddrs() - EmailAddr updated: " + vo.getEmailAddr());
				vo.setMarkedForDeletion(false);
			}
		}
		//refresh();
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	public void addEmailAddrListener(AjaxBehaviorEvent event) {
		addEmailAddr();
	}

	public String addEmailAddr() {
		if (isDebugEnabled)
			logger.debug("addEmailAddr() - Entering...");
		reset();
		this.emailAddr = new EmailAddressVo();
		emailAddr.setMarkedForEdition(true);
		emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
		if (mailingLists != null) {
			mailingLists.clear();
		}
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT;
	}

	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		//refresh();
		beanMode = BeanMode.list;
		if (StringUtils.contains(FacesUtil.getCurrentViewId(), TO_EDIT)) {
			return TO_LIST;
		}
		else {
			return TO_CANCELED;
		}
	}

	public boolean getAnyEmailAddrsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyEmailAddrsMarkedForDeletion() - Entering...");
		if (emailAddrs == null) {
			logger.warn("getAnyEmailAddrsMarkedForDeletion() - EmailAddr List is null.");
			return false;
		}
		List<EmailAddressVo> addrList = getEmailAddrList();
		for (Iterator<EmailAddressVo> it=addrList.iterator(); it.hasNext();) {
			EmailAddressVo vo = it.next();
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
		String address = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - address: " + address);
		EmailAddressVo vo = getEmailAddressDao().getByAddress(address);
		if (vo == null) return;
		if (editMode == true && emailAddr != null
				&& vo.getEmailAddrId() != emailAddr.getEmailAddrId()) {
			// emailAddr does not exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					//"jpa.msgui.messages", "emailAddrDoesNotExist", null);
	        		"jpa.msgui.messages", "emailAddrAlreadyExist", new String[] {address});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false) {
			// emailAddr already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "emailAddrAlreadyExist", new String[] {address});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	void reset() {
		emailAddrInput = null;
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings("unchecked")
	private List<EmailAddressVo> getEmailAddrList() {
		if (emailAddrs == null) {
			return new ArrayList<EmailAddressVo>();
		}
		else {
			return (List<EmailAddressVo>)emailAddrs.getWrappedData();
		}
	}

	public EmailAddressVo getEmailAddr() {
		if (emailAddr == null) {
			// When navigate from List -> Edit, request parameter for current email address is populated
			String addrStr = sessionBean.getRequestParam("emailaddr");
			if (StringUtils.isNotBlank(addrStr)) {
				emailAddr = getEmailAddressDao().getByAddress(addrStr);
			}
		}
		if (emailAddr == null && emailAddrs != null) {
			if (emailAddrs.isRowAvailable()) {
				emailAddr = emailAddrs.getRowData();
			}
		}
		return emailAddr;
	}

	public void setEmailAddr(EmailAddressVo emailAddr) {
		this.emailAddr = emailAddr;
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

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
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

	public UIInput getEmailAddrInput() {
		return emailAddrInput;
	}

	public void setEmailAddrInput(UIInput emailAddrInput) {
		this.emailAddrInput = emailAddrInput;
	}

	public List<MailingListVo> getMailingLists() {
		return mailingLists;
	}

	public void setMailingLists(List<MailingListVo> mailingLists) {
		this.mailingLists = mailingLists;
	}

	public boolean isMailingListsEmpty() {
		return (mailingLists == null || mailingLists.isEmpty()); 
	}
}

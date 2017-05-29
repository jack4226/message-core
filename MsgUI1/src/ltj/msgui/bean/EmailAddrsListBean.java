package ltj.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.util.StringUtil;
import ltj.message.vo.PagingAddrVo;
import ltj.message.vo.PagingVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

public class EmailAddrsListBean {
	static final Logger logger = Logger.getLogger(EmailAddrsListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private EmailAddressDao emailAddressDao = null;
	private MailingListDao mailingListDao = null;
	private DataModel emailAddrs = null;
	private EmailAddressVo emailAddr = null;
	private boolean editMode = true;

	private HtmlDataTable dataTable;
	private final PagingAddrVo pagingAddrVo =  new PagingAddrVo();
	private String searchString = null;
	
	private List<MailingListVo> mailingLists = null;
	private UIInput emailAddrInput = null;
	private String testResult = null;
	private String actionFailure = null;

	private static String TO_FAILED = "emailAddrlist.failed";
	private static String TO_DELETED = "emailAddrlist.deleted";
	private static String TO_EDIT = "emailAddrlist.edit";
	private static String TO_SAVED = "emailAddrlist.saved";
	private static String TO_CANCELED = "emailAddrlist.canceled";
	private static String TO_PAGING = "emailAddrlist.paging";
	private static String TO_SELF = "emailAddrlist.toself";
	
	public DataModel getEmailAddrs() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingAddrVo.getRowCount() < 0) {
			int rowCount = getEmailAddrDao().getEmailAddressCount(pagingAddrVo);
			pagingAddrVo.setRowCount(rowCount);
		}
		if (emailAddrs == null || !pagingAddrVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<EmailAddressVo> emailAddrList = getEmailAddrDao().getEmailAddrsWithPaging(pagingAddrVo);
			/* set keys for paging */
			if (!emailAddrList.isEmpty()) {
				EmailAddressVo firstRow = (EmailAddressVo) emailAddrList.get(0);
				EmailAddressVo lastRow = (EmailAddressVo) emailAddrList.get(emailAddrList.size() - 1);
				//pagingAddrVo.setIdFirst(firstRow.getEmailAddrId());
				//pagingAddrVo.setIdLast(lastRow.getEmailAddrId());
				pagingAddrVo.setStrIdFirst(firstRow.getEmailAddr());
				pagingAddrVo.setStrIdLast(lastRow.getEmailAddr());
			}
			else {
				//pagingAddrVo.setIdFirst(-1);
				//pagingAddrVo.setIdLast(-1);
				pagingAddrVo.setStrIdFirst(null);
				pagingAddrVo.setStrIdLast(null);
			}
			logger.info("PagingAddrVo After: " + pagingAddrVo);
			pagingAddrVo.setPageAction(PagingVo.PageAction.CURRENT);
			//emailAddrs = new ListDataModel(emailAddrList);
			emailAddrs = new PagedListDataModel(emailAddrList, pagingAddrVo.getRowCount(), pagingAddrVo.getPageSize());
		}
		return emailAddrs;
	}

	public String searchByAddress() {
		boolean changed = false;
		if (this.searchString == null) {
			if (pagingAddrVo.getEmailAddr() != null) {
				changed = true;
			}
		}
		else {
			if (!this.searchString.equals(pagingAddrVo.getEmailAddr())) {
				changed = true;
			}
		}
		if (changed) {
			resetPagingVo();
			pagingAddrVo.setEmailAddr(searchString);
		}
		return TO_SELF;
	}
	
	public String resetSearch() {
		searchString = null;
		pagingAddrVo.setEmailAddr(null);
		resetPagingVo();
		return TO_SELF;
	}
	
	public String pageFirst() {
		dataTable.setFirst(0);
		pagingAddrVo.setPageAction(PagingVo.PageAction.FIRST);
		return TO_PAGING;
	}

	public String pagePrevious() {
		dataTable.setFirst(dataTable.getFirst() - dataTable.getRows());
		pagingAddrVo.setPageAction(PagingVo.PageAction.PREVIOUS);
		return TO_PAGING;
	}

	public String pageNext() {
		dataTable.setFirst(dataTable.getFirst() + dataTable.getRows());
		pagingAddrVo.setPageAction(PagingVo.PageAction.NEXT);
		return TO_PAGING;
	}

	public String pageLast() {
		int count = dataTable.getRowCount();
		int rows = dataTable.getRows();
		dataTable.setFirst(count - ((count % rows != 0) ? count % rows : rows));
		pagingAddrVo.setPageAction(PagingVo.PageAction.LAST);
		return TO_PAGING;
	}
    
	public int getLastPageRow() {
		int lastRow = dataTable.getFirst() + dataTable.getRows();
		if (lastRow > dataTable.getRowCount()) {
			return dataTable.getRowCount();
		}
		else {
			return lastRow;
		}
	}
	
	public PagingAddrVo getPagingVo() {
		return pagingAddrVo;
	}
	
	private void refresh() {
		emailAddrs = null;
	}

	public String refreshPage() {
		refresh();
		pagingAddrVo.setRowCount(-1);
		return TO_SELF;
	}
	
	private void resetPagingVo() {
		pagingAddrVo.resetPageContext();
		if (dataTable != null) {
			dataTable.setFirst(0);
		}
		refresh();
	}
	
	public EmailAddressDao getEmailAddrDao() {
		if (emailAddressDao == null) {
			emailAddressDao = (EmailAddressDao) SpringUtil.getWebAppContext().getBean("emailAddressDao");
		}
		return emailAddressDao;
	}

	public void setEmailAddrDao(EmailAddressDao emailAddressDao) {
		this.emailAddressDao = emailAddressDao;
	}

	public MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListDao) SpringUtil.getWebAppContext().getBean("mailingListDao");
		}
		return mailingListDao;
	}

	public void setMailingListDao(MailingListDao mailingListDao) {
		this.mailingListDao = mailingListDao;
	}

	public String viewEmailAddr() {
		if (isDebugEnabled) {
			logger.debug("viewEmailAddr() - Entering...");
		}
		if (emailAddrs == null) {
			logger.warn("viewEmailAddr() - EmailAddr List is null.");
			return TO_FAILED;
		}
		if (!emailAddrs.isRowAvailable()) {
			logger.warn("viewEmailAddr() - EmailAddr Row not available.");
			return TO_FAILED;
		}
		reset();
		this.emailAddr = (EmailAddressVo) emailAddrs.getRowData();
		logger.info("viewEmailAddr() - EmailAddr to be edited: " + emailAddr.getEmailAddr());
		emailAddr.setMarkedForEdition(true);
		editMode = true;
		mailingLists = getMailingListDao().getSubscribedLists(emailAddr.getEmailAddrId());
		if (isDebugEnabled) {
			logger.debug("viewEmailAddr() - EmailAddressVo to be passed to jsp: " + emailAddr);
		}
		return TO_EDIT;
	}

	public String saveEmailAddr() {
		if (isDebugEnabled) {
			logger.debug("saveEmailAddr() - Entering...");
		}
		if (emailAddr == null) {
			logger.warn("saveEmailAddr() - EmailAddressVo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			emailAddr.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getEmailAddrDao().update(emailAddr);
			logger.info("saveEmailAddr() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getEmailAddrDao().insert(emailAddr);
			if (rowsInserted > 0) {
				addToList(emailAddr);
				pagingAddrVo.setRowCount(pagingAddrVo.getRowCount() + rowsInserted);
				refresh();
			}
			logger.info("saveEmailAddr() - Rows Inserted: " + rowsInserted);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(EmailAddressVo vo) {
		List<EmailAddressVo> list = (List<EmailAddressVo>) emailAddrs.getWrappedData();
		list.add(vo);
	}

	public String deleteEmailAddrs() {
		if (isDebugEnabled) {
			logger.debug("deleteEmailAddrs() - Entering...");
		}
		if (emailAddrs == null) {
			logger.warn("deleteEmailAddrs() - EmailAddr List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailAddressVo> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddressVo vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailAddrDao().deleteByAddrId(vo.getEmailAddrId());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailAddrs() - EmailAddr deleted: " + vo.getEmailAddr());
					pagingAddrVo.setRowCount(pagingAddrVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public String saveEmailAddrs() {
		if (isDebugEnabled) {
			logger.debug("saveEmailAddrs() - Entering...");
		}
		if (emailAddrs == null) {
			logger.warn("saveEmailAddrs() - EmailAddr List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailAddressVo> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddressVo vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				if (!vo.getEmailAddr().equals(vo.getCurrEmailAddr())) {
					EmailAddressVo vo2 = getEmailAddrDao().getByAddress(vo.getEmailAddr());
					if (vo2 != null && vo2.getEmailAddrId() != vo.getEmailAddrId()) {
						actionFailure = "Email address " + vo.getEmailAddr() + " already exists.";
						return TO_SELF;
					}
				}
				int rowsUpdated = getEmailAddrDao().update(vo);
				if (rowsUpdated > 0) {
					logger.info("saveEmailAddrs() - EmailAddr updated: " + vo.getEmailAddr());
				}
				vo.setMarkedForDeletion(false);
			}
		}
		refresh();
		return TO_SAVED;
	}

	public String addEmailAddr() {
		if (isDebugEnabled) {
			logger.debug("addEmailAddr() - Entering...");
		}
		reset();
		this.emailAddr = new EmailAddressVo();
		emailAddr.setMarkedForEdition(true);
		emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
		if (mailingLists != null) {
			mailingLists.clear();
		}
		editMode = false;
		return TO_EDIT;
	}

	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getAnyEmailAddrsMarkedForDeletion() {
		if (isDebugEnabled) {
			logger.debug("getAnyEmailAddrsMarkedForDeletion() - Entering...");
		}
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
		if (isDebugEnabled) {
			logger.debug("validatePrimaryKey() - address: " + address);
		}
		EmailAddressVo vo = getEmailAddrDao().getByAddress(address);
		if (editMode == true && vo != null && emailAddr != null && vo.getEmailAddrId() != emailAddr.getEmailAddrId()) {
			// emailAddr does not exist
			FacesMessage message = ltj.msgui.util.Messages.getMessage("ltj.msgui.messages", "emailAddrAlreadyExist",
					null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// emailAddr already exist
			FacesMessage message = ltj.msgui.util.Messages.getMessage("ltj.msgui.messages", "emailAddrAlreadyExist",
					null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	void reset() {
		emailAddrInput = null;
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings({ "unchecked" })
	private List<EmailAddressVo> getEmailAddrList() {
		if (emailAddrs == null) {
			return new ArrayList<EmailAddressVo>();
		}
		else {
			return (List<EmailAddressVo>)emailAddrs.getWrappedData();
		}
	}

	public EmailAddressVo getEmailAddr() {
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

	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
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

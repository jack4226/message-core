package com.legacytojava.msgui.bean;

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

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.emailaddr.MailingListVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class EmailAddrsListBean {
	static final Logger logger = Logger.getLogger(EmailAddrsListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private EmailAddrDao emailAddrDao = null;
	private MailingListDao mailingListDao = null;
	private DataModel emailAddrs = null;
	private EmailAddrVo emailAddr = null;
	private boolean editMode = true;

	private HtmlDataTable dataTable;
	private final PagingVo pagingVo =  new PagingVo();;
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
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getEmailAddrDao().getEmailAddressCount(pagingVo);
			pagingVo.setRowCount(rowCount);
		}
		if (emailAddrs == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<EmailAddrVo> emailAddrList = getEmailAddrDao().getEmailAddrsWithPaging(pagingVo);
			/* set keys for paging */
			if (!emailAddrList.isEmpty()) {
				EmailAddrVo firstRow = (EmailAddrVo) emailAddrList.get(0);
				EmailAddrVo lastRow = (EmailAddrVo) emailAddrList.get(emailAddrList.size() - 1);
				//pagingVo.setIdFirst(firstRow.getEmailAddrId());
				//pagingVo.setIdLast(lastRow.getEmailAddrId());
				pagingVo.setStrIdFirst(firstRow.getEmailAddr());
				pagingVo.setStrIdLast(lastRow.getEmailAddr());
			}
			else {
				//pagingVo.setIdFirst(-1);
				//pagingVo.setIdLast(-1);
				pagingVo.setStrIdFirst(null);
				pagingVo.setStrIdLast(null);
			}
			logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			//emailAddrs = new ListDataModel(emailAddrList);
			emailAddrs = new PagedListDataModel(emailAddrList, pagingVo.getRowCount(), pagingVo.getPageSize());
		}
		return emailAddrs;
	}

	public String searchByAddress() {
		boolean changed = false;
		if (this.searchString == null) {
			if (pagingVo.getSearchString() != null) {
				changed = true;
			}
		}
		else {
			if (!this.searchString.equals(pagingVo.getSearchString())) {
				changed = true;
			}
		}
		if (changed) {
			resetPagingVo();
			pagingVo.setSearchString(searchString);
		}
		return TO_SELF;
	}
	
	public String resetSearch() {
		searchString = null;
		pagingVo.setSearchString(null);
		resetPagingVo();
		return TO_SELF;
	}
	
	public String pageFirst() {
		dataTable.setFirst(0);
		pagingVo.setPageAction(PagingVo.PageAction.FIRST);
		return TO_PAGING;
	}

	public String pagePrevious() {
		dataTable.setFirst(dataTable.getFirst() - dataTable.getRows());
		pagingVo.setPageAction(PagingVo.PageAction.PREVIOUS);
		return TO_PAGING;
	}

	public String pageNext() {
		dataTable.setFirst(dataTable.getFirst() + dataTable.getRows());
		pagingVo.setPageAction(PagingVo.PageAction.NEXT);
		return TO_PAGING;
	}

	public String pageLast() {
		int count = dataTable.getRowCount();
		int rows = dataTable.getRows();
		dataTable.setFirst(count - ((count % rows != 0) ? count % rows : rows));
		pagingVo.setPageAction(PagingVo.PageAction.LAST);
		return TO_PAGING;
	}
    
	public int getLastPageRow() {
		int lastRow = dataTable.getFirst() + dataTable.getRows();
		if (lastRow > dataTable.getRowCount())
			return dataTable.getRowCount();
		else
			return lastRow;
	}
	
	public PagingVo getPagingVo() {
		return pagingVo;
	}
	
	private void refresh() {
		emailAddrs = null;
	}

	public String refreshPage() {
		refresh();
		pagingVo.setRowCount(-1);
		return TO_SELF;
	}
	
	private void resetPagingVo() {
		pagingVo.resetPageContext();
		if (dataTable != null) dataTable.setFirst(0);
		refresh();
	}
	
	public EmailAddrDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddrDao) SpringUtil.getWebAppContext().getBean("emailAddrDao");
		}
		return emailAddrDao;
	}

	public void setEmailAddrDao(EmailAddrDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public MailingListDao getMailingListDao() {
		if (mailingListDao == null)
			mailingListDao = (MailingListDao) SpringUtil.getWebAppContext().getBean("mailingListDao");
		return mailingListDao;
	}

	public void setMailingListDao(MailingListDao mailingListDao) {
		this.mailingListDao = mailingListDao;
	}

	public String viewEmailAddr() {
		if (isDebugEnabled)
			logger.debug("viewEmailAddr() - Entering...");
		if (emailAddrs == null) {
			logger.warn("viewEmailAddr() - EmailAddr List is null.");
			return TO_FAILED;
		}
		if (!emailAddrs.isRowAvailable()) {
			logger.warn("viewEmailAddr() - EmailAddr Row not available.");
			return TO_FAILED;
		}
		reset();
		this.emailAddr = (EmailAddrVo) emailAddrs.getRowData();
		logger.info("viewEmailAddr() - EmailAddr to be edited: " + emailAddr.getEmailAddr());
		emailAddr.setMarkedForEdition(true);
		editMode = true;
		mailingLists = getMailingListDao().getSubscribedLists(emailAddr.getEmailAddrId());
		if (isDebugEnabled) {
			logger.debug("viewEmailAddr() - EmailAddrVo to be passed to jsp: " + emailAddr);
		}
		return TO_EDIT;
	}

	public String saveEmailAddr() {
		if (isDebugEnabled)
			logger.debug("saveEmailAddr() - Entering...");
		if (emailAddr == null) {
			logger.warn("saveEmailAddr() - EmailAddrVo is null.");
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
				pagingVo.setRowCount(pagingVo.getRowCount() + rowsInserted);
				refresh();
			}
			logger.info("saveEmailAddr() - Rows Inserted: " + rowsInserted);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(EmailAddrVo vo) {
		List<EmailAddrVo> list = (List<EmailAddrVo>) emailAddrs.getWrappedData();
		list.add(vo);
	}

	public String deleteEmailAddrs() {
		if (isDebugEnabled)
			logger.debug("deleteEmailAddrs() - Entering...");
		if (emailAddrs == null) {
			logger.warn("deleteEmailAddrs() - EmailAddr List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailAddrVo> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddrVo vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailAddrDao().deleteByAddrId(vo.getEmailAddrId());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailAddrs() - EmailAddr deleted: " + vo.getEmailAddr());
					pagingVo.setRowCount(pagingVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public String saveEmailAddrs() {
		if (isDebugEnabled)
			logger.debug("saveEmailAddrs() - Entering...");
		if (emailAddrs == null) {
			logger.warn("saveEmailAddrs() - EmailAddr List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailAddrVo> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddrVo vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				if (!vo.getEmailAddr().equals(vo.getCurrEmailAddr())) {
					EmailAddrVo vo2 = getEmailAddrDao().getByAddress(vo.getEmailAddr());
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
		if (isDebugEnabled)
			logger.debug("addEmailAddr() - Entering...");
		reset();
		this.emailAddr = new EmailAddrVo();
		emailAddr.setMarkedForEdition(true);
		emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
		if (mailingLists != null)
			mailingLists.clear();
		editMode = false;
		return TO_EDIT;
	}

	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getAnyEmailAddrsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyEmailAddrsMarkedForDeletion() - Entering...");
		if (emailAddrs == null) {
			logger.warn("getAnyEmailAddrsMarkedForDeletion() - EmailAddr List is null.");
			return false;
		}
		List<EmailAddrVo> addrList = getEmailAddrList();
		for (Iterator<EmailAddrVo> it=addrList.iterator(); it.hasNext();) {
			EmailAddrVo vo = it.next();
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
		EmailAddrVo vo = getEmailAddrDao().getByAddress(address);
		if (editMode == true && vo != null && emailAddr != null
				&& vo.getEmailAddrId() != emailAddr.getEmailAddrId()) {
			// emailAddr does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					//"com.legacytojava.msgui.messages", "emailAddrDoesNotExist", null);
	        		"com.legacytojava.msgui.messages", "emailAddrAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// emailAddr already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "emailAddrAlreadyExist", null);
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
	private List<EmailAddrVo> getEmailAddrList() {
		if (emailAddrs == null) {
			return new ArrayList<EmailAddrVo>();
		}
		else {
			return (List<EmailAddrVo>)emailAddrs.getWrappedData();
		}
	}

	public EmailAddrVo getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(EmailAddrVo emailAddr) {
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

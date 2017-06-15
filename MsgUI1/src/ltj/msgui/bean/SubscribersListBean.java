package ltj.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.vo.PagingVo;
import ltj.message.vo.SearchSbsrVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="subscription")
@javax.faces.bean.ViewScoped
public class SubscribersListBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = 6216351042518651517L;
	static final Logger logger = Logger.getLogger(SubscribersListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient EmailSubscrptDao subscriptionDao = null;
	private transient EmailAddressDao emailAddrDao = null;
	private transient MailingListDao mailingListDao = null;
	
	private transient DataModel<EmailSubscrptVo> subscriptions = null;
	private EmailSubscrptVo subscription = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private String listId = null;

	private transient HtmlDataTable dataTable;
	private String searchString = null;
	
	private SearchSbsrVo searchVo = new SearchSbsrVo(getPagingVo());
	
	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_SELF = null;
	static final String TO_FAILED = null;
	static final String TO_PAGING = TO_SELF;
	static final String TO_DELETED = TO_SELF;
	static final String TO_SAVED = TO_SELF;
	static final String TO_EDIT = "subscriptions.xhtml";
	static final String TO_CANCELED = "configureMailingLists.xhtml";

	public DataModel<EmailSubscrptVo> getSubscriptions() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (StringUtils.equals(fromPage,"main")) {
			resetPagingVo();
		}
		else if (StringUtils.equals(fromPage, "mailinglist")) {
			listId = FacesUtil.getRequestParameter("listId"); 
			if (StringUtils.isNotBlank(listId)) {
				resetPagingVo();
			}
		}
		logger.info("getSubscriptions() - From Page: " + fromPage + ", List Id: " + listId);
		// retrieve total number of rows
		if (getPagingVo().getRowCount() < 0) {
			long rowCount = getEmailSubscrptDao().getSubscriberCount(searchVo);
			getPagingVo().setRowCount(rowCount);
		}
		if (subscriptions == null || !getPagingVo().getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<EmailSubscrptVo> subscriptionList = getEmailSubscrptDao().getSubscribersWithPaging(searchVo);
			logger.info("PagingVo After: " + getPagingVo());
			getPagingVo().setPageAction(PagingVo.PageAction.CURRENT);
			subscriptions = new ListDataModel<EmailSubscrptVo>(subscriptionList);
		}
		return subscriptions;
	}

	@Override
	public long getRowCount() {
		if (StringUtils.equals(FacesUtil.getRequestParameter("frompage"), "mailinglist")) {
			listId = FacesUtil.getRequestParameter("listId"); 
		}
		long rowCount = getEmailSubscrptDao().getSubscriberCount(searchVo);
		getPagingVo().setRowCount(rowCount);
		return rowCount;
	}

	public void searchByAddressListener(AjaxBehaviorEvent event) {
		searchByAddress();
	}
	
	public String searchByAddress() {
		boolean changed = false;
		if (this.searchString == null) {
			if (searchVo.getEmailAddr() != null) {
				changed = true;
			}
		}
		else {
			if (!this.searchString.equals(searchVo.getEmailAddr())) {
				changed = true;
			}
		}
		if (changed) {
			resetPagingVo();
			searchVo.setEmailAddr(searchString);
		}
		return null;
	}
	
	public void resetSearchListener(AjaxBehaviorEvent event) {
		resetSearch();
	}
	
	public String resetSearch() {
		searchString = null;
		searchVo.setEmailAddr(searchString);
		resetPagingVo();
		return null;
	}
	
	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public void refresh() {
		subscriptions = null;
	}

	public String refreshPage() {
		refresh();
		getPagingVo().setRowCount(-1);
		return "";
	}
	
	public void refreshPageListener(AjaxBehaviorEvent event) {
		refreshPage();
	}
	
	public EmailSubscrptDao getEmailSubscrptDao() {
		if (subscriptionDao == null) {
			subscriptionDao = SpringUtil.getWebAppContext().getBean(EmailSubscrptDao.class);
		}
		return subscriptionDao;
	}

	public void setEmailSubscrptDao(EmailSubscrptDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
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

	public void deleteSubscriptionsListener(AjaxBehaviorEvent event) {
		deleteSubscriptions();
	}
	
	public String deleteSubscriptions() {
		if (isDebugEnabled)
			logger.debug("deleteSubscriptions() - Entering...");
		if (subscriptions == null) {
			logger.warn("deleteSubscriptions() - EmailSubscrptVo List is null.");
			return TO_FAILED;
		}
		if (listId == null) {
			logger.warn("deleteSubscriptions() - ListId is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailSubscrptVo> subrList = getSubscriptionList();
		for (int i=0; i<subrList.size(); i++) {
			EmailSubscrptVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailSubscrptDao().deleteByPrimaryKey(vo.getEmailAddrId(), listId);
				if (rowsDeleted > 0) {
					logger.info("deleteSubscriptions() - Subscription deleted: " + vo.getEmailAddr());
					getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public void saveSubscriptionsListener(AjaxBehaviorEvent event) {
		saveSubscriptions();
	}
	
	public String saveSubscriptions() {
		if (isDebugEnabled)
			logger.debug("saveSubscriptions() - Entering...");
		if (subscriptions == null) {
			logger.warn("saveSubscriptions() - Subscription List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailSubscrptVo> subrList = getSubscriptionList();
		for (int i=0; i<subrList.size(); i++) {
			EmailSubscrptVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				getEmailSubscrptDao().update(vo);
//				boolean acceptHtml =  CodeType.YES.getValue().equalsIgnoreCase(vo.getAcceptHtmlDesc());
//				EmailAddress emailAddr = getEmailAddressDao().getByRowId(vo.getEmailAddr().getRowId());
//				if (acceptHtml!=emailAddr.isAcceptHtml()) {
//					emailAddr.setAcceptHtml(acceptHtml);
//					getEmailAddressDao().update(emailAddr);
//				}
				logger.info("saveSubscriptions() - Subscription updated: " + vo.getEmailAddr());
			}
		}
		refresh();
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	public void addSubscriptionListener(AjaxBehaviorEvent event) {
		addSubscription();
	}
	
	public String addSubscription() {
		if (isDebugEnabled)
			logger.debug("addSubscription() - Entering...");
		reset();
		this.subscription = new EmailSubscrptVo();
		MailingListVo list = getMailingListDao().getByListId(listId);
		subscription.setListId(list.getListId());
		subscription.setSubscribed(Constants.Y);
		subscription.setMarkedForEdition(true);
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
		
		// it could be called from mailing list page
		MailingListBean mailingList = (MailingListBean) FacesUtil.getManagedBean("mailingList");
		if (mailingList != null) {
			mailingList.cancelEdit();
		}
		
		return TO_CANCELED;
	}

	public boolean getSubscriptionsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getSubscriptionsMarkedForDeletion() - Entering...");
		if (subscriptions == null) {
			logger.warn("getSubscriptionsMarkedForDeletion() - Subscription List is null.");
			return false;
		}
		List<EmailSubscrptVo> subrList = getSubscriptionList();
		for (Iterator<EmailSubscrptVo> it=subrList.iterator(); it.hasNext();) {
			EmailSubscrptVo vo = it.next();
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
		String subId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - subscriptionId: " + subId);
		EmailSubscrptVo vo = getEmailSubscrptDao().getByAddrAndListId(subId, listId);
		if (editMode == false && vo != null) {
			// subscription already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "subscriptionAlreadyExist", new String[] {subId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == true && vo == null) {
			// subscription does not exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "subscriptionDoesNotExist", new String[] {subId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	void reset() {
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings({ "unchecked" })
	private List<EmailSubscrptVo> getSubscriptionList() {
		if (subscriptions == null) {
			return new ArrayList<EmailSubscrptVo>();
		}
		else {
			return (List<EmailSubscrptVo>)subscriptions.getWrappedData();
		}
	}

	public EmailSubscrptVo getSubscription() {
		return subscription;
	}

	public void setSubscription(EmailSubscrptVo subscription) {
		this.subscription = subscription;
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

	public String getListId() {
		return listId;
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
}

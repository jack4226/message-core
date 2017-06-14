package ltj.msgui.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.data.preload.MobileCarrierEnum;
import ltj.message.constant.Constants;
import ltj.message.dao.client.ClientDao;
import ltj.message.dao.customer.CustomerDao;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.PhoneNumberUtil;
import ltj.message.util.PrintUtil;
import ltj.message.util.SsnNumberUtil;
import ltj.message.vo.ClientVo;
import ltj.message.vo.CustomerVo;
import ltj.message.vo.PagingVo;
import ltj.message.vo.SearchCustVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="subscriberData")
@javax.faces.bean.ViewScoped
public class CustomersListBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = 7927665483948452101L;
	static final Logger logger = Logger.getLogger(CustomersListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient CustomerDao customerDao = null;
	private transient EmailAddressDao emailAddrDao = null;
	private transient ClientDao senderDao = null;
	
	private transient DataModel<CustomerVo> subscribers = null;
	private CustomerVo subscriber = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;

	private transient HtmlDataTable dataTable;
	private SearchCustVo searchVo = new SearchCustVo(getPagingVo());
	private String searchString = null;
	
	private transient UIInput subscriberIdInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private transient UIInput emailAddrInput = null;
	private transient UIInput ssnNumberInput = null;
	private transient UIInput dayPhoneInput = null;
	private transient UIInput eveningPhoneInput = null;
	private transient UIInput mobilePhoneInput = null;
	private transient UIInput birthDateInput = null;
	private transient UIInput endDateInput = null;
	private transient UIInput mobileCarrierInput = null;
	
	private static String TO_SELF = null;
	private static String TO_EDIT = "subscriberEdit.xhtml";
	private static String TO_FAILED = TO_SELF;
	private static String TO_LIST = "subscribersList.xhtml";
	private static String TO_SAVED = TO_LIST;
	private static String TO_CANCELED = TO_LIST;

	public DataModel<CustomerVo> getSubscribers() {
		String fromPage = sessionBean.getRequestParam("frompage");
		if (StringUtils.equals(fromPage,"main")) {
			resetPagingVo();
		}
		if (!getPagingVo().getPageAction().equals(PagingVo.PageAction.CURRENT) || subscribers == null) {
			List<CustomerVo> subscriberList = getCustomerDao().getCustomersWithPaging(searchVo);
			logger.info("PagingVo After: " + PrintUtil.prettyPrint(searchVo, 2));
			getPagingVo().setPageAction(PagingVo.PageAction.CURRENT);
			//subscribers = new ListDataModel(subscriberList);
			subscribers = new ListDataModel<CustomerVo>(subscriberList);
		}
		return subscribers;
	}
	
	@Override
	public long getRowCount() {
		long rowCount = getCustomerDao().getCustomerCount(searchVo);
		getPagingVo().setRowCount(rowCount);
		return rowCount;
	}

	public CustomerDao getCustomerDao() {
		if (customerDao == null) {
			customerDao = SpringUtil.getWebAppContext().getBean(CustomerDao.class);
		}
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
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
	
	public EmailAddressDao getEmailAddressDao() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddrDao;
	}

	public void setEmailAddressDao(EmailAddressDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public void viewSubscriberListener(AjaxBehaviorEvent event) {
		viewSubscriber();
	}
	
	public String viewSubscriber() {
		if (isDebugEnabled)
			logger.debug("viewSubscriber() - Entering...");
		if (subscribers == null) {
			logger.warn("viewSubscriber() - Subscriber List is null.");
			return TO_FAILED;
		}
		if (!subscribers.isRowAvailable()) {
			logger.warn("viewSubscriber() - Subscriber Row not available.");
			return TO_FAILED;
		}
		reset();
		this.subscriber = (CustomerVo) subscribers.getRowData();
		logger.info("viewSubscriber() - Subscriber to be edited: " + subscriber.getCustId());
		subscriber.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled) {
			logger.debug("viewSubscriber() - CustomerVo to be passed to jsp: " + subscriber);
		}
		return TO_EDIT;
	}

	public void searchByAddress(AjaxBehaviorEvent event) {
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
		return; // TO_SELF;
	}
	
	public void resetSearch(AjaxBehaviorEvent event) {
		searchString = null;
		searchVo.setEmailAddr(searchString);
		resetPagingVo();
		return; // TO_SELF;
	}
	
	@Override
	protected void refresh() {
		subscribers = null;
	}

	public void refreshPage(AjaxBehaviorEvent event) {
		refresh();
		getPagingVo().setRowCount(-1);
		return; // TO_SELF;
	}
	
	public CustomerVo getData() {
		subscriber = getCustomerDao().getByCustId(subscriber.getCustId());
		reset();
		return subscriber;
	}
	
	public void refreshSubscriber(AjaxBehaviorEvent event) {
		getData();
		FacesUtil.refreshCurrentJSFPage();
		return; // TO_SELF;
	}

	private void reset() {
		testResult = null;
		actionFailure = null;
		subscriberIdInput = null;
		emailAddrInput = null;
		ssnNumberInput = null;
		dayPhoneInput = null;
		eveningPhoneInput = null;
		mobilePhoneInput = null;
		birthDateInput = null;
		endDateInput = null;
		mobileCarrierInput = null;
	}
	
	public void deleteSubscribers(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("deleteSubscribers() - Entering...");
		if (subscribers == null) {
			logger.warn("deleteSubscribers() - Subscriber List is null.");
			return; //TO_FAILED;
		}
		reset();
		List<CustomerVo> addrList = getSubscriberList();
		for (int i=0; i<addrList.size(); i++) {
			CustomerVo vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getCustomerDao().delete(vo.getCustId());
				if (rowsDeleted > 0) {
					logger.info("deleteSubscribers() - Subscriber deleted: " + vo.getCustId());
					getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return; // TO_DELETED;
	}

	public void saveSubscriberListener(AjaxBehaviorEvent event) {
		saveSubscriber();
	}
	
	public String saveSubscriber() {
		if (isDebugEnabled)
			logger.debug("saveSubscriber() - Entering...");
		if (subscriber == null) {
			logger.warn("saveSubscriber() - Subscriber Vo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		//emailAddrInput.getValue();
		// TODO need to check email address from input field
		EmailAddressVo email = getEmailAddressDao().findByAddress(subscriber.getEmailAddr());
		subscriber.setEmailAddr(email.getEmailAddr());
		ClientVo sender = getClientDao().getByClientId(subscriber.getClientId());
		subscriber.setClientId(sender.getClientId());
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			subscriber.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		try {
			if (editMode == true) {
				getCustomerDao().update(subscriber);
				logger.info("saveSubscriber() - Rows Updated: " + 1);
			}
			else {
				getCustomerDao().insert(subscriber);
				addToList(subscriber);
				getPagingVo().setRowCount(getPagingVo().getRowCount() + 1);
				refresh();
				logger.info("saveSubscriber() - Rows Inserted: " + 1);
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	@SuppressWarnings("unchecked")
	private void addToList(CustomerVo vo) {
		List<CustomerVo> list = (List<CustomerVo>)subscribers.getWrappedData();
		list.add(vo);
	}

	public void copySubscriberListener(AjaxBehaviorEvent event) {
		copySubscriber();
	}
	
	public String copySubscriber() {
		if (isDebugEnabled)
			logger.debug("copySubscriber() - Entering...");
		if (subscribers == null) {
			logger.warn("copySubscriber() - Subscriber List is null.");
			return TO_FAILED;
		}
		reset();
		List<CustomerVo> subrList = getSubscriberList();
		for (int i=0; i<subrList.size(); i++) {
			CustomerVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.subscriber = new CustomerVo();
				try {
					vo.copyPropertiesTo(this.subscriber);
					subscriber.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
					if (subscriber.getClientId() == null) {
						subscriber.setClientId(vo.getClientId());
					}
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				//subscriber.setLastName(null);
				//subscriber.setFirstName(null);
				subscriber.setCustId(null);
				subscriber.setEmailAddr(null);
				subscriber.setUserPassword(null);
				subscriber.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public void addSubscriberListener(AjaxBehaviorEvent event) {
		addSubscriber();
	}
	
	public String addSubscriber() {
		if (isDebugEnabled)
			logger.debug("addSubscriber() - Entering...");
		reset();
		this.subscriber = new CustomerVo();
		ClientVo default_sender = getClientDao().getByClientId(Constants.DEFAULT_CLIENTID);
		subscriber.setEmailAddr(null);
		subscriber.setClientId(default_sender.getClientId());
		subscriber.setMarkedForEdition(true);
		subscriber.setUpdtUserId(Constants.DEFAULT_USER_ID);
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

	public boolean getSubscribersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getSubscribersMarkedForDeletion() - Entering...");
		if (subscribers == null) {
			logger.warn("getSubscribersMarkedForDeletion() - Subscriber List is null.");
			return false;
		}
		List<CustomerVo> addrList = getSubscriberList();
		for (Iterator<CustomerVo> it=addrList.iterator(); it.hasNext();) {
			CustomerVo vo = it.next();
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
		String subrId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - SubscriberId: " + subrId);
		CustomerVo vo = getCustomerDao().getByCustId(subrId);
		if (vo != null) {
			if (editMode == true && subscriber != null
					&& vo.getRowId() != subscriber.getRowId()) {
		        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
		        		"jpa.msgui.messages", "subscriberAlreadyExist", new String[] {subrId});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
			else if (editMode == false) {
				// subscriber already exist
		        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "subscriberAlreadyExist", new String[] {subrId});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	public void validateEmailAddress(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailAddress() - addr: " + emailAddr);
		if (StringUtils.isNotBlank(emailAddr)) {
			if (!EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
				// invalid email address
		        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "invalidEmailAddress", new String[] {emailAddr});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
			else {
				CustomerVo vo = getCustomerDao().getByEmailAddress(emailAddr);
				if (vo != null) {
					if (subscriber != null && !vo.getCustId().equals(subscriber.getCustId())) {
						FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
								"jpa.msgui.messages", "emailAddressAlreadyUsed", new String[] {emailAddr});
						message.setSeverity(FacesMessage.SEVERITY_WARN);
						throw new ValidatorException(message);
					}
				}
			}
		}
	}
	
	public void validateSsnNumber(FacesContext context, UIComponent component, Object value) {
		String ssn = (String) value;
		if (isDebugEnabled)
			logger.debug("validateSsnNumber() - SSN: " + ssn);
		if (StringUtils.isNotBlank(ssn) && !SsnNumberUtil.isValidSSN(ssn)) {
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidSsnNumber", new String[] {ssn});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateDate(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateDate() - date = " + value);
		if (value != null && !(value instanceof Date)) {
			FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidDate", new Object[] {value});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validatePhoneNumber(FacesContext context, UIComponent component, Object value) {
		String phone = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePhoneNumber() - Phone Number: " + phone);
		if (StringUtils.isNotBlank(phone) && !PhoneNumberUtil.isValidPhoneNumber(phone)) {
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidPhoneNumber", new String[] {phone});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateMibileCarrier(FacesContext context, UIComponent component, Object value) {
		String carrier = (String) value;
		if (isDebugEnabled)
			logger.debug("validateMibileCarrier() - Phone Number: " + carrier);
		if (StringUtils.isNotBlank(carrier)) {
			try {
				MobileCarrierEnum.getByValue(carrier);
			}
			catch (IllegalArgumentException e) {
		        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "invalidMobileCarrier", new String[] {carrier});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	public void validateZipCode5(FacesContext context, UIComponent component, Object value) {
		String zip5 = (String) value;
		if (isDebugEnabled)
			logger.debug("validateZipCode5() - Zip Code: " + zip5);
		if (StringUtils.isNotBlank(zip5) && !zip5.matches("\\d{5}")) {
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
	        		"jpa.msgui.messages", "invalidZipCode", new String[] {zip5});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateZipCode4(FacesContext context, UIComponent component, Object value) {
		String zip4 = (String) value;
		if (isDebugEnabled)
			logger.debug("validateZipCode4() - Zip Code: " + zip4);
		if (StringUtils.isNotBlank(zip4) && !zip4.matches("\\d{4}")) {
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
	        		"jpa.msgui.messages", "invalidZipCode", new String[] {zip4});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private List<CustomerVo> getSubscriberList() {
		if (subscribers == null) {
			return new ArrayList<CustomerVo>();
		}
		else {
			return (List<CustomerVo>)subscribers.getWrappedData();
		}
	}

	public CustomerVo getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(CustomerVo subscriber) {
		this.subscriber = subscriber;
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

	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
	}

	public UIInput getSubscriberIdInput() {
		return subscriberIdInput;
	}

	public void setSubscriberIdInput(UIInput subscriberIdInput) {
		this.subscriberIdInput = subscriberIdInput;
	}

	public UIInput getEmailAddrInput() {
		return emailAddrInput;
	}

	public void setEmailAddrInput(UIInput emailAddrInput) {
		this.emailAddrInput = emailAddrInput;
	}

	public UIInput getSsnNumberInput() {
		return ssnNumberInput;
	}

	public void setSsnNumberInput(UIInput ssnNumberInput) {
		this.ssnNumberInput = ssnNumberInput;
	}

	public UIInput getDayPhoneInput() {
		return dayPhoneInput;
	}

	public void setDayPhoneInput(UIInput dayPhoneInput) {
		this.dayPhoneInput = dayPhoneInput;
	}

	public UIInput getEveningPhoneInput() {
		return eveningPhoneInput;
	}

	public void setEveningPhoneInput(UIInput eveningPhoneInput) {
		this.eveningPhoneInput = eveningPhoneInput;
	}

	public UIInput getMobilePhoneInput() {
		return mobilePhoneInput;
	}

	public void setMobilePhoneInput(UIInput mobilePhoneInput) {
		this.mobilePhoneInput = mobilePhoneInput;
	}

	public UIInput getBirthDateInput() {
		return birthDateInput;
	}

	public void setBirthDateInput(UIInput birthDateInput) {
		this.birthDateInput = birthDateInput;
	}

	public UIInput getEndDateInput() {
		return endDateInput;
	}

	public void setEndDateInput(UIInput endDateInput) {
		this.endDateInput = endDateInput;
	}

	public UIInput getMobileCarrierInput() {
		return mobileCarrierInput;
	}

	public void setMobileCarrierInput(UIInput mobileCarrierInput) {
		this.mobileCarrierInput = mobileCarrierInput;
	}
}

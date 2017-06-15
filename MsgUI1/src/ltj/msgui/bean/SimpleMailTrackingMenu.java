package ltj.msgui.bean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.data.preload.FolderEnum;
import ltj.message.constant.RuleType;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.vo.PagingVo;
import ltj.message.vo.UserVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.inbox.SearchFieldsVo;
import ltj.message.vo.inbox.SearchFieldsVo.RuleName;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

/**
 * This is a request scoped bean that holds search fields from HTTP request.
 * Whenever MsgInboxBean.getAll() gets called, it retrieves search fields from
 * this bean and uses them to construct a query to retrieve mails from database.
 * By doing this, if a user clicks browser's back button followed by refresh
 * button, the email list returned will still be okay.
 * 
 * Note: request scoped did not work as expected, changed to session scoped.
 */
@ManagedBean(name="mailTracking")
@javax.faces.bean.ViewScoped
public class SimpleMailTrackingMenu extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = -4430208005555443392L;
	static final Logger logger = Logger.getLogger(SimpleMailTrackingMenu.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private String titleKey;
	private String functionKey = null;
	private String ruleName = RuleName.All.name();
	private String fromAddress = null;
	private String toAddress = null;
	private String subject = null;
	private String body = null;

	private final String defaultFolder = FolderEnum.Inbox.name();
	private String defaultRuleName = RuleName.All.name();
	private String defaultToAddr = null;
	
	private final static String TO_SELF = null;
	
	private transient EmailAddressDao emailAddrDao;
	private transient MsgInboxDao msgInboxDao;
	
	public SimpleMailTrackingMenu() {
		initDefaultSearchValues();
		functionKey = defaultFolder;
		ruleName = defaultRuleName;
	}
	
	@Override
	protected void refresh() {
		// dummy to satisfy super class
	}

	@Override
	public long getRowCount() { // dummy to satisfy super class
		return 0;
	}

	void initDefaultSearchValues() {
		// initialize search fields from user's default settings.
		UserVo userVo = FacesUtil.getLoginUserVo();
		if (userVo != null) {
			defaultRuleName = userVo.getDefaultRuleName();
			if (StringUtils.isBlank(defaultRuleName)) {
				defaultRuleName = RuleType.ALL.value();
			}
			if (userVo.getEmailAddr()!=null) {
				defaultToAddr = userVo.getEmailAddr();
				if (StringUtils.isBlank(defaultToAddr)) {
					defaultToAddr = null;
				}
				else {
					getEmailAddressDao().findByAddress(defaultToAddr);
				}
			}
		}
		else {
			logger.error("constructor - UserData not found in HTTP session.");
		}
	}
	
	public String resetSearchFields() {
		ruleName = defaultRuleName;
		fromAddress = null;
		toAddress = defaultToAddr;
		subject = null;
		body = null;
		return TO_SELF;
	}
	
	/*
	 * used by SimpleMailTrackingMenu.xhtml to reset search folder when
	 * navigated from main menu.
	 */
	public void resetFolderIfFromMain() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (StringUtils.equals(fromPage, "main")) {
			functionKey = defaultFolder;
		}
	}

	public void selectAllListener(AjaxBehaviorEvent event) {
		logger.info("Entering selectAllListener()...");
		functionKey = FolderEnum.All.name();
		updateMessageInboxBean();
		return; // TO_SELF;
	}
	
	public void selectReceivedListener(AjaxBehaviorEvent event) {
		logger.info("Entering selectReceivedListener()...");
		functionKey = FolderEnum.Inbox.name();
		updateMessageInboxBean();
		return; // TO_SELF;
	}
	
	public void selectSentListener(AjaxBehaviorEvent event) {
		logger.info("Entering selectSentListener()...");
		functionKey = FolderEnum.Sent.name();
		updateMessageInboxBean();
		return; // TO_SELF;
	}
	
	public void selectDraftListener(AjaxBehaviorEvent event) {
		logger.info("Entering selectDraftListener()...");
		functionKey = FolderEnum.Draft.name();
		updateMessageInboxBean();
		return; // TO_SELF;
	}
	
	public void selectClosedListener(AjaxBehaviorEvent event) {
		logger.info("Entering selectClosedListener()...");
		functionKey = FolderEnum.Closed.name();
		//getPagingVo().setSearchCriteria(PagingVo.Column.statusId, new PagingVo.Criteria(RuleCriteria.EQUALS, MsgStatusCode.CLOSED.getValue()));
		updateMessageInboxBean();
		return; // TO_SELF;
	}
	
	void updateMessageInboxBean() {
		MsgInboxBean bean = (MsgInboxBean) FacesUtil.getManagedBean("messageInbox");
		if (bean != null) {
			SearchFieldsVo beanSearchVo = bean.getSearchFieldsVo();
			//logger.info("Menu SearchFieldVo: " + getSearchFieldsVo());
			//logger.info("Inbox SearchFieldVo: " + beanSearchVo);
			if (!getSearchFieldsVo().equalsLevel1(beanSearchVo)) {
				if (getSearchFieldsVo().getPagingVo().getLogList().size() > 0) {
					logger.info("updateMessageInboxBean() - Search criteria changes:  After <-> Before\n" + getSearchFieldsVo().getPagingVo().listChanges());
				}
				getSearchFieldsVo().copyLevel1To(beanSearchVo);
			}
			bean.resetPagingVo();
		}
	}
	
	/**
	 * This method is designed to go along with following JSF tag:
	 * <h:selectOneMenu value="#{mailtracking.ruleName}" onclick="submit()"
	 * valueChangeListener="#{mailtracking.ruleNameChanged}"/>
	 * 
	 * @param event
	 * 
	 * @deprecated "Search" button is now used to submit rule name changes. The
	 *             value change listener gets executed every time in JSF life
	 *             cycle, so method bean.viewAll() gets called every time which
	 *             resets "folder" to null. This behavior has caused all other
	 *             methods that rely on "folder" to fail.
	 */
	public void ruleNameChanged(ValueChangeEvent event) {
		/*
		 * <h:selectOneMenu value="#{mailTracking.ruleName}" onchange="submit()"
		 *	valueChangeListener="#{mailTracking.ruleNameChanged}"/>
		 */
		logger.info("Entering ruleNameChanged()...");
		MsgInboxBean bean = (MsgInboxBean) FacesUtil.getSessionMapValue("msgfolder");
		if (bean == null) {
			logger.error("ruleNameChanged() - failed to retrieve MsgInboxBean from HTTP session");
			return;
		}
		String newValue = (String) event.getNewValue();
		String oldValue = (String) event.getOldValue();
		if (newValue != null && !newValue.equals(oldValue)) {
			bean.viewAll(); // reset view search criteria
			bean.getSearchFieldsVo().setRuleName(newValue);
		}
	}
	
	public void searchBySearchVoListener(AjaxBehaviorEvent event) {
		logger.info("Entering searchBySearchVo()...");
		updateMessageInboxBean();
		return; // TO_SELF;
	}
	
	public void resetSearchFieldsListener(AjaxBehaviorEvent event) {
		resetSearchFields();
		updateMessageInboxBean();
	}
	
	public void checkEmailAddress(FacesContext context, UIComponent component, Object value) {
		if (value == null || !(value instanceof String)) {
			return;
		}
		
		String addr = (String) value;
		if (StringUtils.isBlank(addr)) {
			return;
		}
		
		EmailAddressVo vo = getEmailAddressDao().getByAddress(addr);
		if (vo == null) {
			FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "emailAddressNotFound", new String[] {addr});
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        throw new ValidatorException(message);
		}
	}
	
	public SearchFieldsVo getSearchFieldsVo() {
		SearchFieldsVo vo = new SearchFieldsVo(getPagingVo());
		FolderEnum msgType = null;
		try {
			msgType = FolderEnum.getByName(functionKey);
		}
		catch (IllegalArgumentException e) {
			logger.error("IllagalArgumentException caught: " + e.getMessage());
			msgType = FolderEnum.Inbox;
		}
		vo.setFolderType(msgType);
		vo.setRuleName(ruleName);
		vo.getPagingVo().setSearchValue(PagingVo.Column.fromAddr, fromAddress);
		if (StringUtils.isNotBlank(fromAddress)) {
			EmailAddressVo from = getEmailAddressDao().getByAddress(fromAddress);
			if (from != null) {
				vo.getPagingVo().setSearchValue(PagingVo.Column.fromAddrId, from.getEmailAddrId());
			}
		}
		if (StringUtils.isNotBlank(toAddress)) {
			EmailAddressVo to = getEmailAddressDao().getByAddress(toAddress);
			if (to != null) {
				vo.getPagingVo().setSearchValue(PagingVo.Column.toAddrId, to.getEmailAddrId());
			}
		}
		vo.getPagingVo().setSearchValue(PagingVo.Column.msgSubject, subject);
		vo.getPagingVo().setSearchValue(PagingVo.Column.msgBody, body);
		
		return vo;
	}
	
	public int getInboxUnreadCount() {
		return getMsgInboxDao().getInboxUnreadCount();
	}

	public int getSentUnreadCount() {
		return getMsgInboxDao().getSentUnreadCount();
	}

	public int getAllUnreadCount() {
		return getMsgInboxDao().getAllUnreadCount();
	}

	// PROPERTY: titleKey
	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

	public String getTitleKey() {
		return titleKey;
	}

	public String getFunctionKey() {
		return functionKey;
	}

	public void setFunctionKey(String function) {
		this.functionKey = function;
	}
	
	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
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

	public MsgInboxDao getMsgInboxDao() {
		if (msgInboxDao == null) {
			msgInboxDao = SpringUtil.getWebAppContext().getBean(MsgInboxDao.class);
		}
		return msgInboxDao;
	}

	public void setMsgInboxDao(MsgInboxDao msgInboxDao) {
		this.msgInboxDao = msgInboxDao;
	}

}

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

import ltj.message.dao.client.ClientDao;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.user.UserDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.vo.ClientVo;
import ltj.message.vo.UserVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="userData")
@javax.faces.bean.ViewScoped
public class UserDataBean implements java.io.Serializable {
	private static final long serialVersionUID = 2276036390316734499L;
	static final Logger logger = Logger.getLogger(UserDataBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient UserDao userDao = null;
	private transient EmailAddressDao emailAddrDao = null;
	private transient ClientDao senderDao = null;
	
	private transient DataModel<UserVo> users = null;
	private UserVo user = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput userIdInput = null;
	private String userEmailAddr = null;
	private String userSenderId = null;
	
	private String testResult = null;
	private String actionFailure = null;

	private static String TO_EDIT = "userAccountEdit.xhtml";
	private static String TO_FAILED = null;
	private static String TO_SAVED = "manageUserAccounts.xhtml";
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public DataModel<UserVo> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (users == null) {
			List<UserVo> userList = getUserDao().getFirst100(false);
			users = new ListDataModel<UserVo>(userList);
		}
		return users;
	}
	
	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}

	public String refresh() {
		users = null;
		return "";
	}
	
	public UserDao getUserDao() {
		if (userDao == null) {
			userDao = SpringUtil.getWebAppContext().getBean(UserDao.class);
		}
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public EmailAddressDao getEmailAddressDao() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddrDao;
	}
	public ClientDao getClientDao() {
		if (senderDao == null) {
			senderDao = SpringUtil.getWebAppContext().getBean(ClientDao.class);
		}
		return senderDao;
	}
	
	public void viewUserListener(AjaxBehaviorEvent event) {
		viewUser();
	}

	public String viewUser() {
		if (isDebugEnabled)
			logger.debug("viewUser() - Entering...");
		if (users == null) {
			logger.warn("viewUser() - User List is null.");
			return TO_FAILED;
		}
		if (!users.isRowAvailable()) {
			logger.warn("viewUser() - User Row not available.");
			return TO_FAILED;
		}
		reset();
		this.user = (UserVo) users.getRowData();
		logger.info("viewUser() - User to be edited: " + user.getUserId());
		user.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (user.getEmailAddr()!=null) {
			setUserEmailAddr(user.getEmailAddr());
		}
		if (user.getClientId()!=null) {
			setUserSenderId(user.getClientId());
		}
		if (isDebugEnabled)
			logger.debug("viewUser() - UserVo to be passed to jsp: " + user);
		
		return TO_EDIT;
	}
	
	public void saveUserListener(AjaxBehaviorEvent event) {
		saveUser();	
	}
	
	public String saveUser() {
		if (isDebugEnabled)
			logger.debug("saveUser() - Entering...");
		if (user == null) {
			logger.warn("saveUser() - UserVo is null.");
			return TO_FAILED;
		}
		reset();
		if (StringUtils.isNotBlank(getUserEmailAddr())) {
			if (!EmailAddrUtil.isRemoteEmailAddress(getUserEmailAddr())) {
				testResult = "invalidEmailAddress";
				return null;
			}
			if (user.getEmailAddr()!=null) {
				if (EmailAddrUtil.compareEmailAddrs(user.getEmailAddr(), getUserEmailAddr())!=0) {
					EmailAddressVo newAddr = getEmailAddressDao().findByAddress(getUserEmailAddr());
					user.setEmailAddr(newAddr.getEmailAddr());
				}
			}
			else {
				EmailAddressVo newAddr = getEmailAddressDao().findByAddress(getUserEmailAddr());
				user.setEmailAddr(newAddr.getEmailAddr());
			}
		}
		else {
			if (user.getEmailAddr()!=null) {
				user.setEmailAddr(null);
			}
		}
		if (StringUtils.isNotBlank(getUserSenderId())) {
			if (user.getClientId()!=null) {
				if (!getUserSenderId().equals(user.getClientId())) {
					ClientVo sender = getClientDao().getByClientId(getUserSenderId());
					user.setClientId(sender.getClientId());
				}
			}
		}
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			user.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getUserDao().update(user);
			logger.info("in saveUser() - Rows Updated: " + 1);
		}
		else {
			getUserDao().insert(user);
			getUserList().add(user);
			logger.info("saveUser() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	public void deleteUsersListener(AjaxBehaviorEvent event) {
		deleteUsers();
	}
	
	public String deleteUsers() {
		if (isDebugEnabled)
			logger.debug("deleteUsers() - Entering...");
		if (users == null) {
			logger.warn("deleteUsers() - User List is null.");
			return TO_FAILED;
		}
		reset();
		List<UserVo> smtpList = getUserList();
		for (int i=0; i<smtpList.size(); i++) {
			UserVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getUserDao().deleteByUserId(vo.getUserId());
				if (rowsDeleted > 0) {
					logger.info("deleteUsers() - User deleted: " + vo.getUserId());
				}
				smtpList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void copyUserListener(AjaxBehaviorEvent event) {
		copyUser();
	}
	
	public String copyUser() {
		if (isDebugEnabled)
			logger.debug("copyUser() - Entering...");
		if (users == null) {
			logger.warn("copyUser() - User List is null.");
			return TO_FAILED;
		}
		reset();
		List<UserVo> smtpList = getUserList();
		for (int i=0; i<smtpList.size(); i++) {
			UserVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.user = new UserVo();
				try {
					vo.copyPropertiesTo(this.user);
					user.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
					user.setHits(0);
					user.setLastVisitTime(null);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				user.setUserId(null);
				user.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public void addUserListener(AjaxBehaviorEvent event) {
		addUser();
	}
	
	public String addUser() {
		if (isDebugEnabled)
			logger.debug("addUser() - Entering...");
		reset();
		this.user = new UserVo();
		user.setMarkedForEdition(true);
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
	
	public boolean getAnyUsersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyUsersMarkedForDeletion() - Entering...");
		if (users == null) {
			logger.warn("getAnyUsersMarkedForDeletion() - User List is null.");
			return false;
		}
		List<UserVo> smtpList = getUserList();
		for (Iterator<UserVo> it=smtpList.iterator(); it.hasNext();) {
			UserVo vo = it.next();
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
		String userId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - userId: " + userId);

		UserVo vo = getUserDao().getByUserId(userId);
		if (editMode == false && vo != null) {
			// user already exist
	        FacesMessage message =ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "userAlreadyExist", new String[] {userId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == true && vo == null) {
			// user does not exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "userDoesNotExist", new String[] {userId});
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
		userIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<UserVo> getUserList() {
		if (users == null) {
			return new ArrayList<UserVo>();
		}
		else {
			return (List<UserVo>)users.getWrappedData();
		}
	}
	
	public UserVo getUser() {
		return user;
	}

	public void setUser(UserVo user) {
		this.user = user;
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

	public String getUserEmailAddr() {
		return userEmailAddr;
	}

	public void setUserEmailAddr(String userEmailAddr) {
		this.userEmailAddr = userEmailAddr;
	}

	public String getUserSenderId() {
		return userSenderId;
	}

	public void setUserSenderId(String userSenderId) {
		this.userSenderId = userSenderId;
	}

	public UIInput getUserIdInput() {
		return userIdInput;
	}

	public void setUserIdInput(UIInput userIdInput) {
		this.userIdInput = userIdInput;
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

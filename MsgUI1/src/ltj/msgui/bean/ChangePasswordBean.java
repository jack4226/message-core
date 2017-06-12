package ltj.msgui.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ltj.message.dao.user.UserDao;
import ltj.message.vo.UserVo;
import ltj.msgui.filter.SessionTimeoutFilter;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="changePassword")
@RequestScoped
public class ChangePasswordBean implements java.io.Serializable {
	private static final long serialVersionUID = -7332671123699551896L;
	static final Logger logger = Logger.getLogger(ChangePasswordBean.class);
	private String currPassword = null;
	private String password = null;
	private String confirm = null;
	private String message = null;
	
	private UserDao userDao = null;
	
	private static String TO_FAILED = null;
	private static String TO_SAVED = "main.xhtml";

	public String changePassword() {
		message = null;
		UserVo vo = getSessionUserData();
		if (vo == null) {
			message = "User is not logged in!";
			return TO_FAILED;
		}
		UserVo vo2 = getUserDao().getByUserId(vo.getUserId());
		if (vo2 == null) {
			message = "Internal error, contact programming!";
			return TO_FAILED;
		}
		logger.info("changePassword() - UserId: " +  vo.getUserId());
		if (!vo2.getPassword().equals(currPassword)) {
			message = "Current password is invalied.";
			return TO_FAILED;
		}
		vo2.setPassword(password);
		getUserDao().update(vo2);
		logger.info("changePassword() - rows updated: " + 1);
		return TO_SAVED;
	}
	
	private UserDao getUserDao() {
		if (userDao == null) {
			userDao = SpringUtil.getWebAppContext().getBean(UserDao.class);
		}
		return userDao;
	}
	
    // Getters
    public UserVo getSessionUserData() {
		return (UserVo) getHttpSession().getAttribute(SessionTimeoutFilter.USER_VO_ID);
	}

	public void setSessionUserData(UserVo userVo) {
		getHttpSession().setAttribute(SessionTimeoutFilter.USER_VO_ID, userVo);
	}

	public HttpSession getHttpSession() {
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		return ((HttpSession) ctx.getSession(true));
	}
    
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getConfirm() {
		return confirm;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public String getCurrPassword() {
		return currPassword;
	}

	public void setCurrPassword(String currPassword) {
		this.currPassword = currPassword;
	}
}

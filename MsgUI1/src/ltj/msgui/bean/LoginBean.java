package ltj.msgui.bean;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.message.constant.Constants;
import ltj.message.dao.client.ClientUtil;
import ltj.message.dao.user.UserDao;
import ltj.message.vo.UserVo;
import ltj.msgui.filter.SessionTimeoutFilter;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

public class LoginBean {
	static final Logger logger = LogManager.getLogger(LoginBean.class);
	private String userId = null;
	private String password = null;
	private String message = null;
	private String source = null; // login or timeout
	
	private UserDao userDao = null;
	
	public String login() {
		logger.info("login() - UserId: " +  userId);
		message = null;
		UserVo vo = getUserDao().getForLogin(userId, password);
		if (vo == null) {
			message = "Unknown UserId and/or invalid password!";
			return null;
		}
		if (isUserLoggedin()) {
			logout();
		}
		vo.setPassword(null); // for security
		setSessionUserVo(vo);
		//logger.info("login() - user logged in: " + userId);
		if (Constants.ADMIN_ROLE.equals(vo.getRole())) {
			return Constants.ADMIN_ROLE;
		}
		else {
			return Constants.USER_ROLE;
		}
	}
	
    public String logout() {
    	getHttpSession().invalidate();
    	// invalidate() will trigger SessionExpiredListener.sessionDestroyed()
		// method to perform clean up.
		return "login";
	}
    
    public String changePassword() {
    	return null;
    }
    
    // Getters
    public UserVo getSessionUserVo() {
        return (UserVo) getHttpSession().getAttribute(SessionTimeoutFilter.USER_VO_ID);
    }

    public void setSessionUserVo(UserVo userVo) {
		getHttpSession().setAttribute(SessionTimeoutFilter.USER_VO_ID, userVo);
    }
    
    public HttpSession getHttpSession() {
    	ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
    	return ((HttpSession) ctx.getSession(true));
    }
    
    // Checkers
    public boolean isUserLoggedin() {
    	return (getSessionUserVo() != null);
    }

    public boolean getIsAdmin() {
    	if (getSessionUserVo() == null) {
    		return false;
    	}
    	else {
    		return getSessionUserVo().getIsAdmin();
    	}
    }
    
    public boolean isCurrentPageMainPage() {
    	String viewId = FacesUtil.getCurrentViewId();
    	return "/main.jsp".equals(viewId);
    }
    
	public boolean getIsProductKeyValid() {
		boolean isValid = ClientUtil.isProductKeyValid();
		return isValid;
	}
	
	public boolean getIsTrialPeriodExpired() {
		return ClientUtil.isTrialPeriodEnded();
	}

    public String getMainPage() {
    	return getHttpSession().getServletContext().getContextPath() + "/main.faces";
    }
    
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public UserDao getUserDao() {
		if (userDao == null) {
			userDao = (UserDao) SpringUtil.getWebAppContext().getBean("userDao");
		}
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
}

package com.legacytojava.msgui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.UserVo;

/**
 * The UserVo filter.
 */
public class SessionTimeoutFilter implements Filter {
	static final Logger logger = Logger.getLogger(SessionTimeoutFilter.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	private String timeoutPage = "/login.faces";
	private String loginPage = "/login.faces";
	private String noPermissionPage = "/noPermission.faces";
	/** The unique ID to set and get the UserVo from the HttpSession. */
	public static final String USER_VO_ID = "SessionTimeoutFilter.UserVo";

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) {
		// Nothing to do here.
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String pathInfo = StringUtil
				.trim(httpRequest.getRequestURI(), httpRequest.getContextPath());
		// Check PathInfo
		if (isPageSessioned(httpRequest, pathInfo) == false) {
			chain.doFilter(request, response);
			return;
		}
		// Get UserVo from HttpSession
		HttpSession httpSession = httpRequest.getSession();
		UserVo userVo = (UserVo) httpSession.getAttribute(USER_VO_ID);
		if (userVo == null || isSessionInvalid(httpRequest)) {
			// No UserVo found in HttpSession
			String timeoutUrl = httpRequest.getContextPath() + timeoutPage + "?source=timeout";
			logger.info("doFilter() - session is invalid! redirecting to: " + timeoutUrl);
			httpResponse.sendRedirect(timeoutUrl);
			return;
		}
		if (!isPagePermitted(userVo, pathInfo)) {
			String noPermissionUrl = httpRequest.getContextPath() + noPermissionPage;
			httpResponse.sendRedirect(noPermissionUrl);
			return;
		}
		// Add hit count and update UserVo
		userVo.addHit();
		// Continue filtering
		chain.doFilter(request, response);
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// Apparently there's nothing to destroy?
	}
	
	/**
	 * Session shouldn't be checked for certain pages. For example for timeout
	 * page. Since we're redirecting to timeout page from this filter, if we
	 * don't disable session control for it, filter will again redirect to it
	 * and this will be result with an infinite loop.
	 */
	private boolean isPageSessioned(HttpServletRequest httpRequest, String pathInfo) {
		boolean pageSessioned = false;
		if (pathInfo.startsWith("/includes") || pathInfo.startsWith("/images")
				|| pathInfo.startsWith("/htmls")) {
			// This is not necessary, but it might be useful if you want to skip
			// some include files. If those include files are loaded, continue 
			// the filter chain and abort this filter, because it is usually not
			// necessary to lookup for any UserVo then. Or, if the url-pattern 
			// in the web.xml is specific enough, this if-block can be removed.
			pageSessioned = false;
		}
		if (pathInfo.matches("^(?:/[a-zA-Z]{5,8})?/[a-zA-Z]\\w{0,250}\\.(?:faces|jsp).*")) {
			// file name matches "/*.faces" or "/*.jsp"
			if (!pathInfo.startsWith(loginPage) && !pathInfo.startsWith(timeoutPage)
					&& !pathInfo.startsWith(noPermissionPage)
					&& !pathInfo.startsWith("/publicsite")) {
				// not login page and timeout page
				pageSessioned = true;
			}
		}
		if (isDebugEnabled) {
			logger.debug("isPageSessioned() - pathInfo: " + pathInfo + ", " + pageSessioned);
		}
		return pageSessioned;
	}
	
	boolean isPagePermitted(UserVo userVo, String pathInfo) {
		//logger.info("isPagePermitted() - pathInfo: " + pathInfo);
		if (userVo == null) {
			return false;
		}
		if (pathInfo.startsWith("/admin/") && !Constants.ADMIN_ROLE.equals(userVo.getRole())) {
			return false;
		}
		return true;
	}
	
	private boolean isSessionInvalid(HttpServletRequest httpRequest) {
		boolean sessionInValid = (httpRequest.getRequestedSessionId() != null)
				&& !httpRequest.isRequestedSessionIdValid();
		return sessionInValid;
	}
}

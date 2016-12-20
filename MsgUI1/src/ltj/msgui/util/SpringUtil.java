package com.legacytojava.msgui.util;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringUtil {
	static WebApplicationContext webContext = null;
	
	/**
	 * get WebApplicationContext, calling from a JSF managed bean.
	 * 
	 * @return a WebApplicationContext reference
	 */
	public static WebApplicationContext getWebAppContext() {
		if (webContext == null) {
			FacesContext facesCtx = FacesContext.getCurrentInstance();
			ServletContext sctx = (ServletContext) facesCtx.getExternalContext().getContext();
			webContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sctx);
		}
		return webContext;
	}
	
	/**
	 * get WebApplicationContext, calling from a Servlet
	 * 
	 * @param sctx -
	 *            a servlet context
	 * @return a WebApplicationContext reference
	 */
	public static WebApplicationContext getWebAppContext(ServletContext sctx) {
		WebApplicationContext webContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(sctx);
		return webContext;
	}	
}

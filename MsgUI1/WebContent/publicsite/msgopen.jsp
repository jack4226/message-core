<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page trimDirectiveWhitespaces="true" %>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="ltj.message.vo.emailaddr.EmailAddressVo"%>
<%@page import="ltj.message.vo.inbox.MsgClickCountVo"%>
<%@page import="ltj.message.util.StringUtil"%>
<%
	Logger logger = Logger.getLogger("com.legacytojava.jsp");
	ServletContext ctx = application;
	String sbsrId = request.getParameter("sbsrid");
	String listId = request.getParameter("listid");
	int rowsUpdated = 0;
	if (!StringUtil.isEmpty(sbsrId) && !StringUtil.isEmpty(listId)) {
		// update subscriber open count
		try {
			EmailAddressVo addrVo = getEmailAddrDao(ctx).getByAddrId(Long.parseLong(sbsrId));
			if (addrVo != null) {
				rowsUpdated += getSubscriptionDao(ctx).updateOpenCount(
						addrVo.getEmailAddrId(), listId);
			}
		}
		catch (NumberFormatException e) {
			logger.error("NumberFormatException caught: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
	else {
		logger.info("msgopen.jsp - sbsrid or listid is not valued.");
	}

	String msgId = request.getParameter("msgid");
	if (!StringUtil.isEmpty(msgId)) {
		// update newsletter open count
		try {
			MsgClickCountVo countVo = getMsgClickCountsDao(ctx).getByPrimaryKey(Long.parseLong(msgId));
			if (countVo != null) {
				rowsUpdated += getMsgClickCountsDao(ctx).updateOpenCount(
						countVo.getMsgId(), 1);
			}
		}
		catch (NumberFormatException e) {
			logger.error("NumberFormatException caught: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
	else {
		logger.info("msgopen.jsp - msgid is not valued.");
	}
	
	logger.info("msgopen.jsp - rows updated: " + rowsUpdated);

	boolean isInfoEnabled = false; //logger.isInfoEnabled();
	if (isInfoEnabled) {
		logger.info("ServletContext RealPath: " + application.getRealPath("./"));
		logger.info("Request Servlet path: " + request.getServletPath());
		logger.info("Request Context path: " + request.getContextPath());
	}
	%>

<%-- Now serve the space.gif file --%>
<%@ include file="./serveImage.jsp" %>
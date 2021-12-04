<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page trimDirectiveWhitespaces="true" %>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="ltj.message.vo.emailaddr.EmailAddressVo"%>
<%@page import="ltj.message.vo.inbox.MsgClickCountVo"%>
<%@page import="ltj.message.util.StringUtil"%>
<%
	Logger logger = LogManager.getLogger("com.legacytojava.jsp");
	ServletContext ctx = application;
	String sbsrId = request.getParameter("sbsrid");
	String listId = request.getParameter("listid");
	int rowsUpdated = 0;
	if (!StringUtil.isEmpty(sbsrId) && !StringUtil.isEmpty(listId)) {
		// update subscriber click count
		try {
			EmailAddressVo addrVo = getEmailAddressDao(ctx).getByAddrId(Long.parseLong(sbsrId));
			if (addrVo != null) {
				rowsUpdated += getEmailSubscrptDao(ctx).updateClickCount(
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
		logger.info("msgclick.jsp - sbsrid or listid is not valued.");
	}

	String msgId = request.getParameter("msgid");
	if (!StringUtil.isEmpty(msgId)) {
		// update newsletter click count
		try {
			MsgClickCountVo countVo = getMsgClickCountDao(ctx).getByPrimaryKey(Long.parseLong(msgId));
			if (countVo != null) {
				rowsUpdated += getMsgClickCountDao(ctx).updateClickCount(
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
		logger.info("msgclick.jsp - msgid is not valued.");
	}
	
	logger.info("msgclick.jsp - rows updated: " + rowsUpdated);
	%>

<%-- Now serve the space.gif file --%>
<%@ include file="./serveImage.jsp" %>
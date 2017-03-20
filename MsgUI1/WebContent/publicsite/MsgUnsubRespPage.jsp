<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="ltj.msgui.publicsite.messages" var="bndl"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="./styles.css" rel="stylesheet" type="text/css">
<title><fmt:message key="unsubscribeFromMailingLists" bundle="${bndl}"/></title>
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body>
<div align="center">

<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="1" cellpadding="1">
	<tr>
		<td colspan="2">
		<table class="gettingStartedHeader">
			<tr>
				<td><span class="gettingStartedTitle">
				<fmt:message key="unsubscribeConfirmTitle" bundle="${bndl}"/>
				</span></td>
			</tr>
		</table>
		</td>
	</tr>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="ltj.message.vo.emailaddr.EmailAddressVo"%>
<%@page import="ltj.message.vo.inbox.MsgClickCountVo"%>
<%@page import="ltj.message.dao.inbox.MsgUnsubCmntDao"%>
<%@page import="ltj.message.vo.inbox.MsgUnsubCmntVo"%>
<%!
MsgUnsubCmntDao unsubCommentsDao = null;
MsgUnsubCmntDao getMsgUnsubCmntDao(ServletContext ctx) {
	if (unsubCommentsDao == null) {
		unsubCommentsDao = (MsgUnsubCmntDao) SpringUtil.getWebAppContext(ctx).getBean("msgUnsubCmntDao");
	}
	return unsubCommentsDao;
}
%>
<%
	Logger logger = Logger.getLogger("com.legacytojava.jsp");
	ServletContext ctx = application;
	
	String sbsrId = request.getParameter("sbsrid");
	Long sbsrIdLong = null;
	String msgId = request.getParameter("msgid");
	String listId = request.getParameter("listid");
	String comments = request.getParameter("comments");
	String submit = request.getParameter("submit");
	EmailAddressVo addrVo = null;
	MailingListVo listVo = null;
	int unsubscribed = 0;
	String unsubListName = listId;
	try {
		sbsrIdLong = Long.valueOf(sbsrId);
		addrVo = getEmailAddrDao(ctx).getByAddrId(Long.parseLong(sbsrId));
		listVo = getMailingListDao(ctx).getByListId(listId);
		if (submit != null && submit.length() > 0 && addrVo != null && listVo != null) {
			unsubscribed = getSubscriptionDao(ctx).unsubscribe(addrVo.getEmailAddrId(), listId);
			if (unsubscribed > 0) {
				MailingListVo vo = getMailingListDao(ctx).getByListId(listId);
				if (vo != null) {
					unsubListName += " - " + vo.getDisplayName();
				}
				else {
					logger.error("MsgUnsubRespPage.jsp - Failed to find mailing list by Id: " + listId);
				}
			}
			pageContext.setAttribute("unsubListName", unsubListName);
			// add user comments
			if (unsubscribed > 0 && comments != null && comments.trim().length() > 0) {
				try {
					MsgUnsubCmntVo commVo = new MsgUnsubCmntVo();
					commVo.setMsgId(Long.parseLong(msgId));
					commVo.setEmailAddrId(addrVo.getEmailAddrId());
					commVo.setListId(listId);
					commVo.setComments(comments.trim());
					int rowsInsrted = getMsgUnsubCmntDao(ctx).insert(commVo);
					logger.info("MsgUnsubRespPage.jsp - unsubcription commonts added: " + rowsInsrted);
				}
			 	catch (Exception e) {
			 		logger.error("MsgUnsubRespPage.jsp - add comments: " + e.toString());
			 	}
			}
		}
	}
	catch (Exception e) {
		logger.error("MsgUnsubRespPage.jsp", e);
	}
	
	MsgClickCountVo countVo = null;
	try {
		countVo = getMsgClickCountsDao(ctx).getByPrimaryKey(Long.parseLong(msgId));
		if (countVo == null) {
			logger.error("MsgUnsubRespPage.jsp - Newsletter MsgId " + msgId + " not found");
		}
		else {
			int rows = getMsgClickCountsDao(ctx).updateUnsubscribeCount(countVo.getMsgId(), 1);
			logger.info("MsgUnsubRespPage.jsp - un-subscribe count updated: " + rows);
		}
	}
 	catch (Exception e) {
 		logger.error("MsgUnsubRespPage.jsp - msgid: " + e.toString());
 	}

	if (unsubscribed > 0 && addrVo != null && listVo != null) {
		Map<String, String> listMap = new HashMap<String, String>();
		listMap.put("_UnsubscribedMailingLists", unsubListName);
		//listMap.put("SubscriberAddressId", addrVo.getEmailAddrId());
		getMailingListBo(ctx).send(addrVo.getEmailAddr(), listMap, "UnsubscriptionLetter");
	%>
 	<tr>
	 	<td align="center" colspan="2">
		 	<table width="90%" border="0">
			<tr>
				<td colspan="2">
					<img src="./images/space.gif" height="10" style="border: 0px">
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<b><fmt:message key="unsubscribeConfirmLabel" bundle="${bndl}"/></b>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<img src="./images/space.gif" height="10" style="border: 0px">
				</td>
			</tr>
			</table>
	 	</td>
 	</tr>
	<tr>
		<td style="width: 80%;" colspan="2" align="center">
		<table style="width: 80%; background: #E5F3FF;" class="jsfDataTable" border="1" cellspacing="0" cellpadding="8">
			<tr>
				<th style="width: 100%;">List Name un-subscribed</th>
			</tr>
			<tr>
				<td style="width: 100%;"><%= unsubListName %></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td style="width: 80%;" colspan="2" align="center">
		<table style="width: 80%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td>
					<span style="font-weight: bold; font-size: 1.0em;">&nbsp;
					<fmt:message key="subscriberEmailAddress" bundle="${bndl}"/></span>&nbsp;
					<b><%= addrVo == null ? "" : addrVo.getEmailAddr() %></b>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="2" align="center">
		<table style="width: 90%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td style="footNote">&nbsp;<br/>
				If you did this by mistake and want to edit your user profile or re-subscribe, 
				<a href="<%= renderURLVariable(ctx, "UserProfileURL", sbsrIdLong) %>">click here.</a>
				</td>
			</tr>
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
		</table>
		</td>
	</tr>
<%	} else { %>
	<tr>
		<td colspan="2" align="center">
		<table style="width: 90%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td class="errorMessage" style="font-size: 1.2em;">&nbsp;<br/>
				<% if (addrVo != null && listVo != null) { %>
				You have already un-subscribed from the list.
				<% } else { %>
				The subscriber code or HTTP request is invalid.
				<% } %>
				</td>
			</tr>
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
		</table>
		</td>
	</tr>
<%	} %>
	<tr>
		<td>
		<table width="100%" class="commandBar">
			<tr>
				<td style="width: 10%;">&nbsp;</td>
				<td style="width: 90%;" align="left">
					<input type="button" value="Return" onclick="javascript:history.go(-2);">
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</div>
</body>
</html>

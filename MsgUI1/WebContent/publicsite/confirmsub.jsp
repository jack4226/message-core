<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="com.legacytojava.msgui.publicsite.messages" var="bndl"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="./styles.css" rel="stylesheet" type="text/css">
<title><fmt:message key="subscriptionConfirmationTitle" bundle="${bndl}"/></title>
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
				<fmt:message key="subscriptionConfirmationTitle" bundle="${bndl}"/>
				</span></td>
			</tr>
		</table>
		</td>
	</tr>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="com.legacytojava.message.dao.idtokens.MsgIdCipher"%>
<%@page import="com.legacytojava.message.vo.emailaddr.EmailAddrVo"%>
<%@page import="com.legacytojava.message.util.EmailAddrUtil"%>
<%
	Logger logger = Logger.getLogger("com.legacytojava.jsp");
	ServletContext ctx = application;
	
	String encodedSbsrId = request.getParameter("sbsrid");
	String listIds = request.getParameter("listids");
	String sbsrAddr = request.getParameter("sbsraddr");
	Long sbsrIdLong = null;
	List<String> subedList = new ArrayList<String>();
	int confirmCount = 0;
	EmailAddrVo addrVo = null;
	StringBuffer sbListNames = new StringBuffer();
	StringBuffer sbListIds = new StringBuffer();
	try {
		long sbsrId = MsgIdCipher.decode(encodedSbsrId);
		sbsrIdLong = Long.valueOf(sbsrId);
		addrVo = getEmailAddrDao(ctx).getByAddrId(sbsrId);
		if (listIds != null && listIds.length() > 0 && addrVo != null) {
			String decodedSbsdAddr = EmailAddrUtil.removeDisplayName(addrVo.getEmailAddr());
			StringTokenizer st = new StringTokenizer(listIds, ",");
			int count = 0;
			while (st.hasMoreTokens()) {
				String listId = st.nextToken();
				try {
					if (listId != null && decodedSbsdAddr.equalsIgnoreCase(sbsrAddr)) {
						confirmCount += getSubscriptionDao(ctx).optInConfirm(sbsrId, listId);
						if (count > 0) {
							sbListIds.append(",");
						}
						sbListIds.append(listId);
						MailingListVo vo = getMailingListDao(ctx).getByListId(listId);
						if (vo != null) {
							subedList.add(listId + " - " + vo.getDisplayName());
							if (count > 0) {
								sbListNames.append(" \n");
							}
							sbListNames.append(vo.getDisplayName());
						}
						else {
							logger.error("confirmsub.jsp - Failed to find mailing list by Id: " + listId);
						}
					}
				}
				catch (NumberFormatException e) {
					logger.error("confirmsub.jsp - " + e.toString());
				}
				count ++;
	 		}
			pageContext.setAttribute("subedList", subedList);
		}
	}
	catch (NumberFormatException e) {
		logger.error("confirmsub.jsp - " + e.toString());
	}
	
	if (confirmCount > 0 && addrVo != null) {
		Map<String, String> listMap = new HashMap<String, String>();
		listMap.put("_SubscribedMailingLists", sbListNames.toString());
		listMap.put("_EncodedSubcriberId", encodedSbsrId);
		listMap.put("_SubscribedListIds", sbListIds.toString());
		getMailingListBo(ctx).send(addrVo.getEmailAddr(), listMap, "SubscriptionWelcomeLetter");
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
					<b><fmt:message key="subscriptionConfirmationLabel" bundle="${bndl}"/></b>
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
				<th style="width: 100%;">List Name</th>
			</tr>
		<% for (int i = 0; i < subedList.size(); i++) { %>
			<tr>
				<td style="width: 100%;"><%= subedList.get(i) %></td>
			</tr>
		<%	} %>
		</table>
		</td>
	</tr>
	<tr>
		<td style="width: 80%;" colspan="2" align="center">
		<table style="width: 80%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td>
					<span class="gridHeader">&nbsp;
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
				If you subscribed already and want to edit your profile or un-subscribe, 
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
				<% if (listIds != null && listIds.length() > 0 && addrVo != null) { %>
				You have already confirmed the subscriptions.
				<% } else { %>
				The confirmation code is invalid or tampered.
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
					<input type="button" value="Return" onclick="javascript:history.go(-1);">
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</div>
</body>
</html>

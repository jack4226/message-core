<%@page import="ltj.message.dao.emailaddr.MailingListDao"%>
<%@page import="ltj.message.dao.emailaddr.EmailAddressDao"%>
<%@page import="ltj.message.dao.emailaddr.EmailSubscrptDao"%>
<%@page import="ltj.message.bo.mailinglist.MailingListBo"%>
<%@page import="ltj.msgui.util.SpringUtil"%>
<%@page import="ltj.message.vo.emailaddr.MailingListVo"%>
<%@page import="ltj.message.vo.emailaddr.EmailVariableVo"%>
<%@page import="ltj.message.dao.emailaddr.EmailVariableDao"%>
<%@page import="ltj.message.dao.inbox.MsgClickCountDao"%>
<%@page import="ltj.message.bo.customer.CustomerBo"%>
<%@page import="ltj.message.constant.Constants"%>
<%@page import="ltj.message.constant.VariableType"%>
<%@page import="ltj.message.bo.template.*"%>
<%@page import="org.apache.log4j.Logger" %>
<%@page import="java.util.*" %>
<%!
	MailingListDao mailingListDao = null;
	MailingListDao getMailingListDao(ServletContext ctx) {
		if (mailingListDao == null) {
			mailingListDao = (MailingListDao) SpringUtil.getWebAppContext(ctx).getBean("mailingListDao");
		}
		return mailingListDao;
	}

	MailingListBo mailingListBo = null;
	MailingListBo getMailingListBo(ServletContext ctx) {
		if (mailingListBo == null) {
			mailingListBo = (MailingListBo) SpringUtil.getWebAppContext(ctx).getBean("mailingListBo");
		}
		return mailingListBo;
	}

	EmailAddressDao emailAddressDao = null;
	EmailAddressDao getEmailAddressDao(ServletContext ctx) {
		if (emailAddressDao == null) {
			emailAddressDao = (EmailAddressDao) SpringUtil.getWebAppContext(ctx).getBean("emailAddressDao");
		}
		return emailAddressDao;
	}

	EmailSubscrptDao emailSubscrptDao = null;
	EmailSubscrptDao getEmailSubscrptDao(ServletContext ctx) {
		if (emailSubscrptDao == null) {
			emailSubscrptDao = (EmailSubscrptDao) SpringUtil.getWebAppContext(ctx).getBean("emailSubscrptDao");
		}
		return emailSubscrptDao;
	}

	EmailVariableDao emailVariableDao = null;
	EmailVariableDao getEmailVariableDao(ServletContext ctx) {
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableDao) SpringUtil.getWebAppContext(ctx).getBean("emailVariableDao");
		}
		return emailVariableDao;
	}

	MsgClickCountDao msgClickCountDao = null;
	MsgClickCountDao getMsgClickCountDao(ServletContext ctx) {
		if (msgClickCountDao == null) {
			msgClickCountDao = (MsgClickCountDao) SpringUtil.getWebAppContext(ctx).getBean("msgClickCountDao");
		}
		return msgClickCountDao;
	}

	private CustomerBo customerBo = null;
	CustomerBo getCustomerBo(ServletContext ctx) {
		if (customerBo == null) {
			customerBo = (CustomerBo) SpringUtil.getWebAppContext(ctx).getBean("customerBo");
		}
		return customerBo;
	}
	
	List<MailingListVo> getSbsrMailingLists(ServletContext ctx, long emailAddrId) {
		List<MailingListVo> subedList = getMailingListDao(ctx).getSubscribedLists(emailAddrId);
		HashMap<String, MailingListVo> map = new HashMap<String, MailingListVo>();
		for (Iterator<MailingListVo> it = subedList.iterator(); it.hasNext();) {
			MailingListVo vo = (MailingListVo) it.next();
			map.put(vo.getListId(), vo);
		}
		List<MailingListVo> allList = getMailingListDao(ctx).getAll(true);
		for (int i = 0; i < allList.size(); i++) {
			MailingListVo vo = (MailingListVo) allList.get(i);
			if (map.containsKey(vo.getListId())) {
				allList.set(i, map.get(vo.getListId()));
			}
		}
		return allList;
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName) {
		return renderURLVariable(ctx, emailVariableName, null);
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName, Long sbsrId) {
		return renderURLVariable(ctx, emailVariableName, sbsrId, null, null);
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName, Long sbsrId, String listId, Long msgId) {
		Logger logger = Logger.getLogger("com.legacytojava.jsp");
		String renderedValue = "";
		EmailVariableVo vo = getEmailVariableDao(ctx).getByName(emailVariableName);
		Map<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
		if (sbsrId != null) {
			RenderVariable var = new RenderVariable(
					"SubscriberAddressId",
					sbsrId.toString(),
					null,
					VariableType.TEXT,
					Constants.Y,
					false,
					null);
			vars.put(var.getVariableName(), var);
		}
		if (listId != null && listId.trim().length() > 0) {
			RenderVariable var = new RenderVariable(
					"MailingListId",
					listId,
					null,
					VariableType.TEXT,
					Constants.Y,
					false,
					null);
			vars.put(var.getVariableName(), var);
		}
		if (msgId != null) {
			RenderVariable var = new RenderVariable(
					"BroadcastMsgId",
					msgId.toString(),
					null,
					VariableType.TEXT,
					Constants.Y,
					false,
					null);
			vars.put(var.getVariableName(), var);
		}
		if (vo != null) {
			try {
				renderedValue = RenderUtil.renderTemplateText(vo.getDefaultValue(), null, vars);
			}
			catch (Exception e) {
				logger.error("loadSbsrDaos.jsp - renderURLVariable: ", e);
			}
		}
		return renderedValue;
	}
	
	String blankToNull(String str) {
		if (str == null || str.trim().length() ==0)
			return null;
		else
			return str;
	}

	String nullToBlank(String str) {
		if (str == null)
			return "";
		else
			return str;
	}
	%>
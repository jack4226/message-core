package ltj.msgui.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;
import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.validation.ValidationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.BodypartBean;
import ltj.message.bean.MessageBean;
import ltj.message.bo.mailsender.MessageBodyBuilder;
import ltj.message.bo.task.AssignRuleNameBoImpl;
import ltj.message.bo.task.TaskBaseBo;
import ltj.message.bo.template.RenderUtil;
import ltj.message.constant.CodeType;
import ltj.message.constant.MLDeliveryType;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.user.SessionUploadDao;
import ltj.message.exception.DataValidationException;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.HtmlTags;
import ltj.message.vo.SessionUploadVo;
import ltj.message.vo.UserVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.message.vo.emailaddr.EmailVariableVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.TemplateRenderVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="mailingListCompose")
@javax.faces.bean.ViewScoped
public class MailingListComposeBean implements java.io.Serializable {
	private static final long serialVersionUID = -2015576038292544848L;
	static final Logger logger = Logger.getLogger(MailingListComposeBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	
	private transient MailingListDao mailingListDao = null;
	private transient SessionUploadDao sessionUploadDao = null;
	private transient EmailTemplateDao emailTemplateDao = null;
	private transient EmailVariableDao emailVariableDao = null;
	private transient EmailAddressDao emailAddrDao = null;
	
	private String listId = null;
	private String msgSubject = null;
	private String msgBody = null;
	private boolean isHtml = true;
	private Boolean embedEmailId = null; // use system default
	private String renderedBody = null;
	private String renderedSubj = null;
	private String templateId =null;
	
	private transient UIInput templateIdInput = null;
	private String sendResult = null;
	private BeanMode beanMode = BeanMode.edit;

	private List<SessionUploadVo> uploads = null;
	
	private javax.servlet.http.Part file;
	
	private String deliveryOption = null;
	private String actionFailure = null;

	private static String TO_SELF = null;
	private static String TO_FAILED = TO_SELF;
	private static String TO_SENT = "main.xhtml";
	private static String TO_PREVIEW = "mailingListPreview.xhtml";
	
	public MailingListComposeBean() {
		//
	}
	
	public SessionUploadDao getSessionUploadDao() {
		if (sessionUploadDao == null) {
			sessionUploadDao = SpringUtil.getWebAppContext().getBean(SessionUploadDao.class);
		}
		return sessionUploadDao;
	}
	
	public MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = SpringUtil.getWebAppContext().getBean(MailingListDao.class);
		}
		return mailingListDao;
	}
	

	public EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = SpringUtil.getWebAppContext().getBean(EmailTemplateDao.class);
		}
		return emailTemplateDao;
	}

	public EmailVariableDao getEmailVariableDao() {
		if (emailVariableDao == null) {
			emailVariableDao = SpringUtil.getWebAppContext().getBean(EmailVariableDao.class);
		}
		return emailVariableDao;
	}

	public EmailAddressDao getEmailAddressDao() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddrDao;
	}

	private void reset() {
		actionFailure = null;
	}
	
	private void clearUploads() {
		String sessionId = FacesUtil.getSessionId();
		if (uploads != null) {
			uploads.clear();
		}
		int rowsDeleted = getSessionUploadDao().deleteBySessionId(sessionId);
		logger.info("clearUploads() - SessionId: " + sessionId + ", rows deleted: " + rowsDeleted);
		uploads = null;
	}
	
	public String attachFiles() {
		String pageUrl = "/upload/msgInboxAttachFiles.jsp?frompage=mailinglist";
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext ectx = context.getExternalContext();
		try {
			ectx.redirect(ectx.encodeResourceURL(ectx.getRequestContextPath() + pageUrl));
		}
		catch (IOException e) {
			logger.error("attachFiles() - IOException caught", e);
			throw new FacesException("Cannot redirect to " + pageUrl + " due to IO exception.", e);
		}
		return null;
	}
	
	public void validateFile(FacesContext ctx, UIComponent comp, Object value) {
		List<FacesMessage> msgs = new ArrayList<FacesMessage>();
		
		try {
			javax.servlet.http.Part file = (javax.servlet.http.Part) value;
			if (file.getSize() > (256 * 1024)) { // limit to 256KB
				FacesMessage msg = ltj.msgui.util.MessageUtil.getMessage("jpa.msgui.messages", "uploadFileTooBig",
						new String[] { "256kb" });
				msgs.add(msg);
			}
		}
		catch (Exception e) {
			throw new ValidationException(e);
		}
		if (!msgs.isEmpty()) {
			throw new ValidatorException(msgs);
		}
	}
	
	public void uploadFileListener(AjaxBehaviorEvent event) {
		uploadFile();
	}
	
	public String uploadFile() {
        logger.info("uploadFile() Enetring...");
        
        String fileName = file.getName();
        for (String hdrName : file.getHeaderNames()) {
        	logger.info("Header mame / value: " + hdrName + " / " + file.getHeader(hdrName));
        	if (StringUtils.contains(hdrName, "content-disposition")) {
        		String parsedName = parseFileName(file.getHeader(hdrName));
        		if (StringUtils.isNotBlank(parsedName)) {
        			fileName = parsedName;
        		}
        	}
        }
        
        String contentType = file.getContentType();
        logger.info("content-type: " + contentType);
        logger.info("filename: " + fileName);
        logger.info("size: " + file.getSize());
        
        SessionUploadVo sessVo = new SessionUploadVo();
    	sessVo.setSessionSeq(0); // ignored by insertLast() method
    	
        String sessionId = FacesUtil.getSessionId();   	
    	sessVo.setSessionId(sessionId);
        
        UserVo userVo = (UserVo) FacesUtil.getLoginUserVo();
        if (userVo != null) {
        	sessVo.setUserId(userVo.getUserId());
        }
        else {
        	logger.warn("process() - UserData not found in httpSession!");
        }
    	sessVo.setFileName(fileName);
    	sessVo.setContentType(contentType);
    	
    	try {
	    	InputStream is = file.getInputStream();
	    	sessVo.setSessionValue(IOUtils.toByteArray(is));
	        // Write uploaded file to database
	    	getSessionUploadDao().insertLast(sessVo);
	    	logger.info("process() - rows inserted: " + 1);
			if (uploads == null) {
				uploads = new ArrayList<SessionUploadVo>();
			}
			uploads.add(sessVo);
		}
		catch (IOException ex) {
           logger.error("IOException caught", ex);
        }
    	
		FacesMessage message = ltj.msgui.util.MessageUtil.getMessage("jpa.msgui.messages", "uploadFileResult",
				new String[] { fileName });
		message.setSeverity(FacesMessage.SEVERITY_WARN);
        return TO_SELF;
    }
	
	private String parseFileName(String headerValue) {
		Pattern p = Pattern.compile("filename=[\"']?([\\w\\s\\.,-]{1,100})[\"']?",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(headerValue);
		if (m.find() && m.groupCount() >= 1) {
			for (int i = 0; i <= m.groupCount(); i++) {
				//System.out.println("Group[" + i + "]: " + m.group(i));
			}
			return m.group(1);
		}
		return null;
	}
	
	public List<SessionUploadVo> retrieveUploadFiles() {
		String sessionId = FacesUtil.getSessionId();
		boolean valid = FacesUtil.isSessionIdValid();
		logger.info("retrieveUploadFiles() - SessionId: " + sessionId + ", Valid? " + valid);
		uploads = getSessionUploadDao().getBySessionId(sessionId);
		if (isDebugEnabled && uploads != null)
			logger.debug("retrieveUploadFiles() - files retrieved: " + uploads.size());
		return uploads;
	}
	
	public void removeUploadFileListener(AjaxBehaviorEvent event) {
		removeUploadFile();
	}

	public String removeUploadFile() {
		String seq = FacesUtil.getRequestParameter("seq");
		String name = FacesUtil.getRequestParameter("name");
		String id = FacesUtil.getSessionId();
		logger.info("removeUploadFile() - id/seq/name: " + id + "/" + seq + "/" + name);
		try {
			int sessionSeq = Integer.parseInt(seq);
			for (int i = 0; uploads != null && i < uploads.size(); i++) {
				SessionUploadVo vo = uploads.get(i);
				if (sessionSeq == vo.getSessionSeq()) {
					uploads.remove(i);
					break;
				}
			}
			int rowsDeleted = getSessionUploadDao().deleteByPrimaryKey(id, sessionSeq);
			logger.info("removeUploadFile() - rows deleted: " + rowsDeleted + ", file name: " + name);
		}
		catch (RuntimeException e) {
			logger.error("RuntimeException caught", e);
		}
		return null;
	}

	public void copyFromTemplateListener(AjaxBehaviorEvent event) {
		copyFromTemplate();
	}
	
	public String copyFromTemplate() {
		String id = (String) templateIdInput.getSubmittedValue();
		logger.info("copyFromTemplate() - templateId = " + id);
		EmailTemplateVo vo = getEmailTemplateDao().getByTemplateId(id);
		if (vo != null) {
			listId = vo.getListId();
			msgSubject = vo.getSubject();
			msgBody = vo.getBodyText();
			isHtml = vo.getIsHtml();
			isHtml = isHtml == false ? HtmlTags.isHTML(msgBody) : isHtml;
			embedEmailId = vo.getEmbedEmailId();
			deliveryOption = vo.getDeliveryOption();
		}
		else {
			logger.error("copyFromTemplate() - template not found by templateId: " + templateId);
		}
		return TO_SELF;
	}
	
	private void checkVariableLoop(String text) throws DataValidationException {
		List<String> varNames = RenderUtil.retrieveVariableNames(text);
		for (String loopName : varNames) {
			EmailVariableVo vo = getEmailVariableDao().getByName(loopName);
			if (vo != null) {
				RenderUtil.checkVariableLoop(vo.getDefaultValue(), loopName);
			}
		}
	}
	
	public void sendMessageListener(AjaxBehaviorEvent event) {
		sendMessage();
	}

	public String sendMessage() {
		logger.info("sendMessage() - Mailing List Selected: " + listId);
		reset();
		// validate variable loops
		try {
			checkVariableLoop(msgBody);
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught: " + e.getMessage());
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		try {
			checkVariableLoop(msgSubject);
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught: " + e.getMessage());
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		// make sure we have all the data to build a message bean
		try {
			MailingListVo listVo =  getMailingListDao().getByListId(listId);
			if (listVo == null) {
				String errmsg = "failed to get mailing list by ListId (" + listId + ")!";
				logger.error("sendMessage() - " + errmsg);
				throw new IllegalStateException(errmsg);
			}
			// retrieve new addresses
			Address[] from = InternetAddress.parse(listVo.getEmailAddr());
			Address[] to = InternetAddress.parse(listVo.getEmailAddr());
			// retrieve new message body
			msgBody = msgBody == null ? "" : msgBody; // just for safety
			// construct messageBean for new message
			MessageBean mBean = new MessageBean();
			mBean.setMailingListId(listId);
			mBean.setClientId(listVo.getClientId());
			mBean.setRuleName(RuleNameEnum.BROADCAST.getValue());
			if (CodeType.Y.value().equals(embedEmailId)) {
				mBean.setEmBedEmailId(Boolean.valueOf(true));
			}
			else if (CodeType.N.value().equals(embedEmailId)) {
				mBean.setEmBedEmailId(Boolean.valueOf(false));
			}
			if (MLDeliveryType.CUSTOMERS_ONLY.value().equals(deliveryOption)) {
				mBean.setToCustomersOnly(true);
			}
			else if (MLDeliveryType.PROSPECTS_ONLY.value().equals(deliveryOption)) {
				mBean.setToProspectsOnly(true);
			}
			String contentType = "text/plain";
			isHtml = isHtml == false ? HtmlTags.isHTML(msgBody) : isHtml;
			if (isHtml) {
				contentType = "text/html";
			}
			// retrieve upload files
			String sessionId = FacesUtil.getSessionId();
			List<SessionUploadVo> list = getSessionUploadDao().getBySessionId(sessionId);
			if (list != null && list.size() > 0) {
				// construct multipart
				mBean.setContentType("multipart/mixed");
				// message body part
				BodypartBean aNode = new BodypartBean();
				aNode.setContentType(contentType);
				aNode.setValue(msgBody);
				aNode.setSize(msgBody.length());
				mBean.put(aNode);
				// message attachments
				for (int i = 0; i < list.size(); i++) {
					SessionUploadVo vo = list.get(i);
					BodypartBean subNode = new BodypartBean();
					subNode.setContentType(vo.getContentType());
					subNode.setDisposition(Part.ATTACHMENT);
					subNode.setDescription(vo.getFileName());
					byte[] bytes = vo.getSessionValue();
					subNode.setValue(bytes);
					if (bytes != null) {
						subNode.setSize(bytes.length);
					}
					else {
						subNode.setSize(0);
					}
					mBean.put(subNode);
					mBean.updateAttachCount(1);
					mBean.getComponentsSize().add(Integer.valueOf(subNode.getSize()));
				}
				// remove uploaded files from session table
				clearUploads();
			}
			else {
				mBean.setContentType(contentType);
				mBean.setBody(msgBody);
			}
			// set addresses and subject
			mBean.setFrom(from);
			mBean.setTo(to);
			mBean.setSubject(msgSubject);
			// process the message
			TaskBaseBo taskBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean(AssignRuleNameBoImpl.class);
			taskBo.setTaskArguments(mBean.getRuleName());
			taskBo.process(mBean);
			Long mailsSent = mBean.getMsgId();
			if (mailsSent != null) {
				logger.info("sendMessage() - Broadcast Message queued: " + mailsSent);
				if (isDebugEnabled)
					logger.debug("sendMessage() - Broadcast message: " + LF + mBean);
			}
			sendResult = "mailingListSendSccess";
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		return TO_SENT;
	}
	
	public void previewMsgBodyListener(AjaxBehaviorEvent event) {
		previewMsgBody();
	}
	
	public String previewMsgBody() {
		try {
			// build variable values using the first email address found in EmailAddr table.
			long previewAddrId = getEmailAddressDao().getEmailAddrIdForPreview();
			// include mailing list variables
			EmailAddressVo addrVo = getEmailAddressDao().getByAddrId(previewAddrId);
			String previewAddr = "1";
			if (addrVo != null) {
				previewAddr = addrVo.getEmailAddr();
			}
			TemplateRenderVo renderVo = RenderUtil.renderEmailText(previewAddr, null, msgSubject,
					msgBody, listId);
			renderedBody = getDisplayBody(renderVo.getBody());
			renderedSubj = renderVo.getSubject();
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		} 
		beanMode = BeanMode.preview;
		return TO_PREVIEW;
	}
	
	private String getDisplayBody(String bodytext) {
		if (bodytext == null) return null;
		if (isHtml) {
			return MessageBodyBuilder.removeHtmlBodyTags(bodytext);
		}
		else {
			return EmailAddrUtil.getHtmlDisplayText(bodytext);
		}
	}

	public void cancelPreviewListener(AjaxBehaviorEvent event) {
		logger.info("cancelPreviewListener() - Entering...");
		beanMode = BeanMode.edit;
	}
	
	/**
	 * Validate FROM email address
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateFromAddress(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateFromAddress() - From Address: " + value);
		String fromAddr = (String) value;
		if (!isValidEmailAddress(fromAddr)) {
			// invalid email address
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidEmailAddress", new String[] {fromAddr});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	private boolean isValidEmailAddress(String addrs) {
		List<String> list = getAddressList(addrs);
		for (int i = 0; i < list.size(); i++) {
			if (!EmailAddrUtil.isRemoteOrLocalEmailAddress(list.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	private List<String> getAddressList(String addrs) {
		List<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(addrs, ",");
		while (st.hasMoreTokens()) {
			String addr = st.nextToken();
			list.add(EmailAddrUtil.removeDisplayName(addr, true));
		}
		return list;
	}
	
	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public List<SessionUploadVo> getUploads() {
		//if (uploads == null)
			retrieveUploadFiles();
		return uploads;
	}

	public void setUploads(List<SessionUploadVo> uploads) {
		this.uploads = uploads;
	}
	
	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public boolean isHtml() {
		return isHtml;
	}

	public void setHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

	public Boolean getEmbedEmailId() {
		return embedEmailId;
	}

	public void setEmbedEmailId(Boolean embedEmailId) {
		this.embedEmailId = embedEmailId;
	}

	public String getRenderedBody() {
		return renderedBody;
	}

	public void setRenderedBody(String renderedBody) {
		this.renderedBody = renderedBody;
	}

	public String getRenderedSubj() {
		return renderedSubj;
	}

	public void setRenderedSubj(String renderedSubj) {
		this.renderedSubj = renderedSubj;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public UIInput getTemplateIdInput() {
		return templateIdInput;
	}

	public void setTemplateIdInput(UIInput templateIdInput) {
		this.templateIdInput = templateIdInput;
	}

	public String getDeliveryOption() {
		return deliveryOption;
	}

	public void setDeliveryOption(String deliveryOption) {
		this.deliveryOption = deliveryOption;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}
	
	public String getSendResult() {
		return sendResult;
	}
	
	public void setSendResult(String sendResult) {
		this.sendResult = sendResult;
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

	public javax.servlet.http.Part getFile() {
		return file;
	}

	public void setFile(javax.servlet.http.Part file) {
		this.file = file;
	}

}
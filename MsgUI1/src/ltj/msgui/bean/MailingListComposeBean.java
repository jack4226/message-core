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

import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.EmailVariableDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.user.SessionUploadDao;
import ltj.message.vo.SessionUploadVo;
import ltj.message.vo.UserVo;
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
	private transient EmailTemplateBo emailTemplateBo = null;
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

	public EmailTemplateBo getEmailTemplateBo() {
		if (emailTemplateBo == null) {
			emailTemplateBo = SpringUtil.getWebAppContext().getBean(EmailTemplateBo.class);
		}
		return emailTemplateBo;
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
				FacesMessage msg = jpa.msgui.util.MessageUtil.getMessage("jpa.msgui.messages", "uploadFileTooBig",
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
    	
		FacesMessage message = jpa.msgui.util.MessageUtil.getMessage("jpa.msgui.messages", "uploadFileResult",
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
				SessionUpload vo = uploads.get(i);
				if (sessionSeq == vo.getSessionUploadPK().getSessionSequence()) {
					uploads.remove(i);
					break;
				}
			}
			SessionUploadPK pk = new SessionUploadPK(id, sessionSeq);
			int rowsDeleted = getSessionUploadDao().deleteByPrimaryKey(pk);
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
		EmailTemplate vo = getEmailTemplateDao().getByTemplateId(id);
		if (vo != null) {
			listId = vo.getMailingList().getListId();
			msgSubject = vo.getSubject();
			msgBody = vo.getBodyText();
			isHtml = vo.isHtml();
			isHtml = isHtml == false ? HtmlUtil.isHTML(msgBody) : isHtml;
			embedEmailId = vo.getIsEmbedEmailId();
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
			EmailVariable vo = getEmailVariableDao().getByVariableName(loopName);
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
			MailingList listVo =  getMailingListDao().getByListId(listId);
			if (listVo == null) {
				String errmsg = "failed to get mailing list by ListId (" + listId + ")!";
				logger.error("sendMessage() - " + errmsg);
				throw new IllegalStateException(errmsg);
			}
			// retrieve new addresses
			Address[] from = InternetAddress.parse(listVo.getListEmailAddr());
			Address[] to = InternetAddress.parse(listVo.getListEmailAddr());
			// retrieve new message body
			msgBody = msgBody == null ? "" : msgBody; // just for safety
			// construct messageBean for new message
			MessageBean mBean = new MessageBean();
			mBean.setMailingListId(listId);
			mBean.setSenderId(listVo.getSenderData().getSenderId());
			mBean.setRuleName(RuleNameEnum.BROADCAST.getValue());
			if (CodeType.YES_CODE.getValue().equals(embedEmailId)) {
				mBean.setEmBedEmailId(Boolean.valueOf(true));
			}
			else if (CodeType.NO_CODE.getValue().equals(embedEmailId)) {
				mBean.setEmBedEmailId(Boolean.valueOf(false));
			}
			if (MailingListDeliveryType.SUBSCRIBERS_ONLY.getValue().equals(deliveryOption)) {
				mBean.setToSubscribersOnly(true);
			}
			else if (MailingListDeliveryType.PROSPECTS_ONLY.getValue().equals(deliveryOption)) {
				mBean.setToProspectsOnly(true);
			}
			String contentType = "text/plain";
			isHtml = isHtml == false ? HtmlUtil.isHTML(msgBody) : isHtml;
			if (isHtml) {
				contentType = "text/html";
			}
			// retrieve upload files
			String sessionId = FacesUtil.getSessionId();
			List<SessionUpload> list = getSessionUploadDao().getBySessionId(sessionId);
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
					SessionUpload vo = list.get(i);
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
			TaskBaseBo taskBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean(AssignRuleName.class);
			MessageContext ctx = new MessageContext(mBean);
			ctx.setTaskArguments(mBean.getRuleName());
			taskBo.process(ctx);
			List<Integer> mailsSent = ctx.getRowIds();
			if (mailsSent != null && !mailsSent.isEmpty()) {
				logger.info("sendMessage() - Broadcast Message queued: " + mailsSent.size());
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
			int previewAddrId = getEmailAddressDao().getRowIdForPreview();
			// include mailing list variables
			EmailAddress addrVo = getEmailAddressDao().getByRowId(previewAddrId);
			String previewAddr = "1";
			if (addrVo != null) {
				previewAddr = addrVo.getAddress();
			}
			TemplateRenderVo renderVo = getEmailTemplateBo().renderEmailText(previewAddr, null, msgSubject,
					msgBody, listId);
			renderedBody = getDisplayBody(renderVo.getBody());
			renderedSubj = renderVo.getSubject();
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		} 
		catch (TemplateException e) {
			logger.error("TemplateException caught", e);
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
			return StringUtil.getHtmlDisplayText(bodytext);
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
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
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

	public List<SessionUpload> getUploads() {
		//if (uploads == null)
			retrieveUploadFiles();
		return uploads;
	}

	public void setUploads(List<SessionUpload> uploads) {
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
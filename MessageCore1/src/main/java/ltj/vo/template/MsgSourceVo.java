package ltj.vo.template;

import java.io.Serializable;
import java.sql.Timestamp;

import ltj.message.constant.CarrierCode;
import ltj.message.constant.Constants;
import ltj.message.vo.BaseVoWithRowId;

public class MsgSourceVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -8801080860617417345L;
	private String msgSourceId = "";
	private String description = null;
	private Long fromAddrId = null;
	private Long replyToAddrId = null;
	private String subjTemplateId = "";
	private String bodyTemplateId = "";
	private String templateVariableId = null;
	private boolean excludeIdToken = false;
	// Y - No email id will be embedded into message
	private String carrierCode = CarrierCode.SMTPMAIL.value();
	// Internet, WebMail, Internal Routing, ...
	private String allowOverride = Constants.Y;
	// allow override templates, addresses to be supplied at runtime
	private boolean saveMsgStream = true;
	// Y - save rendered SMTP message stream to MSGOBSTREAM
	private boolean archiveInd = false;
	// Y - archive the rendered messages
	private Integer purgeAfter = null; // in month
	
	public MsgSourceVo() {
		updtTime = new Timestamp(System.currentTimeMillis());
	}
	
	public String getAllowOverride() {
		return allowOverride;
	}
	public void setAllowOverride(String allowOverride) {
		this.allowOverride = allowOverride;
	}
	public boolean getArchiveInd() {
		return archiveInd;
	}
	public void setArchiveInd(boolean archiveInd) {
		this.archiveInd = archiveInd;
	}
	public String getBodyTemplateId() {
		return bodyTemplateId;
	}
	public void setBodyTemplateId(String bodyTemplateId) {
		this.bodyTemplateId = bodyTemplateId;
	}
	public String getCarrierCode() {
		return carrierCode;
	}
	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean getExcludeIdToken() {
		return excludeIdToken;
	}
	public void setExcludeIdToken(boolean excludingIdToken) {
		this.excludeIdToken = excludingIdToken;
	}
	public Long getFromAddrId() {
		return fromAddrId;
	}
	public void setFromAddrId(Long fromAddrId) {
		this.fromAddrId = fromAddrId;
	}
	public String getMsgSourceId() {
		return msgSourceId;
	}
	public void setMsgSourceId(String msgSourceId) {
		this.msgSourceId = msgSourceId;
	}
	public Integer getPurgeAfter() {
		return purgeAfter;
	}
	public void setPurgeAfter(Integer purgeAfter) {
		this.purgeAfter = purgeAfter;
	}
	public Long getReplyToAddrId() {
		return replyToAddrId;
	}
	public void setReplyToAddrId(Long replyToAddrId) {
		this.replyToAddrId = replyToAddrId;
	}
	public boolean getSaveMsgStream() {
		return saveMsgStream;
	}
	public void setSaveMsgStream(boolean saveMsgStream) {
		this.saveMsgStream = saveMsgStream;
	}
	public String getSubjTemplateId() {
		return subjTemplateId;
	}
	public void setSubjTemplateId(String subjTemplateId) {
		this.subjTemplateId = subjTemplateId;
	}
	public String getTemplateVariableId() {
		return templateVariableId;
	}
	public void setTemplateVariableId(String templateVariableId) {
		this.templateVariableId = templateVariableId;
	}
	public String getUpdtUserId() {
		return updtUserId;
	}
}
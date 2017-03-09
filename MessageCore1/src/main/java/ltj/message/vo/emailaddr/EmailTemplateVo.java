package ltj.message.vo.emailaddr;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import ltj.message.constant.Constants;
import ltj.message.constant.MLDeliveryType;
import ltj.message.constant.MailingListType;
import ltj.message.dao.emailaddr.SchedulesBlob;
import ltj.message.vo.BaseVoWithRowId;

public class EmailTemplateVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -5007781927317135437L;
	private String templateId = "";
	private String listId = "";
	private String subject = null;
	private String bodyText = null;
	private boolean isHtml = true;
	private String listType = MailingListType.TRADITIONAL.value();
	private String deliveryOption = MLDeliveryType.ALL_ON_LIST.value();
	private String selectCriteria = null;
	private String embedEmailId = " "; // use system default
	private String isBuiltIn = Constants.N;
	private SchedulesBlob schedulesBlob = null;
	private String origTemplateId = null;
	private String clientId = "";
	
	/** define components for UI */
	public String getDeliveryOptionDesc() {
		if (MLDeliveryType.ALL_ON_LIST.value().equals(deliveryOption)) {
			return "All on list";
		}
		else if (MLDeliveryType.CUSTOMERS_ONLY.value().equals(deliveryOption)) {
			return "Customers only";
		}
		else if (MLDeliveryType.PROSPECTS_ONLY.value().equals(deliveryOption)) {
			return "Prospects only";
		}
		return "";
	}
	
	public String getSubjectShort() {
		return StringUtils.left(subject, 50);
	}
	
	public boolean isPersonalized() {
		return MailingListType.PERSONALIZED.value().equalsIgnoreCase(listType);
	}
	
	public boolean getIsBuiltInTemplate() {
		return Constants.Y.equals(isBuiltIn);
	}
	/** end of UI */

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public boolean getIsHtml() {
		return isHtml;
	}

	public void setIsHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public String getDeliveryOption() {
		return deliveryOption;
	}

	public void setDeliveryOption(String deliveryOption) {
		this.deliveryOption = deliveryOption;
	}

	public String getEmbedEmailId() {
		return embedEmailId;
	}

	public void setEmbedEmailId(String embedEmailId) {
		this.embedEmailId = embedEmailId;
	}

	public String getIsBuiltIn() {
		return isBuiltIn;
	}

	public void setIsBuiltIn(String isBuiltIn) {
		this.isBuiltIn = isBuiltIn;
	}
	
	public String getSelectCriteria() {
		return selectCriteria;
	}

	public void setSelectCriteria(String selectCriteria) {
		this.selectCriteria = selectCriteria;
	}

	public SchedulesBlob getSchedulesBlob() {
		if (schedulesBlob == null) {
			schedulesBlob = new SchedulesBlob();
		}
		return schedulesBlob;
	}

	public void setSchedulesBlob(SchedulesBlob schedulesBlob) {
		this.schedulesBlob = schedulesBlob;
	}

	public String getOrigTemplateId() {
		return origTemplateId;
	}

	public void setOrigTemplateId(String origTemplateId) {
		this.origTemplateId = origTemplateId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
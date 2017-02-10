package ltj.data.preload;

import org.apache.commons.lang3.StringUtils;

import ltj.message.bo.ActivateBoImpl;
import ltj.message.bo.AssignRuleNameBoImpl;
import ltj.message.bo.AutoReplyBoImpl;
import ltj.message.bo.BounceBoImpl;
import ltj.message.bo.BroadcastBoImpl;
import ltj.message.bo.CloseBoImpl;
import ltj.message.bo.CsrReplyBoImpl;
import ltj.message.bo.DeliveryErrorBoImpl;
import ltj.message.bo.DropBoImpl;
import ltj.message.bo.ForwardBoImpl;
import ltj.message.bo.OpenBoImpl;
import ltj.message.bo.SaveBoImpl;
import ltj.message.bo.SendMailBoImpl;
import ltj.message.bo.SubscribeBoImpl;
import ltj.message.bo.SuspendBoImpl;
import ltj.message.bo.ToCsrBoImpl;
import ltj.message.bo.UnsubscribeBoImpl;

/*
 * define rule actions
 */
public enum RuleActionDetailEnum {
	ACTIVATE("activete email address", serviceName(ActivateBoImpl.class), null, RuleDataTypeEnum.EMAIL_ADDRESS),
	BOUNCE_UP("increase bounce count", serviceName(BounceBoImpl.class), null, RuleDataTypeEnum.EMAIL_ADDRESS),
	CLOSE("close the message", serviceName(CloseBoImpl.class), null, null),
	CSR_REPLY("send off the reply from csr", serviceName(CsrReplyBoImpl.class), null, null),
	AUTO_REPLY("reply to the message automatically", serviceName(AutoReplyBoImpl.class), null, RuleDataTypeEnum.TEMPLATE_ID),
	MARK_DLVR_ERR("mark delivery error", serviceName(DeliveryErrorBoImpl.class), null, null),
	DROP("drop the message", serviceName(DropBoImpl.class), DropBoImpl.class.getName(), null),
	FORWARD("forward the message", serviceName(ForwardBoImpl.class), null, RuleDataTypeEnum.EMAIL_ADDRESS),
	TO_CSR("redirect to message queue", serviceName(ToCsrBoImpl.class), null, RuleDataTypeEnum.EMAIL_ADDRESS),
	SAVE("save the message", serviceName(SaveBoImpl.class), null, null),
	SENDMAIL("simply send the mail off", serviceName(SendMailBoImpl.class), null, RuleDataTypeEnum.EMAIL_ADDRESS),
	SUSPEND("suspend email address", serviceName(SuspendBoImpl.class), null, RuleDataTypeEnum.EMAIL_ADDRESS),
	UNSUBSCRIBE("remove from the mailing list", serviceName(UnsubscribeBoImpl.class), null, RuleDataTypeEnum.EMAIL_ADDRESS),
	SUBSCRIBE("subscribe to the mailing list", serviceName(SubscribeBoImpl.class), null, RuleDataTypeEnum.EMAIL_ADDRESS),
	ASSIGN_RULENAME("set a rule mame and re-process", serviceName(AssignRuleNameBoImpl.class), null, RuleDataTypeEnum.RULE_NAME),
	OPEN("open the message", serviceName(OpenBoImpl.class), null, null),
	BROADCAST("broadcast to mailing list", serviceName(BroadcastBoImpl.class), null, RuleDataTypeEnum.MAILING_LIST);

	private String description;
	private String serviceName;
	private String className;
	private RuleDataTypeEnum dataType;

	private RuleActionDetailEnum(String description, String serviceName,
			String className, RuleDataTypeEnum dataType) {
		this.description = description;
		this.serviceName = serviceName;
		this.className = className;
		this.dataType = dataType;
	}
	
	static String serviceName(Class<?> clazz) {
		String name = StringUtils.uncapitalize(clazz.getSimpleName());
		return StringUtils.removeEnd(name, "Impl");
	}

	public String getDescription() {
		return description;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getClassName() {
		return className;
	}

	public RuleDataTypeEnum getDataType() {
		return dataType;
	}

	public static void main(String[] args) {
		System.out.println(ACTIVATE.getServiceName());
	}
}

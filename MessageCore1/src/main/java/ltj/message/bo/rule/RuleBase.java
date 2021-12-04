package ltj.message.bo.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ltj.message.constant.AddressType;
import ltj.message.constant.RuleCategory;
import ltj.message.constant.RuleCriteria;
import ltj.message.constant.RuleDataName;
import ltj.message.constant.RuleType;
import ltj.message.constant.VariableName;

public abstract class RuleBase implements java.io.Serializable {
	private static final long serialVersionUID = -2619176738651938695L;
	protected static final Logger logger = LogManager.getLogger(RuleBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();

	/** define rule type constants */
	public static final String SIMPLE_RULE = RuleType.SIMPLE.value(); //"Simple";
	public static final String ALL_RULE = RuleType.ALL.value(); //"All";
	public static final String ANY_RULE = RuleType.ANY.value(); //"Any";
	public static final String NONE_RULE = RuleType.NONE.value(); //"None";
	
	public static final String PRE_RULE = RuleCategory.PRE_RULE.value(); //"E";
	public static final String MAIN_RULE = RuleCategory.MAIN_RULE.value(); //"M";
	public static final String POST_RULE = RuleCategory.POST_RULE.value(); //"P";

	/** define criteria constants for simple rule */
	public static final String STARTS_WITH = RuleCriteria.STARTS_WITH.value(); //"starts_with";
	public static final String ENDS_WITH = RuleCriteria.ENDS_WITH.value(); //"ends_with";
	public static final String CONTAINS = RuleCriteria.CONTAINS.value(); //"contains";
	public static final String EQUALS = RuleCriteria.EQUALS.value(); //"equals";
	public static final String GREATER_THAN = RuleCriteria.GREATER_THAN.value(); //"greater_than";
	public static final String LESS_THAN = RuleCriteria.LESS_THAN.value(); //"less_than";
	public static final String IS_NOT_BLANK = RuleCriteria.IS_NOT_BLANK.value(); //"valued";
	public static final String IS_BLANK = RuleCriteria.IS_BLANK.value(); //"not_valued";
	public static final String REG_EX = RuleCriteria.REG_EX.value(); //"reg_ex";

	/** define data type constants for Internet mail */
	public static final String FROM_ADDR = AddressType.FROM_ADDR.value();
	public static final String TO_ADDR = AddressType.TO_ADDR.value();
	public static final String REPLYTO_ADDR = AddressType.REPLYTO_ADDR.value();
	public static final String CC_ADDR = AddressType.CC_ADDR.value();
	public static final String BCC_ADDR = AddressType.BCC_ADDR.value();
	public static final String SUBJECT = VariableName.SUBJECT.value();
	public static final String BODY = VariableName.BODY.value();
	public static final String MSG_REF_ID = VariableName.MSG_REF_ID.value();
	public static final String RULE_NAME = VariableName.RULE_NAME.value();
	public static final String X_HEADER = VariableName.DATA_NAME.value();
	public static final String RETURN_PATH = RuleDataName.RETURN_PATH.getValue(); //"ReturnPath";
	// mailbox properties
	public static final String MAILBOX_USER = VariableName.MAILBOX_USER.value();
	public static final String MAILBOX_HOST = VariableName.MAILBOX_HOST.value();
	// the next two items are not implemented yet
	public static final String RFC822 = VariableName.RFC822.value();
	public static final String DELIVERY_STATUS = VariableName.DELIVERY_STATUS.value();

	/** define data type constants for Internet email attachments */
	public static final String MIME_TYPE = RuleDataName.MIME_TYPE.getValue(); //"MimeType";
	public static final String FILE_NAME = RuleDataName.FILE_NAME.getValue(); //"FileName";

	final static String LF = System.getProperty("line.separator", "\n");

	// store rule names found in rules.xml
	private final static Set<String> ruleNameList = Collections.synchronizedSet(new LinkedHashSet<String>());

	protected final String ruleName, ruleType;
	protected String dataName;
	protected String headerName;
	protected final String mailType, criteria;
	protected final boolean caseSensitive;

	protected final List<String> subRuleList = new ArrayList<String>();

	public RuleBase(String _ruleName, 
			String _ruleType, 
			String _mailType, 
			String _dataName,
			String _headerName,
			String _criteria, 
			boolean _caseSensitive) {
		this.ruleName = _ruleName;
		this.ruleType = _ruleType;
		this.mailType = _mailType;
		this.dataName = _dataName;
		this.headerName = _headerName;
		this.criteria = _criteria;
		this.caseSensitive = _caseSensitive;
		if (this.ruleName != null && !ruleNameList.contains(this.ruleName)) {
			ruleNameList.add(this.ruleName);
		}
	}
	
	public String getRuleName() {
		return ruleName;
	}

	protected String getRuleName(int len) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<len-ruleName.length(); i++) {
			sb.append(" ");
		}
		return ruleName + sb.toString();
	}
	
	public String getRuleType() {
		return mailType;
	}

	public String getDataName() {
		return dataName;
	}

	public String getHeaderName() {
		return headerName;
	}

	public List<String> getSubRules() {
		return subRuleList;
	}

	public String getRuleContent() {
		StringBuffer sb = new StringBuffer();
		sb.append(LF + "---- listing rule content for " + ruleName + " ----" + LF);
		sb.append("Rule Name: " + ruleName + LF);
		sb.append("Rule Type: " + ruleType + LF);
		sb.append("Mail Type: " + mailType + LF);
		sb.append("Data Type: " + dataName + LF);
		if (headerName != null) {
			sb.append("Header Name: " + headerName + LF);
		}
		sb.append("Criteria : " + criteria + LF);
		sb.append("Case Sensitive : " + caseSensitive + LF);
		if (subRuleList != null) {
			sb.append("SubRule List:" + LF);
			for (int i = 0; i < subRuleList.size(); i++) {
				sb.append("     " + subRuleList.get(i) + LF);
			}
		}

		return sb.toString();
	}
	
	public abstract String match(String mail_type, String data_type, String data);
	
	public abstract String match(String mail_type, Object mail_obj);
}
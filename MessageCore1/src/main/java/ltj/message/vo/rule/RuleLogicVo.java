package ltj.message.vo.rule;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

import ltj.message.bo.rule.RuleBase;
import ltj.message.constant.Constants;
import ltj.message.vo.BaseVoWithRowId;

public class RuleLogicVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -3318722130635767052L;
	private String ruleName = "";
	private int ruleSeq = -1;
	private String ruleType = "";
	private Timestamp startTime;
	private String mailType = "";
	private String ruleCategory = RuleBase.MAIN_RULE;
	private String isSubRule = Constants.N;
	private String builtInRule = Constants.N;
	private String description = null;
	private int subRuleCount = -1;
	
	private String origRuleName = null;
	private int origRuleSeq = -1;
	
	/** Define properties for UI components */
	public boolean isSubRule() {
		return Constants.Y.equalsIgnoreCase(isSubRule);
	}
	public void setSubRule(boolean value) {
		setIsSubRule(value == true ? Constants.Y : Constants.N);
	}
	public String getIsSubRuleDesc() {
		if (Constants.Y.equalsIgnoreCase(getIsSubRule())) {
			return "SubRule";
		}
		else if (subRuleCount > 0) {
			return "Edit";
		}
		else {
			return "Add";
		}
	}
	public String getRuleCategoryDesc() {
		if (RuleBase.PRE_RULE.equalsIgnoreCase(getRuleCategory())) {
			return "Pre Scan";
		}
		else if (RuleBase.POST_RULE.equalsIgnoreCase(getRuleCategory())) {
			return "Post Scan";
		}
		else {
			return "Main";
		}
	}
	public boolean isBuiltInRule() {
		return Constants.Y.equalsIgnoreCase(builtInRule);
	}
	
	private java.util.Date startDate = null;
	private int startHour = -1;
	private int startMinute = -1;
	public java.util.Date getStartDate() {
		if (startDate == null) {
			if (getStartTime() == null) {
				setStartTime(new Timestamp(System.currentTimeMillis()));
			}
			startDate = new java.util.Date(getStartTime().getTime());
		}
		return startDate;
	}
	public void setStartDate(java.util.Date startDate) {
		this.startDate = startDate;
	}
	public int getStartHour() {
		if (startHour < 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(getStartTime().getTime());
			startHour = cal.get(Calendar.HOUR_OF_DAY);
		}
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	public int getStartMinute() {
		if (startMinute < 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(getStartTime().getTime());
			startMinute = cal.get(Calendar.MINUTE);
		}
		return startMinute;
	}
	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}
	/** end of UI components */
	
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public int getRuleSeq() {
		return ruleSeq;
	}
	public void setRuleSeq(int ruleSeq) {
		this.ruleSeq = ruleSeq;
	}
	public String getRuleType() {
		return ruleType;
	}
	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public String getMailType() {
		return mailType;
	}
	public void setMailType(String mailType) {
		this.mailType = mailType;
	}
	public String getRuleCategory() {
		return ruleCategory;
	}
	public void setRuleCategory(String ruleCategory) {
		this.ruleCategory = ruleCategory;
	}
	public String getIsSubRule() {
		return isSubRule;
	}
	public void setIsSubRule(String isSubRule) {
		this.isSubRule = isSubRule;
	}
	public String getBuiltInRule() {
		return builtInRule;
	}
	public void setBuiltInRule(String builtInRule) {
		this.builtInRule = builtInRule;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getSubRuleCount() {
		return subRuleCount;
	}
	public void setSubRuleCount(int subRuleCount) {
		this.subRuleCount = subRuleCount;
	}
	public String getOrigRuleName() {
		return origRuleName;
	}
	public void setOrigRuleName(String origRuleName) {
		this.origRuleName = origRuleName;
	}
	public int getOrigRuleSeq() {
		return origRuleSeq;
	}
	public void setOrigRuleSeq(int origRuleSeq) {
		this.origRuleSeq = origRuleSeq;
	}
}
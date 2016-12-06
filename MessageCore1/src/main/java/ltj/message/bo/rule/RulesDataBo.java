package ltj.message.bo.rule;

import java.util.List;

import ltj.message.vo.rule.RuleVo;

public interface RulesDataBo {
	public List<RuleVo> getCurrentRules();
	
	public RuleVo getRuleByPrimaryKey(String key);
}

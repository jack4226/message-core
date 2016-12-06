package ltj.message.dao.rule;

import java.util.List;

import ltj.message.vo.rule.RuleVo;

public interface RuleDao {
	public RuleVo getByPrimaryKey(String ruleName);
	public List<RuleVo> getActiveRules();
}

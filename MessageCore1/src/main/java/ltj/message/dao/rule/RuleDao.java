package ltj.message.dao.rule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ltj.message.dao.abstrct.AbstractDao;
import ltj.message.vo.rule.RuleElementVo;
import ltj.message.vo.rule.RuleLogicVo;
import ltj.message.vo.rule.RuleSubRuleMapVo;
import ltj.message.vo.rule.RuleVo;

@Component("ruleDao")
public class RuleDao extends AbstractDao {
	
	@Autowired
	private RuleLogicDao ruleLogicDao;
	@Autowired
	private RuleElementDao ruleElementDao;
	@Autowired
	private RuleSubRuleMapDao ruleSubRuleMapDao;
	
	private static final class RuleMapper implements RowMapper<RuleVo> {
		
		public RuleVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			RuleVo ruleVo = new RuleVo();
			
			ruleVo.setRuleName(rs.getString("rule_name"));
			
			RuleLogicVo ruleLogicVo = new RuleLogicVo();
			ruleLogicVo.setRuleName(rs.getString("rule_name"));
			ruleLogicVo.setRuleSeq(rs.getInt("rule_seq"));
			ruleLogicVo.setRuleType(rs.getString("rule_type"));
			ruleLogicVo.setStatusId(rs.getString("status_id"));
			ruleLogicVo.setStartTime(rs.getTimestamp("start_time"));
			ruleLogicVo.setMailType(rs.getString("mail_type"));
			ruleLogicVo.setRuleCategory(rs.getString("rule_category"));
			ruleLogicVo.setSubRule(rs.getBoolean("sub_rule"));
			
			RuleElementVo ruleElementVo = new RuleElementVo();
			ruleElementVo.setRuleName(rs.getString("rule_name"));
			ruleElementVo.setElementSeq(rs.getInt("element_seq"));
			ruleElementVo.setDataName(rs.getString("data_name"));
			ruleElementVo.setHeaderName(rs.getString("header_name"));
			ruleElementVo.setCriteria(rs.getString("criteria"));
			ruleElementVo.setCaseSensitive(rs.getBoolean("case_sensitive"));
			ruleElementVo.setTargetText(rs.getString("target_text"));
			ruleElementVo.setExclusions(rs.getString("exclusions"));
			ruleElementVo.setExclListProc(rs.getString("excl_list_proc"));
			ruleElementVo.setDelimiter(rs.getString("delimiter"));
			
			ruleVo.setRuleLogicVo(ruleLogicVo);
			ruleVo.getRuleElementVos().add(ruleElementVo);
			return ruleVo;
		}
	}

	public RuleVo getByPrimaryKey(String ruleName) {
		String sql = 
			"select " +
			"  logic.*, element.* " +
			" from rule_logic as logic " +
			"  join rule_element as element on logic.rule_name=element.rule_name "+
			" where logic.rule_name=? " +
			" order by element.element_seq ";
		
		Object[] parms = new Object[] {ruleName};
		
		List<?> list = getJdbcTemplate().query(sql, parms, new RuleMapper());
		if (list == null || list.size() == 0) {
			return null;
		}

		RuleVo vo = (RuleVo) list.get(0);
		for (int i = 1; i < list.size(); i++) {
			RuleVo ruleVo = (RuleVo) list.get(i);
			vo.getRuleElementVos().addAll(ruleVo.getRuleElementVos());
		}
		List<RuleSubRuleMapVo> subRules = getRuleSubRuleMapDao().getByRuleName(vo.getRuleName());
		vo.getRuleSubRuleVos().addAll(subRules);
		return (RuleVo) vo;
	}
	
	/**
	 * returns a list of RuleVo's
	 */
	public List<RuleVo> getActiveRules() {
		List<RuleLogicVo> ruleLogics = getRuleLogicDao().getActiveRules();
		List<RuleVo> ruleVos = new ArrayList<RuleVo>();
		for (int i = 0; i < ruleLogics.size(); i++) {
			RuleLogicVo ruleLogicVo = ruleLogics.get(i);
			RuleVo ruleVo = new RuleVo();
			ruleVo.setRuleName(ruleLogicVo.getRuleName());
			ruleVo.setRuleLogicVo(ruleLogicVo);
			List<RuleElementVo> elements = getRuleElementDao().getByRuleName(ruleLogicVo.getRuleName());
			ruleVo.getRuleElementVos().addAll(elements);
			List<RuleSubRuleMapVo> subRules = getRuleSubRuleMapDao().getByRuleName(ruleLogicVo.getRuleName());
			ruleVo.getRuleSubRuleVos().addAll(subRules);
			ruleVos.add(ruleVo);
		}
		return ruleVos;
	}
	
	RuleSubRuleMapDao getRuleSubRuleMapDao() {
		return ruleSubRuleMapDao;
	}

	RuleElementDao getRuleElementDao() {
		return ruleElementDao;
	}

	RuleLogicDao getRuleLogicDao() {
		return ruleLogicDao;
	}
}

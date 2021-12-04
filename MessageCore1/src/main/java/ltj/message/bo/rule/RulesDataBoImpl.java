package ltj.message.bo.rule;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ltj.message.bo.task.TaskBaseBo;
import ltj.message.dao.rule.RuleDao;
import ltj.message.external.RuleTargetProc;
import ltj.message.util.PrintUtil;
import ltj.message.vo.rule.RuleElementVo;
import ltj.message.vo.rule.RuleVo;
import ltj.spring.util.SpringUtil;

@Component("rulesDataBo")
@org.springframework.context.annotation.Scope(value="singleton")
public final class RulesDataBoImpl implements RulesDataBo {
	static final Logger logger = LogManager.getLogger(RulesDataBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private RuleDao ruleDao;

	private RulesDataBoImpl() {
	}
	
	public List<RuleVo> getCurrentRules() {
		List<RuleVo> rules = ruleDao.getActiveRules();
		substituteTargetProc(rules);
		substituteExclListProc(rules);
		return rules;
	}
	
	public RuleVo getRuleByPrimaryKey(String key) {
		RuleVo ruleVo = ruleDao.getByPrimaryKey(key);
		substituteTargetProc(ruleVo);
		substituteExclListProc(ruleVo);
		return ruleVo;
	}
	
	public RuleDao getRuleDao() {
		return ruleDao;
	}

	private void substituteTargetProc(List<RuleVo> rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
		for (RuleVo rule : rules) {
			substituteTargetProc(rule);
		}
	}
	
	private void substituteTargetProc(RuleVo rule) {
		List<RuleElementVo> elements = rule.getRuleElementVos();
		if (elements == null) {
			return;
		}
		for (RuleElementVo element : elements) {
			if (StringUtils.isBlank(element.getTargetProc())) {
				continue;
			}
			Object obj = null;
			try { // a TargetProc could be a class name or a bean id
				obj = Class.forName(element.getTargetProc()).getDeclaredConstructor().newInstance();
				logger.info("Loaded class " + element.getTargetProc() + " for rule " + rule.getRuleName());
			}
			catch (Exception e) { // not a class name, try load it as a Bean
				try {
					obj = SpringUtil.getAppContext().getBean(element.getTargetProc());
					logger.info("Loaded bean " + element.getTargetProc() + " for rule " + rule.getRuleName());
				}
				catch (Exception e2) {
					logger.warn("Failed to load: " + element.getTargetProc() + " for rule " + rule.getRuleName());
				}
				if (obj == null) {
					continue;
				}
			}
			try {
				String text = null;
				if (obj instanceof TaskBaseBo) {
					text = (String) ((TaskBaseBo) obj).process(null);
				}
				else if (obj instanceof RuleTargetProc) {
					text = ((RuleTargetProc) obj).process();
				}
				if (StringUtils.isNotBlank(text)) {
					logger.info("Changing Target Text for rule: " + rule.getRuleName());
					logger.info("  From: " + element.getTargetText());
					logger.info("    To: " + text);
					element.setTargetText(text);
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				throw new RuntimeException(e.toString());
			}
		}
	}
	
	private void substituteExclListProc(List<RuleVo> rules) {
		if (rules == null || rules.size() == 0) {
			return;
		}
		for (RuleVo rule : rules) {
			substituteExclListProc(rule);
		}
	}
	
	private void substituteExclListProc(RuleVo rule) {
		List<RuleElementVo> elements = rule.getRuleElementVos();
		if (elements == null) {
			return;
		}
		for (RuleElementVo element : elements) {
			if (StringUtils.isBlank(element.getExclListProc())) {
				continue;
			}
			Object obj = null;
			try {
				obj = SpringUtil.getAppContext().getBean(element.getExclListProc());
				logger.info("Loaded bean: " + element.getExclListProc() + " for rule " + rule.getRuleName());
			}
			catch (Exception e) {
				logger.error("Failed to load bean: " + element.getExclListProc() + " for rule " + rule.getRuleName());
			}
			if (obj instanceof TaskBaseBo) {
				try {
					String text = (String) ((TaskBaseBo) obj).process(null);
					if (StringUtils.isNotBlank(text)) {
						logger.info("Appending Exclusion list for rule: " + rule.getRuleName());
						String delimiter = element.getDelimiter();
						if (delimiter == null || delimiter.length() == 0) {
							delimiter = ",";
						}
						Set<String> emailSet = new LinkedHashSet<>();
						String origText = element.getExclusions();
						if (StringUtils.isNotBlank(origText)) {
							// Exclusion list from database column  
							String[] emails = origText.split(delimiter);
							for (String email : emails) {
								emailSet.add(email);
							}
						}
						// Exclusion list from bean class
						String[] emails = text.split(",");
						for (String email : emails) {
							emailSet.add(email);
						}
						String exclusions = StringUtils.join(emailSet, delimiter);
						element.setExclusions(exclusions);
						logger.info("  Exclusion List - " + exclusions);
					}
				}
				catch (Exception e) {
					logger.error("Exception caught", e);
					throw new RuntimeException(e.toString());
				}
			}
		}
	}
	
	public static void main(String[] args) {
		RulesDataBo bo = SpringUtil.getAppContext().getBean(RulesDataBo.class);
		List<RuleVo> rules = bo.getCurrentRules();
		for (RuleVo vo : rules) {
			logger.info(PrintUtil.prettyPrint(vo, 2));
		}
		System.exit(0);
	}
}

package ltj.msgui.bean;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import ltj.message.vo.rule.RuleLogicVo;
import ltj.msgui.util.FacesUtil;

@ManagedBean(name="builtinRule")
@javax.faces.bean.ViewScoped
public class RuleLogicBuiltinRuleBean extends RuleLogicBean {
	private static final long serialVersionUID = -498930141487046944L;
	protected static final Logger logger = Logger.getLogger(RuleLogicBuiltinRuleBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Override
	public DataModel<RuleLogicVo> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (ruleLogics == null) {
			List<RuleLogicVo> ruleLogicList = getRuleLogicDao().getAll(true);
			for (RuleLogicVo rc : ruleLogicList) {
				boolean hasActions = getMsgActionDao().getHasActions(rc.getRuleName());
				hasActionsMap.put(rc.getRuleName(), hasActions);
			}
			ruleLogics = new ListDataModel<RuleLogicVo>(ruleLogicList);
		}
		return ruleLogics;
	}
	
}

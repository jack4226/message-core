package ltj.msgui.bean;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectOne;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.constant.CodeType;
import ltj.message.constant.RuleCategory;
import ltj.message.constant.RuleCriteria;
import ltj.message.constant.RuleDataName;
import ltj.message.constant.RuleType;
import ltj.message.dao.action.MsgActionDao;
import ltj.message.dao.action.MsgActionDetailDao;
import ltj.message.dao.rule.RuleElementDao;
import ltj.message.dao.rule.RuleLogicDao;
import ltj.message.dao.rule.RuleSubRuleMapDao;
import ltj.message.util.BlobUtil;
import ltj.message.vo.action.MsgActionDetailVo;
import ltj.message.vo.action.MsgActionVo;
import ltj.message.vo.rule.RuleElementVo;
import ltj.message.vo.rule.RuleLogicVo;
import ltj.message.vo.rule.RuleSubRuleMapVo;
import ltj.msgui.util.FacesUtil;
import ltj.msgui.util.SpringUtil;

@ManagedBean(name="ruleLogic")
@javax.faces.bean.ViewScoped
public class RuleLogicBean implements java.io.Serializable {
	private static final long serialVersionUID = 4143310214559095471L;
	protected static final Logger logger = Logger.getLogger(RuleLogicBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	protected transient RuleLogicDao ruleLogicDao = null;
	protected transient RuleElementDao ruleElementDao = null;
	protected transient RuleSubRuleMapDao ruleSubRuleMapDao = null;
	protected transient MsgActionDao msgActionDao = null;
	protected transient MsgActionDetailDao actionDetailDao = null;
		
	protected transient DataModel<RuleLogicVo> ruleLogics = null;
	protected Map<String, Boolean> hasActionsMap = new LinkedHashMap<>();
	protected Map<String, Boolean> hasSubruleMap = new LinkedHashMap<>();
	
	protected RuleLogicVo ruleLogic = null;
	protected boolean editMode = true;
	protected BeanMode beanMode = BeanMode.list;
	
	protected String testResult = null;
	protected String actionFailure = null;
	
	protected transient UIInput ruleNameInput = null;
	protected transient UIInput startDateInput = null;
	
	protected transient DataModel<RuleElementVo> ruleElements = null;
	protected transient DataModel<RuleSubRuleMapVo> subRules = null;
	protected transient DataModel<MsgActionVo> ruleActions = null;
	
	protected RuleElementVo ruleElement = null;
	protected RuleElementVo origRuleElement = null;
	
	/* use navigation rules in faces-config.xml */
	protected static final String TO_SELF = null;
	protected static final String TO_CANCELED = "cancel";
	protected static final String TO_FAILED = null;
	protected static final String TO_EDIT_LOGIC = "ruleLogicEdit.xhtml";
	protected static final String TO_EDIT_ELEMENT = "ruleElementEdit.xhtml";
	protected static final String TO_EDIT_SUBRULE = "ruleSubruleEdit.xhtml";
	protected static final String TO_EDIT_ACTION = "ruleActionEdit.xhtml";
	
	protected static final String TO_CONFIG_CUSTOM_RULES = "configureCustomRules.xhtml";
	protected static final String TO_CUSTOMIZE_BUILTIN_RULES = "customizeBuiltinRules.xhtml";
	protected static final String RULE_ACTION_SAVED = TO_CONFIG_CUSTOM_RULES;
	
	protected String sourcePage; // f:setPropertyActionListener tag
	
	public String getSourcePage() {
		return sourcePage;
	}

	public void setSourcePage(String sourcePage) {
		this.sourcePage = sourcePage;
	}

	protected RuleLogicDao getRuleLogicDao() {
		if (ruleLogicDao == null) {
			ruleLogicDao = SpringUtil.getWebAppContext().getBean(RuleLogicDao.class);
		}
		return ruleLogicDao;
	}

	protected RuleElementDao getRuleElementDao() {
		if (ruleElementDao == null) {
			ruleElementDao = SpringUtil.getWebAppContext().getBean(RuleElementDao.class);
		}
		return ruleElementDao;
	}

	protected RuleSubRuleMapDao getRuleSubRuleMapDao() {
		if (ruleSubRuleMapDao == null) {
			ruleSubRuleMapDao = SpringUtil.getWebAppContext().getBean(RuleSubRuleMapDao.class);
		}
		return ruleSubRuleMapDao;
	}

	protected MsgActionDao getMsgActionDao() {
		if (msgActionDao == null) {
			msgActionDao = SpringUtil.getWebAppContext().getBean(MsgActionDao.class);
		}
		return msgActionDao;
	}

	private MsgActionDetailDao getMsgActionDetailDao() {
		if (actionDetailDao == null) {
			actionDetailDao = SpringUtil.getWebAppContext().getBean(MsgActionDetailDao.class);
		}
		return actionDetailDao;
	}

	/*
	 * Main Page Section 
	 */
	
	public DataModel<RuleLogicVo> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		logger.info("getAll() - From page: " + fromPage + ", ruleLogics==null? " + (ruleLogics==null));
		if (ruleLogics == null) {
			List<RuleLogicVo> ruleLogicList = getRuleLogicDao().getAll(false);
			hasSubruleMap.clear();
			hasActionsMap.clear();
			for (RuleLogicVo rc : ruleLogicList) {
				boolean hasSubrule = getRuleLogicDao().getHasSubRules(rc.getRuleName());
				hasSubruleMap.put(rc.getRuleName(), hasSubrule);
				boolean hasActions = getMsgActionDao().getHasActions(rc.getRuleName());
				hasActionsMap.put(rc.getRuleName(), hasActions);
			}
			ruleLogics = new ListDataModel<RuleLogicVo>(ruleLogicList);
		}
		return ruleLogics;
	}
	
	/*
	 * Use String signature for rowId to support JSF script.
	 */
	public String findRuleNameByRowId(String rowId) {
		RuleLogicVo rl = getRuleLogicDao().getByRowId(Integer.parseInt(rowId));
		if (rl == null) {
			return TO_SELF;
		}
		return rl.getRuleName();
	}

	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public String refresh() {
		if (isDebugEnabled)
			logger.debug("refresh() - Entering...");
		ruleLogics = null;
		ruleElements = null;
		subRules = null;
		ruleActions = null;
		return TO_SELF;
	}
	
	public void viewRuleLogicListener(AjaxBehaviorEvent event) {
		viewRuleLogic();
	}
	
	public String viewRuleLogic() {
		if (isDebugEnabled)
			logger.debug("viewRuleLogic() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewRuleLogic() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewRuleLogic() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		// clean up
		ruleElements = null;
		startDateInput = null; // so it could be rebound to a new record
		// end of clean up
		this.ruleLogic = ruleLogics.getRowData();
		logger.info("viewRuleLogic() - RuleLogic to be edited: " + ruleLogic.getRuleName());
		ruleLogic.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled)
			logger.debug("viewRuleLogic() - RuleLogic to be passed to jsp: " + ruleLogic);
		
		return TO_EDIT_LOGIC;
	}
	
	public void viewSubRulesListener(AjaxBehaviorEvent event) {
		viewSubRules();
	}
	
	public String viewSubRules() {
		if (isDebugEnabled)
			logger.debug("viewSubRules() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewSubRules() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewSubRules() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		subRules = null;
		this.ruleLogic = ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		beanMode = BeanMode.subrules;
		return TO_EDIT_SUBRULE;
	}
	
	public void viewMsgActionsListener(AjaxBehaviorEvent event) {
		viewMsgActions();
	}
	
	public String viewMsgActions() {
		if (isDebugEnabled)
			logger.debug("viewMsgActions() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewMsgActions() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewMsgActions() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		ruleActions = null;
		this.ruleLogic = ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		beanMode = BeanMode.actions;
		return TO_EDIT_ACTION;
	}
	
	public void viewRuleElementListener(AjaxBehaviorEvent event) {
		viewRuleElement();
	}
	
	public String viewRuleElement() {
		if (isDebugEnabled)
			logger.debug("viewRuleElement() - Entering...");
		if (ruleElements == null) {
			logger.warn("viewRuleElement() - RuleElement List is null.");
			return TO_FAILED;
		}
		if (!ruleElements.isRowAvailable()) {
			logger.warn("viewRuleElement() - RuleElement Row not available.");
			return TO_FAILED;
		}
		reset();
		origRuleElement = ruleElements.getRowData();
		ruleElement = (RuleElementVo) BlobUtil.deepCopy(origRuleElement);
		ruleElement.setMarkedForEdition(true);
		beanMode = BeanMode.elements;
		return TO_EDIT_ELEMENT;
	}
	
	public void doneRuleElementEditListener(AjaxBehaviorEvent event) {
		doneRuleElementEdit();
	}
	
	public String doneRuleElementEdit() {
		if (isDebugEnabled)
			logger.debug("doneRuleElementEdit() - Entering...");
		if (ruleElement == null) {
			logger.warn("doneRuleElementEdit() - RuleElement is null.");
			return TO_FAILED;
		}
		copyProperties(origRuleElement, ruleElement);
		if (StringUtils.isNotBlank(origRuleElement.getExclusions())) {
			if (StringUtils.isBlank(origRuleElement.getDelimiter())) {
				origRuleElement.setDelimiter(",");
			}
		}
		beanMode = BeanMode.edit;
		return TO_EDIT_LOGIC;
	}

	private void copyProperties(RuleElementVo dest, RuleElementVo src) {
//		RuleElementPK pk = new RuleElementPK();
//		pk.setRuleLogic(src.getRuleElementPK().getRuleLogic());
//		pk.setElementSequence(src.getRuleElementPK().getElementSequence());
//		dest.setRuleElementPK(pk);
		dest.setDataName(src.getDataName());
		dest.setHeaderName(src.getHeaderName());
		dest.setCriteria(src.getCriteria());
		dest.setCaseSensitive(src.isCaseSensitive());
		dest.setTargetText(src.getTargetText());
		dest.setTargetProc(src.getTargetProc());
		dest.setExclusions(src.getExclusions());
		dest.setExclListProc(src.getExclListProc());
		dest.setDelimiter(src.getDelimiter());
	}
	
	public void saveRuleElementListener(AjaxBehaviorEvent event) {
		saveRuleElement();
	}
	
	public String saveRuleElement() {
		if (isDebugEnabled)
			logger.debug("saveRuleElement() - Entering...");
		if (ruleElement == null) {
			logger.warn("saveRuleElement() - RuleElement is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			ruleLogic.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		// first delete the rule element
		getRuleElementDao().deleteByPrimaryKey(ruleElement.getRuleName(), ruleElement.getElementSeq());
		// insert the record
		getRuleElementDao().insert(ruleElement);
		logger.info("saveRuleElement() - Element Rows Deleted: " + 1);
		//beanMode = BeanMode.edit;
		return "msgrule.ruleelement.saved";
	}

	public void saveRuleLogicListener(AjaxBehaviorEvent event) {
		saveRuleLogic();
	}

	public String saveRuleLogic() {
		if (isDebugEnabled)
			logger.debug("saveRuleLogic() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("saveRuleLogic() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// set startTime from startDate, startHour and startMinute
		Calendar cal = Calendar.getInstance();
		cal.setTime(ruleLogic.getStartTime());
		ruleLogic.setStartTime(new Timestamp(cal.getTimeInMillis()));
		// end of startTime
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			ruleLogic.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getRuleLogicDao().update(ruleLogic);
			logger.info("saveRuleLogic() - Rows Updated: " + 1);
			int rowsDeleted = getRuleElementDao().deleteByRuleName(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Deleted: " + rowsDeleted);
			int rowsInserted = insertRuleElements(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Inserted: " + rowsInserted);
		}
		else {
			List<RuleElementVo> elements = ruleElementDao.getByRuleName(ruleLogic.getRuleName());
			if (elements != null) {
				for (RuleElementVo element : elements) {
					element.setRuleName(ruleLogic.getRuleName());
				}
			}
			getRuleLogicDao().insert(ruleLogic);
			logger.info("saveRuleLogic() - Rows Inserted: " + 1);
			addToRuleList(ruleLogic);
//			int elementsInserted = insertRuleElements(ruleLogic.getRuleName());
//			logger.info("saveRuleLogic() - Element Rows Inserted: " + elementsInserted);
		}
		//beanMode = BeanMode.list;
		return TO_CONFIG_CUSTOM_RULES;
	}

	protected int insertRuleElements(String _ruleName) {
		List<RuleElementVo> list = getRuleElementList();
		int rowsInserted = 0;
		for (int i=0; i<list.size(); i++) {
			RuleElementVo ruleElementVo = list.get(i);
			RuleElementVo vo = new RuleElementVo();
			ruleElementVo.copyPropertiesTo(vo);
			vo.setRuleName(_ruleName);
			vo.setElementSeq(i);
			getRuleElementDao().insert(vo);
		}
		return rowsInserted;
	}
	
	@SuppressWarnings("unchecked")
	protected void addToRuleList(RuleLogicVo vo) {
		List<RuleLogicVo> list = (List<RuleLogicVo>) ruleLogics.getWrappedData();
		list.add(vo);
	}

	public void deleteRuleLogicsListener(AjaxBehaviorEvent event) {
		deleteRuleLogics();
	}

	public String deleteRuleLogics() {
		if (isDebugEnabled)
			logger.debug("deleteRuleLogics() - Entering...");
		if (ruleLogics == null) {
			logger.warn("deleteRuleLogics() - RuleLogic List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleLogicVo> list = getRuleLogicList();
		for (int i = 0; i < list.size(); i++) {
			RuleLogicVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleLogicDao().deleteByPrimaryKey(vo.getRuleName());
				if (rowsDeleted > 0) {
					logger.info("deleteRuleLogics() - RuleLogic deleted: " + vo.getRuleName());
				}
				list.remove(vo);
			}
		}
		return "msgrule.deleted";
	}
	
	public String testRuleLogic() {
		if (isDebugEnabled)
			logger.debug("testRuleLogic() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("testRuleLogic() - RuleLogic is null.");
			return TO_FAILED;
		}
		return TO_SELF;
	}
	
	public void copyRuleLogicListener(AjaxBehaviorEvent event) {
		copyRuleLogic();
	}
	
	public String copyRuleLogic() {
		if (isDebugEnabled)
			logger.debug("copyRuleLogic() - Entering...");
		if (ruleLogics == null) {
			logger.warn("copyRuleLogic() - RuleLogic List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleLogicVo> list = getRuleLogicList();
		for (int i=0; i<list.size(); i++) {
			RuleLogicVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				this.ruleLogic = new RuleLogicVo();
				try {
					vo.copyPropertiesTo(this.ruleLogic);
					this.ruleLogic.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				ruleElements = null;
				getRuleElements();
				// set processor fields to null as they are invisible from UI
				List<RuleElementVo> elements = getRuleElementList();
				for (RuleElementVo element : elements) {
					element.setTargetProc(null);
					element.setExclListProc(null);
				}
				// end of null
				ruleElements.getWrappedData();
				ruleLogic.setRuleName(null);
				ruleLogic.setRuleSeq(getRuleLogicDao().getNextRuleSequence());
				ruleLogic.setRuleType(RuleType.SIMPLE.value());
				ruleLogic.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT_LOGIC;
			}
		}
		return TO_SELF;
	}
	
	public void addRuleLogicListener(AjaxBehaviorEvent event) {
		addRuleLogic();
	}
	
	public String addRuleLogic() {
		if (isDebugEnabled)
			logger.debug("addRuleLogic() - Entering...");
		reset();
		ruleElements = null;
		this.ruleLogic = new RuleLogicVo();
		ruleLogic.setMarkedForEdition(true);
		//ruleLogic.setUpdtUserId(Constants.DEFAULT_USER_ID);
		ruleLogic.setRuleSeq(getRuleLogicDao().getNextRuleSequence());
		ruleLogic.setRuleType(RuleType.SIMPLE.value());
		ruleLogic.setRuleCategory(RuleCategory.MAIN_RULE.value());
		ruleLogic.setStartTime(new Timestamp(System.currentTimeMillis()));
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT_LOGIC;
	}
	
	public void cancelEditListener(AjaxBehaviorEvent event) {
		String sourceId = "";
		if (event != null) {
			logger.info("cancelEditListener() - " + event.getSource());
			Object obj = event.getSource();
			if (obj instanceof HtmlCommandButton) {
				HtmlCommandButton cmdbtn = (HtmlCommandButton) obj;
				logger.info("HtmlCommandButton value: " + cmdbtn.getValue() + ", Id: " + cmdbtn.getId());
				sourceId = cmdbtn.getId();
			}
		}
		cancelEdit(sourceId);
	}
	
	public void cancelEdit(String sourceId) {
		if (isDebugEnabled)
			logger.debug("cancelEdit() - source id = " + sourceId);
		refresh();
		if (StringUtils.contains(sourceId, "LogicEdit")) {
			beanMode = BeanMode.list;
		}
		else if (StringUtils.contains(sourceId, "ElementEdit")) {
			beanMode = BeanMode.edit;
		}
		else if (StringUtils.contains(sourceId, "ActionEdit")) {
			beanMode = BeanMode.list;
		}
		else if (StringUtils.contains(sourceId, "SubruleEdit")) {
			beanMode = BeanMode.list;
		}
		else {
			beanMode = BeanMode.list;
		}
	}
	
	public String cancelEdit() {
		if (isDebugEnabled)
			logger.debug("cancelEdit() - Entering...");
		refresh();
		String viewId = FacesUtil.getCurrentViewId();
		if (StringUtils.contains(viewId, TO_EDIT_ELEMENT)) {
			beanMode = BeanMode.edit;
			return TO_EDIT_LOGIC;
		}
		else if (StringUtils.contains(viewId, TO_EDIT_ACTION)) {
			beanMode = BeanMode.list;
			return TO_CONFIG_CUSTOM_RULES;
		}
		else if (StringUtils.contains(viewId, TO_EDIT_SUBRULE)) {
			beanMode = BeanMode.edit;
			return TO_CONFIG_CUSTOM_RULES;
		}
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}
	
	public boolean getCanMoveUp() {
		if (ruleLogics == null || !ruleLogics.isRowAvailable()) {
			return false;
		}
		RuleLogicVo vo = ruleLogics.getRowData();
		int idx = ruleLogics.getRowIndex();
		if (idx > 0) {
			RuleLogicVo up = getRuleLogicList().get(idx - 1);
			if (vo.getRuleCategory().equals(up.getRuleCategory())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getCanMoveDown() {
		if (ruleLogics == null || !ruleLogics.isRowAvailable()) {
			return false;
		}
		RuleLogicVo vo = ruleLogics.getRowData();
		int idx = ruleLogics.getRowIndex();
		if (idx < (ruleLogics.getRowCount() + 1)) {
			RuleLogicVo down = getRuleLogicList().get(idx + 1);
			if (vo.getRuleCategory().equals(down.getRuleCategory())) {
				return true;
			}
		}
		return false;
	}
	
	public String moveUp() {
		if (isDebugEnabled)
			logger.debug("moveUp() - Entering...");
		moveUpDownRule(-1);
		return TO_SELF;
	}
	
	public String moveDown() {
		if (isDebugEnabled)
			logger.debug("moveDown() - Entering...");
		moveUpDownRule(1);
		return TO_SELF;
	}
	
	public void moveUpListener(AjaxBehaviorEvent event) {
		logger.info("moveUpListener() - Event Source: " + event.getSource());
		moveUp();
	}

	public void moveDownListener(AjaxBehaviorEvent event) {
		logger.info("moveDownListener() - Event Source: " + event.getSource());
		moveDown();
	}

	/*
	 * @param updown - move current rule up or down:
	 * 		-1 -> move up
	 * 		+1 -> move down
	 */
	protected void moveUpDownRule(int updown) {
		reset();
		RuleLogicVo currVo = getAll().getRowData();
		int index = ruleLogics.getRowIndex();
		List<RuleLogicVo> list = getRuleLogicList();
		RuleLogicVo prevVo = list.get(index + updown);
		if (currVo.getRuleCategory().equals(prevVo.getRuleCategory())) {
			int currSeq = currVo.getRuleSeq();
			int prevSeq = prevVo.getRuleSeq();
			currVo.setRuleSeq(prevSeq);
			prevVo.setRuleSeq(currSeq);
			getRuleLogicDao().update(currVo);
			getRuleLogicDao().update(prevVo);
			refresh();
		}
	}
	
	/*
	 * Rule Elements Section
	 */
	
	public void refreshElementsListener(AjaxBehaviorEvent event) {
		refreshElements();
	}
	
	public String refreshElements() {
		if (isDebugEnabled)
			logger.debug("refreshElements() - Entering...");
		ruleElements = null;
		getRuleElements();
		return TO_SELF;
	}
	
	public DataModel<RuleElementVo> getRuleElements() {
		if (isDebugEnabled)
			logger.debug("getRuleElement() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("getRuleElements() - RuleLogic is null.");
			return null;
		}
		if (ruleElements == null) {
			String key = ruleLogic.getRuleName();
			List<RuleElementVo> list = getRuleElementDao().getByRuleName(key);
			ruleElements = new ListDataModel<RuleElementVo>(list);
		}
		return ruleElements;
	}
	
	@SuppressWarnings("unchecked")
	protected List<RuleElementVo> getRuleElementList() {
		List<RuleElementVo> list = (List<RuleElementVo>) getRuleElements().getWrappedData();
		return list;
	}
	
	public void deleteRuleElementsListener(AjaxBehaviorEvent event) {
		deleteRuleElements();
	}

	public String deleteRuleElements() {
		if (isDebugEnabled)
			logger.debug("deleteRuleElements() - Entering...");
		if (ruleElements == null) {
			logger.warn("deleteRuleElements() - RuleElement List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleElementVo> list = getRuleElementList();
		for (int i = 0; i < list.size(); i++) {
			RuleElementVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleElementDao().deleteByPrimaryKey(vo.getRuleName(), vo.getElementSeq());
				if (rowsDeleted > 0) {
					logger.info("deleteRuleElements() - RuleElement deleted: " + vo.getRuleName() + ":" + vo.getElementSeq());
				}
				list.remove(vo);
			}
		}
		return TO_SELF;
	}
	
	public void copyRuleElementListener(AjaxBehaviorEvent event) {
		copyRuleElement();
	}

	public String copyRuleElement() {
		if (isDebugEnabled)
			logger.debug("copyRuleElement() - Entering...");
		if (ruleElements == null) {
			logger.warn("copyRuleElement() - RuleElement List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleElementVo> list = getRuleElementList();
		for (int i=0; i<list.size(); i++) {
			RuleElementVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleElementVo vo2 = new RuleElementVo();
				try {
					vo.copyPropertiesTo(vo2);
					vo2.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				vo2.setElementSeq(getNextRuleElementSeq());
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public void addRuleElementListener(AjaxBehaviorEvent event) {
		addRuleElement();
	}

	public String addRuleElement() {
		if (isDebugEnabled)
			logger.debug("addRuleElement() - Entering...");
		reset();
		List<RuleElementVo> list = getRuleElementList();
		RuleElementVo vo = new RuleElementVo();
		vo.setRuleName(ruleLogic.getRuleName());
		vo.setElementSeq(getNextRuleElementSeq());
		vo.setDataName(RuleDataName.BCC_ADDR.getValue());
		vo.setCriteria(RuleCriteria.STARTS_WITH.value());
		vo.setMarkedForEdition(true);
		list.add(vo);
		return TO_SELF;
	}
	
	private int getNextRuleElementSeq() {
		List<RuleElementVo> list = getRuleElementList();
		if (list == null || list.isEmpty()) {
			return 0;
		}
		else {
			int seq = list.size() - 1;
			for (RuleElementVo vo : list) { // just for safety
				if (vo.getElementSeq() > seq) {
					seq = vo.getElementSeq();
				}
			}
			return seq + 1;
		}
	}
	
	/*
	 * Sub-Rules Section
	 */
	
	public void refreshSubRulesListener(AjaxBehaviorEvent event) {
		refreshSubRules();
	}
	
	public String refreshSubRules() {
		if (isDebugEnabled)
			logger.debug("refreshSubRules() - Entering...");
		subRules = null;
		getSubRules();
		return TO_SELF;
	}
	
	public DataModel<RuleSubRuleMapVo> getSubRules() {
		if (isDebugEnabled)
			logger.debug("getSubRules() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("getSubRules() - RuleLogic is null.");
			return null;
		}
		if (subRules == null) {
			String key = ruleLogic.getRuleName();
			List<RuleSubRuleMapVo> list = getRuleSubRuleMapDao().getByRuleName(key);
			subRules = new ListDataModel<RuleSubRuleMapVo>(list);
		}
		return subRules;
	}
	
	@SuppressWarnings("unchecked")
	protected List<RuleSubRuleMapVo> getSubRuleList() {
		List<RuleSubRuleMapVo> list = (List<RuleSubRuleMapVo>) getSubRules().getWrappedData();
		return list;
	}
	
	public void deleteSubRulesListener(AjaxBehaviorEvent event) {
		deleteSubRules();
	}

	public String deleteSubRules() {
		if (isDebugEnabled)
			logger.debug("deleteSubRules() - Entering...");
		if (subRules == null) {
			logger.warn("deleteSubRules() - SubRule List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleSubRuleMapVo> list = getSubRuleList();
		for (int i=0; i<list.size(); i++) {
			RuleSubRuleMapVo vo = list.get(i); 
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleSubRuleMapDao().deleteByPrimaryKey(vo.getRuleName(), vo.getSubRuleName());
				if (rowsDeleted > 0) {
					logger.info("deleteSubRules() - SubRule deleted: " + vo.getRuleName() + ":" + vo.getSubRuleName());
				}
				vo.setMarkedForDeletion(false);
				list.remove(vo);
			}
		}
		refreshSubRules();
		return TO_SELF;
	}
	
	public void copySubRuleListener(AjaxBehaviorEvent event) {
		copySubRule();
	}

	public String copySubRule() {
		if (isDebugEnabled)
			logger.debug("copySubRule() - Entering...");
		if (subRules == null) {
			logger.warn("copySubRule() - SubRule List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleSubRuleMapVo> list = getSubRuleList();
		for (int i=0; i<list.size(); i++) {
			RuleSubRuleMapVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleSubRuleMapVo vo2 = new RuleSubRuleMapVo();
				try {
					vo.copyPropertiesTo(vo2);
					vo2.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public void addSubRuleListener(AjaxBehaviorEvent event) {
		addSubRule();
	}

	public String addSubRule() {
		if (isDebugEnabled)
			logger.debug("addSubRule() - Entering...");
		reset();
		List<RuleSubRuleMapVo> list = getSubRuleList();
		RuleSubRuleMapVo vo = new RuleSubRuleMapVo();
		vo.setRuleName(ruleLogic.getRuleName());
		vo.setSubRuleSeq(0); // TODO
		vo.setMarkedForEdition(true);
		List<RuleLogicVo> subrules = getRuleLogicDao().getAllSubRules(false);
		if (!subrules.isEmpty()) { // set a default rule name
			vo.setSubRuleName(subrules.get(0).getRuleName());
		}
		list.add(vo);
		return TO_SELF;
	}
	
	public void saveSubRulesListener(AjaxBehaviorEvent event) {
		saveSubRules();
	}

	public String saveSubRules() {
		if (isDebugEnabled)
			logger.debug("saveSubRules() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("saveSubRules() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		int rowsDeleted = getRuleSubRuleMapDao().deleteByRuleName(ruleLogic.getRuleName());
		logger.info("saveSubRules() - SubRule Rows Deleted: " + rowsDeleted);
		
		List<RuleSubRuleMapVo> list = getSubRuleList();
		if (hasDuplicateSubRules(list)) {
			testResult = "duplicateSubRuleFound";
			/* Add to Face message queue. Not working. */
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", testResult, null);
			FacesContext.getCurrentInstance().addMessage(null, message);
			return null;
		}
		for (int i=0; i<list.size(); i++) {
			RuleSubRuleMapVo ruleSubRuleMapVo = list.get(i);
			RuleLogicVo subrule = getRuleLogicDao().getByRuleName(ruleSubRuleMapVo.getSubRuleName());
			if (subrule != null) { // should never happen
				ruleSubRuleMapVo.setSubRuleName(subrule.getRuleName());
				ruleSubRuleMapVo.setSubRuleSeq(i);
				getRuleSubRuleMapDao().insert(ruleSubRuleMapVo);
			}
		}
		logger.info("saveSubRules() - SubRule Rows Inserted: " + list.size());
		//beanMode = BeanMode.subrules;
		return TO_CONFIG_CUSTOM_RULES;
	}

	public void moveUpSubRuleListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("moveUpSubRule() - Entering...");
		moveSubRule(-1);
	}
	
	public void moveDownSubRuleListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("moveDownSubRule() - Entering...");
		moveSubRule(1);
	}
	
	protected boolean hasDuplicateSubRules(List<RuleSubRuleMapVo> list) {
		if (list == null || list.size() <= 1) {
			return false;
		}
		for (int i=0; i<list.size(); i++) {
			RuleSubRuleMapVo vo1 = list.get(i);
			for (int j=i+1; j<list.size(); j++) {
				RuleSubRuleMapVo vo2 = list.get(j);
				if (StringUtils.equals(vo1.getSubRuleName(), vo2.getSubRuleName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * @param updown - move current sub-rule up or down:
	 * 		-1 -> move up
	 * 		+1 -> move down
	 */
	protected void moveSubRule(int updown) {
		reset();
		RuleSubRuleMapVo currVo = getSubRules().getRowData();
		int index = subRules.getRowIndex();
		List<RuleSubRuleMapVo> list = getSubRuleList();
		RuleSubRuleMapVo prevVo = list.get(index + updown);
		int currSeq = currVo.getSubRuleSeq();
		int prevSeq = prevVo.getSubRuleSeq();
		currVo.setSubRuleSeq(prevSeq);
		prevVo.setSubRuleSeq(currSeq);
		getRuleSubRuleMapDao().update(currVo);
		getRuleSubRuleMapDao().update(prevVo);
		refreshSubRules();
	}
	
	/*
	 * Message Actions Section
	 */
	
	public void refreshMsgActionsListener(AjaxBehaviorEvent event) {
		refreshMsgActions();
	}
	
	public String refreshMsgActions() {
		if (isDebugEnabled)
			logger.debug("refreshMsgActions() - Entering...");
		ruleActions = null;
		getMsgActions();
		return TO_SELF;
	}
	
	public DataModel<MsgActionVo> getMsgActions() {
		if (isDebugEnabled)
			logger.debug("getMsgActions() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("getMsgActions() - RuleLogic is null.");
			return null;
		}
		if (ruleActions == null) {
			String key = ruleLogic.getRuleName();
			List<MsgActionVo> list = getMsgActionDao().getByRuleName(key);
			ruleActions = new ListDataModel<MsgActionVo>(list);
		}
		return ruleActions;
	}
	
	protected List<MsgActionVo> getMsgActionList() {
		@SuppressWarnings("unchecked")
		List<MsgActionVo> list = (List<MsgActionVo>) getMsgActions().getWrappedData();
		return list;
	}
	
	public void deleteMsgActionsListener(AjaxBehaviorEvent event) {
		deleteMsgActions();
	}

	public String deleteMsgActions() {
		if (isDebugEnabled)
			logger.debug("deleteMsgActions() - Entering...");
		if (ruleActions == null) {
			logger.warn("deleteMsgActions() - MsgAction List is null.");
			return TO_FAILED;
		}
		reset();
		List<MsgActionVo> list = getMsgActionList();
		for (int i = 0; i < list.size(); i++) {
			MsgActionVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMsgActionDao().deleteByPrimaryKey(vo.getRowId());
				if (rowsDeleted > 0) {
					logger.info("deleteMsgActions() - MsgAction deleted: " + vo.getRuleName() + "."
							+ vo.getActionSeq() + "." + vo.getStartTime() +
							"." + (vo.getClientId()==null?"":vo.getClientId()));
				}
				list.remove(vo);
			}
		}
		return TO_SELF;
	}
	
	public void copyMsgActionListener(AjaxBehaviorEvent event) {
		copyMsgAction();
	}

	public String copyMsgAction() {
		if (isDebugEnabled)
			logger.debug("copyMsgAction() - Entering...");
		if (ruleActions == null) {
			logger.warn("copyMsgAction() - MsgAction List is null.");
			return TO_FAILED;
		}
		reset();
		List<MsgActionVo> list = getMsgActionList();
		for (int i=0; i<list.size(); i++) {
			MsgActionVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				MsgActionVo vo2 =  new MsgActionVo();
				try {
					vo.copyPropertiesTo(vo2);
					vo2.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public void addMsgActionListener(AjaxBehaviorEvent event) {
		addMsgAction();
	}

	public String addMsgAction() {
		if (isDebugEnabled)
			logger.debug("addMsgAction() - Entering...");
		reset();
		List<MsgActionVo> list = getMsgActionList();
		MsgActionVo vo = new MsgActionVo();
		
		vo.setActionSeq(list.size() + 1);
		vo.setStartTime(new Timestamp(System.currentTimeMillis()));
		vo.setStatusId(CodeType.Y.value());
		vo.setMarkedForEdition(true);
		List<String> actionIdList = getMsgActionDetailDao().getActionIds();
		MsgActionDetailVo detail = getMsgActionDetailDao().getByActionId(actionIdList.get(0));
		vo.setActionId(detail.getActionId());
		list.add(vo);
		return TO_SELF;
	}
	
	public void saveMsgActionsListener(AjaxBehaviorEvent event) {
		saveMsgActions();
	}

	public String saveMsgActions() {
		if (isDebugEnabled)
			logger.debug("saveMsgActions() - Entering..., sourcePage = " + sourcePage);
		if (getRuleLogic() == null) {
			logger.warn("saveMsgActions() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		int rowsDeleted = getMsgActionDao().deleteByRuleName(ruleLogic.getRuleName());
		logger.info("saveMsgActions() - MsgAction Rows Deleted: " + rowsDeleted);
		
		List<MsgActionVo> list = getMsgActionList();
		for (int i=0; i<list.size(); i++) {
			MsgActionVo ruleAction = list.get(i);
			ruleAction.setRuleName(ruleLogic.getRuleName());
			// set startTime from startDate and startHour
			Calendar cal = Calendar.getInstance();
			cal.setTime(ruleAction.getStartTime());
			ruleAction.setStartTime(new Timestamp(cal.getTimeInMillis()));
			// end of startTime
			getMsgActionDao().insert(ruleAction);
		}
		logger.info("saveMsgActions() - MsgAction Rows Inserted: " + list.size());
		logger.info("saveMsgActions() - View Id: " + FacesUtil.getCurrentViewId());
		if ("rule-action-built-in".equals(sourcePage)) {
			//beanMode = BeanMode.actions;
			return TO_CUSTOMIZE_BUILTIN_RULES;
		}
		else if ("rule-action".equals(sourcePage)) {
			//beanMode = BeanMode.actions;
			return RULE_ACTION_SAVED;
		}
		return TO_SELF;
	}

	/*
	 * @deprecated - not used by any JSF pages
	 * define value change listener
	 */
	public void actionIdChanged(ValueChangeEvent event) {
		if (getRuleLogic() == null) {
			logger.warn("actionIdChanged() - RuleLogic is null.");
		}
		logger.info("actionIdChanged() - " + event.getOldValue() + " -> " + event.getNewValue());
		for (Iterator<MsgActionVo> it=ruleActions.iterator(); it.hasNext();) {
			MsgActionVo ra = it.next();
			logger.info("actionIdChanged() - RuleAction Id: " + ra.getActionId());
		}
	}

	public void changedActionId(AjaxBehaviorEvent event) {
		if (getRuleLogic() == null) {
			logger.warn("changedActionId() - RuleLogic is null.");
		}
		logger.info("changedActionId() - " + event);
		for (Iterator<MsgActionVo> it=ruleActions.iterator(); it.hasNext();) {
			MsgActionVo ra = it.next();
			logger.info("changedActionId() - RuleAction Id: " + ra.getActionId());
		}
		if (event == null) return;
		UISelectOne select = (UISelectOne) event.getSource();
        if (select.getValue() == null || select.getValue().toString().isEmpty()) {
            logger.info("Selected value is blank");
        }
        else {
            String value = select.getValue().toString();
            logger.info("Selected value: " + value);
        }
	}

	/*
	 * define ajax listener for ruleActionBuiltinEdit.xhtml
	 * jsf2 ajax event list:
	  	blur
		change
		click
		dblclick
		focus
		keydown
		keypress
		keyup
		mousedown
		mousemove
		mouseout
		mouseover
		mouseup
		select
	 */
	public void changedSenderId(AjaxBehaviorEvent event) {
		if (getRuleLogic() == null) {
			logger.warn("changeSenderId() - RuleLogic is null.");
		}
		for (Iterator<MsgActionVo> it=ruleActions.iterator(); it.hasNext();) {
			MsgActionVo ra = it.next();
			logger.info("changeSenderId() - Sender Id: " + ra.getClientId());
		}
		if (event == null) return;
		UISelectOne select = (UISelectOne) event.getSource();
		//UIComponent component = event.getComponent();
        if (select.getValue() == null || select.getValue().toString().isEmpty()) {
            logger.info("Selected value is blank");
        }
        else {
            String value = select.getValue().toString();
            logger.info("Selected value: " + value);
        }
	}


	/*
	 * Logic Evaluation Section 
	 */
	
	public boolean getAnyRulesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyRulesMarkedForDeletion() - Entering...");
		List<RuleLogicVo> list = getRuleLogicList();
		for (Iterator<RuleLogicVo> it=list.iterator(); it.hasNext();) {
			RuleLogicVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnyElementsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyElementsMarkedForDeletion() - Entering...");
		List<RuleElementVo> list = getRuleElementList();
		for (Iterator<RuleElementVo> it=list.iterator(); it.hasNext();) {
			RuleElementVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnySubRulesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnySubRulesMarkedForDeletion() - Entering...");
		List<RuleSubRuleMapVo> list = getSubRuleList();
		for (Iterator<RuleSubRuleMapVo> it=list.iterator(); it.hasNext();) {
			RuleSubRuleMapVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnyMsgActionsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMsgActionsMarkedForDeletion() - Entering...");
		List<MsgActionVo> list = getMsgActionList();
		for (Iterator<MsgActionVo> it=list.iterator(); it.hasNext();) {
			MsgActionVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/*
	 * used by ruleLogicEdit.xhtml, where ruleLogic instance has been instantiated.
	 */
	public boolean isHasSubrules() {
		if (isDebugEnabled)
			logger.debug("isHasSubrules() - Entering...");
		if (getRuleLogic() != null) {
			return getRuleLogicDao().getHasSubRules(ruleLogic.getRuleName());
		}
		return false;
	}

	public boolean hasSubrule(String ruleName) {
		if (isDebugEnabled)
			logger.debug("isHasSubrule() - Entering...");
		if (hasSubruleMap.containsKey(ruleName)) {
			return hasSubruleMap.get(ruleName);
		}
		return getRuleLogicDao().getHasSubRules(ruleName);
	}

	/*
	 * used by customizeBuiltinRules.xhtml, inside DataTable loop where RowData is available.
	 */
	public boolean getHasMsgActions() {
		if (isDebugEnabled)
			logger.debug("getHasMsgActions() - Entering...");
		if (ruleLogics != null) {
			RuleLogicVo vo = ruleLogics.getRowData();
			return getMsgActionDao().getHasActions(vo.getRuleName());
		}
		return false;
	}

	public boolean hasMsgActions(String ruleName) {
		if (isDebugEnabled)
			logger.debug("hasMsgActions(\"" + ruleName + "\") - Entering...");
		if (hasActionsMap.containsKey(ruleName)) {
			return hasActionsMap.get(ruleName);
		}
		return getMsgActionDao().getHasActions(ruleName);
	}

	/*
	 * Validation Section
	 */
	
	/**
	 * check primary key
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - ruleName: " + value);
		String ruleName = (String) value;
		if (getRuleLogic() != null) {
			int seq = ruleLogic.getRuleSeq();
			logger.debug("RuleLogic sequence: " + seq);
		}
		else {
			logger.error("validatePrimaryKey(() - RuleLogic is null");
			return;
		}

		RuleLogicVo vo = getRuleLogicDao().getByRuleName(ruleName);
		if (editMode == false && vo != null) {
			// ruleLogic already exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "ruleLogicAlreadyExist", new String[] {ruleName});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}

		if (editMode == true && vo == null) {
			// ruleLogic does not exist
	        FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "ruleLogicDoesNotExist", new String[] {ruleName});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	/**
	 * check regular expression
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateRegex(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateRegex() - regex: " + value);
		String regex = (String) value;
		try {
			Pattern.compile(regex);
		}
		catch (PatternSyntaxException e) {
			FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
					"ltj.msgui.messages", "invalidRegex", new String[] {regex});
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			//context.addMessage(component.getClientId(context), message);
			throw new ValidatorException(message);
		}
	}
	
	/**
	 * check start date
	 * 
	 * @param context
	 * @param component
	 * @param value
	 */
	public void checkStartDate(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("checkStartDate() - startDate = " + value);
		if (value instanceof Date) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime((Date)value);
			return;
		}
		((UIInput)component).setValid(false);
		FacesMessage message = ltj.msgui.util.MessageUtil.getMessage(
				"ltj.msgui.messages", "invalidDate", new Object[] {value});
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		context.addMessage(component.getClientId(context), message);
	}
	
	/*
	 * "testResult" and "actionFailure" are messages generated by action
	 * commands, set them to null so they will no longer be rendered.
	 */
	void reset() {
		testResult = null;
		actionFailure = null;
		ruleNameInput = null;
		startDateInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	protected List<RuleLogicVo> getRuleLogicList() {
		if (ruleLogics == null) {
			return new ArrayList<RuleLogicVo>();
		}
		else {
			return (List<RuleLogicVo>)ruleLogics.getWrappedData();
		}
	}
	
	public RuleLogicVo getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogicVo ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public String getBeanMode() {
		return beanMode == null ? "" : beanMode.name();
	}

	public void setBeanMode(String beanMode) {
		try {
			this.beanMode = BeanMode.valueOf(beanMode);
		}
		catch (Exception e) {}
	}

	public UIInput getRuleNameInput() {
		return ruleNameInput;
	}

	public void setRuleNameInput(UIInput smtpHostInput) {
		this.ruleNameInput = smtpHostInput;
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}

	public DataModel<RuleLogicVo> getRuleLogics() {
		return ruleLogics;
	}

	public UIInput getStartDateInput() {
		return startDateInput;
	}

	public void setStartDateInput(UIInput startDateInput) {
		this.startDateInput = startDateInput;
	}

	public RuleElementVo getRuleElement() {
		return ruleElement;
	}

	public void setRuleElement(RuleElementVo ruleElement) {
		this.ruleElement = ruleElement;
	}
}

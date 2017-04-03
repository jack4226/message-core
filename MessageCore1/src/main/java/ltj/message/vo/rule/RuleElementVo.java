package ltj.message.vo.rule;

import java.io.Serializable;

import ltj.message.vo.BaseVoWithRowId;

public class RuleElementVo extends BaseVoWithRowId implements Serializable {
	
	private static final long serialVersionUID = -1717399300313450033L;
	private String ruleName = "";
	private int elementSeq = -1;
	private String dataName = "";
	private String headerName = null;
	private String criteria = ""; 
	private boolean caseSensitive = false;
	private String targetText = null;
	private String targetProc = null;
	private String exclusions = null;
	private String exclListProc = null;
	private String delimiter = null;
	
	/**
	 * Web UI methods
	 */
	public boolean getIsCaseSensitive() {
		return isCaseSensitive();
	}
	/**
	 * End Web UI
	 */
	
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
	public String getCriteria() {
		return criteria;
	}
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	public String getDataName() {
		return dataName;
	}
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	public String getDelimiter() {
		return delimiter;
	}
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String elementName) {
		this.ruleName = elementName;
	}
	public String getExclListProc() {
		return exclListProc;
	}
	public void setExclListProc(String exclListProc) {
		this.exclListProc = exclListProc;
	}
	public String getExclusions() {
		return exclusions;
	}
	public void setExclusions(String exclusions) {
		this.exclusions = exclusions;
	}
	public String getTargetText() {
		return targetText;
	}
	public void setTargetText(String targetText) {
		this.targetText = targetText;
	}
	public String getTargetProc() {
		return targetProc;
	}
	public void setTargetProc(String targetProc) {
		this.targetProc = targetProc;
	}
	public int getElementSeq() {
		return elementSeq;
	}
	public void setElementSeq(int elementSeq) {
		this.elementSeq = elementSeq;
	}
	public String getHeaderName() {
		return headerName;
	}
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}
}
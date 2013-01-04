package com.legacytojava.message.jpa.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.legacytojava.message.constant.Constants;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = 3239024926806006588L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*
	 * !!!!!!!!!! Very important !!!!!!!!!!
	 * "name" attribute in Column annotation is required for native query
	 * to map query results.
	 */
	@Column(name="RowId") 
	protected int rowId = -1;
	@Column(name="VariableName", nullable=false, length=26)
	private String variableName = "";
	@Column(name="StartTime", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime = new Date(System.currentTimeMillis());
	@Column(name="VariableFormat", length=50)
	private String variableFormat= null;
	@Column(name="VariableType", nullable=false, length=1)
	private String variableType = "";
	// T - text, N - numeric, D - DateField/time,
	// A - address, X - Xheader, L - LOB(Attachment)
	//private String statusId = Constants.ACTIVE;
	// A - Active, I - Inactive
	@Column(name="StatusId", length=1, nullable=false)
	private String statusId = "";
	@Column(name="AllowOverride", length=1, nullable=false)
	private String allowOverride = Constants.YES_CODE;
	// allow override value to be supplied at runtime, Y/N/M, M=Mandatory
	@Column(name="Required", length=1, nullable=false)
	private String required = Constants.NO_CODE;
	@Column(name="VariableValue", length=255)
	private String variableValue = null;
	
	public int getRowId() {
		return rowId;
	}
	public String getAllowOverride() {
		return allowOverride;
	}
	public void setAllowOverride(String allowOverride) {
		this.allowOverride = allowOverride;
	}
	public String getRequired() {
		return required;
	}
	public void setRequired(String required) {
		this.required = required;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public String getVariableFormat() {
		return variableFormat;
	}
	public void setVariableFormat(String variableFormat) {
		this.variableFormat = variableFormat;
	}
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	public String getVariableType() {
		return variableType;
	}
	public void setVariableType(String variableType) {
		this.variableType = variableType;
	}
	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}
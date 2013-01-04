package com.legacytojava.message.jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.legacytojava.message.constant.Constants;

@Entity
@Table(name="GlobalVariable")
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class GlobalVariable implements Serializable
{
	private static final long serialVersionUID = 3239024926806006588L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="RowId") // needed for native query
	protected int rowId = -1;
	@Column(nullable=false, length=26)
	private String variableName = "";
	@Column(nullable=false)
	private Timestamp startTime = new Timestamp(System.currentTimeMillis());
	@Column(length=50)
	private String variableFormat= null;
	@Column(nullable=false, length=1)
	private String variableType = "";
	// T - text, N - numeric, D - DateField/time,
	// A - address, X - Xheader, L - LOB(Attachment)
	//private String statusId = Constants.ACTIVE;
	// A - Active, I - Inactive
	@Column(length=1, nullable=false)
	private String statusId = "";
	@Column(length=1, nullable=false)
	private String allowOverride = Constants.YES_CODE;
	// allow override value to be supplied at runtime, Y/N/M, M=Mandatory
	@Column(length=1, nullable=false)
	private String required = Constants.NO_CODE;
	@Column(length=255)
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
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
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
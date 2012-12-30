package com.legacytojava.message.jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="IdTokens")
public class IdTokens implements java.io.Serializable {
	private static final long serialVersionUID = -632308305179136081L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int rowId = -1;

	@Column(name="ClientId", unique=true, nullable=false, length=16)
	private String clientId = "";
	@Column(nullable=true, length=100)
	private String description = null;
	@Column(nullable=false, length=16)
	private String bodyBeginToken = "";
	@Column(nullable=false, length=4)
	private String bodyEndToken = "";
	@Column(length=20)
	private String xHeaderName = null;
	@Column(length=16)
	private String xhdrBeginToken = null;
	@Column(length=4)
	private String xhdrEndToken = null;
	@Column(length=11)
	private int maxLength = -1;
	private Timestamp updtTime;
	@Column(length=10)
	private String updtUserId;

	public IdTokens() {
		// must have a no-argument constructor
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBodyBeginToken() {
		return bodyBeginToken;
	}

	public void setBodyBeginToken(String bodyBeginToken) {
		this.bodyBeginToken = bodyBeginToken;
	}

	public String getBodyEndToken() {
		return bodyEndToken;
	}

	public void setBodyEndToken(String bodyEndToken) {
		this.bodyEndToken = bodyEndToken;
	}

	public String getxHeaderName() {
		return xHeaderName;
	}

	public void setxHeaderName(String xHeaderName) {
		this.xHeaderName = xHeaderName;
	}

	public String getXhdrBeginToken() {
		return xhdrBeginToken;
	}

	public void setXhdrBeginToken(String xhdrBeginToken) {
		this.xhdrBeginToken = xhdrBeginToken;
	}

	public String getXhdrEndToken() {
		return xhdrEndToken;
	}

	public void setXhdrEndToken(String xhdrEndToken) {
		this.xhdrEndToken = xhdrEndToken;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public Timestamp getUpdtTime() {
		return updtTime;
	}

	public void setUpdtTime(Timestamp updtTime) {
		this.updtTime = updtTime;
	}

	public String getUpdtUserId() {
		return updtUserId;
	}

	public void setUpdtUserId(String updtUserId) {
		this.updtUserId = updtUserId;
	}
}

package com.legacytojava.message.jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="ClientVariable")
public class ClientVariable extends BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = -5873779791693771806L;
	@Column(name="ClientId", unique=true, nullable=false, length=16)
	private String clientId = "";

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}

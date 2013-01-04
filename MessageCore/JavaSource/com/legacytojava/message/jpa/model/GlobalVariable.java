package com.legacytojava.message.jpa.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="GlobalVariable")
public class GlobalVariable extends BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = 7381275253094081485L;
}

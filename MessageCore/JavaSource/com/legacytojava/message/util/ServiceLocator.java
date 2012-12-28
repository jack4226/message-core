/*
 * com/legacytojava/message/util/ServiceLocator.java
 * 
 * Copyright (C) 2008 Jack W.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.legacytojava.message.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

public final class ServiceLocator {

	private ServiceLocator() {
		// static only
	}
	
	/**
	 * Works with JNDI name: "java:/comp/env/jdbc/msgdb_pool" in JBoss
	 * @param dataSourceJndiName
	 * @return a dataSource instance
	 * @throws NamingException
	 */
	public static DataSource getDataSource(String dataSourceJndiName) throws NamingException {
		Context ctx = new InitialContext();
		DataSource dataSource = (DataSource) ctx.lookup(dataSourceJndiName);
		return dataSource;
	}

	/**
	 * Works with following JNDI names in JBoss
	 *		ServiceLocator.listContext("java:/comp/");
	 *		ServiceLocator.listContext("java:/comp/env");
	 *		ServiceLocator.listContext("java:/comp/env/jdbc");
	 * @throws NamingException
	 */
	public static void listContext(String jndiName) throws NamingException {
		System.out.println("===== List Context - JNDI ====> " + jndiName);
		Context initCtx = new InitialContext();
		
		// method 1
		try
		{
			System.out.println("===== List Context - Method 1 =====");
			NamingEnumeration<?> enu = initCtx.list(jndiName);
			while (enu.hasMore()) {
				System.out.print("Binding : ");
				System.out.println(enu.next().toString());
			}
		}
		catch (NamingException e) {
			System.out.println("JNDI lookup failed : " + e);
		}
		
		// method 2
		try {
			System.out.println("===== List Context - Method 2 =====");
			NamingEnumeration<?> enu = initCtx.listBindings(jndiName);
			while (enu.hasMore()) {
				System.out.print("Binding : ");
				System.out.println(enu.next().toString());
				
			}
		}
		catch (NamingException e) {
			System.out.println("JNDI lookup failed : " + e);
		}
		
		// method 3
		try {
			System.out.println("===== List Context - Method 3 =====");
			listContext(initCtx, jndiName);
		}
		catch (NamingException e) {
			System.out.println("JNDI lookup failed : " + e);
		}
	}
	
	/*
	 * list contents of a Context
	 */
	private static void listContext(Context ctx, String name) throws NamingException {
		NamingEnumeration<?> enu = ctx.listBindings(name);
		while (enu.hasMore()) {
			Object obj = enu.next();
			System.out.print("Binding : ");
			System.out.println(obj.toString());
			if (obj instanceof NameClassPair) {
				NameClassPair ncp = (NameClassPair)obj;
				//System.out.println("NameClassPair classname/name: "+ncp.getClassName()+"/"+ncp.getName());
				if (ncp.getClassName().indexOf("Context")>=0)
					listContext(ctx, name+"/"+ncp.getName());
			}
		}
	}
}

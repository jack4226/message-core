/*
 * com/legacytojava/message/util/StringUtil.java
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.legacytojava.message.constant.EmailIDToken;

public final class StringUtil {
	static final Logger logger = Logger.getLogger(StringUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator", "\n");

    private StringUtil() {
        // static only
    }

	public static boolean isEmpty(String str) {
		return StringUtils.isEmpty(str);
	}

	/**
	 * trim the input string from the right to the provided length.
	 * 
	 * @param str -
	 *            original string
	 * @param len -
	 *            string size
	 * @return string with maximum size of "len" plus three dots.
	 */
	public static String cutWithDots(String str, int len) {
		if (str == null || str.length() <= len || len < 0)
			return str;
		else if (str.length() > len)
			return str.substring(0, len) + "...";
		else
			return str;
	}

	/**
	 * remove double and single quotes from input string
	 * 
	 * @param data -
	 *            input string
	 * @return string with quotes removed, or null if input is null
	 */
	public static String removeQuotes(String data) {
		if (data == null) return data;
		StringTokenizer st = new StringTokenizer(data, "\"\'");
		StringBuffer sb = new StringBuffer();
		while (st.hasMoreTokens())
			sb.append(st.nextToken());

		return sb.toString();
	}

	/**
	 * convert Address array to string, addresses are comma delimited. Display
	 * names are removed from returned addresses by default.
	 * 
	 * @param addrs -
	 *            Address array
	 * @return addresses in string format comma delimited, or null if input is
	 *         null
	 */
	public static String emailAddrToString(Address[] addrs) {
		return emailAddrToString(addrs, true);
	}

	/**
	 * convert Address array to string, addresses are comma delimited.
	 * 
	 * @param addrs -
	 *            Address array
	 * @param removeDisplayName -
	 *            remove display name from addresses if true
	 * @return addresses in string format comma delimited, or null if input is
	 *         null
	 */
	public static String emailAddrToString(Address[] addrs, boolean removeDisplayName) {
		if (addrs == null || addrs.length == 0) {
			return null;
		}
		String str = addrs[0].toString();
		if (removeDisplayName) {
			str = removeDisplayName(str);
		}
		for (int i = 1; i < addrs.length; i++) {
			if (removeDisplayName) {
				str = str + "," + removeDisplayName(addrs[i].toString());
			}
			else {
				str = str + "," + addrs[i].toString();
			}
		}
		return str;
	}

	/**
	 * remove display name from an email address, and convert all characters 
	 * to lower case.
	 * 
	 * @param addr -
	 *            email address
	 * @return email address without display name, or null if input is null.
	 */
	public static String removeDisplayName(String addr) {
		return removeDisplayName(addr, true);
	}

	/**
	 * remove display name from an email address.
	 * 
	 * @param addr -
	 *            email address
	 * @param toLowerCase -
	 *            true to convert characters to lower case
	 * @return email address without display name, or null if input is null.
	 */
	public static String removeDisplayName(String addr, boolean toLowerCase) {
		if (StringUtils.isEmpty(addr)) {
			return addr;
		}
		int at_pos = addr.lastIndexOf("@");
		if (at_pos > 0) {
			int pos1 = addr.lastIndexOf("<", at_pos);
			int pos2 = addr.indexOf(">", at_pos + 1);
			if (pos1 >= 0 && pos2 > pos1) {
				addr = addr.substring(pos1 + 1, pos2);
			}
		}
		if (toLowerCase)
			return addr.toLowerCase();
		else
			return addr;
	}

	/**
	 * check if an email address has a display name.
	 * 
	 * @param addr -
	 *            email address
	 * @return true if it has a display name
	 */
	public static boolean hasDisplayName(String addr) {
		if (isEmpty(addr)) return false;
		return addr.matches("^\\s*\\S+.{0,250}\\<.+\\>\\s*$");
	}

	/**
	 * return the display name of an email address.
	 * 
	 * @param addr -
	 *            email address
	 * @return - display name of the address, or null if the email does not have
	 *         a display name.
	 */
	public static String getDisplayName(String addr) {
		if (isEmpty(addr)) {
			return null;
		}
		int at_pos = addr.lastIndexOf("@");
		if (at_pos > 0) {
			int pos1 = addr.lastIndexOf("<", at_pos);
			int pos2 = addr.indexOf(">", at_pos + 1);
			if (pos1 >= 0 && pos2 > pos1) {
				String dispName = addr.substring(0, pos1);
				return dispName.trim();
			}
		}
		return null;
	}

	/**
	 * Compare two email addresses. Email address could be enclosed by angle
	 * brackets and it should still be equal to the one without angle brackets.
	 * 
	 * @param addr1 -
	 *            email address 1
	 * @param addr2 -
	 *            email address 2
	 * @return 0 if addr1 == addr2, -1 if addr1 < addr2, or 1 if addr1 > addr2.
	 */
	public static int compareEmailAddrs(String addr1, String addr2) {
		if (addr1 == null) {
			if (addr2 != null) {
				return -1;
			}
			else {
				return 0;
			}
		}
		else if (addr2 == null) {
			return 1;
		}
		addr1 = removeDisplayName(addr1);
		addr2 = removeDisplayName(addr2);
		return addr1.compareToIgnoreCase(addr2);
	}

	/**
	 * returns the domain name of an email address.
	 * 
	 * @param addr -
	 *            email address
	 * @return domain name of the address, or null if it's local address
	 */
	public static String getEmailDomainName(String addr) {
		if (isEmpty(addr)) {
			return null;
		}
		int pos;
		if ((pos = addr.lastIndexOf("@")) > 0) {
			String domain = addr.substring(pos + 1).trim();
			if (domain.endsWith(">")) {
				domain = domain.substring(0, domain.length() - 1);
			}
			return (domain.length() == 0 ? null : domain);
		}
		return null;
	}

 	/**
	 * Strip off leading and trailing spaces for all String objects in the list
	 * 
	 * @param list -
	 *            a list objects
	 */
	public static void stripAll(ArrayList<Object> list) {
		if (list==null) return;
		for (int i=0; i<list.size(); i++) {
			Object obj = list.get(i);
			if (obj!=null && obj instanceof String)
				list.set(i,((String)obj).trim());
		}
	}

	/**
	 * Strip off leading and trailing spaces for all String objects in an array
	 * 
	 * @param list -
	 *            a list of objects
	 */
	public static void stripAll(Object[] list) {
		if (list==null) return;
		for (int i=0; i<list.length; i++) {
			Object obj = list[i];
			if (obj!=null && obj instanceof String)
				list[i]=((String)obj).trim();
		}
	}

	/**
	 * For String fields defined in the bean class with a getter and a setter,
	 * this method will strip off those fields' leading and trailing spaces.
	 * 
	 * @param obj -
	 *            a bean object
	 */
	public static void stripAll(Object obj) {
		if (obj == null) {
			return;
		}
		Method methods[] = obj.getClass().getDeclaredMethods();
		try {
			Class<?> setParms[] = { Class.forName("java.lang.String") };
			for (int i = 0; i < methods.length; i++) {
				Method method = (Method) methods[i];
				Class<?> parmTypes[] = method.getParameterTypes();
				int mod = method.getModifiers();
				if (Modifier.isPublic(mod) && !Modifier.isAbstract(mod) && !Modifier.isStatic(mod)) {
					if (method.getName().startsWith("get") && parmTypes.length == 0
							&& method.getReturnType().getName().equals("java.lang.String")) {
						// invoke the get method
						String str = (String) method.invoke(obj, (Object[])parmTypes);
						if (str != null) { // trim the string
							String setMethodName = "set" + method.getName().substring(3);
							try {
								Method setMethod = obj.getClass()
										.getMethod(setMethodName, setParms);
								if (setMethod != null) {
									String strParms[] = { str.trim() };
									setMethod.invoke(obj, (Object[])strParms);
								}
							}
							catch (Exception e) {
								// no corresponding set method, ignore.
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			System.err.println("ERROR: Exception caught during reflection - " + e);
			e.printStackTrace();
		}
	}

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	static boolean sortByMethodName = true;
	static Pattern pkgPattern = Pattern.compile("(\\w{1,20}\\.\\w{1,20})\\..*");

	public static String prettyPrint(Object obj) {
		if (obj == null) {
			return ("-null");
		}
		Stack<Object> stack = new Stack<Object>();
		String pkgName = obj.getClass().getPackage().getName();
		Matcher m = pkgPattern.matcher(pkgName);
		if (m.matches() && m.groupCount()>=1) {
			pkgName = m.group(1);
		}
		return prettyPrint(obj, stack, 0, pkgName);
	}

	private static String prettyPrint(Object obj, Stack<Object> stack, int level, String pkgName) {
		if (obj == null) {
		   return ("-null");
		}
		if (level > 10) {
			System.err.println("StringUtil.prettyPrint - has reached nested level of " + level + ", exit.");
			return (obj.getClass().getCanonicalName() + " - (more)...");
		}
		if (level > 0) {
			for (Enumeration<Object> enu=stack.elements(); enu.hasMoreElements(); ) {
				Object sobj = enu.nextElement();
				if (obj.getClass().getCanonicalName().equals(sobj.getClass().getCanonicalName())) {
					return (obj.getClass().getCanonicalName() + " - (loop)...");
				}
			}
		}
		stack.push(obj);
		Object [] params = {};
		StringBuffer sb = new StringBuffer();
		HashMap<String, Object> methodMap = new HashMap<String, Object>();
		List<String> methodNamelist = new ArrayList<String>();
		Method methods[] = obj.getClass().getMethods();
		
		// sort the attributes by name
		for (int i = 0; i< methods.length; i++) {
			methodMap.put(methods[i].getName(), methods[i]);
			methodNamelist.add(methods[i].getName());
		}
		if (sortByMethodName) {
			Collections.sort(methodNamelist);
		}
		
		if (methodNamelist.size()>0) {
			sb.append(LF + " ");
		}
		for (int i = 0; i < methodNamelist.size(); i++) {
			String methodName = (String) methodNamelist.get(i);
			Method method = (Method) methodMap.get(methodName);
			String paramClassName = obj.getClass().getName();
			paramClassName = paramClassName.substring(paramClassName.lastIndexOf(".") + 1);
		
			//System.err.println(method.getReturnType().getName() + " - " + methodName);
			if ((methodName.length() > 3) && ((methodName.startsWith("get")))) {
				try {
					if (method.getParameterTypes().length > 0) {
						continue;
					}
					sb.append(LF + " ");
					sb.append("     " + dots(level) + paramClassName + "." + methodName.substring(3, 4).toLowerCase() + methodName.substring(4));
					sb.append("=");
					if ((method.getReturnType().equals(Class.forName("java.lang.String")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Integer")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Long")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Short")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Float")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Double")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Boolean")))
							|| (method.getReturnType().equals(java.lang.Character.TYPE))
							|| (method.getReturnType().equals(java.lang.Integer.TYPE))
							|| (method.getReturnType().equals(java.lang.Character.TYPE))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
					else if ("int".equals(method.getReturnType().getName())
							|| "short".equals(method.getReturnType().getName())
							|| "long".equals(method.getReturnType().getName())
							|| "double".equals(method.getReturnType().getName())
							|| "boolean".equals(method.getReturnType().getName())) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.Date"))
							|| method.getReturnType().equals(Class.forName("java.sql.Date"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(sdf.format(((java.util.Date) rtnObj)));
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.Calendar"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(sdf.format(((java.util.Calendar)rtnObj).getTime()));
						}						
					}
					else if (method.getReturnType().equals(Class.forName("java.sql.Timestamp"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							java.sql.Timestamp tms = (java.sql.Timestamp) rtnObj;
							sb.append(sdf.format(tms) + ", Nanos: " + tms.getNanos());
						}						
					}
					else if (method.getReturnType().equals(Class.forName("java.math.BigDecimal"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(((java.math.BigDecimal) rtnObj).floatValue());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.lang.Class"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj.getClass().getName().startsWith(pkgName)) {
							sb.append(prettyPrint(rtnObj, stack, level + 1, pkgName));
						}
						else {
							sb.append((((Class<?>) rtnObj)).getName());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.ArrayList"))
							|| method.getReturnType().equals(Class.forName("java.util.List"))) {
						sb.append("a List");
						List<?> lst = (List<?>) method.invoke(obj, params);
						if (lst != null) {
							for (Iterator<?> it = lst.iterator(); it.hasNext();) {
								Object _obj = it.next();
								if (_obj.getClass().getName().startsWith(pkgName)) {
									sb.append(prettyPrint(_obj, stack, level + 1, pkgName));
								}
								else {
									if (_obj instanceof java.lang.String) {
										sb.append(LF + " ");
										sb.append("     " + dots(level+1) + _obj.getClass().getCanonicalName());
										sb.append("=" + _obj.toString());
									}
								}
							}
						}
					}
					else if (method.getName().indexOf("Bean") > 0) {
						// don't try to call the getMapOfBean method - this is
						// used only for printing
						if (method.getName().equals("getMapOfBean")) {
							continue;
						}
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else if (rtnObj.getClass().getName().startsWith(pkgName)) {
							sb.append(prettyPrint(rtnObj, stack, level + 1, pkgName));
						}
					}
					else {
						Class<?> cls = method.getReturnType();
						if (cls.getName().startsWith("[L")) {
							String nm = cls.getComponentType().getCanonicalName();
							if (nm.startsWith(pkgName)) {
								Object[] objs = (Object[])method.invoke(obj, params);
								for (int j=0; objs!=null && j<objs.length; j++) {
									sb.append(prettyPrint(objs[j], stack, level+1, pkgName));
								}
							}
						}
						else {
							String nm = cls.getCanonicalName();
							if (nm.startsWith(pkgName)) {
								sb.append(prettyPrint(method.invoke(obj, params), stack, level + 1, pkgName));
							}
						}
					}
					//sb.append(LF + " ");
				}
				catch (Exception e) {
					//e.printStackTrace();
					System.err.println("error getting values in toString, method name: " + methodName + ", " + e.getMessage());
				}
			}
			else if ((methodName.length() > 2) && ((methodName.startsWith("is")))) {
				try {
					if (method.getParameterTypes().length > 0) {
						continue;
					}
					sb.append(LF + " ");
					sb.append("     " + dots(level) + paramClassName + "." + methodName);
					sb.append("=");
					if (method.getReturnType().equals(Class.forName("java.lang.Boolean"))
							|| method.getReturnType().getName().equals("boolean")) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
				}
				catch (Exception e) {
					//e.printStackTrace();
					System.err.println("error getting values in toString, method name: " + methodName + ", " + e.getMessage());
				}
			}
		}
		stack.pop();
		return sb.toString();
	}

	private static String dots(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<level; i++)
			sb.append(".");
		return sb.toString();
	}

	/**
	 * replace all occurrences of replFrom with replWith in the body string
	 * 
	 * @param body -
	 *            message text
	 * @param replFrom -
	 *            from string
	 * @param replWith -
	 *            with string
	 * @return new string
	 */
	public static String replaceAll(String body, String replFrom, String replWith) {
		return StringUtils.replace(body, replFrom, replWith);
		/*
		if (body == null || body.trim().length() == 0) {
			return body;
		}
		if (replFrom == null || replWith == null) {
			logger.warn("replaceAll() - either replFrom or replyWith is null.");
			return body;
		}
		StringBuffer sb = new StringBuffer();
		int newpos = 0, pos = 0;
		while ((newpos = body.indexOf(replFrom, pos)) >= 0) {
			sb.append(body.substring(pos, newpos));
			sb.append(replWith);
			pos = newpos + Math.max(1, replFrom.length());
		}
		sb.append(body.substring(pos, body.length()));
		return sb.toString();
		*/
	}

	/**
	 * Add PRE tags for plain text message so the spaces and line breaks are
	 * preserved in web browser.
	 * 
	 * @param msgBody -
	 *            message text
	 * @return new message text
	 */
	public static String getHtmlDisplayText(String text) {
		if (text == null) return null;
		if (text.startsWith("<pre>") && text.endsWith("</pre>")) {
			return text;
		}
		String str = StringUtil.replaceAll(text, "<", "&lt;");
		return "<pre>" + StringUtil.replaceAll(str, ">", "&gt;") + "</pre>";
	}

	/**
	 * remove the first occurrence of the given string from body text.
	 * 
	 * @param body -
	 *            original body
	 * @param removeStr -
	 *            string to be removed
	 * @return new body
	 */
	public static String removeStringFirst(String body, String removeStr) {
		return removeString(body, removeStr, false);
	}

	/**
	 * remove the last occurrence of the given string from body text.
	 * 
	 * @param body -
	 *            original body
	 * @param removeStr -
	 *            string to be removed
	 * @return new body
	 */
	public static String removeStringLast(String body, String removeStr) {
		return removeString(body, removeStr, true);
	}

	private static String removeString(String body, String removeStr, boolean removeLast) {
		if (StringUtils.isEmpty(body) || StringUtils.isEmpty(removeStr)) {
			return body;
		}
		int pos = -1;
		if (removeLast) {
			pos = body.lastIndexOf(removeStr);
		}
		else {
			pos = body.indexOf(removeStr);
		}
		if (pos >= 0) {
			body = body.substring(0, pos) + body.substring(pos + removeStr.length());
		}
		return body;
	}

	/**
	 * returns a string of dots with given number of dots.
	 * 
	 * @param level -
	 *            specify number dots to be returned
	 * @return string of dots
	 */
	public static String getDots(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++) {
			sb.append(".");
		}
		return sb.toString();
	}

	/**
	 * trim white spaces from the RIGHT side of a string.
	 * 
	 * @param text
	 *            to be trimmed
	 * @return trimmed string
	 */
	public static String trimRight(String text) {
		/*
		 * We could also do this: ("A" + text).trim().substring(1)
		 * but the performance is poor.
		 */
		if (StringUtils.isEmpty(text)) return text; 
		int idx = text.length() - 1;
		while (idx >= 0 && Character.isWhitespace(text.charAt(idx))) {
			idx--;
		}
		if (idx < 0) {
			return "";
		}
		else {
			return text.substring(0, idx + 1);
		}
	}

	public static String removeCRLFTabs(String str) {
		// remove possible CR/LF and tabs, that are inserted by some Email
		// servers, from the Email_ID found in bounced E-mails (MS exchange
		// server for one). MS exchange server inserted "\r\n\t" into the
		// Email_ID string, and it caused "check digit test" error.
		StringTokenizer sTokens = new StringTokenizer(str, "\r\n\t");
		StringBuffer sb = new StringBuffer();
		while (sTokens.hasMoreTokens()) {
			sb.append(sTokens.nextToken());
		}
		return sb.toString();
	}

    /**
	 * Trim the given string with the given trim value.
	 * 
	 * @param string
	 *            The string to be trimmed.
	 * @param trim
	 *            The value to trim the given string off.
	 * @return The trimmed string.
	 */
    public static String trim(String string, String trim) {
        if (StringUtils.isEmpty(trim)) {
            return string;
        }
        if (string == null) {
        	return null;
        }
        int start = 0;
        int end = string.length();
        int length = trim.length();
        while ((start + length) <= end && string.substring(start, start + length).equals(trim)) {
			start += length;
		}
		while ((start + length) <= end && string.substring(end - length, end).equals(trim)) {
			end -= length;
		}
        return string.substring(start, end);
    }

    private final static String localPart = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*";
	private final static String remotePart = "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])+";
	private final static String intraPart = "@[a-z0-9](?:[a-z0-9-]*[a-z0-9])+";
	
    private final static Pattern remotePattern = Pattern.compile("^" + localPart + remotePart + "$",
			Pattern.CASE_INSENSITIVE);
    private final static Pattern intraPattern = Pattern.compile("^" + localPart + intraPart + "$",
			Pattern.CASE_INSENSITIVE);
 	private static final Pattern localPattern = Pattern.compile("^" + localPart + "$",
			Pattern.CASE_INSENSITIVE);
 	
 	public static String getEmailRegex() {
 		return localPart + remotePart;
 	}
 
    /**
	 * Check if the provided string is a valid email address. This conforms to
	 * the RFC822 and RFC1035 specifications. Both local part and remote part
	 * are required.
	 * 
	 * @param string
	 *            The string to be checked.
	 * @return True if string is an valid email address. False if not.
	 */
    public static boolean isInternetEmailAddress(String string) {
    	if (string == null) return false;
    	Matcher matcher = remotePattern.matcher(string);
    	return matcher.matches();
        //return string.matches(
        //    "(?i)^[a-z0-9-~#&\\_]+(?:\\.[a-z0-9-~#&\\_]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,5}$");
    }

   /**
	 * Check if the provided string is a valid remote or Intranet email address.
	 * An Intranet email address could include only a sub-domain name such as
	 * "bounce" or "localhost" as its remote part.
	 * 
	 * @param string
	 *            The string to be checked.
	 * @return True if string is an valid email address. False if not.
	 */
    public static boolean isRemoteEmailAddress(String string) {
    	if (string == null) return false;
    	if (isInternetEmailAddress(string)) return true;
    	Matcher matcher = intraPattern.matcher(string);
    	return matcher.matches();
        //return string.matches(
        //    "(?i)^[a-z0-9-~#&\\_]+(?:\\.[a-z0-9-~#&\\_]+)*@(?:[a-z0-9-]+)$");
    }

	/**
	 * matches any remote or local email addresses like john or john@localhost
	 * or john@smith.com.
	 * 
	 * @param string
	 *            the email address to be checked
	 * @return true if it's a valid email address
	 */
	public static boolean isRemoteOrLocalEmailAddress(String string) {
		if (string == null) return false;
		if (isRemoteEmailAddress(string)) return true;
		Matcher matcher = localPattern.matcher(string);
		return matcher.matches();
	}

    public static boolean isValidEmailLocalPart(String string) {
    	Matcher matcher = localPattern.matcher(string);
    	return matcher.matches();
    }

	
	private static String bounceRegex = (new StringBuilder("\\s*\\W?((\\w+)\\-(")).append(
			EmailIDToken.XHDR_BEGIN).append("\\d+").append(EmailIDToken.XHDR_END).append(
			")\\-(.+\\=.+)\\@(.+\\w))\\W?\\s*").toString();
		// for ex.: bounce-10.07410251.0-jsmith=test.com@localhost
	private static Pattern bouncePattern = Pattern.compile(bounceRegex);
	private static String removeRegex = "\\s*\\W?((\\w+)\\-(\\w+)\\-(.+\\=.+)\\@(.+\\w))\\W?\\s*";
		// for ex.: remove-testlist-jsmith=test.com@localhost
	private static Pattern removePattern = Pattern.compile(removeRegex);
	
	public static boolean isVERPAddress(String recipient) {
		if (isEmpty(recipient)) {
			return false;
		}
		Matcher bounceMatcher = bouncePattern.matcher(recipient);
		Matcher removeMatcher = removePattern.matcher(recipient);
		return bounceMatcher.matches() || removeMatcher.matches();
	}
	
	public static String getDestAddrFromVERP(String verpAddr) {
		Matcher bounceMatcher = bouncePattern.matcher(verpAddr);
		if (bounceMatcher.matches()) {
			if (bounceMatcher.groupCount() >= 5) {
				String destAddr = bounceMatcher.group(2) + "@" + bounceMatcher.group(5);
				return destAddr;
			}
		}
		Matcher removeMatcher = removePattern.matcher(verpAddr);
		if (removeMatcher.matches()) {
			if (removeMatcher.groupCount() >= 5) {
				String destAddr = removeMatcher.group(2) + "@" + removeMatcher.group(5);
				return destAddr;
			}
		}
		return verpAddr;
	}
	
	public static String getOrigAddrFromVERP(String verpAddr) {
		Matcher bounceMatcher = bouncePattern.matcher(verpAddr);
		if (bounceMatcher.matches()) {
			if (bounceMatcher.groupCount() >= 4) {
				String origAddr = bounceMatcher.group(4).replace('=', '@');
				return origAddr;
			}
		}
		Matcher removeMatcher = removePattern.matcher(verpAddr);
		if (removeMatcher.matches()) {
			if (removeMatcher.groupCount() >= 4) {
				String origAddr = removeMatcher.group(4).replace('=', '@');
				return origAddr;
			}
		}
		return verpAddr;
	}
	
    private static final String ssnRegex = "^(?!000)(?:[0-6]\\d{2}|7(?:[0-6]\\d|7[0-2]))([ -]?)(?!00)\\d{2}\\1(?!0000)\\d{4}$";
    private final static Pattern ssnPattern = Pattern.compile(ssnRegex);
    
    /*
     * Valid SSN: 	078-05-1120 | 078 05 1120 | 078051120
     * Invalid SSN: 987-65-4320 | 000-00-0000 | (555) 555-5555
     */
    public static boolean isValidSSN(String ssn) {
    	if (ssn == null) return false;
    	Matcher matcher = ssnPattern.matcher(ssn);
    	return matcher.matches();
    }
    
    private static final String phoneRegex = "^(?:1[ -]?)?(?:\\(\\d{3}\\)|\\d{3})[ -]?(?:\\d{3}|[a-z]{3})[ -]?(?:\\d{4}|[a-z]{4})$";
    private final static Pattern phonePattern = Pattern.compile(phoneRegex, Pattern.CASE_INSENSITIVE);
    
    /*
     * Matches: 2405525009 | (240)552-5009 | 1(240) 552-5009 | 240 JOE-CELL
     */
    public static boolean isValidPhoneNumber(String phone) {
    	if (phone == null) return false;
    	Matcher matcher = phonePattern.matcher(phone);
    	return matcher.matches();
    }

	public static void main(String[] args) {
		String addr = "\"ORCPT jwang@nc.rr.com\" <jwang@nc.rr.com>";
		addr = "DirectStarTV <fqusoogd.undlwfeteot@chaffingphotosensitive.com>";
		System.out.println(addr+" --> "+removeDisplayName(addr));
		
		System.out.println(removeStringFirst("<pre>12345abcdefklqhdkh</pre>", "<pre>"));
		System.out.println("EmailAddr: " + isRemoteEmailAddress("A!#$%&'*+/=?.^_`{|}~-BC@localhost.us"));
		System.out.println("EmailAddr: " + isRemoteOrLocalEmailAddress("A!#$%&'*+/=?.^_`{|}~-BC"));
		System.out.println(getOrigAddrFromVERP("bounce-10.07410251.0-jsmith=test.com@localhost"));
		System.out.println(getOrigAddrFromVERP("remove-testlist-jsmith=test.com@localhost"));
	}
}

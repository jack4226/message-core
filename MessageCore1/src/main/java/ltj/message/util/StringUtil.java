/*
 * ltj/message/util/StringUtil.java
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
package ltj.message.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


public final class StringUtil {
	static final Logger logger = Logger.getLogger(StringUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	public static final String LF = System.getProperty("line.separator", "\n");

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

	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	final static int MAX_LEVELS = 12;

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

    public static String getRandomWord(String paragraph) {
    	String[] words = (paragraph + "").split("[ ]+");
    	Random r = new Random();
    	int idx = r.nextInt(words.length);
    	String word = words[idx];
    	int count = 0;
    	while (StringUtils.trim(word).length() <= 3 && count++ < words.length) {
    		idx = r.nextInt(words.length);
    		word = words[idx];
    	}
    	count = 0;
    	while (StringUtils.trim(word).length() <= 2 && count++ < words.length) {
    		idx = r.nextInt(words.length);
    		word = words[idx];
    	}
    	return word;
    }
 
    public static List<String> getRandomWords(String paragraph) {
    	String[] words = (paragraph + "").split("[\\s]+");
    	List<String> list = new ArrayList<>();
    	if (words.length > 0 && words.length <= 5) {
    		int idx = new Random().nextInt(words.length);
    		list.add(words[idx]);
    	}
    	else if (words.length < 20) {
    		int idx = new Random().nextInt(words.length - 4);
    		list.add(words[idx]);
    		list.add(words[idx + 1]);
    	}
    	else {
    		int idx = new Random().nextInt(words.length - 10);
    		list.add(words[idx]);
    		list.add(words[idx + 1]);
    		list.add(words[idx + 2]);
    	}
    	return list;
    }

	public static void main(String[] args) {
		System.out.println(removeStringFirst("<pre>12345abcdefklqhdkh</pre>", "<pre>"));
	}
}

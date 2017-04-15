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
package ltj.selenium;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public final class StringUtil {
	static final Logger logger = Logger.getLogger(StringUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	public static final String LF = System.getProperty("line.separator", "\n");

    private StringUtil() {
        // static only
    }

    public static String getRandomWord(String paragraph) {
    	String[] words = (paragraph + "").split("[ ]+");
    	Random r = new Random();
    	int idx = r.nextInt(words.length);
    	String word = words[idx];
    	int count = 0;
    	while (word.matches(".*[\\p{Punct}].*") && count++ < words.length) {
    		idx = r.nextInt(words.length);
    		word = words[idx];
    	}
    	return word;
    }
 
    public static List<String> getRandomWords(String paragraph) {
    	String[] words = (paragraph + "").split("[\\s]+");
    	List<String> list = null;
    	if (words.length > 0 && words.length <= 5) {
    		list = new ArrayList<>();
    		list.add(getRandomWord(paragraph));
    	}
    	else if (words.length < 20) {
    		int idx = new Random().nextInt(words.length - 4);
    		list = getWords(idx, 2, words);
    	}
    	else {
    		int idx = new Random().nextInt(words.length - 10);
    		list = getWords(idx, 3, words);
    	}
    	return list;
    }

    private static List<String> getWords(int start, int nbrOfWords, String[] words) {
    	List<String> list = new ArrayList<>();
    	for (int i = 0; i < nbrOfWords; i++) {
        	String word = words[start];
    		while (++start < words.length && word.matches(".*[\\p{Punct}].*")) {
        		word = words[start];
        	}
    		list.add(word);
    		if (start >= words.length) {
    			break;
    		}
    	}
    	return list;
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
		if (StringUtils.isEmpty(addr)) {
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

	public static List<Integer> getRandomElements(int sizeOfList) {
		Set<Integer> set = new HashSet<>();
		Random r = new Random();
		for (int i = 0; i < sizeOfList && i < 7; i++) {
			set.add(r.nextInt(sizeOfList));
		}
		return new ArrayList<Integer>(set);
	}
	
	
	public static void main(String[] args) {
		for (int i = 0; i < 50; i++) {
			System.out.println(getRandomWords("This is my ${best} worst test $rest message."));
		}
	}
}

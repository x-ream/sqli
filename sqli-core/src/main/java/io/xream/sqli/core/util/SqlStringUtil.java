/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.sqli.core.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SqlStringUtil {

	private SqlStringUtil(){}

	public static String toUTF8(String strISO88591){
		try {
			return new String(strISO88591.getBytes("ISO8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return strISO88591;
	}
	
	public static boolean isNotNull(String str){
		return !isNullOrEmpty(str);
	}
	
	public static boolean isNullOrEmpty(String str){
		return str == null || str.equals("") || str.equals("null") || str.equals("NaN") || str.equals("undefined");
	}

	public static boolean isNullOrEmpty(Object obj) {

		if (obj == null)
			return true;
		Class<?> clz = obj.getClass();
		if (clz == String.class) {
			return isNullOrEmpty(obj.toString());
		}
		return false;
	}
	
	public static boolean isMobile(String mobile){
		String pMobile = "^(1(([34578][0-9])))\\d{8}$";
		return Pattern.matches(pMobile, mobile);
	}
	
	public static boolean isNumeric(String str){
		int length = str.length();
		 for(int i=0;i<length;i++){
		      if (!Character.isDigit(str.charAt(i))){
		    	  return false;
		      }
		 }
		 return true;
	}
	
	public static boolean isEmail(String email){

		if((email.indexOf("@") == -1)){
			return false;
		}
		String pEmail = "^[\\w-]{1,40}(\\.[\\w-]{1,20}){0,6}@[\\w-]{1,40}(\\.[\\w-]{1,20}){1,6}$";
		return Pattern.matches(pEmail, email);

	}
	
	public static String nullToEmpty(String str){
		if (isNullOrEmpty(str))
			return "";
		return str;
	}

	/**
	 *
	 * @param str  like:   \\$\\{[\\w]*\\}      \{[\\w]*\\}
	 * @param pattern
	 */
	public static List<String> listByRegEx(String str, Pattern pattern){//"$\\{[\\w]*\\}"

		Matcher matcher = pattern.matcher(str);

		List<String> list = new ArrayList<>();
		while(matcher.find()){
			CharSequence subSequence = str.subSequence(matcher.start(0), matcher.end(0));
			list.add(subSequence.toString());
		}

		return list;
	}
}

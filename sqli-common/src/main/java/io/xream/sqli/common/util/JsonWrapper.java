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
package io.xream.sqli.common.util;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;
import java.util.*;

/**
 * 
 * 
 * @author Sim
 *
 */
public class JsonWrapper {

	private JsonWrapper(){}

	public static String toJson(List list){
		if (list == null)
			return null;
		if (list.isEmpty()){
			return "[]";
		}
		return JSON.toJSONString(list);
	}
	
	public static String toJson(Map map){
		if (map == null)
			return null;
		if (map.isEmpty()){
			return "{}";
		}
		return JSON.toJSONString(map);
	}
	
	public static <T> List<T> toList(String json, Class<T> clz){
		if (json == null || json.equals(""))
			return new ArrayList<T>();
		return JSON.parseArray(json, clz);
	}
	
	public static Map toMap(String json){
		if (json == null || json.equals(""))
			return new HashMap();
		return (Map) JSON.parse(json);
	}
	
	public static String toJson(Object obj){
		if (obj == null)
			return null;
		if (obj instanceof String)
			return obj.toString();
		return JSON.toJSONString(obj);
	}
	
	public static <T> T toObject(String json, Class<T> clz){
		if (json == null || json.equals(""))
			return null;
		if (clz == String.class)
			return (T)json;
		return JSON.parseObject(json, clz);
	}
	
	public static <T> T toObject(Object jsonObject, Class<T> clz){
		if (Objects.isNull(jsonObject))
			return null;

		return JSON.toJavaObject((JSON)jsonObject, clz);
	}
	
	public static Map<String,Object> toMap(Object obj){
		return (Map<String,Object>) JSON.toJSON(obj);
	}
	
	public static boolean isJsonable(Class clz) {
		if ( clz == String.class
						|| clz == long.class || clz == Long.class 
						|| clz == int.class || clz == Integer.class
						|| clz == boolean.class || clz == Boolean.class
						|| clz == double.class || clz == Double.class
						|| clz == float.class || clz == Float.class
						|| clz == short.class || clz == Short.class
						|| clz == byte.class || clz == Byte.class
						|| clz == BigDecimal.class || clz == Date.class
						)
			return false;
		return true;
	}
	
	public static Object toObjectByClassName(String unknown, String clzName){
		if (SqlStringUtil.isNullOrEmpty(unknown))
			return null;
		if (SqlStringUtil.isNullOrEmpty(clzName))
			return  unknown;

		if (clzName.contains("java.util.List")){
			int start = clzName.indexOf("<")+1;
			int end = clzName.indexOf(">");
			String actualTypeStr = clzName.substring(start,end);
			try {
				Class actualType = Class.forName(actualTypeStr);
				return  toList(unknown,actualType);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		if (clzName.contains("java.util.Map")){
			return  toMap(unknown);
		}

		Class clz = null;
		try {
			clz = Class.forName(clzName);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		if (clz == Long.class)
			return  Long.valueOf(unknown);

		if (clz == Integer.class)
			return  Integer.valueOf(unknown);

		if (clz == BigDecimal.class)
			return  new BigDecimal(unknown);

		if (clz == Double.class)
			return  Double.valueOf(unknown);

		if (clz == Float.class)
			return  Integer.valueOf(unknown);

		if (clz == Short.class)
			return  Short.valueOf(unknown);

		if (clz == Byte.class)
			return  Byte.valueOf(unknown);

		if (clz == String.class)
			return  String.valueOf(unknown);

		if (clz == Date.class)
			return  new Date(Long.valueOf(unknown));
		if (clz == java.sql.Timestamp.class)
			return  new java.sql.Timestamp(Long.valueOf(unknown));

		return toObject(unknown,clz);
	}

}

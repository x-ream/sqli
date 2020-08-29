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
package io.xream.sqli.util;

import com.alibaba.fastjson.JSON;

import java.util.*;

/**
 * 
 * 
 * @author Sim
 *
 */
public class JsonWrapper {

	private JsonWrapper(){}
	
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

}

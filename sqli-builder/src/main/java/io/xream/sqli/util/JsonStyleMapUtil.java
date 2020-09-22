/*
 * Copyright 2020 io.xream.sqli
 *
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

import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * Bean <--> Map
 * @author Sim
 *
 */
public final class JsonStyleMapUtil {

	private JsonStyleMapUtil(){}

	public static Map<String,Object> toJsonableMap(Map<String,Object> stringKeyMap){
		Map<String,Object> jsonableMap = new HashMap<String,Object>();
		for (Entry<String, Object> es : stringKeyMap.entrySet()){
			String stringKey = es.getKey();
			if (stringKey.contains(".")){
				String[] arr = stringKey.split("\\.");
				String jsonKey = arr[0];
				String propKey = arr[1];
				Object obj = jsonableMap.get(jsonKey);
				Map<String,Object> objMap = null;
				if (Objects.isNull(obj)){
					objMap = new HashMap<>();
					jsonableMap.put(jsonKey, objMap);
				}else {
					objMap = (Map<String,Object>) obj;
				}
				objMap.put(propKey, es.getValue());
			}else{
				jsonableMap.put(stringKey, es.getValue());
			}
		}
		return jsonableMap;
	}
	
	public static List<Map<String,Object>> toJsonableMapList (List<Map<String,Object>> stringKeyList){
		
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		
		for (Map<String,Object> map : stringKeyList){
			Map<String,Object> jsonKeyMap = toJsonableMap(map);
			list.add(jsonKeyMap);
		}
		
		return list;
	}

}

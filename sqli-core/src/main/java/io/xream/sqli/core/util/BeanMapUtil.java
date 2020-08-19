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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * Bean <--> Map
 * @author Sim
 *
 */
public class BeanMapUtil {

	private BeanMapUtil(){}
	
	public static <T> T toObject(Class<T> clz, Map<String,Object> map) {

		if (clz == Map.class){
			return (T) map;
		}
		
		List<Field> fl = new ArrayList<Field>();

		if (clz.getSuperclass() != Object.class) {
			fl.addAll(Arrays.asList(clz.getSuperclass().getDeclaredFields()));
		}
		fl.addAll(Arrays.asList(clz.getDeclaredFields()));

		T obj = null;
		try {
			obj = clz.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {

			for (Field f : fl) {

				int modifiers = f.getModifiers();
				String key = f.getName();
				
				if (Modifier.isStatic(modifiers))
					continue;

				if (Modifier.isFinal(modifiers))
					continue;

				f.setAccessible(true);
				Object v = map.get(key);
				if (v != null){
					String name = f.getType().getName().toLowerCase();
					Class fc = f.getType();
					
					if (name.contains("double")){//MYSQL 方言
						try{
							f.set(obj, v);
						}catch(Exception e){
							f.set(obj, Double.valueOf(v.toString()));
						}
					}else if (name.contains("long") || name.contains("Long") ){
						if (v instanceof String){
							Long vL = Long.valueOf((String)v);
							long vl = vL;
							f.set(obj, vl);
						}else{
							f.set(obj, v);
						}
					}else if (name.contains("int") ){
						if (v instanceof String){
							Integer vL = Integer.valueOf((String)v);
							int vl = vL;
							f.set(obj, vl);
						}else{
							f.set(obj, v);
						}
					}else if (v instanceof Map){
						f.set(obj, toObject(fc, (Map) v));
					}else{
						if (v instanceof Long && f.getType() == Date.class){
							f.set(obj, new Date((Long)v));
						}else{
							f.set(obj, v);
						}
					}
				}
			
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}

	public static Map<String, String> toStringMap(Object obj) {

		List<Field> fl = new ArrayList<Field>();

		@SuppressWarnings("rawtypes")
		Class clz = obj.getClass();

		if (clz.getSuperclass() != Object.class) {
			fl.addAll(Arrays.asList(clz.getSuperclass().getDeclaredFields()));
		}
		fl.addAll(Arrays.asList(clz.getDeclaredFields()));

		Map<String, String> map = new HashMap<String, String>();

		try {

			for (Field f : fl) {

				int modifiers = f.getModifiers();
				String key = f.getName();
				
				if (Modifier.isStatic(modifiers))
					continue;

				if (Modifier.isFinal(modifiers))
					continue;

				f.setAccessible(true);
				Object value = f.get(obj);
				
				if (value == null){
					map.put(key, null);
				}else{
					map.put(key, value.toString());
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}
	
	public static Map<String, Object> toObjectMap(Object obj) {

		List<Field> fl = new ArrayList<Field>();

		@SuppressWarnings("rawtypes")
		Class clz = obj.getClass();

		if (clz.getSuperclass() != Object.class) {
			fl.addAll(Arrays.asList(clz.getSuperclass().getDeclaredFields()));
		}
		fl.addAll(Arrays.asList(clz.getDeclaredFields()));

		Map<String, Object> map = new HashMap<String, Object>();

		try {

			for (Field f : fl) {

				int modifiers = f.getModifiers();
				String key = f.getName();

				
				if (Modifier.isStatic(modifiers))
					continue;

				if (Modifier.isFinal(modifiers))
					continue;


				f.setAccessible(true);
				Object value = f.get(obj);
				
				map.put(key, value);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}
	
	public static Map<String,Object> toJsonableMap(Map<String,Object> stringKeyMap){
		Map<String,Object> jsonableMap = new HashMap<String,Object>();
		for (Entry<String, Object> es : stringKeyMap.entrySet()){
			String stringKey = es.getKey();
			if (stringKey.contains(".")){
				stringKey = stringKey.replace(".", "->");
				String[] arr = stringKey.split("->");
				String jsonKey = arr[0];
				String propKey = arr[1];
				Object obj = jsonableMap.get(jsonKey);
				Map<String,Object> objMap = null;
				if (Objects.isNull(obj)){
					objMap = new HashMap<String,Object>();
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
	
	public static List<String> toStringKeyList(Map<String,Object> jonsableMap){
		List<String> list = new ArrayList<String>();
		
		for (Entry<String,Object> entry : jonsableMap.entrySet()){
			String key = entry.getKey();
			Object obj = entry.getValue();
			if (!Objects.isNull(obj)){
				Map<String,Object> map = (Map<String,Object>)obj;
				for (Entry<String,Object> es : map.entrySet()){
					String str = key + "." + es.getKey();
					list.add(str);
				}
			}
		}
		
		return list;
	}


}

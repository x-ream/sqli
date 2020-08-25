
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
package io.xream.sqli.parser;

import io.xream.sqli.util.BeanUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Sim
 */
public class ReflectionCache {

	private Class clz;
	private Map<String, FieldAndMethod> map = new HashMap<String, FieldAndMethod> ();
	
	private Map<String, FieldAndMethod> tempMap = new HashMap<String, FieldAndMethod> ();
	
	public Class getClz() {
		return clz;
	}
	public void setClz(Class clz) {
		this.clz = clz;
	}
	public Map<String, FieldAndMethod> getMap() {
		return map;
	}
	public void setMap(Map<String, FieldAndMethod> map) {
		this.map = map;
	}
	
	public FieldAndMethod get(String property){
		return map.get(property);
	}
	public FieldAndMethod getTemp(String property){
		return tempMap.get(property);
	}
	public Map<String, FieldAndMethod> getTempMap() {
		return tempMap;
	}
	public void setTempMap(Map<String, FieldAndMethod> tempMap) {
		this.tempMap = tempMap;
	}
	
	public void cache() {
		if (clz == null)
			return;
		if (map.isEmpty()){
			Field[] fArr = clz.getDeclaredFields();
			for (Field f : fArr) {
				if (f.getModifiers() > 2)
					continue;
				String property = f.getName();
				Class type = f.getType();
				
				String getterName = BeanUtil.getGetter(type, property);
				String setterName = BeanUtil.getSetter(type, property);
				
				try{
					Method getter = clz.getDeclaredMethod(getterName);
					Class rt = f.getType();
					Method setter = clz.getDeclaredMethod(setterName, rt);
					
					if (getter == null || setter == null)
						continue;
					f.setAccessible(true);
					FieldAndMethod fnm = new FieldAndMethod();
					fnm.setProperty(property);
					fnm.setSetterName(setterName);
					fnm.setGetterName(getterName);
					fnm.setField(f);
					fnm.setGetter(getter);
					fnm.setSetter(setter);
					map.put(property, fnm);
				}catch (Exception e){
					
				}
			}
		}
		
	}
	
	@Override
	public String toString() {
		return "ReflectionCache [clz=" + clz + ", map=" + map + "]";
	}
	
}

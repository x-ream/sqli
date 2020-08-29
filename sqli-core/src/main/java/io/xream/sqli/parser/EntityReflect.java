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
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * JDK反射加了缓存之后，method.invoke比setter/getter慢10倍, 比没缓存反射快30倍，比缓存后的ReflectASM快2倍
 * @Author Sim
 */
public class EntityReflect {

	private Class clz;
	private Map<String, FieldAndMethod> map = new HashMap<String, FieldAndMethod> ();
	
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
					Method setter = clz.getDeclaredMethod(setterName, type);
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
					fnm.setClzz(type);
					fnm.setGeneType(getReturnGenericTypeForList(f));
					map.put(property, fnm);
				}catch (Exception e){
					
				}
			}
		}
		
	}

	private Class getReturnGenericTypeForList(Field field){
		ParameterizedType pt = (ParameterizedType) field.getGenericType();
		return (Class) pt.getActualTypeArguments()[0];
	}

	@Override
	public String toString() {
		return "ReflectionCache [clz=" + clz + ", map=" + map + "]";
	}
	
}

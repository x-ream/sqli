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

import io.xream.sqli.annotation.X;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @Author Sim
 */
public final class Parsed {


	private boolean isChecked = false;

	private Class clzz;
	private String tableName;
	private String originTable;
	private boolean isNoSpec = true;

	private final Map<Integer,String> keyMap = new HashMap<Integer,String>();
	private final Map<Integer,Field> keyFieldMap = new HashMap<Integer,Field>();
	
	private List<BeanElement> beanElementList;
	
	private Map<String, BeanElement> elementMap = new HashMap<String, BeanElement>();
	private Map<String,String> propertyMapperMap = new HashMap<String,String>();
	private Map<String,String> mapperPropertyMapLower = new HashMap<String,String>();
	
	private boolean isNoCache;

	public Class getClzz() {
		return clzz;
	}

	public void setClzz(Class clzz) {
		this.clzz = clzz;
	}
	
	public Parsed(Class clzz){
		this.clzz = clzz;
	}

	public String getId(){
		return String.valueOf(keyMap.get(X.KEY_ONE));
	}
	
	public BeanElement getElement(String property){
		return elementMap.get(property);
	}

	public BeanElement getElementExisted(String property) {

		BeanElement be = elementMap.get(property);
		if (be == null)
			throw new ParsingException(
					"Not exist: "
							+ property);
		return be;
	}

	public Map<String, BeanElement> getElementMap() {
		return elementMap;
	}

	public Map<Integer, String> getKeyMap() {
		return keyMap;
	}
	
	public boolean contains(String property) {
		return this.elementMap.containsKey(property);
	}

	public Map<Integer, Field> getKeyFieldMap() {
		return keyFieldMap;
	}
	
	public Field getKeyField(int index){
		Field field = keyFieldMap.get(index);
		if (Objects.isNull(field))
			throw new ParsingException("No setting of PrimaryKey by @X.Key");
		return field;
	}

	public boolean isAutoIncreaseId(Long keyOneValue){
		Field keyOneField = getKeyField(X.KEY_ONE);
		Class keyOneType = keyOneField.getType();

		return keyOneType != String.class && (keyOneValue == null || keyOneValue == 0);
	}

	public String getKey(int index){
		String key = keyMap.get(index);
		if (Objects.isNull(key))
			throw new ParsingException("No setting of PrimaryKey by @X.Key");
		return key;
	}

	public Long tryToGetLongKey(Object obj){
		Long keyOneValue = 0L;
		Field keyOneField = getKeyField(X.KEY_ONE);
		if (Objects.isNull(keyOneField))
			throw new ParsingException("No setting of PrimaryKey by @X.Key");
		Class keyOneType = keyOneField.getType();
		if (keyOneType != String.class) {
			try {
				Object keyValue = keyOneField.get(obj);
				if (keyValue != null) {
					keyOneValue = Long.valueOf(keyValue.toString());
				}
			}catch (Exception e){
				throw new ParsingException(e.getMessage());
			}
		}
		return keyOneValue;
	}

	public List<BeanElement> getBeanElementList() {
		return beanElementList;
	}

	public void setOriginTable(String originTable) {
		this.originTable = originTable;
	}

	public void reset(List<BeanElement> beanElementList){
		this.beanElementList = beanElementList;
		this.propertyMapperMap.clear();
		this.elementMap.clear();
		this.mapperPropertyMapLower.clear();
		for (BeanElement e : this.beanElementList){
			String property = e.getProperty();
			String mapper = e.getMapper();
			this.elementMap.put(property, e);
			this.propertyMapperMap.put(property, mapper);
			this.mapperPropertyMapLower.put(mapper.toLowerCase(),property);
		}
	}


	public String getTableName(String alia) {
		if (SqliStringUtil.isNullOrEmpty(alia))
			return tableName;
		if (! alia.equalsIgnoreCase(getClzName()))
			return alia;
		return tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = ParserUtil.filterSQLKeyword(tableName);
	}
	
	public String getClzName() {
		return this.clzz.getSimpleName();
	}

	public boolean isNoCache() {
		return isNoCache;
	}

	public void setNoCache(boolean isNoCache) {
		this.isNoCache = isNoCache;
	}


	public String getMapper(String property) {
		return propertyMapperMap.get(property);
	}

	public String getPropertyByLower(String mapper){
		return mapperPropertyMapLower.get(mapper.toLowerCase());
	}

	public Map<String, String> getPropertyMapperMap() {
		return propertyMapperMap;
	}
	
	public boolean isNoSpec() {
		return isNoSpec;
	}

	public void setNoSpec(boolean isNoSpec2) {
		this.isNoSpec = isNoSpec2;
	}

	@Override
	public String toString() {
		return "Parsed{" +
				", isChecked=" + isChecked +
				", clz=" + clzz +
				", tableName='" + tableName + '\'' +
				", originTable='" + originTable + '\'' +
				", isNoSpec=" + isNoSpec +
				", keyMap=" + keyMap +
				", keyFieldMap=" + keyFieldMap +
				", beanElementList=" + beanElementList +
				", elementMap=" + elementMap +
				", propertyMapperMap=" + propertyMapperMap +
				", isNoCache=" + isNoCache +
				'}';
	}
}

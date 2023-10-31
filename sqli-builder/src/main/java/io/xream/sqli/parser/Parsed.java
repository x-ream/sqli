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
package io.xream.sqli.parser;

import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;


/**
 * @author Sim
 */
public final class Parsed {


	private Class clzz;
	private String tableName;
	private String originTable;
	private boolean isNoSpec = true;

	private String key;
	private Field keyField;
	private Field tagKeyField;
	private final List<Field> tagFieldList = new ArrayList<>();
	
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

	public String getKey() {
		return key;
	}

	public void setKeyField(Field keyField) {
		this.keyField = keyField;
		this.key = keyField.getName();
	}

	public Field getKeyField(){
		return keyField;
	}

	public Field getTagKeyField() {
		return tagKeyField;
	}

	public void setTagKeyField(Field tagKeyField) {
		this.tagKeyField = tagKeyField;
	}

	public List<Field> getTagFieldList() {
		return tagFieldList;
	}

	public boolean isAutoIncreaseId(Long keyOneValue){
		if (keyField == null) return false;
		Class keyOneType = keyField.getType();
		return (keyOneType != String.class && keyOneType != Date.class && keyOneType != Timestamp.class) && (keyOneValue == null || keyOneValue == 0);
	}

	public Long tryToGetLongKey(Object obj){
		Long keyOneValue = 0L;
		if (keyField == null) return null;
		Class keyOneType = keyField.getType();
		if (keyOneType != String.class && keyOneType != Date.class && keyOneType != Timestamp.class) {
			try {
				Object keyValue = keyField.get(obj);
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

}

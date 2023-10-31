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


import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.ParserUtil;

import java.lang.reflect.Method;

/**
 *
 * @author Sim
 */
public final class BeanElement {

	private String property;
	private String setter;
	private String getter;
	private Class clz;
	private Class geneType;
	private Method getMethod;
	private Method setMethod;

	private String mapper = "";
	private int length;
	private String sqlType;
	private boolean isJson;


	public void initMaper(){
		mapper = ParserUtil.getMapper(property);
		mapper = ParserUtil.filterSQLKeyword(mapper);
	}

	public String property() {
		return mapper.equals("") ? property : mapper;
	}

	public boolean isPair() {
		if (setter == null)
			return false;
		if (getter == null)
			return false;
		if (getter.startsWith("is")) {
			return setter.substring(3).equals(getter.substring(2));
		}
		return BeanUtil.getProperty(setter).equals(BeanUtil.getProperty(getter));
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getSetter() {
		return setter;
	}

	public void setSetter(String setter) {
		this.setter = setter;
	}

	public String getGetter() {
		return getter;
	}

	public void setGetter(String getter) {
		this.getter = getter;
	}

	public Class getClz() {
		return clz;
	}

	public void setClz(Class clz) {
		this.clz = clz;
	}

	public Class getGeneType() {
		return geneType;
	}

	public void setGeneType(Class geneType) {
		this.geneType = geneType;
	}

	public Method getGetMethod() {
		return getMethod;
	}

	public void setGetMethod(Method getMethod) {
		this.getMethod = getMethod;
	}

	public Method getSetMethod() {
		return setMethod;
	}

	public void setSetMethod(Method setMethod) {
		this.setMethod = setMethod;
	}

	public String getMapper() {
		return mapper;
	}

	public void setMapper(String mapper) {
		this.mapper = mapper;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public boolean isJson() {
		return isJson;
	}

	public void setJson(boolean json) {
		isJson = json;
	}

	@Override
	public String toString() {
		return "BeanElement [property=" + property + ", setter=" + setter + ", getter=" + getter + ", sqlField="
				+ sqlType + ", clz=" + clz + "]";
	}
}

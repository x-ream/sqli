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
package io.xream.sqli.core.builder;


import io.xream.sqli.common.util.BeanUtil;
import io.xream.sqli.common.util.SqlStringUtil;
import io.xream.sqli.util.BeanUtilX;

import java.lang.reflect.Method;

/**
 * @Author Sim
 */
public class BeanElement {

	public String property;
	public String setter;
	public String getter;
	@SuppressWarnings("rawtypes")
	public Class clz;
	public int length;
	public String sqlType;

	public String mapper = "";

	public Method getMethod;
	public Method setMethod;

	public boolean isJson;
	public Class geneType;

	private String getPrefix() {
		try {
			String prefix = Parser.mappingPrefix;
			if (SqlStringUtil.isNotNull(prefix))
				return prefix;
		} catch (Exception e) {

		}
		return "";
	}

	public String getProperty() {
		
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	public void initMaper(){
		mapper = BeanUtilX.getMapper(property);
		mapper = BeanUtilX.filterSQLKeyword(mapper);
	}

	public String getMapper() {
		return mapper;
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
	

	@Override
	public String toString() {
		return "BeanElement [property=" + property + ", setter=" + setter + ", getter=" + getter + ", sqlField="
				+ sqlType + ", clz=" + clz + "]";
	}
}

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
package io.xream.sqli.core;

import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.RefreshCondition;
import io.xream.sqli.cache.QueryForCache;
import io.xream.sqli.page.Page;

import java.util.List;
import java.util.Map;


/**
 *
 * @author Sim
 *
 */
public interface Repository extends QueryForCache {

	/**
	 * @param clz
	 */
	<T> void refreshCache(Class<T> clz);
	/**
	 * @param obj
	 */
	boolean create(Object obj);
	boolean createOrReplace(Object obj);
	/**
	 * @param refreshCondition
	 * @return true | false
	 */
	<T> boolean refresh(RefreshCondition<T> refreshCondition);
	/**
	 * @param keyOne
	 */
	<T> boolean remove(KeyOne<T> keyOne);

	<T> T get(KeyOne<T> keyOne);
	/**
	 * @param conditionObj
	 * 
	 */
	<T> List<T> list(Object conditionObj);

	/**
	 *  @param criteria
	 */
	<T> Page<T> find(Criteria criteria);

	/**
	 * @param resultMapCriteria
	 * 
	 */
	Page<Map<String,Object>> find(Criteria.ResultMapCriteria resultMapCriteria);
	/**
	 *
	 * @param resultMapCriteria
	 * 
	 */
	List<Map<String,Object>> list(Criteria.ResultMapCriteria resultMapCriteria);

	<K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria resultMapCriteria);

	<T> List<T> list(Criteria criteria);

	boolean createBatch(List<? extends Object> objList);

	<T> T getOne(T condition);

    <T> boolean refresh(T t);

	<T> void findToHandle(Criteria criteria, RowHandler<T> handler);

	void findToHandle(Criteria.ResultMapCriteria resultMapCriteria, RowHandler<Map<String, Object>> handler);

	<T> List<T> listByClzz(Class<T> clzz);
}
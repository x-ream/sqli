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
package io.xream.x7.repository;

import io.xream.sqli.common.web.Page;
import io.xream.sqli.core.builder.Criteria;
import io.xream.sqli.core.builder.RowHandler;
import io.xream.sqli.core.builder.condition.RefreshCondition;
import io.xream.sqli.api.QueryForCache;

import java.util.List;
import java.util.Map;


/**
 * 
 * X7 Persistence Interface<br>
 * @author Sim
 *
 */
public interface Repository extends QueryForCache {

	/**
	 * 更新缓存
	 * @param clz
	 */
	<T> void refreshCache(Class<T> clz);
	/**
	 * 创建
	 * @param obj
	 */
	boolean create(Object obj);
	boolean createOrReplace(Object obj);
	/**
	 * 带条件支持局部更新
	 * @param refreshCondition
	 * @return true | false
	 */
	<T> boolean refresh(RefreshCondition<T> refreshCondition);
	/**
	 * 删除
	 * @param keyOne
	 */
	<T> boolean remove(KeyOne<T> keyOne);

	<T> T get(KeyOne<T> keyOne);
	/**
	 * 根据对象内容查询<br>
	 * 
	 * @param conditionObj
	 * 
	 */
	<T> List<T> list(Object conditionObj);

	/**
	 * 根据对象内容查询<br>
	 *
	 *            可以拼接的条件
	 *  @param criteria
	 */
	<T> Page<T> find(Criteria criteria);

	/**
	 * 连表查询，标准化拼接
	 * 尽量避免在互联网业务系统中使用<br>
	 * 不支持缓存<br>
	 * @param resultMapped
	 * 
	 */
	Page<Map<String,Object>> find(Criteria.ResultMappedCriteria resultMapped);
	/**
	 * 
	 * 不要通过WEB传来的参数调用此接口, 因为没有分页限制
	 * @param resultMapped
	 * 
	 */
	List<Map<String,Object>> list(Criteria.ResultMappedCriteria resultMapped);

	<K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMappedCriteria resultMapped);

	<T> List<T> list(Criteria criteria);

	boolean createBatch(List<? extends Object> objList);

	<T> T getOne(T condition);

    <T> boolean refresh(T t);

	<T> void findToHandle(Criteria criteria, RowHandler<T> handler);
	void findToHandle(Criteria.ResultMappedCriteria resultMappedCriteria, RowHandler<Map<String, Object>> handler);

	<T> List<T> listByClzz(Class<T> clzz);
}
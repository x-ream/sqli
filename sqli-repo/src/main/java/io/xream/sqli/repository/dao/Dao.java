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
package io.xream.sqli.repository.dao;

import io.xream.sqli.page.Page;
import io.xream.sqli.builder.Criteria;
import io.xream.sqli.api.RowHandler;
import io.xream.sqli.builder.InCondition;
import io.xream.sqli.builder.RefreshCondition;
import io.xream.sqli.repository.api.KeyOne;

import java.util.List;
import java.util.Map;


/**
 * 
 * @author Sim
 *
 */
public interface Dao {

	boolean create(Object obj);

	boolean createOrReplace(Object obj);

	boolean createBatch(List<? extends Object> objList);

	<T> boolean remove(KeyOne<T> keyOne);

	<T> boolean refreshByCondition(RefreshCondition<T> conditon);
	
	<T> List<T> list(Object conditionObj);
	
	List<Map<String,Object>>  list(Class clz, String sql,
                                   List<Object> conditionSet);

	<T> T get(KeyOne<T> keyOne);
	
	<T> List<T> in(InCondition inCondition);
	
	Page<Map<String, Object>> find(Criteria.ResultMapCriteria resultMapped);

	List<Map<String,Object>> list(Criteria.ResultMapCriteria resultMapped);

	<K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria resultMapped);

	<T> Page<T> find(Criteria criteria);

	<T> List<T> list(Criteria criteria);

	@Deprecated
	<T>boolean execute(T obj, String sql);

	<T> T getOne(T conditionObj);

    <T> boolean refresh(T t);

	<T> void findToHandle(Criteria criteria, RowHandler<T> handler);
	void findToHandle(Criteria.ResultMapCriteria ResultMapCriteria, RowHandler<Map<String, Object>> handler);
}
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
package io.xream.sqli.repository.dao;

import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.In;
import io.xream.sqli.builder.Qr;
import io.xream.sqli.cache.QueryForCache;
import io.xream.sqli.core.KeyOne;
import io.xream.sqli.core.RowHandler;
import io.xream.sqli.page.Page;

import java.util.List;
import java.util.Map;


/**
 * 
 * @author Sim
 *
 */
public interface Dao extends QueryForCache {

	boolean create(Object obj);

	boolean createOrReplace(Object obj);

	boolean createBatch(List<? extends Object> objList);

	<T> boolean remove(KeyOne<T> keyOne);

	<T> boolean refreshByCondition(Qr<T> conditon);
	
	<T> List<T> list(Object conditionObj);
	
	List<Map<String,Object>>  list(String sql,
                                   List<Object> conditionSet);

	<T> T get(KeyOne<T> keyOne);
	
	<T> List<T> in(In in);
	
	Page<Map<String, Object>> find(Q.X resultMapped);

	List<Map<String,Object>> list(Q.X resultMapped);

	<K> List<K> listPlainValue(Class<K> clzz, Q.X resultMapped);

	<T> Page<T> find(Q q);

	<T> List<T> list(Q q);

	boolean execute(String sql, Object...objs);

	<T> T getOne(T conditionObj);

    <T> boolean refresh(T t);

	<T> void findToHandle(Q q, RowHandler<T> handler);
	void findToHandle(Q.X resultMapCriteria, RowHandler<Map<String, Object>> handler);

    boolean exists(Q q);
}
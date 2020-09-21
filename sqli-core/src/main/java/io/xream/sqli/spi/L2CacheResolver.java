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
package io.xream.sqli.spi;

import io.xream.sqli.builder.Criteria;
import io.xream.sqli.cache.Protection;
import io.xream.sqli.cache.QueryForCache;
import io.xream.sqli.page.Page;

import java.util.List;

/**
 * 
 * 缓存<br>
 * @author sim
 *
 */
public interface L2CacheResolver extends Protection {

	void setL2CacheConsistency(L2CacheConsistency l2CacheConsistency);
	void setCacheStorage(L2CacheStorage cacheStorage);

	boolean isEnabled();
	/**
	 * 标记缓存要更新
	 * @param clz
	 * @return nanuTime_String
	 */
	@SuppressWarnings("rawtypes")
	String markForRefresh(Class clz);

	boolean refresh(Class clz, String key);
	boolean refresh(Class clz);

	<T> List<T> listUnderProtection(Class<T> clz, Object conditionObj, QueryForCache queryForCache, QueryFromDb<List<T>> queryList);
	<T> List<T> listUnderProtection(Criteria criteria, QueryForCache queryForCache, QueryFromDb<List<T>> queryList);
	<T> T getUnderProtection(Class<T> clz, Object conditonObj, QueryFromDb<T> queryObject);
	<T> T getOneUnderProtection(Class<T> clz, Object conditonObj, QueryFromDb<T> queryObject);
	<T> Page<T> findUnderProtection(Criteria criteria, QueryForCache queryForCache, QueryFromDb<Page<T>> queryPage, QueryFromDb<List<T>> queryList);

	default Object getFilterFactor(){
		return get();
	}

	interface QueryFromDb<V> {
		V query();
	}
}

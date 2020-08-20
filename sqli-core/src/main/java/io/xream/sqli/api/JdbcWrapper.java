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
package io.xream.sqli.api;

import io.xream.sqli.core.builder.Criteria;
import io.xream.sqli.core.builder.RowHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author Sim
 */
public interface JdbcWrapper {

    <T> boolean createBatch(Class<T> clzz, String sql, Collection<T> objList, int batchSize, Dialect dialect);

    boolean create(boolean isAutoIncreaseId, String sql, List<Object> valueList);

    boolean createOrReplace(String sql, List<Object> valueList);

    boolean refresh(String sql, Object[] valueList);

    boolean remove(String sql, Object id);

    boolean execute(String sql);

    <T> List<T> queryForList(String sql, Class<T> clz, Collection<Object> list, Dialect dialect);

    <K> List<K> queryForPlainValueList(Class<K> clzz, String sql, Collection<Object> valueList, Dialect dialect);

    List<Map<String, Object>> queryForMapList(String sql, Criteria.ResultMappedCriteria resultMapped, Dialect dialect);

    <T> void queryForMapToHandle(Class clzz, String sql, Collection<Object> valueList, Dialect dialect, Criteria.ResultMappedCriteria resultMappedCriteria, RowHandler<T> handler);
}

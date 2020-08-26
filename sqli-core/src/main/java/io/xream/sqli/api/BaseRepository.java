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

import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.InCondition;
import io.xream.sqli.builder.RefreshCondition;
import io.xream.sqli.builder.RemoveRefreshCreate;
import io.xream.sqli.page.Page;

import java.util.List;
import java.util.Map;

/**
 * jdbc-template-plus programmer API
 *
 * @param <T>
 * @author Sim
 */
public interface BaseRepository<T> extends Typed<T> {

    long createId();

    void refreshCache();

    boolean createBatch(List<T> objList);

    boolean create(T obj);

    /**
     * replace: clear all the value of the row, and insert the new value </>
     * is not refreshOrCreate </>
     * x7 will not support refreshOrCreate, coding: query at first, then refresh of create <br>
     */
    boolean createOrReplace(T obj);

    boolean refresh(RefreshCondition<T> RefreshCondition_build);

    /**
     *
     *  refreshCondition without keyOne
     */
    boolean refreshUnSafe(RefreshCondition<T> RefreshCondition_build);

    boolean remove(String keyOne);

    boolean remove(long keyOne);

    /**
     *
     * caution:  sometimes, should not use the api </>
     *
     */
    boolean removeRefreshCreate(RemoveRefreshCreate<T> RemoveRrefreshCreate_wrap);
    /**
     * @param keyOne
     */
    T get(long keyOne);

    T get(String keyOne);

    /**
     * LOAD
     *
     */
    List<T> list();

    /**
     * 根据对象查询
     *
     * @param conditionObj
     */
    List<T> list(T conditionObj);

    T getOne(T conditionObj);

    /**
     * in API
     *
     */
    List<T> in(InCondition InCondition_wrap);

    /**
     * Standard query pageable API
     *
     * @param CriteriaBuilder_build_get
     */
    Page<T> find(Criteria CriteriaBuilder_build_get);

    Page<Map<String, Object>> find(Criteria.ResultMapCriteria CriteriaBuilder_ResultMappedBuilder_build_get);

    List<Map<String, Object>> list(Criteria.ResultMapCriteria CriteriaBuilder_ResultMappedBuilder_build_get);

    <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria CriteriaBuilder_ResultMappedBuilder_build_get);

    List<T> list(Criteria CriteriaBuilder_build_get);

    /**
     * like stream, fetchSize=50, the api not fast, to avoid OOM when scheduling
     * @param criteria
     * @param handler
     * @param <T>
     */
    <T> void findToHandle(Criteria criteria, RowHandler<T> handler);

    /**
     * like stream, fetchSize=50, the api not fast, to avoid OOM when scheduling
     * @param ResultMapCriteria
     * @param handler
     */
    void findToHandle(Criteria.ResultMapCriteria ResultMapCriteria, RowHandler<Map<String, Object>> handler);
}
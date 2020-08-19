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
package io.xream.x7.repository.transform;

import io.xream.sqli.common.util.SqliExceptionUtil;
import io.xream.sqli.common.web.Page;
import io.xream.sqli.core.builder.Criteria;
import io.xream.sqli.core.builder.RowHandler;
import io.xream.sqli.core.builder.condition.InCondition;
import io.xream.sqli.core.builder.condition.RefreshCondition;
import io.xream.x7.repository.KeyOne;
import io.xream.x7.repository.dao.Dao;

import java.util.List;
import java.util.Map;

public class SqlDataTransform implements DataTransform {

    private Dao dao;

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    @Deprecated
    @Override
    public <T> void refreshCache(Class<T> clz) {
        throw new RuntimeException("Wrong Code");
    }

    @Override
    public boolean create(Object obj) {
        return this.dao.create(obj);
    }

    @Override
    public boolean createOrReplace(Object obj) {
        return this.dao.createOrReplace(obj);
    }

    @Override
    public boolean createBatch(List<?> objList) {
        return this.dao.createBatch(objList);
    }

    @Override
    public <T> T getOne(T condition) {
        return this.dao.getOne(condition);
    }

    @Override
    public <T> boolean refresh(T t) {
        return this.dao.refresh(t);
    }

    @Override
    public <T> boolean refresh(RefreshCondition<T> refreshCondition) {
        return this.dao.refreshByCondition(refreshCondition);
    }

    @Override
    public <T> boolean remove(KeyOne<T> keyOne) {
        return this.dao.remove(keyOne);
    }


    @Override
    public <T> boolean execute(T obj, String sql) {
        return this.dao.execute(obj, sql);
    }


    @Override
    public <T> List<T> list(Object obj) {
        return this.dao.list(obj);
    }

    @Override
    public List<Map<String, Object>> list(Class clz, String sql, List<Object> conditionSet) {
        return this.dao.list(clz, sql, conditionSet);
    }

    @Override
    public <T> T get(KeyOne<T> keyOne) {//带ID查询, 不需要alia; 不带ID查询,需要alia
        return this.dao.get(keyOne);
    }


    @Override
    public <T> List<T> in(InCondition inCondition) {
        return this.dao.in(inCondition);
    }


    @Override
    public <T> Page<T> find(Criteria criteria) {
        return this.dao.find(criteria);
    }

    @Override
    public Page<Map<String, Object>> find(Criteria.ResultMappedCriteria resultMapped) {
        return this.dao.find(resultMapped);
    }

    @Override
    public List<Map<String, Object>> list(Criteria.ResultMappedCriteria resultMapped) {
        return this.dao.list(resultMapped);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMappedCriteria resultMapped){
        return this.dao.listPlainValue(clzz, resultMapped);
    }

    @Override
    public <T> List<T> list(Criteria criteria) {
        return this.dao.list(criteria);
    }


    @Override
    public <T> void findToHandle(Criteria criteria, RowHandler<T> handler) {
        this.dao.findToHandle(criteria,handler);
    }

    @Override
    public void findToHandle(Criteria.ResultMappedCriteria resultMappedCriteria, RowHandler<Map<String,Object>> handler) {
        this.dao.findToHandle(resultMappedCriteria,handler);
    }

    @Override
    public <T> List<T> listByClzz(Class<T> clzz) {

        T obj = null;
        try{
            obj = clzz.newInstance();
        }catch (Exception e){
            throw new RuntimeException(SqliExceptionUtil.getMessage(e));
        }
        return this.dao.list(obj);
    }
}

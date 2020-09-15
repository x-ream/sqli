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
package io.xream.sqli.repository.cache;


import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.InCondition;
import io.xream.sqli.builder.KV;
import io.xream.sqli.builder.RefreshCondition;
import io.xream.sqli.cache.L2CacheResolver;
import io.xream.sqli.core.RowHandler;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.core.KeyOne;
import io.xream.sqli.repository.core.Manuable;
import io.xream.sqli.repository.core.Repository;
import io.xream.sqli.repository.transform.DataTransform;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliLoggerProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author Sim
 */
public final class CacheableRepository implements Repository, Manuable {

    private final static Logger logger = LoggerFactory.getLogger(Repository.class);

    private DataTransform dataTransform;
    private L2CacheResolver cacheResolver;

    public void setDataTransform(DataTransform dataTransform) {
        logger.info("x7-repo/x7-jdbc-template-plus on starting....");
        this.dataTransform = dataTransform;
    }

    public void setCacheResolver(L2CacheResolver cacheResolver) {
        this.cacheResolver = cacheResolver;
    }

    private boolean isCacheEnabled(Parsed parsed) {
        boolean b = cacheResolver.isEnabled() && !parsed.isNoCache();
        if (b){
            SqliLoggerProxy.debug(parsed.getClzz(), "L2Cache effected");
        }else{
            SqliLoggerProxy.debug(parsed.getClzz(), "L2Cache not effected");
        }
        return b;
    }


    @Override
    public boolean create(Object obj) {

        Class clz = obj.getClass();
        Parsed parsed = Parser.get(clz);
        boolean flag = dataTransform.create(obj);

        if (isCacheEnabled(parsed))
            cacheResolver.markForRefresh(clz);
        return flag;
    }

    @Override
    public boolean createOrReplace(Object obj) {

        Class clz = obj.getClass();
        Parsed parsed = Parser.get(clz);
        Object id = CreateOrReplaceOptimization.tryToGetId(obj,parsed);

        boolean flag = dataTransform.createOrReplace(obj);

        if (!flag) return flag;

        if (isCacheEnabled(parsed)) {
            cacheResolver.refresh(clz, String.valueOf(id));
        }
        return flag;
    }

    @Override
    public <T> boolean refresh(T t) {
        if (t == null)
            return false;
        boolean flag = dataTransform.refresh(t);
        if (!flag) return flag;

        Class clz = t.getClass();
        Parsed parsed = Parser.get(clz);
        if (isCacheEnabled(parsed)) {
            Object id = ParserUtil.tryToGetId(t, parsed);
            cacheResolver.refresh(clz, String.valueOf(id));
        }

        return flag;
    }

    @Override
    public <T> boolean refresh(RefreshCondition<T> refreshCondition) {

        boolean flag = dataTransform.refresh(refreshCondition);

        if (!flag) return flag;

        Class clz = refreshCondition.getClz();
        Parsed parsed = Parser.get(clz);
        if (isCacheEnabled(parsed)) {

            KV keyOne = refreshCondition.tryToGetKeyOne();

            String key = null;
            if (keyOne != null && Objects.nonNull(keyOne.getV())) {
                key = String.valueOf(keyOne.getV());
            }
            cacheResolver.refresh(clz, key);
        }
        return flag;
    }


    public <T> void refreshCache(Class<T> clz) {
        Parsed parsed = Parser.get(clz);
        if (isCacheEnabled(parsed)) {
            cacheResolver.refresh(clz);
        }
    }

    @Override
    public <T> boolean remove(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        Parsed parsed = Parser.get(clz);
        boolean flag = dataTransform.remove(keyOne);

        if (!flag) return flag;

        if (isCacheEnabled(parsed)) {
            String key = String.valueOf(keyOne.get());
            cacheResolver.refresh(clz, key);
        }
        return flag;
    }

    @Override
    public <T> List<T> listByClzz(Class<T> clzz) {
        return this.dataTransform.listByClzz(clzz);
    }

    @Override
    public <T> List<T> list(Object conditionObj) {

        Class clz = conditionObj.getClass();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dataTransform.list(conditionObj);

        return cacheResolver.listUnderProtection(clz,
                conditionObj,
                dataTransform,
                (Callable<List<T>>) () -> dataTransform.list(conditionObj));
    }

    @Override
    public <T> Page<T> find(Criteria criteria) {

        Class clz = criteria.getClzz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dataTransform.find(criteria);

        return cacheResolver.findUnderProtection(criteria,
                dataTransform,
                () -> dataTransform.find(criteria),
                () -> dataTransform.list(criteria));
    }


    @Override
    public <T> List<T> list(Criteria criteria) {

        Class clz = criteria.getClzz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dataTransform.list(criteria);

        return cacheResolver.listUnderProtection(
                criteria,
                dataTransform,
                () -> dataTransform.list(criteria));

    }


    public <T> boolean execute(T obj, String sql) {

        Class clz = obj.getClass();
        Parsed parsed = Parser.get(clz);
        boolean b = dataTransform.execute(obj, sql);

        if (!b)
            return b;
        if (isCacheEnabled(parsed)) {
            String key = ParserUtil.getCacheKey(obj, parsed);
            cacheResolver.refresh(clz, key);
        }

        return b;
    }


    protected <T> List<T> in0(InCondition inCondition) {

        Class clz = inCondition.getClz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dataTransform.in(inCondition);

        String condition = InOptimization.keyCondition(inCondition);

        return cacheResolver.listUnderProtection(clz,
                condition,
                dataTransform,
                (Callable<List<T>>) () -> dataTransform.in(inCondition));

    }


    @Override
    public <T> List<T> in(InCondition inCondition) {
        return InOptimization.in(inCondition, this);
    }


    @Override
    public boolean createBatch(List<? extends Object> objList) {
        if (objList.isEmpty())
            return false;

        Class clz = objList.get(0).getClass();
        Parsed parsed = Parser.get(clz);
        boolean flag = this.dataTransform.createBatch(objList);
        if (isCacheEnabled(parsed))
            cacheResolver.markForRefresh(clz);

        return flag;
    }


    @Override
    public <T> T get(KeyOne<T> keyOne) {

        Class<T> clz = keyOne.getClzz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dataTransform.get(keyOne);

        String condition = String.valueOf(keyOne.get());//IMPORTANT

        return cacheResolver.getUnderProtection(clz, condition, () -> dataTransform.get(keyOne));

    }

    @Override
    public <T> T getOne(T condition) {

        Class<T> clz = (Class<T>) condition.getClass();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dataTransform.getOne(condition);

        return cacheResolver.getOneUnderProtection(clz, condition, () -> dataTransform.getOne(condition));
    }

    @Override
    public Page<Map<String, Object>> find(Criteria.ResultMapCriteria resultMapped) {
        return dataTransform.find(resultMapped);
    }

    @Override
    public List<Map<String, Object>> list(Criteria.ResultMapCriteria resultMapped) {
        return dataTransform.list(resultMapped);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria resultMapped){
        return dataTransform.listPlainValue(clzz,resultMapped);
    }


    @Override
    public List<Map<String, Object>> list(Class clz, String sql, List<Object> conditionSet) {
        return dataTransform.list(clz, sql, conditionSet);
    }

    @Override
    public <T> void findToHandle(Criteria criteria, RowHandler<T> handler) {
        this.dataTransform.findToHandle(criteria,handler);
    }

    @Override
    public void findToHandle(Criteria.ResultMapCriteria ResultMapCriteria, RowHandler<Map<String, Object>> handler) {
        this.dataTransform.findToHandle(ResultMapCriteria,handler);
    }

}

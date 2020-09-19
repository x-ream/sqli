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
package io.xream.sqli.core.core;


import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.InCondition;
import io.xream.sqli.builder.KV;
import io.xream.sqli.builder.RefreshCondition;
import io.xream.sqli.cache.L2CacheResolver;
import io.xream.sqli.core.KeyOne;
import io.xream.sqli.core.NativeSupport;
import io.xream.sqli.core.Repository;
import io.xream.sqli.core.RowHandler;
import io.xream.sqli.core.dao.Dao;
import io.xream.sqli.exception.QueryException;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliLoggerProxy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author Sim
 */
public final class CacheableRepository implements Repository, NativeSupport {

    private static CacheableRepository instance;
    private Dao dao;
    private L2CacheResolver cacheResolver;

    private CacheableRepository(){};
    public static CacheableRepository newInstance(){
        if (instance == null){
            instance = new CacheableRepository();
            return instance;
        }
        return null;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
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
        boolean flag = dao.create(obj);

        if (isCacheEnabled(parsed))
            cacheResolver.markForRefresh(clz);
        return flag;
    }

    @Override
    public boolean createOrReplace(Object obj) {

        Class clz = obj.getClass();
        Parsed parsed = Parser.get(clz);
        Object id = CreateOrReplaceOptimization.tryToGetId(obj,parsed);

        boolean flag = dao.createOrReplace(obj);

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
        boolean flag = dao.refresh(t);
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

        boolean flag = dao.refreshByCondition(refreshCondition);

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
        boolean flag = dao.remove(keyOne);

        if (!flag) return flag;

        if (isCacheEnabled(parsed)) {
            String key = String.valueOf(keyOne.get());
            cacheResolver.refresh(clz, key);
        }
        return flag;
    }

    @Override
    public <T> List<T> listByClzz(Class<T> clzz) {
        try {
            return this.dao.list(clzz.newInstance());
        }catch (Exception e){
            if (e instanceof RuntimeException){
                throw (RuntimeException) e;
            }
            throw new QueryException(SqliExceptionUtil.getMessage(e));
        }
    }

    @Override
    public <T> List<T> list(Object conditionObj) {

        Class clz = conditionObj.getClass();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.list(conditionObj);

        return cacheResolver.listUnderProtection(clz,
                conditionObj,
                dao,
                (Callable<List<T>>) () -> dao.list(conditionObj));
    }

    @Override
    public <T> Page<T> find(Criteria criteria) {

        Class clz = criteria.getClzz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.find(criteria);

        return cacheResolver.findUnderProtection(criteria,
                dao,
                () -> dao.find(criteria),
                () -> dao.list(criteria));
    }


    @Override
    public <T> List<T> list(Criteria criteria) {

        Class clz = criteria.getClzz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.list(criteria);

        return cacheResolver.listUnderProtection(
                criteria,
                dao,
                () -> dao.list(criteria));

    }


    public <T> boolean execute(Class<T> clzz, String sql) {

        Parsed parsed = Parser.get(clzz);
        boolean b = dao.execute(clzz, sql);

        if (!b)
            return b;
        if (isCacheEnabled(parsed)) {
            String key = ParserUtil.getCacheKey(clzz, parsed);
            cacheResolver.refresh(clzz, key);
        }

        return b;
    }


    protected <T> List<T> in0(InCondition inCondition) {

        Class clz = inCondition.getClz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.in(inCondition);

        String condition = InOptimization.keyCondition(inCondition);

        return cacheResolver.listUnderProtection(clz,
                condition,
                dao,
                (Callable<List<T>>) () -> dao.in(inCondition));

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
        boolean flag = this.dao.createBatch(objList);
        if (isCacheEnabled(parsed))
            cacheResolver.markForRefresh(clz);

        return flag;
    }


    @Override
    public <T> T get(KeyOne<T> keyOne) {

        Class<T> clz = keyOne.getClzz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.get(keyOne);

        String condition = String.valueOf(keyOne.get());//IMPORTANT

        return cacheResolver.getUnderProtection(clz, condition, () -> dao.get(keyOne));

    }

    @Override
    public <T> T getOne(T condition) {

        Class<T> clz = (Class<T>) condition.getClass();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.getOne(condition);

        return cacheResolver.getOneUnderProtection(clz, condition, () -> dao.getOne(condition));
    }

    @Override
    public Page<Map<String, Object>> find(Criteria.ResultMapCriteria resultMapped) {
        return dao.find(resultMapped);
    }

    @Override
    public List<Map<String, Object>> list(Criteria.ResultMapCriteria resultMapped) {
        return dao.list(resultMapped);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria resultMapped){
        return dao.listPlainValue(clzz,resultMapped);
    }


    @Override
    public List<Map<String, Object>> list(String sql, List<Object> conditionSet) {
        return dao.list(sql, conditionSet);
    }

    @Override
    public <T> void findToHandle(Criteria criteria, RowHandler<T> handler) {
        this.dao.findToHandle(criteria,handler);
    }

    @Override
    public void findToHandle(Criteria.ResultMapCriteria ResultMapCriteria, RowHandler<Map<String, Object>> handler) {
        this.dao.findToHandle(ResultMapCriteria,handler);
    }

}

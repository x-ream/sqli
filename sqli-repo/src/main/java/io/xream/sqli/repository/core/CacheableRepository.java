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
package io.xream.sqli.repository.core;


import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.In;
import io.xream.sqli.builder.KV;
import io.xream.sqli.builder.Qr;
import io.xream.sqli.core.KeyOne;
import io.xream.sqli.core.NativeSupport;
import io.xream.sqli.core.Repository;
import io.xream.sqli.core.RowHandler;
import io.xream.sqli.exception.QueryException;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.dao.Dao;
import io.xream.sqli.spi.L2CacheResolver;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliLoggerProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sim
 */
public final class CacheableRepository implements Repository, NativeSupport {

    private static CacheableRepository instance;
    private Dao dao;
    private L2CacheResolver cacheResolver;

    private CacheableRepository(){}
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
    public <T> boolean refresh(Qr<T> qr) {

        if (qr.isAbort())
            return false;

        boolean flag = dao.refreshByCondition(qr);

        if (!flag) return flag;

        Class clz = qr.getClz();
        Parsed parsed = Parser.get(clz);
        if (isCacheEnabled(parsed)) {

            KV keyOne = qr.tryToGetKeyOne();

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

        if (parsed.getKey() == null)
            throw new IllegalStateException("no primary key, can not call remove(id)");

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
            SqliExceptionUtil.throwRuntimeExceptionFirst(e);
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
                () -> dao.list(conditionObj));
    }

    @Override
    public <T> Page<T> find(Q q) {

        if (q.isAbort()) {
            Page page = new Page<>();
            page.setClzz(q.getClzz());
            page.setRows(q.getRows());
            page.setPage(q.getPage());
            return page;
        }

        Class clz = q.getClzz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.find(q);

        return cacheResolver.findUnderProtection(q,
                dao,
                () -> dao.find(q),
                () -> dao.list(q));
    }


    @Override
    public <T> List<T> list(Q q) {

        if (q.isAbort())
            return new ArrayList<>();

        Class clz = q.getClzz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.list(q);

        return cacheResolver.listUnderProtection(
                q,
                dao,
                () -> dao.list(q));

    }


    public boolean execute(String sql, Object...objs) {
        return dao.execute(sql,objs);
    }


    protected <T> List<T> in0(In in) {

        Class clz = in.getClz();
        Parsed parsed = Parser.get(clz);

        if (!isCacheEnabled(parsed))
            return dao.in(in);

        String condition = InOptimization.keyCondition(in);

        return cacheResolver.listUnderProtection(clz,
                condition,
                dao,
                () -> dao.in(in));

    }


    @Override
    public <T> List<T> in(In in) {
        return InOptimization.in(in, this);
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

        if (parsed.getKey() == null)
            throw new IllegalStateException("no primary key, can not call get(id)");

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
    public Page<Map<String, Object>> find(Q.X xq) {
        if (xq.isAbort()) {
            Page page = new Page<>();
            page.setClzz(xq.getClzz());
            page.setRows(xq.getRows());
            page.setPage(xq.getPage());
            return page;
        }
        return dao.find(xq);
    }

    @Override
    public List<Map<String, Object>> list(Q.X xq) {

        if (xq.isAbort())
            return new ArrayList<>();

        return dao.list(xq);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Q.X xq){
        if (xq.isAbort())
            return new ArrayList<>();
        return dao.listPlainValue(clzz,xq);
    }


    @Override
    public List<Map<String, Object>> list(String sql, List<Object> conditionSet) {
        return dao.list(sql, conditionSet);
    }

    @Override
    public <T> void findToHandle(Q q, RowHandler<T> handler) {
        if (q.isAbort())
            return;
        this.dao.findToHandle(q,handler);
    }

    @Override
    public void findToHandle(Q.X xq, RowHandler<Map<String, Object>> handler) {
        if (xq.isAbort())
            return;
        this.dao.findToHandle(xq,handler);
    }

    @Override
    public boolean exists(Q q) {
        return this.dao.exists(q);
    }
}

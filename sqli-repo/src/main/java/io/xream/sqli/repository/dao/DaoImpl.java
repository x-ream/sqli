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

import io.xream.sqli.builder.In;
import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.Qr;
import io.xream.sqli.builder.internal.PageBuilderHelper;
import io.xream.sqli.builder.internal.Q2Sql;
import io.xream.sqli.builder.internal.SqlBuilt;
import io.xream.sqli.converter.ObjectDataConverter;
import io.xream.sqli.core.KeyOne;
import io.xream.sqli.core.Keys;
import io.xream.sqli.core.RowHandler;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.exception.ExceptionTranslator;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.exception.TooManyResultsException;
import io.xream.sqli.repository.init.SqlInit;
import io.xream.sqli.repository.init.SqlTemplate;
import io.xream.sqli.repository.util.ResultSortUtil;
import io.xream.sqli.spi.JdbcHelper;
import io.xream.sqli.util.SqliLoggerProxy;
import io.xream.sqli.util.SqliStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Sim
 */
public final class DaoImpl implements Dao, SqlTemplate {

    private Logger logger = LoggerFactory.getLogger(Dao.class);

    private static Dao instance;
    private Q2Sql q2Sql;
    private Dialect dialect;
    private JdbcHelper jdbcHelper;

    private SqlBuilder sqlBuilder = SqlBuilder.getInstance();
    
    private DaoImpl(){}

    public static Dao newInstance(){
        if (instance == null){
            instance = new DaoImpl();
            return instance;
        }
        return null;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }
    public Dialect getDialect(){
        return this.dialect;
    }

    public void set2Sql(Q2Sql q2Sql) {
        this.q2Sql = q2Sql;
    }

    public void setJdbcHelper(JdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }

    @Override
    public boolean createBatch(List<? extends Object> objList) {

        if (objList.isEmpty())
            return false;
        Object obj = objList.get(0);
        Class clz = obj.getClass();
        String sql = getSql(clz, SqlInit.CREATE);

        SqliLoggerProxy.debug(clz, sql);

        Parsed parsed = Parser.get(clz);
        JdbcHelper.BatchObjectValues batchObjectValues =  () -> {
            List<Collection<Object>> valuesList = new ArrayList<>();
            for (Object o : objList) {
                Collection<Object> values= ObjectDataConverter.objectToListForCreate(o, parsed, dialect);
                valuesList.add(values);
            }
            return valuesList;
        };

        final int batchSize = 500;
        try {
            return this.jdbcHelper.createBatch(clz, sql, batchObjectValues, batchSize, this.dialect);
        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(clz, e, logger);
        }//1618978538016
    }

    @Override
    public <T> boolean remove(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        String sql = getSql(clz, SqlInit.REMOVE);

        SqliLoggerProxy.debug(clz, keyOne.get());
        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcHelper.remove(sql, keyOne.get());
    }

    @Override
    public <T> boolean removeIn(Keys<T> keys) {

        Class clz = keys.getClzz();
        String sql = getSql(clz, SqlInit.REMOVE_IN);

        sql = sqlBuilder.buildQueryByInCondition(sql, clz, keys.list());

        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcHelper.execute(sql);
    }

    @Override
    public boolean create(Object obj) {

        Class clz = obj.getClass();

        try {
            String sql = getSql(clz, SqlInit.CREATE);

            Parsed parsed = Parser.get(clz);

            Long keyOneValue = parsed.tryToGetLongKey(obj);
            boolean isAutoIncreaseId = parsed.isAutoIncreaseId(keyOneValue);

            List<Object> valueList = ObjectDataConverter.objectToListForCreate(obj, parsed, dialect);

            SqliLoggerProxy.debug(clz, valueList);
            SqliLoggerProxy.debug(clz, sql);

            return this.jdbcHelper.create(isAutoIncreaseId,sql,valueList);

        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(clz, e, logger);
        }

    }

    @Override
    public boolean createOrReplace(Object obj) {

        Class clz = obj.getClass();

        try {
            String createSql = getSql(clz, SqlInit.CREATE);
            final String sql = this.dialect.createOrReplaceSql(createSql);

            Parsed parsed = Parser.get(clz);
            List<Object> valueList = ObjectDataConverter.objectToListForCreate(obj, parsed, dialect);

            SqliLoggerProxy.debug(clz, valueList);
            SqliLoggerProxy.debug(clz, sql);

            return this.jdbcHelper.createOrReplace(sql, valueList);

        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(clz, e, logger);
        }
    }


    @Override
    public List<Map<String, Object>> list(String sql, List<Object> conditionList) {

        return this.jdbcHelper.queryForXList(sql, conditionList,null, null,this.dialect);
    }


    @Override
    public <T> T get(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        String sql = getSql(clz, SqlInit.GET_ONE);
        SqliLoggerProxy.debug(clz, keyOne.get());
        SqliLoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcHelper.queryForList(sql, Arrays.asList(keyOne.get()), Parser.get(clz), this.dialect);

        if (list.isEmpty())
            return null;

        return list.get(0);
    }


    @Override
    public <T> List<T> list(Object conditionObj) {

        Class clz = conditionObj.getClass();
        String sql = getSql(clz, SqlInit.LOAD);
        Parsed parsed = Parser.get(clz);

        Map<String, Object> queryMap = ObjectDataConverter.objectToMap(parsed, conditionObj);
        sql = sqlBuilder.buildQueryByObject(parsed, sql, queryMap);
        SqliLoggerProxy.debug(clz, conditionObj);
        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcHelper.queryForList(sql, queryMap.values(), parsed, this.dialect);

    }

    @Override
    public <T> List<T> list(Q q) {

        Class clz = q.getClzz();
        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, q, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();
        SqliLoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcHelper.queryForList(sql, valueList, Parser.get(clz), this.dialect);
        ResultSortUtil.sort(list, q, Parser.get(clz));
        return list;
    }

    @Override
    public <T> Page<T> find(Q q) {

        Class clz = q.getClzz();
        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, q, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();

        SqliLoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcHelper.queryForList(sql, valueList,Parser.get(clz), this.dialect);
        Parsed parsed = Parser.get(clz);
        ResultSortUtil.sort(list, q, parsed);

        Page<T> pagination = PageBuilderHelper.build(q, list, () -> getCount(clz, sqlBuilt.getCountSql(), valueList));

        return pagination;
    }


    /**
     * getCount
     *
     * @param sql
     * @param list
     * @return
     */
    private long getCount(Class clz, String sql, Collection<Object> list) {
        SqliLoggerProxy.debug(clz, sql);
        return this.jdbcHelper.queryForPlainValueList(Long.class,sql,list,this.dialect).get(0);
    }


    /**
     *
     * @param
     * @param sql
     */
    @Deprecated
    @Override
    public boolean execute(String sql, Object...objs) {

        return this.jdbcHelper.execute(sql,objs);

    }


    @Override
    public boolean refreshByCondition(Qr qr) {

        Class clz = qr.getClz();
        Parsed parsed = Parser.get(clz);
        String sql = sqlBuilder.buildRefreshByCondition(parsed, qr, this.q2Sql,this.dialect);
        List<Object> valueList = qr.getValueList();

        SqliLoggerProxy.debug(clz, valueList);
        SqliLoggerProxy.debug(clz, sql);

        return update(sql, valueList, dialect, jdbcHelper);
    }

    @Override
    public <T> boolean refresh(T t) {

        Class clz = t.getClass();
        Object[] arr = sqlBuilder.buildRefreshSqlAndValueListByObject(t,clz,dialect);

        String sql = (String)arr[0];
        Collection<Object> valueList = (Collection<Object>)arr[1];
        SqliLoggerProxy.debug(clz, valueList);
        SqliLoggerProxy.debug(clz, sql);

        return update(sql,valueList,dialect, jdbcHelper);
    }


    @Override
    public <T> List<T> in(In in) {

        Class<T> clz = in.getClz();
        Parsed parsed = Parser.get(clz);

        String inProperty = in.getProperty();
        if (SqliStringUtil.isNullOrEmpty(inProperty)) {
            inProperty = parsed.getKey();
        }

        BeanElement be = parsed.getElementExisted(inProperty);

        String sql = getSql(clz, SqlInit.LOAD);
        String mapper = parsed.getMapper(inProperty);
        List<? extends Object> inList = in.getInList();

        sql = sqlBuilder.buildQueryByInCondition(sql, mapper, be, inList);

        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcHelper.queryForList(sql, null, parsed,  this.dialect);
    }

    @Override
    public Page<Map<String, Object>> find(Q.X xq) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, xq, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();
        Class clz = xq.getClzz();

        SqliLoggerProxy.debug(xq.getRepositoryClzz(), sql);

        List<Map<String, Object>> list = this.jdbcHelper.queryForXList(sql, valueList, xq, clz, this.dialect);
        ResultSortUtil.sort(list, xq);
        Page<Map<String, Object>> pagination = PageBuilderHelper.build(xq, list, () -> getCount(xq.getRepositoryClzz(), sqlBuilt.getCountSql(), valueList));

        return pagination;
    }

    @Override
    public List<Map<String, Object>> list(Q.X xq) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, xq, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();

        SqliLoggerProxy.debug(xq.getRepositoryClzz(), sql);

        List<Map<String,Object>> list = this.jdbcHelper.queryForXList(sql, valueList, xq, xq.getClzz(), this.dialect);
        ResultSortUtil.sort(list, xq);
        return list;
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Q.X xq){
        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, xq, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();

        SqliLoggerProxy.debug(xq.getRepositoryClzz(), sql);

        List<K> list = this.jdbcHelper.queryForPlainValueList(clzz,sql,valueList,this.dialect);
        return list;
    }


    @Override
    public <T> T getOne(T conditionObj) {

        Class clz = conditionObj.getClass();
        String sql = getSql(clz, SqlInit.LOAD);
        Parsed parsed = Parser.get(clz);

        Map<String, Object> queryMap = ObjectDataConverter.objectToMap(parsed, conditionObj);
        sql = sqlBuilder.buildQueryByObject(parsed, sql, queryMap);
        sql = sqlBuilder.buildPageSql(sql, 1, 1, this.dialect);
        SqliLoggerProxy.debug(clz, conditionObj);
        SqliLoggerProxy.debug(clz, sql);

        if (queryMap.isEmpty())
            throw new IllegalArgumentException("API of getOne(T) can't accept blank object: " + conditionObj);

        List<T> list = this.jdbcHelper.queryForList(sql, queryMap.values(),parsed, this.dialect);

        if (list.isEmpty())
            return null;
        if (list.size() > 1)
            throw new TooManyResultsException("Expected one result (or null) to be returned by API of getOne(T), but found: " + list.size());
        return list.get(0);
    }

    @Override
    public <T> T getOne(Q q) {
        Class clz = q.getClzz();
        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, q, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();
        SqliLoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcHelper.queryForList(sql, valueList, Parser.get(clz), this.dialect);

        if (list.isEmpty())
            return null;
        if (list.size() > 1)
            throw new TooManyResultsException("Expected one result (or null) to be returned by API of getOne(T), but found: " + list.size());
        return list.get(0);
    }

    @Override
    public void findToHandle(Q.X xq, RowHandler<Map<String,Object>> handler) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, xq, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();

        SqliLoggerProxy.debug(xq.getRepositoryClzz(), sql);

        this.jdbcHelper.queryForMapToHandle(sql, valueList, dialect, xq, null, handler);
    }

    @Override
    public <T> void findToHandle(Q q, RowHandler<T> handler) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, q, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();
        Class clz = q.getClzz();

        SqliLoggerProxy.debug(clz, sql);

        this.jdbcHelper.queryForMapToHandle(sql, valueList, dialect,null, Parser.get(clz), handler);
    }

    @Override
    public boolean exists(Q q) {

        Class clz = q.getClzz();
        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByQ(valueList, q, q2Sql, dialect);
        String sql = sqlBuilt.getSql().toString();
        sql = sql.replace("*","1");
        sql += " LIMIT 1";
        SqliLoggerProxy.debug(clz, sql);

        return ! this.jdbcHelper.queryForPlainValueList(Long.class, sql, valueList,this.dialect).isEmpty();
    }

    private boolean update(String sql, Collection<Object> list, Dialect dialect, JdbcHelper jdbcHelper) {
        try {
            Object[] arr = dialect.toArr(list);
            return jdbcHelper.refresh(sql,arr);
        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(null, e, logger);
        }
    }

}

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

import io.xream.sqli.annotation.X;
import io.xream.sqli.builder.*;
import io.xream.sqli.builder.internal.PageBuilderHelper;
import io.xream.sqli.converter.ObjectDataConverter;
import io.xream.sqli.core.Dialect;
import io.xream.sqli.core.KeyOne;
import io.xream.sqli.core.RowHandler;
import io.xream.sqli.exception.ExceptionTranslator;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.exception.TooManyResultsException;
import io.xream.sqli.repository.init.SqlInit;
import io.xream.sqli.repository.init.SqlInitFactory;
import io.xream.sqli.repository.util.ResultSortUtil;
import io.xream.sqli.repository.util.SqlParserUtil;
import io.xream.sqli.spi.JdbcHelper;
import io.xream.sqli.util.SqliLoggerProxy;
import io.xream.sqli.util.SqliStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Sim
 */
public final class DaoImpl implements Dao {

    private Logger logger = LoggerFactory.getLogger(Dao.class);

    private static Dao instance;
    private CriteriaToSql criteriaToSql;
    private Dialect dialect;
    private JdbcHelper jdbcHelper;

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

    public void setCriteriaToSql(CriteriaToSql criteriaToSql) {
        this.criteriaToSql = criteriaToSql;
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
        String sql = SqlInitFactory.getSql(clz, SqlInit.CREATE);

        SqliLoggerProxy.debug(clz, sql);

        Parsed parsed = Parser.get(clz);
        JdbcHelper.BatchObjectValues batchObjectValues =  () -> {
            List<Collection<Object>> valuesList = new ArrayList<>();
            for (Object o : objList) {
                Collection<Object> values= ObjectDataConverter.objectToListForCreate(o, parsed.getBeanElementList(), dialect);
                valuesList.add(values);
            }
            return valuesList;
        };

        final int batchSize = 500;
        try {
            return this.jdbcHelper.createBatch(clz, sql, batchObjectValues, batchSize, this.dialect);
        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(obj, e, logger);
        }

    }

    @Override
    public <T> boolean remove(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        String sql = SqlInitFactory.getSql(clz, SqlInit.REMOVE);

        SqliLoggerProxy.debug(clz, keyOne.get());
        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcHelper.remove(sql, keyOne.get());
    }

    @Override
    public boolean create(Object obj) {

        Class clz = obj.getClass();

        try {
            String sql = SqlInitFactory.getSql(clz, SqlInit.CREATE);

            Parsed parsed = Parser.get(clz);

            Long keyOneValue = parsed.tryToGetLongKey(obj);
            boolean isAutoIncreaseId = parsed.isAutoIncreaseId(keyOneValue);

            List<Object> valueList = ObjectDataConverter.objectToListForCreate(obj, parsed.getBeanElementList(), dialect);

            SqliLoggerProxy.debug(clz, valueList);
            SqliLoggerProxy.debug(clz, sql);

            return this.jdbcHelper.create(isAutoIncreaseId,sql,valueList);

        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(obj, e, logger);
        }

    }

    @Override
    public boolean createOrReplace(Object obj) {

        Class clz = obj.getClass();

        try {
            String createSql = SqlInitFactory.getSql(clz, SqlInit.CREATE);
            final String sql = this.dialect.createOrReplaceSql(createSql);

            Parsed parsed = Parser.get(clz);
            List<Object> valueList = ObjectDataConverter.objectToListForCreate(obj, parsed.getBeanElementList(), dialect);

            SqliLoggerProxy.debug(clz, valueList);
            SqliLoggerProxy.debug(clz, sql);

            return this.jdbcHelper.createOrReplace(sql, valueList);

        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(obj, e, logger);
        }
    }


    @Override
    public List<Map<String, Object>> list(String sql, List<Object> conditionList) {

        sql = DaoHelper.filter(sql);

        return this.jdbcHelper.queryForResultMapList(sql, conditionList,null, null,this.dialect);
    }


    @Override
    public <T> T get(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        String sql = SqlInitFactory.getSql(clz, SqlInit.GET_ONE);

        SqliLoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcHelper.queryForList(sql, Arrays.asList(keyOne.get()), Parser.get(clz), this.dialect);

        if (list.isEmpty())
            return null;

        return list.get(0);
    }


    @Override
    public <T> List<T> list(Object conditionObj) {

        Class clz = conditionObj.getClass();
        String sql = SqlInitFactory.getSql(clz, SqlInit.LOAD);
        Parsed parsed = Parser.get(clz);

        Map<String, Object> queryMap = ObjectDataConverter.objectToMap(parsed, conditionObj);
        sql = DaoHelper.concat(parsed, sql, queryMap);
        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcHelper.queryForList(sql, queryMap.values(), parsed, this.dialect);

    }

    @Override
    public <T> List<T> list(Criteria criteria) {

        Class clz = criteria.getClzz();
        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = DaoHelper.fromCriteria(valueList,criteria, criteriaToSql, dialect);
        String sql = sqlBuilt.getSql().toString();
        SqliLoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcHelper.queryForList(sql, valueList, Parser.get(clz), this.dialect);
        ResultSortUtil.sort(list, criteria, Parser.get(clz));
        return list;
    }

    @Override
    public <T> Page<T> find(Criteria criteria) {

        Class clz = criteria.getClzz();
        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = DaoHelper.fromCriteria(valueList,criteria, criteriaToSql, dialect);
        String sql = sqlBuilt.getSql().toString();

        SqliLoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcHelper.queryForList(sql, valueList,Parser.get(clz), this.dialect);
        Parsed parsed = Parser.get(clz);
        ResultSortUtil.sort(list, criteria, parsed);

        Page<T> pagination = PageBuilderHelper.build(criteria, list, () -> getCount(clz, sqlBuilt.getCountSql(), valueList));

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
     * @param clzz
     * @param sql
     */
    @Deprecated
    @Override
    public boolean execute(Class clzz, String sql) {

        Parsed parsed = Parser.get(clzz);

        sql = DaoHelper.filter(sql);
        sql = SqlParserUtil.mapperForNative(sql, parsed);

        SqliLoggerProxy.debug(clzz, sql);

        return this.jdbcHelper.execute(sql);

    }


    @Override
    public boolean refreshByCondition(RefreshCondition refreshCondition) {

        Class clz = refreshCondition.getClz();
        Parsed parsed = Parser.get(clz);
        String sql = DaoHelper.buildRefresh(parsed, refreshCondition, this.criteriaToSql,this.dialect);
        List<Object> valueList = refreshCondition.getValueList();

        SqliLoggerProxy.debug(clz, valueList);
        SqliLoggerProxy.debug(clz, sql);

        return update(sql, valueList, dialect, jdbcHelper);
    }

    @Override
    public <T> boolean refresh(T t) {

        Class clz = t.getClass();
        Object[] arr = DaoHelper.refresh(t,clz);

        String sql = (String)arr[0];
        Collection<Object> valueList = (Collection<Object>)arr[1];
        SqliLoggerProxy.debug(clz, valueList);
        SqliLoggerProxy.debug(clz, sql);

        return update(sql,valueList,dialect, jdbcHelper);
    }


    @Override
    public <T> List<T> in(InCondition inCondition) {

        Class<T> clz = inCondition.getClz();
        Parsed parsed = Parser.get(clz);

        String inProperty = inCondition.getProperty();
        if (SqliStringUtil.isNullOrEmpty(inProperty)) {
            inProperty = parsed.getKey(X.KEY_ONE);
        }

        BeanElement be = parsed.getElementExisted(inProperty);

        String sql = SqlInitFactory.getSql(clz, SqlInit.LOAD);
        String mapper = parsed.getMapper(inProperty);
        List<? extends Object> inList = inCondition.getInList();

        sql = DaoHelper.buildIn(sql, mapper, be, inList);

        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcHelper.queryForList(sql, null, parsed,  this.dialect);
    }

    @Override
    public Page<Map<String, Object>> find(Criteria.ResultMapCriteria resultMapped) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = DaoHelper.fromCriteria(valueList,resultMapped, criteriaToSql, dialect);
        String sql = sqlBuilt.getSql().toString();
        Class clz = resultMapped.getClzz();

        SqliLoggerProxy.debug(clz, sql);

        List<Map<String, Object>> list = this.jdbcHelper.queryForResultMapList(sql, valueList,resultMapped, clz, this.dialect);

        Page<Map<String, Object>> pagination = PageBuilderHelper.build(resultMapped, list, () -> getCount(clz, sqlBuilt.getCountSql(), valueList));

        return pagination;
    }

    @Override
    public List<Map<String, Object>> list(Criteria.ResultMapCriteria resultMapped) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = DaoHelper.fromCriteria(valueList,resultMapped, criteriaToSql, dialect);
        String sql = sqlBuilt.getSql().toString();

        SqliLoggerProxy.debug(resultMapped.getClzz(), sql);

        return this.jdbcHelper.queryForResultMapList(sql, valueList,resultMapped, resultMapped.getClzz(), this.dialect);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria resultMapped){
        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = DaoHelper.fromCriteria(valueList,resultMapped, criteriaToSql, dialect);
        String sql = sqlBuilt.getSql().toString();

        SqliLoggerProxy.debug(resultMapped.getClzz(), sql);

        List<K> list = this.jdbcHelper.queryForPlainValueList(clzz,sql,valueList,this.dialect);
        return list;
    }


    @Override
    public <T> T getOne(T conditionObj) {

        Class clz = conditionObj.getClass();
        String sql = SqlInitFactory.getSql(clz, SqlInit.LOAD);
        Parsed parsed = Parser.get(clz);

        Map<String, Object> queryMap = ObjectDataConverter.objectToMap(parsed, conditionObj);
        sql = DaoHelper.concat(parsed, sql, queryMap);
        sql = DaoHelper.paged(sql, 1, 1, this.dialect);

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
    public void findToHandle(Criteria.ResultMapCriteria resultMapped, RowHandler<Map<String,Object>> handler) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = DaoHelper.fromCriteria(valueList,resultMapped, criteriaToSql, dialect);
        String sql = sqlBuilt.getSql().toString();
        Class clz = resultMapped.getClzz();

        SqliLoggerProxy.debug(clz, sql);

        this.jdbcHelper.queryForMapToHandle(sql, valueList, dialect, resultMapped, null, handler);
    }

    @Override
    public <T> void findToHandle(Criteria criteria, RowHandler<T> handler) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = DaoHelper.fromCriteria(valueList,criteria, criteriaToSql, dialect);
        String sql = sqlBuilt.getSql().toString();
        Class clz = criteria.getClzz();

        SqliLoggerProxy.debug(clz, sql);

        this.jdbcHelper.queryForMapToHandle(sql, valueList, dialect,null, Parser.get(clz), handler);
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
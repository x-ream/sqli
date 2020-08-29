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
package io.xream.sqli.repository.dao;

import io.xream.sqli.annotation.X;
import io.xream.sqli.api.Dialect;
import io.xream.sqli.api.JdbcWrapper;
import io.xream.sqli.api.RowHandler;
import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.InCondition;
import io.xream.sqli.builder.RefreshCondition;
import io.xream.sqli.converter.ObjectDataConverter;
import io.xream.sqli.exception.ExceptionTranslator;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.api.CriteriaToSql;
import io.xream.sqli.repository.api.KeyOne;
import io.xream.sqli.repository.exception.TooManyResultsException;
import io.xream.sqli.repository.mapper.Mapper;
import io.xream.sqli.repository.mapper.MapperFactory;
import io.xream.sqli.repository.util.ResultSortUtil;
import io.xream.sqli.repository.util.SqlParserUtil;
import io.xream.sqli.util.SqliLoggerProxy;
import io.xream.sqli.util.SqliStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Sim
 */
public class DaoImpl implements Dao {

    private Logger logger = LoggerFactory.getLogger(Dao.class);

    private CriteriaToSql criteriaToSql;

    private Dialect dialect;

    private JdbcWrapper jdbcWrapper;

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public void setCriteriaToSql(CriteriaToSql criteriaToSql) {
        this.criteriaToSql = criteriaToSql;
    }

    public void setJdbcWrapper(JdbcWrapper jdbcWrapper) {
        this.jdbcWrapper = jdbcWrapper;
    }

    @Override
    public boolean createBatch(List<? extends Object> objList) {

        if (objList.isEmpty())
            return false;
        Object obj = objList.get(0);
        Class clz = obj.getClass();
        String sql = MapperFactory.getSql(clz, Mapper.CREATE);

        SqliLoggerProxy.debug(clz, sql);

        Parsed parsed = Parser.get(clz);
        JdbcWrapper.BatchObjectValues batchObjectValues =  () -> {
            List<Collection<Object>> valuesList = new ArrayList<>();
            for (Object o : objList) {
                Collection<Object> values= ObjectDataConverter.objectToListForCreate(o, parsed.getBeanElementList(), dialect);
                valuesList.add(values);
            }
            return valuesList;
        };

        final int batchSize = 500;
        try {
            return this.jdbcWrapper.createBatch(clz, sql, batchObjectValues, batchSize, this.dialect);
        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(obj, e, logger);
        }

    }

    @Override
    public <T> boolean remove(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        String sql = MapperFactory.getSql(clz, Mapper.REMOVE);

        SqliLoggerProxy.debug(clz, keyOne.get());
        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.remove(sql, keyOne.get());
    }

    @Override
    public boolean create(Object obj) {

        Class clz = obj.getClass();

        try {
            String sql = MapperFactory.getSql(clz, Mapper.CREATE);

            Parsed parsed = Parser.get(clz);

            Long keyOneValue = parsed.tryToGetLongKey(obj);
            boolean isAutoIncreaseId = parsed.isAutoIncreaseId(keyOneValue);

            List<Object> valueList = ObjectDataConverter.objectToListForCreate(obj, parsed.getBeanElementList(), dialect);

            SqliLoggerProxy.debug(clz, valueList);
            SqliLoggerProxy.debug(clz, sql);

            return this.jdbcWrapper.create(isAutoIncreaseId,sql,valueList);

        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(obj, e, logger);
        }

    }

    @Override
    public boolean createOrReplace(Object obj) {

        Class clz = obj.getClass();

        try {
            String createSql = MapperFactory.getSql(clz, Mapper.CREATE);
            final String sql = this.dialect.createOrReplaceSql(createSql);

            Parsed parsed = Parser.get(clz);
            List<Object> valueList = ObjectDataConverter.objectToListForCreate(obj, parsed.getBeanElementList(), dialect);

            SqliLoggerProxy.debug(clz, valueList);
            SqliLoggerProxy.debug(clz, sql);

            return this.jdbcWrapper.createOrReplace(sql, valueList);

        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(obj, e, logger);
        }
    }


    @Override
    public List<Map<String, Object>> list(Class clz, String sql, List<Object> conditionList) {

        sql = SqlUtil.filter(sql);
        Parsed parsed = Parser.get(clz);
        sql = SqlParserUtil.mapperForManu(sql, parsed);

        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.queryForList(sql, conditionList,parsed, this.dialect);
    }


    @Override
    public <T> T get(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        String sql = MapperFactory.getSql(clz, Mapper.GET_ONE);

        SqliLoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcWrapper.queryForList(sql, Arrays.asList(keyOne.get()), Parser.get(clz), this.dialect);

        if (list.isEmpty())
            return null;

        return list.get(0);
    }


    @Override
    public <T> List<T> list(Object conditionObj) {

        Class clz = conditionObj.getClass();
        String sql = MapperFactory.getSql(clz, Mapper.LOAD);
        Parsed parsed = Parser.get(clz);

        Map<String, Object> queryMap = ObjectDataConverter.objectToMapForQuery(parsed, conditionObj);
        sql = SqlUtil.concat(parsed, sql, queryMap);
        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.queryForList(sql, queryMap.values(), parsed, this.dialect);

    }

    @Override
    public <T> List<T> list(Criteria criteria) {

        Class clz = criteria.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(criteria, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();
        SqliLoggerProxy.debug(clz, sql);

        List<Object> valueList = criteria.getValueList();
        List<T> list = this.jdbcWrapper.queryForList(sql, valueList, Parser.get(clz), this.dialect);
        ResultSortUtil.sort(list, criteria, Parser.get(clz));
        return list;
    }

    @Override
    public <T> Page<T> find(Criteria criteria) {

        Class clz = criteria.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(criteria, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();

        SqliLoggerProxy.debug(clz, sql);

        List<Object> valueList = criteria.getValueList();
        List<T> list = this.jdbcWrapper.queryForList(sql, valueList,Parser.get(clz), this.dialect);
        Parsed parsed = Parser.get(clz);
        ResultSortUtil.sort(list, criteria, parsed);

        Page<T> pagination = PageBuilder.build(criteria, list, () -> getCount(clz, sqlParsed.getCountSql(), valueList));

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
        return this.jdbcWrapper.queryForPlainValueList(Long.class,sql,list,this.dialect).get(0);
    }


    /**
     * 没有特殊需求，请不要调用此代码
     *
     * @param obj
     * @param sql
     */
    @Deprecated
    @Override
    public boolean execute(Object obj, String sql) {

        Class clz = obj.getClass();
        Parsed parsed = Parser.get(obj.getClass());

        sql = SqlUtil.filter(sql);
        sql = SqlParserUtil.mapperForManu(sql, parsed);

        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.execute(sql);

    }


    @Override
    public boolean refreshByCondition(RefreshCondition refreshCondition) {

        Class clz = refreshCondition.getClz();
        Parsed parsed = Parser.get(clz);
        String sql = SqlUtil.buildRefresh(parsed, refreshCondition, this.criteriaToSql);
        List<Object> valueList = refreshCondition.getValueList();

        SqliLoggerProxy.debug(clz, valueList);
        SqliLoggerProxy.debug(clz, sql);

        return update(sql, valueList, dialect, jdbcWrapper);
    }

    @Override
    public <T> boolean refresh(T t) {

        Class clz = t.getClass();
        Object[] arr = SqlUtil.refresh(t,clz);

        String sql = (String)arr[0];
        Collection<Object> valueList = (Collection<Object>)arr[1];
        SqliLoggerProxy.debug(clz, valueList);
        SqliLoggerProxy.debug(clz, sql);

        return update(sql,valueList,dialect,jdbcWrapper);
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

        String sql = MapperFactory.getSql(clz, Mapper.LOAD);
        String mapper = parsed.getMapper(inProperty);
        List<? extends Object> inList = inCondition.getInList();

        sql = SqlUtil.buildIn(sql, mapper, be, inList);

        SqliLoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.queryForList(sql, null, parsed,  this.dialect);
    }

    @Override
    public Page<Map<String, Object>> find(Criteria.ResultMapCriteria resultMapped) {

        Class clz = resultMapped.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(resultMapped, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();

        SqliLoggerProxy.debug(clz, sql);

        List<Map<String, Object>> list = this.jdbcWrapper.queryForResultMapList(sql, resultMapped, this.dialect);

        Page<Map<String, Object>> pagination = PageBuilder.build(resultMapped, list, () -> getCount(clz, sqlParsed.getCountSql(), resultMapped.getValueList()));

        return pagination;
    }

    @Override
    public List<Map<String, Object>> list(Criteria.ResultMapCriteria resultMapped) {

        SqlParsed sqlParsed = SqlUtil.fromCriteria(resultMapped, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();

        SqliLoggerProxy.debug(resultMapped.getClz(), sql);

        return this.jdbcWrapper.queryForResultMapList(sql, resultMapped, this.dialect);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria resultMapped){

        SqlParsed sqlParsed = SqlUtil.fromCriteria(resultMapped, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();

        SqliLoggerProxy.debug(resultMapped.getClz(), sql);

        List<K> list = this.jdbcWrapper.queryForPlainValueList(clzz,sql,resultMapped.getValueList(),this.dialect);
        return list;
    }


    @Override
    public <T> T getOne(T conditionObj) {

        Class clz = conditionObj.getClass();
        String sql = MapperFactory.getSql(clz, Mapper.LOAD);
        Parsed parsed = Parser.get(clz);

        Map<String, Object> queryMap = ObjectDataConverter.objectToMapForQuery(parsed, conditionObj);
        sql = SqlUtil.concat(parsed, sql, queryMap);
        sql = SqlUtil.paged(sql, 1, 1, this.dialect);

        SqliLoggerProxy.debug(clz, sql);

        if (queryMap.isEmpty())
            throw new IllegalArgumentException("API of getOne(T) can't accept blank object: " + conditionObj);

        List<T> list = this.jdbcWrapper.queryForList(sql, queryMap.values(),parsed, this.dialect);

        if (list.isEmpty())
            return null;
        if (list.size() > 1)
            throw new TooManyResultsException("Expected one result (or null) to be returned by API of getOne(T), but found: " + list.size());
        return list.get(0);
    }

    @Override
    public void findToHandle(Criteria.ResultMapCriteria resultMapped, RowHandler<Map<String,Object>> handler) {

        Class clz = resultMapped.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(resultMapped, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();
        SqliLoggerProxy.debug(clz, sql);

        List<Object> valueList = resultMapped.getValueList();

        this.jdbcWrapper.queryForMapToHandle(sql, valueList, dialect, resultMapped, null, handler);
    }

    @Override
    public <T> void findToHandle(Criteria criteria, RowHandler<T> handler) {

        Class clz = criteria.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(criteria, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();
        SqliLoggerProxy.debug(clz, sql);

        List<Object> valueList = criteria.getValueList();

        this.jdbcWrapper.queryForMapToHandle(sql, valueList, dialect,null, Parser.get(clz), handler);
    }


    private boolean update(String sql, Collection<Object> list, Dialect dialect, JdbcWrapper jdbcWrapper) {
        try {
            Object[] arr = dialect.toArr(list);
            return jdbcWrapper.refresh(sql,arr);
        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(null, e, logger);
        }
    }

}
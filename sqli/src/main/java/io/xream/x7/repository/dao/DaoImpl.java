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
package io.xream.x7.repository.dao;

import io.xream.sqli.core.builder.*;
import io.xream.sqli.core.builder.condition.InCondition;
import io.xream.sqli.core.builder.condition.RefreshCondition;
import io.xream.sqli.api.Dialect;
import io.xream.sqli.api.JdbcWrapper;
import io.xream.sqli.annotation.X;
import io.xream.sqli.core.util.LoggerProxy;
import io.xream.sqli.core.util.SqlStringUtil;
import io.xream.sqli.core.web.Page;
import io.xream.sqli.exception.ExceptionTranslator;
import io.xream.x7.repository.CriteriaToSql;
import io.xream.x7.repository.KeyOne;
import io.xream.x7.repository.SqlParsed;
import io.xream.x7.repository.exception.TooManyResultsException;
import io.xream.x7.repository.mapper.DataObjectConverter;
import io.xream.x7.repository.mapper.Mapper;
import io.xream.x7.repository.mapper.MapperFactory;
import io.xream.x7.repository.util.ResultSortUtil;
import io.xream.x7.repository.util.SqlParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

        LoggerProxy.debug(clz, sql);

        Parsed parsed = Parser.get(clz);
        final int batchSize = 500;
        try {
            return this.jdbcWrapper.createBatch(clz, sql, objList, batchSize, this.dialect);

        } catch (Exception e) {
            throw ExceptionTranslator.onRollback(obj, e, logger);
        }

    }

    @Override
    public <T> boolean remove(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        String sql = MapperFactory.getSql(clz, Mapper.REMOVE);

        LoggerProxy.debug(clz, keyOne.get());
        LoggerProxy.debug(clz, sql);

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

            List<Object> valueList = DataObjectConverter.objectToListForCreate(obj, parsed.getBeanElementList(), dialect);

            LoggerProxy.debug(clz, valueList);
            LoggerProxy.debug(clz, sql);

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
            List<Object> valueList = DataObjectConverter.objectToListForCreate(obj, parsed.getBeanElementList(), dialect);

            LoggerProxy.debug(clz, valueList);
            LoggerProxy.debug(clz, sql);

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

        LoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.queryForList(sql, clz, conditionList, this.dialect);
    }


    @Override
    public <T> T get(KeyOne<T> keyOne) {

        Class clz = keyOne.getClzz();
        String sql = MapperFactory.getSql(clz, Mapper.GET_ONE);

        LoggerProxy.debug(clz, sql);

        List<T> list = this.jdbcWrapper.queryForList(sql, keyOne.getClzz(), Arrays.asList(keyOne.get()), this.dialect);

        if (list.isEmpty())
            return null;

        return list.get(0);
    }


    @Override
    public <T> List<T> list(Object conditionObj) {

        Class clz = conditionObj.getClass();
        String sql = MapperFactory.getSql(clz, Mapper.LOAD);
        Parsed parsed = Parser.get(clz);

        Map<String, Object> queryMap = DataObjectConverter.objectToMapForQuery(parsed, conditionObj);
        sql = SqlUtil.concat(parsed, sql, queryMap);
        LoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.queryForList(sql, clz, queryMap.values(), this.dialect);

    }

    @Override
    public <T> List<T> list(Criteria criteria) {

        Class clz = criteria.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(criteria, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();
        LoggerProxy.debug(clz, sql);

        List<Object> valueList = criteria.getValueList();
        List<T> list = this.jdbcWrapper.queryForList(sql, clz, valueList, this.dialect);
        ResultSortUtil.sort(list, criteria, Parser.get(clz));
        return list;
    }

    @Override
    public <T> Page<T> find(Criteria criteria) {

        Class clz = criteria.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(criteria, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();

        LoggerProxy.debug(clz, sql);

        List<Object> valueList = criteria.getValueList();
        List<T> list = this.jdbcWrapper.queryForList(sql, clz, valueList, this.dialect);
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
        LoggerProxy.debug(clz, sql);
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

        LoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.execute(sql);

    }


    @Override
    public boolean refreshByCondition(RefreshCondition refreshCondition) {

        Class clz = refreshCondition.getClz();
        Parsed parsed = Parser.get(clz);
        String sql = SqlUtil.buildRefresh(parsed, refreshCondition, this.criteriaToSql);
        List<Object> valueList = refreshCondition.getValueList();

        LoggerProxy.debug(clz, valueList);
        LoggerProxy.debug(clz, sql);

        return update(sql, valueList, dialect, jdbcWrapper);
    }

    @Override
    public <T> boolean refresh(T t) {

        Class clz = t.getClass();
        Object[] arr = SqlUtil.refresh(t,clz);

        String sql = (String)arr[0];
        Collection<Object> valueList = (Collection<Object>)arr[1];
        LoggerProxy.debug(clz, valueList);
        LoggerProxy.debug(clz, sql);

        return update(sql,valueList,dialect,jdbcWrapper);
    }


    @Override
    public <T> List<T> in(InCondition inCondition) {

        Class<T> clz = inCondition.getClz();
        Parsed parsed = Parser.get(clz);

        String inProperty = inCondition.getProperty();
        if (SqlStringUtil.isNullOrEmpty(inProperty)) {
            inProperty = parsed.getKey(X.KEY_ONE);
        }

        BeanElement be = parsed.getElementExisted(inProperty);

        String sql = MapperFactory.getSql(clz, Mapper.LOAD);
        String mapper = parsed.getMapper(inProperty);
        List<? extends Object> inList = inCondition.getInList();

        sql = SqlUtil.buildIn(sql, mapper, be, inList);

        LoggerProxy.debug(clz, sql);

        return this.jdbcWrapper.queryForList(sql, clz, null, this.dialect);
    }

    @Override
    public Page<Map<String, Object>> find(Criteria.ResultMappedCriteria resultMapped) {

        Class clz = resultMapped.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(resultMapped, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();

        LoggerProxy.debug(clz, sql);

        List<Map<String, Object>> list = this.jdbcWrapper.queryForMapList(sql, resultMapped, this.dialect);

        Page<Map<String, Object>> pagination = PageBuilder.build(resultMapped, list, () -> getCount(clz, sqlParsed.getCountSql(), resultMapped.getValueList()));

        return pagination;
    }

    @Override
    public List<Map<String, Object>> list(Criteria.ResultMappedCriteria resultMapped) {

        SqlParsed sqlParsed = SqlUtil.fromCriteria(resultMapped, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();

        LoggerProxy.debug(resultMapped.getClz(), sql);

        return this.jdbcWrapper.queryForMapList(sql, resultMapped, this.dialect);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMappedCriteria resultMapped){

        SqlParsed sqlParsed = SqlUtil.fromCriteria(resultMapped, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();

        LoggerProxy.debug(resultMapped.getClz(), sql);

        List<K> list = this.jdbcWrapper.queryForPlainValueList(clzz,sql,resultMapped.getValueList(),this.dialect);
        return list;
    }


    @Override
    public <T> T getOne(T conditionObj) {

        Class clz = conditionObj.getClass();
        String sql = MapperFactory.getSql(clz, Mapper.LOAD);
        Parsed parsed = Parser.get(clz);

        Map<String, Object> queryMap = DataObjectConverter.objectToMapForQuery(parsed, conditionObj);
        sql = SqlUtil.concat(parsed, sql, queryMap);
        sql = SqlUtil.paged(sql, 1, 1, this.dialect);

        LoggerProxy.debug(clz, sql);

        if (queryMap.isEmpty())
            throw new IllegalArgumentException("API of getOne(T) can't accept blank object: " + conditionObj);

        List<T> list = this.jdbcWrapper.queryForList(sql, clz, queryMap.values(), this.dialect);

        if (list.isEmpty())
            return null;
        if (list.size() > 1)
            throw new TooManyResultsException("Expected one result (or null) to be returned by API of getOne(T), but found: " + list.size());
        return list.get(0);
    }

    @Override
    public void findToHandle(Criteria.ResultMappedCriteria resultMapped, RowHandler<Map<String,Object>> handler) {

        Class clz = resultMapped.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(resultMapped, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();
        LoggerProxy.debug(clz, sql);

        List<Object> valueList = resultMapped.getValueList();

        this.jdbcWrapper.queryForMapToHandle(clz, sql, valueList, dialect, resultMapped, handler);
    }

    @Override
    public <T> void findToHandle(Criteria criteria, RowHandler<T> handler) {

        Class clz = criteria.getClz();
        SqlParsed sqlParsed = SqlUtil.fromCriteria(criteria, criteriaToSql, dialect);
        String sql = sqlParsed.getSql().toString();
        LoggerProxy.debug(clz, sql);

        List<Object> valueList = criteria.getValueList();

        this.jdbcWrapper.queryForMapToHandle(clz, sql, valueList, dialect,null, handler);
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
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
import io.xream.sqli.converter.ObjectDataConverter;
import io.xream.sqli.core.Dialect;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.starter.DbType;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @Author Sim
 */
public final class DaoHelper {

    protected static String paged(String sql, int page, int rows, Dialect dialect) {
        int start = (page - 1) * rows;
        return dialect.buildPage(sql, start, rows);
    }

    /**
     * 拼接SQL
     */
    protected static String concat(Parsed parsed, String sql, Map<String, Object> queryMap) {

        StringBuilder sb = new StringBuilder();

        boolean flag = (sql.contains(SqlScript.WHERE) || sql.contains(SqlScript.WHERE.toLowerCase()));

        for (String key : queryMap.keySet()) {

            String mapper = parsed.getMapper(key);
            if (flag) {
                sb.append(Op.AND.sql()).append(mapper).append(SqlScript.EQ_PLACE_HOLDER);
            } else {
                sb.append(SqlScript.WHERE).append(mapper).append(SqlScript.EQ_PLACE_HOLDER);
                flag = true;
            }

        }

        sql += sb.toString();

        return sql;
    }


    protected static String buildRefresh(Parsed parsed, RefreshCondition refreshCondition, CriteriaToSql criteriaParser, DialectSupport dialectSupport) {
        return criteriaParser.toSql(parsed,refreshCondition, dialectSupport);
    }

    protected static String concatRefresh(StringBuilder sb, Parsed parsed, Map<String, Object> refreshMap) {

        sb.append(SqlScript.SET);
        int size = refreshMap.size();
        int i = 0;
        for (String key : refreshMap.keySet()) {

            BeanElement element = parsed.getElement(key);
            if (element.isJson() && "oracle".equals(DbType.value())){
                Object obj = refreshMap.get(key);
                Reader reader = new StringReader(obj.toString());
                refreshMap.put(key,reader);
            }

            String mapper = parsed.getMapper(key);
            sb.append(mapper);
            sb.append(SqlScript.EQ_PLACE_HOLDER);
            if (i < size - 1) {
                sb.append(SqlScript.COMMA);
            }
            i++;
        }

        String keyOne = parsed.getKey(X.KEY_ONE);

        sb.append(SqlScript.WHERE);
        String mapper = parsed.getMapper(keyOne);
        sb.append(mapper).append(SqlScript.EQ_PLACE_HOLDER);

        return sb.toString();
    }

    protected static String buildIn(String sqlSegment, String mapper, BeanElement be, List<? extends Object> inList) {

        StringBuilder sb = new StringBuilder();
        sb.append(sqlSegment).append(SqlScript.WHERE);
        sb.append(mapper).append(SqlScript.IN);//" IN "

        Class<?> keyType = be.getClz();

        ConditionToSql.buildIn(sb,keyType,inList);

        return sb.toString();
    }

    protected static SqlBuilt fromCriteria(List<Object> valueList, Criteria criteria, CriteriaToSql criteriaParser, Dialect dialect) {

        final SqlBuilt sqlBuilt = new SqlBuilt();
        final List<SqlBuilt> subList = new ArrayList<>();

        criteriaParser.toSql(false, criteria, sqlBuilt, new SqlBuildingAttached() {
            @Override
            public List<Object> getValueList() {
                return valueList;
            }

            @Override
            public List<SqlBuilt> getSubList() {
                return subList;
            }
        });

        String sql = sqlBuilt.getSql().toString();

        int page = criteria.getPage();
        int rows = criteria.getRows();

        int start = (page - 1) * rows;

        sql = dialect.buildPage(sql, start, rows);

        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sqlBuilt.setSql(sb);
        ObjectDataConverter.log(criteria.getClzz(), valueList);

        return sqlBuilt;
    }

    protected static String filter(String sql) {
        sql = sql.replace("drop", SqlScript.SPACE)
                .replace(";", SqlScript.SPACE);// 手动拼接SQL,
        return sql;
    }


    public static <T> Object[] refresh(T t, Class<T> clz) {

        Parsed parsed = Parser.get(clz);
        String tableName = parsed.getTableName();

        StringBuilder sb = new StringBuilder();
        sb.append(SqlScript.UPDATE).append(SqlScript.SPACE).append(tableName).append(SqlScript.SPACE);

        Map<String, Object> refreshMap = ObjectDataConverter.objectToMap(parsed, t);

        String keyOne = parsed.getKey(X.KEY_ONE);
        Object keyOneValue = refreshMap.remove(keyOne);

        String sql = concatRefresh(sb, parsed, refreshMap);

        List<Object> valueList = new ArrayList<>();
        valueList.addAll(refreshMap.values());
        valueList.add(keyOneValue);

        return new Object[]{sql,valueList};
    }
}

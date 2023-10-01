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

import io.xream.sqli.builder.*;
import io.xream.sqli.builder.internal.*;
import io.xream.sqli.converter.ObjectDataConverter;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Sim
 */
public final class SqlBuilder implements BbQToSql {

    private static SqlBuilder instance;
    private SqlBuilder(){}
    protected static SqlBuilder getInstance() {
        if (instance == null) {
            instance = new SqlBuilder();
        }
        return instance;
    }

    protected String buildPageSql(String sql, int page, int rows, Dialect dialect) {
        int start = (page - 1) * rows;

        return dialect.buildPageSql(sql, start, rows,0);
    }

    /**
     * 拼接SQL
     */
    protected String buildQueryByObject(Parsed parsed, String sql, Map<String, Object> queryMap) {

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


    protected String buildRefreshByCondition(Parsed parsed, Qr qr, Q2Sql q2Sql, DialectSupport dialectSupport) {
        return q2Sql.toSql(parsed,qr, dialectSupport);
    }


    protected String buildQueryByInCondition(String sqlSegment, String mapper, BeanElement be, List<? extends Object> inList) {

        StringBuilder sb = new StringBuilder();
        sb.append(sqlSegment).append(SqlScript.WHERE);
        sb.append(mapper).append(SqlScript.IN);//" IN "

        Class<?> keyType = be.getClz();

        buildIn(sb,keyType,inList);

        return sb.toString();
    }

    protected String buildQueryByInCondition(String sqlSegment, Class<?> keyType, List<? extends Object> inList) {

        StringBuilder sb = new StringBuilder();
        sb.append(sqlSegment);

        buildIn(sb,keyType,inList);

        return sb.toString();
    }

    protected SqlBuilt buildQueryByQ(List<Object> valueList, Q q, Q2Sql qParser, Dialect dialect) {

        final SqlBuilt sqlBuilt = new SqlBuilt();
        final List<SqlBuilt> subList = new ArrayList<>();

        qParser.toSql(false, q, sqlBuilt, new SqlBuildingAttached() {
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

        int page = q.getPage();
        int rows = q.getRows();

        int start = (page - 1) * rows;
        long last = q.getLast();

        sql = dialect.buildPageSql(sql, start, rows,last);

        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sqlBuilt.setSql(sb);
        ObjectDataConverter.log(q, valueList);

        return sqlBuilt;
    }

    protected <T> Object[] buildRefreshSqlAndValueListByObject(T t, Class<T> clz, Dialect dialect) {

        Parsed parsed = Parser.get(clz);
        String tableName = parsed.getTableName();

        StringBuilder sb = new StringBuilder();
        sb.append(SqlScript.UPDATE).append(SqlScript.SPACE).append(tableName).append(SqlScript.SPACE);

        Map<String, Object> refreshMap = ObjectDataConverter.objectToMap(parsed, t);

        String keyOne = parsed.getKey();
        Object keyOneValue = refreshMap.remove(keyOne);

        String sql = concatRefresh(sb, parsed, refreshMap,dialect);

        List<Object> valueList = new ArrayList<>();
        valueList.addAll(refreshMap.values());
        valueList.add(keyOneValue);

        return new Object[]{sql,valueList};
    }


    private String concatRefresh(StringBuilder sb, Parsed parsed, Map<String, Object> refreshMap, Dialect dialect) {

        sb.append(SqlScript.SET);
        int size = refreshMap.size();
        int i = 0;
        for (String key : refreshMap.keySet()) {

            BeanElement element = parsed.getElement(key);
            if (element.isJson() ){
                Object json = refreshMap.get(key);
                Object o = dialect.convertJsonToPersist(json);
                refreshMap.put(key,o);
            }

            String mapper = parsed.getMapper(key);
            sb.append(mapper);
            sb.append(SqlScript.EQ_PLACE_HOLDER);
            if (i < size - 1) {
                sb.append(SqlScript.COMMA);
            }
            i++;
        }

        String keyOne = parsed.getKey();

        sb.append(SqlScript.WHERE);
        String mapper = parsed.getMapper(keyOne);
        sb.append(mapper).append(SqlScript.EQ_PLACE_HOLDER);

        return sb.toString();
    }
}

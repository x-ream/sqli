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
package io.xream.sqli.repository.init;

import io.xream.sqli.annotation.X;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.util.SqlParserUtil;
import io.xream.sqli.util.BeanUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Author Sim
 */
public interface SqlTemplate {

    Map<Class, Map<String, String>> SQLS_MAP = new HashMap<>();

    String CREATE = "CREATE";
    String REMOVE = "REMOVE";
    String LOAD = "LOAD";
    String CREATE_TABLE = "CREATE_TABLE";
    String GET_ONE = "GET_ONE";

    Dialect getDialect();
    void setDialect(Dialect dialect);

    default Map<String,String> getSqlMap(Class clzz) {
        Map<String, String> sqlMap = SQLS_MAP.get(clzz);
        if (sqlMap == null) {
            sqlMap = new HashMap<>();
            SQLS_MAP.put(clzz, sqlMap);
        }
        return sqlMap;
    }

    default String getSql(Class clzz, String type) {
        return getSqlMap(clzz).get(type);
    }

    default String buildTableSql(Class clz, boolean isTemporary) {
        Parsed parsed = Parser.get(clz);
        List<BeanElement> temp = parsed.getBeanElementList();
        Map<String, BeanElement> map = new HashMap<String, BeanElement>();
        List<BeanElement> list = new ArrayList<BeanElement>();
        for (BeanElement be : temp) {
            if (be.getSqlType() != null && be.getSqlType().equals("text")) {
                list.add(be);
                continue;
            }
            map.put(be.getProperty(), be);
        }

        final String keyOne = parsed.getKey(X.KEY_ONE);

        StringBuilder sb = new StringBuilder();
        if (isTemporary) {
            sb.append(getDialect().getTemporaryTableCreate());
        } else {
            sb.append("CREATE TABLE IF NOT EXISTS ");
        }
        sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(" (")
                .append("\n");

        sb.append("   ").append(keyOne);

        BeanElement be = map.get(keyOne);
        String sqlType = getSqlTypeRegX(be);

        if (sqlType.equals(Dialect.INT)) {
            sb.append(" ").append(Dialect.INT + " NOT NULL");
        } else if (sqlType.equals(Dialect.LONG)) {
            sb.append(" ").append(Dialect.LONG + " NOT NULL");
        } else if (sqlType.equals(Dialect.STRING)) {
            sb.append(" ").append(Dialect.STRING).append("(").append(be.getLength()).append(") NOT NULL");
        }

        sb.append(", ");// FIXME ORACLE

        sb.append("\n");
        map.remove(keyOne);

        for (BeanElement bet : map.values()) {
            sqlType = getSqlTypeRegX(bet);
            sb.append("   ").append(bet.getProperty()).append(" ");

            sb.append(sqlType);

            if (sqlType.equals(Dialect.BIG)) {
                sb.append(" DEFAULT 0.00 ");
            } else if (sqlType.equals(Dialect.DATE)) {
                sb.append(" NULL");

            } else if (BeanUtil.isEnum(bet.getClz())) {
                sb.append("(").append(bet.getLength()).append(") NOT NULL");
            } else if (sqlType.equals(Dialect.STRING)) {
                sb.append("(").append(bet.getLength()).append(") NULL");
            } else {
                Class clzz = bet.getClz();
                if (clzz == Boolean.class || clzz == boolean.class || clzz == Integer.class
                        || clzz == int.class || clzz == Long.class || clzz == long.class) {
                    sb.append(" DEFAULT 0");
                } else {
                    sb.append(" DEFAULT NULL");
                }
            }
            sb.append(",").append("\n");
        }

        for (BeanElement bet : list) {
            sqlType = getSqlTypeRegX(bet);
            sb.append("   ").append(bet.getProperty()).append(" ").append(sqlType).append(",").append("\n");
        }

        sb.append("   PRIMARY KEY ( ").append(keyOne).append(" )");

        sb.append("\n");
        sb.append(") ").append(" ").append(Dialect.ENGINE).append(";");
        String sql = sb.toString();
        sql = getDialect().replaceAll(sql);
        sql = SqlParserUtil.mapper(sql, Parser.get(clz));
        getSqlMap(clz).put(CREATE_TABLE, sql);
        return sql;
    }

    default String getSqlTypeRegX(BeanElement be) {

        Class clz = be.getClz();
        if (clz == Date.class || clz == java.sql.Date.class || clz == java.sql.Timestamp.class) {
            return Dialect.DATE;
        } else if (clz == String.class) {
            return Dialect.STRING;
        } else if (BeanUtil.isEnum(clz)) {
            return Dialect.STRING;
        } else if (clz == int.class || clz == Integer.class) {
            return Dialect.INT;
        } else if (clz == long.class || clz == Long.class) {
            return Dialect.LONG;
        } else if (clz == double.class || clz == Double.class) {
            return Dialect.BIG;
        } else if (clz == float.class || clz == Float.class) {
            return Dialect.BIG;
        } else if (clz == BigDecimal.class) {
            return Dialect.BIG;
        } else if (clz == boolean.class || clz == Boolean.class) {
            return Dialect.BYTE;
        } else if (clz == short.class || clz == Short.class) {
            return Dialect.INT;
        } else if (clz == byte.class || clz == Byte.class) {
            return Dialect.BYTE;
        }
        return Dialect.TEXT;
    }
}

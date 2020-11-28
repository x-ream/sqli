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
package io.xream.sqli.dialect;


import io.xream.sqli.builder.DialectSupport;
import io.xream.sqli.builder.PageSqlSupport;
import io.xream.sqli.core.ValuePost;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.BeanUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author Sim
 */
public interface Dialect extends DialectSupport, PageSqlSupport, ValuePost {

    String DATE = "${DATE}";
    String BYTE = "${BYTE}";
    String INT = "${INT}";
    String LONG = "${LONG}";
    String BIG = "${BIG}";
    String STRING = "${STRING}";
    String TEXT = "${TEXT}";
    String LONG_TEXT = "${LONG_TEXT}";
    String INCREAMENT = "${INCREAMENT}";
    String ENGINE = "${ENGINE}";

    String replaceAll(String sql);

    String transformAlia(String mapper, Map<String, String> aliaMap, Map<String, String> resultKeyAliaMap);

    Object[] toArr(Collection<Object> list);

    Object mappingToObject(Object obj, BeanElement element);

    String createOrReplaceSql(String sql);

    String createSql(Parsed parsed, List<BeanElement> tempList);

    String buildTableSql(Class clzz, boolean isTemporary);

    default String replace(String originSql, Map<String, String> map) {
        String dateV = map.get(DATE);
        String byteV = map.get(BYTE);
        String intV = map.get(INT);
        String longV = map.get(LONG);
        String bigV = map.get(BIG);
        String textV = map.get(TEXT);
        String longTextV = map.get(LONG_TEXT);
        String stringV = map.get(STRING);
        String increamentV = map.get(INCREAMENT);
        String engineV = map.get(ENGINE);

        return originSql.replace(DATE, dateV).replace(BYTE, byteV).replace(INT, intV)
                .replace(LONG, longV).replace(BIG, bigV).replace(TEXT, textV)
                .replace(LONG_TEXT, longTextV).replace(STRING, stringV)
                .replace(INCREAMENT, increamentV).replace(ENGINE, engineV);
    }

    default String getSqlTypeRegX(BeanElement be) {

        Class clz = be.getClz();
        if (clz == Date.class || clz == java.sql.Date.class || clz == java.sql.Timestamp.class) {
            return DATE;
        } else if (clz == String.class) {
            return STRING;
        } else if (BeanUtil.isEnum(clz)) {
            return STRING;
        } else if (clz == int.class || clz == Integer.class) {
            return INT;
        } else if (clz == long.class || clz == Long.class) {
            return LONG;
        } else if (clz == double.class || clz == Double.class) {
            return BIG;
        } else if (clz == float.class || clz == Float.class) {
            return BIG;
        } else if (clz == BigDecimal.class) {
            return BIG;
        } else if (clz == boolean.class || clz == Boolean.class) {
            return BYTE;
        } else if (clz == short.class || clz == Short.class) {
            return INT;
        } else if (clz == byte.class || clz == Byte.class) {
            return BYTE;
        }
        return TEXT;
    }

    default String getDefaultCreateSql(Parsed parsed, List<BeanElement> tempList) {
        String space = " ";
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");

        sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);
        sb.append("(");
        int size = tempList.size();
        for (int i = 0; i < size; i++) {
            String p = tempList.get(i).getProperty();

            sb.append(" ").append(p).append(" ");
            if (i < size - 1) {
                sb.append(",");
            }
        }

        sb.append(") VALUES (");

        for (int i = 0; i < size; i++) {

            sb.append("?");
            if (i < size - 1) {
                sb.append(",");
            }
        }
        sb.append(")");

        return sb.toString();
    }
}
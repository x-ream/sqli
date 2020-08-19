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
package io.xream.x7.repository.util;

import io.xream.sqli.core.builder.BeanElement;
import io.xream.sqli.core.builder.Parsed;
import io.xream.sqli.core.util.BeanUtil;
import io.xream.sqli.core.util.JsonWrapper;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SqlParserUtil {

    public final static String COMMA = ",";
    public final static String SPACE = " ";
    public final static String SQL_KEYWORD_MARK = "`";

    /**
     *
     * @param parsed
     * @param obj
     */
    public static Map<String, Object> getRefreshMap(Parsed parsed, Object obj) {

        Map<String, Object> map = new HashMap<String, Object>();

        if (Objects.isNull(obj))
            return map;

        Class clz = obj.getClass();

        try {
            for (BeanElement element : parsed.getBeanElementList()) {

                Method method = element.getMethod;
                Object value = method.invoke(obj);
                Class type = method.getReturnType();
                String property = element.getProperty();
                if (type == int.class) {
                    if ((int) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Integer.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == long.class) {
                    if ((long) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Long.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == double.class) {
                    if ((double) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Double.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == float.class) {
                    if ((float) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Float.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == boolean.class) {
                    if ((boolean) value != false) {
                        map.put(property, value);
                    }
                } else if (type == Boolean.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == String.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (BeanUtil.isEnum(type)){
                    if (value != null) {
                        map.put(property, ((Enum)value).name());
                    }
                }else if (type == Date.class || clz == java.sql.Date.class || type == Timestamp.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == BigDecimal.class){
                    if (value != null) {
                        map.put(property, value);
                    }
                }else if (element.isJson) {

                    if (value != null) {
                        String str = JsonWrapper.toJson(value);
                        map.put(property, str);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;

    }


    public static String mapperForManu(String sql, Parsed parsed) {

        sql = mapper(sql,parsed);

        if (parsed.isNoSpec())
            return sql;

        if (!sql.contains(COMMA))
            return sql;

        for (String property : parsed.getPropertyMapperMap().keySet()){//FIXME 解析之后, 替换,拼接
            String key = SPACE+property+COMMA;
            String value = SPACE+parsed.getMapper(property)+COMMA;
            sql = sql.replaceAll(key, value);
        }
        for (String property : parsed.getPropertyMapperMap().keySet()){//FIXME 解析之后, 替换,拼接
            String key = COMMA+property+COMMA;
            String value = COMMA+parsed.getMapper(property)+COMMA;
            sql = sql.replaceAll(key, value);
        }
        return sql;
    }


    public static String mapper(String sql, Parsed parsed) {

        if (parsed.isNoSpec())
            return sql;

        sql = mapperName(sql, parsed);

        boolean flag = sql.contains(SQL_KEYWORD_MARK);
        for (String property : parsed.getPropertyMapperMap().keySet()){//FIXME 解析之后, 替换,拼接
            if (flag){
                String key = SQL_KEYWORD_MARK+property+SQL_KEYWORD_MARK;
                if (sql.contains(key)) {
                    String value = parsed.getMapper(property);
                    if (!value.startsWith(SQL_KEYWORD_MARK)) {
                        value = SQL_KEYWORD_MARK + parsed.getMapper(property) + SQL_KEYWORD_MARK;
                    }
                    sql = sql.replace(key, value);
                    continue;
                }
            }
            String key = SPACE + property + SPACE;
            String value = SPACE + parsed.getMapper(property) + SPACE;
            if (!sql.startsWith(SPACE)){
                sql = SPACE + sql;
            }
            sql = sql.replaceAll(key, value);
        }
        return sql;
    }


    public static String mapperName(String sql, Parsed parsed) {

        String clzName = parsed.getClzName();
        clzName = BeanUtil.getByFirstLower(clzName);
        String tableName = parsed.getTableName();

        return mapperName (sql, clzName, tableName);
    }

    public static String mapperName(String sql, String clzName, String tableName) {

        if (sql.endsWith(clzName)){
            sql += SPACE;
        }
        sql = sql.replace(SPACE +clzName+SPACE, SPACE+tableName+SPACE);
        if (sql.contains(SQL_KEYWORD_MARK)) {
            sql = sql.replace(SQL_KEYWORD_MARK +clzName+SQL_KEYWORD_MARK, SQL_KEYWORD_MARK+tableName+SQL_KEYWORD_MARK);
        }

        return sql;
    }
}

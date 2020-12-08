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
package io.xream.sqli.repository.util;

import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.BeanUtil;

/**
 * @Author Sim
 */
public final class SqlParserUtil {

    public final static String SPACE = " ";
    public final static String SQL_KEYWORD_MARK = "`";


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
        sql = sql.trim();
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

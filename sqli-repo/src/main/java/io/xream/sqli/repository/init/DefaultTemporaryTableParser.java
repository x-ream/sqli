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

import io.xream.sqli.api.TemporaryRepository;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.util.SqlParserUtil;
import io.xream.sqli.spi.Schema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Sim
 */
public final class DefaultTemporaryTableParser implements TemporaryRepository.Parser, SqlTemplate {

    private static DefaultTemporaryTableParser instance;

    private Map<Class, String> sqlMap = new ConcurrentHashMap<>();

    private Dialect dialect;

    private Schema schema;

    private Object lock = new Object();

    private DefaultTemporaryTableParser(){}

    public static DefaultTemporaryTableParser newInstance(){
        if (instance == null){
            instance = new DefaultTemporaryTableParser();
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

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    public String parseAndGetSql(Class clzz){

        if (schema == null) {
            throw new UnsupportedOperationException("Not support temporary table, dialect: " + dialect.getKey() +
                    ", try to implements Schema, and configure it");
        }

        String sql = sqlMap.get(clzz);
        if (sql != null)
            return sql;

        synchronized (lock) {
            Parsed parsed = Parser.get(clzz.getSimpleName());
            if (parsed != null)
                throw new ParsingException("Table exists while parse temporary table entity to get sql: " + clzz.getName());
            Parser.parse(clzz);
            parsed = Parser.get(clzz);

            String createTableSql = schema.createTableSqlUnMapped(parsed, true);
            createTableSql = SqlParserUtil.mapper(createTableSql, parsed);
            sqlMap.put(clzz, createTableSql);
            return createTableSql;
        }
    }


}

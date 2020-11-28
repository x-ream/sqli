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

import io.xream.sqli.builder.SqlScript;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.util.SqlParserUtil;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.SqliLoggerProxy;
import io.xream.sqli.util.SqliStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Sim
 */
public final class DefaultSqlInit implements SqlInit {

    private Dialect dialect;
    private static SqlInit instance;

    private DefaultSqlInit() {
    }

    public static SqlInit newInstance() {
        if (instance == null) {
            instance = new DefaultSqlInit();
            return instance;
        }
        return null;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public Dialect getDialect() {
        return this.dialect;
    }

    public String getRemoveSql(Class clz) {
        Parsed parsed = Parser.get(clz);
        StringBuilder sb = new StringBuilder();
        sb.append(dialect.getAlterTableDelete()).append(SqlScript.SPACE);
        sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(dialect.getCommandDelete());
        sb.append("WHERE ");

        parseKey(sb, clz);

        if (SqliStringUtil.isNotNull(dialect.getLimitOne())){
            sb.append(dialect.getLimitOne());
        }

        String sql = sb.toString();

        sql = SqlParserUtil.mapper(sql, parsed);

        getSqlMap(clz).put(REMOVE, sql);

        SqliLoggerProxy.debug(clz, sb);

        return sql;

    }

    public String getOneSql(Class clz) {
        Parsed parsed = Parser.get(clz);
        String space = " ";
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);
        sb.append("WHERE ");

        parseKey(sb, clz);

        String sql = sb.toString();

        sql = SqlParserUtil.mapper(sql, parsed);

        getSqlMap(clz).put(GET_ONE, sql);

        SqliLoggerProxy.debug(clz, sb);

        return sql;

    }

    public void parseKey(StringBuilder sb, Class clz) {
        Parsed parsed = Parser.get(clz);

        sb.append(parsed.getKey());
        sb.append(" = ?");

    }

    public String getLoadSql(Class clz) {

        Parsed parsed = Parser.get(clz);
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(BeanUtil.getByFirstLower(parsed.getClzName()));

        String sql = sb.toString();

        sql = SqlParserUtil.mapper(sql, parsed);

        getSqlMap(clz).put(LOAD, sql);

        SqliLoggerProxy.debug(clz, sb);

        return sql;

    }


    public String getCreateSql(Class clz) {

        List<BeanElement> list = Parser.get(clz).getBeanElementList();

        Parsed parsed = Parser.get(clz);

        List<BeanElement> tempList = new ArrayList<>();
        for (BeanElement p : list) {
            tempList.add(p);
        }
        String sql = dialect.createSql(parsed,tempList);
        sql = SqlParserUtil.mapper(sql, parsed);
        getSqlMap(clz).put(CREATE, sql);

        SqliLoggerProxy.debug(clz, sql);

        return sql;

    }

    public String getTableSql(Class clz) {
        return buildTableSql(clz, false);
    }


}

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
package io.xream.x7.repository.mapper;

import io.xream.sqli.core.builder.BeanElement;
import io.xream.sqli.core.builder.Parsed;
import io.xream.sqli.core.builder.Parser;
import io.xream.sqli.core.repository.Dialect;
import io.xream.sqli.core.repository.X;
import io.xream.sqli.core.util.BeanUtil;
import io.xream.sqli.api.TemporaryRepository;
import io.xream.x7.repository.util.SqlParserUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTemporaryTableParser implements TemporaryRepository.Parser {

    String CREATE_TABLE = "CREATE_TABLE";

    private Map<Class, String> sqlMap = new ConcurrentHashMap<>();

    private Dialect dialect;
    public void setDialect(Dialect dialect){
        this.dialect = dialect;
    }

    @Override
    public String parseAndGetSql(Class clzz){

        String sql = sqlMap.get(clzz);
        if (sql != null)
            return sql;

        Parsed parsed = Parser.get(clzz.getSimpleName());
        if (parsed != null)
            throw new RuntimeException("Table exists while parse temporary table entity to get sql: " + clzz.getName());
        Parser.parse(clzz);

        return getTableSql(clzz);
    }


    protected String getTableSql(Class clz) {

        List<BeanElement> temp = Parser.get(clz).getBeanElementList();
        Map<String, BeanElement> map = new HashMap<String, BeanElement>();
        List<BeanElement> list = new ArrayList<BeanElement>();
        for (BeanElement be : temp) {
            if (be.sqlType != null && be.sqlType.equals("text")) {
                list.add(be);
                continue;
            }
            map.put(be.property, be);
        }
        Parsed parsed = Parser.get(clz);

        final String keyOne = parsed.getKey(X.KEY_ONE);

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TEMPORARY TABLE ").append(BeanUtil.getByFirstLower(parsed.getClzName())).append(" (")
                .append("\n");

        sb.append("   ").append(keyOne);

        BeanElement be = map.get(keyOne);
        String sqlType = Mapper.getSqlTypeRegX(be);

        if (sqlType.equals(Dialect.INT)) {
            sb.append(Dialect.INT + " NOT NULL");
        } else if (sqlType.equals(Dialect.LONG)) {
            sb.append(Dialect.LONG + " NOT NULL");
        } else if (sqlType.equals(Dialect.STRING)) {
            sb.append(Dialect.STRING).append("(").append(be.length).append(") NOT NULL");
        }

        sb.append(", ");// FIXME ORACLE

        sb.append("\n");
        map.remove(keyOne);

        for (BeanElement bet : map.values()) {
            sqlType = Mapper.getSqlTypeRegX(bet);
            sb.append("   ").append(bet.property).append(" ");

            sb.append(sqlType);

            if (sqlType.equals(Dialect.BIG)) {
                sb.append(" DEFAULT 0.00 ");
            } else if (sqlType.equals(Dialect.DATE)) {
                sb.append(" NULL");

            }else if (BeanUtil.isEnum(bet.clz)) {
                sb.append("(").append(bet.length).append(") NOT NULL");
            } else if (sqlType.equals(Dialect.STRING)) {
                sb.append("(").append(bet.length).append(") NULL");
            } else {
                if (bet.clz == Boolean.class || bet.clz == boolean.class || bet.clz == Integer.class
                        || bet.clz == int.class || bet.clz == Long.class || bet.clz == long.class) {
                    sb.append(" DEFAULT 0");
                } else {
                    sb.append(" DEFAULT NULL");
                }
            }
            sb.append(",").append("\n");
        }

        for (BeanElement bet : list) {
            sqlType = Mapper.getSqlTypeRegX(bet);
            sb.append("   ").append(bet.property).append(" ").append(sqlType).append(",").append("\n");
        }

        sb.append("   PRIMARY KEY ( ").append(keyOne).append(" )");

        sb.append("\n");
        sb.append(") ").append(";");

        String sql = sb.toString();
        sql = dialect.match(sql, CREATE_TABLE);
        sql = SqlParserUtil.mapper(sql, parsed);

        sqlMap .put(clz, sql);

        return sql;
    }
}

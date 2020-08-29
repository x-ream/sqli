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
package io.xream.sqli.repository.mapper;

import io.xream.sqli.annotation.X;
import io.xream.sqli.api.Dialect;
import io.xream.sqli.api.TemporaryRepository;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.util.SqlParserUtil;
import io.xream.sqli.util.BeanUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Sim
 */
public class DefaultTemporaryTableParser implements TemporaryRepository.Parser {

    private String CREATE_TABLE = "CREATE_TABLE";
    private Dialect dialect;

    private Map<Class, String> sqlMap = new ConcurrentHashMap<>();

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
            throw new ParsingException("Table exists while parse temporary table entity to get sql: " + clzz.getName());
        Parser.parse(clzz);

        return getTableSql(clzz);
    }


    protected String getTableSql(Class clz) {

        List<BeanElement> temp = Parser.get(clz).getBeanElementList();
        Map<String, BeanElement> map = new HashMap<String, BeanElement>();
        List<BeanElement> list = new ArrayList<BeanElement>();
        for (BeanElement be : temp) {
            if (be.getSqlType() != null && be.getSqlType().equals("text")) {
                list.add(be);
                continue;
            }
            map.put(be.getProperty(), be);
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
            sb.append(Dialect.STRING).append("(").append(be.getLength()).append(") NOT NULL");
        }

        sb.append(", ");// FIXME ORACLE

        sb.append("\n");
        map.remove(keyOne);

        for (BeanElement bet : map.values()) {
            Class clzz = bet.getClz();
            sqlType = Mapper.getSqlTypeRegX(bet);
            sb.append("   ").append(bet.getProperty()).append(" ");

            sb.append(sqlType);

            if (sqlType.equals(Dialect.BIG)) {
                sb.append(" DEFAULT 0.00 ");
            } else if (sqlType.equals(Dialect.DATE)) {
                sb.append(" NULL");

            }else if (BeanUtil.isEnum(bet.getClz())) {
                sb.append("(").append(bet.getLength()).append(") NOT NULL");
            } else if (sqlType.equals(Dialect.STRING)) {
                sb.append("(").append(bet.getLength()).append(") NULL");
            } else {
                if (clzz == Boolean.class || clzz== boolean.class || clzz == Integer.class
                        || clzz == int.class || clzz == Long.class || clzz == long.class) {
                    sb.append(" DEFAULT 0");
                } else {
                    sb.append(" DEFAULT NULL");
                }
            }
            sb.append(",").append("\n");
        }

        for (BeanElement bet : list) {
            sqlType = Mapper.getSqlTypeRegX(bet);
            sb.append("   ").append(bet.getProperty()).append(" ").append(sqlType).append(",").append("\n");
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

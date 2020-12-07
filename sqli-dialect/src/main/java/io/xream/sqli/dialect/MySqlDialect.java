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

import io.xream.sqli.builder.SqlScript;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.EnumUtil;
import io.xream.sqli.util.SqliJsonUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;


/**
 * @Author Sim
 */
public class MySqlDialect implements Dialect {

    private final Map<String, String> map = new HashMap<String, String>() {
        {
            put(DATE, "timestamp");
            put(BYTE, "tinyint(1)");
            put(INT, "int(11)");
            put(LONG, "bigint(13)");
            put(BIG, "decimal(15,2)");
            put(STRING, "varchar");
            put(TEXT, "text");
            put(LONG_TEXT, "longtext");
            put(INCREAMENT, "AUTO_INCREMENT");
            put(ENGINE, "ENGINE=InnoDB DEFAULT CHARSET=utf8");
        }
    };

    @Override
    public String getKey(){
        return "mysql";
    }

    @Override
    public String buildPageSql(String origin, long start, long rows) {

        if (rows == 0)
            return origin;
        StringBuilder sb = new StringBuilder();
        sb.append(origin);
        sb.append(SqlScript.LIMIT).append(rows);
        if (start > 0){
            sb.append(SqlScript.OFFSET).append(start);
        }
        return sb.toString();
    }

    @Override
    public String replaceAll(String origin) {
        return replace(origin,map);
    }

    @Override
    public Object mappingToObject( Object obj, BeanElement element) {
        if (obj == null)
            return null;
        Class ec = element.getClz();

        if (EnumUtil.isEnum(ec)) {
            return EnumUtil.deserialize(ec, obj.toString());
        } else if (element.isJson()) {

            if (SqliStringUtil.isNullOrEmpty(obj))
                return null;
            String str = obj.toString().trim();

            if (ec == List.class) {
                Class geneType = element.getGeneType();
                return SqliJsonUtil.toList(str, geneType);
            } else if (ec == Map.class) {
                return SqliJsonUtil.toMap(str);
            } else {
                return SqliJsonUtil.toObject(str, ec);
            }
        } else if (ec == BigDecimal.class) {
            return new BigDecimal(String.valueOf(obj));
        } else if (ec == double.class || ec == Double.class) {
            return Double.valueOf(obj.toString());
        }

        return obj;
    }

    @Override
    public String createOrReplaceSql(String sql) {
        return sql.replaceFirst("INSERT","REPLACE");
    }

    @Override
    public String createSql(Parsed parsed, List<BeanElement> tempList) {
        return getDefaultCreateSql(parsed,tempList);
    }

    @Override
    public String buildTableSql(Class clz, boolean isTemporary) {
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

        final String keyOne = parsed.getKey();

        StringBuilder sb = new StringBuilder();
        if (isTemporary) {
            sb.append(getTemporaryTableCreate());
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

            } else if (EnumUtil.isEnum(bet.getClz())) {
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
        return replaceAll(sql);
    }

    @Override
    public Object convertJsonToPersist(Object json) {
        return json;
    }

    @Override
    public String transformAlia(String mapper,Map<String, String> aliaMap,  Map<String, String> resultKeyAliaMap) {

        if (resultKeyAliaMap.containsKey(mapper)) {
             mapper = resultKeyAliaMap.get(mapper);
        }
        return mapper;
    }

    @Override
    public Object filterValue(Object object) {
        return filter(object,null);
    }

    @Override
    public Object[] toArr(Collection<Object> list) {

        if (list == null || list.isEmpty())
            return null;
        int size = list.size();
        Object[] arr = new Object[size];
        int i =0;
        for (Object obj : list) {
            obj = filterValue(obj);
            arr[i++] = obj;
        }

        return arr;
    }


    @Override
    public String getAlterTableUpdate() {
        return SqlScript.UPDATE;
    }

    @Override
    public String getAlterTableDelete() {
        return SqlScript.DELETE_FROM ;
    }

    @Override
    public String getCommandUpdate() {
        return SqlScript.SET;
    }

    @Override
    public String getCommandDelete() {
        return SqlScript.SPACE;
    }

    @Override
    public String getTemporaryTableCreate() {
        return "CREATE TEMPORARY TABLE IF NOT EXISTS ";
    }

    @Override
    public String getLimitOne() {
        return SqlScript.LIMIT_ONE;
    }

    @Override
    public String getInsertTagged() {
        return null;
    }

    @Override
    public void filterTags(List<BeanElement> list, List<Field> tagList) {
        return;
    }

    @Override
    public List<Object> objectToListForCreate(Object obj, Parsed parsed) {
        List<BeanElement> tempList = parsed.getBeanElementList();

        List<Object> list = new ArrayList<>();

        objectToListForCreate(list, obj, tempList);

        return list;

    }


}

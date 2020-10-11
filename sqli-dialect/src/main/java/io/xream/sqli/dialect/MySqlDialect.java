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
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.JsonWrapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public String buildPage(String origin, long start, long rows) {

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

        if (BeanUtil.isEnum(ec)) {
            return Enum.valueOf(ec, obj.toString());
        } else if (element.isJson()) {
            if (ec == List.class) {
                Class geneType = element.getGeneType();
                return JsonWrapper.toList(obj.toString(), geneType);
            } else if (ec == Map.class) {
                return JsonWrapper.toMap(obj);
            } else {
                return JsonWrapper.toObject(obj.toString(), ec);
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
}

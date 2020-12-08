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
import io.xream.sqli.dialect.Schema;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.EnumUtil;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;

/**
 * @Author Sim
 */
public class DefaultMySqlSchema implements Schema{


    @Override
    public String createTableSqlUnMapped(Parsed parsed, boolean isTemporaryTable) {

        BeanElement keyOneBe = null;
        List<BeanElement> list = new ArrayList<BeanElement>();
        for (BeanElement be : parsed.getBeanElementList()) {
            if (be.getProperty().equals(parsed.getKey())) {
                keyOneBe = be;
            }else {
                list.add(be);
            }
        }

        StringBuilder sb = new StringBuilder();
        if (isTemporaryTable) {
            sb.append(SqlScript.CREATE_TEMPORARY_TABLE);
        } else {
            sb.append("CREATE TABLE IF NOT EXISTS ");
        }
        sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(" (")
                .append("\n");

        sb.append("   ").append(keyOneBe.getProperty());

        int type = getSqlType(keyOneBe);

        if (type == Types.INTEGER) {
            sb.append(" ").append(get(type) + " NOT NULL");
        } else if (type == Types.BIGINT) {
            sb.append(" ").append(get(type) + " NOT NULL");
        } else if (type == Types.VARCHAR) {
            sb.append(" ").append(get(type)).append("(").append(keyOneBe.getLength()).append(") NOT NULL");
        }

        sb.append(", ");// FIXME ORACLE

        sb.append("\n");


        for (BeanElement bet : list) {
            type = getSqlType(bet);
            sb.append("   ").append(bet.getProperty()).append(" ");

            sb.append(get(type));

            if (type == Types.DECIMAL) {
                sb.append(" DEFAULT 0.00 ");
            } else if (type == Types.DATE) {
                sb.append(" NULL");
            } else if (EnumUtil.isEnum(bet.getClz())) {
                sb.append("(").append(bet.getLength()).append(") NOT NULL");
            } else if (type == Types.VARCHAR) {
                sb.append("(").append(bet.getLength()).append(") NULL");
            } else if (type != Types.LONGVARCHAR){
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


        sb.append("   PRIMARY KEY ( ").append(keyOneBe.getProperty()).append(" )");

        sb.append("\n");
        sb.append(") ").append(" ").append(getDefaultEngine()).append(";");
        String sql = sb.toString();
        return sql;
    }


    private String get(int type) {
        return map.get(type);
    }

    private static final Map<Integer, String> map = new HashMap<Integer, String>() {
        {
            put(Types.DATE, "timestamp");
            put(Types.TINYINT, "tinyint(1)");
            put(Types.INTEGER, "int(11)");
            put(Types.BIGINT, "bigint(13)");
            put(Types.DECIMAL, "decimal(15,2)");
            put(Types.VARCHAR, "varchar");
            put(Types.LONGVARCHAR, "text");
        }
    };

    private String getDefaultEngine() {
        return "ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }

    private int getSqlType(BeanElement be) {

        Class clz = be.getClz();
        if (clz == Date.class || clz == java.sql.Date.class || clz == java.sql.Timestamp.class) {
            return Types.DATE;
        } else if (clz == String.class) {
            return Types.VARCHAR;
        } else if (EnumUtil.isEnum(clz)) {
            return Types.VARCHAR;
        } else if (clz == int.class || clz == Integer.class) {
            return Types.INTEGER;
        } else if (clz == long.class || clz == Long.class) {
            return Types.BIGINT;
        } else if (clz == double.class || clz == Double.class) {
            return Types.DECIMAL;
        } else if (clz == float.class || clz == Float.class) {
            return Types.DECIMAL;
        } else if (clz == BigDecimal.class) {
            return Types.DECIMAL;
        } else if (clz == boolean.class || clz == Boolean.class) {
            return Types.TINYINT;
        } else if (clz == short.class || clz == Short.class) {
            return Types.INTEGER;
        } else if (clz == byte.class || clz == Byte.class) {
            return Types.TINYINT;
        }
        return Types.LONGVARCHAR;
    }

}

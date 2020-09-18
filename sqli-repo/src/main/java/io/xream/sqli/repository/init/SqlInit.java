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

import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.repository.core.Dialect;
import io.xream.sqli.util.BeanUtil;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author Sim
 */
public interface SqlInit {

    String CREATE = "CREATE";
    String REMOVE = "REMOVE";
    String LOAD = "LOAD";
    String CREATE_TABLE = "CREATE_TABLE";
    String GET_ONE = "GET_ONE";


    interface Interpreter {

        String getTableSql(Class clz);

        String getLoadSql(Class clz);

        String getCreateSql(Class clz);

        String getOneSql(Class clz);

        String getRemoveSql(Class clz);

    }

    static String getSqlTypeRegX(BeanElement be) {

        Class clz = be.getClz();
        if (clz == Date.class || clz == java.sql.Date.class || clz == java.sql.Timestamp.class) {
            return Dialect.DATE;
        } else if (clz == String.class) {
            return Dialect.STRING;
        } else if (BeanUtil.isEnum(clz)) {
            return Dialect.STRING;
        } else if (clz == int.class || clz == Integer.class) {
            return Dialect.INT;
        } else if (clz == long.class || clz == Long.class) {
            return Dialect.LONG;
        } else if (clz == double.class || clz == Double.class) {
            return Dialect.BIG;
        } else if (clz == float.class || clz == Float.class) {
            return Dialect.BIG;
        } else if (clz == BigDecimal.class) {
            return Dialect.BIG;
        } else if (clz == boolean.class || clz == Boolean.class) {
            return Dialect.BYTE;
        } else if (clz == short.class || clz == Short.class) {
            return Dialect.INT;
        } else if (clz == byte.class || clz == Byte.class) {
            return Dialect.BYTE;
        }
        return Dialect.TEXT;

    }

}

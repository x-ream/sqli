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

import io.xream.sqli.dialect.Dialect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sim
 */
public interface SqlTemplate {

    Map<Class, Map<String, String>> SQLS_MAP = new HashMap<>();
    Map<Class, List<String>> CREATE_ELE_MAP = new HashMap<>();


    String CREATE = "CREATE";
    String REMOVE = "REMOVE";
    String REMOVE_IN = "REMOVE_IN";
    String LOAD = "LOAD";
    String GET_ONE = "GET_ONE";

    Dialect getDialect();
    void setDialect(Dialect dialect);

    default Map<String,String> getSqlMap(Class clzz) {
        Map<String, String> sqlMap = SQLS_MAP.get(clzz);
        if (sqlMap == null) {
            sqlMap = new HashMap<>();
            SQLS_MAP.put(clzz, sqlMap);
        }
        return sqlMap;
    }

    default String getSql(Class clzz, String type) {
        return getSqlMap(clzz).get(type);
    }

}

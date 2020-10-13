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

import io.xream.sqli.parser.BeanElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Sim
 */
public final class DynamicDialect implements Dialect{

    private Dialect defaultDialect;
    private Map<String,Dialect> map = new HashMap<>();
    public void setDefaultDialect(Dialect dialect) {
        if (this.defaultDialect == null) {
            this.defaultDialect = dialect;
            map.put(dialect.getKey(),dialect);
        }else {
            throw new IllegalArgumentException();
        }
    }

    public void addDialect(Dialect dialect) {
        map.put(dialect.getKey(), dialect);
    }

    @Override
    public String getKey(){
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.getKey();
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.getKey();
    }

    private Dialect getCurrentDialect() {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect;
        }
        return map.get(key);
    }

    @Override
    public String buildPageSql(String sql, long start, long rows) {
        return getCurrentDialect().buildPageSql(sql,start,rows);
    }

    @Override
    public String replaceAll(String sql) {
        return getCurrentDialect().replaceAll(sql);
    }

    @Override
    public String transformAlia(String mapper, Map<String, String> aliaMap, Map<String, String> resultKeyAliaMap) {
        return getCurrentDialect().transformAlia(mapper,aliaMap,resultKeyAliaMap);
    }

    @Override
    public Object filterValue(Object value) {
        return getCurrentDialect().filterValue(value);
    }

    @Override
    public Object[] toArr(Collection<Object> list) {
        return getCurrentDialect().toArr(list);
    }

    @Override
    public Object mappingToObject(Object obj, BeanElement element) {
        return getCurrentDialect().mappingToObject(obj, element);
    }

    @Override
    public String createOrReplaceSql(String sql) {
        return getCurrentDialect().createOrReplaceSql(sql);
    }

    @Override
    public Object convertJsonToPersist(Object json) {
        return getCurrentDialect().convertJsonToPersist(json);
    }

    @Override
    public String getAlterTableUpdate() {
        return getCurrentDialect().getAlterTableUpdate();
    }

    @Override
    public String getAlterTableDelete() {
        return getCurrentDialect().getAlterTableDelete();
    }

    @Override
    public String getCommandUpdate() {
        return getCurrentDialect().getCommandUpdate();
    }

    @Override
    public String getCommandDelete() {
        return getCurrentDialect().getCommandDelete();
    }

    @Override
    public String getTemporaryTableCreate() {
        return getCurrentDialect().getTemporaryTableCreate();
    }
}

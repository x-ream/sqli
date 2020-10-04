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

    @Override
    public String buildPage(String sql, long start, long rows) {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.buildPage(sql,start,rows);
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.buildPage(sql,start,rows);
    }

    @Override
    public String replaceAll(String sql) {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.replaceAll(sql);
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.replaceAll(sql);
    }

    @Override
    public String transformAlia(String mapper, Map<String, String> aliaMap, Map<String, String> resultKeyAliaMap) {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.transformAlia(mapper,aliaMap,resultKeyAliaMap);
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.transformAlia(mapper,aliaMap,resultKeyAliaMap);
    }

    @Override
    public Object filterValue(Object value) {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.filterValue(value);
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.filterValue(value);
    }

    @Override
    public Object[] toArr(Collection<Object> list) {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.toArr(list);
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.toArr(list);
    }

    @Override
    public Object mappingToObject(Object obj, BeanElement element) {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.mappingToObject(obj, element);
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.mappingToObject(obj, element);
    }

    @Override
    public String createOrReplaceSql(String sql) {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.createOrReplaceSql(sql);
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.createOrReplaceSql(sql);
    }

    @Override
    public Object convertJsonToPersist(Object json) {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.convertJsonToPersist(json);
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.convertJsonToPersist(json);
    }

    @Override
    public String getAlterTableUpdate() {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.getAlterTableUpdate();
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.getAlterTableUpdate();
    }

    @Override
    public String getAlterTableDelete() {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.getAlterTableDelete();
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.getAlterTableDelete();
    }

    @Override
    public String getCommandUpdate() {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.getCommandUpdate();
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.getCommandUpdate();
    }

    @Override
    public String getCommandDelete() {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.getCommandDelete();
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.getCommandDelete();
    }

    @Override
    public String getTemporaryTableCreate() {
        String key = DynamicDialectHolder.getDialectKey();
        if (key == null){
            return defaultDialect.getTemporaryTableCreate();
        }
        Dialect currentDialect = map.get(key);
        return currentDialect.getTemporaryTableCreate();
    }
}

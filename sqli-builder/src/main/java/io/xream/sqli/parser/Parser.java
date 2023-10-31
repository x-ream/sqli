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
package io.xream.sqli.parser;

import io.xream.sqli.annotation.X;
import io.xream.sqli.builder.Q;
import io.xream.sqli.exception.NotSupportedException;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sim
 */
public final class Parser {

    private static Logger LOGGER = LoggerFactory.getLogger(Parser.class);
    @SuppressWarnings("rawtypes")
    private final static Map<Class, Parsed> map = new ConcurrentHashMap<Class, Parsed>();
    private final static Map<String, Parsed> simpleNameMap = new ConcurrentHashMap<String, Parsed>();

    public static String mappingPrefix;
    public static String mappingSpec;

    private Parser(){}

    @SuppressWarnings("rawtypes")
    public static void put(Class clz, Parsed parsed) {
        map.put(clz, parsed);
        String key = BeanUtil.getByFirstLower(clz.getSimpleName());
        simpleNameMap.put(key, parsed);
    }

    @SuppressWarnings("rawtypes")
    public static Parsed get(Class clz) {
        Parsed parsed = map.get(clz);
        if (parsed == null) {
            parse(clz);
            parsed = map.get(clz);
        }
        return parsed;
    }

    public static Parsed get(String simpleName) {
        return simpleNameMap.get(simpleName);
    }

    public static boolean contains(String simpleName) {
        return simpleNameMap.containsKey(simpleName);
    }

    private static void parseElement(Class clz, Parsed parsed,List<BeanElement> elementList) {

        for (BeanElement element : elementList) {
            if (SqliStringUtil.isNullOrEmpty(element.getMapper())) {
                element.initMaper();
            }
        }
        boolean isNoSpec = true;
        try {
            if (SqliStringUtil.isNotNull(mappingSpec)) {
                isNoSpec = false;
            } else {
                for (BeanElement element : elementList) {
                    if (!element.getProperty().equals(element.getMapper())) {
                        isNoSpec = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info(SqliExceptionUtil.getMessage(e));
        }
        parsed.setNoSpec(isNoSpec);
        parsed.reset(elementList);
        ParserUtil.parseKey(parsed, clz);
        ParserUtil.parseTagAndSub(parsed, clz);
    }

    private static void parseTableNameMapping(Class clz, Parsed parsed) {
        X.Mapping mapping = (X.Mapping) clz.getAnnotation(X.Mapping.class);
        if (mapping != null) {
            String tableName = mapping.value();
            if (!tableName.equals("")) {
                parsed.setTableName(tableName);
                parsed.setOriginTable(tableName);
                parsed.setNoSpec(false);
            } else {
                String name = BeanUtil.getByFirstLower(clz.getSimpleName());
                String mapper = ParserUtil.getMapper(name);
                String prefix = mappingPrefix;
                if (SqliStringUtil.isNotNull(prefix)) {
                    if (!prefix.endsWith("_")) {
                        prefix += "_";
                    }
                    mapper = prefix + mapper;
                }

                parsed.setTableName(mapper);
                parsed.setOriginTable(mapper);
            }
        } else {
            String name = BeanUtil.getByFirstLower(clz.getSimpleName());
            String mapper = ParserUtil.getMapper(name);
            String prefix = mappingPrefix;
            if (SqliStringUtil.isNotNull(prefix)) {
                if (!prefix.endsWith("_")) {
                    prefix += "_";
                }
                mapper = prefix + mapper;
            }

            parsed.setTableName(mapper);
            parsed.setOriginTable(mapper);
        }
    }

    private static void sortOnParsing(Parsed parsed, List<BeanElement> elementList) {

        List<BeanElement> tempList = new ArrayList<>();
        tempList.addAll(elementList);
        elementList.clear();
        BeanElement one = null;
        Iterator<BeanElement> ite = tempList.iterator();
        while (ite.hasNext()) {
            BeanElement be = ite.next();
            if (be.getProperty().equals(parsed.getKey())) {
                one = be;
                ite.remove();
            }
        }

        Iterator<BeanElement> beIte = tempList.iterator();
        while (beIte.hasNext()) {
            if (null == beIte.next()) {
                beIte.remove();
            }
        }

        if (one != null) {
            elementList.add(0, one);
        }

        for (Field field : parsed.getClzz().getDeclaredFields()){
            for (BeanElement be: tempList) {
                if (be.getProperty().equals(field.getName())){
                    elementList.add(be);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes"})
    public static void parse(Class clz) {

        if (clz == Q.class || clz == Q.X.class || clz == Void.class)
            throw new IllegalArgumentException("parser unsupport Q, Q.X, ....");

        Parsed parsed = new Parsed(clz);
        List<BeanElement> elementList = ParserUtil.parseElementList(clz);

        parseElement(clz, parsed, elementList);

        parseTableNameMapping(clz, parsed);

        sortOnParsing(parsed,elementList);

        ParserUtil.parseCacheableAnno(clz, parsed);

        put(clz, parsed);

    }

    protected static void onStarted(){
        for (Map.Entry<String,Parsed> entry : simpleNameMap.entrySet()) {
            Parsed parsed = entry.getValue();
            for (Map.Entry<String,String> pmEntry : parsed.getPropertyMapperMap().entrySet()) {
                String property = pmEntry.getKey();
                String mapper = pmEntry.getValue();
                Parsed same = simpleNameMap.get(property);
                if (same != null && !same.getTableName().equals(mapper)) {
                    throw new NotSupportedException("not support the spell of property: " + parsed.getClzz().getName()+"."+property +", modify " + property + " or try @X.Mapping(\"" + same.getTableName() +"\")");
                }
            }
        }
    }

}

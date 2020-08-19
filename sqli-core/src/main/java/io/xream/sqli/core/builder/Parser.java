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
package io.xream.sqli.core.builder;

import io.xream.sqli.core.util.BeanUtil;
import io.xream.sqli.core.util.BeanUtilX;
import io.xream.sqli.core.util.SqlStringUtil;
import io.xream.sqli.core.repository.ReflectionCache;
import io.xream.sqli.annotation.X;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Parser {

    private static Logger logger = LoggerFactory.getLogger(Parser.class);
    @SuppressWarnings("rawtypes")
    private final static Map<Class, Parsed> map = new ConcurrentHashMap<Class, Parsed>();

    private final static Map<String, Parsed> simpleNameMap = new ConcurrentHashMap<String, Parsed>();

    private final static Map<Class, ReflectionCache> cacheMap = new ConcurrentHashMap<Class, ReflectionCache>();

    public static String mappingPrefix;
    public static String mappingSpec;


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
            Field f = parsed.getKeyField(X.KEY_ONE);
            if (f == null)
                throw new RuntimeException("No Primary Key, class: " + clz.getName());
        }
        return parsed;
    }

    public static Parsed get(String simpleName) {
        return simpleNameMap.get(simpleName);
    }

    @SuppressWarnings({"rawtypes"})
    public static void parse(Class clz) {

        if (clz == Criteria.class || clz == Criteria.ResultMappedCriteria.class)
            throw new RuntimeException("parser unsupport Criteria, CriteriaJoinable, ....");

        List<BeanElement> elementList = BeanUtilX.getElementList(clz);
        Parsed parsed = new Parsed(clz);
        for (BeanElement element : elementList) {
            if (SqlStringUtil.isNullOrEmpty(element.getMapper())) {
                element.initMaper();
            }
        }
        boolean isNoSpec = true;
        try {
            if (SqlStringUtil.isNotNull(mappingSpec)) {
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
            e.printStackTrace();
        }
        parsed.setNoSpec(isNoSpec);
        parsed.reset(elementList);
        BeanUtilX.parseKey(parsed, clz);

        /*
         * tableName,
         */
        X.Mapping mapping = (X.Mapping) clz.getAnnotation(X.Mapping.class);
        if (mapping != null) {
            String tableName = mapping.value();
            if (!tableName.equals("")) {
                parsed.setTableName(tableName);
                parsed.setOriginTable(tableName);
                parsed.setNoSpec(false);
            } else {
                String name = BeanUtil.getByFirstLower(clz.getSimpleName());
                String mapper = BeanUtil.getMapper(name);
                String prefix = mappingPrefix;
                if (SqlStringUtil.isNotNull(prefix)) {
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
            String mapper = BeanUtil.getMapper(name);
            String prefix = mappingPrefix;
            if (SqlStringUtil.isNotNull(prefix)) {
                if (!prefix.endsWith("_")) {
                    prefix += "_";
                }
                mapper = prefix + mapper;
            }

            parsed.setTableName(mapper);
            parsed.setOriginTable(mapper);
        }

        /*
         * 排序
         */
        BeanElement one = null;
        Iterator<BeanElement> ite = elementList.iterator();
        while (ite.hasNext()) {
            BeanElement be = ite.next();
            if (be.getProperty().equals(parsed.getKey(X.KEY_ONE))) {
                one = be;
                ite.remove();
                continue;
            }
        }

        elementList.add(0, one);

        Iterator<BeanElement> beIte = parsed.getBeanElementList().iterator();
        while (beIte.hasNext()) {
            if (null == beIte.next()) {
                beIte.remove();
            }
        }

        /*
         * parseCacheable
         */
        BeanUtilX.parseCacheableAnno(clz, parsed);

        put(clz, parsed);


    }

    public static ReflectionCache getReflectionCache(Class clz) {
        ReflectionCache cache = cacheMap.get(clz);
        if (cache == null) {
            cache = new ReflectionCache();
            cache.setClz(clz);
            cache.cache();
            cacheMap.put(clz, cache);
        }
        return cache;
    }


}

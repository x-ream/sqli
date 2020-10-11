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
package io.xream.sqli.converter;

import io.xream.sqli.builder.SqlScript;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.exception.PersistenceException;
import io.xream.sqli.mapping.ResultMapHelpful;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.BeanUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author Sim
 */
public final class DataObjectConverter {

    public static Map<String,Object> toMapWithKeyOfObjectProperty(Map<String,Object> dataMap, Class orClzz, ResultMapHelpful resultMapHelpful, Dialect dialect) {
        if (resultMapHelpful == null && orClzz == null)
            return dataMap;
        Map<String, Object> propertyMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String mapper = entry.getKey();
            String property = null;
            BeanElement be = null;
            if (resultMapHelpful == null) {
                Parsed parsed = Parser.get(orClzz);
                property = parsed.getPropertyByLower(mapper);
                be = parsed.getElement(property);
            } else {

                if (mapper.contains(SqlScript.DOLLOR)) {
                    property = dialect.transformAlia(mapper, resultMapHelpful.getAliaMap(), resultMapHelpful.getResultKeyAliaMap());
                } else {
                    mapper = dialect.transformAlia(mapper, resultMapHelpful.getAliaMap(), resultMapHelpful.getResultKeyAliaMap());
                    property = resultMapHelpful.getMapperPropertyMap().get(mapper);
                    if (property == null) {
                        property = mapper;
                    }else if (property.contains(".")) {
                        String[] arr = property.split("\\.");
                        String clzName = resultMapHelpful.getAliaMap().get(arr[0]);
                        Parsed parsed = Parser.get(clzName);
                        be = parsed.getElement(arr[1]);
                    } else {
                        Parsed parsed = Parser.get(orClzz);
                        be = parsed.getElement(property);
                    }
                }
            }
            Object value = entry.getValue();
            value = filter(value);
            if (be != null) {
                value = dialect.mappingToObject(value, be);
            }
            propertyMap.put(property, value);
        }
        return propertyMap;
    }


    public static <T> void initObj(T obj, Map<String, Object> map, List<BeanElement> eles,Dialect dialect) throws Exception {

        for (BeanElement ele : eles) {

            Method method = ele.getSetMethod();
            String mapper = ele.getMapper();

            Object value = map.get(mapper);

            if (value == null) {
                if (BeanUtil.isEnum(ele.getClz()))
                    throw new PersistenceException(
                            "ENUM CAN NOT NULL, property:" + obj.getClass().getName() + "." + ele.getProperty());
            } else {
                value = filter(value);
                Object v = dialect.mappingToObject(value,ele);
                method.invoke(obj, v);
            }

        }
    }

    private static Object filter(Object value) {
        if (value == null)
            return null;
        if (value instanceof String) {
            String str = (String) value;
            value = str.replace("<","&lt").replace(">","&gt");
        }
        return value;
    }

}

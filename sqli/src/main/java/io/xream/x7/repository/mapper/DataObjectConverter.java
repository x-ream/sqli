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
package io.xream.x7.repository.mapper;

import io.xream.sqli.core.builder.*;
import io.xream.sqli.core.repository.Dialect;
import io.xream.sqli.core.util.BeanUtil;
import io.xream.sqli.core.util.SqliExceptionUtil;
import io.xream.sqli.core.util.JsonWrapper;
import io.xream.sqli.core.util.LoggerProxy;
import io.xream.sqli.exception.PersistenceException;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


public class DataObjectConverter {

    public static Map<String,Object> dataToPropertyObjectMap(Class clz, Map<String,Object> dataMap, Criteria.ResultMappedCriteria resultMapped, Dialect dialect) {
        Map<String, Object> propertyMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String mapper = entry.getKey();
            String property = null;
            BeanElement be = null;
            if (resultMapped == null) {
                Parsed parsed = Parser.get(clz);
                property = parsed.getPropertyByLower(mapper);
                be = parsed.getElement(property);
            } else {

                if (mapper.contains(SqlScript.DOLLOR)) {
                    property = dialect.transformAlia(mapper, resultMapped.getAliaMap(), resultMapped.getResultKeyAliaMap());
                } else {
                    mapper = dialect.transformAlia(mapper, resultMapped.getAliaMap(), resultMapped.getResultKeyAliaMap());
                    property = resultMapped.getPropertyMapping().property(mapper);

                    if (property.contains(".")) {
                        String[] arr = property.split("\\.");
                        String clzName = resultMapped.getAliaMap().get(arr[0]);
                        Parsed parsed = Parser.get(clzName);
                        be = parsed.getElement(arr[1]);
                    } else {
                        Parsed parsed = Parser.get(clz);
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

            Method method = ele.setMethod;
            String mapper = ele.mapper;

            Object value = map.get(mapper);

            if (value == null) {
                if (BeanUtil.isEnum(ele.clz))
                    throw new PersistenceException(
                            "ENUM CAN NOT NULL, property:" + obj.getClass().getName() + "." + ele.getProperty());
            } else {
                value = filter(value);
                Object v = dialect.mappingToObject(value,ele);
                method.invoke(obj, v);
            }

        }
    }

    public static List<Object> objectToListForCreate(Object obj, List<BeanElement> eles, Dialect dialect) {

        List<Object> list = new ArrayList<>();
        try {
            for (BeanElement ele : eles) {
                Object value = ele.getMethod.invoke(obj);
                if (value == null) {
                    if (BeanUtil.isEnum(ele.clz))
                        throw new PersistenceException(
                                "ENUM CAN NOT NULL, property:" + obj.getClass().getName() + "." + ele.getProperty());
                    if (ele.clz == Boolean.class || ele.clz == Integer.class || ele.clz == Long.class
                            || ele.clz == Double.class || ele.clz == Float.class || ele.clz == BigDecimal.class
                            || ele.clz == Byte.class || ele.clz == Short.class)
                        list.add(0);
                    else
                        list.add(null);
                } else {
                    if (ele.isJson) {
                        String str = JsonWrapper.toJson(value);
                        list.add(str);
                    } else if (BeanUtil.isEnum(ele.clz)) {
                        String str = ((Enum) value).name();
                        list.add(str);
                    } else {
                        value = dialect.filterValue(value);
                        list.add(value);
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException(SqliExceptionUtil.getMessage(e));
        }

        return list;
    }

    /**
     * 默认值为0的不做查询条件<br>
     * 额外条件从另外一个map参数获得<br>
     * boolean必须从另外一个map参数获得
     */
    @SuppressWarnings({"rawtypes", "unused"})
    public static Map<String, Object> objectToMapForQuery(Parsed parsed, Object obj) {

        Map<String, Object> map = new HashMap<String, Object>();

        Class clz = obj.getClass();
        try {
            for (BeanElement element : parsed.getBeanElementList()) {

                Method method = element.getMethod;
                Object value = method.invoke(obj);
                Class type = method.getReturnType();

                String property = element.getProperty();

                if (type == long.class) {
                    if ((long) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Long.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == String.class) {
                    if (value != null && !value.equals("")) {
                        map.put(property, value);
                    }
                } else if (BeanUtil.isEnum(type)) {
                    if (value != null) {
                        map.put(property, ((Enum) value).name());
                    }
                } else if (type == int.class) {
                    if ((int) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Integer.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == double.class) {
                    if ((double) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Double.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == float.class) {
                    if ((float) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Float.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == boolean.class) {
                    if ((boolean) value != false) {
                        map.put(property, value);
                    }
                } else if (type == BigDecimal.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == Boolean.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else if (type == Date.class || clz == java.sql.Date.class || type == Timestamp.class) {
                    if (value != null) {
                        map.put(property, value);
                    }
                } else {
                    if (value != null) {
                        map.put(property, value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        LoggerProxy.debug(clz, map);

        return map;

    }


    public static void log(Class clz, List<Object> valueList) {
        LoggerProxy.debug(clz, valueList);
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

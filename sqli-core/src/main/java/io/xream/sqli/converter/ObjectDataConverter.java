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

import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.exception.PersistenceException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.JsonWrapper;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliLoggerProxy;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


/**
 * @Author Sim
 */
public final class ObjectDataConverter {

    public static List<Object> objectToListForCreate(Object obj, List<BeanElement> eles, Dialect dialect) {

        List<Object> list = new ArrayList<>();
        try {
            for (BeanElement ele : eles) {
                Object value = ele.getGetMethod().invoke(obj);
                Class clz = ele.getClz();
                if (value == null) {
                    if (BeanUtil.isEnum(clz))
                        throw new PersistenceException(
                                "ENUM CAN NOT NULL, property:" + obj.getClass().getName() + "." + ele.getProperty());
                    if (clz == Boolean.class || clz == Integer.class || clz == Long.class
                            || clz == Double.class || clz == Float.class || clz == BigDecimal.class
                            || clz == Byte.class || clz == Short.class)
                        list.add(0);
                    else
                        list.add(null);
                } else {
                    if (ele.isJson()) {
                        String str = JsonWrapper.toJson(value);
                        Object jsonStr = dialect.convertJsonToPersist(str);
                        list.add(jsonStr);
                    } else if (BeanUtil.isEnum(clz)) {
                        String str = ((Enum) value).name();
                        list.add(str);
                    } else {
                        value = dialect.filterValue(value);
                        list.add(value);
                    }
                }
            }
        } catch (Exception e) {
            SqliExceptionUtil.throwRuntimeExceptionFirst(e);
            throw new ParsingException(SqliExceptionUtil.getMessage(e));
        }

        return list;
    }

    /**
     * 默认值为0的不做查询条件<br>
     * 额外条件从另外一个map参数获得<br>
     * boolean必须从另外一个map参数获得
     */
    @SuppressWarnings({"rawtypes", "unused"})
    public static Map<String, Object> objectToMap(Parsed parsed, Object obj) {

        Map<String, Object> map = new HashMap<String, Object>();

        if (Objects.isNull(obj))
            return map;

        Class clz = obj.getClass();

        try {
            for (BeanElement element : parsed.getBeanElementList()) {

                Method method = element.getGetMethod();
                Object value = method.invoke(obj);
                if (value == null)
                    continue;
                Class type = method.getReturnType();
                String property = element.getProperty();
                if (type == int.class) {
                    if ((int) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Integer.class) {
                    map.put(property, value);
                } else if (type == long.class) {
                    if ((long) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Long.class) {
                    map.put(property, value);
                } else if (type == double.class) {
                    if ((double) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Double.class) {
                    map.put(property, value);
                } else if (type == float.class) {
                    if ((float) value != 0) {
                        map.put(property, value);
                    }
                } else if (type == Float.class) {
                    map.put(property, value);
                } else if (type == boolean.class) {
                    if ((boolean) value) {
                        map.put(property, value);
                    }
                } else if (type == Boolean.class) {
                    map.put(property, value);
                } else if (type == String.class) {
                    map.put(property, value);
                } else if (BeanUtil.isEnum(type)){
                    map.put(property, ((Enum)value).name());
                }else if (type == Date.class || clz == java.sql.Date.class || type == Timestamp.class) {
                    map.put(property, value);
                } else if (type == BigDecimal.class){
                    map.put(property, value);
                }else if (element.isJson()) {
                    String str = JsonWrapper.toJson(value);
                    map.put(property, str);
                }else {
                    map.put(property, value);
                }
            }
        } catch (Exception e) {
            SqliExceptionUtil.throwRuntimeExceptionFirst(e);
            throw new ParsingException(SqliExceptionUtil.getMessage(e));
        }

        return map;
    }

    public static void log(Class clz, List<Object> valueList) {
        SqliLoggerProxy.debug(clz, valueList);
    }

}

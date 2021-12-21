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
package io.xream.sqli.builder;

import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.exception.PersistenceException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.EnumUtil;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliJsonUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author Sim
 */
public interface DialectSupport {

    String getKey();

    Object convertJsonToPersist(Object json);

    String getAlterTableUpdate();
    String getAlterTableDelete();
    String getCommandUpdate();
    String getCommandDelete();

    String getLimitOne();

    String getInsertTagged();

    Object filterValue(Object value);

    void filterTags(List<BeanElement> list,List<Field> tagList);
    List<Object> objectToListForCreate(Object obj, Parsed parsed);

    default void objectToListForCreate(List<Object> list, Object obj, List<BeanElement> tempList) {

        try {
            for (BeanElement ele : tempList) {
                Object value = ele.getGetMethod().invoke(obj);
                Class clz = ele.getClz();
                if (value == null) {
                    if (EnumUtil.isEnum(clz))
                        throw new PersistenceException(
                                "ENUM CAN NOT NULL, property:" + obj.getClass().getName() + "." + ele.getProperty());
//                    if (clz == Boolean.class || clz == Integer.class || clz == Long.class
//                            || clz == Double.class || clz == Float.class || clz == BigDecimal.class
//                            || clz == Byte.class || clz == Short.class)
//                        list.add(0);
//                    else
                        list.add(null);
                } else {
                    if (ele.isJson()) {
                        String str = SqliJsonUtil.toJson(value);
                        Object jsonStr = convertJsonToPersist(str);
                        list.add(jsonStr);
                    } else if (EnumUtil.isEnum(clz)) {
                        list.add(EnumUtil.serialize((Enum) value));
                    } else {
                        value = filterValue(value);
                        list.add(value);
                    }
                }
            }
        } catch (Exception e) {
            SqliExceptionUtil.throwRuntimeExceptionFirst(e);
            throw new ParsingException(e);
        }


    }
}

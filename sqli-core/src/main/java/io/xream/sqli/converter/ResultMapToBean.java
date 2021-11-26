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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author Sim
 */
public class ResultMapToBean {

    private ResultMapToBean(){}

    /**
     * not support resultMap by builder.resultWithDottedKey();
     */
    public static <T> T copy(Class<T> clz, Map<String, Object> map) {

        if (clz == Map.class)
            return (T) map;

        List<Field> filedList = new ArrayList<>();

        if (clz.getSuperclass() != Object.class) {
            filedList.addAll(Arrays.asList(clz.getSuperclass().getDeclaredFields()));
        }
        filedList.addAll(Arrays.asList(clz.getDeclaredFields()));

        T obj = null;
        try {
            obj = clz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }

        try {
            for (Field field : filedList) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers))
                    continue;
                if (Modifier.isFinal(modifiers))
                    continue;

                field.setAccessible(true);

                String key = field.getName();
                Object v = map.get(key);
                if (v == null)
                    continue;

                if (v instanceof Map) {
                    Class fc = field.getType();
                    field.set(obj, copy(fc, (Map) v));
                }else {
                    field.set(obj, v);
                }
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
        return obj;
    }
}

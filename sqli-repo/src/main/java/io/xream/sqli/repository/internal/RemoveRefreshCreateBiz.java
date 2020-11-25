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
package io.xream.sqli.repository.internal;

import io.xream.sqli.builder.RemoveRefreshCreate;
import io.xream.sqli.core.KeyOne;
import io.xream.sqli.core.Repository;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliJsonUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @Author Sim
 */
public final class RemoveRefreshCreateBiz {

    protected static <T> boolean doIt(Class<T> clzz, Repository repository, RemoveRefreshCreate wrapper) {

        Objects.requireNonNull(wrapper, "removeRefreshCreate(wrapper),wrapper is null");

        Map<Object,T> map = new HashMap<>();
        List<T> entityList = wrapper.getList();
        if (entityList != null && !entityList.isEmpty()) {
            List<T> list = null;
            Object obj = entityList.get(0);
            if (obj.getClass() != clzz) {
                list = new ArrayList<>();
                for (Object o : entityList) {
                    T t = SqliJsonUtil.toObject(o, clzz);
                    list.add(t);
                }
            } else {
                list = entityList;
            }

            Parsed parsed = Parser.get(clzz);
            Field f = parsed.getKeyField();
            try {
                for (T t : list) {
                    Object id = f.get(t);
                    map.put(id, t);
                }
            } catch (Exception e) {
                SqliExceptionUtil.throwRuntimeExceptionFirst(e);
                throw new ParsingException(SqliExceptionUtil.getMessage(e));
            }
        }

        Object[] ins = wrapper.getIns();

        List<Object> inList = new ArrayList<>();
        if (ins != null ) {
            for (Object in : ins){
                if (in == null)
                    continue;
                String strId = String.valueOf(in);
                if (SqliStringUtil.isNullOrEmpty(strId))
                    continue;
                inList.add(strId);
            }
        }

        /*
         * remove
         */
        Iterator<Object> existIte = inList.iterator();
        while (existIte.hasNext()) {
            Object id = existIte.next();
            if (map.containsKey(id))
                continue;
            existIte.remove();
            repository.remove(new KeyOne<Object>() {
                @Override
                public Object get() {
                    return id;
                }

                @Override
                public Class<Object> getClzz() {
                    return (Class<Object>) clzz;
                }
            });
        }

        /*
         * refreshOrCreate
         */
        for (Map.Entry<Object,T> entry : map.entrySet()) {
            Object id = entry.getKey();
            T t = entry.getValue();
            if (inList.contains(id)) {
                repository.refresh(t);
            }else {
                repository.create(t);
            }

        }

        return true;
    }
}

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
package io.xream.sqli.core.internal;

import io.xream.sqli.annotation.X;
import io.xream.sqli.builder.RemoveRefreshCreate;
import io.xream.sqli.core.KeyOne;
import io.xream.sqli.core.Repository;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.JsonWrapper;
import io.xream.sqli.util.SqliExceptionUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @Author Sim
 */
public final class RemoveRefreshCreateBiz {

    protected static <T> boolean doIt(Class<T> clz, Repository repository, RemoveRefreshCreate wrapper) {

        Objects.requireNonNull(wrapper, "removeRefreshCreate(wrapper),wrapper is null");

        if (wrapper.getList() == null || wrapper.getList().isEmpty())
            return false;
        List<T> list = null;
        Object obj = wrapper.getList().get(0);
        if (obj.getClass() != clz){
            list = new ArrayList<>();
            for (Object o : wrapper.getList()){
                T t = JsonWrapper.toObject(o,clz);
                list.add(t);
            }
        }else{
            list = wrapper.getList();
        }

        Object[] ins = wrapper.getIns();

        List<String> inList = new ArrayList<>();
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

        final Parsed parsed = Parser.get(clz);
        Field f = parsed.getKeyField(X.KEY_ONE);
        Map<String,T> map = new HashMap<>();
        try {
            for (T t : list) {
                Object id = f.get(t);
                map.put(String.valueOf(id), t);
            }
        }catch (Exception e){
            SqliExceptionUtil.throwRuntimeExceptionFirst(e);
            throw new ParsingException(SqliExceptionUtil.getMessage(e));
        }


        /*
         * remove
         */
        Iterator<String> existIte = inList.iterator();
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
                    return parsed.getClzz();
                }
            });
        }

        /*
         * refreshOrCreate
         */
        for (Map.Entry<String,T> entry : map.entrySet()) {
            String id = entry.getKey();
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

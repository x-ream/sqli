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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.xream.sqli.core.Routable;

import java.util.List;


/**
 * @Author Sim
 */
public final class RemoveRefreshCreate<T> implements Routable {

    private Object routeKey;
    @JsonIgnore
    private transient Class clz;

    private List<T> list;
    private Object[] ins;

    @Deprecated
    public RemoveRefreshCreate(){}
    public static <T> RemoveRefreshCreate of(List<T> list, Object[] ins){
        return of(null,list,ins);
    }

    public static <T> RemoveRefreshCreate of(Object routeKey, List<T> list, Object[] ins){
        RemoveRefreshCreate rrc =  new RemoveRefreshCreate();
        rrc.routeKey = routeKey;
        rrc.list = list;
        rrc.ins = ins;
        return rrc;
    }

    @Override
    public Object getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Object routeKey) {
        this.routeKey = routeKey;
    }

    public Class getClz() {
        return clz;
    }

    public void setClz(Class clz) {
        this.clz = clz;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Object[] getIns() {
        return ins;
    }

    public void setIns(Object[] ins) {
        this.ins = ins;
    }

    @Override
    public String toString() {
        return "RemoveRefreshCreate{" +
                "routeKey=" + routeKey +
                ", list=" + list +
                ", ins=" + ins +
                '}';
    }
}

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
package io.xream.sqli.api;

import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.Qr;
import io.xream.sqli.builder.RemoveRefreshCreate;
import io.xream.sqli.core.RowHandler;
import io.xream.sqli.core.Typed;
import io.xream.sqli.page.Page;

import java.util.List;

/**
 * API
 *
 * @param <T>
 * @author Sim
 */
public interface BaseRepository<T> extends Typed<T> {

    long createId();

    void refreshCache();

    boolean createBatch(List<T> objList);

    boolean create(T obj);

    /**
     * replace: clear all the value of the row, and insert the new value </>
     * is not refreshOrCreate </>
     * x7 will not support refreshOrCreate, coding: query at first, then refresh of create <br>
     */
    boolean createOrReplace(T obj);

    boolean refresh(Qr<T> qr);

    /**
     *
     *  qr without keyOne
     */
    boolean refreshUnSafe(Qr<T> qr);

    boolean remove(String id);

    boolean remove(long id);

    boolean removeIn(List<? extends Object> idList);

    /**
     *
     * caution:  sometimes, should not use the api </>
     *
     */
    boolean removeRefreshCreate(RemoveRefreshCreate<T> RemoveRefreshCreate_of);
    /**
     * @param keyOne
     */
    T get(long keyOne);

    T get(String keyOne);

    /**
     * LOAD
     *
     */
    List<T> list();

    /**
     * 根据对象查询
     *
     * @param conditionObj
     */
    @Deprecated
    List<T> list(T conditionObj);

    @Deprecated
    T getOne(T conditionObj);

    T getOne(Q<T> q);
    /**
     * in API
     *
     */
    List<T> in(String property, List<? extends Object> inList);

    /**
     * Standard query pageable API
     *
     * @param q
     */
    Page<T> find(Q<T> q);

    List<T> list(Q<T> q);

    /**
     * like stream, fetchSize=50, the api not fast, to avoid OOM when scheduling
     * @param q
     * @param handler
     * @param <T>
     */
    <T> void findToHandle(Q<T> q, RowHandler<T> handler);

    boolean exists(Q<T> q);
    
}
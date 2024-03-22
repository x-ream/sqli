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
import io.xream.sqli.core.RowHandler;
import io.xream.sqli.page.Page;

import java.util.List;
import java.util.Map;

/**
 * X API
 * @author Sim
 */
public interface RepositoryX {

    Page<Map<String, Object>> findX(Q.X xq);

    List<Map<String, Object>> listX(Q.X xq);

    <K> List<K> listPlainValue(Class<K> clzz, Q.X xq);

    <K> K getPlainValue(Class<K> clzz, Q.X xq);
    /**
     * like stream, fetchSize=50, the api not fast, to avoid OOM when scheduling
     * @param xq
     * @param handler
     */
    void findToHandleX(Q.X xq, RowHandler<Map<String, Object>> handler);
}

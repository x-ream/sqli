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
package io.xream.sqli.repository.api;

import io.xream.sqli.builder.Criteria;
import io.xream.sqli.repository.core.RowHandler;
import io.xream.sqli.page.Page;

import java.util.List;
import java.util.Map;

/**
 * ResultMap API
 * @Author Sim
 */
public interface ResultMapRepository {

    Page<Map<String, Object>> find(Criteria.ResultMapCriteria CriteriaBuilder_ResultMapBuilder_build_get);

    List<Map<String, Object>> list(Criteria.ResultMapCriteria CriteriaBuilder_ResultMapBuilder_build_get);

    <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria CriteriaBuilder_ResultMapBuilder_build_get);

    /**
     * like stream, fetchSize=50, the api not fast, to avoid OOM when scheduling
     * @param resultMapCriteria
     * @param handler
     */
    void findToHandle(Criteria.ResultMapCriteria resultMapCriteria, RowHandler<Map<String, Object>> handler);
}

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
package io.xream.sqli.repository.core;

import io.xream.sqli.repository.converter.DataObjectConverter;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author Sim
 */
public interface BaseFinder {

    <T> List<T> queryForList(String sql, Collection<Object> list, Parsed parsed, Dialect dialect);

    default <T> List<T> toObjectList(DataObjectQuery<T> dataObjectQuery) {
        return dataObjectQuery.query(DataObjectQuery.FIXED_ROW_MAPPER);
    }

    default <T> void toObject(T t, Map<String, Object> dataMap, List<BeanElement> list, Dialect dialect) throws Exception{
        DataObjectConverter.initObj(t,dataMap,list,dialect);
    }

    interface DataObjectQuery<T> {
        FixedRowMapper FIXED_ROW_MAPPER = (t, dataMap, beanElementList, dialect) -> DataObjectConverter.initObj(t,dataMap,beanElementList,dialect);
        List<T> query(FixedRowMapper<T> fixedObjectBuilder) ;
    }

    interface FixedRowMapper<T> {
        void mapRow(T t, Map<String, Object> dataMap, List<BeanElement> beanElementList, Dialect dialect) throws Exception ;
    }
}

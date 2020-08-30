/*
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

import io.xream.sqli.builder.Criteria;
import io.xream.sqli.converter.DataObjectConverter;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.JsonStyleMapUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author Sim
 */
public interface ResultMapFinder {

    List<Map<String, Object>> queryForResultMapList(String sql, Criteria.ResultMapCriteria resultMapped, Dialect dialect);

    <T> void queryForMapToHandle(String sql, Collection<Object> valueList, Dialect dialect, Criteria.ResultMapCriteria ResultMapCriteria, Parsed orParsed, RowHandler<T> handler);

    default List<Map<String, Object>> toResultMapList(boolean isResultWithDottedKey, DataMapQuery dataMapQuery) {

        List<Map<String, Object>> objectPropertyMapList = dataMapQuery.query(DataMapQuery.FIXED_ROW_MAPPER);
        if (isResultWithDottedKey)
            return objectPropertyMapList;

        if (!objectPropertyMapList.isEmpty())
            return JsonStyleMapUtil.toJsonableMapList(objectPropertyMapList);

        return objectPropertyMapList;
    }

    default Map<String,Object> toResultMap(Criteria.ResultMapCriteria resultMapCriteria, Dialect dialect, Map<String,Object> dataMap) {
        Map<String,Object> map = DataMapQuery.FIXED_ROW_MAPPER.mapRow(dataMap,null,resultMapCriteria,dialect);
        if (resultMapCriteria.isResultWithDottedKey())
            return map;
        if (!map.isEmpty())
            return JsonStyleMapUtil.toJsonableMap(map);
        return map;
    }

    interface DataMapQuery {
        FixedRowMapper FIXED_ROW_MAPPER = (dataMap, clzz, resultMapCriteria, dialect) -> DataObjectConverter.toMapWithKeyOfObjectProperty(dataMap,clzz,resultMapCriteria,dialect);
        List<Map<String,Object>> query(FixedRowMapper fixedRowMapper);
    }

    interface FixedRowMapper{
        Map<String,Object> mapRow(Map<String,Object> dataMap, Class clzz, Criteria.ResultMapCriteria resultMapCriteria, Dialect dialect);
    }

}

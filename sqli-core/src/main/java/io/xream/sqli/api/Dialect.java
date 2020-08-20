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


import io.xream.sqli.core.builder.BeanElement;
import io.xream.sqli.core.builder.Criteria;

import java.util.Collection;
import java.util.Map;

/**
 * @Author Sim
 */
public interface Dialect {

    String DATE = " ${DATE}";
    String BYTE = " ${BYTE}";
    String INT = " ${INT}";
    String LONG = " ${LONG}";
    String BIG = " ${BIG}";
    String STRING = " ${STRING}";
    String TEXT = " ${TEXT}";
    String LONG_TEXT = " ${LONG_TEXT}";
    String INCREAMENT = " ${INCREAMENT}";
    String ENGINE = " ${ENGINE}";


    String match(String sql, long start, long rows);

    String match(String sql, String sqlType);

    String transformAlia(String mapper, Map<String, String> aliaMap, Map<String, String> resultKeyAliaMap) ;

    Object filterValue(Object value);

    String resultKeyAlian(String mapper, Criteria.ResultMappedCriteria criteria);

    Object[] toArr(Collection<Object> list);

    Object mappingToObject(Object obj, BeanElement element);

    String createOrReplaceSql(String sql);
}
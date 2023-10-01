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
package io.xream.sqli.dialect;


import io.xream.sqli.builder.internal.DialectSupport;
import io.xream.sqli.builder.internal.PageSqlSupport;
import io.xream.sqli.core.ValuePost;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.BeanUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Sim
 */
public interface Dialect extends DialectSupport, PageSqlSupport, ValuePost {


    String transformAlia(String mapper, Map<String, String> aliaMap, Map<String, String> resultKeyAliaMap);

    Object[] toArr(Collection<Object> list);

    Object mappingToObject(Object obj, BeanElement element);

    String createOrReplaceSql(String sql);

    String createSql(Parsed parsed, List<BeanElement> tempList);

    default String getDefaultCreateSql(Parsed parsed, List<BeanElement> tempList) {
        String space = " ";
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");

        sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);
        sb.append("(");
        int size = tempList.size();
        for (int i = 0; i < size; i++) {
            String p = tempList.get(i).getProperty();

            sb.append(" ").append(p).append(" ");
            if (i < size - 1) {
                sb.append(",");
            }
        }

        sb.append(") VALUES (");

        for (int i = 0; i < size; i++) {

            sb.append("?");
            if (i < size - 1) {
                sb.append(",");
            }
        }
        sb.append(")");

        return sb.toString();
    }
}
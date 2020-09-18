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
package io.xream.sqli.repository.cache;

import io.xream.sqli.builder.InCondition;
import io.xream.sqli.filter.BaseTypeFilter;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author Sim
 */
public interface InOptimization {

    int IN_MAX = 500;

    static  <T> List<T> in(InCondition inCondition, CacheableRepository repository){

        if (inCondition.getInList().isEmpty())
            return new ArrayList<T>();

        List<Object> inList = new ArrayList<Object>();

        for (Object obj : inCondition.getInList()) {
            if (Objects.isNull(obj))
                continue;
            Parsed parsed = Parser.get(inCondition.getClz());
            if (BaseTypeFilter.isBaseType(inCondition.getProperty(), obj, parsed))
                continue;
            if (!inList.contains(obj)) {
                inList.add(obj);
            }
        }

        if (inList.isEmpty())
            return new ArrayList<T>();

        int size = inList.size();

        if (size <= IN_MAX) {
            inCondition.setInList(inList);
            return repository.in0(inCondition);
        }

        List<T> list = new ArrayList<>(size);
        int i = 0;
        while (size > 0) {
            int segSize = (size > IN_MAX ? IN_MAX : size);
            size -= segSize;
            int fromIndex = i++ * IN_MAX;
            int toIndex = fromIndex + segSize;
            List<? extends Object> segInList = inList.subList(fromIndex, toIndex);

            InCondition ic = InCondition.of(inCondition.getProperty(), segInList);
            ic.setClz(inCondition.getClz());
            List<T> segList = repository.in0(ic);
            list.addAll(segList);
        }

        return list;
    }

    static String keyCondition(InCondition inCondition) {
        String inProperty = inCondition.getProperty();
        List<? extends Object> inList = inCondition.getInList();

        StringBuilder sb = new StringBuilder();
        sb.append(inProperty).append(":");
        for (Object obj : inList) {
            sb.append(obj.toString()).append("_");
        }
        return sb.toString();
    }
}

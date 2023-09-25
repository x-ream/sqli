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

import io.xream.sqli.builder.In;
import io.xream.sqli.filter.BaseTypeFilter;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Sim
 */
public interface InOptimization {

    int IN_MAX = 500;

    static  <T> List<T> in(In in, CacheableRepository repository){

        if (in.getInList().isEmpty())
            return new ArrayList<T>();

        List<Object> inList = new ArrayList<Object>();

        for (Object obj : in.getInList()) {
            if (Objects.isNull(obj))
                continue;
            Parsed parsed = Parser.get(in.getClz());
            if (BaseTypeFilter.isBaseType(in.getProperty(), obj, parsed))
                continue;
            if (!inList.contains(obj)) {
                inList.add(obj);
            }
        }

        if (inList.isEmpty())
            return new ArrayList<T>();

        int size = inList.size();

        if (size <= IN_MAX) {
            in.setInList(inList);
            return repository.in0(in);
        }

        List<T> list = new ArrayList<>(size);
        int i = 0;
        while (size > 0) {
            int segSize = (size > IN_MAX ? IN_MAX : size);
            size -= segSize;
            int fromIndex = i++ * IN_MAX;
            int toIndex = fromIndex + segSize;
            List<? extends Object> segInList = inList.subList(fromIndex, toIndex);

            In ic = In.of(in.getProperty(), segInList);
            ic.setClz(in.getClz());
            List<T> segList = repository.in0(ic);
            list.addAll(segList);
        }

        return list;
    }

    static String keyCondition(In in) {
        String inProperty = in.getProperty();
        List<? extends Object> inList = in.getInList();

        StringBuilder sb = new StringBuilder();
        sb.append(inProperty).append(":");
        for (Object obj : inList) {
            sb.append(obj.toString()).append("_");
        }
        return sb.toString();
    }
}

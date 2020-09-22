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
package io.xream.sqli.mapping;

import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliStringUtil;

/**
 * @Author Sim
 */
public interface Mapper {

    default void mapping(ScriptSplitable scriptSplitable, Mappable mappable, StringBuilder sb) {
        String[] keyArr = scriptSplitable.split(Script.SPACE);
        int length = keyArr.length;
        for (int i = 0; i < length; i++) {
            String origin = keyArr[i].trim();

            String target = mapping(origin, mappable);
            sb.append(target).append(Script.SPACE);
        }
    }

    default String mapping(String key, Mappable mappable) {

        if (SqliStringUtil.isNullOrEmpty(key))
            return key;
        if (key.contains(Script.DOT)) {

            String[] arr = key.split("\\.");
            String alia = arr[0];
            String property = arr[1];

            String clzName = ParserUtil.getClzName(alia, mappable.getAliaMap());

            Parsed parsed = Parser.get(clzName);
            if (parsed == null)
                return key;

            String p = parsed.getMapper(property);
            if (SqliStringUtil.isNullOrEmpty(p)) {
                return mappable.getResultKeyAliaMap().get(key);
            }

            return parsed.getTableName(alia) + Script.DOT + p;
        }

        /*
         * if (anyCloumn != anyTableName) {
         *     if (anyProperty.equals(firstLetterLower(anyClzz.getSimpleName))) {
         *         throw new NotSupportedException();
         *     }
         * }
         *
         * if (anyTableAlia.equals(firstLetterLower(anyClzz.getSimpleName))) {
         *    throw new NotSupportedException();
         * }
         */
        Parsed parsed = Parser.get(key);
        if (parsed != null) {
            return parsed.getTableName();
        }

        parsed = mappable.getParsed();
        if (parsed == null)
            return key;
        String value = parsed.getMapper(key);
        return value == null ? key : value;
    }

    interface ScriptSplitable {
        String[] split(String reg);
    }

}

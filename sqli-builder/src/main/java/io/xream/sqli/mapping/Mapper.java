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

import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sim
 */
public interface Mapper {

    default List<String> mapping(ScriptSplitable scriptSplitable, Mappable mappable, StringBuilder sb) {
        String[] keyArr = scriptSplitable.split(Script.SPACE);
        int length = keyArr.length;
        List<String> originList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            String origin = keyArr[i].trim();
            originList.add(origin);
            String target = mapping(origin, mappable);
            sb.append(target).append(Script.SPACE);
        }
        return originList;
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

            String m = parsed.getMapper(property);
            if (SqliStringUtil.isNullOrEmpty(m)) {
                String s = mappable.getResultKeyAliaMap().get(key);
                if (SqliStringUtil.isNullOrEmpty(s))
                    throw new ParsingException(key);
                return s;
            }

            return parsed.getTableName(alia) + Script.DOT + m;
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

    default Class mapClzz(String key, Mappable mappable) {

        if (key.contains(Script.DOT)) {

            if (mappable == null)
                return String.class;

            String[] arr = key.split("\\.");
            String alia = arr[0];
            String property = arr[1];

            String clzName = ParserUtil.getClzName(alia, mappable.getAliaMap());

            Parsed parsed = Parser.get(clzName);
            if (parsed != null) {
                return parsed.getElement(property).getClz();
            } else {
                return String.class;
            }
        }

        Parsed parsed = mappable.getParsed();
        if (parsed != null)
            return parsed.getElement(key).getClz();

        return String.class;
    }


    interface ScriptSplitable {
        String[] split(String reg);
    }

}

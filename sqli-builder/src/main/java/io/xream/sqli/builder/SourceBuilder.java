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
package io.xream.sqli.builder;


import io.xream.sqli.exception.NotSupportedException;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.SqliStringUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Sim
 */
public interface SourceBuilder {

    SourceBuilder source(String source);

    SourceBuilder source(Class clzz);

    SourceBuilder sub(Sub sub);

    SourceBuilder with(Sub sub);

    SourceBuilder alia(String alia);

    SourceBuilder join(JoinType joinType);

    SourceBuilder join(String joinStr);

    SourceBuilder on(String key, JoinFrom joinFrom);

    SourceBuilder on(String key, Op op, JoinFrom joinFrom);

    BbQBuilder more();

    QB.X build();


    static void checkSourceAndAlia(List<SourceScript> list) {
        for (SourceScript sourceScript : list) {
            final String source = sourceScript.getSource();
            if (SqliStringUtil.isNotNull(source) && !Parser.contains(source)) {
                String tip = "";
                if (sourceScript.getJoinType() != null) {
                    tip += sourceScript.getJoinType().name().replace("_", " ");
                } else if (SqliStringUtil.isNotNull(sourceScript.getJoinStr())) {
                    tip += sourceScript.getJoinStr();
                } else {
                    tip += SqlScript.FROM;
                }
                throw new ParsingException(tip + SqlScript.SPACE + source);
            }
            String alia = sourceScript.getAlia();
            if (source != null && alia != null && !alia.equals(source) && Parser.contains(alia)) {
                throw new NotSupportedException("not support table alia = firstLetterLower(parsedEntityName), name+alia: " + source + " " + alia);
            }
        }
    }


    /**
     * @param sourceScriptsSplittedList
     * @return
     */
    static List<SourceScript> parseScriptAndBuild(List<String> sourceScriptsSplittedList, List<Object> sourceScriptValues) {

        List<Object> sourceScriptValueList = new LinkedList<>();
        if (sourceScriptValues != null) {
            sourceScriptValueList.addAll(sourceScriptValues);
        }

        List<SourceScript> list = new ArrayList<>();

        SourceScript sourceScript = null;
        int size = sourceScriptsSplittedList.size();
        for (int i = 0; i < size; i++) {
            String str = sourceScriptsSplittedList.get(i);
            String strUpper = str.toUpperCase();

            if (strUpper.equals("AND") || strUpper.equals("OR")) {

                sourceScript = getLast(list);
                List<Bb> bbList = sourceScript.getBbList();
                if (bbList == null) {
                    bbList = new ArrayList<>();
                    sourceScript.setBbList(bbList);
                }

                int j = i;
                for (;j<size;j++) {
                    if (BbQParser.JOIN_SET.contains(sourceScriptsSplittedList.get(j).toUpperCase())){
                        break;
                    }
                }

                i = BbQParser.parse(i, sourceScriptsSplittedList,
                        bbList, sourceScriptValueList);

                if (i == -1){
                    i = j - 1;
                }

                continue;
            }

            if ("FROM".equals(strUpper))
                continue;

            switch (strUpper) {
                case "INNER":
                    sourceScript = createAndGet(list);
                    sourceScript.setJoinType(JoinType.INNER_JOIN);
                    i++;
                    break;
                case "LEFT":
                    sourceScript = createAndGet(list);
                    sourceScript.setJoinType(JoinType.LEFT_JOIN);
                    i++;
                    break;
                case "RIGHT":
                    sourceScript = createAndGet(list);
                    sourceScript.setJoinType(JoinType.RIGHT_JOIN);
                    i++;
                    break;
                case "OUTER":
                    sourceScript = createAndGet(list);
                    sourceScript.setJoinType(JoinType.OUTER_JOIN);
                    i++;
                    break;
                case "FULL":
                    sourceScript = createAndGet(list);
                    sourceScript.setJoinType(JoinType.JOIN);
                    i++;
                    break;
                case "JOIN":
                    sourceScript = createAndGet(list);
                    sourceScript.setJoinType(JoinType.JOIN);
                    break;
                case ",":
                    sourceScript = createAndGet(list);
                    sourceScript.setJoinType(JoinType.COMMA);
                    break;
                case "ON":
                    String selfKey = sourceScriptsSplittedList.get(++i);
                    String op = sourceScriptsSplittedList.get(++i);// op
                    String fromKey = sourceScriptsSplittedList.get(++i);
                    if (fromKey.startsWith(sourceScript.getSource()) || (sourceScript.getAlia() != null && fromKey.startsWith(sourceScript.getAlia()))) {
                        String temp = selfKey;
                        selfKey = fromKey;
                        fromKey = temp;
                    }

                    int selfIndex = selfKey.indexOf(".");
                    int fromIndex = fromKey.indexOf(".");

                    JoinFrom joinFrom = new JoinFrom();
                    joinFrom.setAlia(fromKey.substring(0, fromIndex));
                    joinFrom.setKey(fromKey.substring(fromIndex + 1));
                    On on = new On();
                    on.setKey(selfKey.substring(selfIndex + 1));
                    on.setOp(op);
                    on.setJoinFrom(joinFrom);
                    sourceScript.setOn(on);
                    break;
                default:
                    if (sourceScript == null) {
                        sourceScript = createAndGet(list);
                    }
                    sourceScript.setSource(str);
                    if (i < size - 1) {
                        String tryAlia = sourceScriptsSplittedList.get(i + 1);
                        if (!SqlScript.SOURCE_SCRIPT.contains(tryAlia.toUpperCase())) {
                            sourceScript.setAlia(tryAlia);
                            i++;
                        }
                    }
                    break;
            }
        }

        return list;
    }

    static List<String> split(String script) {
        String[] opArrTwo = {"!=", "<>", "<=", ">="};
        String[] opArrTwoTemp = {"&ne", "&ne", "&lte", "&gte"};
        String[] opArrOne = {"=", "<", ">"};

        String sourceScript = script;
        boolean flag = false;
        for (int i = 0; i < 4; i++) {
            if (sourceScript.contains(opArrTwo[i])) {
                flag = true;
                sourceScript = sourceScript.replace(opArrTwo[i], opArrTwoTemp[i]);
            }
        }

        for (String op : opArrOne) {
            if (sourceScript.contains(op))
                sourceScript = sourceScript.replace(op, " " + op + " ");
        }

        if (flag) {
            for (int i = 0; i < 4; i++) {
                if (sourceScript.contains(opArrTwoTemp[i]))
                    sourceScript = sourceScript.replace(opArrTwoTemp[i], " " + opArrTwo[i] + " ");
            }
        }

        if (sourceScript.contains(",")) {
            sourceScript = sourceScript.replace(",", " , ");
        }
        String[] arr = sourceScript.split(" ");
        List<String> list = new ArrayList<>();
        for (String str : arr) {
            if (SqliStringUtil.isNotNull(str))
                list.add(str);
        }
        return list;
    }

    static SourceScript createAndGet(List<SourceScript> list) {
        SourceScript sourceScript = new SourceScript();
        list.add(sourceScript);
        return sourceScript;
    }

    static SourceScript getLast(List<SourceScript> list) {
        return list.get(list.size() - 1);
    }

}

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
package io.xream.sqli.builder;


import io.xream.sqli.util.SqliStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Sim
 */
public interface SourceScriptBuilder {
    SourceScriptBuilder source(String source);

    SourceScriptBuilder sub(Sub sub);

    SourceScriptBuilder alia(String alia);

    SourceScriptBuilder join(JoinType joinType);

    SourceScriptBuilder join(String joinStr);

    SourceScriptBuilder on(String key, JoinFrom joinFrom);

    SourceScriptBuilder on(String key, Op op, JoinFrom joinFrom);

    ConditionCriteriaBuilder more();

    /**
     * NOT SUPPORT CONDITION(AND|OR) OF lEFT JOIN | RIGHT JOIN
     *
     * @param sourceScriptsSplittedList
     * @return
     */
    static List<SourceScript> parse(List<String> sourceScriptsSplittedList) {

        List<SourceScript> list = new ArrayList<>();

        SourceScript sourceScript = null;
        int size = sourceScriptsSplittedList.size();
        for (int i = 0; i < size; i++) {
            String str = sourceScriptsSplittedList.get(i);
            String strUpper = str.toUpperCase();
            if (strUpper.equals("AND") || strUpper.equals("OR"))
                throw new IllegalArgumentException("SourceScript String does not support ON AND | OR, try to call builder.sourceScript()");

            if ("from".equals(str.toLowerCase()))
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

}

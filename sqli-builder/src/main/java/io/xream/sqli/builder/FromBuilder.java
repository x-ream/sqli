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


import io.xream.sqli.builder.internal.*;
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
public interface FromBuilder {


    FromBuilder of(Class clz);

    FromBuilder JOIN(JoinType joinType);
    FromBuilder JOIN(String joinStr);

    FromBuilder of(Class clz, String alia);
    FromBuilder sub(Sub sub, String alia);
    FromBuilder with(Sub sub, String alia);

    FromBuilder on(String onSql);
    FromBuilder on(String onSql, On on);


    static void checkSourceAndAlia(List<Froms> list) {
        for (Froms froms : list) {
            final String source = froms.getSource();
            if (SqliStringUtil.isNotNull(source) && !Parser.contains(source)) {
                String tip = "";
                if (froms.getJoin().getJoin() != null) {
                    tip += froms.getJoin().getJoin().replace("_", " ");
                } else {
                    tip += SqlScript.FROM;
                }
                throw new ParsingException(tip + SqlScript.SPACE + source);
            }
            String alia = froms.getAlia();
            if (source != null && alia != null && !alia.equals(source) && Parser.contains(alia)) {
                throw new NotSupportedException("not support table alia = firstLetterLower(parsedEntityName), name+alia: " + source + " " + alia);
            }
        }
    }


    /**
     * @param sourceScriptsSplittedList
     * @return
     */
    static List<Froms> parseScriptAndBuild(List<String> sourceScriptsSplittedList, List<Object> sourceScriptValues) {

        List<Object> sourceScriptValueList = new LinkedList<>();
        if (sourceScriptValues != null) {
            sourceScriptValueList.addAll(sourceScriptValues);
        }

        List<Froms> list = new ArrayList<>();

        Froms froms = null;
        int size = sourceScriptsSplittedList.size();
        for (int i = 0; i < size; i++) {
            String str = sourceScriptsSplittedList.get(i);
            String strUpper = str.toUpperCase();

            if (strUpper.equals("AND") || strUpper.equals("OR")) {

                froms = getLast(list);
                List<Bb> bbList = froms.getJoin().getOn().getBbs();

                int j = i;
                for (;j<size;j++) {
                    if (CondQParser.JOIN_SET.contains(sourceScriptsSplittedList.get(j).toUpperCase())){
                        break;
                    }
                }

                i = CondQParser.parse(i, sourceScriptsSplittedList,
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
                    froms = createAndGet(list);
                    froms.getJoin().setJoin(JoinType.INNER);
                    i++;
                    break;
                case "LEFT":
                    froms = createAndGet(list);
                    froms.getJoin().setJoin(JoinType.LEFT);
                    i++;
                    break;
                case "RIGHT":
                    froms = createAndGet(list);
                    froms.getJoin().setJoin(JoinType.RIGHT);
                    i++;
                    break;
                case "OUTER":
                    froms = createAndGet(list);
                    froms.getJoin().setJoin(JoinType.OUTER);
                    i++;
                    break;
                case "FULL":
                    froms = createAndGet(list);
                    froms.getJoin().setJoin(JoinType.JOIN);
                    i++;
                    break;
                case "JOIN":
                    froms = createAndGet(list);
                    froms.getJoin().setJoin(JoinType.JOIN);
                    break;
                case ",":
                    froms = createAndGet(list);
                    froms.getJoin().setJoin(JoinType.NON_JOIN);
                    break;
                case "ON","AND","OR":
                    String selfKey = sourceScriptsSplittedList.get(++i);
                    String op = sourceScriptsSplittedList.get(++i);// op
                    String fromKey = sourceScriptsSplittedList.get(++i);
                    if (fromKey.startsWith(froms.getSource()) || (froms.getAlia() != null && fromKey.startsWith(froms.getAlia()))) {
                        String temp = selfKey;
                        selfKey = fromKey;
                        fromKey = temp;
                    }
                    Bb bb = new Bb();
                    bb.setC(Op.NONE);
                    bb.setP(Op.X);
                    bb.setKey(selfKey + " " + op + " " + fromKey);
                    froms.getJoin().getOn().getBbs().add(bb);

                    break;
                default:
                    if (froms == null) {
                        froms = createAndGet(list);
                    }
                    froms.setSource(str);
                    if (i < size - 1) {
                        String tryAlia = sourceScriptsSplittedList.get(i + 1);
                        if (!SqlScript.SOURCE_SCRIPT.contains(tryAlia.toUpperCase())) {
                            froms.setAlia(tryAlia);
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

    static Froms createAndGet(List<Froms> list) {
        Froms froms = new Froms();
        ON on = new ON();
        JOIN join = new JOIN();
        join.setOn(on);
        froms.setJoin(join);
        list.add(froms);
        return froms;
    }

    static Froms getLast(List<Froms> list) {
        return list.get(list.size() - 1);
    }

}

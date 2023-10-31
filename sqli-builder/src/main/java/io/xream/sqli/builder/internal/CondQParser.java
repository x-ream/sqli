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
package io.xream.sqli.builder.internal;

import io.xream.sqli.builder.Op;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sim
 */
public interface CondQParser {

    Set<String> JOIN_SET = new HashSet<String>(){
        {
            add("INNER");
            add("LEFT");
            add("RIGHT");
            add("OUTER");
            add("FULL");
            add("JOIN");
            add(",");
        }
    };

    static int parse(int i, List<String> conditionScriptSplittedList,
                         List<Bb> bbList, List<Object> valueList) {
        int size = conditionScriptSplittedList.size();

        Bb bb = null;
        while (true) {
            if (i > size - 1)
                return i;
            String s = conditionScriptSplittedList.get(i++);

            if (s.equals(SqlScript.PLACE_HOLDER)) { // ?
                Object v = valueList.remove(0);
                if (v == null || (v instanceof List && ((List) v).isEmpty())) {
                    bbList.remove(bbList.size()-1);
                }
                bb.setValue(v);
            }

            String u = s.toUpperCase();
            if (s.startsWith("(")) {
                bb = new Bb();
                bb.setKey("");
                bb.setC(Op.AND);
                bb.setP(Op.SUB);
                bbList.add(bb);
                List<Bb> subList = new ArrayList<>();
                bb.setSubList(subList);
                i = parse(i, conditionScriptSplittedList,
                        subList, valueList);
                continue;
            }

            else if (s.startsWith(")")) {
                return i;
            }

            else if (u.startsWith("AND") || u.startsWith("OR")) {

                String next = conditionScriptSplittedList.get(i);
                if (next.equals("(")){
                    bb = new Bb();
                    bb.setKey("");
                    bb.setC(Op.valueOf(u));
                    bb.setP(Op.SUB);
                    bbList.add(bb);
                    List<Bb> subList = new ArrayList<>();
                    bb.setSubList(subList);
                    i++;
                    i = parse(i, conditionScriptSplittedList,
                            subList, valueList);
                    continue;
                }

                bb = new Bb();
                bb.setKey("");
                bb.setC(Op.valueOf(u));
                bb.setP(Op.X);
                bbList.add(bb);
            } else if (JOIN_SET.contains(s.toUpperCase())) {
                return -1;
            } else {
                if (bb == null) {
                    bb = new Bb();
                    bb.setKey("");
                    bb.setP(Op.X);
                    bb.setC(Op.AND);
                    bbList.add(bb);
                }
                bb.setKey(bb.getKey() + " " + s);
            }
        }
    }
}

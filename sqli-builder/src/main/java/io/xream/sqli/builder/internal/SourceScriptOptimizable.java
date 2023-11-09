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

import io.xream.sqli.util.SqliStringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sim
 */
public interface SourceScriptOptimizable {

    default void addConditonBeforeOptimization(String key, Set<String> conditionSet){
        conditionSet.add(key);
    }

    default void addConditionBeforeOptimization(List<Bb> bbList, Set<String> conditionSet) {
        if (bbList == null)
            return;
        for (Bb bb : bbList) {
            conditionSet.add(bb.getKey());
            List<Bb> subList = bb.getSubList();
            if (subList != null && !subList.isEmpty()) {
                addConditionBeforeOptimization(subList, conditionSet);
            }
        }
    }

    default void optimizeSourceScript(Set<String> conditionSet, List<Froms> fromsList) {
        if (fromsList.size() <= 1)
            return;
        if (conditionSet.size() > 0) {
            for (String test : conditionSet) {
                if (test != null) {
                    if (test.contains("."))
                        break;
                    return;
                }
            }
        }
        for (Froms froms : fromsList) {
            if (froms.getSubQ() != null) {
                froms.used();
                continue;
            }
            for (String key : conditionSet) {
                if (key == null)
                    continue;
                if (SqliStringUtil.isNullOrEmpty(froms.alia())) {
                    if (key.contains(froms.getSource() + ".")) {
                        froms.used();
                        break;
                    }
                } else {
                    if (key.contains(froms.alia() + ".")) {
                        froms.used();
                        break;
                    }
                }
            }
        }

        int size = fromsList.size();
        for (int i = size - 1; i >= 0; i--) {
            Froms from = fromsList.get(i);
            if (from.getSubQ() != null) {
                from.targeted();
                continue;
            }
//            if (!sourceScript.isUsed() && !sourceScript.isTargeted())
//                continue;
            if (from.getJoin() == null)
                continue;
            ON on = from.getJoin().getOn();
            if (on == null )
                continue;
            String str = on.getBbs().stream().map(Bb::getKey).collect(Collectors.joining(","));

            for (int j = i - 1; j >= 0; j--) {
                Froms sc = fromsList.get(j);
                if (sc.isTargeted())
                    continue;
                if (from.getSource().equals(sc.getSource()))
                    continue;
                //FIXME
                if (str.contains(sc.alia()+".")) {
                    sc.targeted();
                    break;
                }
            }
        }

        Iterator<Froms> ite = fromsList.iterator();
        while (ite.hasNext()) {
            Froms froms = ite.next();
            if (!froms.isUsed() && !froms.isTargeted())
                ite.remove();
        }
    }

}

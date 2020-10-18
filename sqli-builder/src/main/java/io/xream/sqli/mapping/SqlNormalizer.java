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

import io.xream.sqli.builder.SqlScript;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author Sim
 */
public interface SqlNormalizer {

    Set<String> OP_SET = new HashSet() {
        {
            add("=");
            add("!");
            add(">");
            add("<");
            add("+");
            add("-");
            add("*");
            add("/");
//            add("(");
//            add(")");
            add(";");
            add(":");
        }
    };

    default void normalizeFunctionParentThesis(int j, String strEle,StringBuilder valueSb, String handwritten) {
        if (strEle.equals(SqlScript.LEFT_PARENTTHESIS) && j - 1 > -1) {
            String pre = String.valueOf(handwritten.charAt(j - 1));
            if (pre.equals(SqlScript.SPACE) || OP_SET.contains(pre)) {
                valueSb.append(Script.SPACE);
            }
        }else{
            valueSb.append(Script.SPACE);
        }
    }



    default int normalizeParentThesis(int j, int length,String strEle, StringBuilder valueSb, String handwritten){

        normalizeFunctionParentThesis(j, strEle, valueSb, handwritten);

        valueSb.append(strEle);

        for (; j + 1 < length; ) {
            String nextOp = String.valueOf(handwritten.charAt(j + 1));
            if (nextOp.equals(strEle)) {
                valueSb.append(nextOp);
                j++;
            } else {
                break;
            }
        }
        valueSb.append(Script.SPACE);
        return j;
    }

    default int normalizeOp(int j, int length, String strEle, StringBuilder valueSb, String handwritten){
        valueSb.append(Script.SPACE).append(strEle);
        if (j + 1 < length) {
            String nextOp = String.valueOf(handwritten.charAt(j + 1));
            if (OP_SET.contains(nextOp)) {
                valueSb.append(nextOp);
                j++;
            }
        }
        valueSb.append(Script.SPACE);
        return j;
    }

    default String normalizeSql(final String handwritten) {
        StringBuilder valueSb = new StringBuilder();
        boolean ignored = false;
        int length = handwritten.length();
        for (int j = 0; j < length; j++) {
            String strEle = String.valueOf(handwritten.charAt(j));
            if (Script.SPACE.equals(strEle)) {
                ignored = true;
                continue;
            }

            if (strEle.equals(SqlScript.LEFT_PARENTTHESIS) || strEle.equals(SqlScript.RIGHT_PARENTTHESIS)) {
                j = normalizeParentThesis(j,length,strEle,valueSb,handwritten);
                continue;
            }

            if (OP_SET.contains(strEle)) {
                j = normalizeOp(j,length,strEle,valueSb,handwritten);
            } else {
                if (ignored)
                    valueSb.append(Script.SPACE);
                valueSb.append(strEle);
            }
            ignored = false;
        }
        return valueSb.toString();
    }

}

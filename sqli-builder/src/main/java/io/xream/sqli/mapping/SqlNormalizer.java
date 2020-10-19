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

import java.util.HashSet;
import java.util.Set;

/**
 * @Author Sim
 */
public interface SqlNormalizer extends Script{

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
            add(";");
            add(":");
        }
    };

    default void normalizeFunctionParentThesis(int idx, String strEle,StringBuilder valueSb, String handwritten) {
        if (strEle.equals(LEFT_PARENTTHESIS) && idx - 1 > -1) {
            String pre = String.valueOf(handwritten.charAt(idx - 1));
            if (pre.equals(SPACE) || OP_SET.contains(pre)) {
                valueSb.append(SPACE);
            }
        }else{
            valueSb.append(SPACE);
        }
    }

    default int normalizeParentThesis(int idx, int length,String strEle, StringBuilder valueSb, String handwritten){

        normalizeFunctionParentThesis(idx, strEle, valueSb, handwritten);

        valueSb.append(strEle);

        for (; idx + 1 < length; ) {
            String nextOp = String.valueOf(handwritten.charAt(idx + 1));
            if (nextOp.equals(strEle)) {
                valueSb.append(nextOp);
                idx++;
            } else {
                break;
            }
        }
        valueSb.append(SPACE);
        return idx;
    }

    default int normalizeOp(int idx, int length, String strEle, StringBuilder valueSb, String handwritten){
        valueSb.append(SPACE).append(strEle);
        if (idx + 1 < length) {
            String nextOp = String.valueOf(handwritten.charAt(idx + 1));
            if (OP_SET.contains(nextOp)) {
                valueSb.append(nextOp);
                idx++;
            }
        }
//        valueSb.append(SPACE);
        return idx;
    }

    default String normalizeSql(final String handwritten) {
        StringBuilder valueSb = new StringBuilder();
        boolean ignored = false;
        int length = handwritten.length();
        for (int j = 0; j < length; j++) {
            String strEle = String.valueOf(handwritten.charAt(j));
            if (SPACE.equals(strEle)) {
                ignored = true;
                continue;
            }

            if (strEle.equals(LEFT_PARENTTHESIS) || strEle.equals(RIGHT_PARENTTHESIS)) {
                j = normalizeParentThesis(j,length,strEle,valueSb,handwritten);
                continue;
            }

            if (OP_SET.contains(strEle)) {
                j = normalizeOp(j,length,strEle,valueSb,handwritten);
            } else {
                if (ignored)
                    valueSb.append(SPACE);
                valueSb.append(strEle);
                ignored = false;
            }

        }
        return valueSb.toString();
    }

}

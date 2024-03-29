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

/**
 * @author Sim
 */
public enum  Op {
    EQ ("="),
    NE( "<>"),
    GT(">"),
    LT ("<"),
    GTE (">="),
    LTE ("<="),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    IN("IN"),
    NOT_IN("NOT IN"),
    IS_NOT_NULL("IS NOT NULL"),
    IS_NULL("IS NULL"),
    X(""),
    LIMIT("LIMIT"),
    OFFSET("OFFSET"),
    SUB("SUB"),

    NONE(""),
    AND(" AND "),
    OR(" OR "),
    ORDER_BY(" ORDER BY "),
    GROUP_BY(" GROUP BY "),
    HAVING(" HAVING "),
    WHERE(" WHERE "),
    X_AGGR("");

    private String op;
    Op(String str){
        op = str;
    }
    public String sql(){
        return op;
    }

    public static Op valueOfSql(String str) {
        String t = str.trim();
        for (Op op : values()) {
            if (op.sql().equals(str))
                return op;
        }
        return NONE;
    }
}

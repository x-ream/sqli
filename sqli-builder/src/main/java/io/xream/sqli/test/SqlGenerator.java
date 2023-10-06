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
package io.xream.sqli.test;

import io.xream.sqli.builder.*;
import io.xream.sqli.builder.internal.DefaultQ2Sql;
import io.xream.sqli.builder.internal.Q2Sql;
import io.xream.sqli.builder.internal.SqlSubsAndValueBinding;
import io.xream.sqli.builder.internal.SqlBuilt;
import io.xream.sqli.parser.Parser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sim
 */
public class SqlGenerator {

    private static SqlGenerator instance;
    private static Q2Sql condToSql;

    private static List<KV> xKvList = new ArrayList<>();


    private SqlGenerator(){}

    public static SqlGenerator generator() {
        if (condToSql == null) {
            condToSql = DefaultQ2Sql.newInstance();
            instance = new SqlGenerator();
        }
        return instance;
    }

    public SqlGenerator source(Class<?> clzz) {
        Parser.parse(clzz);
        return instance;
    }

    public SqlGenerator build(String traceKey, Q.X xq){
        KV kv = KV.of(traceKey,xq);
        xKvList.add(kv);
        return instance;
    }

    public void generate(String fileName){

        StringBuilder sb = new StringBuilder();

        for (KV kv : xKvList) {

            SqlBuilt sqlBuilt = new SqlBuilt();

            List<Object> valueList = new ArrayList<>();
            List<SqlBuilt> sqlBuiltList = new ArrayList<>();

            SqlSubsAndValueBinding subsAndValueBinding = new SqlSubsAndValueBinding() {
                @Override
                public List<Object> getValueList() {
                    return valueList;
                }

                @Override
                public List<SqlBuilt> getSubList() {
                    return sqlBuiltList;
                }
            };
            condToSql.toSql(false,(Q.X) kv.getV(),sqlBuilt, subsAndValueBinding);

            sb.append("-- Test trace: " + kv.getK()).append("\r\n");
            sb.append("-- Test value: " + valueList).append("\r\n");
            sb.append(sqlBuilt.getSql()).append(";").append("\r\n");
            sb.append("-- -------------------------------------------").append("\r\n").append("\r\n");
        }

        write(fileName,sb);

    }

    private static void write(String fileName, StringBuilder sb) {
        try {
            File d = new File(".sql");
            if (! d.exists()){
                d.mkdir();
            }
            fileName = fileName.endsWith(".sql") ? fileName : fileName + ".sql";
            Files.write(Paths.get(".sql/" + fileName), sb.toString().getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            xKvList.clear();
        }
    }
}

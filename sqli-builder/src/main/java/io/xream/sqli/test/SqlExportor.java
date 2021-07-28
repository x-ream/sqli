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
import io.xream.sqli.builder.internal.DefaultCriteriaToSql;
import io.xream.sqli.parser.Parser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Sim
 */
public class SqlExportor {

    private static SqlExportor instance;
    private static CriteriaToSql criteriaToSql;

    private static List<KV> resultMapCriteriaList = new ArrayList<>();


    private SqlExportor(){}

    public static SqlExportor exportor() {
        if (criteriaToSql == null) {
            criteriaToSql = DefaultCriteriaToSql.newInstance();
            instance = new SqlExportor();
        }
        return instance;
    }

    public SqlExportor source(Class<?> clzz) {
        Parser.parse(clzz);
        return instance;
    }

    public SqlExportor build(String traceKey, Criteria.ResultMapCriteria resultMapCriteria){
        KV kv = new KV(traceKey,resultMapCriteria);
        resultMapCriteriaList.add(kv);
        return instance;
    }

    public void export(String fileName){

        StringBuilder sb = new StringBuilder();

        for (KV kv : resultMapCriteriaList) {

            SqlBuilt sqlBuilt = new SqlBuilt();

            List<Object> valueList = new ArrayList<>();
            List<SqlBuilt> sqlBuiltList = new ArrayList<>();

            SqlBuildingAttached sqlBuildingAttached = new SqlBuildingAttached() {
                @Override
                public List<Object> getValueList() {
                    return valueList;
                }

                @Override
                public List<SqlBuilt> getSubList() {
                    return sqlBuiltList;
                }
            };
            criteriaToSql.toSql(false,(Criteria.ResultMapCriteria) kv.getV(),sqlBuilt,sqlBuildingAttached);

            sb.append("-- Test trace: " + kv.getK()).append("\r\n");
            sb.append("-- Test value: " + valueList).append("\r\n");
            sb.append(sqlBuilt.getSql()).append("\r\n");
            sb.append("-- -------------------------------------------").append("\r\n").append("\r\n");
        }

        write(fileName,sb);

    }

    private static void write(String fileName, StringBuilder sb) {
        try {
            File d = new File(".sql");
            if (! d.exists()){
                d.mkdir();
            }else {
                File[] arr = d.listFiles();
                if (arr != null) {
                    for (File f : arr) {
                        f.delete();
                    }
                }
            }
            fileName = fileName.endsWith(".sql") ? fileName : fileName + ".sql";
            Files.write(Paths.get(".sql/" + fileName), sb.toString().getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            resultMapCriteriaList.clear();
        }
    }
}

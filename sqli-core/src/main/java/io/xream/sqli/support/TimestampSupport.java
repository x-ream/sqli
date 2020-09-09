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
package io.xream.sqli.support;

import io.xream.sqli.builder.BuildingBlock;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @Author Sim
 */
public final class TimestampSupport {

    public static boolean testNumberValueToDate(Class clzz, BuildingBlock buildingBlock){
        if (clzz == Date.class) {
            Object v = buildingBlock.getValue();
            if (v instanceof Long || v instanceof Integer) {
                if (Long.valueOf(v.toString()) == 0){
                    buildingBlock.setValue(null);
                }else {
                    buildingBlock.setValue(new Date(toLongValue(v)));
                }
            }
            return true;
        } else if (clzz == Timestamp.class) {
            Object v = buildingBlock.getValue();
            if (v instanceof Long || v instanceof Integer) {
                if (Long.valueOf(v.toString()) == 0){
                    buildingBlock.setValue(null);
                }else {
                    buildingBlock.setValue(new Timestamp(toLongValue(v)));
                }
            }
            return true;
        }

        return false;
    }

    private static long toLongValue(Object v){
        if (v instanceof Integer){
            return ((Integer) v).intValue();
        }else{
            return ((Long) v).longValue();
        }
    }



}

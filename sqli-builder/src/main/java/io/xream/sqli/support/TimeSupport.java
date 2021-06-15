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
package io.xream.sqli.support;

import io.xream.sqli.builder.Bb;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @Author Sim
 */
public final class TimeSupport {

    public static Object afterReadTime(Class propertyType, Object obj) {
        if (obj instanceof LocalDateTime) {
            if (propertyType == Date.class) {
                Instant instant = ((LocalDateTime)obj).atZone(ZoneId.systemDefault()).toInstant();
                obj = Date.from(instant);
            }else if (propertyType == Timestamp.class) {
                Instant instant = ((LocalDateTime)obj).atZone(ZoneId.systemDefault()).toInstant();
                obj = Timestamp.from(instant);
            }else if (propertyType == LocalDate.class) {
                obj = ((LocalDateTime)obj).toLocalDate();
            }
        } else if (obj instanceof Timestamp) {
            if (propertyType == LocalDateTime.class) {
                obj = Instant.ofEpochMilli(((Timestamp)obj).getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }else if (propertyType == Date.class) {
                obj = new Date(((Timestamp)obj).getTime());
            }else if (propertyType == LocalDate.class) {
                obj = Instant.ofEpochMilli(((Timestamp)obj).getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
        return obj;
    }

    public static boolean testWriteNumberValueToTime(Class propertyType, Bb bb){
       if (propertyType == LocalDateTime.class) {
            Object v = bb.getValue();
            if (v instanceof Long || v instanceof Integer) {
                if (Long.valueOf(v.toString()) == 0){
                    bb.setValue(null);
                }else {
                    LocalDateTime time = Instant.ofEpochMilli(toLongValue(v)).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    bb.setValue(time);
                }
            }
            return true;
        }else  if (propertyType == Date.class) {
           Object v = bb.getValue();
           if (v instanceof Long || v instanceof Integer) {
               if (Long.valueOf(v.toString()) == 0){
                   bb.setValue(null);
               }else {
                   bb.setValue(new Date(toLongValue(v)));
               }
           }
           return true;
       } else if (propertyType == Timestamp.class) {
           Object v = bb.getValue();
           if (v instanceof Long || v instanceof Integer) {
               if (Long.valueOf(v.toString()) == 0){
                   bb.setValue(null);
               }else {
                   bb.setValue(new Timestamp(toLongValue(v)));
               }
           }
           return true;
       } else if (propertyType == LocalDate.class) {
           Object v = bb.getValue();
           if (v instanceof Long || v instanceof Integer) {
               if (Long.valueOf(v.toString()) == 0){
                   bb.setValue(null);
               }else {
                   LocalDate date = Instant.ofEpochMilli(toLongValue(v)).atZone(ZoneId.systemDefault()).toLocalDate();
                   bb.setValue(date);
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

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
package io.xream.sqli.util;

import io.xream.sqli.support.EnumSupport;

/**
 * @author Sim
 */
public class EnumUtil {

    private static EnumSupport enumSupport;
    protected static void setEnumSupport(EnumSupport es) {
        enumSupport = es;
    }

    public static boolean isEnum(Class clzz) {
        Class superClzz = clzz.getSuperclass();
        return clzz.isEnum() || (superClzz != null && superClzz.isEnum());
    }

    public static Object serialize(Enum enumObj) {
        return enumSupport.serialize(enumObj);
    }

    public static Enum deserialize(Class<Enum> clzz, Object enumNameOrCode) {
        return enumSupport.deserialize(clzz, enumNameOrCode);
    }

    public static Object filterInComplexScriptSimply(Object maybeEnum) {
        if (maybeEnum == null)
            return null;
        if (isEnum(maybeEnum.getClass())) {
            return serialize((Enum) maybeEnum);
        }
        return maybeEnum;
    }

    public static Object serialize(Class<Enum> clzz, Object strOrEnum) {
        if (strOrEnum instanceof String){
            strOrEnum = deserialize(clzz,strOrEnum);
        }
        return serialize((Enum)strOrEnum);
    }
}

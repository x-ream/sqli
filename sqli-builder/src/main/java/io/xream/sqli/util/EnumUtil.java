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
 * @Author Sim
 */
public class EnumUtil {

    private static EnumSupport enumSupport;
    protected static void setEnumSupport(EnumSupport es) {
        enumSupport = es;
    }

    public static boolean isEnum(Class clz) {
        Class superClzz = clz.getSuperclass();
        return clz.isEnum() || (superClzz != null && superClzz.isEnum());
    }

    public static Object serialize(Enum obj) {
        return enumSupport.serialize(obj);
    }

    public static Enum deSerialize(Class<Enum> clzz, Object obj) {
        return enumSupport.deserialize(clzz, obj);
    }

    public static Object filter(Object obj) {
        if (obj == null)
            return null;
        if (isEnum(obj.getClass())) {
            return serialize((Enum) obj);
        }
        return obj;
    }
}

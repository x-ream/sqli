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
package io.xream.sqli.util;


/**
 * @Author Sim
 */
public class BeanUtil {

    protected BeanUtil(){}

    public static String getSetter(String property) {
        if (property.startsWith("is")) {
            String rest = property.substring(2);
            return "set" + rest;
        }

        String a = property.substring(0, 1);
        String rest = property.substring(1);
        return "set" + a.toUpperCase() + rest;
    }

    public static String getByFirstLower(String str) {
        if (SqliStringUtil.isNullOrEmpty(str))
            return str;

        String a = str.substring(0, 1);
        String rest = str.substring(1);
        String result = a.toLowerCase() + rest;
        return result;

    }

    public static String getByFirstUpper(String str) {
        if (SqliStringUtil.isNullOrEmpty(str))
            return str;

        String a = str.substring(0, 1);
        String rest = str.substring(1);
        String result = a.toUpperCase() + rest;
        return result;

    }

    public static String getProperty(String methodName) {
        if (methodName.startsWith("is"))
            return methodName;
        String str = methodName.substring(3);
        return getByFirstLower(str);
    }

    public static boolean isEnum(Class clz) {
        Class superClzz = clz.getSuperclass();
        return clz.isEnum() || (superClzz != null && superClzz.isEnum());
    }

}

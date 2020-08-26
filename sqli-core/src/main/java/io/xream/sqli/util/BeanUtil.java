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


import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


/**
 * @Author Sim
 */
public class BeanUtil {

    protected BeanUtil(){}

    public static String getSetter(Class<?> type, String property) {

        if (type != Boolean.class && property.startsWith("is")) {
            return "set" + property.substring(2);
        }

        String a = property.substring(0, 1);
        String rest = property.substring(1);
        return "set" + a.toUpperCase() + rest;
    }

    public static String getGetter(Class<?> type, String property) {
        if (type != Boolean.class) {
            if (property.startsWith("is")) {
                return property;
            }
        }
        String a = property.substring(0, 1);
        String rest = property.substring(1);
        return "get" + a.toUpperCase() + rest;
    }

    public static String getGetter(String property) {
        if (property.startsWith("is")) {
            return property;
        }
        String a = property.substring(0, 1);
        String rest = property.substring(1);
        return "get" + a.toUpperCase() + rest;
    }

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
        methodName = methodName.substring(3);
        return getByFirstLower(methodName);
    }

    public static String getPropertyOfBoolen(String setter) {
        return "is" + setter.substring(3);
    }



    /**
     * 通过setter拷贝值
     *
     * @param target
     * @param origin
     */
    public static void copy(Object target, Object origin) {

        if (origin == null || target == null)
            return;
        try {
            Class clz = target.getClass();

            Class oc = origin.getClass();

            Method[] originMethodArr = oc.getDeclaredMethods();

            Set<String> methodSet = new HashSet<String>();

            for (Method m : originMethodArr) {
                methodSet.add(m.getName());
            }

            for (Method m : clz.getDeclaredMethods()) {

                if (m.getName().startsWith("set")) {

                    String p = "";

                    if (m.getParameterTypes()[0] == boolean.class || m.getParameterTypes()[0] == Boolean.class) {
                        p = getPropertyOfBoolen(m.getName());
                    } else {
                        p = getProperty(m.getName());
                    }

                    String getter = getGetter(p);

                    if (!methodSet.contains(getter)) {
                        continue;
                    }

                    Object v = null;
                    try {
                        v = oc.getDeclaredMethod(getter).invoke(origin);
                    } catch (Exception e) {

                    }
                    if (v != null) {
                        m.invoke(target, v);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static boolean isEnum(Class clz) {
        Class superClzz = clz.getSuperclass();
        return clz.isEnum() || (superClzz != null && superClzz.isEnum());
    }

    public static boolean testEnumConstant(Class clz, Object value) {
        if (value instanceof String && isEnum(clz)){
            Enum.valueOf(clz, (String)value);
            return true;
        }
        return false;
    }

}

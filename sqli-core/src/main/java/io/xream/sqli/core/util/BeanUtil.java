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
package io.xream.sqli.core.util;

import io.xream.sqli.core.builder.FieldAndMethod;
import io.xream.sqli.core.builder.Parser;
import io.xream.sqli.core.repository.ReflectionCache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeanUtil {

    protected BeanUtil(){}

    public static String getSetter(Class<?> type, String property) {

        if (type != Boolean.class) {
            if (property.startsWith("is")) {
                String rest = property.substring(2);
                return "set" + rest;
            }
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
        if (SqlStringUtil.isNullOrEmpty(str))
            return str;

        String a = str.substring(0, 1);
        String rest = str.substring(1);
        String result = a.toLowerCase() + rest;
        return result;

    }

    public static String getByFirstUpper(String str) {
        if (SqlStringUtil.isNullOrEmpty(str))
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
     * @param clz
     * @param origin
     */
    public static <T> T copy(Class<T> clz, Object origin) {

        if (origin == null)
            return null;

        T t = null;
        String p = "";
        Object v = null;
        try {
            t = clz.newInstance();

            Class oc = origin.getClass();

            // Method[] originMethodArr = oc.getDeclaredMethods();
            ReflectionCache originCache = Parser.getReflectionCache(oc); // origin

            ReflectionCache cache = Parser.getReflectionCache(clz); // target

            for (FieldAndMethod fnm : cache.getMap().values()) {

                FieldAndMethod originFnm = originCache.get(fnm.getProperty());

                if (originFnm == null) {

                    originFnm = originCache.getTemp(fnm.getProperty());
                    /*
                     * 增加临时缓存
                     */
                    if (originFnm == null) {
                        originFnm = new FieldAndMethod(); // NEW
                        originCache.getTempMap().put(fnm.getProperty(), originFnm);

                        String getterName = fnm.getGetterName();
                        Method orginGetter = null;
                        try {
                            orginGetter = oc.getDeclaredMethod(getterName);
                        } catch (Exception e) {

                        }
                        if (orginGetter != null) {

                            originFnm.setGetter(orginGetter);
                            originFnm.setGetterName(getterName);

                            String setterName = fnm.getSetterName();
                            Method orginSetter = null;
                            try {
                                orginSetter = oc.getDeclaredMethod(setterName, fnm.getField().getType());
                            } catch (Exception e) {

                            }
                            if (orginSetter != null) {
                                originFnm.setSetter(orginSetter);
                                originFnm.setSetterName(setterName);
                            }
                            originFnm.setProperty(fnm.getProperty());

                        }
                    }
                }

                try {
                    if (originFnm != null && originFnm.getGetterName() != null) {
                        v = oc.getDeclaredMethod(originFnm.getGetterName()).invoke(origin);

                        Method m = fnm.getSetter();
                        m.invoke(t, v);
                    }
                } catch (Exception e) {

                }

            }

        } catch (Exception e) {
            System.out.println("p = " + p + ", v = " + v);
            e.printStackTrace();
        }

        return t;
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

    /**
     * 通过setter拷贝原始对象的从getter获取的值
     *
     * @param target
     * @param origin
     */
    public static void copyX(Object target, Object origin) {

        if (origin == null || target == null)
            return;

        Class clz = target.getClass();

        Class oc = origin.getClass();

        ReflectionCache cache = Parser.getReflectionCache(clz); // target

        Set<String> set = new HashSet<String>();
        for (Method m : oc.getDeclaredMethods()) {
            set.add(m.getName());
        }

        for (FieldAndMethod fam : cache.getMap().values()) {

            if (!set.contains(fam.getGetterName())) {
                continue;
            }

            Object v = null;
            try {
                Method om = oc.getDeclaredMethod(fam.getGetterName());
                v = om.invoke(origin);
                Class rt = om.getReturnType();
                if (rt == int.class || rt == long.class || rt == double.class || rt == float.class
                        || rt == boolean.class) {
                    if (v.toString().equals("0")) {
                        v = null;
                    }
                }

            } catch (Exception e) {

            }
            if (v != null) {
                try {
                    fam.getSetter().invoke(target, v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String getMapper(String property) {

        String AZ = "AZ";
        int min = AZ.charAt(0) - 1;
        int max = AZ.charAt(1) + 1;

        try {
            String spec = Parser.mappingSpec;
            if (SqlStringUtil.isNotNull(spec)) {
                char[] arr = property.toCharArray();
                int length = arr.length;
                List<String> list = new ArrayList<String>();
                StringBuilder temp = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    char c = arr[i];
                    if (c > min && c < max) {
                        String ts = temp.toString();
                        if (SqlStringUtil.isNotNull(ts)) {
                            list.add(temp.toString());
                        }
                        temp = new StringBuilder();
                        String s = String.valueOf(c);
                        temp.append(s.toLowerCase());
                    } else {
                        temp = temp.append(c);
                    }

                    if (i == length - 1) {
                        list.add(temp.toString());
                    }

                }

                String str = "";

                int size = list.size();
                for (int i = 0; i < size; i++) {
                    String s = list.get(i);
                    str += s;
                    if (i < size - 1) {
                        str += "_";
                    }
                }
                return str;
            }

        } catch (Exception e) {

        }
        return property;
    }

    public static boolean isEnum(Class clz) {
        Class superClzz = clz.getSuperclass();
        return clz.isEnum() || (superClzz != null && superClzz.isEnum());
    }

    public static boolean testEnumConstant(Class clz, Object value) {
        if (value instanceof String){
            if (isEnum(clz)) {
                Enum.valueOf(clz, (String)value);
                return true;
            }
        }
        return false;
    }

    public static boolean isEnumConstant(Class<? extends Enum> clz, String value) {
        Enum.valueOf(clz, (String)value);
        return true;
    }

}

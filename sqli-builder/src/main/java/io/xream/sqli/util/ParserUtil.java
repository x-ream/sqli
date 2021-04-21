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

import io.xream.sqli.annotation.X;
import io.xream.sqli.builder.SqlScript;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


/**
 * @Author Sim
 */
public final class ParserUtil {

    public final static String SQL_KEYWORD_MARK = "`";

    private ParserUtil() {
        super();
    }

    private static void parseFieldsOfElementList(Class clz, Map<String, Field> filterMap, Map<String, Field> allMap) {

        List<Field> fl = new ArrayList<>();

        if (clz.getSuperclass() != Object.class) {
            fl.addAll(Arrays.asList(clz.getSuperclass().getDeclaredFields()));
        }
        fl.addAll(Arrays.asList(clz.getDeclaredFields()));

        /*
         * 排除transient
         */
        for (Field f : fl) {
            allMap.put(f.getName(), f);

            if (f.getModifiers() >= 128) {
                filterMap.put(f.getName(), f);
            }

            /*
             * ignored anno
             */
            X.Ignore p = f.getAnnotation(X.Ignore.class);
            if (p != null) {
                filterMap.put(f.getName(), f);
            }
        }
    }

    private static void parseMethodsOfElementList(Class clz, Set<String> mns, List<Method> methodList) {
        if (clz.getSuperclass() != Object.class) {
            methodList.addAll(Arrays.asList(clz.getSuperclass().getDeclaredMethods()));
        }
        methodList.addAll(Arrays.asList(clz.getDeclaredMethods())); // 仅仅XxxMapped子类

        for (Method m : methodList) {
            mns.add(m.getName());
        }
    }

    private static void parseFilterListOfElementList(List<BeanElement> filterList, Set<String> mns, List<Method> ml) {

        for (Method m : ml) {
            String name = m.getName();
            if (!(name.startsWith("set") || name.startsWith("get") || name.startsWith("is")))
                continue;

            String key = BeanUtil.getProperty(name);
            BeanElement be = null;
            for (BeanElement b : filterList) {
                if (b.getProperty().equals(key)) {
                    be = b;
                    break;
                }
            }
            if (be == null) {
                be = new BeanElement();
                be.setProperty(key);
                filterList.add(be);
            }
            if (name.startsWith("set")) {
                be.setSetter(name);
            } else if (name.startsWith("get")) {
                be.setGetter(name);
                be.setClz(m.getReturnType());
            } else if (name.startsWith("is")) {
                be.setGetter(name);
                be.setClz(m.getReturnType());
                be.setProperty(name);
                String setter = BeanUtil.getSetter(name); // FIXME 可能有BUG
                if (mns.contains(setter)) {
                    be.setSetter(setter);
                }
            }

        }
    }

    private static void filterElementList(List<BeanElement> filterList, Map<String, Field> filterMap) {
        /*
         * 找出有setter 和 getter的一对
         */
        Iterator<BeanElement> ite = filterList.iterator();
        while (ite.hasNext()) {// BUG, 这里去掉了boolen属性
            BeanElement be = ite.next();
            if (!be.isPair()) {
                ite.remove();
            }
        }

        /*
         * 去掉transient
         */
        for (String key : filterMap.keySet()) {
            Iterator<BeanElement> beIte = filterList.iterator();
            while (beIte.hasNext()) {
                BeanElement be = beIte.next();
                if (be.getProperty().equals(key)) {
                    beIte.remove();
                    break;
                }
            }
        }
    }

    private static List<BeanElement> buildElementList(Class clz, List<BeanElement> filterList, Map<String, Field> allMap) {
        List<BeanElement> list = new ArrayList<BeanElement>();

        for (BeanElement element : filterList) {

            parseAnno(clz, element, allMap.get(element.getProperty()));

            Class ec = element.getClz();
            if (element.getSqlType() == null) {
                if (ec == int.class || ec == Integer.class) {
                    element.setSqlType(SqlFieldType.INT);
                    element.setLength(11);
                } else if (ec == long.class || ec == Long.class) {
                    element.setSqlType(SqlFieldType.LONG);
                    element.setLength(13);
                } else if (ec == double.class || ec == Double.class) {
                    element.setSqlType(SqlFieldType.DOUBLE);
                    element.setLength(13);
                } else if (ec == float.class || ec == Float.class) {
                    element.setSqlType(SqlFieldType.FLOAT);
                    element.setLength(13);
                } else if (ec == boolean.class || ec == Boolean.class) {
                    element.setSqlType(SqlFieldType.BYTE);
                    element.setLength(1);
                } else if (ec == Date.class || ec == java.sql.Date.class || ec == Timestamp.class ) {
                    element.setSqlType(SqlFieldType.DATE);
                } else if (ec == String.class) {
                    element.setSqlType(SqlFieldType.VARCHAR);
                    if (element.getLength() == 0)
                        element.setLength(60);
                } else if (ec == BigDecimal.class) {
                    element.setSqlType(SqlFieldType.DECIMAL);
                } else if (EnumUtil.isEnum(ec)) {
                    element.setSqlType(SqlFieldType.VARCHAR);
                    if (element.getLength() == 0)
                        element.setLength(20);
                } else {
                    element.setJson(true);
                    if (ec == List.class) {
                        Field field = null;
                        try {
                            field = clz.getDeclaredField(element.getProperty());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ParameterizedType pt = (ParameterizedType) field.getGenericType();

                        Class geneType = (Class) pt.getActualTypeArguments()[0];
                        element.setGeneType(geneType);
                    }
                    element.setSqlType(SqlFieldType.VARCHAR);
                    if (element.getLength() == 0)
                        element.setLength(512);
                }
            } else if (element.getSqlType().contains(SqlFieldType.TEXT)) {
                element.setLength(0);
            } else {
                element.setSqlType(SqlFieldType.VARCHAR);
            }

            list.add(element);
        }
        return list;
    }

    private static void initMethodCache(Class clz, List<BeanElement> list) {
        try {
            for (BeanElement be : list) {
                try {
                    be.setSetMethod(clz.getDeclaredMethod(be.getSetter(), be.getClz()));
                } catch (NoSuchMethodException e) {
                    be.setSetMethod(clz.getSuperclass().getDeclaredMethod(be.getSetter(), be.getClz()));
                }
                try {
                    be.setGetMethod(clz.getDeclaredMethod(be.getGetter()));
                } catch (NoSuchMethodException e) {
                    be.setGetMethod(clz.getSuperclass().getDeclaredMethod(be.getGetter()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    public static List<BeanElement> parseElementList(Class clz) {

        Map<String, Field> filterMap = new HashMap<>();
        Map<String, Field> allMap = new HashMap<>();
        parseFieldsOfElementList(clz, filterMap, allMap); //Step 1

        Set<String> mns = new HashSet<>();
        List<Method> ml = new ArrayList<>();
        parseMethodsOfElementList(clz, mns, ml);


        List<BeanElement> filterList = new ArrayList<>();
        parseFilterListOfElementList(filterList, mns, ml);
        filterElementList(filterList, filterMap);

        List<BeanElement> list = buildElementList(clz, filterList, allMap);

        initMethodCache(clz, list);

        return list;
    }

    public static void parseCacheableAnno(Class clz, Parsed parsed) {
        X.NoCache p = (X.NoCache) clz.getAnnotation(X.NoCache.class);
        if (p != null) {
            parsed.setNoCache(true);
        }
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void parseAnno(Class clz, BeanElement ele, Field f) {

        Method m = null;
        try {
            m = clz.getDeclaredMethod(ele.getGetter());
        } catch (NoSuchMethodException e) {

        }
        if (m != null) {
            X p = m.getAnnotation(X.class);
            if (p != null) {
                ele.setLength(p.length());
            }
        }

        if (f != null) {
            X p = f.getAnnotation(X.class);
            if (p != null) {
                ele.setLength(p.length());
            }

            X.Mapping mapping = f.getAnnotation(X.Mapping.class);
            if (mapping != null && SqliStringUtil.isNotNull(mapping.value())) {
                ele.setMapper(mapping.value());
            }

        }

    }

    @SuppressWarnings({"rawtypes"})
    public static void parseKey(Parsed parsed, Class clz) {

        List<Field> list = new ArrayList<>();

        try {

            list.addAll(Arrays.asList(clz.getDeclaredFields()));
            Class sc = clz.getSuperclass();
            if (sc != Object.class) {
                list.addAll(Arrays.asList(sc.getDeclaredFields()));
            }
        } catch (Exception e) {

        }

        for (Field f : list) {
            X.Key a = f.getAnnotation(X.Key.class);
            if (a != null) {
                f.setAccessible(true);
                parsed.setKeyField(f);
            }

        }
    }

    public static void parseTagAndSub(Parsed parsed, Class clz) {

        List<Field> list = new ArrayList<>();

        try {
            list.addAll(Arrays.asList(clz.getDeclaredFields()));
            Class sc = clz.getSuperclass();
            if (sc != Object.class) {
                list.addAll(Arrays.asList(sc.getDeclaredFields()));
            }
        } catch (Exception e) {

        }

        for (Field f : list) {
            X.Tag t = f.getAnnotation(X.Tag.class);
            if (t != null) {
                f.setAccessible(true);
                parsed.getTagFieldList().add(f);
            }
            X.TagTarget tt = f.getAnnotation(X.TagTarget.class);
            if (tt != null) {
                f.setAccessible(true);
                if (parsed.getTagKeyField() != null)
                    throw new ParsingException("find another annotation: X.TagTarget, class: " + clz);
                parsed.setTagKeyField(f);
            }
        }
    }

    public static String filterSQLKeyword(String mapper) {
        for (String keyWord : SqlScript.KEYWORDS) {
            if (keyWord.equalsIgnoreCase(mapper)) {
                return SQL_KEYWORD_MARK + mapper + SQL_KEYWORD_MARK;
            }
        }
        return mapper;
    }

    public static String getClzName(String alia, Map<String, String> aliaMap) {
        String a = aliaMap.get(alia);
        if (SqliStringUtil.isNotNull(a))
            return a;
        return alia;
    }

    public static <T> Object tryToGetId(T t, Parsed parsed) {

        Field f = parsed.getKeyField();
        Object id = null;
        try {
            id = f.get(t);
        } catch (Exception e) {

        }
        if (id == null)
            throw new IllegalArgumentException("obj keyOne = " + id + ", " + t);
        return id;
    }

    public static String getCacheKey(Object obj, Parsed parsed) {
        try {
            Object keyOneObj = tryToGetId(obj, parsed);
            if (keyOneObj != null)
                return keyOneObj.toString();

        } catch (Exception e) {
        }
        return null;
    }


    public static String getMapper(String property) {

        String AZ = "AZ";
        int min = AZ.charAt(0) - 1;
        int max = AZ.charAt(1) + 1;

        try {
            String spec = Parser.mappingSpec;
            if (SqliStringUtil.isNotNull(spec)) {
                char[] arr = property.toCharArray();
                int length = arr.length;
                List<String> list = new ArrayList<String>();
                StringBuilder temp = new StringBuilder();
                for (int i = 0; i < length; i++) {
                    char c = arr[i];
                    if (c > min && c < max) {
                        String ts = temp.toString();
                        if (SqliStringUtil.isNotNull(ts)) {
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

    public interface SqlFieldType {

        String TEXT = "text";
        String VARCHAR = "varchar";
        String DATE = "timestamp";
        String INT = "int";
        String LONG = "bigint";
        String BYTE = "tinyint";
        String DOUBLE = "float";//float
        String FLOAT = "float";//real
        String DECIMAL = "decimal";
    }
}

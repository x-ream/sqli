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

import io.xream.sqli.annotation.X;
import io.xream.sqli.builder.CriteriaCondition;
import io.xream.sqli.builder.SqlScript;
import io.xream.sqli.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


/**
 * @Author Sim
 */
public class BeanUtilX extends BeanUtil {

    private static Logger logger = LoggerFactory.getLogger(BeanUtilX.class);

    public final static String SQL_KEYWORD_MARK = "`";

    private BeanUtilX() {
        super();
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
            logger.info("Copy() exception: p = " + p + ", v = " + v);
        }

        return t;
    }


    @SuppressWarnings("rawtypes")
    public static List<BeanElement> parseElementList(Class clz) {

        List<Field> fl = new ArrayList<Field>();

        if (clz.getSuperclass() != Object.class) {
            fl.addAll(Arrays.asList(clz.getSuperclass().getDeclaredFields()));
        }
        fl.addAll(Arrays.asList(clz.getDeclaredFields()));

        /*
         * 排除transient
         */
        Map<String, Field> filterMap = new HashMap<String, Field>();
        Map<String, Field> allMap = new HashMap<String, Field>();
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

        Set<String> mns = new HashSet<String>();
        List<Method> ml = new ArrayList<Method>();
        if (clz.getSuperclass() != Object.class) {
            ml.addAll(Arrays.asList(clz.getSuperclass().getDeclaredMethods()));
        }
        ml.addAll(Arrays.asList(clz.getDeclaredMethods())); // 仅仅XxxMapped子类

        for (Method m : ml) {
            mns.add(m.getName());
        }

        List<BeanElement> filterList = new ArrayList<BeanElement>();
        for (Method m : ml) {
            String name = m.getName();
            if (!(name.startsWith("set") || name.startsWith("get") || name.startsWith("is")))
                continue;

            String key = getProperty(name);
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
                be.setter = name;
            } else if (name.startsWith("get")) {
                be.getter = name;
                be.clz = m.getReturnType();
            } else if (name.startsWith("is")) {
                be.getter = name;
                be.clz = m.getReturnType();
                be.setProperty(name);
                String setter = getSetter(name); // FIXME 可能有BUG
                if (mns.contains(setter)) {
                    be.setter = setter;
                }
            }

        }

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

        List<BeanElement> list = new ArrayList<BeanElement>();

        for (BeanElement element : filterList) {

            parseAnno(clz, element, allMap.get(element.getProperty()));

            Class ec = element.clz;
            if (element.sqlType == null) {
                if (ec == int.class || ec == Integer.class) {
                    element.sqlType = SqlFieldType.INT;
                    element.length = 11;
                } else if (ec == long.class || ec == Long.class) {
                    element.sqlType = SqlFieldType.LONG;
                    element.length = 13;
                } else if (ec == double.class || ec == Double.class) {
                    element.sqlType = SqlFieldType.DOUBLE;
                    element.length = 13;
                } else if (ec == float.class || ec == Float.class) {
                    element.sqlType = SqlFieldType.FLOAT;
                    element.length = 13;
                } else if (ec == boolean.class || ec == Boolean.class) {
                    element.sqlType = SqlFieldType.BYTE;
                    element.length = 1;
                } else if (ec == Date.class || ec == java.sql.Date.class || ec == Timestamp.class) {
                    element.sqlType = SqlFieldType.DATE;
                } else if (ec == String.class) {
                    element.sqlType = SqlFieldType.VARCHAR;
                    if (element.length == 0)
                        element.length = 60;
                } else if (ec == BigDecimal.class) {
                    element.sqlType = SqlFieldType.DECIMAL;
                } else if (BeanUtil.isEnum(ec)) {
                    element.sqlType = SqlFieldType.VARCHAR;
                    if (element.length == 0)
                        element.length = 40;
                } else {
                    element.isJson = true;
                    if (ec == List.class) {
                        Field field = null;
                        try {
                            field = clz.getDeclaredField(element.getProperty());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ParameterizedType pt = (ParameterizedType) field.getGenericType();

                        Class geneType = (Class) pt.getActualTypeArguments()[0];
                        element.geneType = geneType;
                    }
                    element.sqlType = SqlFieldType.VARCHAR;
                    if (element.length == 0)
                        element.length = 512;
                }
            } else if (element.sqlType.contains(SqlFieldType.TEXT)) {
                element.length = 0;
            } else {
                element.sqlType = SqlFieldType.VARCHAR;
            }

            list.add(element);
        }

        try {
            for (BeanElement be : list) {
                try {
                    be.setMethod = clz.getDeclaredMethod(be.setter, be.clz);
                } catch (NoSuchMethodException e) {
                    be.setMethod = clz.getSuperclass().getDeclaredMethod(be.setter, be.clz);
                }
                try {
                    be.getMethod = clz.getDeclaredMethod(be.getter);
                } catch (NoSuchMethodException e) {
                    be.getMethod = clz.getSuperclass().getDeclaredMethod(be.getter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            m = clz.getDeclaredMethod(ele.getter);
        } catch (NoSuchMethodException e) {

        }
        if (m != null) {
            X p = m.getAnnotation(X.class);
            if (p != null) {
                ele.length = p.length();
            }
        }

        if (f != null) {
            X p = f.getAnnotation(X.class);
            if (p != null) {
                ele.length = p.length();
            }

            X.Mapping mapping = f.getAnnotation(X.Mapping.class);
            if (mapping != null && SqliStringUtil.isNotNull(mapping.value())) {
                ele.mapper = mapping.value();
            }

        }

    }

    @SuppressWarnings({"rawtypes"})
    public static void parseKey(Parsed parsed, Class clz) {

        Map<Integer, String> map = parsed.getKeyMap();
        Map<Integer, Field> keyFieldMap = parsed.getKeyFieldMap();
        List<Field> list = new ArrayList<Field>();

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
                map.put(X.KEY_ONE, f.getName());
                f.setAccessible(true);
                keyFieldMap.put(X.KEY_ONE, f);
            }

        }
    }

    public static String filterSQLKeyword(String mapper) {
        for (String keyWord : SqlScript.KEYWORDS) {
            if (keyWord.equals(mapper.toLowerCase())) {
                return SQL_KEYWORD_MARK + mapper + SQL_KEYWORD_MARK;
            }
        }
        return mapper;
    }


    private static Set<String> opSet = new HashSet() {
        {
            add("=");
            add("!");
            add(">");
            add("<");
            add("+");
            add("-");
            add("*");
            add("/");
            add("(");
            add(")");
            add(";");
        }
    };

    public static String normalizeSql(final String manuSql) {
        StringBuilder valueSb = new StringBuilder();

        boolean ignore = false;
        int length = manuSql.length();
        for (int j = 0; j < length; j++) {
            String strEle = String.valueOf(manuSql.charAt(j));
            if (SqlScript.SPACE.equals(strEle)) {
                ignore = true;
                continue;
            }
            if (opSet.contains(strEle)) {

                valueSb.append(SqlScript.SPACE);

                valueSb.append(strEle);
                if (j + 1 < length) {
                    String nextOp = String.valueOf(manuSql.charAt(j + 1));
                    if (opSet.contains(nextOp)) {
                        valueSb.append(nextOp);
                        j++;
                    }
                }
                valueSb.append(SqlScript.SPACE);
            } else {
                if (ignore)
                    valueSb.append(SqlScript.SPACE);
                valueSb.append(strEle);
            }
            ignore = false;
        }

        return valueSb.toString();

    }


    public static String getClzName(String alia, CriteriaCondition criteria) {
        if (criteria.getAliaMap() != null) {
            String a = criteria.getAliaMap().get(alia);
            if (SqliStringUtil.isNotNull(a))
                return a;
        }
        return alia;
    }

    public static <T> Object tryToGetId(T t, Parsed parsed) {

        Field f = parsed.getKeyField(X.KEY_ONE);
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
        String DOUBLE_COMMON = "double";//float
        String DECIMAL = "decimal";
    }
}

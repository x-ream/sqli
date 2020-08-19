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
package io.xream.sqli.core.builder;

import io.xream.sqli.common.util.BeanUtil;
import io.xream.sqli.common.util.SqlStringUtil;
import io.xream.sqli.core.filter.BaseTypeFilter;
import io.xream.sqli.core.support.TimestampSupport;

import java.util.Iterator;
import java.util.List;

public interface ConditionCriteriaToSql extends KeyMapper{

    default void buildConditionSql(StringBuilder sb, List<X> listX) {
        if (listX == null || listX.isEmpty())
            return;
        for (X x : listX) {

            if (x.getPredicate() == PredicateAndOtherScript.SUB) {

                if (x.getSubList().isEmpty())
                    continue;
                x.getSubList().get(0).setConjunction(ConjunctionAndOtherScript.NONE);
                sb.append(x.getConjunction().sql());
                sb.append(SqlScript.SPACE).append(SqlScript.LEFT_PARENTTHESIS).append(SqlScript.SPACE);
                buildConditionSql(sb, x.getSubList());
                sb.append(SqlScript.SPACE).append(SqlScript.RIGHT_PARENTTHESIS);
                continue;
            }

            sb.append(x.getConjunction().sql()).append(x.getKey()).append(x.getPredicate().sql());
            if (x.getValue() != null) {
                if (x.getPredicate() == PredicateAndOtherScript.IN || x.getPredicate() == PredicateAndOtherScript.NOT_IN) {
                    List<Object> inList = (List<Object>) x.getValue();
                    Object v = inList.get(0);
                    Class<?> vType = v.getClass();
                    buildIn(sb, vType, inList);
                } else if (x.getPredicate() == PredicateAndOtherScript.IS_NULL
                        || x.getPredicate() == PredicateAndOtherScript.IS_NOT_NULL
                        || x.getPredicate() == PredicateAndOtherScript.X) {
                } else {
                    sb.append(SqlScript.PLACE_HOLDER).append(SqlScript.SPACE);
                }
            }
        }

    }


    static void buildIn(StringBuilder sb, Class clz, List<? extends Object> inList) {

        sb.append(SqlScript.LEFT_PARENTTHESIS).append(SqlScript.SPACE);//"( "

        int length = inList.size();
        if (clz == String.class) {

            for (int j = 0; j < length; j++) {
                Object value = inList.get(j);
                if (value == null || SqlStringUtil.isNullOrEmpty(value.toString()))
                    continue;
                value = filter(value.toString());
                sb.append(SqlScript.SINGLE_QUOTES).append(value).append(SqlScript.SINGLE_QUOTES);//'string'
                if (j < length - 1) {
                    sb.append(SqlScript.COMMA);
                }
            }

        } else if (BeanUtil.isEnum(clz)) {
            for (int j = 0; j < length; j++) {
                Object value = inList.get(j);
                if (value == null)
                    continue;
                String ev = null;
                if (value instanceof String){
                    ev = (String) value;
                }else {
                    ev = ((Enum) value).name();
                }
                sb.append(SqlScript.SINGLE_QUOTES).append(ev).append(SqlScript.SINGLE_QUOTES);//'string'
                if (j < length - 1) {
                    sb.append(SqlScript.COMMA);
                }
            }
        } else {
            for (int j = 0; j < length; j++) {
                Object value = inList.get(j);
                if (value == null)
                    continue;
                sb.append(value);
                if (j < length - 1) {
                    sb.append(SqlScript.COMMA);
                }
            }
        }

        sb.append(SqlScript.SPACE).append(SqlScript.RIGHT_PARENTTHESIS);
    }

    static String filter(String sql) {
        sql = sql.replace("drop", SqlScript.SPACE)
                .replace(";", SqlScript.SPACE);// 手动拼接SQL,
        return sql;
    }

    interface Filter {

        default void filter(List<X> xList, CriteriaCondition criteria) {

            if (xList == null || xList.isEmpty())
                return;

            Iterator<X> ite = xList.iterator();
            while (ite.hasNext()) {
                X x = ite.next();
                PredicateAndOtherScript p = x.getPredicate();
                String key = x.getKey();
                if (p == PredicateAndOtherScript.SUB){
                    filter(x.getSubList(),criteria);
                    if (x.getSubList().isEmpty()) {
                        ite.remove();
                    }
                }else if (p == PredicateAndOtherScript.EQ
                        || p == PredicateAndOtherScript.NE
                        || p == PredicateAndOtherScript.GT
                        || p == PredicateAndOtherScript.GTE
                        || p == PredicateAndOtherScript.LT
                        || p == PredicateAndOtherScript.LTE) {

                    if (key.contains(".")){
                        String[] arr = key.split("\\.");
                        String alia = arr[0];
                        String clzName = criteria.getAliaMap().get(alia);
                        if (clzName == null)
                            clzName = alia;
                        Parsed parsed = Parser.get(clzName);
                        if (BaseTypeFilter.isBaseType_0(arr[1],x.getValue(),parsed)){
                            ite.remove();
                        }else{
                            BeanElement be = parsed.getElement(arr[1]);
                            if (be == null){
                                throw new RuntimeException("property of " + parsed.getClz() +" not exists: " + arr[1]);
                            }
                            TimestampSupport.testNumberValueToDate(be.clz,x);
                            if (x.getValue() == null)
                                ite.remove();
                        }
                    }else{
                        Parsed parsed = criteria.getParsed();
                        if (BaseTypeFilter.isBaseType_0(key,x.getValue(),parsed)){
                            ite.remove();
                        }else{
                            BeanElement be = parsed.getElement(key);
                            if (be == null){
                                throw new RuntimeException("property of " + parsed.getClz() +" not exists: " + key);
                            }
                            TimestampSupport.testNumberValueToDate(be.clz,x);
                            if (x.getValue() == null)
                                ite.remove();
                        }
                    }
                }else if (p == PredicateAndOtherScript.IN
                        || p == PredicateAndOtherScript.NOT_IN) {

                    List valueList = (List)x.getValue();
                    if (valueList.size() > 1)
                        continue;

                    if (key.contains(".")){
                        if (BaseTypeFilter.isBaseType_0(key,valueList.get(0),criteria)){
                            ite.remove();
                        }
                    }else{
                        Parsed parsed = criteria.getParsed();
                        if (BaseTypeFilter.isBaseType_0(key,valueList.get(0),parsed)){
                            ite.remove();
                        }
                    }
                }
                List<X> subList = x.getSubList();
                if (subList == null || subList.isEmpty())
                    continue;
                filter(subList,criteria);
            }
        }

    }

    interface Pre {
        default void pre(List<Object> valueList, List<X> listX) {
            for (X x : listX) {
                if (x.getPredicate() == PredicateAndOtherScript.SUB){
                    pre(valueList,x.getSubList());
                    continue;
                } else if (x.getPredicate() == PredicateAndOtherScript.IN
                        || x.getPredicate() == PredicateAndOtherScript.NOT_IN
                        || x.getPredicate() == PredicateAndOtherScript.IS_NULL
                        || x.getPredicate() == PredicateAndOtherScript.IS_NOT_NULL) {
                    //....
                }else if (x.getPredicate() == PredicateAndOtherScript.X) {
                    Object value = x.getValue();
                    if (value == null)
                        continue;
                    if (value instanceof Object[]) {
                        for (Object v : (Object[])value){
                            add(valueList,v);
                        }
                    }
                } else if (x.getPredicate() == PredicateAndOtherScript.BETWEEN) {
                    MinMax minMax = (MinMax) x.getValue();
                    valueList.add(minMax.getMin());
                    valueList.add(minMax.getMax());
                }else {
                    Object v = x.getValue();
                    add(valueList, v);
                }
                // NO JSON OBJECT CONDITION
            }
        }

        static void add(List<Object> valueList, Object value){
            if (BeanUtil.isEnum(value.getClass())) {
                try {
                    valueList.add(((Enum) value).name());
                } catch (Exception e) {
                }
            } else {
                valueList.add(value);
            }
        }
    }

}

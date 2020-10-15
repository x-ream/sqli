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
package io.xream.sqli.builder;

import io.xream.sqli.filter.BaseTypeFilter;
import io.xream.sqli.filter.UnsafeSyntaxFilter;
import io.xream.sqli.mapping.Mappable;
import io.xream.sqli.mapping.Mapper;
import io.xream.sqli.mapping.Script;
import io.xream.sqli.mapping.SqlNormalizer;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.support.TimestampSupport;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.util.Iterator;
import java.util.List;

/**
 * @Author Sim
 */
public interface ConditionToSql extends Mapper, SqlNormalizer, UnsafeSyntaxFilter {

    default void buildConditionSql(StringBuilder sb, List<Bb> bbList, Mappable mappable) {
        if (bbList == null || bbList.isEmpty())
            return;
        for (Bb bb : bbList) {

            if (bb.getP() == Op.SUB) {

                if (bb.getSubList().isEmpty())
                    continue;
                bb.getSubList().get(0).setC(Op.NONE);
                sb.append(bb.getC().sql());
                sb.append(SqlScript.SPACE).append(SqlScript.LEFT_PARENTTHESIS).append(SqlScript.SPACE);
                buildConditionSql(sb, bb.getSubList(), mappable);
                sb.append(SqlScript.SPACE).append(SqlScript.RIGHT_PARENTTHESIS);
                continue;
            }

            String mapper = null;
            if (bb.getP() == Op.X){
                final String str  = normalizeSql(bb.getKey());
                StringBuilder sbx = new StringBuilder();
                mapping((reg)->str.split(reg), mappable,sbx);
                mapper = sbx.toString();
                sb.append(bb.getC().sql()).append(mapper);
            }else {
                mapper = mapping(bb.getKey(), mappable);
                sb.append(bb.getC().sql()).append(mapper).append(Script.SPACE).append(bb.getP().sql()).append(Script.SPACE);
            }

            if (bb.getValue() != null) {
                if (bb.getP() == Op.IN || bb.getP() == Op.NOT_IN) {
                    List<Object> inList = (List<Object>) bb.getValue();
                    Object v = inList.get(0);
                    Class<?> vType = v.getClass();
                    buildIn(sb, vType, inList);
                } else if (!(bb.getP() == Op.IS_NULL
                        || bb.getP() == Op.IS_NOT_NULL
                        || bb.getP() == Op.X)) {
                    sb.append(SqlScript.PLACE_HOLDER).append(SqlScript.SPACE);
                }
            }
        }

    }


    default void buildIn(StringBuilder sb, Class clz, List<? extends Object> inList) {

        sb.append(SqlScript.LEFT_PARENTTHESIS).append(SqlScript.SPACE);//"( "

        int length = inList.size();
        if (clz == String.class) {

            for (int j = 0; j < length; j++) {
                Object value = inList.get(j);
                if (value == null || SqliStringUtil.isNullOrEmpty(value.toString()))
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


    interface Filter {

        default void filter(List<Bb> bbList, Mappable mappable) {

            if (bbList == null || bbList.isEmpty() )
                return;

            Iterator<Bb> ite = bbList.iterator();
            while (ite.hasNext()) {
                Bb bb = ite.next();
                Op p = bb.getP();
                String key = bb.getKey();
                if (p == Op.SUB){
                    filter(bb.getSubList(), mappable);
                    if (bb.getSubList().isEmpty()) {
                        ite.remove();
                    }
                }else if (p == Op.EQ
                        || p == Op.NE
                        || p == Op.GT
                        || p == Op.GTE
                        || p == Op.LT
                        || p == Op.LTE) {

                    if (key.contains(".")){
                        String[] arr = key.split("\\.");
                        String alia = arr[0];
                        String clzName = mappable.getAliaMap().get(alia);
                        if (clzName == null)
                            clzName = alia;
                        Parsed parsed = Parser.get(clzName);
                        if (parsed != null) {
                            if (BaseTypeFilter.isBaseType(arr[1], bb.getValue(), parsed)) {
                                ite.remove();
                            } else {
                                BeanElement be = parsed.getElement(arr[1]);
                                if (be != null) {
                                    TimestampSupport.testNumberValueToDate(be.getClz(), bb);
                                    if (bb.getValue() == null)
                                        ite.remove();
                                }
                            }
                        }
                    }else{
                        Parsed parsed = mappable.getParsed();
                        if (parsed == null) {
                            String ss = ((Criteria.ResultMapCriteria)mappable).sourceScript();
                            if (ss != null) {
                                parsed = Parser.get(ss);
                            }
                        }
                        if (parsed != null) {
                            if (BaseTypeFilter.isBaseType(key, bb.getValue(), parsed)) {
                                ite.remove();
                            } else {
                                BeanElement be = parsed.getElement(key);
                                if (be != null) {
                                    TimestampSupport.testNumberValueToDate(be.getClz(), bb);
                                    if (bb.getValue() == null)
                                        ite.remove();
                                }
                            }
                        }
                    }
                }else if (p == Op.IN
                        || p == Op.NOT_IN) {

                    List valueList = (List) bb.getValue();
                    if (valueList.size() > 1)
                        continue;

                    if (key.contains(".")){
                        if (BaseTypeFilter.isBaseType(key,valueList.get(0),mappable)){
                            ite.remove();
                        }
                    }else{
                        Parsed parsed = mappable.getParsed();
                        if (parsed != null && BaseTypeFilter.isBaseType(key, valueList.get(0), parsed)) {
                                ite.remove();
                        }
                    }
                }
                List<Bb> subList = bb.getSubList();
                if (subList == null || subList.isEmpty())
                    continue;
                filter(subList, mappable);
            }
        }

    }

    interface Pre {
        default void pre(List<Object> valueList, List<Bb> bbList) {
            for (Bb bb : bbList) {
                if (bb.getP() == Op.SUB){
                    pre(valueList, bb.getSubList());
                    continue;
                }else if (bb.getP() == Op.X) {
                    Object value = bb.getValue();
                    if (value == null)
                        continue;
                    if (value instanceof Object[]) {
                        for (Object v : (Object[])value){
                            add(valueList,v);
                        }
                    }
                }else if (!(bb.getP() == Op.IN
                        || bb.getP() == Op.NOT_IN
                        || bb.getP() == Op.IS_NULL
                        || bb.getP() == Op.IS_NOT_NULL)) {
                    Object v = bb.getValue();
                    add(valueList, v);
                }
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

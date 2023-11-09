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

import io.xream.sqli.builder.internal.Bb;
import io.xream.sqli.builder.internal.CondQ;
import io.xream.sqli.builder.internal.SqlScript;
import io.xream.sqli.mapping.SqlNormalizer;
import io.xream.sqli.util.EnumUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Sim
 */
public class CondBuilder implements SqlNormalizer {

    private CondBuilder instance;

    private transient List<Bb> bbList;
    protected transient boolean isAbort;
    private transient boolean isOr;

    private transient List<Bb> tempList;
    private transient List<List<Bb>> subsList;

    protected CondBuilder(){
        this.instance = this;
    }

    protected CondBuilder(List<Bb> bbList){
        this.instance = this;
        this.instance.bbList = bbList;
    }

    protected void init(List<Bb> bbList){
        this.bbList = bbList;
    }

    private List<List<Bb>> getSubsList(){
        if (this.subsList == null) {
            this.subsList = new LinkedList<>();
        }
        return this.subsList;
    }

    public static CondBuilder builder(List<Bb> bbList){
        return new CondBuilder(bbList);
    }

    public static CondBuilder builder() {
        return new CondBuilder(new ArrayList<>());
    }

    public CondQ build() {
        return () -> bbList;
    }

    public CondBuilder bool(Bool condition, Then then) {
        if (condition.isOk()) {
            then.build(this.instance);
        }
        return this.instance;
    }

    public CondBuilder eq(String property, Object value){
        return doGle(Op.EQ, property, value);
    }

    public CondBuilder lt(String property, Object value){
        return doGle(Op.LT, property, value);
    }

    public CondBuilder lte(String property, Object value){
        return doGle(Op.LTE, property, value);
    }

    public CondBuilder gt(String property, Object value){
        return doGle(Op.GT, property, value);
    }

    public CondBuilder gte(String property, Object value){
        return doGle(Op.GTE, property, value);
    }

    public CondBuilder ne(String property, Object value){
        return doGle(Op.NE, property, value);
    }

    public CondBuilder like(String property, String value){
        if (SqliStringUtil.isNullOrEmpty(value)) {
            isOr();
            return instance;
        }
        String likeValue = SqlScript.LIKE_HOLDER + value + SqlScript.LIKE_HOLDER;
        return doLike(Op.LIKE,property,likeValue);
    }

    public CondBuilder likeRight(String property, String value){
        if (SqliStringUtil.isNullOrEmpty(value)) {
            isOr();
            return instance;
        }
        String likeValue = value + SqlScript.LIKE_HOLDER;
        return doLike(Op.LIKE,property,likeValue);
    }

    public CondBuilder notLike(String property, String value){
        if (SqliStringUtil.isNullOrEmpty(value)) {
            isOr();
            return instance;
        }
        String likeValue = SqlScript.LIKE_HOLDER + value + SqlScript.LIKE_HOLDER;
        return doLike(Op.NOT_LIKE,property,likeValue);
    }

    public CondBuilder in(String property, List list){
        return doIn(Op.IN,property,list);
    }

    public CondBuilder inRequired(String property, List list){
        if (list == null || list.isEmpty()){
            this.instance.isAbort = true;
        }
        return doIn(Op.IN,property,list);
    }

    public CondBuilder nin(String property, List list){
        return doIn(Op.NOT_IN,property,list);
    }

    public CondBuilder nonNull(String property){
        return doNull(Op.IS_NOT_NULL,property);
    }

    public CondBuilder isNull(String property){
        return doNull(Op.IS_NULL,property);
    }

    public CondBuilder x(String sqlSegment, Object... values){

        if (SqliStringUtil.isNullOrEmpty(sqlSegment)){
            isOr();
            return instance;
        }

        String sql = normalizeSql(sqlSegment);

        Object[] arr = null;
        if (values != null){
            int length = values.length;
            arr = new Object[length];
            for (int i=0; i<length; i++) {
                arr[i] = EnumUtil.filterInComplexScriptSimply(values[i]);
            }
        }

        Bb bb = new Bb(isOr());
        bb.setP(Op.X);
        bb.setKey(sql);
        bb.setValue(arr);
        this.add(bb);

        return instance;
    }

    public CondBuilder or(){
        this.isOr = true;
        return this.instance;
    }

    public CondBuilder and(SubCond sub){
        return orAnd(Op.AND, sub);
    }

    public CondBuilder or(SubCond sub) {
        return orAnd(Op.OR, sub);
    }

    private CondBuilder orAnd(Op c, SubCond s){

        CondBuilder sub = CondBuilder.builder();
        s.buildBy(sub);
        CondQ condQ = sub.build();

        List<Bb> subList = condQ.getBbs();
        if (subList == null || subList.isEmpty())
            return instance;

        Bb bb = new Bb(isOr());
        bb.setP(Op.SUB);
        bb.setC(c);

        bb.setSubList(subList);
        this.add(bb);

        return instance;
    }

    private CondBuilder doGle(Op p, String property, Object value) {
        if (value == null){
            isOr();
            return instance;
        }
        if (SqliStringUtil.isNullOrEmpty(value)){
            isOr();
            return instance;
        }
        if (value instanceof List || value.getClass().isArray()) {
            throw new IllegalArgumentException(property + " " +p + " " + value + ", try " + property +" " + Op.IN + " (" + value + ")");
        }
        Bb bb = new Bb(isOr());
        bb.setP(p);
        bb.setKey(property);
        bb.setValue(value);
        this.add(bb);

        return instance;
    }

    private CondBuilder doLike(Op p, String property, String likeWalue){

        Bb bb = new Bb(isOr());
        bb.setP(p);
        bb.setKey(property);
        bb.setValue(likeWalue);
        this.add(bb);
        return instance;
    }

    private CondBuilder doIn(Op p, String property, List<? extends Object> list){

        if (list == null || list.isEmpty()){
            isOr();
            return instance;
        }

        List<Object> tempList = new ArrayList<>();
        for (Object obj : list) {
            if (SqliStringUtil.isNullOrEmpty(obj))
                continue;
            if (!tempList.contains(obj)) {
                tempList.add(obj);
            }
        }

        if (tempList.isEmpty()){
            isOr();
            return instance;
        }

        Bb bb = new Bb(isOr());
        bb.setP(p);
        bb.setKey(property);
        bb.setValue(tempList);
        this.add(bb);

        return instance;
    }

    private CondBuilder doNull(Op p, String property){
        if (SqliStringUtil.isNullOrEmpty(property)){
            isOr();
            return instance;
        }

        Bb bb = new Bb(isOr());
        bb.setP(p);
        bb.setKey(property);
        bb.setValue(p);
        this.add(bb);

        return instance;
    }

    private boolean isOr(){
        if (isOr){
            isOr = false;
            return true;
        }else{
            return false;
        }
    }

    private void add(Bb bb){
        if (this.tempList == null)
            this.bbList.add(bb);
        else
            this.tempList.add(bb);
    }

}

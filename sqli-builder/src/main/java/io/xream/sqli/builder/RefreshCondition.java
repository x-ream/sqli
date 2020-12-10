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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.xream.sqli.api.Routable;
import io.xream.sqli.mapping.Mappable;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.util.*;


/**
 * @Author Sim
 */
public final class RefreshCondition<T> extends ConditionCriteriaBuilder implements Mappable,CriteriaCondition, Routable {

    private List<Bb> refreshList = new ArrayList<>();
    private String sourceScript;//FIXME

    private List<Bb> bbList = new ArrayList<>();
    private Object routeKey;
    private transient int limit;
    @JsonIgnore
    private transient Class clz;
    @JsonIgnore
    private transient List<Object> valueList = new ArrayList<>();
    @JsonIgnore
    private transient Map<String,String> aliaMap = new HashMap<>();


    public Class getClz() {
        return clz;
    }

    public void setClz(Class clz) {
        this.clz = clz;
    }

    public List<Bb> getRefreshList() {
        return refreshList;
    }


    public String getSourceScript() {
        return this.sourceScript;
    }

    public void setSourceScript(String sourceScript) {
        this.sourceScript = sourceScript;
    }

    public List<Object> getValueList() {
        return this.valueList;
    }

    @Override
    public List<Bb> getBbList() {
        return bbList;
    }

    @Override
    public Map<String, String> getAliaMap() {
        return this.aliaMap;
    }
    @Override
    public Map<String,String> getResultKeyAliaMap() {return null;}

    @Override
    public Parsed getParsed() {
        if (this.clz == null)
            return null;
        return Parser.get(this.clz);
    }


    @Override
    public Object getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Object routeKey) {
        this.routeKey = routeKey;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Deprecated
    public RefreshCondition(){
        super();
        init(this.bbList);
    }


    public static RefreshCondition build(){
        return new RefreshCondition();
    }


    public RefreshCondition  and(){
        return this;
    }


    public RefreshCondition or() {
        return (RefreshCondition) super.or();
    }

    /**
     *
     * String sqlX = "propertyA = propertyA + propertyB + 1"
     * @return RefreshCondition
     */
    public RefreshCondition refresh(String sqlX){

        if (Objects.isNull(sqlX))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.X);
        bb.setKey(sqlX);
        this.refreshList.add(bb);

        return this;
    }

    public RefreshCondition refresh(String property, Object value){

        if (Objects.isNull(value))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.EQ);
        bb.setKey(property);
        bb.setValue(value);
        this.refreshList.add(bb);

        return this;
    }

    public KV tryToGetKeyOne() {
        if (clz == null)
            return null;
        Parsed parsed = Parser.get(clz);
        String keyOne = parsed.getKey();
        for (Bb bb : bbList) {
            String key = bb.getKey();
            if (key != null && key.equals(keyOne)) {
                return new KV(key, bb.getValue());
            }
        }
        return null;
    }


    public RefreshCondition routeKey(Object routeKey) {
        this.routeKey = routeKey;
        return this;
    }


    public RefreshCondition eq(String key, Object value) {
        return (RefreshCondition) super.eq(key,value);
    }

    public RefreshCondition gt(String key, Object value) {
        return (RefreshCondition) super.gt(key,value);
    }

    public RefreshCondition gte(String key, Object value) {
        return (RefreshCondition) super.gte(key,value);
    }

    public RefreshCondition lt(String key, Object value) {
        return (RefreshCondition) super.lt(key,value);
    }

    public RefreshCondition lte(String key, Object value) {
        return (RefreshCondition) super.lte(key,value);
    }

    public RefreshCondition ne(String property, Object value) {
        return (RefreshCondition) super.ne(property, value);
    }

    public RefreshCondition like(String property, String value) {
        return (RefreshCondition) super.like(property, value);
    }

    public RefreshCondition likeRight(String property, String value) {
        return (RefreshCondition) super.likeRight(property, value);
    }

    public RefreshCondition notLike(String property, String value) {
        return (RefreshCondition) super.notLike(property, value);
    }

    public RefreshCondition in(String property, List<? extends Object> list) {
        return (RefreshCondition) super.in(property,list);
    }

    public RefreshCondition nin(String property, List<? extends Object> list) {
        return (RefreshCondition) super.nin(property,list);
    }

    public RefreshCondition nonNull(String property){
        return (RefreshCondition) super.nonNull(property);
    }

    public RefreshCondition isNull(String property){
        return (RefreshCondition) super.isNull(property);
    }

    public RefreshCondition  x(String sqlSegment){
        return (RefreshCondition) super.x(sqlSegment);
    }

    public RefreshCondition  x(String sqlSegment, Object...values){
        return (RefreshCondition) super.x(sqlSegment, values);
    }

    public RefreshCondition  beginSub(){
        return (RefreshCondition) super.beginSub();
    }

    public RefreshCondition  endSub(){
        return (RefreshCondition) super.endSub();
    }

    public RefreshCondition sourceScript(String sourceScript) {
        this.sourceScript = sourceScript;
        return this;
    }


}

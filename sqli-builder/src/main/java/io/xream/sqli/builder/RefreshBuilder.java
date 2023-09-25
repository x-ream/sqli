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

import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.util.List;
import java.util.Objects;

/**
 * @author Sim
 */
public class RefreshBuilder<T> extends BbQBuilder {

    private RefreshCond<T> refreshCondition;

    private RefreshBuilder(RefreshCond refreshCondition){
        super();
        init(refreshCondition.getBbList());
    }


    public static RefreshBuilder builder(){
        RefreshCond refreshCondition = new RefreshCond();
        RefreshBuilder builder = new RefreshBuilder(refreshCondition);
        builder.refreshCondition = refreshCondition;

        return builder;
    }

    public RefreshCond<T> build(){
        return this.refreshCondition;
    }

    public RefreshBuilder<T> and(){
        return this;
    }

    public RefreshBuilder<T> or() {
        return (RefreshBuilder<T>) super.or();
    }

    /**
     *
     * String sqlX = "propertyA = propertyA + propertyB + 1"
     * @return RefreshCondition
     */
    public RefreshBuilder<T> refresh(String sqlX){

        if (Objects.isNull(sqlX))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.X);
        bb.setKey(sqlX);
        this.refreshCondition.getRefreshList().add(bb);

        return this;
    }

    public RefreshBuilder<T> refresh(String property, Object value){

        if (Objects.isNull(value))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.EQ);
        bb.setKey(property);
        bb.setValue(value);
        this.refreshCondition.getRefreshList().add(bb);

        return this;
    }

    public KV tryToGetKeyOne() {
        if (this.refreshCondition.getClz() == null)
            return null;
        Parsed parsed = Parser.get(this.refreshCondition.getClz());
        String keyOne = parsed.getKey();
        for (Bb bb : this.refreshCondition.getBbList()) {
            String key = bb.getKey();
            if (key != null && key.equals(keyOne)) {
                return new KV(key, bb.getValue());
            }
        }
        return null;
    }


    public RefreshBuilder<T> routeKey(Object routeKey) {
        this.refreshCondition.setRouteKey(routeKey);
        return this;
    }


    public RefreshBuilder<T> eq(String key, Object value) {
        return (RefreshBuilder<T>) super.eq(key,value);
    }

    public RefreshBuilder<T> gt(String key, Object value) {
        return (RefreshBuilder<T>) super.gt(key,value);
    }

    public RefreshBuilder<T> gte(String key, Object value) {
        return (RefreshBuilder<T>) super.gte(key,value);
    }

    public RefreshBuilder<T> lt(String key, Object value) {
        return (RefreshBuilder<T>) super.lt(key,value);
    }

    public RefreshBuilder<T> lte(String key, Object value) {
        return (RefreshBuilder<T>) super.lte(key,value);
    }

    public RefreshBuilder<T> ne(String property, Object value) {
        return (RefreshBuilder<T>) super.ne(property, value);
    }

    public RefreshBuilder<T> like(String property, String value) {
        return (RefreshBuilder<T>) super.like(property, value);
    }

    public RefreshBuilder<T> likeRight(String property, String value) {
        return (RefreshBuilder<T>) super.likeRight(property, value);
    }

    public RefreshBuilder<T> notLike(String property, String value) {
        return (RefreshBuilder<T>) super.notLike(property, value);
    }

    public RefreshBuilder<T> in(String property, List<? extends Object> list) {
        return (RefreshBuilder<T>) super.in(property,list);
    }

    public RefreshBuilder<T> inRequired(String property, List<? extends Object> list) {
        if (list.isEmpty()) {
            refreshCondition.setAbort(true);
        }
        return (RefreshBuilder<T>) super.in(property,list);
    }

    public RefreshBuilder<T> nin(String property, List<? extends Object> list) {
        return (RefreshBuilder<T>) super.nin(property,list);
    }

    public RefreshBuilder<T> nonNull(String property){
        return (RefreshBuilder<T>) super.nonNull(property);
    }

    public RefreshBuilder<T> isNull(String property){
        return (RefreshBuilder<T>) super.isNull(property);
    }

    public RefreshBuilder  x(String sqlSegment){
        return (RefreshBuilder<T>) super.x(sqlSegment);
    }

    public RefreshBuilder  x(String sqlSegment, Object...values){
        return (RefreshBuilder<T>) super.x(sqlSegment, values);
    }

    public RefreshBuilder  beginSub(){
        return (RefreshBuilder<T>) super.beginSub();
    }

    public RefreshBuilder  endSub(){
        return (RefreshBuilder<T>) super.endSub();
    }

    public RefreshBuilder  bool(Bool condition, ThenRefresh then){
        if (condition.isOk()) {
            then.build(this);
        }
        return this;
    }

    public RefreshBuilder sourceScript(String sourceScript) {
        this.refreshCondition.setSourceScript(sourceScript);
        return this;
    }


}

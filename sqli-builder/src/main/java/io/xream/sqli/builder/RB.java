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
public class RB<T> extends BbQBuilder {

    private RQ<T> RQ;

    private RB(RQ refreshCondition){
        super();
        init(refreshCondition.getBbList());
    }


    public static <T> RB<T> of(Class<T> clzz){
        RQ RQ = new RQ();
        RQ.setClz(clzz);
        RB builder = new RB(RQ);
        builder.RQ = RQ;

        return builder;
    }

    public RQ<T> build(){
        return this.RQ;
    }

    public RB<T> and(){
        return this;
    }

    public RB<T> or() {
        return (RB<T>) super.or();
    }

    /**
     *
     * String sqlX = "propertyA = propertyA + propertyB + 1"
     * @return RefreshCondition
     */
    public RB<T> refresh(String sqlX){

        if (Objects.isNull(sqlX))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.X);
        bb.setKey(sqlX);
        this.RQ.getRefreshList().add(bb);

        return this;
    }

    public RB<T> refresh(String property, Object value){

        if (Objects.isNull(value))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.EQ);
        bb.setKey(property);
        bb.setValue(value);
        this.RQ.getRefreshList().add(bb);

        return this;
    }

    public KV tryToGetKeyOne() {
        if (this.RQ.getClz() == null)
            return null;
        Parsed parsed = Parser.get(this.RQ.getClz());
        String keyOne = parsed.getKey();
        for (Bb bb : this.RQ.getBbList()) {
            String key = bb.getKey();
            if (key != null && key.equals(keyOne)) {
                return new KV(key, bb.getValue());
            }
        }
        return null;
    }


    public RB<T> routeKey(Object routeKey) {
        this.RQ.setRouteKey(routeKey);
        return this;
    }


    public RB<T> eq(String key, Object value) {
        return (RB<T>) super.eq(key,value);
    }

    public RB<T> gt(String key, Object value) {
        return (RB<T>) super.gt(key,value);
    }

    public RB<T> gte(String key, Object value) {
        return (RB<T>) super.gte(key,value);
    }

    public RB<T> lt(String key, Object value) {
        return (RB<T>) super.lt(key,value);
    }

    public RB<T> lte(String key, Object value) {
        return (RB<T>) super.lte(key,value);
    }

    public RB<T> ne(String property, Object value) {
        return (RB<T>) super.ne(property, value);
    }

    public RB<T> like(String property, String value) {
        return (RB<T>) super.like(property, value);
    }

    public RB<T> likeRight(String property, String value) {
        return (RB<T>) super.likeRight(property, value);
    }

    public RB<T> notLike(String property, String value) {
        return (RB<T>) super.notLike(property, value);
    }

    public RB<T> in(String property, List<? extends Object> list) {
        return (RB<T>) super.in(property,list);
    }

    public RB<T> inRequired(String property, List<? extends Object> list) {
        if (list.isEmpty()) {
            RQ.setAbort(true);
        }
        return (RB<T>) super.in(property,list);
    }

    public RB<T> nin(String property, List<? extends Object> list) {
        return (RB<T>) super.nin(property,list);
    }

    public RB<T> nonNull(String property){
        return (RB<T>) super.nonNull(property);
    }

    public RB<T> isNull(String property){
        return (RB<T>) super.isNull(property);
    }

    public RB x(String sqlSegment){
        return (RB<T>) super.x(sqlSegment);
    }

    public RB x(String sqlSegment, Object...values){
        return (RB<T>) super.x(sqlSegment, values);
    }

    public RB beginSub(){
        return (RB<T>) super.beginSub();
    }

    public RB endSub(){
        return (RB<T>) super.endSub();
    }

    public RB bool(Bool condition, ThenRefresh then){
        if (condition.isOk()) {
            then.build(this);
        }
        return this;
    }

    public RB sourceScript(String sourceScript) {
        this.RQ.setSourceScript(sourceScript);
        return this;
    }


}

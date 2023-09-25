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

    private Qr<T> qr;

    private RB(Qr qr){
        super();
        init(qr.getBbList());
    }


    public static <T> RB<T> of(Class<T> clzz){
        Qr Qr = new Qr();
        Qr.setClz(clzz);
        RB builder = new RB(Qr);
        builder.qr = Qr;

        return builder;
    }

    public Qr<T> build(){
        return this.qr;
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
     * @return qr
     */
    public RB<T> refresh(String sqlX){

        if (Objects.isNull(sqlX))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.X);
        bb.setKey(sqlX);
        this.qr.getRefreshList().add(bb);

        return this;
    }

    public RB<T> refresh(String property, Object value){

        if (Objects.isNull(value))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.EQ);
        bb.setKey(property);
        bb.setValue(value);
        this.qr.getRefreshList().add(bb);

        return this;
    }

    public KV tryToGetKeyOne() {
        if (this.qr.getClz() == null)
            return null;
        Parsed parsed = Parser.get(this.qr.getClz());
        String keyOne = parsed.getKey();
        for (Bb bb : this.qr.getBbList()) {
            String key = bb.getKey();
            if (key != null && key.equals(keyOne)) {
                return new KV(key, bb.getValue());
            }
        }
        return null;
    }


    public RB<T> routeKey(Object routeKey) {
        this.qr.setRouteKey(routeKey);
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
            qr.setAbort(true);
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

    public RB<T> x(String sqlSegment){
        return (RB<T>) super.x(sqlSegment);
    }

    public RB<T> x(String sqlSegment, Object...values){
        return (RB<T>) super.x(sqlSegment, values);
    }

    public RB<T> beginSub(){
        return (RB<T>) super.beginSub();
    }

    public RB<T> endSub(){
        return (RB<T>) super.endSub();
    }

    public RB<T> bool(Bool condition, ThenRefresh then){
        if (condition.isOk()) {
            then.build(this);
        }
        return this;
    }

    public RB<T> sourceScript(String sourceScript) {
        this.qr.setSourceScript(sourceScript);
        return this;
    }


}

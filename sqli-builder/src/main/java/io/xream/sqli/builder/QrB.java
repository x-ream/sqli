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
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.util.List;
import java.util.Objects;

/**
 * @author Sim
 */
public class QrB<T> extends CondBuilder {

    private Qr<T> qr;

    private QrB(Qr qr){
        super();
        init(qr.getBbs());
    }


    public static <T> QrB<T> of(Class<T> clzz){
        Qr Qr = new Qr();
        Qr.setClz(clzz);
        QrB builder = new QrB(Qr);
        builder.qr = Qr;

        return builder;
    }

    public Qr<T> build(){
        return this.qr;
    }

    public QrB<T> or() {
        return (QrB<T>) super.or();
    }

    /**
     *
     * String sqlX = "propertyA = propertyA + propertyB + 1"
     * @return qr
     */
    public QrB<T> refresh(String sqlX){

        if (Objects.isNull(sqlX))
            return this;

        Bb bb = new Bb();
        bb.setP(Op.X);
        bb.setKey(sqlX);
        this.qr.getRefreshList().add(bb);

        return this;
    }

    public QrB<T> refresh(String property, Object value){

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
        for (Bb bb : this.qr.getBbs()) {
            String key = bb.getKey();
            if (key != null && key.equals(keyOne)) {
                return KV.of(key, bb.getValue());
            }
        }
        return null;
    }


    public QrB<T> routeKey(Object routeKey) {
        this.qr.setRouteKey(routeKey);
        return this;
    }


    public QrB<T> eq(String key, Object value) {
        return (QrB<T>) super.eq(key,value);
    }

    public QrB<T> gt(String key, Object value) {
        return (QrB<T>) super.gt(key,value);
    }

    public QrB<T> gte(String key, Object value) {
        return (QrB<T>) super.gte(key,value);
    }

    public QrB<T> lt(String key, Object value) {
        return (QrB<T>) super.lt(key,value);
    }

    public QrB<T> lte(String key, Object value) {
        return (QrB<T>) super.lte(key,value);
    }

    public QrB<T> ne(String property, Object value) {
        return (QrB<T>) super.ne(property, value);
    }

    public QrB<T> like(String property, String value) {
        return (QrB<T>) super.like(property, value);
    }

    public QrB<T> likeLeft(String property, String value) {
        return (QrB<T>) super.likeLeft(property, value);
    }

    public QrB<T> notLike(String property, String value) {
        return (QrB<T>) super.notLike(property, value);
    }

    public QrB<T> in(String property, List list) {
        return (QrB<T>) super.in(property,list);
    }

    public QrB<T> inRequired(String property, List list) {
        if (list.isEmpty()) {
            qr.setAbort(true);
        }
        return (QrB<T>) super.in(property,list);
    }

    public QrB<T> nin(String property, List list) {
        return (QrB<T>) super.nin(property,list);
    }

    public QrB<T> nonNull(String property){
        return (QrB<T>) super.nonNull(property);
    }

    public QrB<T> isNull(String property){
        return (QrB<T>) super.isNull(property);
    }

    public QrB<T> x(String sqlSegment){
        return (QrB<T>) super.x(sqlSegment);
    }

    public QrB<T> x(String sqlSegment, Object...values){
        return (QrB<T>) super.x(sqlSegment, values);
    }

    public QrB<T> and(SubCond sub) {
        return (QrB<T>) super.and(sub);
    }

    public QrB<T> or(SubCond sub) {
        return (QrB<T>) super.or(sub);
    }

    public QrB<T> bool(Bool condition, ThenRefresh then){
        if (condition.isOk()) {
            then.build(this);
        }
        return this;
    }

    public QrB<T> any(Any any){
        return (QrB<T>) super.any(any);
    }

    public QrB<T> sourceScript(String sourceScript) {
        this.qr.setSourceScript(sourceScript);
        return this;
    }


}

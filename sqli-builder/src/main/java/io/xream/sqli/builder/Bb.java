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


import java.util.List;
import java.util.Objects;

/**
 * @author Sim
 */
public final class Bb {

    private Op c;
    private Op p;
    private String key;
    private Object value;
    private List<Bb> subList;
    public Bb(){}
    public Bb(boolean isOr){
        if (isOr)
            c = Op.OR;
        else
            c = Op.AND;
    }
    public Op getC() {
        return c;
    }
    public void setC(Op c) {
        this.c = c;
    }
    public Op getP() {
        return p;
    }
    public void setP(Op p) {
        this.p = p;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public List<Bb> getSubList() {
        return subList;
    }
    public void setSubList(List<Bb> subList) {
        this.subList = subList;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Bb that = (Bb) object;
        return c == that.c &&
                p == that.p &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(c, p, key, value);
    }

    @Override
    public String toString() {
        return "Bb{" +
                "c=" + c +
                ", p=" + p +
                ", key=" + key +
                ", value=" + value +
                ", subList=" + subList +
                '}';
    }
}

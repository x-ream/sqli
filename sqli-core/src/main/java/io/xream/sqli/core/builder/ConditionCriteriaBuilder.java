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

import io.xream.sqli.common.util.SqliStringUtil;
import io.xream.sqli.util.BeanUtilX;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @Author Sim
 */
public class ConditionCriteriaBuilder {

    private ConditionCriteriaBuilder instance;

    private transient List<X> listX;
    private transient boolean isOr;

    private transient List<X> tempList;
    private transient List<List<X>> subsList = new LinkedList<>();

    protected ConditionCriteriaBuilder(){
        this.instance = this;
    }

    protected ConditionCriteriaBuilder(List<X> listX){
        this.instance = this;
        this.instance.listX = listX;
    }


    protected void init(List<X> listX){
        this.listX = listX;
    }

    public static ConditionCriteriaBuilder build(List<X> listX){
        return new ConditionCriteriaBuilder(listX);
    }

    public ConditionCriteriaBuilder eq(String property, Object value){
        return doGle(PredicateAndOtherScript.EQ, property, value);
    }

    public ConditionCriteriaBuilder lt(String property, Object value){
        return doGle(PredicateAndOtherScript.LT, property, value);
    }

    public ConditionCriteriaBuilder lte(String property, Object value){
        return doGle(PredicateAndOtherScript.LTE, property, value);
    }

    public ConditionCriteriaBuilder gt(String property, Object value){
        return doGle(PredicateAndOtherScript.GT, property, value);
    }

    public ConditionCriteriaBuilder gte(String property, Object value){
        return doGle(PredicateAndOtherScript.GTE, property, value);
    }

    public ConditionCriteriaBuilder ne(String property, Object value){
        return doGle(PredicateAndOtherScript.NE, property, value);
    }

    public ConditionCriteriaBuilder like(String property, String value){
        if (SqliStringUtil.isNullOrEmpty(value)) {
            isOr();
            return instance;
        }
        String likeValue = SqlScript.LIKE_HOLDER + value + SqlScript.LIKE_HOLDER;
        return doLike(PredicateAndOtherScript.LIKE,property,likeValue);
    }

    public ConditionCriteriaBuilder likeRight(String property, String value){
        if (SqliStringUtil.isNullOrEmpty(value)) {
            isOr();
            return instance;
        }
        String likeValue = value + SqlScript.LIKE_HOLDER;
        return doLike(PredicateAndOtherScript.LIKE,property,likeValue);
    }

    public ConditionCriteriaBuilder notLike(String property, String value){
        if (SqliStringUtil.isNullOrEmpty(value)) {
            isOr();
            return instance;
        }
        String likeValue = SqlScript.LIKE_HOLDER + value + SqlScript.LIKE_HOLDER;
        return doLike(PredicateAndOtherScript.NOT_LIKE,property,likeValue);
    }

    public ConditionCriteriaBuilder in(String property, List<? extends Object> list){
        return doIn(PredicateAndOtherScript.IN,property,list);
    }

    public ConditionCriteriaBuilder nin(String property, List<? extends Object> list){
        return doIn(PredicateAndOtherScript.NOT_IN,property,list);
    }

    public ConditionCriteriaBuilder nonNull(String property){
        return doNull(PredicateAndOtherScript.IS_NOT_NULL,property);
    }

    public ConditionCriteriaBuilder isNull(String property){
        return doNull(PredicateAndOtherScript.IS_NULL,property);
    }


    public ConditionCriteriaBuilder x(String sql, Object... values){

        if (SqliStringUtil.isNullOrEmpty(sql)){
            isOr();
            return instance;
        }

        sql = BeanUtilX.normalizeSql(sql);

        X x = new X(isOr());
        x.setPredicate(PredicateAndOtherScript.X);
        x.setKey(sql);
        x.setValue(values);
        this.add(x);

        return instance;
    }

    public ConditionCriteriaBuilder and(){
        this.isOr = false;
        return this.instance;
    }

    public ConditionCriteriaBuilder or(){
        this.isOr = true;
        return this.instance;
    }

    public ConditionCriteriaBuilder beginSub(){
        X x = new X(isOr());
        x.setPredicate(PredicateAndOtherScript.SUB);

        List<X> subList = new ArrayList<>();
        x.setSubList(subList);
        this.add(x);

        this.tempList = subList;
        this.subsList.add(subList);

        return instance;
    }
    public ConditionCriteriaBuilder endSub(){
        isOr();
        int size = subsList.size();
        if (--size >= 0)
        subsList.remove(size);
        if (--size >= 0){
            this.tempList = subsList.get(size);
        }else {
            this.tempList = null;
        }

        return this.instance;
    }


    private ConditionCriteriaBuilder doGle(PredicateAndOtherScript p, String property, Object value) {
        if (value == null){
            isOr();
            return instance;
        }
        if (SqliStringUtil.isNullOrEmpty(value)){
            isOr();
            return instance;
        }
        X x = new X(isOr());
        x.setPredicate(p);
        x.setKey(property);
        x.setValue(value);
        this.add(x);

        return instance;
    }

    private ConditionCriteriaBuilder doLike(PredicateAndOtherScript p,String property, String likeWalue){

        X x = new X(isOr());
        x.setPredicate(p);
        x.setKey(property);
        x.setValue(likeWalue);
        this.add(x);
        return instance;
    }


    private ConditionCriteriaBuilder doIn(PredicateAndOtherScript p, String property,List<? extends Object> list){

        if (list == null || list.isEmpty()){
            isOr();
            return instance;
        }

        List<Object> tempList = new ArrayList<>();
        for (Object obj : list) {
            if (Objects.isNull(obj))
                continue;
            if (!tempList.contains(obj)) {
                tempList.add(obj);
            }
        }

        if (tempList.isEmpty()){
            isOr();
            return instance;
        }

        X x = new X(isOr());
        x.setPredicate(p);
        x.setKey(property);
        x.setValue(tempList);
        this.add(x);

        return instance;
    }

    private ConditionCriteriaBuilder doNull(PredicateAndOtherScript p, String property){
        if (SqliStringUtil.isNullOrEmpty(property)){
            isOr();
            return instance;
        }

        X x = new X(isOr());
        x.setPredicate(p);
        x.setKey(property);
        x.setValue(p);
        this.add(x);

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

    private void add(X x){
        if (this.tempList == null)
            this.listX.add(x);
        else
            this.tempList.add(x);
    }

}

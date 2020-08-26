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
package io.xream.sqli.builder;

import io.xream.sqli.util.BeanUtilX;
import io.xream.sqli.util.SqliStringUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @Author Sim
 */
public class ConditionCriteriaBuilder {

    private ConditionCriteriaBuilder instance;

    private transient List<BuildingBlock> buildingBlockList;
    private transient boolean isOr;

    private transient List<BuildingBlock> tempList;
    private transient List<List<BuildingBlock>> subsList = new LinkedList<>();

    protected ConditionCriteriaBuilder(){
        this.instance = this;
    }

    protected ConditionCriteriaBuilder(List<BuildingBlock> buildingBlockList){
        this.instance = this;
        this.instance.buildingBlockList = buildingBlockList;
    }


    protected void init(List<BuildingBlock> buildingBlockList){
        this.buildingBlockList = buildingBlockList;
    }

    public static ConditionCriteriaBuilder build(List<BuildingBlock> buildingBlockList){
        return new ConditionCriteriaBuilder(buildingBlockList);
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

        BuildingBlock buildingBlock = new BuildingBlock(isOr());
        buildingBlock.setPredicate(PredicateAndOtherScript.X);
        buildingBlock.setKey(sql);
        buildingBlock.setValue(values);
        this.add(buildingBlock);

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
        BuildingBlock buildingBlock = new BuildingBlock(isOr());
        buildingBlock.setPredicate(PredicateAndOtherScript.SUB);

        List<BuildingBlock> subList = new ArrayList<>();
        buildingBlock.setSubList(subList);
        this.add(buildingBlock);

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
        BuildingBlock buildingBlock = new BuildingBlock(isOr());
        buildingBlock.setPredicate(p);
        buildingBlock.setKey(property);
        buildingBlock.setValue(value);
        this.add(buildingBlock);

        return instance;
    }

    private ConditionCriteriaBuilder doLike(PredicateAndOtherScript p,String property, String likeWalue){

        BuildingBlock buildingBlock = new BuildingBlock(isOr());
        buildingBlock.setPredicate(p);
        buildingBlock.setKey(property);
        buildingBlock.setValue(likeWalue);
        this.add(buildingBlock);
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

        BuildingBlock buildingBlock = new BuildingBlock(isOr());
        buildingBlock.setPredicate(p);
        buildingBlock.setKey(property);
        buildingBlock.setValue(tempList);
        this.add(buildingBlock);

        return instance;
    }

    private ConditionCriteriaBuilder doNull(PredicateAndOtherScript p, String property){
        if (SqliStringUtil.isNullOrEmpty(property)){
            isOr();
            return instance;
        }

        BuildingBlock buildingBlock = new BuildingBlock(isOr());
        buildingBlock.setPredicate(p);
        buildingBlock.setKey(property);
        buildingBlock.setValue(p);
        this.add(buildingBlock);

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

    private void add(BuildingBlock buildingBlock){
        if (this.tempList == null)
            this.buildingBlockList.add(buildingBlock);
        else
            this.tempList.add(buildingBlock);
    }

}

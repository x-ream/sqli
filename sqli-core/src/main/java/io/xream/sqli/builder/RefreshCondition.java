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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.xream.sqli.annotation.X;
import io.xream.sqli.api.Alias;
import io.xream.sqli.api.Routeable;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.util.*;


/**
 * @Author Sim
 */
public final class RefreshCondition<T> extends ConditionCriteriaBuilder implements Alias,CriteriaCondition, Routeable {

    private List<BuildingBlock> refreshList = new ArrayList<>();
    private String sourceScript;//FIXME

    private List<BuildingBlock> buildingBlockList = new ArrayList<>();
    private Object routeKey;
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

    public List<BuildingBlock> getRefreshList() {
        return refreshList;
    }


    public String getSourceScript() {
        return this.sourceScript;
    }

    public void setSourceScript(String sourceScript) {
        this.sourceScript = sourceScript;
    }

    @Override
    public List<BuildingBlock> getBuildingBlockList() {
        return buildingBlockList;
    }

    public List<Object> getValueList() {
        return this.valueList;
    }

    @Override
    public Map<String, String> getAliaMap() {
        return this.aliaMap;
    }

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

    @Deprecated
    public RefreshCondition(){
        super();
        init(this.buildingBlockList);
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

        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setPredicate(PredicateAndOtherScript.X);
        buildingBlock.setKey(sqlX);
        this.refreshList.add(buildingBlock);

        return this;
    }

    public RefreshCondition refresh(String property, Object value){

        if (Objects.isNull(value))
            return this;

        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setPredicate(PredicateAndOtherScript.EQ);
        buildingBlock.setKey(property);
        buildingBlock.setValue(value);
        this.refreshList.add(buildingBlock);

        return this;
    }

    public KV tryToGetKeyOne() {
        if (clz == null)
            return null;
        Parsed parsed = Parser.get(clz);
        String keyOne = parsed.getKey(X.KEY_ONE);
        for (BuildingBlock buildingBlock : buildingBlockList) {
            String key = buildingBlock.getKey();
            if (key != null && key.equals(keyOne)) {
                return new KV(key, buildingBlock.getValue());
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

    public RefreshCondition  x(String sql, List<? extends Object> valueList){
        return (RefreshCondition) super.x(sql, valueList);
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

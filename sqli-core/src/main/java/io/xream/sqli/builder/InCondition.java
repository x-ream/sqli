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
import io.xream.sqli.api.Routable;

import java.util.List;


/**
 * @Author Sim
 */
public final class InCondition implements Routable {

    private String property;
    private List<? extends Object> inList;

    private Object routeKey;

    @JsonIgnore
    private transient Class clz;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public List<? extends Object> getInList() {
        return inList;
    }

    public void setInList(List<? extends Object> inList) {
        this.inList = inList;
    }

    public Class getClz() {
        return clz;
    }

    public void setClz(Class clz) {
        this.clz = clz;
    }

    @Override
    public Object getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Object routeKey) {
        this.routeKey = routeKey;
    }

    @Deprecated
    public InCondition(){
    }

    @Deprecated
    public InCondition(String property,List<? extends Object> inList ){
        this.property = property;
        this.inList = inList;
    }

    public static InCondition of(String property, List<? extends Object> inList ){
        return of(null,property,inList);
    }

    public static InCondition of(Object routeKey, String property, List<? extends Object> inList ){
        InCondition inCondition = new InCondition();
        inCondition.setRouteKey(routeKey);
        inCondition.setProperty(property);
        inCondition.setInList(inList);
        return inCondition;
    }

    @Override
    public String toString() {
        return "InCondition{" +
                "property='" + property + '\'' +
                ", inList=" + inList +
                ", clz=" + clz +
                '}';
    }
}
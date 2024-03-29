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


import io.xream.sqli.util.SqliStringUtil;

/**
 * @author Sim
 */
public final class ResultKeyAlia {

    private String objectOrAlia;
    private String propertyOrAlia;

    public void setObjectOrAlia(String objectOrAlia) {
        this.objectOrAlia = objectOrAlia;
    }

    public void setPropertyOrAlia(String propertyOrAlia) {
        this.propertyOrAlia = propertyOrAlia;
    }

    public static ResultKeyAlia of(String propertyOrAlia) {
        ResultKeyAlia functionAlia = new ResultKeyAlia();
        functionAlia.setPropertyOrAlia(propertyOrAlia);
        return functionAlia;
    }

    public static ResultKeyAlia of(String objectOrAlia, String propertyOrAlia) {
        ResultKeyAlia functionAlia = new ResultKeyAlia();
        functionAlia.setObjectOrAlia(objectOrAlia);
        functionAlia.setPropertyOrAlia(propertyOrAlia);
        return functionAlia;
    }

    public String getAlia(){
        if (SqliStringUtil.isNullOrEmpty(objectOrAlia))
            return propertyOrAlia;
        return objectOrAlia + "." + propertyOrAlia;
    }

    public String getKey(){
        if (SqliStringUtil.isNullOrEmpty(objectOrAlia))
            return propertyOrAlia;
        return objectOrAlia + "_" + propertyOrAlia;
    }

    @Override
    public String toString() {
        return "FunctionAlia{" +
                "objectOrAlia='" + objectOrAlia + '\'' +
                ", propertyOrAlia='" + propertyOrAlia + '\'' +
                '}';
    }
}

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
package io.xream.sqli.builder.internal;

import io.xream.sqli.builder.CondBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sim
 */
public final class On {

    private List<Bb> bbs = new ArrayList<>();
    private CondBuilder builder;
    private String orUsingKey;

    public List<Bb> getBbs() {
        return bbs;
    }

    public void setBbs(List<Bb> bbs) {
        this.bbs = bbs;
    }

    public CondBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(CondBuilder builder) {
        this.builder = builder;
    }

    public String getOrUsingKey() {
        return orUsingKey;
    }

    public void setOrUsingKey(String orUsingKey) {
        this.orUsingKey = orUsingKey;
    }
}

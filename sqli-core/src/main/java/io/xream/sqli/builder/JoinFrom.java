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

/**
 * @Author Sim
 */
public class JoinFrom {

    private String alia;
    private String key;

    public String getAlia() {
        return alia;
    }

    public void setAlia(String alia) {
        this.alia = alia;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static JoinFrom of(String alia, String key) {
        if (key.contains("."))
            throw new IllegalArgumentException("JoinFrom key can not contains '.'");
        JoinFrom joinFrom = new JoinFrom();
        joinFrom.setAlia(alia);
        joinFrom.setKey(key);
        return joinFrom;
    }

    @Override
    public String toString() {
        return "JoinFrom{" +
                "alia='" + alia + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}

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
package io.xream.sqli.cache;


import io.xream.sqli.util.SqliStringUtil;

/**
 * @Author Sim
 */
public final class L2CacheFilter {

    private static final ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    /**
     * partialKey maybe is userId
     * @param partialKey
     */
    public static void filter(Object partialKey) {
        if (SqliStringUtil.isNullOrEmpty(partialKey))
            return;
        threadLocal.set(partialKey);
    }

    protected static Object get() {
        return threadLocal.get();
    }

    protected static void close() {
        threadLocal.remove();
    }

}

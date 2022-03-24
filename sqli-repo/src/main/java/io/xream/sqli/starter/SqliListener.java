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
package io.xream.sqli.starter;

import io.xream.sqli.api.customizer.DialectCustomizer;
import io.xream.sqli.core.NativeSupport;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.parser.ParserListener;
import io.xream.sqli.repository.exception.UninitializedException;
import io.xream.sqli.repository.init.SqlInit;
import io.xream.sqli.spi.L2CacheConsistency;
import io.xream.sqli.spi.L2CacheResolver;
import io.xream.sqli.spi.Schema;
import io.xream.sqli.util.SqliJsonUtil;

/**
 * @author Sim
 */
public class SqliListener {

    private static SqliListener instance;
    private static boolean initialized = false;

    private SqliListener(){}

    public static void cusomizeJsonConfig(SqliJsonUtil.Customizer customizer) {
        customizer.customize();
    }

    public static void onBeanCreated(InitPhaseable initPhaseable){
        initialized |= initPhaseable.init();
    }

    public static void onL2CacheEnabled(L2CacheResolver l2CacheResolver, L2CacheConsistency l2CacheConsistency) {
        if (l2CacheResolver != null && l2CacheConsistency != null) {
            l2CacheResolver.setL2CacheConsistency(l2CacheConsistency);
        }
    }

    public static void customizeDialectOnStarted(Dialect dialect, DialectCustomizer dialectCustomizer) {
        DialectListener.customizeOnStarted(dialect,dialectCustomizer);
    }

    public static void onStarted(NativeSupport nativeSupport,  SqlInit sqlInit, Schema schema){
        if (instance != null)
            return;

        if (! initialized)
            throw new UninitializedException("to confirm all bean initialized, please call SqliListener.onBeanCreated(...) at leaset one time");

        instance = new SqliListener();

        InitializerListener.onStarted(nativeSupport,sqlInit, schema);
        ParserListener.onStarted();
    }
}

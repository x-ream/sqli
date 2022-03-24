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
package io.xream.sqli.util;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Sim
 */
public final class SqliLoggerProxy {

    private final static Map<Class, Logger> loggerMap = new HashMap<>();

    private SqliLoggerProxy(){}

    public static void put(Class clzz, Logger logger) {
        loggerMap.put(clzz,logger);
    }

    public static void debug(Class clzz, Object obj) {
        Logger logger = loggerMap.get(clzz);
        if (logger == null || obj == null)
            return;
        if (logger.isDebugEnabled()) {
            logger.debug(obj.toString());
        }
    }


    public static void debug(Class clzz,  LogCallable callable) {
        Logger logger = loggerMap.get(clzz);
        if (logger == null )
            return;
        if (logger.isDebugEnabled()) {
            if (callable == null)
                return;
            String str = callable.call();
            if (SqliStringUtil.isNullOrEmpty(str))
                return;
            logger.debug(str);
        }
    }

    public static void info(Class clzz, Object obj) {
        Logger logger = loggerMap.get(clzz);
        if (logger == null || obj == null)
            return;
        else if (logger.isInfoEnabled()){
            logger.info(obj.toString());
        }
    }

    public static long getTimeMills(Class clzz){
        Logger logger = loggerMap.get(clzz);
        if (logger == null)
            return 0;
        if (logger.isDebugEnabled())
            return System.currentTimeMillis();
        return 0;
    }

    public interface LogCallable{
        String call();
    }
}

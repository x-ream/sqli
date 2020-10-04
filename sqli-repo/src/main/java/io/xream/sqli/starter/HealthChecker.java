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

import io.xream.sqli.api.BaseRepository;
import io.xream.sqli.builder.DialectSupport;
import io.xream.sqli.core.NativeSupport;
import io.xream.sqli.core.RepositoryManagement;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.exception.UninitializedException;
import io.xream.sqli.repository.init.SqlInit;
import io.xream.sqli.repository.init.SqlInitFactory;
import io.xream.sqli.util.SqliStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Sim
 */
public class HealthChecker {

    private final static Logger logger = LoggerFactory.getLogger(HealthChecker.class);

    private static HealthChecker instance;
    private HealthChecker(){}

    public static void onStarted(NativeSupport nativeSupport, DialectSupport dialect) {

        if (instance != null)
            return;
        instance = new HealthChecker();

        for (BaseRepository repository : RepositoryManagement.REPOSITORY_LIST) {
            if (repository.getClzz() == Void.class)
                continue;
            logger.info("Parsing {}" ,repository.getClzz());
            Parser.get(repository.getClzz());
        }


        boolean flag = false;

        for (BaseRepository repository : RepositoryManagement.REPOSITORY_LIST) {

            try {
                Class clz = repository.getClzz();
                if (repository.getClzz() == Void.class)
                    continue;
                String createSql = SqlInitFactory.tryToCreate(clz);
                String test = SqlInitFactory.getSql(clz, SqlInit.CREATE);
                if (SqliStringUtil.isNullOrEmpty(test)) {
                    logger.info("Failed to start sqli-repo, check Bean: {}",clz);
                    throw new UninitializedException("Failed to start sqli-repo, check Bean: " + clz);
                }

                if (dialect.getKey().contains("mysql") && SqliStringUtil.isNotNull(createSql)) {
                    nativeSupport.execute(clz, createSql);
                }

            } catch (Exception e) {
                flag |= true;
            }
        }

        logger.info("sqli-repo " + (flag ? "still " : "") + "started" + (flag ? " OK, wtih some problem" : "" ) + "\n");

    }
}
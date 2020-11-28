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
import io.xream.sqli.core.NativeSupport;
import io.xream.sqli.core.RepositoryManagement;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.exception.UninitializedException;
import io.xream.sqli.repository.init.SqlInit;
import io.xream.sqli.util.SqliStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Sim
 */
public class InitializerListener {

    private final static Logger logger = LoggerFactory.getLogger(InitializerListener.class);

    private static InitializerListener instance;

    private InitializerListener() {
    }

    protected static void onStarted(NativeSupport nativeSupport, SqlInit sqlInit) {

        if (instance != null)
            return;
        instance = new InitializerListener();


        for (BaseRepository repository : RepositoryManagement.REPOSITORY_LIST) {
            if (repository.getClzz() == Void.class)
                continue;
            logger.info("Parsing {}", repository.getClzz());
            try {
                Parser.get(repository.getClzz());
            } catch (ParsingException pe) {
                throw new ParsingException(repository.getClzz() + ", " + pe.getMessage());
            }catch (Exception e) {
            }
        }

        boolean flag = false;
        boolean isNotSupportTableSql = false;

        for (BaseRepository repository : RepositoryManagement.REPOSITORY_LIST) {

            try {
                Class clz = repository.getClzz();
                if (repository.getClzz() == Void.class)
                    continue;
                String createSql = sqlInit.tryToParse(clz);
                String test = sqlInit.getSql(clz, SqlInit.CREATE);
                if (SqliStringUtil.isNullOrEmpty(test)) {
                    logger.info("Failed to start sqli-repo, check Bean: {}", clz);
                    throw new UninitializedException("Failed to start sqli-repo, check Bean: " + clz);
                }

                if (SqliStringUtil.isNotNull(createSql)) {
                    nativeSupport.execute(createSql);
                }

            } catch (Exception e) {
                flag |= true;
                if (e.getClass().getSimpleName().toLowerCase().contains("grammar")) {
                    isNotSupportTableSql = true;
                }
            }
        }

        if (isNotSupportTableSql) {
            logger.info("The dialect not support creating table, try to implement Dialect.buildTableSql(clzz, isTemporary)");
        }

        logger.info("sqli-repo " + (flag ? "still " : "") + "started" + (flag ? " OK, wtih some problem" : "") + "\n");

    }
}

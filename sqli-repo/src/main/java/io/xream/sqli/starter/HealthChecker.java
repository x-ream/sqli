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
package io.xream.sqli.starter;

import io.xream.sqli.api.BaseRepository;
import io.xream.sqli.api.RepositoryManagement;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.api.ManuRepository;
import io.xream.sqli.repository.mapper.Mapper;
import io.xream.sqli.repository.mapper.MapperFactory;
import io.xream.sqli.util.SqliStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Sim
 */
public class HealthChecker {

    private final static Logger logger = LoggerFactory.getLogger(HealthChecker.class);


    public static void onStarted() {

        for (BaseRepository repository : RepositoryManagement.REPOSITORY_LIST) {
            logger.info("Parsing {}" ,repository.getClz());
            Parser.get(repository.getClz());
        }

        logger.info("-------------------------------------------------");

        boolean flag = false;

        for (BaseRepository repository : RepositoryManagement.REPOSITORY_LIST) {

            try {
                Class clz = repository.getClz();
                String createSql = MapperFactory.tryToCreate(clz);
                String test = MapperFactory.getSql(clz, Mapper.CREATE);
                if (SqliStringUtil.isNullOrEmpty(test)) {
                    logger.info("Failed to start x7-jdbc-template-plus, check Bean: {}",clz);
                    Runtime.getRuntime().exit(1);
                }

                if (DbType.value.equals(DbType.MYSQL) && SqliStringUtil.isNotNull(createSql)) {
                    ManuRepository.execute(clz.newInstance(), createSql);
                }


            } catch (Exception e) {
                flag |= true;
            }
        }

        logger.info("x7-repo/x7-jdbc-template-plus " + (flag ? "still " : "") + "started" + (flag ? " OK, wtih some problem" : "" ) + "\n");

    }
}
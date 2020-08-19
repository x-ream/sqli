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
package io.xream.x7.repository.internal;

import io.xream.sqli.core.builder.Criteria;
import io.xream.sqli.core.builder.Parsed;
import io.xream.sqli.core.util.SqliExceptionUtil;
import io.xream.sqli.api.TemporaryRepository;
import io.xream.x7.repository.dao.TemporaryDao;
import io.xream.x7.repository.transform.DataTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class DefaultTemporaryRepository implements TemporaryRepository {

    private Logger logger = LoggerFactory.getLogger(TemporaryRepository.class);


    private TemporaryDao temporaryDao;

    private Parser temporaryRepositoryParser;

    private DataTransform dataTransform;
    public void setDataTransform(DataTransform dataTransform) {
        this.dataTransform = dataTransform;
    }

    public void setTemporaryDao(TemporaryDao temporaryDao) {
        this.temporaryDao = temporaryDao;
    }

    public void setTemporaryRepositoryParser(Parser temporaryRepositoryParser) {
        this.temporaryRepositoryParser = temporaryRepositoryParser;
    }

    private boolean doProxy(String logTag, Callable<Boolean> callable) {
        long startTime = System.currentTimeMillis();
        boolean flag = false;
        try {
            flag = callable.call();
        }catch (Exception e){
            logger.warn("{} exception: {}" , logTag, SqliExceptionUtil.getMessage(e));
            throw new RuntimeException(SqliExceptionUtil.getMessage(e));
        }finally {
            long endTime = System.currentTimeMillis();
            logger.info("{} result: {}, cost time: {}ms" , logTag, flag, (endTime - startTime));
        }

        return flag;
    }


    @Override
    public boolean create(Object obj) {
        return doProxy("create(Object)", () -> dataTransform.create(obj));
    }

    @Override
    public boolean createBatch(List objList) {
        return doProxy("createBatch(List)", () -> dataTransform.createBatch(objList) );
    }

    @Override
    public boolean findToCreate(Class clzz, Criteria.ResultMappedCriteria resultMappedCriteria) {

        return doProxy("findToCreate(Class, ResultMappedCriteria)", () -> {
            Parsed parsed = io.xream.sqli.core.builder.Parser.get(clzz.getSimpleName());
            if (parsed == null) {
                io.xream.sqli.core.builder.Parser.parse(clzz);
            }

            return temporaryDao.findToCreate(clzz, resultMappedCriteria);
        });

    }

    @Override
    public boolean createRepository(Class clzz) {

        return doProxy("createRepository(Class)", () -> {
            String sql = temporaryRepositoryParser.parseAndGetSql(clzz);
            return temporaryDao.execute(sql);
        });

    }

    @Override
    public boolean dropRepository(Class clzz) {

        return doProxy("dropRepository(Class)", () -> {
            Parsed parsed = io.xream.sqli.core.builder.Parser.get(clzz.getSimpleName());
            if (parsed == null) {
                parsed = io.xream.sqli.core.builder.Parser.get(clzz);
            }
            String sql = "DROP TABLE " + parsed.getTableName();
            return temporaryDao.execute(sql);
        });

    }
}

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

import io.xream.sqli.api.*;
import io.xream.sqli.cache.L2CacheResolver;
import io.xream.sqli.repository.api.CriteriaToSql;
import io.xream.sqli.repository.api.Manuable;
import io.xream.sqli.repository.api.Repository;
import io.xream.sqli.repository.cache.CacheableRepository;
import io.xream.sqli.repository.dao.*;
import io.xream.sqli.repository.internal.DefaultRepository;
import io.xream.sqli.repository.internal.DefaultTemporaryRepository;
import io.xream.sqli.repository.mapper.DefaultTemporaryTableParser;
import io.xream.sqli.repository.transform.DataTransform;

import java.util.concurrent.Callable;

/**
 * @Author Sim
 */
public class SqliStarter implements RepositoryInitializer{

    private IdGenerator idGenerator;
    private CriteriaToSql criteriaParser;
    private JdbcWrapper jdbcWrapper;
    private Dao dao;
    private Repository dataRepository;
    private TemporaryRepository.Parser temporaryTableParser;
    private TemporaryDao temporaryDao;
    private TemporaryRepository temporaryRepository;


    private static SqliStarter instance;
    private static SqliStarter getInstance(){
        if (instance == null) {
            instance = new SqliStarter();
        }
        return instance;
    }
    private SqliStarter(){
        init();
    }
    private void init(){
        criteriaParser =  new DefaultCriteriaToSql();

        dao = new DaoImpl();
//        dataTransform.setDao(dao);

        dataRepository = new CacheableRepository();
        ManuRepositoryStarter.init((Manuable) dataRepository);

        temporaryTableParser = new DefaultTemporaryTableParser();
        temporaryDao = new TemporaryDaoImpl();
        temporaryRepository = new DefaultTemporaryRepository();
        ((DefaultTemporaryRepository)temporaryRepository).setTemporaryDao(temporaryDao);
        ((DefaultTemporaryRepository)temporaryRepository).setTemporaryRepositoryParser(temporaryTableParser);

    }

    public Dao getDao(){
        return this.dao;
    }

    public void setDataTransformAfterInit(Callable<DataTransform> callable){
        DataTransform dataTransform = null;
        try{
            dataTransform = callable.call();
        }catch (Exception e){
        }
        ((CacheableRepository)dataRepository).setDataTransform(dataTransform);
        ((DefaultTemporaryRepository)temporaryRepository).setDataTransform(dataTransform);
    }

    public void setJdbcWrapper(JdbcWrapper jdbcWrapper){
        this.jdbcWrapper = jdbcWrapper;
        ((DaoImpl)dao).setJdbcWrapper(jdbcWrapper);
        ((TemporaryDaoImpl)temporaryDao).setJdbcWrapper(jdbcWrapper);
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public void setL2CacheResolver(L2CacheResolver l2CacheResolver) {
        ((CacheableRepository)dataRepository).setCacheResolver(l2CacheResolver);
    }

    public void setDialect(Dialect dialect) {

        criteriaParser.setDialect(dialect);
        ((DefaultTemporaryTableParser)temporaryTableParser).setDialect(dialect);
        ((DaoImpl)dao).setDialect(dialect);
        ((TemporaryDaoImpl)temporaryRepository).setDialect(dialect);
    }

    @Override
    public <T> void register(BaseRepository<T> baseRepository){
        ((DefaultRepository<T>)baseRepository).setIdGeneratorService(idGenerator);
        ((DefaultRepository<T>)baseRepository).setRepository(dataRepository);
        RepositoryManagement.REPOSITORY_LIST.add(baseRepository);
    }
}

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

import io.xream.sqli.api.NativeRepository;
import io.xream.sqli.core.NativeSupport;
import io.xream.sqli.api.TemporaryRepository;
import io.xream.sqli.builder.CriteriaToSql;
import io.xream.sqli.builder.internal.DefaultCriteriaToSql;
import io.xream.sqli.cache.L2CacheResolver;
import io.xream.sqli.core.Dialect;
import io.xream.sqli.core.JdbcWrapper;
import io.xream.sqli.core.Repository;
import io.xream.sqli.core.core.CacheableRepository;
import io.xream.sqli.core.dao.Dao;
import io.xream.sqli.core.dao.DaoImpl;
import io.xream.sqli.core.dao.TemporaryDao;
import io.xream.sqli.core.dao.TemporaryDaoImpl;
import io.xream.sqli.core.init.DefaultTemporaryTableParser;
import io.xream.sqli.core.internal.DefaultTemporaryRepository;
import io.xream.sqli.core.internal.NativeRepositoryImpl;

/**
 * @Author Sim
 */
public class SqliStarter {

    private static SqliStarter instance;
    public static SqliStarter getInstance(){
        if (instance == null) {
            instance = new SqliStarter();
        }
        return instance;
    }
    private SqliStarter(){

    }

    public CriteriaToSql criteriaToSql(){
        return DefaultCriteriaToSql.newInstance();
    }

    public Repository repository(CriteriaToSql criteriaToSql, JdbcWrapper jdbcWrapper,
                                         Dialect dialect,
                                         L2CacheResolver l2CacheResolver
                                         ){
        Dao dao = DaoImpl.newInstance();

        CacheableRepository repository = CacheableRepository.newInstance();

        repository.setDao(dao);
        ((DaoImpl)dao).setCriteriaToSql(criteriaToSql);
        ((DaoImpl)dao).setJdbcWrapper(jdbcWrapper);
        ((DaoImpl)dao).setDialect(dialect);

        repository.setCacheResolver(l2CacheResolver);

        return repository;
    }


    public TemporaryRepository temporaryRepository(CriteriaToSql criteriaToSql, JdbcWrapper jdbcWrapper,Dialect dialect,Repository repository){
        TemporaryRepository.Parser temporaryTableParser = DefaultTemporaryTableParser.newInstance();
        TemporaryDao temporaryDao = TemporaryDaoImpl.newInstance();
        ((TemporaryDaoImpl)temporaryDao).setJdbcWrapper(jdbcWrapper);
        ((TemporaryDaoImpl)temporaryDao).setCriteriaToSql(criteriaToSql);
        ((TemporaryDaoImpl)temporaryDao).setDialect(dialect);

        TemporaryRepository tr = DefaultTemporaryRepository.newInstance();
        ((DefaultTemporaryRepository)tr).setTemporaryDao(temporaryDao);
        ((DefaultTemporaryRepository)tr).setTemporaryRepositoryParser(temporaryTableParser);
        ((DefaultTemporaryRepository)tr).setRepository(repository);
        return tr;
    }


    public NativeRepository nativeRepository(Repository repository){
        NativeRepository nativeRepository = NativeRepositoryImpl.newInstance();
        ((NativeRepositoryImpl) nativeRepository).setNativeSupport((NativeSupport) repository);
        return nativeRepository;
    }

}

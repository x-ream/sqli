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
import io.xream.sqli.api.TemporaryRepository;
import io.xream.sqli.builder.CriteriaToSql;
import io.xream.sqli.builder.internal.DefaultCriteriaToSql;
import io.xream.sqli.core.NativeSupport;
import io.xream.sqli.core.Repository;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.dialect.DynamicDialect;
import io.xream.sqli.repository.core.CacheableRepository;
import io.xream.sqli.repository.dao.Dao;
import io.xream.sqli.repository.dao.DaoImpl;
import io.xream.sqli.repository.dao.TemporaryDao;
import io.xream.sqli.repository.dao.TemporaryDaoImpl;
import io.xream.sqli.repository.init.DefaultSqlInit;
import io.xream.sqli.repository.init.DefaultTemporaryTableParser;
import io.xream.sqli.repository.init.SqlInit;
import io.xream.sqli.repository.internal.DefaultNativeRepository;
import io.xream.sqli.repository.internal.DefaultTemporaryRepository;
import io.xream.sqli.spi.JdbcHelper;
import io.xream.sqli.spi.L2CacheResolver;

/**
 * @author Sim
 */
public class SqliStarter {

    private static SqliStarter instance;
    public static SqliStarter getInstance(){
        if (instance == null) {
            instance = new SqliStarter();
        }
        return instance;
    }
    private SqliStarter(){}

    public Dialect dialect(Dialect dialect) {
        DynamicDialect dynamicDialect = new DynamicDialect();
        dynamicDialect.setDefaultDialect(dialect);
        return dynamicDialect;
    }

    public CriteriaToSql criteriaToSql(){
        return DefaultCriteriaToSql.newInstance();
    }

    public Repository repository(CriteriaToSql criteriaToSql, JdbcHelper jdbcHelper,
                                         Dialect dialect,
                                         L2CacheResolver l2CacheResolver
                                         ){
        Dao dao = DaoImpl.newInstance();

        CacheableRepository repository = CacheableRepository.newInstance();

        repository.setDao(dao);
        ((DaoImpl)dao).setCriteriaToSql(criteriaToSql);
        ((DaoImpl)dao).setJdbcHelper(jdbcHelper);
        ((DaoImpl)dao).setDialect(dialect);

        repository.setCacheResolver(l2CacheResolver);

        return repository;
    }

    public TemporaryRepository temporaryRepository(CriteriaToSql criteriaToSql, JdbcHelper jdbcHelper, Dialect dialect, Repository repository){
        DefaultTemporaryTableParser temporaryTableParser = DefaultTemporaryTableParser.newInstance();
        temporaryTableParser.setDialect(dialect);
        TemporaryDao temporaryDao = TemporaryDaoImpl.newInstance();
        ((TemporaryDaoImpl)temporaryDao).setJdbcHelper(jdbcHelper);
        ((TemporaryDaoImpl)temporaryDao).setCriteriaToSql(criteriaToSql);
        ((TemporaryDaoImpl)temporaryDao).setDialect(dialect);

        TemporaryRepository tr = DefaultTemporaryRepository.newInstance();
        ((DefaultTemporaryRepository)tr).setTemporaryDao(temporaryDao);
        ((DefaultTemporaryRepository)tr).setTemporaryRepositoryParser(temporaryTableParser);
        ((DefaultTemporaryRepository)tr).setRepository(repository);
        return tr;
    }

    public NativeRepository nativeRepository(Repository repository){
        NativeRepository nativeRepository = DefaultNativeRepository.newInstance();
        ((DefaultNativeRepository) nativeRepository).setNativeSupport((NativeSupport) repository);
        return nativeRepository;
    }

    public SqlInit sqlInit(Dialect dialect){
        SqlInit sqlInit = DefaultSqlInit.newInstance();
        sqlInit.setDialect(dialect);
        return sqlInit;
    }

}

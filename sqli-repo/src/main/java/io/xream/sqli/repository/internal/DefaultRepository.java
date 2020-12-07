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
package io.xream.sqli.repository.internal;


import io.xream.sqli.api.BaseRepository;
import io.xream.sqli.api.ResultMapRepository;
import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.InCondition;
import io.xream.sqli.builder.RefreshCondition;
import io.xream.sqli.builder.RemoveRefreshCreate;
import io.xream.sqli.core.*;
import io.xream.sqli.exception.PersistenceException;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.SqliStringUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Implement of BaseRepository, ResultMapRepository
 *
 * @param <T>
 * @author Sim
 */
public abstract class DefaultRepository<T> implements BaseRepository<T>, ResultMapRepository, SafeRefreshBiz<T> {

    private Class<T> clzz;
    private Class repositoryClzz;
    private IdGenerator idGenerator;
    private Repository repository;


    @Override
    public Class<T> getClzz() {
        return this.clzz;
    }
    /**
     *
     * Can not rename setClzz
     */
    public void setClz(Class<T> clz) {
        this.clzz = clz;
    }

    /**
     * para implements IdGeneratorProxy
     * @param idGenerator
     */
    public void setIdGenerator(IdGenerator idGenerator){
        this.idGenerator = idGenerator;
    }

    public void setRepository(Repository repository) {
        this.repository =repository;
    }

    public DefaultRepository(){
        parse();
    }

    private void parse(){
        Type genType = getClass().getGenericSuperclass();

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (! (params[0] instanceof Class))
            return;

        this.clzz = (Class) params[0];

        hook();
    }

    protected void hook() {
        if (!RepositoryManagement.REPOSITORY_LIST.contains(this)) {
            RepositoryManagement.REPOSITORY_LIST.add(this);
        }
    }


    @Override
    public long createId() {

        final String clzName = this.clzz.getName();

        final long id = this.idGenerator.createId(clzName);

        if (id == 0)
            throw new PersistenceException("UNEXPECTED EXCEPTION WHILE CREATING ID");

        return id;
    }

    @Override
    public boolean createBatch(List<T> objList) {
        return repository.createBatch(objList);
    }

    @Override
    public boolean create(T obj) {
        return repository.create(obj);
    }

    @Override
    public boolean createOrReplace(T obj) {
        return repository.createOrReplace(obj);
    }


    @Override
    public boolean refresh(RefreshCondition refreshCondition) {

        tryToRefreshSafe(this.clzz, refreshCondition);

        return repository.refresh(refreshCondition);
    }

    @Override
    public boolean refreshUnSafe(RefreshCondition<T> refreshCondition) {
        refreshCondition.setClz(this.clzz);
        return repository.refresh(refreshCondition);
    }

    @Override
    public boolean removeRefreshCreate(RemoveRefreshCreate<T> wrapper){
        return RemoveRefreshCreateBiz.doIt(this.clzz,this.repository,wrapper);
    }

    @Override
    public boolean remove(String keyOne) {

        if (SqliStringUtil.isNullOrEmpty(keyOne))
            return false;

        return repository.remove(new KeyOne<T>() {

            @Override
            public Object get() {
                return keyOne;
            }

            @Override
            public Class<T> getClzz() {
                return clzz;
            }
        });
    }

    @Override
    public boolean remove(long  keyOne) {

        if (keyOne == 0)
            return false;

        return repository.remove(new KeyOne<T>() {
            @Override
            public Object get() {
                return keyOne;
            }

            @Override
            public Class<T> getClzz() {
                return clzz;
            }
        });
    }

    @Override
    public T get(long keyOne) {

        if (keyOne == 0)
            return null;

        return repository.get(new KeyOne<T>() {
            @Override
            public Object get() {
                return keyOne;
            }

            @Override
            public Class<T> getClzz() {
                return clzz;
            }
        });
    }

    @Override
    public T get(String keyOne) {

        if (SqliStringUtil.isNullOrEmpty(keyOne))
            return null;

        return repository.get(new KeyOne<T>() {
            @Override
            public Object get() {
                return keyOne;
            }

            @Override
            public Class<T> getClzz() {
                return clzz;
            }
        });
    }

    @Override
    public List<T> list() {
        return repository.listByClzz(this.clzz);
    }

    @Override
    public List<T> list(T conditionObj) {
        return repository.list(conditionObj);
    }

    @Override
    public T getOne(T conditionObj) {
        if (conditionObj == null)
            return null;
        return repository.getOne(conditionObj);
    }

    @Override
    public void refreshCache() {
        repository.refreshCache(this.clzz);
    }


    @Override
    public List<T> in(String property, List<? extends Object> inList) {
        InCondition inCondition = InCondition.of(property,inList);
        inCondition.setClz(this.clzz);
        return repository.in(inCondition);
    }


    @Override
    public Page<T> find(Criteria criteria) {
        this.setDefaultClzz(criteria);
        return repository.find(criteria);
    }


    @Override
    public List<T> list(Criteria criteria) {
        this.setDefaultClzz(criteria);
        return repository.list(criteria);

    }

    @Override
    public <T> void findToHandle(Criteria criteria, RowHandler<T> handler) {
        this.setDefaultClzz(criteria);
        this.repository.findToHandle(criteria,handler);
    }


    @Override
    public Page<Map<String, Object>> find(Criteria.ResultMapCriteria resultMapCriteria) {
        this.setDefaultClzz(resultMapCriteria);
        return repository.find(resultMapCriteria);
    }


    @Override
    public List<Map<String, Object>> list(Criteria.ResultMapCriteria resultMapCriteria) {
        this.setDefaultClzz(resultMapCriteria);
        return repository.list(resultMapCriteria);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMapCriteria resultMapCriteria){
        this.setDefaultClzz(resultMapCriteria);
        return repository.listPlainValue(clzz,resultMapCriteria);
    }

    @Override
    public void findToHandle(Criteria.ResultMapCriteria resultMapCriteria, RowHandler<Map<String,Object>> handler) {
        this.setDefaultClzz(resultMapCriteria);
        this.repository.findToHandle(resultMapCriteria,handler);
    }

    private void setDefaultClzz(Criteria.ResultMapCriteria resultMapCriteria) {
        if (this.clzz != Void.class) {
            resultMapCriteria.setClzz(this.clzz);
            resultMapCriteria.setParsed(Parser.get(this.clzz));
        }else{
            resultMapCriteria.setClzz(Void.class);
        }
        resultMapCriteria.setRepositoryClzz(this.repositoryClzz);
    }
    private void setDefaultClzz(Criteria criteria) {
        criteria.setClzz(this.clzz);
        criteria.setParsed(Parser.get(this.clzz));
    }

    protected void setRepositoryClzz(Class clzz){
        this.repositoryClzz = clzz;
    }

}

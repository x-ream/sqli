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
import io.xream.sqli.api.RepositoryX;
import io.xream.sqli.builder.Cond;
import io.xream.sqli.builder.InCondition;
import io.xream.sqli.builder.RefreshCond;
import io.xream.sqli.builder.RemoveRefreshCreate;
import io.xream.sqli.core.*;
import io.xream.sqli.exception.CriteriaSyntaxException;
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
public abstract class DefaultRepositoryX<T> implements BaseRepository<T>, RepositoryX, SafeRefreshBiz<T> {

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

    public DefaultRepositoryX(){
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
    public boolean refresh(RefreshCond refreshCondition) {

        tryToRefreshSafe(this.clzz, refreshCondition);

        return repository.refresh(refreshCondition);
    }

    @Override
    public boolean refreshUnSafe(RefreshCond<T> refreshCondition) {
        refreshCondition.setClz(this.clzz);
        return repository.refresh(refreshCondition);
    }

    @Override
    public boolean removeRefreshCreate(RemoveRefreshCreate<T> removeRefreshCreate){
        return RemoveRefreshCreateBiz.doIt(this.clzz,this.repository,removeRefreshCreate);
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
        if (inList == null)
            throw new IllegalArgumentException("inList can not be null");
        InCondition inCondition = InCondition.of(property,inList);
        inCondition.setClz(this.clzz);
        return repository.in(inCondition);
    }


    @Override
    public Page<T> find(Cond cond) {
        assertCriteriaClzz(cond);
        this.setDefaultClzz(cond);
        Page<T> page = repository.find(cond);
        page.setClzz(this.clzz);
        return page;
    }


    @Override
    public List<T> list(Cond cond) {
        assertCriteriaClzz(cond);
        this.setDefaultClzz(cond);
        return repository.list(cond);

    }

    @Override
    public <T> void findToHandle(Cond cond, RowHandler<T> handler) {
        assertCriteriaClzz(cond);
        this.setDefaultClzz(cond);
        this.repository.findToHandle(cond,handler);
    }

    @Override
    public boolean exists(Cond cond) {
        assertCriteriaClzz(cond);
        this.setDefaultClzz(cond);
        return this.repository.exists(cond);
    }

    @Override
    public Page<Map<String, Object>> find(Cond.X resultMapCriteria) {
        this.setDefaultClzz(resultMapCriteria);
        return repository.find(resultMapCriteria);
    }


    @Override
    public List<Map<String, Object>> list(Cond.X resultMapCriteria) {
        this.setDefaultClzz(resultMapCriteria);
        return repository.list(resultMapCriteria);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Cond.X resultMapCriteria){
        this.setDefaultClzz(resultMapCriteria);
        return repository.listPlainValue(clzz,resultMapCriteria);
    }

    @Override
    public void findToHandle(Cond.X resultMapCriteria, RowHandler<Map<String,Object>> handler) {
        this.setDefaultClzz(resultMapCriteria);
        this.repository.findToHandle(resultMapCriteria,handler);
    }

    private void setDefaultClzz(Cond.X resultMapCriteria) {
        if (this.clzz != Void.class) {
            resultMapCriteria.setParsed(Parser.get(this.clzz));
        }
        resultMapCriteria.setClzz(this.clzz);
        resultMapCriteria.setRepositoryClzz(this.repositoryClzz);
    }
    private void setDefaultClzz(Cond cond) {
        cond.setClzz(this.clzz);
        cond.setParsed(Parser.get(this.clzz));
    }

    private void assertCriteriaClzz(Cond cond) {
        if (this.clzz != cond.getClzz())
            throw new CriteriaSyntaxException("T: " + this.clzz +", Criteria.clzz:" + cond.getClzz());
    }
    //can not delete this method: protected void setRepositoryClzz(Class clzz)
    protected void setRepositoryClzz(Class clzz){
        this.repositoryClzz = clzz;
    }

}

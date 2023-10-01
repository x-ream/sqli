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
import io.xream.sqli.builder.In;
import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.Qr;
import io.xream.sqli.builder.RemoveRefreshCreate;
import io.xream.sqli.core.*;
import io.xream.sqli.exception.PersistenceException;
import io.xream.sqli.exception.QSyntaxException;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.SqliStringUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implement of BaseRepository, RepositoryX
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
    public boolean refresh(Qr<T> qr) {

        if (qr.getRefreshList().isEmpty())
            return true;

        tryToRefreshSafe(this.clzz, qr);

        return repository.refresh(qr);
    }

    @Override
    public boolean refreshUnSafe(Qr<T> qr) {

        if (qr.getRefreshList().isEmpty())
            return true;

        qr.setClz(this.clzz);
        return repository.refresh(qr);
    }

    @Override
    public boolean removeRefreshCreate(RemoveRefreshCreate<T> removeRefreshCreate){
        return RemoveRefreshCreateBiz.doIt(this.clzz,this.repository,removeRefreshCreate);
    }

    @Override
    public boolean remove(String id) {

        if (SqliStringUtil.isNullOrEmpty(id))
            return false;

        return repository.remove(new KeyOne<T>() {

            @Override
            public Object get() {
                return id;
            }

            @Override
            public Class<T> getClzz() {
                return clzz;
            }
        });
    }

    @Override
    public boolean remove(long  id) {

        if (id == 0)
            return false;

        return repository.remove(new KeyOne<T>() {
            @Override
            public Object get() {
                return id;
            }

            @Override
            public Class<T> getClzz() {
                return clzz;
            }
        });
    }

    @Override
    public boolean removeIn(List<? extends Object> idList) {
        if (idList == null || idList.isEmpty())
            return false;

        return this.repository.removeIn(new Keys<T>() {
            @Override
            public List<? extends Object> list() {
                return idList;
            }

            @Override
            public Class getClzz() {
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
        In in = In.of(property,inList);
        in.setClz(this.clzz);
        return repository.in(in);
    }


    @Override
    public Page<T> find(Q q) {
        assertQClzz(q);
        this.setDefaultClzz(q);
        Page<T> page = repository.find(q);
        page.setClzz(this.clzz);
        return page;
    }


    @Override
    public List<T> list(Q q) {
        assertQClzz(q);
        this.setDefaultClzz(q);
        return repository.list(q);

    }

    @Override
    public <T> void findToHandle(Q q, RowHandler<T> handler) {
        assertQClzz(q);
        this.setDefaultClzz(q);
        this.repository.findToHandle(q,handler);
    }

    @Override
    public boolean exists(Q q) {
        assertQClzz(q);
        this.setDefaultClzz(q);
        return this.repository.exists(q);
    }

    @Override
    public Page<Map<String, Object>> findX(Q.X xq) {
        this.setDefaultClzz(xq);
        return repository.find(xq);
    }


    @Override
    public List<Map<String, Object>> listX(Q.X xq) {
        this.setDefaultClzz(xq);
        return repository.list(xq);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Q.X xq){
        this.setDefaultClzz(xq);
        return repository.listPlainValue(clzz,xq);
    }

    @Override
    public void findToHandleX(Q.X xq, RowHandler<Map<String,Object>> handler) {
        this.setDefaultClzz(xq);
        this.repository.findToHandle(xq,handler);
    }

    private void setDefaultClzz(Q.X xq) {
        if (this.clzz != Void.class) {
            xq.setParsed(Parser.get(this.clzz));
        }
        xq.setClzz(this.clzz);
        xq.setRepositoryClzz(this.repositoryClzz);
    }
    private void setDefaultClzz(Q q) {
        q.setClzz(this.clzz);
        q.setParsed(Parser.get(this.clzz));
    }

    private void assertQClzz(Q q) {
        if (this.clzz != q.getClzz())
            throw new QSyntaxException("T: " + this.clzz +", Q.clzz:" + q.getClzz());
    }
    //can not delete this method: protected void setRepositoryClzz(Class clzz)
    protected void setRepositoryClzz(Class clzz){
        this.repositoryClzz = clzz;
    }

}

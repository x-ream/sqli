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
package io.xream.sqli.repository.internal;


import io.xream.sqli.annotation.X;
import io.xream.sqli.api.BaseRepository;
import io.xream.sqli.api.IdGenerator;
import io.xream.sqli.api.RepositoryManagement;
import io.xream.sqli.common.util.SqliStringUtil;
import io.xream.sqli.page.Page;
import io.xream.sqli.core.builder.*;
import io.xream.sqli.core.builder.condition.InCondition;
import io.xream.sqli.core.builder.condition.RefreshCondition;
import io.xream.sqli.core.builder.condition.RemoveRefreshCreate;
import io.xream.sqli.exception.PersistenceException;
import io.xream.sqli.repository.api.KeyOne;
import io.xream.sqli.repository.api.Repository;
import io.xream.sqli.repository.exception.CriteriaSyntaxException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implement BaseRepository
 *
 * @param <T>
 * @author Sim
 */
public abstract class DefaultRepository<T> implements BaseRepository<T> {

    private Class<T> clz;

    @Override
    public Class<T> getClz() {
        return this.clz;
    }

    public void setClz(Class<T> clz) {
        this.clz = clz;
    }


    private IdGenerator idGeneratorService;
    public void setIdGeneratorService(IdGenerator  idGeneratorService){
        this.idGeneratorService = idGeneratorService;
    }

    private Repository repository;
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
        this.clz = (Class) params[0];

        hook();
    }

    protected void hook() {
        if (!RepositoryManagement.REPOSITORY_LIST.contains(this)) {
            RepositoryManagement.REPOSITORY_LIST.add(this);
        }
    }


    @Override
    public long createId() {

        final String clzName = this.clz.getName();

        final long id = this.idGeneratorService.createId(clzName);

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

        refreshCondition.setClz(this.clz);
        Parsed parsed = Parser.get(this.clz);
        Field keyField = parsed.getKeyField(X.KEY_ONE);
        if (Objects.isNull(keyField))
            throw new CriteriaSyntaxException("No PrimaryKey, UnSafe Refresh, try to invoke DefaultRepository.refreshUnSafe(RefreshCondition<T> refreshCondition)");

        boolean unSafe = true;//Safe

        if (unSafe) {
            String key = parsed.getKey(X.KEY_ONE);
            List<io.xream.sqli.core.builder.X> listX = refreshCondition.getListX();
            for (io.xream.sqli.core.builder.X x : listX) {
                String k = x.getKey();
                boolean b = k.contains(".") ? k.endsWith("."+key) : key.equals(k);
                if (b) {
                    Object value = x.getValue();
                    if (Objects.nonNull(value) && !value.toString().equals("0")) {
                        unSafe = false;//Safe
                        break;
                    }
                }
            }
        }

        if (unSafe)
            throw new CriteriaSyntaxException("UnSafe Refresh, try to invoke DefaultRepository.refreshUnSafe(RefreshCondition<T> refreshCondition)");

        return repository.refresh(refreshCondition);
    }

    @Override
    public boolean refreshUnSafe(RefreshCondition<T> refreshCondition) {
        refreshCondition.setClz(this.clz);
        return repository.refresh(refreshCondition);
    }

    @Override
    public boolean removeRefreshCreate(RemoveRefreshCreate<T> wrapper){
        return RemoveRefreshCreateBiz.doIt(this.clz,this.repository,wrapper);
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
                return clz;
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
                return clz;
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
                return clz;
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
                return clz;
            }
        });
    }

    @Override
    public List<T> list() {
        return repository.listByClzz(this.clz);
    }

    @Override
    public List<T> list(T conditionObj) {

        if (conditionObj instanceof CriteriaBuilder || conditionObj instanceof Criteria)
            throw new IllegalArgumentException("list(obj), obj: " + conditionObj);

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
        repository.refreshCache(this.clz);
    }


    @Override
    public List<T> in(InCondition inCondition) {
        inCondition.setClz(this.clz);
        return repository.in(inCondition);
    }


    @Override
    public Page<T> find(Criteria criteria) {

        if (criteria instanceof Criteria.ResultMappedCriteria)
            throw new CriteriaSyntaxException("Codeing Exception: maybe {Criteria.ResultMappedCriteria criteria = builder.get();} instead of {Criteria criteria = builder.get();}");
        criteria.setClz(this.clz);
        criteria.setParsed(Parser.get(this.clz));
        return repository.find(criteria);
    }


    @Override
    public Page<Map<String, Object>> find(Criteria.ResultMappedCriteria resultMapped) {
        resultMapped.setClz(this.clz);
        resultMapped.setParsed(Parser.get(this.clz));
        return repository.find(resultMapped);
    }


    @Override
    public List<Map<String, Object>> list(Criteria.ResultMappedCriteria resultMapped) {
        resultMapped.setClz(this.clz);
        resultMapped.setParsed(Parser.get(this.clz));
        return repository.list(resultMapped);
    }

    @Override
    public <K> List<K> listPlainValue(Class<K> clzz, Criteria.ResultMappedCriteria resultMapped){
        resultMapped.setClz(this.clz);
        resultMapped.setParsed(Parser.get(this.clz));
        return repository.listPlainValue(clzz,resultMapped);
    }

    @Override
    public List<T> list(Criteria criteria) {

        if (criteria instanceof Criteria.ResultMappedCriteria)
            throw new CriteriaSyntaxException("Codeing Exception: mraybe {Criteria.ResultMappedCriteria criteria = builder.get();} instead of {Criteria criteria = builder.get();}");
        criteria.setClz(this.clz);
        criteria.setParsed(Parser.get(this.clz));
        return repository.list(criteria);

    }

    @Override
    public <T> void findToHandle(Criteria criteria, RowHandler<T> handler) {
        criteria.setClz(this.clz);
        criteria.setParsed(Parser.get(this.clz));
        this.repository.findToHandle(criteria,handler);
    }

    @Override
    public void findToHandle(Criteria.ResultMappedCriteria resultMappedCriteria, RowHandler<Map<String,Object>> handler) {
        resultMappedCriteria.setClz(this.clz);
        resultMappedCriteria.setParsed(Parser.get(this.clz));
        this.repository.findToHandle(resultMappedCriteria,handler);
    }

}

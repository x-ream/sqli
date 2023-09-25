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
package io.xream.sqli.builder;

import io.xream.sqli.page.Paged;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Sim
 */
public class Builder<T> extends BbQBuilder {

    private Cond cond;
    private PageBuilder pageBuilder;
    protected SourceScript sourceScriptTemp;

    public Builder<T> routeKey(Object routeKey) {
        this.cond.setRouteKey(routeKey);
        return this;
    }

    public PageBuilder paged() {
        if ( this.pageBuilder != null)
            return this.pageBuilder;
        this.pageBuilder  = new PageBuilder() {

            @Override
            public PageBuilder ignoreTotalRows() {
                cond.setTotalRowsIgnored(true);
                return this;
            }
            
            @Override
            public PageBuilder ignoreTotalRows(boolean ignored) {
                cond.setTotalRowsIgnored(ignored);
                return this;
            }

            @Override
            public PageBuilder rows(int rows) {
                cond.setRows(rows);
                return this;
            }

            @Override
            public PageBuilder page(int page) {
                cond.setPage(page);
                return this;
            }

            @Override
            public PageBuilder last(long last) {
                cond.setLast(last);
                return this;
            }


        };
        return this.pageBuilder;
    }

    public void paged(Paged paged) {
        cond.paged(paged);
    }

    public Builder<T> sortIn(String porperty, List<? extends Object> inList) {
        if (Objects.nonNull(inList) && inList.size() > 0) {
            KV kv = new KV(porperty, inList);
            List<KV> fixedSortList = cond.getFixedSortList();
            if (fixedSortList == null){
                fixedSortList = new ArrayList<>();
                cond.setFixedSortList(fixedSortList);
            }
            fixedSortList.add(kv);
        }
        return this;
    }

    public Builder<T> sort(String orderBy, Direction direction) {
        if (SqliStringUtil.isNullOrEmpty(orderBy))
            return this;
        List<Sort> sortList = cond.getSortList();
        if (sortList == null) {
            sortList = new ArrayList<>();
            cond.setSortList(sortList);
        }
        Sort sort = new Sort(orderBy, direction);
        sortList.add(sort);
        return this;
    }

    private Builder(Cond cond) {
        super(cond.getBbList());
        this.cond = cond;
    }

    public static <T> Builder<T> of(Class<?> clz) {
        Cond cond = new Cond();
        cond.setClzz(clz);
        Builder<T> builder = new Builder(cond);

        if (cond.getParsed() == null) {
            Parsed parsed = Parser.get(clz);
            cond.setParsed(parsed);
        }

        return builder;
    }

    public static X x() {
        Cond.X resultMapCriteria = new Cond.X();
        return new X(resultMapCriteria);
    }

    public Class<?> getClz() {
        return this.cond.getClzz();
    }

    protected Cond get() {
        return this.cond;
    }

    public Cond build(){
        this.cond.setAbort(isAbort);
        return this.cond;
    }

    public void clear(){
        this.cond = null;
    }


    public static final class X extends Builder {

        private SourceScriptBuilder sourceScriptBuilder = new SourceScriptBuilder() {

            @Override
            public SourceScriptBuilder source(String source) {
                sourceScriptTemp.setSource(source);
                return this;
            }

            @Override
            public SourceScriptBuilder source(Class clzz) {
                sourceScriptTemp.setSource(BeanUtil.getByFirstLower(clzz.getSimpleName()));
                return this;
            }

            @Override
            public SourceScriptBuilder sub(Sub sub) {
                X subBuilder = Builder.x();
                sub.buildBy(subBuilder);
                Cond.X resultMapCriteria = subBuilder.build();
                sourceScriptTemp.setSubCriteria(resultMapCriteria);
                subBuilder.clear();
                return this;
            }

            @Override
            public SourceScriptBuilder with(Sub sub){
                sourceScriptTemp.setWith(true);
                return sub(sub);
            }

            @Override
            public SourceScriptBuilder alia(String alia) {
                sourceScriptTemp.setAlia(alia);
                return this;
            }

            @Override
            public SourceScriptBuilder join(JoinType joinType) {
                sourceScriptTemp.setJoinType(joinType);
                return this;
            }

            @Override
            public SourceScriptBuilder join(String joinStr) {
                sourceScriptTemp.setJoinStr(joinStr);
                return this;
            }

            @Override
            public SourceScriptBuilder on(String key, JoinFrom joinFrom) {
                if (key.contains("."))
                    throw new IllegalArgumentException("On key can not contains '.'");
                On on = new On();
                on.setKey(key);
                on.setOp(Op.EQ.sql());
                on.setJoinFrom(joinFrom);
                sourceScriptTemp.setOn(on);
                return this;
            }

            @Override
            public SourceScriptBuilder on(String key, Op op, JoinFrom joinFrom) {
                if (key.contains("."))
                    throw new IllegalArgumentException("On key can not contains '.'");
                On on = new On();
                on.setKey(key);
                on.setOp(op.sql());
                on.setJoinFrom(joinFrom);
                sourceScriptTemp.setOn(on);
                return this;
            }

            @Override
            public BbQBuilder more() {
                List<Bb> bbList = new ArrayList<>();
                sourceScriptTemp.setBbList(bbList);
                return builder(bbList);
            }

            @Override
            public X build() {
                return getInstance();
            }

        };

        private X instance;
        protected X getInstance(){
            return this.instance;
        }

        public SourceScriptBuilder sourceBuilder() {
            sourceScriptTemp = new SourceScript();
            get().getSourceScripts().add(sourceScriptTemp);
            return this.sourceScriptBuilder;
        }

        public X withoutOptimization() {
            get().setWithoutOptimization(true);
            return this;
        }

        @Override
        protected Cond.X get() {
            return (Cond.X) super.get();
        }

        @Override
        public Cond.X build() {
            return (Cond.X) super.build();
        }

        public X(Cond cond) {
            super(cond);
            instance = this;
        }

        public X resultKey(String resultKey) {
            if (SqliStringUtil.isNullOrEmpty(resultKey))
                return this;
            get().getResultKeyList().add(resultKey);
            return this;
        }

        public X resultKeys(String... resultKeys) {
            if (resultKeys == null)
                return this;
            for (String resultKey : resultKeys){
                resultKey(resultKey);
            }
            return this;
        }

        /**
         *
         * @param resultKey
         * @param alia
         * @return resultKey set by framework, not alia, (temporaryRepository.findToCreate)
         *
         */
        public X resultKey(String resultKey, String alia) {
            if (SqliStringUtil.isNullOrEmpty(resultKey))
                return this;
            Objects.requireNonNull(alia,"resultKeyAssignedAlia(), alia can not null");
            get().getResultKeyAssignedAliaList().add(new KV(resultKey,alia));
            return this;
        }

        public X resultWithDottedKey() {
            get().setResultWithDottedKey(true);
            return this;
        }

        /**
         * @param functionScript FUNCTION(?,?)
         * @param values           "test", 1000
         */
        public X resultKeyFunction(ResultKeyAlia resultKeyAlia, String functionScript, String... values) {
            if (SqliStringUtil.isNullOrEmpty(functionScript) || values == null)
                return this;
            Objects.requireNonNull(resultKeyAlia, "function no alia");
            Objects.requireNonNull(resultKeyAlia.getAlia());
            FunctionResultKey functionResultKey = new FunctionResultKey();
            functionResultKey.setScript(functionScript);
            functionResultKey.setAlia(resultKeyAlia.getAlia());
            functionResultKey.setValues(values);
            get().getResultFunctionList().add(functionResultKey);
            return this;
        }

        public X sourceScript(String sourceScript) {
            if (SqliStringUtil.isNullOrEmpty(sourceScript))
                return this;
            sourceScript = normalizeSql(sourceScript);
            get().setSourceScript(sourceScript);
            return this;
        }

        public X sourceScript(String sourceScript, Object...vs) {
            if (SqliStringUtil.isNullOrEmpty(sourceScript))
                return this;
            sourceScript = normalizeSql(sourceScript);
            get().setSourceScript(sourceScript);
            get().setSourceScriptValueList(vs);
            return this;
        }

        public X distinct(String... objs) {
            if (objs == null)
                throw new IllegalArgumentException("distinct non resultKey");
            Cond.X resultMapped = get();
            Distinct distinct = resultMapped.getDistinct();
            if (Objects.isNull(distinct)) {
                distinct = new Distinct();
                resultMapped.setDistinct(distinct);
            }
            for (String obj : objs) {
                distinct.add(obj);
            }
            return this;
        }

        public X groupBy(String property) {
            get().setGroupBy(property);
            return this;
        }

        public X xAggr(String function, Object...values) {
            List<Bb> list = get().getAggrList();
            if (list == null) {
                list = new ArrayList<>();
                get().setAggrList(list);
            }
            Bb bb = new Bb();
            bb.setKey(function);
            bb.setValue(values);
            bb.setP(Op.X_AGGR);
            list.add(bb);
            return this;
        }

        public X having(ResultKeyAlia resultKeyAlia, Op op, Object value) {
            Having having = Having.of(op,value);
            having.setAliaOrFunction(resultKeyAlia.getKey());
            get().getHavingList().add(having);
            return this;
        }

        public X having(String functionScript, Op op, Object value) {
            Having having = Having.of(op,value);
            having.setAliaOrFunction(functionScript);
            get().getHavingList().add(having);
            return this;
        }

        public X reduce(ReduceType type, String property) {
            Reduce reduce = new Reduce();
            reduce.setType(type);
            reduce.setProperty(property);
            get().getReduceList().add(reduce);
            return this;
        }

        @Override
        public X sort(String orderBy, Direction direction){
            return (X) super.sort(orderBy,direction);
        }

        /**
         * @param type
         * @param property
         * @param having   paged().totalRowsIgnored(true), if isTotalRowsIgnored == false，will throw Exception
         */
        public X reduce(ReduceType type, String property, Having having) {
            Reduce reduce = new Reduce();
            reduce.setType(type);
            reduce.setProperty(property);
            reduce.setHaving(having);
            get().getReduceList().add(reduce);
            return this;
        }


        public X eq(String key, Object value) {
            return (X) super.eq(key,value);
        }

        public X gt(String key, Object value) {
            return (X) super.gt(key,value);
        }

        public X gte(String key, Object value) {
            return (X) super.gte(key,value);
        }

        public X lt(String key, Object value) {
            return (X) super.lt(key,value);
        }

        public X lte(String key, Object value) {
            return (X) super.lte(key,value);
        }

        public X ne(String property, Object value) {
            return (X) super.ne(property, value);
        }

        public X like(String property, String value) {
            return (X) super.like(property, value);
        }

        public X likeRight(String property, String value) {
            return (X) super.likeRight(property, value);
        }

        public X notLike(String property, String value) {
            return (X) super.notLike(property, value);
        }

        public X in(String property, List<? extends Object> list) {
            return (X) super.in(property,list);
        }

        public X inRequired(String property, List<? extends Object> list) {
            return (X) super.inRequired(property,list);
        }

        public X nin(String property, List<? extends Object> list) {
            return (X) super.nin(property,list);
        }

        public X nonNull(String property){
            return (X) super.nonNull(property);
        }

        public X isNull(String property){
            return (X) super.isNull(property);
        }

        public X x(String sqlSegment){
            return (X) super.x(sqlSegment);
        }

        public X x(String sqlSegment, Object...values){
            return (X) super.x(sqlSegment, values);
        }

        public X beginSub(){
            return (X) super.beginSub();
        }

        public X endSub(){
            return (X) super.endSub();
        }

        public X bool(Bool conditon, Then then){
            return (X) super.bool(conditon, then);
        }

        public void clear(){
            super.clear();
        }

    }

}

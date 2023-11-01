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

import io.xream.sqli.builder.internal.*;
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
public class QB<T> extends CondBuilder {

    private Q q;
    private PageBuilder pageBuilder;
    protected SourceScript sourceScriptTemp;

    public QB<T> routeKey(Object routeKey) {
        this.q.setRouteKey(routeKey);
        return this;
    }

    public QB<T> paged(Pageable pageable) {
        if ( this.pageBuilder != null) {
            pageable.buildBy(pageBuilder);
            return this;
        }
        this.pageBuilder  = new PageBuilder() {

            @Override
            public PageBuilder ignoreTotalRows() {
                q.setTotalRowsIgnored(true);
                return this;
            }

            @Override
            public PageBuilder ignoreTotalRows(boolean ignored) {
                q.setTotalRowsIgnored(ignored);
                return this;
            }

            @Override
            public PageBuilder rows(int rows) {
                q.setRows(rows);
                return this;
            }

            @Override
            public PageBuilder page(int page) {
                q.setPage(page);
                return this;
            }

            @Override
            public PageBuilder last(long last) {
                q.setLast(last);
                return this;
            }


        };
        pageable.buildBy(pageBuilder);
        return this;
    }

    public PageBuilder paged() {
        if ( this.pageBuilder != null)
            return this.pageBuilder;
        this.pageBuilder  = new PageBuilder() {

            @Override
            public PageBuilder ignoreTotalRows() {
                q.setTotalRowsIgnored(true);
                return this;
            }
            
            @Override
            public PageBuilder ignoreTotalRows(boolean ignored) {
                q.setTotalRowsIgnored(ignored);
                return this;
            }

            @Override
            public PageBuilder rows(int rows) {
                q.setRows(rows);
                return this;
            }

            @Override
            public PageBuilder page(int page) {
                q.setPage(page);
                return this;
            }

            @Override
            public PageBuilder last(long last) {
                q.setLast(last);
                return this;
            }


        };
        return this.pageBuilder;
    }

    public QB<T> paged(Paged paged) {
        q.paged(paged);
        return this;
    }

    public QB<T> sortIn(String porperty, List<? extends Object> inList) {
        if (Objects.nonNull(inList) && inList.size() > 0) {
            KV kv = KV.of(porperty, inList);
            List<KV> fixedSortList = q.getFixedSortList();
            if (fixedSortList == null){
                fixedSortList = new ArrayList<>();
                q.setFixedSortList(fixedSortList);
            }
            fixedSortList.add(kv);
        }
        return this;
    }

    public QB<T> sort(String orderBy, Direction direction) {
        if (SqliStringUtil.isNullOrEmpty(orderBy))
            return this;
        List<Sort> sortList = q.getSortList();
        if (sortList == null) {
            sortList = new ArrayList<>();
            q.setSortList(sortList);
        }
        Sort sort = new Sort(orderBy, direction);
        sortList.add(sort);
        return this;
    }

    private QB(Q q) {
        super(q.getBbs());
        this.q = q;
    }

    public static <T> QB<T> of(Class<T> clz) {
        Q q = new Q();
        q.setClzz(clz);
        QB<T> QB = new QB(q);

        if (q.getParsed() == null) {
            Parsed parsed = Parser.get(clz);
            q.setParsed(parsed);
        }

        return QB;
    }

    public static X x() {
        Q.X xq = new Q.X();
        return new X(xq);
    }

    public Class<?> getClz() {
        return this.q.getClzz();
    }

    protected Q<T> get() {
        return this.q;
    }

    public Q<T> build(){
        this.q.setAbort(isAbort);
        return this.q;
    }



    public QB eq(String key, Object value) {
        return (QB) super.eq(key,value);
    }

    public QB gt(String key, Object value) {
        return (QB) super.gt(key,value);
    }

    public QB gte(String key, Object value) {
        return (QB) super.gte(key,value);
    }

    public QB lt(String key, Object value) {
        return (QB) super.lt(key,value);
    }

    public QB lte(String key, Object value) {
        return (QB) super.lte(key,value);
    }

    public QB ne(String property, Object value) {
        return (QB) super.ne(property, value);
    }

    public QB like(String property, String value) {
        return (QB) super.like(property, value);
    }

    public QB likeRight(String property, String value) {
        return (QB) super.likeRight(property, value);
    }

    public QB notLike(String property, String value) {
        return (QB) super.notLike(property, value);
    }

    public QB in(String property, List list) {
        return (QB) super.in(property,list);
    }

    public QB inRequired(String property, List list) {
        return (QB) super.inRequired(property,list);
    }

    public QB nin(String property, List list) {
        return (QB) super.nin(property,list);
    }

    public QB nonNull(String property){
        return (QB) super.nonNull(property);
    }

    public QB isNull(String property){
        return (QB) super.isNull(property);
    }

    public QB x(String sqlSegment){
        return (QB) super.x(sqlSegment);
    }

    public QB x(String sqlSegment, Object...values){
        return (QB) super.x(sqlSegment, values);
    }

    public QB and(SubCond sub){
        return (QB) super.and(sub);
    }

    public QB or(SubCond sub){
        return (QB) super.or(sub);
    }

    public QB beginSub(){
        return (QB) super.beginSub();
    }

    public QB endSub(){
        return (QB) super.endSub();
    }

    public QB bool(Bool cond, Then then){
        return (QB) super.bool(cond, then);
    }

    public QB or(){
        return (QB) super.or();
    }


    public void clear(){
        this.q = null;
    }


    public static final class X extends QB {

        private SourceBuilder sourceBuilder = new SourceBuilder() {

            @Override
            public SourceBuilder source(Class clz) {
                sourceScriptTemp.setSource(BeanUtil.getByFirstLower(clz.getSimpleName()));
                return this;
            }

            @Override
            public SourceBuilder sub(Sub sub) {
                X subBuilder = QB.x();
                sub.buildBy(subBuilder);
                Q.X xq = subBuilder.build();
                sourceScriptTemp.setSubQ(xq);
                subBuilder.clear();
                return this;
            }

            @Override
            public SourceBuilder with(Sub sub){
                sourceScriptTemp.setWith(true);
                return sub(sub);
            }

            @Override
            public SourceBuilder alia(String alia) {
                sourceScriptTemp.setAlia(alia);
                return this;
            }

            @Override
            public SourceBuilder join(JoinType joinType,Class clz) {
                Join join = join();
                join.setJoin(joinType);
                return this;
            }

            @Override
            public SourceBuilder join(String joinStr,Class clz) {
                Join join = join();
                join.setJoin(joinStr);
                return this;
            }



            @Override
            public CondBuilder on(String onSql) {

                Bb bb = new Bb();
                bb.setC(Op.NONE);
                bb.setP(Op.X);
                bb.setKey(onSql);

                On on = new On();
                Join join = join();
                join.setOn(on);
                on.setBuilder(new CondBuilder(on.getBbs()));
                on.getBbs().add(bb);
                return on.getBuilder();
            }

            private Join join(){
                Join join = sourceScriptTemp.getJoin();
                if (join == null) {
                    join = new Join();
                    sourceScriptTemp.setJoin(join);
                }
                return join;
            }


        };

        private X instance;

//        public SourceBuilder sourceBuilder() {
//            sourceScriptTemp = new SourceScript();
//            get().getSourceScripts().add(sourceScriptTemp);
//            return this.sourceBuilder;
//        }

        public X sourceX(SourceX x) {
            sourceScriptTemp = new SourceScript();
            get().getSourceScripts().add(sourceScriptTemp);
            x.buildBy(sourceBuilder);
            return this;
        }

        public X withoutOptimization() {
            get().setWithoutOptimization(true);
            return this;
        }

        @Override
        protected Q.X get() {
            return (Q.X) super.get();
        }

        @Override
        public Q.X build() {
            return (Q.X) super.build();
        }

        private X(Q q) {
            super(q);
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
            get().getResultKeyAssignedAliaList().add(KV.of(resultKey,alia));
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
            Q.X xq = get();
            Distinct distinct = xq.getDistinct();
            if (Objects.isNull(distinct)) {
                distinct = new Distinct();
                xq.setDistinct(distinct);
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

        public X in(String property, List list) {
            return (X) super.in(property,list);
        }

        public X inRequired(String property, List list) {
            return (X) super.inRequired(property,list);
        }

        public X nin(String property, List list) {
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

        public X and(SubCond sub){
            return (X) super.and(sub);
        }

        public X or(SubCond sub){
            return (X) super.or(sub);
        }

        public X beginSub(){
            return (X) super.beginSub();
        }

        public X endSub(){
            return (X) super.endSub();
        }

        public X bool(Bool cond, Then then){
            return (X) super.bool(cond, then);
        }

        public X or(){
            return (X) super.or();
        }


        public void clear(){
            super.clear();
        }

        public X paged(Pageable pageable) {

            super.paged(pageable);

            return this;
        }

        public X paged(Paged paged) {

            super.paged(paged);

            return this;
        }

    }

}

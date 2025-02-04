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
    protected Froms fromsTemp;

    public QB<T> routeKey(Object routeKey) {
        this.q.setRouteKey(routeKey);
        return this;
    }

    public QB<T> paged(Pageable pageable) {
        if (this.pageBuilder != null) {
            pageable.buildBy(pageBuilder);
            return this;
        }
        this.pageBuilder = new PageBuilder() {

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

    public QB<T> sortIn(String porperty, List<? extends Object> inList) {
        if (Objects.nonNull(inList) && inList.size() > 0) {
            KV kv = KV.of(porperty, inList);
            List<KV> fixedSortList = q.getFixedSortList();
            if (fixedSortList == null) {
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

    @Override
    public Q<T> build() {
        this.q.setAbort(isAbort);
        return this.q;
    }

    @Override
    public QB<T> eq(String property, Object value) {
        return (QB<T>) super.eq(property, value);
    }

    @Override
    public QB<T> gt(String property, Object value) {
        return (QB<T>) super.gt(property, value);
    }

    @Override
    public QB<T> gte(String property, Object value) {
        return (QB<T>) super.gte(property, value);
    }

    @Override
    public QB<T> lt(String property, Object value) {
        return (QB<T>) super.lt(property, value);
    }

    @Override
    public QB<T> lte(String property, Object value) {
        return (QB<T>) super.lte(property, value);
    }

    @Override
    public QB<T> ne(String property, Object value) {
        return (QB<T>) super.ne(property, value);
    }

    @Override
    public QB<T> like(String property, String value) {
        return (QB<T>) super.like(property, value);
    }

    @Override
    public QB<T> likeLeft(String property, String value) {
        return (QB<T>) super.likeLeft(property, value);
    }

    @Override
    public QB<T> notLike(String property, String value) {
        return (QB<T>) super.notLike(property, value);
    }

    @Override
    public QB<T> in(String property, List list) {
        return (QB<T>) super.in(property, list);
    }

    @Override
    public QB<T> inRequired(String property, List list) {
        return (QB<T>) super.inRequired(property, list);
    }

    @Override
    public QB<T> nin(String property, List list) {
        return (QB<T>) super.nin(property, list);
    }

    @Override
    public QB<T> nonNull(String property) {
        return (QB<T>) super.nonNull(property);
    }

    @Override
    public QB<T> isNull(String property) {
        return (QB<T>) super.isNull(property);
    }

    public QB<T> x(String sqlSegment) {
        return (QB<T>) super.x(sqlSegment);
    }

    @Override
    public QB<T> x(String sqlSegment, Object... values) {
        return (QB<T>) super.x(sqlSegment, values);
    }

    @Override
    public QB<T> and(SubCond sub) {
        return (QB<T>) super.and(sub);
    }

    @Override
    public QB<T> or(SubCond sub) {
        return (QB<T>) super.or(sub);
    }

    @Override
    public QB<T> bool(Bool cond, Then then) {
        return (QB<T>) super.bool(cond, then);
    }

    @Override
    public QB<T> any(Any any) {
        return (QB<T>)super.any(any);
    }

    @Override
    public QB<T> or() {
        return (QB<T>) super.or();
    }

    public QB<T> last(String lastSqlSegment) {
        this.q.setLastSqlSegment(lastSqlSegment);
        return this;
    }


    public void clear() {
        this.q = null;
    }


    public static final class X extends QB {

        private FromBuilder fb = new FromBuilder() {

            @Override
            public FromBuilder of(Class clz) {
                return of(clz, null);
            }

            @Override
            public FromBuilder of(Class clz, String alia) {
                if (get().getSourceScripts().isEmpty()) {
                    sourceScript();
                }
                fromsTemp.setAlia(alia);
                fromsTemp.setSource(BeanUtil.getByFirstLower(clz.getSimpleName()));
                return this;
            }

            @Override
            public FromBuilder sub(Sub sub, String alia) {
                sourceScript();
                fromsTemp.setAlia(alia);

                X subBuilder = QB.x();
                sub.buildBy(subBuilder);
                Q.X xq = subBuilder.build();
                fromsTemp.setSubQ(xq);
                subBuilder.clear();
                return this;
            }

            @Override
            public FromBuilder with(Sub sub, String alia) {
                sub(sub, alia);
                fromsTemp.setWith(true);
                return this;
            }

            @Override
            public FromBuilder JOIN(JoinType joinType) {
                sourceScript();
                JOIN join = JOIN();
                join.setJoin(joinType);
                return this;
            }

            @Override
            public FromBuilder JOIN(String joinStr) {
                sourceScript();
                JOIN join = JOIN();
                join.setJoin(joinStr);
                return this;
            }

            @Override
            public FromBuilder on(String onSql) {

                return on(onSql, null);
            }

            @Override
            public FromBuilder on(String onSql, On on) {

                JOIN join = JOIN();
                ON onE = join.getOn();
                if (onE == null) {

                    onE = new ON();
                    join.setOn(onE);
                    CondBuilder cb = new CondBuilder(onE.getBbs());
                    onE.setBuilder(cb);
                    onE.getBbs().add(Bb.of(Op.NONE,Op.X,onSql,null));

                    if (on != null) {
                        on.buildBy(cb);
                    }
                }else {
                    onE.getBbs().add(Bb.of(Op.AND,Op.X,onSql,null));
                }


                return this;
            }

            private JOIN JOIN() {
                JOIN join = fromsTemp.getJoin();
                if (join == null) {
                    join = new JOIN();
                    fromsTemp.setJoin(join);
                }
                return join;
            }
        };

        private X instance;


        private void sourceScript() {
            fromsTemp = new Froms();
            get().getSourceScripts().add(fromsTemp);
        }

        public X from(Class clz) {
            get().setSourceScript(BeanUtil.getByFirstLower(clz.getSimpleName()));
            return this;
        }

        public X fromX(FromX x) {
            x.buildBy(fb);
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

        private X select0(String resultKey) {
            if (SqliStringUtil.isNullOrEmpty(resultKey))
                return this;
            get().getResultKeyList().add(resultKey);
            return this;
        }


        public X select(String... resultKeys) {
            if (resultKeys == null)
                return this;
            for (String resultKey : resultKeys) {
                select0(resultKey);
            }
            return this;
        }

        /**
         * @param resultKey
         * @param alia
         * @return resultKey set by framework, not alia, (temporaryRepository.findToCreate)
         */
        public X selectWithAlia(String resultKey, String alia) {
            if (SqliStringUtil.isNullOrEmpty(resultKey))
                return this;
            Objects.requireNonNull(alia, "resultKeyAssignedAlia(), alia can not null");
            get().getResultKeyAssignedAliaList().add(KV.of(resultKey, alia));
            return this;
        }

        public X resultWithDottedKey() {
            get().setResultWithDottedKey(true);
            return this;
        }

        /**
         * @param functionScript FUNCTION(?,?)
         * @param values         "test", 1000
         */
        public X selectWithFunc(ResultKeyAlia resultKeyAlia, String functionScript, String... values) {
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

        public X from(String fromSql, Object... vs) {
            if (SqliStringUtil.isNullOrEmpty(fromSql))
                return this;
            fromSql = normalizeSql(fromSql);
            get().setSourceScript(fromSql);
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

        public X xAggr(String function, Object... values) {
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

        public X having(Having having) {
            CondBuilder cb = new CondBuilder(get().getHavingList());
            having.buildBy(cb);
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
        public X sort(String orderBy, Direction direction) {
            return (X) super.sort(orderBy, direction);
        }

        /**
         * @param type
         * @param property
         * @param havingBb   paged().totalRowsIgnored(true), if isTotalRowsIgnored == false，will throw Exception
         */
        public X reduce(ReduceType type, String property, Bb havingBb) {
            Reduce reduce = new Reduce();
            reduce.setType(type);
            reduce.setProperty(property);
            reduce.setHaving(havingBb);
            get().getReduceList().add(reduce);
            return this;
        }


        @Override
        public X eq(String property, Object value) {
            return (X) super.eq(property, value);
        }

        @Override
        public X gt(String property, Object value) {
            return (X) super.gt(property, value);
        }

        @Override
        public X gte(String property, Object value) {
            return (X) super.gte(property, value);
        }

        @Override
        public X lt(String property, Object value) {
            return (X) super.lt(property, value);
        }

        @Override
        public X lte(String property, Object value) {
            return (X) super.lte(property, value);
        }

        @Override
        public X ne(String property, Object value) {
            return (X) super.ne(property, value);
        }

        @Override
        public X like(String property, String value) {
            return (X) super.like(property, value);
        }

        @Override
        public X likeLeft(String property, String value) {
            return (X) super.likeLeft(property, value);
        }

        @Override
        public X notLike(String property, String value) {
            return (X) super.notLike(property, value);
        }

        @Override
        public X in(String property, List list) {
            return (X) super.in(property, list);
        }

        @Override
        public X inRequired(String property, List list) {
            return (X) super.inRequired(property, list);
        }

        @Override
        public X nin(String property, List list) {
            return (X) super.nin(property, list);
        }

        @Override
        public X nonNull(String property) {
            return (X) super.nonNull(property);
        }

        @Override
        public X isNull(String property) {
            return (X) super.isNull(property);
        }

        @Override
        public X x(String sqlSegment) {
            return (X) super.x(sqlSegment);
        }

        @Override
        public X x(String sqlSegment, Object... values) {
            return (X) super.x(sqlSegment, values);
        }

        @Override
        public X and(SubCond sub) {
            return (X) super.and(sub);
        }

        @Override
        public X or(SubCond sub) {
            return (X) super.or(sub);
        }

        @Override
        public X bool(Bool cond, Then then) {
            return (X) super.bool(cond, then);
        }

        @Override
        public X any(Any any) {
            return (X) super.any(any);
        }


        @Override
        public X or() {
            return (X) super.or();
        }

        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public X paged(Pageable pageable) {

            super.paged(pageable);

            return this;
        }

        @Override
        public X last(String lastSqlSegment) {
            super.last(lastSqlSegment);
            return this;
        }

    }

}

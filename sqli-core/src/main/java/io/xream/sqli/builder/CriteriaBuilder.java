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
package io.xream.sqli.builder;

import io.xream.sqli.page.Direction;
import io.xream.sqli.page.Paged;
import io.xream.sqli.page.Sort;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.SqliStringUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Sim
 */
public class CriteriaBuilder extends ConditionCriteriaBuilder {

    private Criteria criteria;
    protected SourceScript sourceScriptTemp;

    public CriteriaBuilder routeKey(Object routeKey) {
        this.criteria.setRouteKey(routeKey);
        return this;
    }

    public PageBuilder paged() {
        return this.pageBuilder;
    }

    public void paged(Paged paged) {
        criteria.paged(paged);
    }

    public CriteriaBuilder forceIndex(String indexName) {
        if (SqliStringUtil.isNullOrEmpty(indexName))
            return this;
        this.criteria.setForceIndex(indexName);
        return this;
    }

    private PageBuilder pageBuilder = new PageBuilder() {

        @Override
        public PageBuilder ignoreTotalRows() {
            criteria.setTotalRowsIgnored(true);
            return this;
        }

        @Override
        public PageBuilder rows(int rows) {
            criteria.setRows(rows);
            return this;
        }

        @Override
        public PageBuilder page(int page) {
            criteria.setPage(page);
            return this;
        }

        @Override
        public PageBuilder orderIn(String porperty, List<? extends Object> inList) {
            if (Objects.nonNull(inList) && inList.size() > 0) {
                KV kv = new KV(porperty, inList);
                criteria.getFixedSortList().add(kv);
            }
            return this;
        }

        @Override
        public PageBuilder sort(String orderBy, Direction direction) {
            if (SqliStringUtil.isNullOrEmpty(orderBy))
                return this;
            List<Sort> sortList = criteria.getSortList();
            if (sortList == null) {
                sortList = new ArrayList<>();
                criteria.setSortList(sortList);
            }
            Sort sort = new Sort(orderBy, direction);
            sortList.add(sort);
            return this;
        }
    };


    private CriteriaBuilder(Criteria criteria) {
        super(criteria.getBuildingBlockList());
        this.criteria = criteria;
    }

    public static CriteriaBuilder builder(Class<?> clz) {
        Criteria criteria = new Criteria();
        criteria.setClz(clz);
        CriteriaBuilder builder = new CriteriaBuilder(criteria);

        if (criteria.getParsed() == null) {
            Parsed parsed = Parser.get(clz);
            criteria.setParsed(parsed);
        }

        return builder;
    }

    public static ResultMapBuilder resultMapBuilder() {
        Criteria.ResultMapCriteria resultMapCriteria = new Criteria.ResultMapCriteria();
        return new ResultMapBuilder(resultMapCriteria);
    }

    public Class<?> getClz() {
        return this.criteria.getClz();
    }

    protected Criteria get() {
        return this.criteria;
    }

    public Criteria build(){
        return this.criteria;
    }


    public static class ResultMapBuilder extends CriteriaBuilder {

        private SourceScriptBuilder sourceScriptBuilder = new SourceScriptBuilder() {

            @Override
            public SourceScriptBuilder source(String source) {
                sourceScriptTemp.setSource(source);
                return this;
            }

            @Override
            public SourceScriptBuilder alia(String alia) {
                sourceScriptTemp.setAlia(alia);
                return this;
            }

            @Override
            public SourceScriptBuilder joinType(JoinType joinType) {
                sourceScriptTemp.setJoinType(joinType);
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
            public ConditionCriteriaBuilder more() {
                return build(sourceScriptTemp.getBuildingBlockList());
            }

        };

        public SourceScriptBuilder sourceScript() {
            sourceScriptTemp = new SourceScript();
            get().getSourceScripts().add(sourceScriptTemp);
            return this.sourceScriptBuilder;
        }

        public ResultMapBuilder withoutOptimization() {
            get().setWithoutOptimization(true);
            return this;
        }

        @Override
        protected Criteria.ResultMapCriteria get() {
            return (Criteria.ResultMapCriteria) super.get();
        }

        @Override
        public Criteria.ResultMapCriteria build() {
            return (Criteria.ResultMapCriteria) super.get();
        }

        private void init(Class<?> clz) {
            Criteria.ResultMapCriteria f = (Criteria.ResultMapCriteria) super.criteria;
            f.setClz(clz);
            Parsed parsed = Parser.get(clz);
            f.setParsed(parsed);
        }

        public ResultMapBuilder(Criteria criteria) {
            super(criteria);
        }

        public ResultMapBuilder resultKey(String resultKey) {
            if (SqliStringUtil.isNullOrEmpty(resultKey))
                return this;
            get().getResultKeyList().add(resultKey);
            return this;
        }

        /**
         *
         * @param resultKey
         * @param alia
         * @return resultKey set by framework, not alia, (temporaryRepository.findToCreate)
         *
         */
        public ResultMapBuilder resultKey(String resultKey, String alia) {
            if (SqliStringUtil.isNullOrEmpty(resultKey))
                return this;
Objects.requireNonNull(alia,"resultKeyAssignedAlia(), alia can not null");
            get().getResultKeyAssignedAliaList().add(new KV(resultKey,alia));
            return this;
        }

        public ResultMapBuilder resultWithDottedKey() {
            get().setResultWithDottedKey(true);
            return this;
        }

        /**
         * @param functionScript FUNCTION(?,?)
         * @param keys           test.createAt, test.endAt
         */
        public ResultMapBuilder resultKeyFunction(ResultKeyAlia functionAlia_wrap, String functionScript, String... keys) {
            if (SqliStringUtil.isNullOrEmpty(functionScript) || keys == null)
                return this;
            Objects.requireNonNull(functionAlia_wrap, "function no alia");
            Objects.requireNonNull(functionAlia_wrap.getAlia());
            FunctionResultKey functionResultKey = new FunctionResultKey();
            functionResultKey.setScript(functionScript);
            functionResultKey.setAlia(functionAlia_wrap.getAlia());
            functionResultKey.setKeys(keys);
            get().getResultFuntionList().add(functionResultKey);
            return this;
        }

        @Override
        public void paged(Paged paged) {
            super.criteria.paged(paged);
        }

        public ResultMapBuilder sourceScript(String sourceScript) {
            if (SqliStringUtil.isNullOrEmpty(sourceScript))
                return this;
            get().setSourceScript(sourceScript);
            return this;
        }

        public ResultMapBuilder distinct(String... objs) {
            if (objs == null)
                throw new IllegalArgumentException("distinct non resultKey");
            Criteria.ResultMapCriteria resultMapped = get();
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

        public ResultMapBuilder groupBy(String property) {
            get().setGroupBy(property);
            return this;
        }

        public ResultMapBuilder reduce(ReduceType type, String property) {
            Reduce reduce = new Reduce();
            reduce.setType(type);
            reduce.setProperty(property);
            get().getReduceList().add(reduce);
            return this;
        }

        /**
         * @param type
         * @param property
         * @param having   paged().totalRowsIgnored(true), if isTotalRowsIgnored == false，will throw Exception
         */
        public ResultMapBuilder reduce(ReduceType type, String property, Having having) {
            Reduce reduce = new Reduce();
            reduce.setType(type);
            reduce.setProperty(property);
            reduce.setHaving(having);
            get().getReduceList().add(reduce);
            return this;
        }

    }

}
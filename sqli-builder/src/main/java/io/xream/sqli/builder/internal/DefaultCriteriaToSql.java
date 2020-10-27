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
package io.xream.sqli.builder.internal;

import io.xream.sqli.builder.*;
import io.xream.sqli.exception.CriteriaSyntaxException;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.exception.SqlBuildException;
import io.xream.sqli.filter.BaseTypeFilter;
import io.xream.sqli.mapping.Mappable;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.support.ResultMapSingleSourceSupport;
import io.xream.sqli.support.TimestampSupport;
import io.xream.sqli.util.JsonWrapper;
import io.xream.sqli.util.SqliStringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Sim
 */
public final class DefaultCriteriaToSql implements CriteriaToSql, ResultKeyGenerator, SourceScriptOptimizable, ResultMapSingleSourceSupport {

    private static CriteriaToSql instance;
    private DefaultCriteriaToSql(){}

    public static CriteriaToSql newInstance(){
        if(instance == null){
            instance = new DefaultCriteriaToSql();
            return instance;
        }
        return null;
    }

    @Override
    public String toSql(CriteriaCondition criteriaCondition,List<Object> valueList, Mappable mappable) {
        if (Objects.isNull(criteriaCondition))
            return "";
        StringBuilder sb = new StringBuilder();
        List<Bb> bbList = criteriaCondition.getBbList();

        if (bbList.isEmpty())
            return "";

        filter(bbList, mappable);//过滤
        if (bbList.isEmpty())
            return "";

        pre(valueList, bbList);

        bbList.get(0).setC(Op.WHERE);

        buildConditionSql(sb, bbList, mappable);

        return sb.toString();
    }

    @Override
    public void toSql(boolean isSub, Criteria criteria, SqlBuilt sqlBuilt, SqlBuildingAttached sqlBuildingAttached) {

        SqlSth sqlSth = SqlSth.get();

        parseAlia(criteria, sqlSth);

        filter0(criteria);
        /*
         * select column
         */
        select(sqlSth, resultKey(sqlSth,criteria,sqlBuildingAttached));
        /*
         * force index
         */
        forceIndex(isSub, sqlSth, criteria);

        sourceScriptPre(criteria, sqlBuildingAttached);
        /*
         * StringList
         */
        condition(sqlSth, criteria.getBbList(), criteria, sqlBuildingAttached.getValueList());

        count(isSub, criteria.isTotalRowsIgnored(), sqlSth);
        /*
         * group by
         */
        groupBy(sqlSth, criteria);

        having(sqlSth, criteria);
        /*
         * sort
         */
        sort(sqlSth, criteria);
        /*
         * from table
         */
        sourceScript(sqlSth, criteria);

        sqlArr(isSub, criteria.isTotalRowsIgnored(),sqlBuilt, sqlBuildingAttached, sqlSth);

    }

    private String sourceScriptOfRefresh(Parsed parsed, RefreshCondition refreshCondition) {
        String sourceScript = refreshCondition.getSourceScript();
        if (SqliStringUtil.isNullOrEmpty(sourceScript))
            return parsed.getTableName();

        parseAliaFromRefresh(refreshCondition);

        final String str = normalizeSql(sourceScript);

        StringBuilder sb = new StringBuilder();
        mapping(reg -> str.split(reg), refreshCondition, sb);

        return sb.toString();
    }

    @Override
    public String toSql(Parsed parsed, RefreshCondition refreshCondition, DialectSupport dialectSupport) {

        String sourceScript = sourceScriptOfRefresh(parsed, refreshCondition);

        StringBuilder sb = new StringBuilder();
        sb.append(dialectSupport.getAlterTableUpdate()).append(SqlScript.SPACE).append(sourceScript)
                .append(SqlScript.SPACE).append(dialectSupport.getCommandUpdate()).append(SqlScript.SPACE);

        concatRefresh(sb, parsed, refreshCondition,dialectSupport);

        String conditionSql = toSql(refreshCondition, refreshCondition.getValueList(), refreshCondition);

        sb.append(conditionSql);

        if (SqliStringUtil.isNotNull(dialectSupport.getLimitOne()) && refreshCondition.getLimit() > 0){
            sb.append(SqlScript.LIMIT).append(refreshCondition.getLimit());
        }

        String sql = sb.toString();

        if (sql.contains("SET  WHERE"))
            throw new SqlBuildException(sql);

        return sql;
    }

    private void concatRefresh(StringBuilder sb, Parsed parsed, RefreshCondition refreshCondition, DialectSupport dialectSupport) {

        List<Bb> refreshList = refreshCondition.getRefreshList();

        List<Object> refreshValueList = new ArrayList<>();

        boolean isNotFirst = false;
        for (Bb bb : refreshList) {

            if (bb.getP() == Op.X) {

                if (isNotFirst) {
                    sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                }

                isNotFirst = true;

                Object key = bb.getKey();
                String str = key.toString();
                final String sql = normalizeSql(str);
                mapping((reg) -> sql.split(reg), refreshCondition, sb);

            } else {
                String key = bb.getKey();
                if (key.contains("?")) {

                    if (isNotFirst) {
                        sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                    }

                    isNotFirst = true;
                    final String sql = normalizeSql(key);
                    mapping((reg) -> sql.split(reg), refreshCondition, sb);
                } else {

                    String k = null;
                    Parsed p;
                    if (key.contains(".")) {
                        String[] arr = key.split("\\.");
                        p = Parser.get(arr[0]);
                        if (p == null)
                            throw new ParsingException("can not find the clzz: " + arr[0]);
                        k = arr[1];
                    }else{
                        k = key;
                        p = parsed;
                    }

                    BeanElement be = p.getElement(k);
                    if (be == null) {
                        throw new ParsingException("can not find the property " + key + " of " + parsed.getClzName());
                    }

                    TimestampSupport.testNumberValueToDate(be.getClz(), bb);

                    if (SqliStringUtil.isNullOrEmpty(String.valueOf(bb.getValue()))
                            || BaseTypeFilter.isBaseType(key, bb.getValue(), parsed)) {
                        continue;
                    }

                    if (isNotFirst) {
                        sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                    }

                    isNotFirst = true;

                    String mapper = mapping(key, refreshCondition);
                    sb.append(mapper);
                    sb.append(SqlScript.EQ_PLACE_HOLDER);

                    if (be.isJson()) {
                        Object v = bb.getValue();
                        if (v != null) {
                            String str = JsonWrapper.toJson(v);
                            Object jsonStr = dialectSupport.convertJsonToPersist(str);
                            bb.setValue(jsonStr);
                        }
                    }
                }

                refreshValueList.add(bb.getValue());
            }

        }

        if (!refreshValueList.isEmpty()) {
            refreshCondition.getValueList().addAll(0, refreshValueList);
        }
    }

    private void sqlArr(boolean isSub, boolean isTotalRowsIgnored, SqlBuilt sqlBuilt, SqlBuildingAttached sqlBuildingAttached, SqlSth sb) {

        if (! isSub){
            for (SqlBuilt sub : sqlBuildingAttached.getSubList()){
                int start = sb.sbSource.indexOf(SqlScript.SUB);
                sb.sbSource.replace(start, start + SqlScript.SUB.length(),
                        SqlScript.LEFT_PARENTTHESIS + sub.getSql().toString() + SqlScript.RIGHT_PARENTTHESIS
                );
            }

            if (! isTotalRowsIgnored) {
                StringBuilder sqlSb = new StringBuilder();
                sqlSb.append(SqlScript.SELECT).append(SqlScript.SPACE).append(sb.countSql).append(SqlScript.SPACE)
                .append(sb.sbSource).append(sb.countCondition);
                sqlBuilt.setCountSql(sqlSb.toString());
            }
        }

        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append(sb.sbResult).append(sb.sbSource).append(sb.sbCondition);

        sqlBuilt.setSql(sqlSb);
    }


    private String resultKey(SqlSth sqlSth, Criteria criteria, SqlBuildingAttached sqlBuildingAttached) {
        if (!(criteria instanceof Criteria.ResultMapCriteria))
            return SqlScript.STAR;

        boolean flag = false;

        Criteria.ResultMapCriteria resultMapped = (Criteria.ResultMapCriteria) criteria;
        StringBuilder columnBuilder = new StringBuilder();

        Map<String,String> mapperPropertyMap = resultMapped.getMapperPropertyMap();

        if (Objects.nonNull(resultMapped.getDistinct())) {

            columnBuilder.append(SqlScript.DISTINCT);
            List<String> list = resultMapped.getDistinct().getList();
            int size = list.size();
            int i = 0;
            StringBuilder distinctColumn = new StringBuilder();
            distinctColumn.append(columnBuilder);
            for (String resultKey : list) {
                addConditonBeforeOptimization(resultKey, sqlSth.conditionSet);
                String mapper = mapping(resultKey, resultMapped);
                mapperPropertyMap.put(mapper, resultKey);//REDUCE ALIAN NAME
                distinctColumn.append(SqlScript.SPACE).append(mapper);
                mapper = generate(mapper, resultMapped);
                columnBuilder.append(SqlScript.SPACE).append(mapper);
                i++;
                if (i < size) {
                    columnBuilder.append(SqlScript.COMMA);
                    distinctColumn.append(SqlScript.COMMA);
                }
            }
            sqlSth.countSql = "COUNT(" + distinctColumn.toString() + ") count";
            flag = true;
        }

        List<Reduce> reduceList = resultMapped.getReduceList();

        if (!reduceList.isEmpty()) {

            for (Reduce reduce : reduceList) {
                if (flag) {
                    columnBuilder.append(SqlScript.COMMA);
                }
                addConditonBeforeOptimization(reduce.getProperty(), sqlSth.conditionSet);
                String alianProperty = reduce.getProperty() + SqlScript.UNDER_LINE + reduce.getType().toString().toLowerCase();//property_count
                String alianName = alianProperty.replace(SqlScript.DOT, SqlScript.DOLLOR);
                resultMapped.getResultKeyAliaMap().put(alianName, alianProperty);

                String value = mapping(reduce.getProperty(), criteria);

                ReduceType reduceType = reduce.getType();
                if (reduceType == ReduceType.GROUP_CONCAT_DISTINCT) {
                    reduceType = ReduceType.GROUP_CONCAT;
                    value = "DISTINCT " + value;
                } else if (reduceType == ReduceType.SUM_DISTINCT) {
                    reduceType = ReduceType.SUM;
                    value = "DISTINCT " + value;
                } else if (reduceType == ReduceType.COUNT_DISTINCT) {
                    reduceType = ReduceType.COUNT;
                    value = "DISTINCT " + value;
                } else if (reduceType == ReduceType.AVG_DISTINCT) {
                    reduceType = ReduceType.AVG;
                    value = "DISTINCT " + value;
                }

                columnBuilder.append(SqlScript.SPACE)
                        .append(reduceType)
                        .append(SqlScript.LEFT_PARENTTHESIS)//" ( "
                        .append(value)
                        .append(SqlScript.RIGHT_PARENTTHESIS).append(SqlScript.SPACE)//" ) "
                        .append(SqlScript.AS).append(SqlScript.SPACE).append(alianName);

                Having h = reduce.getHaving();
                if (h != null) {
                    h.setAliaOrFunction(alianName);
                    resultMapped.getHavingList().add(h);
                }
                flag = true;
            }
        }

        List<String> resultList = resultMapped.getResultKeyList();
        if (!resultList.isEmpty()) {
            if (flag) {
                columnBuilder.append(SqlScript.COMMA);
            }
            int size = resultList.size();
            for (int i = 0; i < size; i++) {
                String resultKey = resultList.get(i);
                addConditonBeforeOptimization(resultKey, sqlSth.conditionSet);
                String mapper = mapping(resultKey, criteria);
                mapperPropertyMap.put(mapper, resultKey);
                mapper = generate(mapper, resultMapped);
                columnBuilder.append(SqlScript.SPACE).append(mapper);
                if (i < size - 1) {
                    columnBuilder.append(SqlScript.COMMA);
                }
                flag = true;
            }

        }

        List<KV> resultListAssignedAliaList = resultMapped.getResultKeyAssignedAliaList();
        if (!resultListAssignedAliaList.isEmpty()) {
            if (flag) {
                columnBuilder.append(SqlScript.COMMA);
            }
            int size = resultListAssignedAliaList.size();
            for (int i = 0; i < size; i++) {
                KV kv = resultListAssignedAliaList.get(i);
                String key = kv.getK();
                addConditonBeforeOptimization(key, sqlSth.conditionSet);
                String mapper = mapping(key, criteria);
                mapperPropertyMap.put(mapper, key);
                String alian = kv.getV().toString();
                resultMapped.getResultKeyAliaMap().put(alian, mapper);
                columnBuilder.append(SqlScript.SPACE).append(mapper).append(SqlScript.AS).append(alian);
                if (i < size - 1) {
                    columnBuilder.append(SqlScript.COMMA);
                }
                flag = true;
            }

        }

        List<FunctionResultKey> functionList = resultMapped.getResultFunctionList();
        if (!functionList.isEmpty()) {//
            if (flag) {
                columnBuilder.append(SqlScript.COMMA);
            }

            Map<String, String> resultKeyAliaMap = resultMapped.getResultKeyAliaMap();

            int size = functionList.size();
            for (int i = 0; i < size; i++) {
                FunctionResultKey functionResultKey = functionList.get(i);

                String function = functionResultKey.getScript();

                columnBuilder.append(SqlScript.SPACE);
                final String functionStr = normalizeSql(function);
                List<String> originList = mapping((reg) -> functionStr.split(reg), criteria, columnBuilder);
                for (String origin : originList){
                    addConditonBeforeOptimization(origin, sqlSth.conditionSet);
                }

                for (Object obj : functionResultKey.getValues()) {
                    sqlBuildingAttached.getValueList().add(obj);
                }

                String aliaKey = functionResultKey.getAlia();
                String alian = aliaKey.replace(".","_");
                resultKeyAliaMap.put(aliaKey, alian);
                mapperPropertyMap.put(alian, aliaKey);
                columnBuilder.append(SqlScript.AS).append(alian);
                if (i < size - 1) {
                    columnBuilder.append(SqlScript.COMMA);
                }
            }
        }

        String script = columnBuilder.toString();
        if (SqliStringUtil.isNullOrEmpty(script)) {
            throw new CriteriaSyntaxException("Suggest API: find(Criteria criteria), no any resultKey for ResultMapCriteria");
        }

        return script;

    }

    private void select(SqlSth sqlSth, String resultKeys) {
        sqlSth.sbResult.append(SqlScript.SELECT).append(SqlScript.SPACE).append(resultKeys).append(SqlScript.SPACE);
    }

    private void groupBy(SqlSth sb, Criteria criteria) {
        if (criteria instanceof Criteria.ResultMapCriteria) {
            Criteria.ResultMapCriteria rm = (Criteria.ResultMapCriteria) criteria;

            String groupByS = rm.getGroupBy();
            if (SqliStringUtil.isNullOrEmpty(groupByS))
                return;

            sb.sbCondition.append(Op.GROUP_BY.sql());

            String[] arr = groupByS.split(SqlScript.COMMA);

            int i = 0;
            int l = arr.length;
            for (String groupBy : arr) {
                String groupByStr = groupBy.trim();
                if (SqliStringUtil.isNotNull(groupBy)) {
                    if (groupBy.contains(SqlScript.LEFT_PARENTTHESIS)){
                        final String groupByStrFinal = normalizeSql(groupByStr);
                        List<String> originList = mapping((reg) -> groupByStrFinal.split(reg), criteria, sb.sbCondition);
                        for (String origin : originList){
                            addConditonBeforeOptimization(origin,sb.conditionSet);
                        }
                    }else {
                        String mapper = mapping(groupByStr, rm);
                        addConditonBeforeOptimization(groupByStr,sb.conditionSet);
                        sb.sbCondition.append(mapper);
                    }
                    i++;
                    if (i < l) {
                        sb.sbCondition.append(SqlScript.COMMA);
                    }
                }
            }
        }
    }

    private void having(SqlSth sb, Criteria criteria) {
        if (!(criteria instanceof Criteria.ResultMapCriteria))
            return;

        Criteria.ResultMapCriteria resultMapped = (Criteria.ResultMapCriteria) criteria;
        List<Having> havingList = resultMapped.getHavingList();

        if (havingList.isEmpty())
            return;

        if (!criteria.isTotalRowsIgnored()) {
            throw new CriteriaSyntaxException("Reduce with having not support totalRows query, try to builder.paged().ignoreTotalRows()");
        }

        boolean flag = true;
        for (Having h : havingList) {
            if (h == null)
                continue;
            if (flag) {
                sb.sbCondition.append(Op.HAVING.sql());
                flag = false;
            } else {
                sb.sbCondition.append(Op.AND.sql());
            }

            String alia = h.getAliaOrFunction();
            if (alia.contains(SqlScript.LEFT_PARENTTHESIS)) {
                alia = normalizeSql(alia);
                final String finalKey = alia;
                List<String> originList = mapping((reg) -> finalKey.split(reg), criteria, sb.sbCondition);
                for (String origin : originList){
                    addConditonBeforeOptimization(origin,sb.conditionSet);
                }
            }else {
                sb.sbCondition.append(alia);
                addConditonBeforeOptimization(alia,sb.conditionSet);
            }
            sb.sbCondition.append(SqlScript.SPACE).append(h.getOp().sql()).append(SqlScript.SPACE).append(h.getValue());
        }
    }

    private void parseAliaFromRefresh(RefreshCondition refreshCondition) {

        String script = refreshCondition.getSourceScript();//string -> list<>
        List<String> list = SourceScriptBuilder.split(script);
        List<SourceScript> sourceScripts = SourceScriptBuilder.parse(list);
        SourceScriptBuilder.checkAlia(sourceScripts);
        for (SourceScript sc : sourceScripts) {
            refreshCondition.getAliaMap().put(sc.alia(), sc.getSource());
        }

    }


    private void parseAlia(Criteria criteria, SqlSth sqlSth) {

        if (criteria instanceof Criteria.ResultMapCriteria) {
            Criteria.ResultMapCriteria rmc = (Criteria.ResultMapCriteria) criteria;

            if (rmc.getSourceScripts().isEmpty()) {// builderSource null
                String sourceScript = rmc.sourceScript();//string -> list<>

                List<String> list = SourceScriptBuilder.split(sourceScript);
                List<SourceScript> sourceScripts = SourceScriptBuilder.parse(list);
                rmc.getSourceScripts().addAll(sourceScripts);
            }

            SourceScriptBuilder.checkAlia(rmc.getSourceScripts());
            supportSingleSource(rmc);

            Map<String, String> aliaMap = rmc.getAliaMap();
            for (SourceScript sc : rmc.getSourceScripts()) {
                if (SqliStringUtil.isNotNull(sc.getSource())) {
                    aliaMap.put(sc.alia(), sc.getSource());
                }
            }

            for (SourceScript sourceScript : rmc.getSourceScripts()) {
                addConditionBeforeOptimization(sourceScript.getBbList(), sqlSth.conditionSet);
            }
        }

    }

    private void sourceScript(SqlSth sb, Criteria criteria) {

        sb.sbSource.append(SqlScript.SPACE);

        String script = null;
        if (criteria instanceof Criteria.ResultMapCriteria) {
            Criteria.ResultMapCriteria rmc = (Criteria.ResultMapCriteria) criteria;

            if (rmc.getSourceScripts().isEmpty()) {// builderSource null
                String str = criteria.sourceScript();
                Objects.requireNonNull(str,"Not set sourceScript of ResultMappedBuilder");
                final String strd = normalizeSql(str);
                StringBuilder sbs = new StringBuilder();
                mapping((reg) -> strd.split(reg), rmc, sbs);
                script = sbs.toString();
            } else {
                if (!rmc.isWithoutOptimization()) {
                    optimizeSourceScript(sb.conditionSet, rmc.getSourceScripts());//FIXME  + ON AND
                }
                script = rmc.getSourceScripts().stream()
                        .map(sourceScript -> sourceScript.sql(rmc))
                        .collect(Collectors.joining()).trim();
            }

            sb.sbSource.append(SqlScript.FROM).append(SqlScript.SPACE);

        } else {
            script = mapping(criteria.sourceScript(),criteria);
            if (!script.startsWith(SqlScript.FROM) || !script.startsWith(SqlScript.FROM.toLowerCase()))
                sb.sbSource.append(SqlScript.FROM).append(SqlScript.SPACE);
        }
        sb.sbSource.append(script);

    }

    private void forceIndex(boolean isSub, SqlSth sqlSth, Criteria criteria) {
        if (isSub)
            return;
        if (SqliStringUtil.isNullOrEmpty(criteria.getForceIndex()))
            return;
        sqlSth.sbCondition.append(" FORCE INDEX(" + criteria.getForceIndex() + ")");
        addConditonBeforeOptimization(criteria.getForceIndex(), sqlSth.conditionSet);
    }

    private void count(boolean isSub,  boolean isTotalRowsIgnored,  SqlSth sqlSth) {

        if (isSub || isTotalRowsIgnored)
            return;
            sqlSth.countCondition = new StringBuilder();
            sqlSth.countCondition.append(sqlSth.sbCondition);
    }

    private void sort(SqlSth sb, Criteria criteria) {

        if (criteria.isFixedSort())
            return;

        List<Sort> sortList = criteria.getSortList();
        if (sortList != null && !sortList.isEmpty()) {

            sb.sbCondition.append(Op.ORDER_BY.sql());
            int size = sortList.size();
            int i = 0;
            for (Sort sort : sortList) {
                String orderBy = sort.getOrderBy();
                orderBy = normalizeSql(orderBy);
                orderBy = noSpace(orderBy);
                String mapper = mapping(orderBy, criteria);
                sb.sbCondition.append(mapper).append(SqlScript.SPACE);
                addConditonBeforeOptimization(orderBy,sb.conditionSet);
                Direction direction = sort.getDirection();
                if (direction == null) {
                    sb.sbCondition.append(Direction.DESC);
                } else {
                    sb.sbCondition.append(direction);
                }
                i++;
                if (i < size) {
                    sb.sbCondition.append(SqlScript.COMMA).append(SqlScript.SPACE);
                }
            }
        }

    }

    private void filter0(Criteria criteria) {
        List<Bb> bbList = criteria.getBbList();

        if (criteria instanceof Criteria.ResultMapCriteria) {
            Criteria.ResultMapCriteria resultMapCriteria = (Criteria.ResultMapCriteria)criteria;//FIXME 判断是虚表
            filter(bbList, resultMapCriteria);
            for (SourceScript sourceScript : ((Criteria.ResultMapCriteria) criteria).getSourceScripts()) {
                List<Bb> bbs = sourceScript.getBbList();
                if (bbs == null || bbs.isEmpty())
                    continue;
                filter(bbs, resultMapCriteria);
            }
        }else{
            filter(bbList,criteria);
        }
    }

    private void sourceScriptPre(Criteria criteria, SqlBuildingAttached attached) {
        if (criteria instanceof Criteria.ResultMapCriteria) {
            for (SourceScript sourceScript : ((Criteria.ResultMapCriteria) criteria).getSourceScripts()) {
                sourceScript.pre(attached, this);
            }
        }
    }

    private void condition(SqlSth sqlSth, List<Bb> bbList, Criteria criteria, List<Object> valueList) {
        if (bbList.isEmpty())
            return;
        addConditionBeforeOptimization(bbList, sqlSth.conditionSet);//优化连表查询前的准备

        StringBuilder xsb = new StringBuilder();

        pre(valueList, bbList);//提取占位符对应的值
        if (bbList.isEmpty())
            return;
        bbList.get(0).setC(Op.WHERE);
        buildConditionSql(xsb, bbList,criteria);
        sqlSth.sbCondition.append(xsb);

    }


    public static final class SqlSth {

        private StringBuilder sbResult = new StringBuilder();
        private StringBuilder sbSource = new StringBuilder();
        private StringBuilder sbCondition = new StringBuilder();
        private Set<String> conditionSet = new HashSet<>();
        private String countSql = "COUNT(*) count";
        private StringBuilder countCondition;

        public static SqlSth get() {
            return new SqlSth();
        }
    }


}

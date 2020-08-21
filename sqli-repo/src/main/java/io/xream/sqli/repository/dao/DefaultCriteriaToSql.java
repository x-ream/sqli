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
package io.xream.sqli.repository.dao;

import io.xream.sqli.api.Dialect;
import io.xream.sqli.common.util.BeanUtil;
import io.xream.sqli.common.util.JsonWrapper;
import io.xream.sqli.common.util.SqliStringUtil;
import io.xream.sqli.page.Direction;
import io.xream.sqli.page.Sort;
import io.xream.sqli.core.builder.*;
import io.xream.sqli.core.builder.condition.RefreshCondition;
import io.xream.sqli.core.filter.BaseTypeFilter;
import io.xream.sqli.core.support.TimestampSupport;
import io.xream.sqli.repository.api.CriteriaToSql;
import io.xream.sqli.repository.exception.CriteriaSyntaxException;
import io.xream.sqli.repository.exception.SqlBuildException;
import io.xream.sqli.repository.util.SqlParserUtil;
import io.xream.sqli.util.BeanUtilX;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Sim
 */
public class DefaultCriteriaToSql implements CriteriaToSql, ConditionCriteriaToSql, ConditionCriteriaToSql.Filter, ConditionCriteriaToSql.Pre {

    private Dialect dialect;

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    private void mapping(String script, CriteriaCondition criteria, StringBuilder sb) {
        String[] keyArr = script.split(SqlScript.SPACE);//
        int length = keyArr.length;
        for (int i = 0; i < length; i++) {
            String origin = keyArr[i].trim();

            String target = mapping(origin, criteria);
            sb.append(target).append(SqlScript.SPACE);
        }
    }


    @Override
    public String fromCondition(CriteriaCondition criteriaCondition) {
        if (Objects.isNull(criteriaCondition))
            return "";
        StringBuilder sb = new StringBuilder();
        List<X> xList = criteriaCondition.getListX();

        if (xList.isEmpty())
            return "";

        filter(xList, criteriaCondition);//过滤
        if (xList.isEmpty())
            return "";

        pre(criteriaCondition.getValueList(), xList);

        xList.get(0).setConjunction(ConjunctionAndOtherScript.WHERE);

        buildConditionSql(sb, xList);

        String script = sb.toString();
        StringBuilder sbb = new StringBuilder();
        mapping(script, criteriaCondition, sbb);
        return sbb.toString();
    }

    @Override
    public SqlParsed from(Criteria criteria) {

        SqlBuilder sqlBuilder = SqlBuilder.get();

        parseAlia(criteria, sqlBuilder);

        filter0(criteria);

        env(criteria);

        resultKey(sqlBuilder, criteria);
        /*
         * select column
         */
        select(sqlBuilder, criteria);
        /*
         * force index
         */
        forceIndex(sqlBuilder, criteria);

        sourceScriptValueList(criteria);
        /*
         * StringList
         */
        condition(sqlBuilder, criteria.getListX(), criteria);

        SqlBuilder countSql = count(sqlBuilder.sbCondition, criteria);
        /*
         * group by
         */
        groupBy(sqlBuilder, criteria);

        having(sqlBuilder, criteria);
        /*
         * sort
         */
        sort(sqlBuilder, criteria);
        /*
         * from table
         */
        sourceScript(sqlBuilder, criteria);

        return sqlArr(sqlBuilder, criteria, countSql);
    }

    private String sourceScriptOfRefresh(Parsed parsed, RefreshCondition refreshCondition) {
        String sourceScript = refreshCondition.getSourceScript();
        if (SqliStringUtil.isNullOrEmpty(sourceScript))
            return parsed.getTableName();

        parseAliaFromRefresh(refreshCondition);

        sourceScript = BeanUtilX.normalizeSql(sourceScript);

        StringBuilder sb = new StringBuilder();
        mapping(sourceScript, refreshCondition, sb);

        return sb.toString();
    }

    @Override
    public String fromRefresh(Parsed parsed, RefreshCondition refreshCondition) {

        String sourceScript = sourceScriptOfRefresh(parsed, refreshCondition);

        StringBuilder sb = new StringBuilder();
        sb.append(SqlScript.UPDATE).append(SqlScript.SPACE).append(sourceScript).append(SqlScript.SPACE);

        concatRefresh(sb, parsed, refreshCondition);

        filter(refreshCondition.getListX(), refreshCondition);

        String conditionSql = fromCondition(refreshCondition);

        conditionSql = SqlParserUtil.mapper(conditionSql, parsed);

        sb.append(conditionSql);

        String sql = sb.toString();

        if (sql.contains("SET  WHERE"))
            throw new SqlBuildException(sql);

        return sql;
    }

    private void concatRefresh(StringBuilder sb, Parsed parsed, RefreshCondition refreshCondition) {

        sb.append(SqlScript.SET);

        List<X> refreshList = refreshCondition.getRefreshList();

        List<Object> refreshValueList = new ArrayList<>();

        boolean isNotFirst = false;
        for (X x : refreshList) {

            if (x.getPredicate() == PredicateAndOtherScript.X) {

                if (isNotFirst) {
                    sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                }

                isNotFirst = true;

                Object key = x.getKey();
                String str = key.toString();
                String sql = BeanUtilX.normalizeSql(str);
                mapping(sql, refreshCondition, sb);

            } else {
                String key = x.getKey();
                if (key.contains("?")) {

                    if (isNotFirst) {
                        sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                    }

                    isNotFirst = true;
                    String sql = BeanUtilX.normalizeSql(key);

                    mapping(sql, refreshCondition, sb);
                } else {

                    String k = null;
                    Parsed p;
                    if (key.contains(".")) {
                        String[] arr = key.split("\\.");
                        p = Parser.get(arr[0]);
                        if (p == null)
                            throw new RuntimeException("can not find the clzz: " + arr[0]);
                        k = arr[1];
                    }else{
                        k = key;
                        p = parsed;
                    }

                    BeanElement be = p.getElementMap().get(k);
                    if (be == null) {
                        throw new RuntimeException("can not find the property " + key + " of " + parsed.getClzName());
                    }

                    TimestampSupport.testNumberValueToDate(be.clz, x);

                    if (SqliStringUtil.isNullOrEmpty(String.valueOf(x.getValue())) || BaseTypeFilter.isBaseType_0(key, x.getValue(), parsed)) {
                        continue;
                    }

                    if (isNotFirst) {
                        sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                    }

                    isNotFirst = true;

                    String mapper = mapping(key,refreshCondition);
                    sb.append(mapper);
                    sb.append(SqlScript.EQ_PLACE_HOLDER);

                    if (BeanUtil.testEnumConstant(be.clz, x.getValue())) {
                    } else if (be.isJson) {
                        Object v = x.getValue();
                        if (v != null) {
                            String str = JsonWrapper.toJson(v);
                            x.setValue(str);
                        }
                    }
                }

                refreshValueList.add(x.getValue());
            }

        }

        if (!refreshValueList.isEmpty()) {
            refreshCondition.getValueList().addAll(0, refreshValueList);
        }
    }

    private SqlParsed sqlArr(SqlBuilder sb, Criteria criteria, SqlBuilder countSb) {

        SqlParsed sqlParsed = new SqlParsed();

        if (countSb != null) {
            StringBuilder sqlSb = new StringBuilder();
            sqlSb.append(countSb.sbResult).append(sb.sbSource).append(countSb.sbCondition);
            sqlParsed.setCountSql(sqlSb.toString());
        }

        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append(sb.sbResult).append(sb.sbSource).append(sb.sbCondition);

        sqlParsed.setSql(sqlSb);

        return sqlParsed;
    }


    private void env(Criteria criteria) {
        if (criteria instanceof Criteria.ResultMappedCriteria) {
            Criteria.ResultMappedCriteria resultMapped = (Criteria.ResultMappedCriteria) criteria;
            PropertyMapping propertyMapping = resultMapped.getPropertyMapping();//
            if (Objects.isNull(propertyMapping)) {
                propertyMapping = new PropertyMapping();
                resultMapped.setPropertyMapping(propertyMapping);
            }
        }
        criteria.getValueList().clear();
    }

    private void resultKey(SqlBuilder sqlBuilder, Criteria criteria) {
        if (!(criteria instanceof Criteria.ResultMappedCriteria))
            return;

        boolean flag = false;

        Criteria.ResultMappedCriteria resultMapped = (Criteria.ResultMappedCriteria) criteria;
        StringBuilder column = new StringBuilder();

        PropertyMapping propertyMapping = resultMapped.getPropertyMapping();

        if (Objects.nonNull(resultMapped.getDistinct())) {

//            if (!flag) resultMapped.getResultKeyList().clear();//去掉构造方法里设置的返回key

            column.append(SqlScript.DISTINCT);
            List<String> list = resultMapped.getDistinct().getList();
            int size = list.size();
            int i = 0;
            StringBuilder distinctColumn = new StringBuilder();
            distinctColumn.append(column);
            for (String resultKey : list) {
                sqlBuilder.conditionSet.add(resultKey);
                String mapper = mapping(resultKey, criteria);
                propertyMapping.put(resultKey, mapper);//REDUCE ALIAN NAME
                distinctColumn.append(SqlScript.SPACE).append(mapper);
                mapper = this.dialect.resultKeyAlian(mapper, resultMapped);
                column.append(SqlScript.SPACE).append(mapper);
                i++;
                if (i < size) {
                    column.append(SqlScript.COMMA);
                    distinctColumn.append(SqlScript.COMMA);
                }
            }
            criteria.setCountDistinct("COUNT(" + distinctColumn.toString() + ") count");
            flag = true;
        }

        List<Reduce> reduceList = resultMapped.getReduceList();

        if (!reduceList.isEmpty()) {

            for (Reduce reduce : reduceList) {
                if (flag) {
                    column.append(SqlScript.COMMA);
                }
                sqlBuilder.conditionSet.add(reduce.getProperty());
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

                column.append(SqlScript.SPACE)
                        .append(reduceType)
                        .append(SqlScript.LEFT_PARENTTHESIS)//" ( "
                        .append(value)
                        .append(SqlScript.RIGHT_PARENTTHESIS).append(SqlScript.SPACE)//" ) "
                        .append(SqlScript.AS).append(SqlScript.SPACE).append(alianName);

                Having h = reduce.getHaving();
                if (h != null) {
                    h.setKey(alianName);
                    if (!criteria.isTotalRowsIgnored()) {
                        throw new CriteriaSyntaxException("Reduce with having not support totalRows query, try to builder.paged().ignoreTotalRows()");
                    }
                }
                flag = true;
            }
        }

        List<String> resultList = resultMapped.getResultKeyList();
        if (!resultList.isEmpty()) {
            if (flag) {
                column.append(SqlScript.COMMA);
            }
            int size = resultList.size();
            for (int i = 0; i < size; i++) {
                String key = resultList.get(i);
                sqlBuilder.conditionSet.add(key);
                String mapper = mapping(key, criteria);
                propertyMapping.put(key, mapper);
                mapper = this.dialect.resultKeyAlian(mapper, resultMapped);
                column.append(SqlScript.SPACE).append(mapper);
                if (i < size - 1) {
                    column.append(SqlScript.COMMA);
                }
                flag = true;
            }

        }


        List<KV> resultListAssignedAliaList = resultMapped.getResultKeyAssignedAliaList();
        if (!resultListAssignedAliaList.isEmpty()) {
            if (flag) {
                column.append(SqlScript.COMMA);
            }
            int size = resultListAssignedAliaList.size();
            for (int i = 0; i < size; i++) {
                KV kv = resultListAssignedAliaList.get(i);
                sqlBuilder.conditionSet.add(kv.getK());
                String mapper = mapping(kv.getK(), criteria);
                propertyMapping.put(kv.getK(), mapper);
                String alian = kv.getV().toString();
                resultMapped.getResultKeyAliaMap().put(alian, mapper);
                column.append(SqlScript.SPACE).append(mapper).append(SqlScript.AS).append(alian);
                if (i < size - 1) {
                    column.append(SqlScript.COMMA);
                }
                flag = true;
            }

        }

        List<FunctionResultKey> functionList = resultMapped.getResultFuntionList();
        if (!functionList.isEmpty()) {//
            if (flag) {
                column.append(SqlScript.COMMA);
            }

            Map<String, String> resultKeyAliaMap = resultMapped.getResultKeyAliaMap();

            int size = functionList.size();
            for (int i = 0; i < size; i++) {
                FunctionResultKey functionResultKey = functionList.get(i);

                String function = functionResultKey.getScript();

                for (String key : functionResultKey.getKeys()) {
                    sqlBuilder.conditionSet.add(key);
                    String mapper = mapping(key, criteria);
                    function = function.replaceFirst("\\?", mapper);
                }
                String aliaKey = functionResultKey.getAlia();
                String alian = aliaKey.replace(".","_");
                resultKeyAliaMap.put(aliaKey, alian);
                propertyMapping.put(aliaKey, alian);
                column.append(SqlScript.SPACE).append(function).append(SqlScript.AS).append(alian);
                if (i < size - 1) {
                    column.append(SqlScript.COMMA);
                }
            }
        }

        String script = column.toString();
        if (SqliStringUtil.isNullOrEmpty(script)) {
            throw new CriteriaSyntaxException("Suggest API: find(Criteria criteria), no any resultKey for ResultMappedCriteria");
        }
        criteria.setCustomedResultKey(column.toString());

        ((Criteria.ResultMappedCriteria) criteria).adpterResultScript();
    }

    private void select(SqlBuilder sb, Criteria criteria) {
        sb.sbResult.append(SqlScript.SELECT).append(SqlScript.SPACE).append(criteria.resultAllScript()).append(SqlScript.SPACE);
    }

    private void groupBy(SqlBuilder sb, Criteria criteria) {
        if (criteria instanceof Criteria.ResultMappedCriteria) {
            Criteria.ResultMappedCriteria rm = (Criteria.ResultMappedCriteria) criteria;

            String groupByS = rm.getGroupBy();
            if (SqliStringUtil.isNullOrEmpty(groupByS))
                return;

            sb.sbCondition.append(ConjunctionAndOtherScript.GROUP_BY.sql());

            String[] arr = groupByS.split(SqlScript.COMMA);

            int i = 0, l = arr.length;
            for (String groupBy : arr) {
                groupBy = groupBy.trim();
                sb.conditionSet.add(groupBy);
                if (SqliStringUtil.isNotNull(groupBy)) {
                    String mapper = mapping(groupBy, criteria);
                    sb.sbCondition.append(mapper);
                    i++;
                    if (i < l) {
                        sb.sbCondition.append(SqlScript.COMMA);
                    }
                }
            }
        }
    }

    private void having(SqlBuilder sb, Criteria criteria) {
        if (!(criteria instanceof Criteria.ResultMappedCriteria))
            return;

        Criteria.ResultMappedCriteria resultMapped = (Criteria.ResultMappedCriteria) criteria;
        List<Reduce> reduceList = resultMapped.getReduceList();

        if (reduceList.isEmpty())
            return;
        boolean flag = true;
        for (Reduce reduce : reduceList) {
            Having h = reduce.getHaving();
            if (h == null)
                continue;
            if (flag) {
                sb.sbCondition.append(ConjunctionAndOtherScript.HAVING.sql());
                flag = false;
            } else {
                sb.sbCondition.append(ConjunctionAndOtherScript.AND.sql());
            }
            sb.sbCondition.append(h.getKey()).append(h.getOp().sql()).append(h.getValue());
        }
    }


    private void parseAliaFromRefresh(RefreshCondition refreshCondition) {

        String script = refreshCondition.getSourceScript();//string -> list<>
        List<String> list = SourceScriptBuilder.split(script);
        List<SourceScript> sourceScripts = SourceScriptBuilder.parse(list);

        for (SourceScript sc : sourceScripts) {
            refreshCondition.getAliaMap().put(sc.alia(), sc.getSource());
        }

    }


    private void parseAlia(Criteria criteria, SqlBuilder sqlBuilder) {

        if (criteria instanceof Criteria.ResultMappedCriteria) {
            Criteria.ResultMappedCriteria rmc = (Criteria.ResultMappedCriteria) criteria;

            if (rmc.getSourceScripts().isEmpty()) {// builderSource null
                String script = criteria.sourceScript();//string -> list<>
                List<String> list = SourceScriptBuilder.split(script);
                List<SourceScript> sourceScripts = SourceScriptBuilder.parse(list);
                rmc.getSourceScripts().addAll(sourceScripts);
            }

            Map<String, String> aliaMap = new HashMap<>();
            for (SourceScript sc : rmc.getSourceScripts()) {
                aliaMap.put(sc.alia(), sc.getSource());
            }

            rmc.setAliaMap(aliaMap);

            for (SourceScript sourceScript : rmc.getSourceScripts()) {
                preOptimizeListX(sourceScript.getListX(), sqlBuilder.conditionSet);
            }
        }

    }

    private void preOptimizeListX(List<X> xList, Set<String> conditionSet) {
        for (X x : xList) {
            conditionSet.add(x.getKey());
            List<X> subList = x.getSubList();
            if (subList != null && !subList.isEmpty()) {
                preOptimizeListX(subList, conditionSet);
            }
        }
    }

    private void optimizeSourceScript(List<SourceScript> sourceScripts, Set<String> conditionSet) {
        if (sourceScripts.size() <= 1)
            return;
        if (conditionSet.size() > 0) {
            for (String test : conditionSet) {
                if (test != null) {
                    if (test.contains("."))
                        break;
                    return;
                }
            }
        }
        for (SourceScript sourceScript : sourceScripts) {
            for (String key : conditionSet) {
                if (key == null)
                    continue;
                if (SqliStringUtil.isNullOrEmpty(sourceScript.getAlia())) {
                    if (key.contains(sourceScript.getSource() + ".")) {
                        sourceScript.used();
                        break;
                    }
                } else {
                    if (key.contains(sourceScript.getAlia() + ".")) {
                        sourceScript.used();
                        break;
                    }
                }
            }
        }

        int size = sourceScripts.size();
        for (int i = size - 1; i >= 0; i--) {
            SourceScript sourceScript = sourceScripts.get(i);
            if (!sourceScript.isUsed() && !sourceScript.isTargeted())
                continue;
            for (int j = i - 1; j >= 0; j--) {
                SourceScript sc = sourceScripts.get(j);
                if (sourceScript.getSource().equals(sc.getSource()))
                    continue;
                //FIXME
                On on = sourceScript.getOn();
                if (on == null || on.getJoinFrom() == null)
                    continue;
                if (sc.alia().equals(on.getJoinFrom().getAlia())) {
                    sc.targeted();
                    break;
                }
            }
        }

        Iterator<SourceScript> ite = sourceScripts.iterator();
        while (ite.hasNext()) {
            SourceScript sourceScript = ite.next();
            if (!sourceScript.isUsed() && !sourceScript.isTargeted())
                ite.remove();
        }
    }

    private void sourceScript(SqlBuilder sb, Criteria criteria) {

        sb.sbSource.append(SqlScript.SPACE);

        String script = null;
        if (criteria instanceof Criteria.ResultMappedCriteria) {
            Criteria.ResultMappedCriteria rmc = (Criteria.ResultMappedCriteria) criteria;

            if (rmc.getSourceScripts().isEmpty()) {// builderSource null
                script = criteria.sourceScript();
            } else {
                if (!rmc.isWithoutOptimization()) {
                    if (!rmc.resultAllScript().trim().equals("*")) {
                        optimizeSourceScript(rmc.getSourceScripts(), sb.conditionSet);//FIXME  + ON AND
                    }
                }
                script = rmc.getSourceScripts().stream().map(SourceScript::sql).collect(Collectors.joining()).trim();
            }

//            Assert.notNull(script, "Not set sourceScript of ResultMappedBuilder");
            sb.sbSource.append(SqlScript.FROM).append(SqlScript.SPACE);

        } else {
            script = criteria.sourceScript();
            if (!script.startsWith(SqlScript.FROM) || !script.startsWith(SqlScript.FROM.toLowerCase()))
                sb.sbSource.append(SqlScript.FROM).append(SqlScript.SPACE);
        }


        mapping(script, criteria, sb.sbSource);
    }

    private void forceIndex(SqlBuilder sqlBuilder, Criteria criteria) {
        if (SqliStringUtil.isNullOrEmpty(criteria.getForceIndex()))
            return;
        sqlBuilder.sbCondition.append("FORCE INDEX(" + criteria.getForceIndex() + ")");
        sqlBuilder.conditionSet.add(criteria.getForceIndex());
    }

    private SqlBuilder count(StringBuilder sbCondition, Criteria criteria) {
        if (!criteria.isTotalRowsIgnored()) {
            SqlBuilder sqlBuilder = SqlBuilder.get();
            sqlBuilder.sbResult.append(SqlScript.SELECT).append(SqlScript.SPACE).append(criteria.getCountDistinct()).append(SqlScript.SPACE);
            sqlBuilder.sbCondition.append(sbCondition.toString());
            return sqlBuilder;
        }
        return null;
    }

    private void sort(SqlBuilder sb, Criteria criteria) {

        if (criteria.isFixedSort())
            return;

        List<Sort> sortList = criteria.getSortList();
        if (sortList != null && !sortList.isEmpty()) {

            sb.sbCondition.append(ConjunctionAndOtherScript.ORDER_BY.sql());
            int size = sortList.size();
            int i = 0;
            for (Sort sort : sortList) {
                String orderBy = sort.getOrderBy();
                String mapper = mapping(orderBy, criteria);
                sb.sbCondition.append(mapper).append(SqlScript.SPACE);
                sb.conditionSet.add(orderBy);
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
        List<X> xList = criteria.getListX();
        filter(xList, criteria);
        if (criteria instanceof Criteria.ResultMappedCriteria) {

            for (SourceScript sourceScript : ((Criteria.ResultMappedCriteria) criteria).getSourceScripts()) {
                filter(sourceScript.getListX(), criteria);
            }
        }
    }

    private void sourceScriptValueList(Criteria criteria) {
        if (criteria instanceof Criteria.ResultMappedCriteria) {
            List<Object> valueList = criteria.getValueList();
            for (SourceScript sourceScript : ((Criteria.ResultMappedCriteria) criteria).getSourceScripts()) {
                sourceScript.pre(valueList);
            }
        }
    }

    private void condition(SqlBuilder sqlBuilder, List<X> xList, Criteria criteria) {
        if (xList.isEmpty())
            return;
        preOptimizeListX(xList, sqlBuilder.conditionSet);//优化连表查询前的准备

        StringBuilder xsb = new StringBuilder();

        pre(criteria.getValueList(), xList);//提取占位符对应的值
        if (xList.isEmpty())
            return;
        xList.get(0).setConjunction(ConjunctionAndOtherScript.WHERE);
        buildConditionSql(xsb, xList);

        String script = xsb.toString();

        mapping(script, criteria, sqlBuilder.sbCondition);

    }


    public static class SqlBuilder {

        private StringBuilder sbResult = new StringBuilder();
        private StringBuilder sbSource = new StringBuilder();
        private StringBuilder sbCondition = new StringBuilder();
        private Set<String> conditionSet = new HashSet<>();

        public static SqlBuilder get() {
            return new SqlBuilder();
        }
    }
}

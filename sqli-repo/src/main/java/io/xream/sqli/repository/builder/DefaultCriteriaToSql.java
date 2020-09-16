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
package io.xream.sqli.repository.builder;

import io.xream.sqli.builder.*;
import io.xream.sqli.core.Mappable;
import io.xream.sqli.core.PropertyMapping;
import io.xream.sqli.core.SqlScript;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.filter.BaseTypeFilter;
import io.xream.sqli.builder.Direction;
import io.xream.sqli.builder.Sort;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.exception.CriteriaSyntaxException;
import io.xream.sqli.repository.exception.SqlBuildException;
import io.xream.sqli.support.TimestampSupport;
import io.xream.sqli.util.JsonWrapper;
import io.xream.sqli.util.SqliStringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Sim
 */
public class DefaultCriteriaToSql implements CriteriaToSql, ResultKeyGenerator {

    @Override
    public String toSql(CriteriaCondition criteriaCondition,List<Object> valueList, Mappable mappable) {
        if (Objects.isNull(criteriaCondition))
            return "";
        StringBuilder sb = new StringBuilder();
        List<BuildingBlock> buildingBlockList = criteriaCondition.getBuildingBlockList();

        if (buildingBlockList.isEmpty())
            return "";

        filter(buildingBlockList, mappable);//过滤
        if (buildingBlockList.isEmpty())
            return "";

        pre(valueList, buildingBlockList);

        buildingBlockList.get(0).setConjunction(ConjunctionAndOtherScript.WHERE);

        buildConditionSql(sb, buildingBlockList, mappable);

        return sb.toString();
    }

    @Override
    public void toSql(boolean isSub, Criteria criteria, SqlBuilt sqlBuilt, SqlBuildingAttached sqlBuildingAttached) {


        SqlBuilder sqlBuilder = SqlBuilder.get();

        parseAlia(isSub, criteria, sqlBuilder);

        filter0(criteria);

        env(criteria);

        /*
         * select column
         */
        select(sqlBuilder, resultKey(sqlBuilder,criteria));
        /*
         * force index
         */
        forceIndex(isSub,sqlBuilder, criteria);

        sourceScriptPre(criteria, sqlBuildingAttached);
        /*
         * StringList
         */
        condition(sqlBuilder, criteria.getBuildingBlockList(), criteria, sqlBuildingAttached.getValueList());

        count(isSub, criteria.isTotalRowsIgnored(),sqlBuilder);
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

        sqlArr(isSub, criteria.isTotalRowsIgnored(),sqlBuilt, sqlBuildingAttached, sqlBuilder);

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
    public String toSql(Parsed parsed, RefreshCondition refreshCondition) {

        String sourceScript = sourceScriptOfRefresh(parsed, refreshCondition);

        StringBuilder sb = new StringBuilder();
        sb.append(SqlScript.UPDATE).append(SqlScript.SPACE).append(sourceScript).append(SqlScript.SPACE);

        concatRefresh(sb, parsed, refreshCondition);

        String conditionSql = toSql(refreshCondition, refreshCondition.getValueList(), refreshCondition);

        sb.append(conditionSql);

        String sql = sb.toString();

        if (sql.contains("SET  WHERE"))
            throw new SqlBuildException(sql);

        return sql;
    }

    private void concatRefresh(StringBuilder sb, Parsed parsed, RefreshCondition refreshCondition) {

        sb.append(SqlScript.SET);

        List<BuildingBlock> refreshList = refreshCondition.getRefreshList();

        List<Object> refreshValueList = new ArrayList<>();

        boolean isNotFirst = false;
        for (BuildingBlock buildingBlock : refreshList) {

            if (buildingBlock.getPredicate() == PredicateAndOtherScript.X) {

                if (isNotFirst) {
                    sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                }

                isNotFirst = true;

                Object key = buildingBlock.getKey();
                String str = key.toString();
                final String sql = normalizeSql(str);
                mapping((reg) -> sql.split(reg), refreshCondition, sb);

            } else {
                String key = buildingBlock.getKey();
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

                    BeanElement be = p.getElementMap().get(k);
                    if (be == null) {
                        throw new ParsingException("can not find the property " + key + " of " + parsed.getClzName());
                    }

                    TimestampSupport.testNumberValueToDate(be.getClz(), buildingBlock);

                    if (SqliStringUtil.isNullOrEmpty(String.valueOf(buildingBlock.getValue()))
                            || BaseTypeFilter.isBaseType(key, buildingBlock.getValue(), parsed)) {
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
                        Object v = buildingBlock.getValue();
                        if (v != null) {
                            String str = JsonWrapper.toJson(v);
                            buildingBlock.setValue(str);
                        }
                    }
//                    else if (BeanUtil.testEnumConstant(be.clz, buildingBlock.getValue())) {
//                        //FIXME
//                    }
                }

                refreshValueList.add(buildingBlock.getValue());
            }

        }

        if (!refreshValueList.isEmpty()) {
            refreshCondition.getValueList().addAll(0, refreshValueList);
        }
    }

    private void sqlArr(boolean isSub, boolean isTotalRowsIgnored, SqlBuilt sqlBuilt, SqlBuildingAttached sqlBuildingAttached, SqlBuilder sb) {

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


    private void env(Criteria criteria) {
        if (criteria instanceof Criteria.ResultMapCriteria) {
            Criteria.ResultMapCriteria resultMapped = (Criteria.ResultMapCriteria) criteria;
            PropertyMapping propertyMapping = resultMapped.getPropertyMapping();//
            if (Objects.isNull(propertyMapping)) {
                propertyMapping = new PropertyMapping();
                resultMapped.setPropertyMapping(propertyMapping);
            }
        }
    }

    private String resultKey(SqlBuilder sqlBuilder, Criteria criteria) {
        if (!(criteria instanceof Criteria.ResultMapCriteria))
            return SqlScript.STAR;

        boolean flag = false;

        Criteria.ResultMapCriteria resultMapped = (Criteria.ResultMapCriteria) criteria;
        StringBuilder column = new StringBuilder();

        PropertyMapping propertyMapping = resultMapped.getPropertyMapping();

        if (Objects.nonNull(resultMapped.getDistinct())) {

            column.append(SqlScript.DISTINCT);
            List<String> list = resultMapped.getDistinct().getList();
            int size = list.size();
            int i = 0;
            StringBuilder distinctColumn = new StringBuilder();
            distinctColumn.append(column);
            for (String resultKey : list) {
                sqlBuilder.conditionSet.add(resultKey);
                String mapper = mapping(resultKey, resultMapped);
                propertyMapping.put(resultKey, mapper);//REDUCE ALIAN NAME
                distinctColumn.append(SqlScript.SPACE).append(mapper);
                mapper = generate(mapper, resultMapped);
                column.append(SqlScript.SPACE).append(mapper);
                i++;
                if (i < size) {
                    column.append(SqlScript.COMMA);
                    distinctColumn.append(SqlScript.COMMA);
                }
            }
            sqlBuilder.countSql = "COUNT(" + distinctColumn.toString() + ") count";
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
                mapper = generate(mapper, resultMapped);
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

        List<FunctionResultKey> functionList = resultMapped.getResultFunctionList();
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
            throw new CriteriaSyntaxException("Suggest API: find(Criteria criteria), no any resultKey for ResultMapCriteria");
        }
//        criteria.setCustomedResultKey(column.toString());

//        ((Criteria.ResultMapCriteria) criteria).adpterResultScript();
        return script;

    }

    private void select(SqlBuilder sqlBuilder, String resultKeys) {
        sqlBuilder.sbResult.append(SqlScript.SELECT).append(SqlScript.SPACE).append(resultKeys).append(SqlScript.SPACE);
    }

    private void groupBy(SqlBuilder sb, Criteria criteria) {
        if (criteria instanceof Criteria.ResultMapCriteria) {
            Criteria.ResultMapCriteria rm = (Criteria.ResultMapCriteria) criteria;

            String groupByS = rm.getGroupBy();
            if (SqliStringUtil.isNullOrEmpty(groupByS))
                return;

            sb.sbCondition.append(ConjunctionAndOtherScript.GROUP_BY.sql());

            String[] arr = groupByS.split(SqlScript.COMMA);

            int i = 0;
            int l = arr.length;
            for (String groupBy : arr) {
                groupBy = groupBy.trim();
                sb.conditionSet.add(groupBy);
                if (SqliStringUtil.isNotNull(groupBy)) {
                    String mapper = mapping(groupBy, (Mappable)rm);
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
        if (!(criteria instanceof Criteria.ResultMapCriteria))
            return;

        Criteria.ResultMapCriteria resultMapped = (Criteria.ResultMapCriteria) criteria;
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


    private void parseAlia(boolean isSub, Criteria criteria, SqlBuilder sqlBuilder) {

        if (criteria instanceof Criteria.ResultMapCriteria) {
            Criteria.ResultMapCriteria rmc = (Criteria.ResultMapCriteria) criteria;

            if (rmc.getSourceScripts().isEmpty()) {// builderSource null
                String sourceScript = rmc.sourceScript();//string -> list<>

                if (isSub && ! sourceScript.contains(".")){
                    String[] arr = sourceScript.split(" ");
                    Parsed parsed = Parser.get(arr[0].trim());
                    rmc.setParsed(parsed);
                    rmc.setClzz(parsed.getClzz());
                }

                List<String> list = SourceScriptBuilder.split(sourceScript);
                List<SourceScript> sourceScripts = SourceScriptBuilder.parse(list);
                rmc.getSourceScripts().addAll(sourceScripts);
            }

            Map<String, String> aliaMap = rmc.getAliaMap();
            for (SourceScript sc : rmc.getSourceScripts()) {
                if (SqliStringUtil.isNotNull(sc.getSource())) {
                    aliaMap.put(sc.alia(), sc.getSource());
                }
            }


            for (SourceScript sourceScript : rmc.getSourceScripts()) {
                preOptimizeListX(sourceScript.getBuildingBlockList(), sqlBuilder.conditionSet);
            }
        }

    }

    private void preOptimizeListX(List<BuildingBlock> buildingBlockList, Set<String> conditionSet) {
        for (BuildingBlock buildingBlock : buildingBlockList) {
            conditionSet.add(buildingBlock.getKey());
            List<BuildingBlock> subList = buildingBlock.getSubList();
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
            if (sourceScript.getSubCriteria() != null) {
                sourceScript.used();
                continue;
            }
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
            if (sourceScript.getSubCriteria() != null) {
                sourceScript.targeted();
                continue;
            }
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
                    optimizeSourceScript(rmc.getSourceScripts(), sb.conditionSet);//FIXME  + ON AND
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

    private void forceIndex(boolean isSub, SqlBuilder sqlBuilder, Criteria criteria) {
        if (isSub)
            return;
        if (SqliStringUtil.isNullOrEmpty(criteria.getForceIndex()))
            return;
        sqlBuilder.sbCondition.append(" FORCE INDEX(" + criteria.getForceIndex() + ")");
        sqlBuilder.conditionSet.add(criteria.getForceIndex());
    }

    private void count(boolean isSub,  boolean isTotalRowsIgnored,  SqlBuilder sqlBuilder) {

        if (isSub || isTotalRowsIgnored)
            return;
            sqlBuilder.countCondition = new StringBuilder();
            sqlBuilder.countCondition.append(sqlBuilder.sbCondition);
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
        List<BuildingBlock> buildingBlockList = criteria.getBuildingBlockList();

        if (criteria instanceof Criteria.ResultMapCriteria) {
            Criteria.ResultMapCriteria resultMapCriteria = (Criteria.ResultMapCriteria)criteria;//FIXME 判断是虚表
            filter(buildingBlockList, resultMapCriteria);
            for (SourceScript sourceScript : ((Criteria.ResultMapCriteria) criteria).getSourceScripts()) {
                filter(sourceScript.getBuildingBlockList(), resultMapCriteria);
            }
        }else{
            filter(buildingBlockList,criteria);
        }
    }

    private void sourceScriptPre(Criteria criteria, SqlBuildingAttached attached) {
        if (criteria instanceof Criteria.ResultMapCriteria) {
            for (SourceScript sourceScript : ((Criteria.ResultMapCriteria) criteria).getSourceScripts()) {
                sourceScript.pre(attached, this);
            }
        }
    }

    private void condition(SqlBuilder sqlBuilder, List<BuildingBlock> buildingBlockList, Criteria criteria,List<Object> valueList) {
        if (buildingBlockList.isEmpty())
            return;
        preOptimizeListX(buildingBlockList, sqlBuilder.conditionSet);//优化连表查询前的准备

        StringBuilder xsb = new StringBuilder();

        pre(valueList, buildingBlockList);//提取占位符对应的值
        if (buildingBlockList.isEmpty())
            return;
        buildingBlockList.get(0).setConjunction(ConjunctionAndOtherScript.WHERE);
        buildConditionSql(xsb, buildingBlockList,criteria);
        sqlBuilder.sbCondition.append(xsb);

    }


    public static class SqlBuilder {

        private StringBuilder sbResult = new StringBuilder();
        private StringBuilder sbSource = new StringBuilder();
        private StringBuilder sbCondition = new StringBuilder();
        private Set<String> conditionSet = new HashSet<>();
        private String countSql = "COUNT(*) count";
        private StringBuilder countCondition;

        public static SqlBuilder get() {
            return new SqlBuilder();
        }
    }


}

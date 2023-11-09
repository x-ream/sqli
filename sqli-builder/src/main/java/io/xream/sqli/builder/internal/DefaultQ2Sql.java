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
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.exception.QSyntaxException;
import io.xream.sqli.exception.SqlBuildException;
import io.xream.sqli.filter.BaseTypeFilter;
import io.xream.sqli.mapping.Mappable;
import io.xream.sqli.page.Page;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.support.TimeSupport;
import io.xream.sqli.support.XSingleSourceSupport;
import io.xream.sqli.util.EnumUtil;
import io.xream.sqli.util.SqliJsonUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sim
 */
public final class DefaultQ2Sql implements Q2Sql, ResultKeyGenerator, SourceScriptOptimizable, XSingleSourceSupport {

    private static Q2Sql instance;

    private DefaultQ2Sql() {
    }

    public static Q2Sql newInstance() {
        if (instance == null) {
            instance = new DefaultQ2Sql();
            return instance;
        }
        return instance;
    }

    @Override
    public String toCondSql(CondQ condQ, List<Object> valueList, Mappable mappable) {
        if (Objects.isNull(condQ))
            return "";
        StringBuilder sb = new StringBuilder();
        List<Bb> bbList = condQ.getBbs();

        if (bbList.isEmpty())
            return "";

        filter(bbList, mappable);//过滤
        if (bbList.isEmpty())
            return "";

        pre(valueList, bbList, mappable);

        bbList.get(0).setC(Op.WHERE);

        buildConditionSql(sb, bbList, mappable);

        return sb.toString();
    }

    @Override
    public void toSql(boolean isSub, Q q, SqlBuilt sqlBuilt, SqlSubsAndValueBinding subsAndValueBinding) {

        SqlSth sqlSth = SqlSth.get();

        parseAlia(q, sqlSth);

        filter0(q);
        /*
         * select column
         */
        select(sqlSth, resultKey(sqlSth, q, subsAndValueBinding));

        sourceScriptPre(q, subsAndValueBinding);

        lastForPage(q);
        /*
         * StringList
         */
        condition(sqlSth, q, subsAndValueBinding.getValueList());

        count(isSub, q.isTotalRowsIgnored(), sqlSth);

        xAggr(sqlSth, q, subsAndValueBinding.getValueList());
        /*
         * group by
         */
        groupBy(sqlSth, q);

        having(sqlSth, q);
        /*
         * sort
         */
        sort(sqlSth, q);
        /*
         * from table
         */
        sourceScript(sqlSth, q);

        sqlArr(isSub, q.isTotalRowsIgnored(), sqlBuilt, subsAndValueBinding, sqlSth);

    }

    private void lastForPage(Q q) {
        long last = q.getLast();
        if (last <= 0)
            return;
        List<Sort> list = q.getSortList();
        if (list == null || list.isEmpty())
            return;
        Sort sort = list.get(0);
        Bb bb = new Bb();
        bb.setC(Op.AND);
        bb.setKey(sort.getOrderBy());
        bb.setValue(last);
        if (sort.getDirection() == Direction.ASC) bb.setP(Op.GT);
        else bb.setP(Op.LT);
        q.getBbs().add(bb);
    }

    private String sourceScriptOfRefresh(Parsed parsed, Qr qr) {
        String sourceScript = qr.getSourceScript();
        if (SqliStringUtil.isNullOrEmpty(sourceScript))
            return parsed.getTableName();

        parseAliaFromRefresh(qr);

        final String str = normalizeSql(sourceScript);

        StringBuilder sb = new StringBuilder();
        mapping(reg -> str.split(reg), qr, sb);

        return sb.toString();
    }

    @Override
    public String toSql(Parsed parsed, Qr qr, DialectSupport dialectSupport) {

        String sourceScript = sourceScriptOfRefresh(parsed, qr);

        StringBuilder sb = new StringBuilder();
        sb.append(dialectSupport.getAlterTableUpdate()).append(SqlScript.SPACE).append(sourceScript)
                .append(SqlScript.SPACE).append(dialectSupport.getCommandUpdate()).append(SqlScript.SPACE);

        concatRefresh(sb, parsed, qr, dialectSupport);

        String conditionSql = toCondSql(qr, qr.getValueList(), qr);

        sb.append(conditionSql);

        if (SqliStringUtil.isNotNull(dialectSupport.getLimitOne()) && qr.getLimit() > 0) {
            sb.append(SqlScript.LIMIT).append(qr.getLimit());
        }

        String sql = sb.toString();

        if (sql.contains("SET  WHERE"))
            throw new SqlBuildException(sql);

        return sql;
    }

    private void concatRefresh(StringBuilder sb, Parsed parsed, Qr qr, DialectSupport dialectSupport) {

        List<Bb> refreshList = qr.getRefreshList();

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
                mapping((reg) -> sql.split(reg), qr, sb);

            } else {
                String key = bb.getKey();
                if (key.contains("?")) {

                    if (isNotFirst) {
                        sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                    }

                    isNotFirst = true;
                    final String sql = normalizeSql(key);
                    mapping((reg) -> sql.split(reg), qr, sb);
                } else {

                    String k = null;
                    Parsed p;
                    if (key.contains(".")) {
                        String[] arr = key.split("\\.");
                        p = Parser.get(arr[0]);
                        if (p == null)
                            throw new ParsingException("can not find the clzz: " + arr[0]);
                        k = arr[1];
                    } else {
                        k = key;
                        p = parsed;
                    }

                    BeanElement be = p.getElement(k);
                    if (be == null) {
                        throw new ParsingException("can not find the property " + key + " of " + parsed.getClzName());
                    }

                    TimeSupport.testWriteNumberValueToTime(be.getClz(), bb);

                    if (SqliStringUtil.isNullOrEmpty(String.valueOf(bb.getValue()))
                            || BaseTypeFilter.isBaseType(key, bb.getValue(), parsed)) {
                        continue;
                    }

                    if (isNotFirst) {
                        sb.append(SqlScript.COMMA).append(SqlScript.SPACE);
                    }

                    isNotFirst = true;

                    String mapper = mapping(key, qr);
                    sb.append(mapper);
                    sb.append(SqlScript.EQ_PLACE_HOLDER);

                    tryToFixBbValue(bb, be, dialectSupport);
                }

                add(refreshValueList, bb.getValue());
            }

        }

        if (!refreshValueList.isEmpty()) {
            qr.getValueList().addAll(0, refreshValueList);
        }
    }

    private void tryToFixBbValue(Bb bb,BeanElement be, DialectSupport dialectSupport) {

        if (be.isJson()) {
            Object v = bb.getValue();
            if (v != null) {
                String str = SqliJsonUtil.toJson(v);
                Object jsonStr = dialectSupport.convertJsonToPersist(str);
                bb.setValue(jsonStr);
            }
        } else if (EnumUtil.isEnum(be.getClz())) {
            Object v = bb.getValue();
            if (v instanceof String) {
                v = EnumUtil.deserialize(be.getClz(), v);
            }
            v = EnumUtil.serialize((Enum) v);
            bb.setValue(v);
        }
    }

    private void sqlArr(boolean isSub, boolean isTotalRowsIgnored, SqlBuilt sqlBuilt, SqlSubsAndValueBinding subsAndValueBinding, SqlSth sb) {
        if (sb.with == null)
            sqlArr0(isSub,isTotalRowsIgnored,sqlBuilt, subsAndValueBinding,sb);
        else
            sqlArr1(isSub,isTotalRowsIgnored,sqlBuilt, subsAndValueBinding,sb);
    }

    private void sqlArr0(boolean isSub, boolean isTotalRowsIgnored, SqlBuilt sqlBuilt, SqlSubsAndValueBinding subsAndValueBinding, SqlSth sb) {

        if (!isSub) {

            for (SqlBuilt sub : subsAndValueBinding.getSubList()) {
                int start = sb.sbSource.indexOf(SqlScript.SUB);
                sb.sbSource.replace(start, start + SqlScript.SUB.length(),
                        SqlScript.LEFT_PARENTTHESIS + sub.getSql().toString() + SqlScript.RIGHT_PARENTTHESIS
                );
            }

            if (!isTotalRowsIgnored) {
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

    private void sqlArr1(boolean isSub, boolean isTotalRowsIgnored, SqlBuilt sqlBuilt, SqlSubsAndValueBinding subsAndValueBinding, SqlSth sb) {

        if (!isSub) {

            StringBuilder sqlSb = new StringBuilder();
            sqlSb.append(sb.with).append(SqlScript.WITH_PLACE).append(sb.sbSource);

            for (SqlBuilt sub : subsAndValueBinding.getSubList()) {
                int start = sqlSb.indexOf(SqlScript.SUB);
                sqlSb.replace(start, start + SqlScript.SUB.length(),
                        SqlScript.LEFT_PARENTTHESIS + sub.getSql().toString() + SqlScript.RIGHT_PARENTTHESIS
                );
            }

            if (!isTotalRowsIgnored) {
                StringBuilder sqlSbc = new StringBuilder();

                sqlSbc.append(sqlSb);
                int start = sqlSbc.indexOf(SqlScript.WITH_PLACE);
                sqlSbc.replace(start, start + SqlScript.WITH_PLACE.length(),
                        SqlScript.SELECT + SqlScript.SPACE + sb.countSql + SqlScript.SPACE);

                sqlSbc.append(sb.countCondition);
                sqlBuilt.setCountSql(sqlSbc.toString());
            }

            int start = sqlSb.indexOf(SqlScript.WITH_PLACE);
            sqlSb.replace(start, start + SqlScript.WITH_PLACE.length(),sb.sbResult.toString()).append(sb.sbCondition);
            sqlBuilt.setSql(sqlSb);
            return;
        }

        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append(sb.with).append(sb.sbResult).append(sb.sbSource).append(sb.sbCondition);

        sqlBuilt.setSql(sqlSb);
    }


    private String resultKey(SqlSth sqlSth, Q q, SqlSubsAndValueBinding subsAndValueBinding) {
        if (!(q instanceof Q.X))
            return SqlScript.STAR;

        boolean flag = false;

        Q.X xq = (Q.X) q;
        StringBuilder columnBuilder = new StringBuilder();

        Map<String, String> mapperPropertyMap = xq.getMapperPropertyMap();

        if (Objects.nonNull(xq.getDistinct())) {

            columnBuilder.append(SqlScript.DISTINCT);
            List<String> list = xq.getDistinct().getList();
            int size = list.size();
            int i = 0;
            StringBuilder distinctColumn = new StringBuilder();
            distinctColumn.append(columnBuilder);
            for (String resultKey : list) {
                addConditonBeforeOptimization(resultKey, sqlSth.conditionSet);
                String mapper = mapping(resultKey, xq);
                mapperPropertyMap.put(mapper, resultKey);//REDUCE ALIAN NAME
                distinctColumn.append(SqlScript.SPACE).append(mapper);
                mapper = generate(mapper, xq);
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

        List<Reduce> reduceList = xq.getReduceList();

        if (!reduceList.isEmpty()) {

            for (Reduce reduce : reduceList) {
                if (flag) {
                    columnBuilder.append(SqlScript.COMMA);
                }
                addConditonBeforeOptimization(reduce.getProperty(), sqlSth.conditionSet);
                String alianProperty = reduce.getProperty() + SqlScript.UNDER_LINE + reduce.getType().toString().toLowerCase();//property_count
                String alianName = alianProperty.replace(SqlScript.DOT, SqlScript.DOLLOR);
                xq.getResultKeyAliaMap().put(alianName, alianProperty);

                String value = mapping(reduce.getProperty(), q);

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

                Bb h = reduce.getHaving();
                if (h != null) {
                    xq.getHavingList().add(h);
                }
                flag = true;
            }
        }

        List<String> resultList = xq.getResultKeyList();
        if (!resultList.isEmpty()) {
            if (flag) {
                columnBuilder.append(SqlScript.COMMA);
            }
            int size = resultList.size();
            for (int i = 0; i < size; i++) {
                String resultKey = resultList.get(i);
                addConditonBeforeOptimization(resultKey, sqlSth.conditionSet);
                String mapper = mapping(resultKey, q);
                mapperPropertyMap.put(mapper, resultKey);
                mapper = generate(mapper, xq);
                columnBuilder.append(SqlScript.SPACE).append(mapper);
                if (i < size - 1) {
                    columnBuilder.append(SqlScript.COMMA);
                }
                flag = true;
            }

        }

        List<KV> resultListAssignedAliaList = xq.getResultKeyAssignedAliaList();
        if (!resultListAssignedAliaList.isEmpty()) {
            if (flag) {
                columnBuilder.append(SqlScript.COMMA);
            }
            int size = resultListAssignedAliaList.size();
            for (int i = 0; i < size; i++) {
                KV kv = resultListAssignedAliaList.get(i);
                String key = kv.getK();
                addConditonBeforeOptimization(key, sqlSth.conditionSet);
                String mapper = mapping(key, q);
                mapperPropertyMap.put(mapper, key);
                String alian = kv.getV().toString();
                xq.getResultKeyAliaMap().put(alian, mapper);
                columnBuilder.append(SqlScript.SPACE).append(mapper).append(SqlScript.AS).append(alian);
                if (i < size - 1) {
                    columnBuilder.append(SqlScript.COMMA);
                }
                flag = true;
            }

        }

        List<FunctionResultKey> functionList = xq.getResultFunctionList();
        if (!functionList.isEmpty()) {//
            if (flag) {
                columnBuilder.append(SqlScript.COMMA);
            }

            Map<String, String> resultKeyAliaMap = xq.getResultKeyAliaMap();

            int size = functionList.size();
            for (int i = 0; i < size; i++) {
                FunctionResultKey functionResultKey = functionList.get(i);

                String function = functionResultKey.getScript();

                columnBuilder.append(SqlScript.SPACE);
                final String functionStr = normalizeSql(function);
                List<String> originList = mapping((reg) -> functionStr.split(reg), q, columnBuilder);
                for (String origin : originList) {
                    addConditonBeforeOptimization(origin, sqlSth.conditionSet);
                }

                for (Object obj : functionResultKey.getValues()) {
                    subsAndValueBinding.getValueList().add(obj);
                }

                String aliaKey = functionResultKey.getAlia();
                String alian = aliaKey.replace(".", "_");
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
            throw new QSyntaxException("Suggest API: find(Q Q), q any resultKey for Q.X");
        }

        return script;

    }

    private void select(SqlSth sqlSth, String resultKeys) {
        sqlSth.sbResult.append(SqlScript.SELECT).append(SqlScript.SPACE).append(resultKeys).append(SqlScript.SPACE);
    }

    private void xAggr(SqlSth sqlSth, Q q, List<Object> valueList) {
        if (q instanceof Q.X) {
            Q.X rm = (Q.X) q;
            List<Bb> list = rm.getAggrList();
            if (list == null)
                return;
            for (Bb bb : list) {
                String key = bb.getKey();
                if (key.contains(SqlScript.PLACE_HOLDER) && Objects.isNull(bb.getValue()))
                    continue;
                List<String> originList = mapping((reg) -> key.split(reg), q, sqlSth.sbCondition);
                for (String origin : originList) {
                    addConditonBeforeOptimization(origin, sqlSth.conditionSet);
                }
                Object values = bb.getValue();
                if (values instanceof Object[]) {
                    for (Object obj : (Object[]) values) {
                        add(valueList, obj);
                    }
                }else if (values instanceof List) {//deserialized from json
                    for (Object obj : (List) values) {
                        add(valueList, obj);
                    }
                }
            }
        }
    }

    private void groupBy(SqlSth sqlSth, Q q) {
        if (q instanceof Q.X) {
            Q.X rm = (Q.X) q;

            String groupByS = rm.getGroupBy();
            if (SqliStringUtil.isNullOrEmpty(groupByS))
                return;

            sqlSth.sbCondition.append(Op.GROUP_BY.sql());

            String[] arr = groupByS.split(SqlScript.COMMA);

            int i = 0;
            int l = arr.length;
            for (String groupBy : arr) {
                String groupByStr = groupBy.trim();
                if (SqliStringUtil.isNotNull(groupBy)) {
                    if (groupBy.contains(SqlScript.LEFT_PARENTTHESIS)) {
                        final String groupByStrFinal = normalizeSql(groupByStr);
                        List<String> originList = mapping((reg) -> groupByStrFinal.split(reg), q, sqlSth.sbCondition);
                        for (String origin : originList) {
                            addConditonBeforeOptimization(origin, sqlSth.conditionSet);
                        }
                    } else {
                        String mapper = mapping(groupByStr, rm);
                        addConditonBeforeOptimization(groupByStr, sqlSth.conditionSet);
                        sqlSth.sbCondition.append(mapper);
                    }
                    i++;
                    if (i < l) {
                        sqlSth.sbCondition.append(SqlScript.COMMA);
                    }
                }
            }
        }
    }

    private void having(SqlSth sqlSth, Q q) {
        if (!(q instanceof Q.X))
            return;

        Q.X xq = (Q.X) q;
        List<Bb> bbList = xq.getHavingList();

        if (bbList == null || bbList.isEmpty())
            return;

        if (!q.isTotalRowsIgnored()) {
            throw new QSyntaxException("Reduce with having not support totalRows query, try to builder.paged().ignoreTotalRows()");
        }

        boolean flag = true;
        for (Bb h : bbList) {
            if (h == null)
                continue;
            if (flag) {
                sqlSth.sbCondition.append(Op.HAVING.sql());
                flag = false;
            } else {
                sqlSth.sbCondition.append(Op.AND.sql());
            }

            String alia = h.getKey();
            if (alia.contains(SqlScript.LEFT_PARENTTHESIS)) {
                alia = normalizeSql(alia);
                final String finalKey = alia;
                List<String> originList = mapping((reg) -> finalKey.split(reg), q, sqlSth.sbCondition);
                for (String origin : originList) {
                    addConditonBeforeOptimization(origin, sqlSth.conditionSet);
                }
            } else {
                sqlSth.sbCondition.append(mapping(alia,q));
                addConditonBeforeOptimization(alia, sqlSth.conditionSet);
            }
            sqlSth.sbCondition.append(SqlScript.SPACE).append(h.getP().sql()).append(SqlScript.SPACE).append(h.getValue());
        }
    }

    private void parseAliaFromRefresh(Qr qr) {

        String script = qr.getSourceScript();//string -> list<>
        List<String> list = FromBuilder.split(script);
        List<Froms> froms = FromBuilder.parseScriptAndBuild(list,null);
        FromBuilder.checkSourceAndAlia(froms);
        for (Froms sc : froms) {
            qr.getAliaMap().put(sc.alia(), sc.getSource());
        }

    }


    private void parseAlia(Q q, SqlSth sqlSth) {

        if (q instanceof Q.X) {
            Q.X rmc = (Q.X) q;

            if (rmc.getSourceScripts().isEmpty()) {// builderSource null
                String sourceScript = rmc.sourceScript();//string -> list<>

                List<String> list = FromBuilder.split(sourceScript);
                List<Froms> froms = FromBuilder.parseScriptAndBuild(list,rmc.getSourceScriptValueList());
                rmc.getSourceScripts().addAll(froms);
            }

            FromBuilder.checkSourceAndAlia(rmc.getSourceScripts());
            supportSingleSource(rmc);

            Map<String, String> aliaMap = rmc.getAliaMap();
            for (Froms sc : rmc.getSourceScripts()) {
                if (SqliStringUtil.isNotNull(sc.getSource())) {
                    aliaMap.put(sc.alia(), sc.getSource());
                }
            }

            for (Froms froms : rmc.getSourceScripts()) {
                JOIN join = froms.getJoin();
                if (join != null && join.getOn() != null && join.getOn().getBbs() != null ) {
                    List<Bb>  bbs = join.getOn().getBbs();
                    if (bbs.size() > 1) {
                        List<Bb> tempList = new ArrayList<>();
                        int i = 0;
                        for (Bb bb : bbs) {
                            if (i++ > 0) {
                                tempList.add(bb);
                            }
                        }
                        addConditionBeforeOptimization(tempList, sqlSth.conditionSet);
                    }
                }
            }
        }

    }

    private void with(SqlSth sb, Q.X rmc) {
        
        List<Froms> ssList = rmc.getSourceScripts();
        String subStr = null;
        for (Froms ss : ssList) {
            if (ss.isWith()) {
                subStr = subStr == null ? "" : subStr + ", ";
                subStr += (ss.getAlia() + SqlScript.AS + SqlScript.SUB );
            }
        }
        if (subStr != null){
            sb.with = "WITH " + subStr + SqlScript.SPACE;
        }
    }

    private void sourceScript(SqlSth sb, Q q) {

        sb.sbSource.append(SqlScript.SPACE);

        String script = null;
        if (q instanceof Q.X) {
            Q.X xq = (Q.X) q;

            if (xq.getSourceScripts().isEmpty()) {// builderSource null
                String str = q.sourceScript();
                Objects.requireNonNull(str, "Not set sourceScript of QB.X");
                final String strd = normalizeSql(str);
                StringBuilder sbs = new StringBuilder();
                mapping((reg) -> strd.split(reg), xq, sbs);
                script = sbs.toString();
            } else {
                if (!xq.isWithoutOptimization()) {
                    optimizeSourceScript(sb.conditionSet, xq.getSourceScripts());//FIXME  + ON AND
                }
                script = xq.getSourceScripts().stream()
                        .map(sourceScript -> sourceScript.sql(xq))
                        .collect(Collectors.joining()).trim();
                with(sb,xq);
            }

            sb.sbSource.append(SqlScript.FROM).append(SqlScript.SPACE);

        } else {
            script = mapping(q.sourceScript(), q);
            if (!script.startsWith(SqlScript.FROM) || !script.startsWith(SqlScript.FROM.toLowerCase()))
                sb.sbSource.append(SqlScript.FROM).append(SqlScript.SPACE);
        }
        sb.sbSource.append(script);

    }

    private void count(boolean isSub, boolean isTotalRowsIgnored, SqlSth sqlSth) {

        if (isSub || isTotalRowsIgnored)
            return;
        sqlSth.countCondition = new StringBuilder();
        sqlSth.countCondition.append(sqlSth.sbCondition);
    }

    private void sort(SqlSth sb, Q q) {

        if (q.isFixedSort())
            return;

        List<Sort> sortList = q.getSortList();
        if (sortList != null && !sortList.isEmpty()) {

            sb.sbCondition.append(Op.ORDER_BY.sql());
            int size = sortList.size();
            int i = 0;
            for (Sort sort : sortList) {
                String orderBy = sort.getOrderBy();
                orderBy = normalizeSql(orderBy);
                orderBy = noSpace(orderBy);
                String mapper = mapping(orderBy, q);
                sb.sbCondition.append(mapper).append(SqlScript.SPACE);
                addConditonBeforeOptimization(orderBy, sb.conditionSet);
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

    private void filter0(Q q) {
        List<Bb> bbList = q.getBbs();

        if (q instanceof Q.X) {
            Q.X xq = (Q.X) q;//FIXME 判断是虚表
            filter(bbList, xq);
            for (Froms froms : ((Q.X) q).getSourceScripts()) {
                if (froms.getJoin() == null || froms.getJoin().getOn() == null)
                    continue;
                List<Bb> bbs = froms.getJoin().getOn().getBbs();
                if (bbs == null || bbs.isEmpty())
                    continue;
                filter(bbs, xq);
            }
        } else {
            filter(bbList, q);
        }
    }

    private void sourceScriptPre(Q q, SqlSubsAndValueBinding attached) {
        if (q instanceof Q.X) {
            for (Froms froms : ((Q.X) q).getSourceScripts()) {
                froms.pre(attached, this, q);
            }
        }
    }

    private void condition(SqlSth sqlSth, Q q, List<Object> valueList) {
        List<Bb> bbList = q.getBbs();
        if (bbList.isEmpty())
            return;
        addConditionBeforeOptimization(bbList, sqlSth.conditionSet);//优化连表查询前的准备

        StringBuilder xsb = new StringBuilder();

        pre(valueList, bbList, q);//提取占位符对应的值
        if (bbList.isEmpty())
            return;
        withSourceScriptValuelist(q,valueList);
        bbList.get(0).setC(Op.WHERE);
        buildConditionSql(xsb, bbList, q);
        sqlSth.sbCondition.append(xsb);

    }

    private void withSourceScriptValuelist(Q q, List<Object> valueList){
        List<Object> objectList = q.getSourceScriptValueList();
        if (objectList != null ){
            for (Object v : objectList) {
                if (v == null) continue;
                if (v instanceof List && ((List) v).isEmpty()) continue;
                valueList.add(0,v);
            }
        }
    }


    public static final class SqlSth {

        private String with = null;
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

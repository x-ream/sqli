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

import io.xream.sqli.mapping.Mappable;
import io.xream.sqli.util.SqliStringUtil;

import java.util.List;

/**
 * @Author Sim
 */
public final class SourceScript implements ConditionToSql, ConditionToSql.Pre {

    private String source;
    private Criteria.ResultMapCriteria subCriteria;
    private JoinType joinType;
    private String joinStr;
    private On on;
    private String alia;
    private List<Bb> bbList;
    private boolean isWith;

    private transient boolean used;
    private transient boolean targeted;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Criteria.ResultMapCriteria getSubCriteria() {
        return subCriteria;
    }

    public void setSubCriteria(Criteria.ResultMapCriteria subCriteria) {
        this.subCriteria = subCriteria;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public String getJoinStr() {
        return joinStr;
    }

    public void setJoinStr(String joinStr) {
        this.joinStr = joinStr;
    }

    public List<Bb> getBbList() {
        return bbList;
    }

    public void setBbList(List<Bb> bbs) {
        this.bbList = bbs;
    }

    public boolean isWith() {
        return isWith;
    }

    public void setWith(boolean with) {
        isWith = with;
    }

    public On getOn() {
        return on;
    }

    public void setOn(On on) {
        this.on = on;
    }

    public String getAlia() {
        return alia;
    }

    public void setAlia(String alia) {
        this.alia = alia;
    }

    public boolean isUsed() {
        return this.used;
    }

    public void used() {
        this.used = true;
    }

    public boolean isTargeted() {
        return targeted;
    }

    public void targeted() {
        this.targeted = true;
    }

    public String alia() {
        return alia == null ? source : alia;
    }


    public void pre(SqlBuildingAttached attached, CriteriaToSql criteriaToSql, Mappable mappable) {

        if (subCriteria != null) {
            final SqlBuilt sqlBuilt = new SqlBuilt();
            attached.getSubList().add(sqlBuilt);
            criteriaToSql.toSql(true, subCriteria, sqlBuilt, attached);
        }
        if (bbList == null || bbList.isEmpty())
            return;
        pre(attached.getValueList(), bbList, subCriteria == null ? mappable : subCriteria);

    }

    public String sql(Mappable mappable) {
        if ((SqliStringUtil.isNullOrEmpty(source) && SqliStringUtil.isNullOrEmpty(alia))
                && subCriteria == null)
            return "";
        if (subCriteria != null)
            source = isWith ? "" : SqlScript.SUB;
        if (source == null)
            source = "";
        if (joinStr == null && (joinType == null || joinType == JoinType.MAIN)) {
            if (alia != null && !alia.equals(source)) {
                return mapping(source, mappable) + " " + alia;
            }
            return mapping(source, mappable);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(joinStr == null ? joinType.sql() : SqlScript.SPACE + joinStr + SqlScript.SPACE);

        sb.append(mapping(source, mappable));

        if (alia != null && !alia.equals(source))
            sb.append(SqlScript.SPACE).append(alia);

        if (on != null) {
            sb.append(SqlScript.ON);
            String aliaName = alia == null ? mapping(source, mappable) : alia;
            String key = on.getKey();
            if (SqliStringUtil.isNotNull(key)) {
                sb.append(
                        mapping(on.getJoinFrom().getAlia() + "." + on.getJoinFrom().getKey(), mappable)
                ).append(SqlScript.SPACE).append(on.getOp()).append(SqlScript.SPACE)
                        .append(
                                mapping(aliaName + "." +key, mappable)
                        );
            }
        }

        buildConditionSql(sb, bbList, mappable);

        return sb.toString();
    }

    @Override
    public String toString() {
        return "SourceScript{" +
                "source='" + source + '\'' +
                ", subCriteria=" + subCriteria +
                ", joinType=" + joinType +
                ", joinStr='" + joinStr + '\'' +
                ", on=" + on +
                ", alia='" + alia + '\'' +
                ", bbList=" + bbList +
                ", used=" + used +
                ", targeted=" + targeted +
                '}';
    }
}

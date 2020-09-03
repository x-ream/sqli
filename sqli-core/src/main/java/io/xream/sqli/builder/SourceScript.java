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

import io.xream.sqli.util.SqliStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Sim
 */
public final class SourceScript implements ConditionCriteriaToSql, ConditionCriteriaToSql.Pre {

    private String source;
    private JoinType joinType;
    private On on;
    private String alia;
    private List<BuildingBlock> buildingBlockList = new ArrayList<>();

    private transient boolean used;
    private transient boolean targeted;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public List<BuildingBlock> getBuildingBlockList() {
        return buildingBlockList;
    }

    public void setBuildingBlockList(List<BuildingBlock> buildingBlocks) {
        this.buildingBlockList = buildingBlocks;
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


    public void pre(List<Object> valueList) {
        pre(valueList, buildingBlockList);
    }

    public String sql() {
        if (SqliStringUtil.isNullOrEmpty(source))
            return "";
        if (joinType == null || joinType == JoinType.MAIN) {
            if (alia != null && !alia.equals(source))
                return source + " " + alia;
            return source;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(joinType.sql()).append(source);

        if (alia != null && !alia.equals(source))
            sb.append(SqlScript.SPACE).append(alia);

        {
            if (on != null) {
                sb.append(SqlScript.ON);
                String aliaName = alia == null ? source : alia;
                String key = on.getKey();
                if (SqliStringUtil.isNotNull(key)) {
                    sb.append(on.getJoinFrom().getAlia()).append(".").append(on.getJoinFrom().getKey())
                            .append(SqlScript.SPACE).append(on.getOp()).append(SqlScript.SPACE)
                            .append(aliaName)
                            .append(".")
                            .append(key);
                }
            }
        }

        buildConditionSql(sb, buildingBlockList);

        return sb.toString();
    }

    @Override
    public String toString() {
        return "SourceScript{" +
                "source='" + source + '\'' +
                ", joinType=" + joinType +
                ", on =" + on +
                ", alia='" + alia + '\'' +
                ", buildingBlockList='" + buildingBlockList + '\'' +
                ", used=" + used +
                ", targeted=" + targeted +
                '}';
    }

}

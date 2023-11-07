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

import io.xream.sqli.builder.Q;
import io.xream.sqli.mapping.Mappable;
import io.xream.sqli.util.SqliStringUtil;

import java.util.List;

/**
 * @author Sim
 */
public final class Froms implements CondQToSql, CondQToSql.Pre {

    private String source;
    private Q.X subQ;
    private String alia;
    private JOIN join;
    private boolean isWith;

    private transient boolean used;
    private transient boolean targeted;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Q.X getSubQ() {
        return subQ;
    }

    public void setSubQ(Q.X subQ) {
        this.subQ = subQ;
    }

    public String getAlia() {
        return alia;
    }

    public void setAlia(String alia) {
        this.alia = alia;
    }

    public JOIN getJoin() {
        return join;
    }

    public void setJoin(JOIN join) {
        this.join = join;
    }

    public boolean isWith() {
        return isWith;
    }

    public void setWith(boolean with) {
        isWith = with;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isTargeted() {
        return targeted;
    }

    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
    }

    public void used() {
        this.used = true;
    }

    public void targeted() {
        this.targeted = true;
    }


    public String alia() {
        return alia == null ? source : alia;
    }


    public void pre(SqlSubsAndValueBinding attached, Q2Sql condToSql, Mappable mappable) {

        if (subQ != null) {
            final SqlBuilt sqlBuilt = new SqlBuilt();
            attached.getSubList().add(sqlBuilt);
            condToSql.toSql(true, subQ, sqlBuilt, attached);
        }
        if (join == null || join.getOn() == null)
            return;
        List<Bb> bbList = join.getOn().getBbs();
        if (bbList == null || bbList.isEmpty())
            return;
        pre(attached.getValueList(), bbList, subQ == null ? mappable : subQ);

    }

    public String sql(Mappable mappable) {
        if ((SqliStringUtil.isNullOrEmpty(source) && SqliStringUtil.isNullOrEmpty(alia))
                && subQ == null)
            return "";
        if (subQ != null)
            source = isWith ? "" : SqlScript.SUB;
        if (source == null)
            source = "";
        if (join == null) {
            if (alia != null && !alia.equals(source)) {
                return mapping(source, mappable) + " " + alia;
            }
            return mapping(source, mappable);
        }
        StringBuilder sb = new StringBuilder();
        if (join.getJoin() != null) {
            sb.append(SqlScript.SPACE + join.getJoin() + SqlScript.SPACE);
        }

        sb.append(mapping(source, mappable));

        if (alia != null && !alia.equals(source)) {
            sb.append(SqlScript.SPACE).append(alia);
        }

        ON on = join.getOn();
        if (on != null && !on.getBbs().isEmpty()) {
            sb.append(SqlScript.ON);
            buildConditionSql(sb, on.getBbs(), mappable);
        }


        return sb.toString();
    }

}

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
package io.xream.sqli.repository.dao;

import io.xream.sqli.api.TemporaryRepository;
import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.CriteriaToSql;
import io.xream.sqli.builder.SqlBuilt;
import io.xream.sqli.builder.SqlScript;
import io.xream.sqli.dialect.Dialect;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.spi.JdbcHelper;
import io.xream.sqli.util.EnumUtil;
import io.xream.sqli.util.SqliLoggerProxy;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Sim
 */
public final class TemporaryDaoImpl implements TemporaryDao{

    private static TemporaryDao instance;

    private CriteriaToSql criteriaToSql;

    private Dialect dialect;

    private JdbcHelper jdbcHelper;

    private SqlBuilder sqlBuilder = SqlBuilder.getInstance();

    private TemporaryDaoImpl(){}

    public static TemporaryDao newInstance(){
        if(instance == null){
            instance = new TemporaryDaoImpl();
            return instance;
        }
        return null;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public void setCriteriaToSql(CriteriaToSql criteriaToSql) {
        this.criteriaToSql = criteriaToSql;
    }

    public void setJdbcHelper(JdbcHelper jdbcHelper) {
        this.jdbcHelper = jdbcHelper;
    }


    @Override
    public boolean   findToCreate(Class clzz, Criteria.ResultMapCriteria ResultMapCriteria) {

        List<Object> valueList = new ArrayList<>();
        SqlBuilt sqlBuilt = sqlBuilder.buildQueryByCriteria(valueList,ResultMapCriteria, criteriaToSql, dialect);
        StringBuilder fromSqlSb = sqlBuilt.getSql();

        Parsed parsed = Parser.get(clzz);

        StringBuilder sb = new StringBuilder();
        sb.append(SqlScript.CREATE_TEMPORARY_TABLE).append(parsed.getTableName())
                .append(SqlScript.AS);

        String sql = null;
        if (valueList == null || valueList.isEmpty()) {
            sql = sb.append(fromSqlSb).toString();
        } else {
            Object[] arr = dialect.toArr(valueList);
            String fromSql = fromSqlSb.toString();
            for (Object obj : arr) {
                if (obj instanceof String) {
                    fromSql = fromSql.replaceFirst("\\?", "'" + obj.toString() + "'");
                }else if (EnumUtil.isEnum(obj.getClass())){
                    Object enumObj = EnumUtil.serialize((Enum)obj);
                    if (enumObj instanceof String) {
                        fromSql = fromSql.replaceFirst("\\?", "'" + EnumUtil.serialize((Enum) obj) + "'");
                    }else{
                        fromSql = fromSql.replaceFirst("\\?", String.valueOf(EnumUtil.serialize((Enum) obj)));
                    }
                }else if (obj instanceof Date ) {
                    fromSql = fromSql.replaceFirst("\\?",
                            "DATE_FORMAT(" + ((Date)obj).getTime()
                            + ", '%Y-%m-%d %h:%i:%s')"
                    );
                }else if (obj instanceof Timestamp) {
                    fromSql = fromSql.replaceFirst("\\?",
                            "DATE_FORMAT(" + ((Timestamp)obj).getTime()
                                    + ", '%Y-%m-%d %h:%i:%s')"
                    );
                }else {
                    fromSql = fromSql.replaceFirst("\\?", obj.toString());
                }
            }
            sb.append(fromSql);
            sql = sb.toString();
        }
        this.jdbcHelper.execute(sql);
        SqliLoggerProxy.debug(TemporaryRepository.class,sql);

        return true;
    }

    @Override
    public boolean execute(String sql) {
        this.jdbcHelper.execute(sql);
        return true;
    }


}

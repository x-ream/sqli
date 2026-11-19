/*
 * Copyright 2020 io.xream.sqli
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
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
package io.xream.sqli.dialect;

import io.xream.sqli.exception.NotSupportedException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * SQL dialect for Apache IoTDB's table model.
 */
public final class IoTDBDialect extends MySqlDialect {

    @Override
    public String getKey() {
        return "iotdb";
    }

    @Override
    public String createOrReplaceSql(String sql) {
        throw new NotSupportedException("IoTDB does not support createOrReplace(); use create() or an explicit UPDATE");
    }

    @Override
    public String createSql(Parsed parsed, List<BeanElement> tempList) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(parsed.getTableName()).append(" (");
        for (int i = 0; i < tempList.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append(" ").append(tempList.get(i).property()).append(" ");
        }
        sql.append(") VALUES (");
        for (int i = 0; i < tempList.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        return sql.append(")").toString();
    }

    @Override
    public Object filterValue(Object value) {
        if (value instanceof Timestamp || value instanceof java.sql.Date) {
            return value;
        }
        if (value instanceof Date) {
            return new Timestamp(((Date) value).getTime());
        }
        return super.filterValue(value);
    }
}

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
package io.xream.sqli.repository.mapper;

import io.xream.sqli.api.Dialect;
import io.xream.sqli.api.TemporaryRepository;
import io.xream.sqli.exception.ParsingException;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Sim
 */
public class DefaultTemporaryTableParser implements TemporaryRepository.Parser {

    private Dialect dialect;

    private Map<Class, String> sqlMap = new ConcurrentHashMap<>();

    public void setDialect(Dialect dialect){
        this.dialect = dialect;
    }

    @Override
    public String parseAndGetSql(Class clzz){

        String sql = sqlMap.get(clzz);
        if (sql != null)
            return sql;

        Parsed parsed = Parser.get(clzz.getSimpleName());
        if (parsed != null)
            throw new ParsingException("Table exists while parse temporary table entity to get sql: " + clzz.getName());
        Parser.parse(clzz);

        return getTableSql(clzz);
    }


    protected String getTableSql(Class clz) {
        return MapperFactory.StandardSql.buildTableSql(clz,true);
    }
}

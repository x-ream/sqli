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
package io.xream.sqli.dialect;

import io.xream.sqli.builder.internal.SqlScript;

/**
 * @author Sim
 */
public final class ClickhouseDialect extends MySqlDialect{

    @Override
    public String getKey(){
        return "clickhouse";
    }

    @Override
    public String getAlterTableUpdate() {
        return SqlScript.ALTER_TABLE;
    }

    @Override
    public String getAlterTableDelete() {
        return SqlScript.ALTER_TABLE ;
    }

    @Override
    public String getCommandUpdate() {
        return SqlScript.UPDATE;
    }

    @Override
    public String getCommandDelete() {
        return SqlScript.DELETE;
    }

}

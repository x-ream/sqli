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
package io.xream.sqli.exception;

import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.SqliExceptionUtil;
import org.slf4j.Logger;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author Sim
 */
public class ExceptionTranslator {

    private ExceptionTranslator(){}

    public static  SqliRuntimeException onRollback(Class clzz, Exception e, Logger logger) {
        Throwable t = SqliExceptionUtil.unwrapThrowable(e);
        logger.error(SqliExceptionUtil.getMessage(t));
        if ( t instanceof SQLIntegrityConstraintViolationException){
            String msg = t.getMessage();
            if (msg.contains("cannot be null")) {
                String prefix = (clzz == null ? "" : ("Table of "+ Parser.get(clzz).getTableName()));
                throw new SqliRuntimeException(prefix+", " + msg);
            }
        }
        if (t instanceof RuntimeException)
            throw (RuntimeException)t;
        return new SqliRuntimeException(t);
    }

    public static QueryException onQuery(Exception e, Logger logger) {
        Throwable t = SqliExceptionUtil.unwrapThrowable(e);
        logger.error(SqliExceptionUtil.getMessage(t));
        if (t instanceof RuntimeException)
            throw (RuntimeException)t;
        return new QueryException(e);
    }
}

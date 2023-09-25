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
package io.xream.sqli.repository.internal;

import io.xream.sqli.builder.Bb;
import io.xream.sqli.builder.Op;
import io.xream.sqli.builder.RefreshCond;
import io.xream.sqli.exception.CriteriaSyntaxException;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * @author Sim
 */
public interface SafeRefreshBiz<T> {

    default void tryToRefreshSafe(Class clzz, RefreshCond refreshCondition) {
        refreshCondition.setClz(clzz);
        Parsed parsed = Parser.get(clzz);
        Field keyField = parsed.getKeyField();
        if (Objects.isNull(keyField))
            throw new CriteriaSyntaxException("No PrimaryKey, UnSafe Refresh, try to invoke DefaultRepository.refreshUnSafe(RefreshCondition<T> refreshCondition)");

        boolean unSafe = true;//Safe

        if (unSafe) {
            String key = parsed.getKey();
            List<Bb> bbList = refreshCondition.getBbList();
            for (Bb bb : bbList) {
                String k = bb.getKey();
                boolean b = k.contains(".") ? k.endsWith("."+key) : key.equals(k);
                if (b) {
                    Object value = bb.getValue();
                    if (Objects.nonNull(value) && !value.toString().equals("0")) {
                        unSafe = false;//Safe
                        if (bb.getP() == Op.EQ || bb.getP() == Op.IN) {
                            int size = 1;
                            if (value instanceof List) {
                                size = ((List) value).size();
                            }
                            if (size > refreshCondition.getLimit()) {
                                refreshCondition.setLimit(size);
                            }
                        }
                        break;
                    }
                }
            }
        }

        if (unSafe)
            throw new CriteriaSyntaxException("UnSafe Refresh, try to invoke DefaultRepository.refreshUnSafe(RefreshCondition<T> refreshCondition)");
    }
}

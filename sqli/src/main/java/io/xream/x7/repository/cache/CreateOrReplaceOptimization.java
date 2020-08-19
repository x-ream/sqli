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
package io.xream.x7.repository.cache;

import io.xream.sqli.core.builder.Parsed;
import io.xream.sqli.core.util.BeanUtilX;
import io.xream.sqli.core.util.SqlStringUtil;

public interface CreateOrReplaceOptimization {

    static Object tryToGetId(Object obj, Parsed parsed){
        Object id = BeanUtilX.tryToGetId(obj, parsed);
        String idStr = String.valueOf(id);
        if (SqlStringUtil.isNullOrEmpty(idStr) || idStr.equals("0"))
            throw new IllegalArgumentException("createOrReplace(obj),  obj keyOne = " + id);
        return id;
    }
}

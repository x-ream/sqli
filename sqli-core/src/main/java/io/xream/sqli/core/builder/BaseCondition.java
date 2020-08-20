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
package io.xream.sqli.core.builder;


import java.util.List;

/**
 * @Author Sim
 */
public interface BaseCondition {

    BaseCondition eq(String property, Object value);

    BaseCondition lt(String property, Object value);

    BaseCondition lte(String property, Object value);

    BaseCondition gt(String property, Object value);

    BaseCondition gte(String property, Object value);

    BaseCondition ne(String property, Object value);

    BaseCondition like(String property, String value);

    BaseCondition likeRight(String property, String value);

    BaseCondition notLike(String property, String value);

    BaseCondition between(String property, Object min, Object max);

    BaseCondition in(String property, List<? extends Object> list);

    BaseCondition nin(String property, List<Object> list);

    BaseCondition nonNull(String property);

    BaseCondition isNull(String property);

    BaseCondition x(String sql);

    BaseCondition x(String sql, List<Object> valueList);
}

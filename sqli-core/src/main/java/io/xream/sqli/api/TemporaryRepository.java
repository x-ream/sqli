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
package io.xream.sqli.api;

import io.xream.sqli.builder.Q;
import io.xream.sqli.spi.Schema;

import java.util.List;

/**
 * How to update with TemporaryRepository?
 * suggest:
 *      .findToHandle(Q.X, map -> {
 *
 *             refresh(
 *                  qr.build()....
 *             )
 *
 *         });
 *
 * @author  Sim
 */
public interface TemporaryRepository {

    boolean create(Object obj);
    boolean createBatch(List objList);
    boolean findToCreate(Class clzz, Q.X xq);

    boolean createRepository(Class clzz);
    boolean dropRepository(Class clzz);

    interface Parser {
        void setSchema(Schema schema);
        String parseAndGetSql(Class clzz);
    }
}

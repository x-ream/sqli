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
package io.xream.sqli.core;

import io.xream.sqli.util.EnumUtil;

import java.util.Objects;

/**
 * @author Sim
 */
public interface ValuePost {
    default Object filter(Object object, MoreFilter moreFilter) {
        Object o = null;
        if (object instanceof String) {
            String str = (String) object;
            o = str.replace("<", "&lt").replace(">", "&gt");
        }else if (Objects.nonNull(object) && EnumUtil.isEnum(object.getClass())){
            o = EnumUtil.serialize((Enum) object);
        }else{
            o = object;
        }

        if (moreFilter == null)
            return o;

        return moreFilter.filter(o);
    }

    interface MoreFilter{
        Object filter(Object object);
    }

}

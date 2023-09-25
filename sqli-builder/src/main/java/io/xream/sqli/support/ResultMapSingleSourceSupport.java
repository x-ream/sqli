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
package io.xream.sqli.support;

import io.xream.sqli.builder.Q;
import io.xream.sqli.builder.SourceScript;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;

/**
 * @author Sim
 */
public interface ResultMapSingleSourceSupport {

    default void supportSingleSource(Q.X resultMapCriteria) {
        if (resultMapCriteria.getSourceScripts().size() == 1 && resultMapCriteria.getParsed() == null) {
            SourceScript sourceScript = resultMapCriteria.getSourceScripts().get(0);
            String source = sourceScript.getSource();
            if (source != null) {
                Parsed parsed = Parser.get(source);
                resultMapCriteria.setParsed(parsed);
                resultMapCriteria.setClzz(parsed.getClzz());
            }
        }
    }
}

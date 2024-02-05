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
package io.xream.sqli.builder.internal;

import io.xream.sqli.builder.Q;
import io.xream.sqli.page.Page;

import java.util.List;
import java.util.Map;

/**
 * @author Sim
 */
public final class PageBuilderHelper {

    private PageBuilderHelper(){}

    public static <T> Page<T> build(Q q, List<T> list, TotalRows totalRows) {

        long count = 0;
        int rows = q.getRows();
        int page = q.getPage();
        if (!q.isTotalRowsIgnored()) {
            int size = list.size();
            if (page == 0) {
                count = size;
            } else {
                try {
                    count = totalRows.count();
                }catch (Exception e){

                }
            }
        }else{
            count = -1;
        }

        Page<T> pagination = new Page<>();
        if (q instanceof Q.X)
            pagination.setClzz(Map.class);
        else
            pagination.setClzz(q.getClzz());
        pagination.setPage(page == 0 ? 1 : page);
        pagination.setRows(rows == 0 ? Integer.MAX_VALUE : rows);
        pagination.setSortList(q.getSortList());
        pagination.setTotalRowsIgnored(q.isTotalRowsIgnored());
        pagination.setList(list);
        pagination.setTotalRows(count);

        return pagination;
    }

    public interface TotalRows {
        long count();
    }

}

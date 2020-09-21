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
package io.xream.sqli.cache.internal;

import io.xream.sqli.builder.BuildingBlock;
import io.xream.sqli.builder.Criteria;
import io.xream.sqli.builder.KV;
import io.xream.sqli.builder.Sort;
import io.xream.sqli.cache.CriteriaCacheKeyBuildable;
import io.xream.sqli.util.SqliStringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @Author Sim
 */
public class CriteriaCacheKeyBuilder implements CriteriaCacheKeyBuildable {

    @Override
    public String buildCacheKey(Criteria criteria, boolean isOfTotalRows){
        StringBuilder sb = new StringBuilder();
        sb.append(criteria.getRouteKey());
        buildCacheKeyByBuildingBlockList(criteria.getBuildingBlockList(), sb);

        if (! isOfTotalRows) {
            sb.append(criteria.getPage()).append(criteria.getRows());
            if (criteria.getSortList() != null) {
                for (Sort sort : criteria.getSortList()) {
                    sb.append(sort.getOrderBy()).append(sort.getDirection());
                }
            }
            if (criteria.getFixedSortList() != null) {
                for (KV kv : criteria.getFixedSortList()) {
                    sb.append(kv.k).append(kv.v);
                }
            }
        }
        return sb.toString();
    }

    private void buildCacheKeyByBuildingBlock(BuildingBlock buildingBlock, StringBuilder sb) {
        sb.append(buildingBlock.getConjunction()).append(buildingBlock.getPredicate());
        if (SqliStringUtil.isNotNull(buildingBlock.getKey()) || Objects.nonNull(buildingBlock.getValue())) {
            sb.append(buildingBlock.getKey()).append(buildingBlock.getValue());
        }
        List<BuildingBlock> subList = buildingBlock.getSubList();
        if (subList != null && !subList.isEmpty()){
            buildCacheKeyByBuildingBlockList(subList,sb);
        }
    }

    private void buildCacheKeyByBuildingBlockList(List<BuildingBlock> buildingBlockList, StringBuilder sb){
        int size = buildingBlockList.size();
        if (size == 0)
            return;
        if (size == 1){
            BuildingBlock buildingBlock = buildingBlockList.get(0);
            buildCacheKeyByBuildingBlock(buildingBlock,sb);
        }else{
            Set<BuildingBlock> set = new HashSet<>();
            for (BuildingBlock buildingBlock : buildingBlockList) {
                set.add(buildingBlock);
            }
            for (BuildingBlock buildingBlock : set){
                buildCacheKeyByBuildingBlock(buildingBlock,sb);
            }
        }

    }
}

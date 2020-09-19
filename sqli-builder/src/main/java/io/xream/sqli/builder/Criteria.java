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
package io.xream.sqli.builder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.xream.sqli.api.Routable;
import io.xream.sqli.core.Mappable;
import io.xream.sqli.core.ResultMapHelpful;
import io.xream.sqli.core.SqlNormalizer;
import io.xream.sqli.page.Paged;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.SqliStringUtil;

import java.io.Serializable;
import java.util.*;

/**
 * 
 * @author sim
 *
 */
public class Criteria implements Mappable,CriteriaCondition, Paged, Routable,Serializable {

	private static final long serialVersionUID = 7088698915888081349L;

	private Class<?> clzz;
	private boolean isTotalRowsIgnored;
	private int page;
	private int rows;
	private Object routeKey;
	private List<Sort> sortList;
	private List<KV> fixedSortList = new ArrayList<>();
	private List<BuildingBlock> buildingBlockList = new ArrayList<>();
	private String forceIndex;

	@JsonIgnore
	private transient Parsed parsed;

	@Override
	public Map<String,String> getAliaMap(){
		return null;
	}

	@Override
	public Map<String,String> getResultKeyAliaMap() {return null;}

	public Class<?> getClzz() {
		return clzz;
	}

	public void setClzz(Class<?> clz) {
		this.clzz = clz;
	}

	public Parsed getParsed() {
		return parsed;
	}

	public void setParsed(Parsed parsed) {
		this.parsed = parsed;
	}

	public String sourceScript() {
		return BeanUtil.getByFirstLower(getClzz().getSimpleName());
	}

	public List<Sort> getSortList() {
		if (sortList == null || sortList.isEmpty())
			return null;
		Iterator<Sort> ite = sortList.iterator();
		while (ite.hasNext()){
			Sort sort = ite.next();
			if (SqliStringUtil.isNullOrEmpty(sort.getOrderBy())) {
				ite.remove();
			}
		}
		return sortList;
	}

	public void setSortList(List<Sort> sortList) {
		this.sortList = sortList;
	}

	public List<KV> getFixedSortList() {
		return fixedSortList;
	}

	public void setTotalRowsIgnored(boolean totalRowsIgnored) {
		isTotalRowsIgnored = totalRowsIgnored;
	}

	@Override
	public boolean isTotalRowsIgnored() {
		return isTotalRowsIgnored;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public String getForceIndex() {
		return forceIndex;
	}

	public void setForceIndex(String forceIndex) {
		this.forceIndex = forceIndex;
	}

	@Override
	public Object getRouteKey() {
		return routeKey;
	}

	public void setRouteKey(Object routeKey) {
		this.routeKey = routeKey;
	}

	@Override
	public List<BuildingBlock> getBuildingBlockList() {
		return this.buildingBlockList;
	}
	
	protected void add(BuildingBlock buildingBlock) {
		this.buildingBlockList.add(buildingBlock);
	}

	public boolean isFixedSort() {
		return !this.fixedSortList.isEmpty();
	}

	public void paged(Paged paged) {

		this.isTotalRowsIgnored = paged.isTotalRowsIgnored();
		this.page = paged.getPage();
		this.rows = paged.getRows();
		this.sortList = paged.getSortList();
	}

	public String getCacheKey(){
		StringBuilder sb = new StringBuilder();
		sb.append(isTotalRowsIgnored).append(page).append(rows);
		if (sortList != null) {
			for (Sort sort : sortList) {
				sb.append(sort.getOrderBy()).append(sort.getDirection());
			}
		}
		for (KV kv : fixedSortList){
			sb.append(kv.k).append(kv.v);
		}
		for (BuildingBlock buildingBlock : buildingBlockList){
			sb.append(buildingBlock.getConjunction()).append(buildingBlock.getPredicate()).append(buildingBlock.getKey()).append(buildingBlock.getValue());
		}
		sb.append(forceIndex);
		sb.append(clzz);
		sb.append(routeKey);

		return sb.toString();
	}

	@Override
	public String toString() {
		return "Criteria{" +
				"isTotalRowsIgnored=" + isTotalRowsIgnored +
				", page=" + page +
				", rows=" + rows +
				", sortList='" + sortList + '\'' +
				", buildingBlockList=" + buildingBlockList +
				", forceIndex=" + forceIndex +
				", clz=" + clzz +
				'}';
	}

	public static final class ResultMapCriteria extends Criteria implements ResultMapHelpful, SqlNormalizer,Serializable{

		private static final long serialVersionUID = -2365612538012282380L;
		private List<String> resultKeyList = new ArrayList<String>();
		private List<FunctionResultKey> resultFunctionList = new ArrayList<>();
		private List<KV> resultKeyAssignedAliaList = new ArrayList<>();
		private String groupBy;
		private Distinct distinct;
		private String sourceScript;
		private List<SourceScript> sourceScripts = new ArrayList<>();
		private List<Reduce> reduceList = new ArrayList<>();
		private boolean isResultWithDottedKey;
		private boolean isWithoutOptimization;
		@JsonIgnore
		private transient Map<String,String> mapperPropertyMap = new HashMap<>();
		@JsonIgnore
		private transient Map<String,String> aliaMap = new HashMap<>();
		@JsonIgnore
		private transient Map<String,String> resultKeyAliaMap = new HashMap<>();

		public Distinct getDistinct() {
			return distinct;
		}

		public List<Reduce> getReduceList() {
			return reduceList;
		}

		public List<SourceScript> getSourceScripts() {
			return sourceScripts;
		}

		public String getGroupBy() {
			return groupBy;
		}

		public void setGroupBy(String groupBy) {
			if (SqliStringUtil.isNullOrEmpty(this.groupBy)){
				this.groupBy = groupBy;
				return;
			}
			if (this.groupBy.contains(groupBy))
				return;
			this.groupBy = this.groupBy + ", " + groupBy;
		}

		public void setDistinct(Distinct distinct) {
			this.distinct = distinct;
		}

		@Override
		public Map<String, String> getMapperPropertyMap() {
			return mapperPropertyMap;
		}

		public Map<String, String> getResultKeyAliaMap() {
			return this.resultKeyAliaMap;
		}

		public Map<String, String> getAliaMap() {
			return aliaMap;
		}

		public void setSourceScript(String sourceScript) {
			this.sourceScript = normalizeSql(sourceScript);
		}

		public List<String> getResultKeyList() {
			return resultKeyList;
		}

		public List<KV> getResultKeyAssignedAliaList() {
			return resultKeyAssignedAliaList;
		}

		public List<FunctionResultKey> getResultFunctionList() {
			return resultFunctionList;
		}

		public boolean isResultWithDottedKey() {
			return isResultWithDottedKey;
		}

		public void setResultWithDottedKey(boolean resultWithDottedKey) {
			isResultWithDottedKey = resultWithDottedKey;
		}

		public boolean isWithoutOptimization() {
			return isWithoutOptimization;
		}

		public void setWithoutOptimization(boolean withoutOptimization) {
			isWithoutOptimization = withoutOptimization;
		}

		@Override
		public Class<?> getClzz() {
			return super.clzz == null ? Map.class : super.clzz;
		}

		@Override
		public String sourceScript() {
			if (sourceScript == null) {
				if (super.getClzz() == null)
					return null;
				return BeanUtil.getByFirstLower(super.getClzz().getSimpleName());
			} else {
				return sourceScript;
			}
		}


		@Override
		public String toString() {
			return "ResultMapCriteria{" +
					"resultKeyList=" + resultKeyList +
					", sourceScript='" + sourceScript + '\'' +
					", distinct=" + distinct +
					", groupBy='" + groupBy + '\'' +
					", reduceList=" + reduceList +
					", aliaMap=" + aliaMap +
					'}';
		}

	}

}
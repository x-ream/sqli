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
import io.xream.sqli.mapping.Mappable;
import io.xream.sqli.mapping.ResultMapHelpful;
import io.xream.sqli.mapping.SqlNormalizer;
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
	private List<KV> fixedSortList;
	private List<Bb> bbList = new ArrayList<>();

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

	public void setFixedSortList(List<KV> fixedSortList) {
		this.fixedSortList = fixedSortList;
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

	@Override
	public Object getRouteKey() {
		return routeKey;
	}

	public void setRouteKey(Object routeKey) {
		this.routeKey = routeKey;
	}

	@Override
	public List<Bb> getBbList() {
		return this.bbList;
	}
	
	protected void add(Bb bb) {
		this.bbList.add(bb);
	}

	public boolean isFixedSort() {
		return this.fixedSortList != null && !this.fixedSortList.isEmpty();
	}

	public void paged(Paged paged) {

		this.isTotalRowsIgnored = paged.isTotalRowsIgnored();
		this.page = paged.getPage();
		this.rows = paged.getRows();
		if (this.sortList == null){
			this.sortList = paged.getSortList();
		}else{
			this.sortList.addAll(paged.getSortList());
		}
	}


	@Override
	public String toString() {
		return "Criteria{" +
				"isTotalRowsIgnored=" + isTotalRowsIgnored +
				", page=" + page +
				", rows=" + rows +
				", sortList='" + sortList + '\'' +
				", bbList=" + bbList +
				", clz=" + clzz +
				'}';
	}

	public static final class ResultMapCriteria extends Criteria implements ResultMapHelpful, SqlNormalizer,Serializable{

		private static final long serialVersionUID = -2365612538012282380L;
		private List<String> resultKeyList = new ArrayList<String>();
		private List<FunctionResultKey> resultFunctionList;
		private List<KV> resultKeyAssignedAliaList;
		private String groupBy;
		private List<Bb> aggrList;
		private Distinct distinct;
		private String sourceScript;
		private List<SourceScript> sourceScripts;
		private List<Reduce> reduceList;
		private List<Having> havingList;
		private boolean isResultWithDottedKey;
		private boolean isWithoutOptimization;
		@JsonIgnore
		private transient Map<String,String> mapperPropertyMap = new HashMap<>();
		@JsonIgnore
		private transient Map<String,String> aliaMap = new HashMap<>();
		@JsonIgnore
		private transient Map<String,String> resultKeyAliaMap = new HashMap<>();
		@JsonIgnore
		private transient Class repositoryClzz;

		public Distinct getDistinct() {
			return distinct;
		}

		public List<Reduce> getReduceList() {
			if (this.reduceList == null) {
				this.reduceList = new ArrayList<>();
			}
			return reduceList;
		}

		public List<Having> getHavingList() {
			if (this.havingList == null) {
				this.havingList = new ArrayList<>();
			}
			return havingList;
		}

		public List<SourceScript> getSourceScripts() {
			if (this.sourceScripts == null){
				this.sourceScripts = new ArrayList<>();
			}
			return this.sourceScripts;
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

		public List<Bb> getAggrList() {
			return aggrList;
		}

		public void setAggrList(List<Bb> aggrList) {
			this.aggrList = aggrList;
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
			if (this.resultKeyAssignedAliaList == null){
				this.resultKeyAssignedAliaList = new ArrayList<>();
			}
			return this.resultKeyAssignedAliaList;
		}

		public List<FunctionResultKey> getResultFunctionList() {
			if (this.resultFunctionList == null) {
				this.resultFunctionList = new ArrayList<>();
			}
			return this.resultFunctionList;
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

		public void setResultFunctionList(List<FunctionResultKey> resultFunctionList) {
			this.resultFunctionList = resultFunctionList;
		}

		public void setResultKeyAssignedAliaList(List<KV> resultKeyAssignedAliaList) {
			this.resultKeyAssignedAliaList = resultKeyAssignedAliaList;
		}

		public void setSourceScripts(List<SourceScript> sourceScripts) {
			this.sourceScripts = sourceScripts;
		}

		public void setReduceList(List<Reduce> reduceList) {
			this.reduceList = reduceList;
		}

		public void setHavingList(List<Having> havingList) {
			this.havingList = havingList;
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

		public Class getRepositoryClzz() {
			return repositoryClzz;
		}

		public void setRepositoryClzz(Class repositoryClzz) {
			this.repositoryClzz = repositoryClzz;
		}

		@Override
		public String toString() {
			return "ResultMapCriteria{" +
					"resultKeyList=" + resultKeyList +
					", sourceScript='" + sourceScript + '\'' +
					", distinct=" + distinct +
					", groupBy='" + groupBy + '\'' +
					", aggrList='" + aggrList + '\'' +
					", reduceList=" + reduceList +
					", aliaMap=" + aliaMap +
					'}';
		}

	}

}
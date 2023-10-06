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
package io.xream.sqli.page;


import io.xream.sqli.builder.Sort;
import io.xream.sqli.util.SqliJsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Page
 * @author Sim
 *
 * @param <T>
 */
public final class Page<T> implements Paged, Serializable{
	
	private static final long serialVersionUID = -3917421382413274341L;

	private Class<T> clzz;

	private int rows = 20;
	private int page = 1;
	private long last;
	private long totalRows = -1;
	private List<T> list;
	private List<String> keyList;
	private boolean totalRowsIgnored;
	private List<Sort> sortList;


	public Class<T> getClzz() {
		return clzz;
	}

	public void setClzz(Class clz) {
		this.clzz = clz;
		if (Objects.nonNull(this.list) && this.list.size() > 0){

			List tempList = new ArrayList<>();
			tempList.addAll(this.list);

			this.list.clear();

			this.setList(tempList);

		}
	}


	public int getRows() {
		if (rows == 0)
			return 20;
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public long getLast() {
		return last;
	}

	public void setLast(long last) {
		this.last = last;
	}

	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}

	public List<T> getList() {
		return list;
	}

	/**
	 * Codeable Method,instead of setList
	 * @param list
	 */
	public void reSetList(List<T> list){
		this.list = list;
	}

	@Deprecated
	public void setList(List<T> list) {
		if (Objects.isNull(this.clzz) || this.clzz == Void.class){
			this.list = list;
			return;
		}
		if (Objects.nonNull(this.list) && !this.list.isEmpty()) {
			this.list = list;
			return;
		}


		/*
		 * Maybe from Json
		 */
		if (Objects.isNull(this.list)){
			this.list = new ArrayList<>();
		}
		for (T t : list){

			if (this.clzz == Map.class){
				this.list.add(t);
			}else {
				if (t instanceof Map){
					T obj = SqliJsonUtil.toObject(t,this.clzz);
					this.list.add(obj);
				}else{
					this.list.add(t);
				}
			}
		}
	}

	public List<String> getKeyList() {
		return this.keyList;
	}

	public void setKeyList(List<String> keyList) {
		this.keyList = keyList;
	}

	public boolean isTotalRowsIgnored() {
		return totalRowsIgnored;
	}

	public void setTotalRowsIgnored(boolean totalRowsIgnored) {
		this.totalRowsIgnored = totalRowsIgnored;
	}

	public List<Sort> getSortList() {
		return sortList;
	}

	public void setSortList(List<Sort> sortList) {
		this.sortList = sortList;
	}

	public int getTotalPages() {
		int totalPages = (int) (totalRows / getRows());
		if (totalRows % getRows() > 0)
			totalPages += 1;
		return totalPages;
	}
	
	public int getPage() {
		if (totalRowsIgnored){
			return page;
		}
		if (totalRows == -1)
			return page;
		if (totalRows == 0)
			return 1;
		int maxPage = (int) (totalRows / getRows());
		if (totalRows % getRows() > 0)
			maxPage += 1;
		if (page > maxPage)
			page = maxPage;
		if (page < 1)
			return 1;
		return page;
	}
	
	@Override
	public String toString() {
		return "Page [totalRowsIgnored=" + totalRowsIgnored + ", " +
				"totalRows=" + totalRows + ", " +
				"page=" + page + ", " +
				"rows=" + rows + ", " +
				"last=" + last + ", " +
				"sortList=" + sortList + ", " +
				"list=" + list + ", " +
				"keyList=" + keyList ;
	}

}

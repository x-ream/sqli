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
package io.xream.sqli.core.web;

import io.xream.sqli.core.builder.Sort;
import io.xream.sqli.core.util.SqlStringUtil;

import java.util.ArrayList;
import java.util.List;

public class TokenedAndPagedRo implements Paged, Tokened{

	private String passportId;
	private String token;
	private String passportType;
	private boolean totalRowsIgnored;
	private int page;
	private int rows;
	private String orderBy;
	private Direction direction;
	private List<Sort> sortList;
	public long getPassportId() {
		if (SqlStringUtil.isNullOrEmpty(passportId))
			return 0;
		return Long.valueOf(passportId);
	}
	public void setPassportId(String passportId) {
		this.passportId = passportId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getPassportType() {
		return passportType;
	}
	public void setPassportType(String passportType) {
		this.passportType = passportType;
	}
	@Override
	public boolean isTotalRowsIgnored() {
		return this.totalRowsIgnored;
	}
	public void setTotalRowsIgnored(boolean totalRowsIgnored) {
		this.totalRowsIgnored = totalRowsIgnored;
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
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction sc) {
		this.direction = sc;
	}
	public void setScroll(boolean isScroll) {
		this.totalRowsIgnored = isScroll;
	}

	@Override
	public List<Sort> getSortList() {
		if (sortList != null && !sortList.isEmpty())
			return sortList;
		if (SqlStringUtil.isNotNull(orderBy)){
			if(sortList == null){
				sortList = new ArrayList<>();
			}
			Direction d = this.direction == null ? Direction.DESC : this.direction;
			String[] arr = orderBy.split(",");
			for (String str : arr) {
				Sort sort = new Sort(str.trim(), d);
				sortList.add(sort);
			}
		}
		return sortList;
	}

	public void setSortList(List<Sort> sortList) {
		this.sortList = sortList;
	}

	@Override
	public String toString() {
		return "TokenedAndPagedRo{" +
				"passportId='" + passportId + '\'' +
				", token='" + token + '\'' +
				", passportType='" + passportType + '\'' +
				", totalRowsIgnored=" + totalRowsIgnored +
				", page=" + page +
				", rows=" + rows +
				", orderBy='" + orderBy + '\'' +
				", direction=" + direction +
				", sortList=" + sortList +
				'}';
	}
}

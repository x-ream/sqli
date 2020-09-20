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

/**
 * @Author Sim
 */
public enum ConjunctionAndOtherScript implements SqlScript {

	NONE(""),
	AND(" AND "),
	OR(" OR "),
	ORDER_BY(" ORDER BY "),
	GROUP_BY(" GROUP BY "),
	HAVING(" HAVING "),
	WHERE(" WHERE "),

	EQ(" = "),
	LT(" < "),
	GT(" > "),
	LTE(" <= "),
	GTE(" >= "),
	NE(" <> "),
	LIKE(" LIKE "),
	NOT_LIKE(" NOT LIKE "),
	IN(" IN "),
	NOT_IN(" NOT IN "),
	IS_NOT_NULL(" IS NOT NULL "),
	IS_NULL(" IS NULL "),
	SUB(" SUB "),
	X(" ");
	;
	
	private String sqlOper;
	ConjunctionAndOtherScript(String sqlOper){
		this.sqlOper = sqlOper;
	}
	@Override
	public String sql() {
		return this.sqlOper;
	}
	
}

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

import io.xream.sqli.mapping.Script;

import java.util.Arrays;
import java.util.List;

/**
 * @Author Sim
 */
public interface SqlScript extends Script {

	List<String> SOURCE_SCRIPT = Arrays.asList("INNER","LEFT","RIGHT","OUTER","JOIN",",","FULL","ON", "AND","OR","LIKE", "!=", "<=", ">=", "<>", "=", "<",">", "(",")");

	String[] KEYWORDS = {
			"order",
			"state",
			"desc",
			"group",
			"asc",
			"key",
			"select",
			"delete",
			"from",
			"update",
			"create",
			"drop",
			"dump",
			"alter",
			"all",
			"distinct",
			"table",
			"column",
			"database",
			"left",
			"right",
			"inner",
			"join",
			"union",
			"natural",
			"between",
			"except",
			"in",
			"as",
			"into",
			"set",
			"values",
			"min",
			"max",
			"sum",
			"avg",
			"count",
			"on",
			"where",
			"and",
			"add",
			"index",
			"exists",
			"or",
			"null",
			"is",
			"not",
			"by",
			"having",
			"concat",
			"cast",
			"convert",
			"case",
			"when",
			"like",
			"replace",
			"primary",
			"foreign",
			"references",
			"char",
			"varchar",
			"varchar2",
			"int",
			"bigint",
			"smallint",
			"tinyint",
			"text",
			"longtext",
			"tinytext",
			"decimal",
			"numeric",
			"float",
			"double",
			"timestamp",
			"date",
			"real",
			"precision",
			"date",
			"datetime",
			"boolean",
			"bool",
			"blob",
			"now",
			"function",
			"procedure",
			"trigger"
	};



	String SELECT = "SELECT";
	String DISTINCT = "DISTINCT";
	String WHERE = " WHERE ";
	String FROM = "FROM";
	String LIMIT = " LIMIT ";
	String SET = " SET ";
	String UPDATE = "UPDATE";
	String IN = " IN ";
	String ON = " ON ";

	String AS = " AS ";

	String PLACE_HOLDER = "?";
	String EQ_PLACE_HOLDER = " = ?";
	String LIKE_HOLDER = "%";
	String COMMA = ",";
	String STAR = "*";
	String UNDER_LINE = "_";
	String LEFT_PARENTTHESIS = "(";
	String RIGHT_PARENTTHESIS = ")";
	String DOLLOR = "$";
	String SINGLE_QUOTES = "'";
	String KEYWORD_MARK = "`";
	String SUB = "${SUB}";

	String sql();
}

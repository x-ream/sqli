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
package io.xream.sqli.repository.mapper;

import io.xream.sqli.annotation.X;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.repository.api.Mapped;
import io.xream.sqli.repository.util.SqlParserUtil;
import io.xream.sqli.starter.DbType;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.SqliLoggerProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Sim
 */
public class MapperFactory implements Mapper {

	private static Map<Class, Map<String, String>> sqlsMap = new HashMap<>();

	public static io.xream.sqli.api.Dialect Dialect;

	/**
	 * 返回SQL
	 * 
	 * @param clz
	 *            ? extends IAutoMapped
	 * @param type
	 *            (BeanMapper.CREATE|BeanMapper.REFRESH|BeanMapper.DROP|
	 *            BeanMapper.QUERY)
	 */
	@SuppressWarnings({ "rawtypes" })
	public static String getSql(Class clz, String type) {

		Map<String, String> sqlMap = sqlsMap.get(clz);
		if (sqlMap == null) {
			sqlMap = new HashMap<>();
			sqlsMap.put(clz, sqlMap);
			parseBean(clz);
		}

		return sqlMap.get(type);

	}

	@SuppressWarnings({ "rawtypes" })
	public static String tryToCreate(Class clz) {

		getSql(clz,CREATE_TABLE);
		sqlsMap.get(clz).remove(CREATE_TABLE);

		return "";
	}


	@SuppressWarnings({ "rawtypes" })
	public static void parseBean(Class clz) {

		String dbType = DbType.value;
		switch (dbType) {
		default:
			StandardSql sql = new StandardSql();
			sql.getTableSql(clz);
			sql.getRefreshSql(clz);
			sql.getRemoveSql(clz);
			sql.getOneSql(clz);
			sql.getQuerySql(clz);
			sql.getLoadSql(clz);
			sql.getCreateSql(clz);
			sql.getTagSql(clz);
			return;
		}

	}

	public static class StandardSql implements Interpreter {
		public String getRefreshSql(Class clz) {

			Parsed parsed = Parser.get(clz);

			List<BeanElement> list = Parser.get(clz).getBeanElementList();

			String space = " ";
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE ");
			sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);
			sb.append("SET ");

			String keyOne = parsed.getKey(X.KEY_ONE);

			List<BeanElement> tempList = new ArrayList<BeanElement>();
			for (BeanElement p : list) {
				String column = p.getProperty();
				if (column.equals(keyOne))
					continue;

				tempList.add(p);
			}

			int size = tempList.size();
			for (int i = 0; i < size; i++) {
				String column = tempList.get(i).getProperty();

				sb.append(column).append(" = ?");
				if (i < size - 1) {
					sb.append(", ");
				}
			}

			sb.append(" WHERE ");

			parseKey(sb, clz);

			String sql = sb.toString();

			sql = SqlParserUtil.mapper(sql, parsed);

			sqlsMap.get(clz).put(REFRESH, sql);

			SqliLoggerProxy.debug(clz, sb);

			return sql;

		}

		public String getRemoveSql(Class clz) {
			Parsed parsed = Parser.get(clz);
			String space = " ";
			StringBuilder sb = new StringBuilder();
			sb.append("DELETE FROM ");
			sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);
			sb.append("WHERE ");

			parseKey(sb, clz);

			String sql = sb.toString();

			sql = SqlParserUtil.mapper(sql, parsed);

			sqlsMap.get(clz).put(REMOVE, sql);

			SqliLoggerProxy.debug(clz, sb);

			return sql;

		}

		public String getOneSql(Class clz) {
			Parsed parsed = Parser.get(clz);
			String space = " ";
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM ");
			sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);
			sb.append("WHERE ");

			parseKey(sb, clz);

			String sql = sb.toString();

			sql = SqlParserUtil.mapper(sql, parsed);

			sqlsMap.get(clz).put(GET_ONE, sql);

			SqliLoggerProxy.debug(clz, sb);

			return sql;

		}

		public void parseKey(StringBuilder sb, Class clz) {
			Parsed parsed = Parser.get(clz);

			sb.append(parsed.getKey(X.KEY_ONE));
			sb.append(" = ?");

		}

		public String getQuerySql(Class clz) {

			Parsed parsed = Parser.get(clz);
			String space = " ";
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM ");
			sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);
			sb.append("WHERE ");

			sb.append(parsed.getKey(X.KEY_ONE));
			sb.append(" = ?");

			String sql = sb.toString();
			sql = SqlParserUtil.mapper(sql, parsed);

			sqlsMap.get(clz).put(QUERY, sql);

			SqliLoggerProxy.debug(clz, sb);

			return sql;

		}

		public String getLoadSql(Class clz) {

			Parsed parsed = Parser.get(clz);
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM ");
			sb.append(BeanUtil.getByFirstLower(parsed.getClzName()));

			String sql = sb.toString();

			sql = SqlParserUtil.mapper(sql, parsed);

			sqlsMap.get(clz).put(LOAD, sql);

			SqliLoggerProxy.debug(clz, sb);

			return sql;

		}



		public String getCreateSql(Class clz) {

			List<BeanElement> list = Parser.get(clz).getBeanElementList();

			Parsed parsed = Parser.get(clz);

			List<BeanElement> tempList = new ArrayList<BeanElement>();
			for (BeanElement p : list) {

				tempList.add(p);
			}

			String space = " ";
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ");
			sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);

			sb.append("(");
			int size = tempList.size();
			for (int i = 0; i < size; i++) {
				String p = tempList.get(i).getProperty();

				sb.append(" ").append(p).append(" ");
				if (i < size - 1) {
					sb.append(",");
				}
			}
			sb.append(") VALUES (");

			for (int i = 0; i < size; i++) {

				sb.append("?");
				if (i < size - 1) {
					sb.append(",");
				}
			}
			sb.append(")");

			String sql = sb.toString();
			sql = SqlParserUtil.mapper(sql, parsed);
			sqlsMap.get(clz).put(CREATE, sql);

			SqliLoggerProxy.debug(clz, sb);

			return sql;

		}

		public static String buildTableSql(Class clz,boolean isTemporary){
			Parsed parsed = Parser.get(clz);
			List<BeanElement> temp = parsed.getBeanElementList();
			Map<String, BeanElement> map = new HashMap<String, BeanElement>();
			List<BeanElement> list = new ArrayList<BeanElement>();
			for (BeanElement be : temp) {
				if (be.getSqlType() != null && be.getSqlType().equals("text")) {
					list.add(be);
					continue;
				}
				map.put(be.getProperty(), be);
			}

			final String keyOne = parsed.getKey(X.KEY_ONE);

			StringBuilder sb = new StringBuilder();
			if (isTemporary){
				sb.append("\"CREATE TEMPORARY TABLE IF NOT EXISTS \"");
			}else{
				sb.append("CREATE TABLE IF NOT EXISTS ");
			}
			sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(" (")
					.append("\n");

			sb.append("   ").append(keyOne);

			BeanElement be = map.get(keyOne);
			String sqlType = Mapper.getSqlTypeRegX(be);

			if (sqlType.equals(Dialect.INT)) {
				sb.append(Dialect.INT + " NOT NULL");
			} else if (sqlType.equals(Dialect.LONG)) {
				sb.append(Dialect.LONG + " NOT NULL");
			} else if (sqlType.equals(Dialect.STRING)) {
				sb.append(Dialect.STRING).append("(").append(be.getLength()).append(") NOT NULL");
			}

			sb.append(", ");// FIXME ORACLE

			sb.append("\n");
			map.remove(keyOne);

			for (BeanElement bet : map.values()) {
				sqlType = Mapper.getSqlTypeRegX(bet);
				sb.append("   ").append(bet.getProperty()).append(" ");

				sb.append(sqlType);

				if (sqlType.equals(Dialect.BIG)) {
					sb.append(" DEFAULT 0.00 ");
				} else if (sqlType.equals(Dialect.DATE)) {
					sb.append(" NULL");

				}else if (BeanUtil.isEnum(bet.getClz())) {
					sb.append("(").append(bet.getLength()).append(") NOT NULL");
				} else if (sqlType.equals(Dialect.STRING)) {
					sb.append("(").append(bet.getLength()).append(") NULL");
				} else {
					Class clzz = bet.getClz();
					if (clzz == Boolean.class || clzz == boolean.class || clzz == Integer.class
							|| clzz == int.class || clzz == Long.class || clzz == long.class) {
						sb.append(" DEFAULT 0");
					} else {
						sb.append(" DEFAULT NULL");
					}
				}
				sb.append(",").append("\n");
			}

			for (BeanElement bet : list) {
				sqlType = Mapper.getSqlTypeRegX(bet);
				sb.append("   ").append(bet.getProperty()).append(" ").append(sqlType).append(",").append("\n");
			}

			sb.append("   PRIMARY KEY ( ").append(keyOne).append(" )");

			sb.append("\n");
			sb.append(") ").append(Dialect.ENGINE).append(";");
			String sql = sb.toString();
			sql = Dialect.match(sql, CREATE_TABLE);
			sql = SqlParserUtil.mapper(sql, Parser.get(clz));
			sqlsMap.get(clz).put(CREATE_TABLE, sql);
			return sql;
		}

		public String getTableSql(Class clz) {
			return buildTableSql(clz,false);
		}

		public String getTagSql(Class clz) {
			Parsed parsed = Parser.get(clz);
			String space = " ";
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT " + Mapped.TAG + " FROM ");
			sb.append(BeanUtil.getByFirstLower(parsed.getClzName())).append(space);

			String sql = sb.toString();

			sql = SqlParserUtil.mapper(sql, parsed);
			sqlsMap.get(clz).put(TAG, sql);

			SqliLoggerProxy.debug(clz, sb);

			return sql;
		}
	}

}

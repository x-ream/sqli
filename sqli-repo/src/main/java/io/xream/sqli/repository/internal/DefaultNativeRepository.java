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
package io.xream.sqli.repository.internal;


import io.xream.sqli.api.NativeRepository;
import io.xream.sqli.core.NativeSupport;
import io.xream.sqli.dialect.DynamicDialectKeyRemovable;
import io.xream.sqli.exception.PersistenceException;
import io.xream.sqli.exception.QueryException;
import io.xream.sqli.util.SqliExceptionUtil;

import java.util.List;
import java.util.Map;


/**
 * @Author Sim
 */
public final class DefaultNativeRepository implements NativeRepository, DynamicDialectKeyRemovable {

	private static NativeRepository instance;
	private NativeSupport nativeSupport;

	private DefaultNativeRepository(){}
	public static NativeRepository newInstance(){
		if (instance == null){
			instance = new DefaultNativeRepository();
			return instance;
		}
		return null;
	}

	public void setNativeSupport(NativeSupport nativeSupport){
		if (this.nativeSupport == null){
			this.nativeSupport = nativeSupport;
		}
	}

	@Override
	public boolean execute(String sql, Object...objs){
		try {
			return nativeSupport.execute(sql, objs);
		}catch (Exception e) {
			SqliExceptionUtil.throwRuntimeExceptionFirst(e);
			throw new PersistenceException(e);
		}finally {
			removeDialectKey();
		}
	}
	@Override
	public  List<Map<String,Object>> list(String sql, List<Object> conditionList){
		try {
			return nativeSupport.list(sql, conditionList);
		}catch (Exception e) {
			SqliExceptionUtil.throwRuntimeExceptionFirst(e);
			throw new QueryException(SqliExceptionUtil.getMessage(e));
		}finally {
			removeDialectKey();
		}
	}

}

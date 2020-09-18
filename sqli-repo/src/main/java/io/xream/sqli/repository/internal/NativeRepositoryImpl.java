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


import io.xream.sqli.repository.api.NativeRepository;
import io.xream.sqli.repository.api.NativeSupport;

import java.util.List;
import java.util.Map;


/**
 * @Author Sim
 */
public final class NativeRepositoryImpl implements NativeRepository {

	private static NativeRepository instance;
	private NativeRepositoryImpl(){}
	public static NativeRepository newInstance(){
		if (instance == null){
			instance = new NativeRepositoryImpl();
			return instance;
		}
		return null;
	}

	private NativeSupport cacheableRepository;
	public void setNativeSupport(NativeSupport nativeSupport){
		if (this.cacheableRepository == null){
			this.cacheableRepository = nativeSupport;
		}
	}

	@Override
	public <T> boolean execute(Class<T> clzz, String sql){
		return cacheableRepository.execute(clzz, sql);
	}
	@Override
	public  List<Map<String,Object>> list(String sql, List<Object> conditionList){
		return cacheableRepository.list(sql, conditionList);
	}

}

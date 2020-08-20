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
package io.xream.sqli.repository.api;


import java.util.List;
import java.util.Map;


public final class ManuRepository {

	private static ManuRepository instance;

	private static Manuable cacheableRepository;

	private ManuRepository(){

	}

	public static void init(Manuable repository){

		if (instance == null) {
			instance = new ManuRepository();
			cacheableRepository = repository;
		}
	}

	public static <T> boolean execute(T obj, String sql){
		return cacheableRepository.execute(obj, sql);
	}

	public static List<Map<String,Object>> list(Class clz, String sql, List<Object> conditionSet){
		return cacheableRepository.list(clz, sql, conditionSet);
	}

}

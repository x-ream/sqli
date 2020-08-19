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
package io.xream.sqli.core.builder;

import io.xream.sqli.core.util.SqliExceptionUtil;
import io.xream.sqli.core.util.JsonWrapper;

public class GenericObject<T> {

	private String clzz;
	private Object obj;

	public GenericObject(){}
	public GenericObject(T t){
		this.obj = t;
	}

	public String getClzz() {
		if (this.clzz == null) {
			this.clzz =  obj.getClass().getName();
		}
		return this.clzz;
	}
	public void setClzz(String clzz) {
		this.clzz = clzz;
	}

	public Object getObj() {
//		if (Objects.nonNull(obj) ){
//			if (this.obj instanceof JSON) {
//				this.obj = JsonX.toObject(obj, clzz);
//			}
//		}
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}

	public T get(){
		if (this.clzz == null)
			throw new RuntimeException("clzz is null");
		try {
			Class<T> clz = (Class<T>)Class.forName(this.clzz);
			return JsonWrapper.toObject(this.obj,clz);
		}catch (Exception e) {
			throw new RuntimeException(SqliExceptionUtil.getMessage(e));
		}

	}

	@Override
	public String toString() {
		return ""+getObj();
	}

	
}

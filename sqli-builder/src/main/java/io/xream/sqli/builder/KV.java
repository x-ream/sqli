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

import java.io.Serializable;

/**
 * @author Sim
 */
public final class KV implements Serializable{

	private static final long serialVersionUID = -3617796537738183236L;
	public String k;
	public Object v;
	public KV(){}
	public KV(String k, Object v){
		this.k = k;
		this.v = v;
	}
	
	public String getK() {
		return k;
	}
	public void setK(String k) {
		this.k = k;
	}
	public Object getV() {
		return v;
	}
	public void setV(Object v) {
		this.v = v;
	}

	@Override
	public String toString() {
		return "KV{" +
				"k='" + k + '\'' +
				", v=" + v +
				'}';
	}
}

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

import io.xream.sqli.core.util.SqlStringUtil;

public class TokenedRo implements Tokened{

	private String passportId;//groupId
	private String token;//
	private String passportType;// USER | STAFF
	public long getPassportId() {
		if (SqlStringUtil.isNullOrEmpty(passportId))
			return 0;
		return Long.valueOf(passportId);
	}
	public void setPassportId(String passportId) {
		this.passportId = passportId;
	}
	public String getPassportName(){
		return this.passportId;
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
	public String toString() {
		return "TokenedRo [passportId=" + passportId + ", token=" + token + ", passportType=" + passportType + "]";
	}
}

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
package io.xream.sqli.common.util;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Util {
	
	private Md5Util(){}

	public static String toMD5(String str){   
	    try {   
	        MessageDigest md = MessageDigest.getInstance("MD5");   
	        md.update(str.getBytes());   
	        byte[] byteDigest = md.digest();   
	        StringBuffer buf = new StringBuffer("");  
	        int i = 0;
	        for (int offset = 0; offset < byteDigest.length; offset++) {   
	            i = byteDigest[offset];   
	            if (i < 0)   
	               i += 256;
	           if (i < 16)   
	               buf.append("0");   
	           buf.append(Integer.toHexString(i));   
	        }
	        return buf.toString();
	        //return buf.toString().substring(8, 24);    
	    } catch (NoSuchAlgorithmException e) {   
	        e.printStackTrace();
	    }
		return null;
	}  


}

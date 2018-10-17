/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qbao.catagent;

import java.security.ProtectionDomain;
import java.util.Properties;

/**
 * @author andersen
 *
 */
public interface ClassPathPreSetProcessor {
	/**
	 * 
	 * @param prop 解析出来的配置信息
	 */
	void initialize(Properties prop);

	/**
	 * untouch
	 * @param className
	 * @param bytes
	 * @param classLoader
	 * @param a protection domain that may be used for defining extraneous classes generated as part of modifying the one passed in
	 * @return
	 */
	byte[] preProcess(String className, byte[] bytes, ClassLoader classLoader, ProtectionDomain protectionDomain);

}

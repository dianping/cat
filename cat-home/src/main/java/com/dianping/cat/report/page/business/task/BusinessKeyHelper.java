/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page.business.task;

import org.unidal.lookup.annotation.Named;

@Named
public class BusinessKeyHelper {

	public final String SPLITTER = ":";

	public String getType(String key) {
		int index = key.lastIndexOf(SPLITTER);
		return key.substring(index + 1);
	}

	public String getBusinessItemId(String key) {
		int first = key.indexOf(SPLITTER);
		int last = key.lastIndexOf(SPLITTER);
		return key.substring(first + 1, last);
	}

	public String getDomain(String key) {
		int index = key.indexOf(SPLITTER);
		return key.substring(0, index);
	}

	public String generateKey(String id, String domain, String type) {
		StringBuilder sb = new StringBuilder();

		sb.append(domain);
		sb.append(SPLITTER);
		sb.append(id);
		sb.append(SPLITTER);
		sb.append(type);

		return sb.toString();
	}
}

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
package com.dianping.cat.report;

import org.unidal.lookup.annotation.Named;

import java.util.concurrent.ConcurrentHashMap;

@Named
public class DomainValidator {

	private ConcurrentHashMap<String, String> m_valids = new ConcurrentHashMap<String, String>();

	public boolean validate(String domain) {
		if (!m_valids.containsKey(domain)) {
			int length = domain.length();
			char c;

			for (int i = 0; i < length; i++) {
				c = domain.charAt(i);

				if (c > 126 || c < 32) {
					return false;
				}
			}
			m_valids.put(domain, domain);
		}
		return true;
	}
}

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
package com.dianping.cat.report.page.problem;

public class LongConfig {
	private int m_sqlThreshold = 50;

	private int m_urlThreshold = 1000;

	private int m_serviceThreshold = 50;

	private int m_cacheThreshold = 10;

	private int m_callThreshold = 50;

	public int getCacheThreshold() {
		return m_cacheThreshold;
	}

	public LongConfig setCacheThreshold(int cacheThreshold) {
		m_cacheThreshold = cacheThreshold;
		return this;
	}

	public int getCallThreshold() {
		return m_callThreshold;
	}

	public LongConfig setCallThreshold(int callThreshold) {
		m_callThreshold = callThreshold;
		return this;
	}

	public int getServiceThreshold() {
		return m_serviceThreshold;
	}

	public LongConfig setServiceThreshold(int serviceThreshold) {
		m_serviceThreshold = serviceThreshold;
		return this;
	}

	public int getSqlThreshold() {
		return m_sqlThreshold;
	}

	public LongConfig setSqlThreshold(int sqlThreshold) {
		m_sqlThreshold = sqlThreshold;
		return this;
	}

	public int getUrlThreshold() {
		return m_urlThreshold;
	}

	public LongConfig setUrlThreshold(int urlThreshold) {
		m_urlThreshold = urlThreshold;
		return this;
	}

}

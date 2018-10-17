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
package com.dianping.cat.consumer.storage.builder;

public class StorageItem {

	private String m_id;

	private String m_type;

	private String m_method;

	private String m_ip;

	private int m_threshold;

	public StorageItem(String id, String type, String method, String ip, int threshold) {
		m_id = id;
		m_type = type;
		m_method = method;
		m_ip = ip;
		m_threshold = threshold;
	}

	public String getId() {
		return m_id;
	}

	public String getIp() {
		return m_ip;
	}

	public String getMethod() {
		return m_method;
	}

	public String getReportId() {
		return m_id + "-" + m_type;
	}

	public int getThreshold() {
		return m_threshold;
	}

	public String getType() {
		return m_type;
	}

}

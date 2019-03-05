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
package com.dianping.cat.system.page.project;

public enum UpdateStatus {

	SUCCESS(200, "success"),

	INTERNAL_ERROR(500, "internal error");

	private int m_code;

	private String m_info;

	private UpdateStatus(int code, String info) {
		m_code = code;
		m_info = info;
	}

	public String getStatusJson() {
		StringBuilder sb = new StringBuilder();

		sb.append("{\"status\":").append(m_code).append(", \"info\":\"").append(m_info).append("\"}");
		return sb.toString();
	}

}

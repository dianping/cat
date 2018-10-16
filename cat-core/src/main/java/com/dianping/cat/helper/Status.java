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
package com.dianping.cat.helper;

public enum Status {

	NO_MAP(0),
	// 原始日志没有被混淆

	NOT_MAPPED(1),
	// 原始日志被混淆，还没有反混淆

	MAPPING(2),
	// 正在反混淆

	MAPPED(3),
	// 原始日志被混淆，且已经反混淆

	FAILED(4); // 反混淆失败

	private int m_status;

	private Status(int status) {
		m_status = status;
	}

	public int getStatus() {
		return m_status;
	}

}

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
package com.dianping.cat.report.page.monitor;

import com.google.gson.annotations.SerializedName;

public class HttpStatus {

	public static final int SUCCESS = 0;

	public static final int FAIL = -1;

	@SerializedName("statusCode")
	private String m_statusCode;

	@SerializedName("errorMsg")
	private String m_errorMsg;

	public String getErrorMsg() {
		return m_errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		m_errorMsg = errorMsg;
	}

	public String getStatusCode() {
		return m_statusCode;
	}

	public void setStatusCode(String statusCode) {
		m_statusCode = statusCode;
	}

}

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
package com.dianping.cat.system.page.login.service;

import com.dianping.cat.system.page.login.spi.IToken;

public class Token implements IToken {

	public static final String TOKEN = "ct";

	private String m_realName;

	private String m_userName;

	public Token(String realName, String userName) {
		m_realName = realName;
		m_userName = userName;
	}

	@Override
	public String getName() {
		return TOKEN;
	}

	public String getRealName() {
		return m_realName;
	}

	public String getUserName() {
		return m_userName;
	}
}

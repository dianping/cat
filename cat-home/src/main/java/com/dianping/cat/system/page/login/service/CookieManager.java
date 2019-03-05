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

import javax.servlet.http.Cookie;

public class CookieManager {
	protected Cookie createCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);

		cookie.setPath("/");
		return cookie;
	}

	public String getCookie(SigninContext ctx, String name) {
		Cookie[] cookies = ctx.getRequest().getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	public void removeCookie(SigninContext ctx, String name) {
		Cookie cookie = createCookie(name, null);

		cookie.setMaxAge(0);
		ctx.getResponse().addCookie(cookie);
	}

	public void setCookie(SigninContext ctx, String name, String value) {
		Cookie cookie = createCookie(name, value);

		ctx.getResponse().addCookie(cookie);
	}
}

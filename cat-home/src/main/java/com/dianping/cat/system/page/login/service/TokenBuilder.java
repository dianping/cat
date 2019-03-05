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

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import com.dianping.cat.system.page.login.spi.ITokenBuilder;

public class TokenBuilder implements ITokenBuilder<SigninContext, Token> {
	private static final String SP = "|";

	private static final long ONE_DAY = 24 * 60 * 60 * 1000L;

	@Override
	public String build(SigninContext ctx, Token token) {
		StringBuilder sb = new StringBuilder(256);
		String userName = token.getUserName();
		String userNameValue = "";

		try {
			userNameValue = java.net.URLEncoder.encode(userName, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}

		String realName = token.getRealName();
		String value = "";
		try {
			value = java.net.URLEncoder.encode(realName, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		sb.append(value).append(SP);
		sb.append(userNameValue).append(SP);
		sb.append(System.currentTimeMillis()).append(SP);
		sb.append(ctx.getRequest().getRemoteAddr()).append(SP);
		sb.append(getCheckSum(sb.toString()));

		return sb.toString();
	}

	protected int getCheckSum(String str) {
		return str.hashCode();
	}

	@Override
	public Token parse(SigninContext ctx, String value) {
		String[] parts = value.split(Pattern.quote(SP));

		if (parts.length == 5) {
			int index = 0;
			String realName = parts[index++];
			String userName = parts[index++];
			long lastLoginDate = Long.parseLong(parts[index++]);
			String remoteIp = parts[index++];
			int checkSum = Integer.parseInt(parts[index++]);
			int expectedCheckSum = getCheckSum(value.substring(0, value.lastIndexOf(SP) + 1));

			if (checkSum == expectedCheckSum) {
				if (remoteIp.equals(ctx.getRequest().getRemoteAddr())) {
					if (lastLoginDate + ONE_DAY > System.currentTimeMillis()) {
						String realNameValue = "";
						String userNameVaule = "";
						try {
							realNameValue = java.net.URLDecoder.decode(realName, "utf-8");
						} catch (UnsupportedEncodingException e) {
						}
						try {
							userNameVaule = java.net.URLDecoder.decode(userName, "utf-8");
						} catch (UnsupportedEncodingException e) {
						}
						return new Token(realNameValue, userNameVaule);
					}
				}
			}
		}

		return null;
	}
}

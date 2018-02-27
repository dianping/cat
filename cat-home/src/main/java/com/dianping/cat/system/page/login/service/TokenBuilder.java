package com.dianping.cat.system.page.login.service;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import com.dianping.cat.system.page.login.spi.ITokenBuilder;
import com.dianping.cat.utils.HttpRequestUtils;

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
		sb.append(HttpRequestUtils.getAddr(ctx.getRequest())).append(SP);
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
				if (remoteIp.equals(HttpRequestUtils.getAddr(ctx.getRequest()))) {
					if (lastLoginDate + ONE_DAY > System.currentTimeMillis()) {
						return new Token( realName, userName);
					}
				}
			}
		}

		return null;
	}
}

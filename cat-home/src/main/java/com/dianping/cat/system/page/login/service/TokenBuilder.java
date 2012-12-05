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

		String realName = token.getRealName();
		String value = "";
		try {
			value = java.net.URLEncoder.encode(realName, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		sb.append(value).append(SP);
		sb.append(token.getMemberId()).append(SP);
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
			int memberId = Integer.parseInt(parts[index++]);
			long lastLoginDate = Long.parseLong(parts[index++]);
			String remoteIp = parts[index++];
			int checkSum = Integer.parseInt(parts[index++]);
			int expectedCheckSum = getCheckSum(value.substring(0, value.lastIndexOf(SP) + 1));

			if (checkSum == expectedCheckSum) {
				if (remoteIp.equals(ctx.getRequest().getRemoteAddr())) {
					if (lastLoginDate + ONE_DAY > System.currentTimeMillis()) {
						return new Token(memberId, realName);
					}
				}
			}
		}

		return null;
	}
}

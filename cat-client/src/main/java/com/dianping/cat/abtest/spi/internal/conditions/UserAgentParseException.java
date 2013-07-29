package com.dianping.cat.abtest.spi.internal.conditions;

public class UserAgentParseException extends RuntimeException {

	private static final long serialVersionUID = 7475386088154459364L;

	public UserAgentParseException(String message) {
		super(message);
	}
}
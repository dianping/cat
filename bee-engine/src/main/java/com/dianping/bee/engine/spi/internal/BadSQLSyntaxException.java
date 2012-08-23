package com.dianping.bee.engine.spi.internal;

public class BadSQLSyntaxException extends RuntimeException {
	private static final long serialVersionUID = 4674438101686847844L;

	public BadSQLSyntaxException(String message) {
		super(message);
	}

	public BadSQLSyntaxException(String pattern, Object... args) {
		super(String.format(pattern, args));
	}
}

package com.dianping.cat.component;

import com.dianping.cat.apiguardian.api.API;

@API(status = API.Status.INTERNAL, since = "3.1")
public class ComponentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ComponentException(String message, Object... args) {
		super(String.format(message, args));
	}

	public ComponentException(Throwable cause, String message, Object... args) {
		super(String.format(message, args), cause);
	}
}

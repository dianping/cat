package com.dianping.dog.lifecycle;


public class LifeCycleException extends Exception {

	private static final long	serialVersionUID	= -1647902659395829435L;

	public LifeCycleException() {
		super();
	}

	public LifeCycleException(String message) {
		super(message);
	}

	public LifeCycleException(Throwable throwable) {
		super(throwable);
	}

	public LifeCycleException(String message, Throwable throwable) {
		super(message, throwable);
	}
}

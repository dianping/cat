package com.dianping.dog.lifecycle;


public interface LifeCycle {
	public void init() throws LifeCycleException;

	public void start() throws LifeCycleException;

	public void stop() throws LifeCycleException;
}

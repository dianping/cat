package com.dianping.cat.report;

public interface ReportDelegate<T> {
	public T parse(String xml) throws Exception;

	public T make(String domain, long startTime, long duration);

	public T merge(T old, T other);
}
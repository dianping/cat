package com.dianping.cat.service;

import java.util.Map;

public interface ReportDelegate<T> {
	public void afterLoad(Map<String, T> reports);

	public void beforeSave(Map<String, T> reports);

	public byte[] buildBinary(T report);
	
	public T parseBinary(byte[] bytes);
	
	public String buildXml(T report);

	public String getDomain(T report);

	public T makeReport(String domain, long startTime, long duration);

	public T mergeReport(T old, T other);

	public T parseXml(String xml) throws Exception;
	
	public boolean createHourlyTask(T report);
}
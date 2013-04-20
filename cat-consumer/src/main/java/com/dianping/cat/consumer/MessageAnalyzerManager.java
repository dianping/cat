package com.dianping.cat.consumer;

public interface MessageAnalyzerManager {
	public MessageAnalyzer getAnalyzer(String name, long startTime);
}

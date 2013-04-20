package com.dianping.cat.consumer;

public interface MessageAnalyzerFactory {
	public MessageAnalyzer create(String name, long start, long duration, long extraTime);
}
package com.dianping.cat.consumer;

import java.util.List;

public interface MessageAnalyzerManager {
	public List<String> getAnalyzerNames();

	public MessageAnalyzer getAnalyzer(String name, long startTime);
}

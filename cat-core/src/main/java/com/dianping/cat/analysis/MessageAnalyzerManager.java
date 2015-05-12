package com.dianping.cat.analysis;

import java.util.List;

public interface MessageAnalyzerManager {
	public List<String> getAnalyzerNames();

	public List<MessageAnalyzer> getAnalyzer(String name, long startTime);
}

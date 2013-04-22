package com.dianping.cat.consumer;

import java.util.Arrays;
import java.util.List;

import com.dianping.cat.consumer.OneAnalyzerTwoDurationTest.MockAnalyzer;

public class OneAnalyzerMockManager implements MessageAnalyzerManager {
	@Override
	public List<String> getAnalyzerNames() {
		return Arrays.asList("mock");
	}

	@Override
	public MessageAnalyzer getAnalyzer(String name, long startTime) {
		if (name.equals("mock")) {
			MockAnalyzer analyzer = new OneAnalyzerTwoDurationTest.MockAnalyzer();
			return analyzer;
		}

		return null;
	}
}

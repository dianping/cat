package com.dianping.cat.consumer;

import com.dianping.cat.consumer.OneAnalyzerTwoDurationTest.MockAnalyzer;

public class OneAnalyzerMockFactory implements MessageAnalyzerFactory {
	@Override
	public MessageAnalyzer create(String name, long start, long duration, long extraTime) {
		if (name.equals("mock")) {
			MockAnalyzer analyzer = new OneAnalyzerTwoDurationTest.MockAnalyzer();
			return analyzer;
		}
		return null;
	}
}

package com.dianping.cat.consumer.demo;

import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.consumer.MessageAnalyzerFactory;
import com.dianping.cat.consumer.demo.OneAnalyzerTwoDurationTest.MockAnalyzer;

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

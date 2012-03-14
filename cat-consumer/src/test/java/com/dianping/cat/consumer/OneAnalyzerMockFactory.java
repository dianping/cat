package com.dianping.cat.consumer;

import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.OneAnalyzerTwoDurationTest.MockAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;

public class OneAnalyzerMockFactory implements AnalyzerFactory {
	public MessageAnalyzer create(String name, long start, long duration, long extraTime) {
		if (name.equals("mock")) {
			MockAnalyzer analyzer = new OneAnalyzerTwoDurationTest.MockAnalyzer();
			return analyzer;
		}
		return null;
	}

	@Override
	public void release(Object component) {

	}
}

package com.dianping.cat.consumer;

import com.dianping.cat.consumer.ManyAnalyzerTest.MockAnalyzer1;
import com.dianping.cat.consumer.ManyAnalyzerTest.MockAnalyzer2;
import com.dianping.cat.consumer.ManyAnalyzerTest.MockAnalyzer3;
import com.dianping.cat.message.spi.MessageAnalyzer;

public class ManyAnalyerMockFactory implements AnalyzerFactory {

	public MessageAnalyzer create(String name, long start, long duration, long extraTime) {
		if (name.equals("mock1")) {
			MockAnalyzer1 analyzer = new MockAnalyzer1();
			return analyzer;
		} else if (name.equals("mock2")) {
			MockAnalyzer2 analyzer = new MockAnalyzer2();
			return analyzer;
		} else if (name.equals("mock3")) {
			MockAnalyzer3 analyzer = new MockAnalyzer3();
			return analyzer;
		}
		return null;
	}

	@Override
	public void release(Object component) {

	}
}

package com.dianping.cat.consumer.demo;

import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.consumer.MessageAnalyzerFactory;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer1;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer2;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer3;

public class ManyAnalyerMockFactory implements MessageAnalyzerFactory {
	@Override
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
}
